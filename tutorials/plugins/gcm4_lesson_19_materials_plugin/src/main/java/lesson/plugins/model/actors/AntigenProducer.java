package lesson.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialManufactureSpecification;
import nucleus.ActorContext;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageConversionInfo;
import plugins.materials.support.StageId;

public final class AntigenProducer {
	private ActorContext actorContext;

	private final MaterialsProducerId materialsProducerId;

	private MaterialsDataManager materialsDataManager;

	private GlobalPropertiesDataManager globalPropertiesDataManager;

	private final Map<MaterialId, MaterialManufactureSpecification> materialRecs = new LinkedHashMap<>();

	private final int stageCapacity = 60;// to be non-constraining,
											// stageCapacity =
											// fermentationTime/batchAssemblyDuration

	private final double fermentationTime = 15.0;

	private final double antigenUnits = 25.0;

	private final double batchAssemblyDuration = 0.25;

	private double lastBatchAssemblyEndTime;

	public AntigenProducer(final MaterialsProducerId materialsProducerId) {
		this.materialsProducerId = materialsProducerId;
	}

	private void addMaterialRec(final MaterialId materialId, final MaterialManufactureSpecification.Builder builder) {
		final BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo.builder()//
				.setMaterialId(materialId)//
				.setMaterialsProducerId(materialsProducerId)//
				.build();//
		final BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
		builder.setBatchId(batchId);
		builder.setMaterialId(materialId);
		final MaterialManufactureSpecification materialManufactureSpecification = builder.build();
		materialRecs.put(materialId, materialManufactureSpecification);
	}

	private void endFermentationStage(final StageId stageId) {
		final BatchId batch = materialsDataManager.convertStageToBatch(//
				StageConversionInfo.builder()//
						.setAmount(antigenUnits)//
						.setMaterialId(Material.ANTIGEN)//
						.setStageId(stageId)//
						.build());//

		final StageId antigenStage = materialsDataManager.addStage(materialsProducerId);
		materialsDataManager.moveBatchToStage(batch, antigenStage);
		materialsDataManager.setStageOfferState(antigenStage, true);
		planFermentation();
	}

	private boolean hasSufficientMaterialsForNewStage() {
		for (final MaterialId materialId : materialRecs.keySet()) {
			final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
			final double batchAmount = materialsDataManager
					.getBatchAmount(materialManufactureSpecification.getBatchId());
			if (batchAmount < materialManufactureSpecification.getStageAmount()) {
				return false;
			}
		}
		return true;
	}

	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;
		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);
		addMaterialRec(Material.GROWTH_MEDIUM, MaterialManufactureSpecification.builder()//
				.setDeliveryAmount(35.0)//
				.setDeliveryDelay(7.0)//
				.setStageAmount(1.0));//

		addMaterialRec(Material.VIRUS, MaterialManufactureSpecification.builder()//
				.setDeliveryAmount(100.0)//
				.setDeliveryDelay(21.0)//
				.setStageAmount(1.0));//

		// each time a stage is transferred
		actorContext.subscribe(
				materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId),
				(c, e) -> planFermentation());

		// each time the manufacture policy is changed
		actorContext.subscribe(globalPropertiesDataManager.getEventFilterForGlobalPropertyUpdateEvent(
				GlobalProperty.MANUFACTURE_VACCINE), (c, e) -> planFermentation());

		planFermentation();
	}

	private void orderMaterial(final MaterialId materialId) {
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		if (materialRec.isOnOrder()) {
			return;
		}

		double requiredAmount = stageCapacity * materialRec.getStageAmount();
		requiredAmount /= batchAssemblyDuration;
		requiredAmount *= materialRec.getDeliveryDelay();

		final List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId,
				materialId);
		double currentAmount = 0;
		for (final BatchId batchId : batches) {
			currentAmount += materialsDataManager.getBatchAmount(batchId);
		}
		double amountToOrder = requiredAmount - currentAmount;
		if (amountToOrder <= 0) {
			return;
		}

		amountToOrder = FastMath.ceil(amountToOrder / materialRec.getDeliveryAmount())
				* materialRec.getDeliveryAmount();
		final double deliveryTime = materialRec.getDeliveryDelay() + actorContext.getTime();
		final double amount = amountToOrder;
		materialRec.toggleOnOrder();

		actorContext.addPlan((c) -> receiveMaterial(materialId, amount), deliveryTime);
	}

	private void orderMaterials() {
		for (final MaterialId materialId : materialRecs.keySet()) {
			orderMaterial(materialId);
		}
	}

	private void planFermentation() {

		final Boolean continueManufature = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE);
		if (!continueManufature) {
			return;
		}
		orderMaterials();

		while (!stagesAtCapacity() && hasSufficientMaterialsForNewStage()) {
			final StageId stageId = materialsDataManager.addStage(materialsProducerId);
			for (final MaterialId materialId : materialRecs.keySet()) {
				final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
				final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder()
						.setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).build());
				materialsDataManager.transferMaterialBetweenBatches(materialManufactureSpecification.getBatchId(),
						newBatchId, materialManufactureSpecification.getStageAmount());
				materialsDataManager.moveBatchToStage(newBatchId, stageId);
			}
			final double batchAssemblyStartTime = FastMath.max(actorContext.getTime(), lastBatchAssemblyEndTime);
			final double fermentationStartTime = batchAssemblyStartTime + batchAssemblyDuration;
			lastBatchAssemblyEndTime = fermentationStartTime;
			final double planTime = fermentationStartTime + fermentationTime;
			actorContext.addPlan((c) -> endFermentationStage(stageId), planTime);
		}
	}

	private void receiveMaterial(final MaterialId materialId, final double amount) {
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		materialRec.toggleOnOrder();

		final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder()
				.setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).setAmount(amount).build());
		materialsDataManager.transferMaterialBetweenBatches(newBatchId, materialRec.getBatchId(), amount);
		materialsDataManager.removeBatch(newBatchId);
		planFermentation();
	}

	private boolean stagesAtCapacity() {
		final List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		return stages.size() >= stageCapacity;
	}

}
