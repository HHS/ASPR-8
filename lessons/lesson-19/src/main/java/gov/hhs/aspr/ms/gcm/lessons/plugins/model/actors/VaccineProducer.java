package gov.hhs.aspr.ms.gcm.lessons.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Material;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.MaterialManufactureSpecification;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Resource;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.StageOfferUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchConstructionInfo;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.StageId;

public final class VaccineProducer {

	private ActorContext actorContext;

	private final MaterialsProducerId materialsProducerId;

	private MaterialsDataManager materialsDataManager;

	private GlobalPropertiesDataManager globalPropertiesDataManager;

	private final Map<MaterialId, MaterialManufactureSpecification> materialRecs = new LinkedHashMap<>();

	private final int stageCapacity = 15;

	private final double vaccinePreparationTime = 2.0;

	private final long vaccineUnits = 50;

	private final double batchAssemblyDuration = 0.1;

	private double lastBatchAssemblyEndTime;

	private BatchId antigenBatchId;

	private final double antigenAmountPerBatch = 50;

	private final long vaccineCapacity = 100;

	public VaccineProducer(final MaterialsProducerId materialsProducerId) {
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

	private void captureStage(final StageId stageId) {

		materialsDataManager.transferOfferedStage(stageId, materialsProducerId);
		final List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
		for (final BatchId batchId : batches) {
			final MaterialId material = materialsDataManager.getBatchMaterial(batchId);
			if (material.equals(Material.ANTIGEN)) {
				final double amount = materialsDataManager.getBatchAmount(batchId);
				materialsDataManager.transferMaterialBetweenBatches(batchId, antigenBatchId, amount);
			}
		}
		materialsDataManager.removeStage(stageId, true);
	}

	/* start code_ref=materials_plugin_vaccine_producer_end_vaccine_preparation|code_cap=When a vaccine production stage is ready for release, the vaccine producer converts the stage doses of vaccine and places them in its resource inventory.*/
	private void endVaccinePreparation(final StageId stageId) {
		materialsDataManager.convertStageToResource(stageId, Resource.VACCINE, vaccineUnits);
		planVaccinePrepartion();
	}
	/* end */

	private void handleStageOfferUpdateEvent(final ActorContext actorContext,
			final StageOfferUpdateEvent stageOfferUpdateEvent) {
		if (isCapturableStage(stageOfferUpdateEvent)) {
			captureStage(stageOfferUpdateEvent.stageId());
			planVaccinePrepartion();
		}
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

		return materialsDataManager.getBatchAmount(antigenBatchId) >= antigenAmountPerBatch;

	}

	/* start code_ref=materials_plugin_vaccine_producer_init|code_cap=The vaccine producer initializes by subscribing to offered stages(from the antigen producer) and subscribing to the start of vaccine manufacture.*/
	public void init(final ActorContext actorContext) {
		this.actorContext = actorContext;
		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		final BatchConstructionInfo batchConstructionInfo = //
				BatchConstructionInfo.builder()//
						.setMaterialId(Material.ANTIGEN)//
						.setMaterialsProducerId(materialsProducerId)//
						.build();//
		antigenBatchId = materialsDataManager.addBatch(batchConstructionInfo);

		addMaterialRec(Material.ADJUVANT, MaterialManufactureSpecification.builder()//
				.setDeliveryAmount(150.0)//
				.setDeliveryDelay(3.0)//
				.setStageAmount(2.7));//

		addMaterialRec(Material.PRESERVATIVE, MaterialManufactureSpecification.builder()//
				.setDeliveryAmount(1000.0)//
				.setDeliveryDelay(14.0)//
				.setStageAmount(3.0));//

		addMaterialRec(Material.STABILIZER, MaterialManufactureSpecification.builder()//
				.setDeliveryAmount(100.0)//
				.setDeliveryDelay(14.0)//
				.setStageAmount(1.0));//

		actorContext.subscribe(materialsDataManager.getEventFilterForStageOfferUpdateEvent(),
				this::handleStageOfferUpdateEvent);
		actorContext.subscribe(globalPropertiesDataManager.getEventFilterForGlobalPropertyUpdateEvent(
				GlobalProperty.MANUFACTURE_VACCINE), (c, e) -> planVaccinePrepartion());

		planVaccinePrepartion();
	}
	/* end */

	private boolean isCapturableStage(final StageOfferUpdateEvent stageOfferUpdateEvent) {
		// the stage must be offered
		if (!stageOfferUpdateEvent.currentOfferState()) {
			return false;
		}

		// the stage must be from a materials producer not managed by this actor
		final StageId stageId = stageOfferUpdateEvent.stageId();
		final MaterialsProducerId producerId = materialsDataManager.getStageProducer(stageId);
		if (materialsProducerId.equals(producerId)) {
			return false;
		}

		// the stage must contain antigen
		final List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
		double antigenLevel = 0;
		for (final BatchId batchId : batches) {
			final MaterialId material = materialsDataManager.getBatchMaterial(batchId);
			if (material.equals(Material.ANTIGEN)) {
				antigenLevel += materialsDataManager.getBatchAmount(batchId);
			}
		}
		if (antigenLevel == 0) {
			return false;
		}

		// there must be room for new stages
		final List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		if (stages.size() >= stageCapacity) {
			return false;
		}

		// there must be a need for more antigen to reach stage capacity
		double requiredAmount = stageCapacity - stages.size();
		requiredAmount *= antigenAmountPerBatch;
		final double currentAmount = materialsDataManager.getBatchAmount(antigenBatchId);
		requiredAmount -= currentAmount;
		if (requiredAmount <= 0) {
			return false;
		}

		return true;

	}

	private void orderMaterial(final MaterialId materialId) {
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		if (materialRec.isOnOrder()) {
			return;
		}

		double requiredAmount = materialRec.getStageAmount() * stageCapacity;
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

	/* start code_ref=materials_plugin_vaccine_producer_plan_vaccine_preparation|code_cap=Responding to events that may allow for additional manufacture of vaccine doses, the vaccine producer attempts to continue manufacturing.*/
	private void planVaccinePrepartion() {
		final Boolean continueManufature = globalPropertiesDataManager
				.getGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE);
		if (!continueManufature) {
			return;
		}
		orderMaterials();

		while (!stagesAtCapacity() && hasSufficientMaterialsForNewStage() && vaccineLevelBelowCapacity()) {
			final StageId stageId = materialsDataManager.addStage(materialsProducerId);
			for (final MaterialId materialId : materialRecs.keySet()) {
				final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
				final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder()
						.setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).build());
				materialsDataManager.transferMaterialBetweenBatches(materialManufactureSpecification.getBatchId(),
						newBatchId, materialManufactureSpecification.getStageAmount());
				materialsDataManager.moveBatchToStage(newBatchId, stageId);
			}

			BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder()
					.setMaterialsProducerId(materialsProducerId).setMaterialId(Material.ANTIGEN).build());
			materialsDataManager.transferMaterialBetweenBatches(antigenBatchId, newBatchId, antigenAmountPerBatch);
			materialsDataManager.moveBatchToStage(newBatchId, stageId);

			final double batchAssemblyStartTime = FastMath.max(actorContext.getTime(), lastBatchAssemblyEndTime);
			final double fermentationStartTime = batchAssemblyStartTime + batchAssemblyDuration;
			lastBatchAssemblyEndTime = fermentationStartTime;
			final double planTime = fermentationStartTime + vaccinePreparationTime;
			actorContext.addPlan((c) -> endVaccinePreparation(stageId), planTime);
		}
	}
	/* end */

	private void receiveMaterial(final MaterialId materialId, final double amount) {
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		materialRec.toggleOnOrder();

		final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder()
				.setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).setAmount(amount).build());
		materialsDataManager.transferMaterialBetweenBatches(newBatchId, materialRec.getBatchId(), amount);
		materialsDataManager.removeBatch(newBatchId);

		planVaccinePrepartion();
	}

	private boolean stagesAtCapacity() {
		final List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		return stages.size() >= stageCapacity;
	}

	private boolean vaccineLevelBelowCapacity() {
		final long vaccineCount = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId,
				Resource.VACCINE);
		return vaccineCount <= vaccineCapacity;
	}

}
