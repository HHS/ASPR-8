package lesson.plugins.model.actors.antigenproducer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialManufactureSpecification;
import nucleus.ActorContext;
import nucleus.Plan;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageConversionInfo;
import plugins.materials.support.StageId;

public final class AntigenProducer {
	private boolean logActive = true;
	

	private void log(Object... values) {
		if (logActive) {
			StringBuilder sb = new StringBuilder();
			sb.append("Vaccine Producer : ");
			sb.append(actorContext.getTime());
			sb.append(" :");
			for (Object value : values) {
				sb.append(" ");
				sb.append(String.valueOf(value));
			}
			System.out.println(sb);
		}
	}

	private ActorContext actorContext;

	private MaterialsProducerId materialsProducerId;

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

	private AntigenProducerPluginData antigenProducerPluginData;

	public AntigenProducer(AntigenProducerPluginData antigenProducerPluginData) {
		this.antigenProducerPluginData = antigenProducerPluginData;

	}

	private void endFermentationStage(final StageId stageId) {
		log("producing fermentation from stage", stageId, "yielding", antigenUnits, "of antigen");
		final BatchId batch = materialsDataManager.convertStageToBatch(//
				StageConversionInfo	.builder()//
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
			final double batchAmount = materialsDataManager.getBatchAmount(materialManufactureSpecification.getBatchId());
			if (batchAmount < materialManufactureSpecification.getStageAmount()) {
				return false;
			}
		}
		return true;
	}

	private Consumer<ActorContext> getConsumerFromMaterialReceiptPlanData(MaterialReceiptPlanData materialReceiptPlanData) {
		return (c) -> receiveMaterial(materialReceiptPlanData.getMaterialId(), materialReceiptPlanData.getAmount());//
	}

	private Consumer<ActorContext> getConsumerFromStagePlanData(StagePlanData stagePlanData) {

		return (c) -> endFermentationStage(stagePlanData.getStageId());

	}
	
	private void initializeMaterialRecsFromPreviousRun() {
		Set<Material> materials = new LinkedHashSet<>();
		materials.add(Material.GROWTH_MEDIUM);
		materials.add(Material.VIRUS);
		

		for (Material material : materials) {

			MaterialManufactureSpecification materialManufactureSpecification = //
					MaterialManufactureSpecification.builder()//
													.setDeliveryAmount(antigenProducerPluginData.getDeliveryAmount(material))//
													.setDeliveryDelay(antigenProducerPluginData.getDeliveryDelay(material))//
													.setStageAmount(antigenProducerPluginData.getStageAmount(material))//
													.setMaterialId(material)//
													.setBatchId(antigenProducerPluginData.getMaterialBatchId(material))//
													.setOnOrder(antigenProducerPluginData.getOrderStatus(material))//
													.build();
			materialRecs.put(material, materialManufactureSpecification);
		}

	}

	private void initializeMaterialRecsFromEmpty() {
		BatchConstructionInfo batchConstructionInfo = //
				BatchConstructionInfo	.builder()//
										.setMaterialId(Material.GROWTH_MEDIUM)//
										.setMaterialsProducerId(materialsProducerId)//
										.build();//
		BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
		MaterialManufactureSpecification materialManufactureSpecification = //
				MaterialManufactureSpecification.builder()//
												.setDeliveryAmount(35.0)//
												.setDeliveryDelay(7.0)//
												.setStageAmount(1.0)//
												.setMaterialId(Material.GROWTH_MEDIUM)//
												.setBatchId(batchId)//
												.setOnOrder(false)//
												.build();
		materialRecs.put(Material.GROWTH_MEDIUM, materialManufactureSpecification);

		batchConstructionInfo = BatchConstructionInfo	.builder()//
														.setMaterialId(Material.VIRUS)//
														.setMaterialsProducerId(materialsProducerId)//
														.build();//
		batchId = materialsDataManager.addBatch(batchConstructionInfo);
		materialManufactureSpecification = MaterialManufactureSpecification	.builder()//
																			.setDeliveryAmount(100.0)//
																			.setDeliveryDelay(21.0)//
																			.setStageAmount(1.0)//
																			.setMaterialId(Material.VIRUS)//
																			.setBatchId(batchId)//
																			.setOnOrder(false)//
																			.build();
		materialRecs.put(Material.VIRUS, materialManufactureSpecification);

	}


	public void init(final ActorContext actorContext) {
		
		
		this.actorContext = actorContext;

		materialsProducerId = antigenProducerPluginData.getMaterialsProducerId();
		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		actorContext.setPlanDataConverter(MaterialReceiptPlanData.class, this::getConsumerFromMaterialReceiptPlanData);
		actorContext.setPlanDataConverter(StagePlanData.class, this::getConsumerFromStagePlanData);

		// each time a stage is transferred
		actorContext.subscribe(materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId), (c, e) -> planFermentation());

		// each time the manufacture policy is changed
		actorContext.subscribe(globalPropertiesDataManager.getEventFilterForGlobalPropertyUpdateEvent(GlobalProperty.MANUFACTURE_VACCINE), (c, e) -> planFermentation());

		if (actorContext.stateRecordingIsScheduled()) {
			actorContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		if (actorContext.getTime() > 0) {
			initializeMaterialRecsFromPreviousRun();
			for (MaterialId materialId : materialRecs.keySet()) {
				boolean onOrder = antigenProducerPluginData.getOrderStatus(materialId);
				if (onOrder) {
					materialRecs.get(materialId).setIsOnOrder(true);
				}
			}

			lastBatchAssemblyEndTime = antigenProducerPluginData.getLastBatchAssemblyEndTime();

		} else {
			initializeMaterialRecsFromEmpty();
			planFermentation();
		}
	}

	private void recordSimulationState(ActorContext actorContext) {
		AntigenProducerPluginData.Builder builder = AntigenProducerPluginData.builder();

		builder.setLastBatchAssemblyEndTime(lastBatchAssemblyEndTime);
		builder.setMaterialsProducerId(materialsProducerId);
		for (MaterialId materialId : materialRecs.keySet()) {
			builder.addMaterialId(materialId);
			MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
			builder.setOrderStatus(materialId, materialManufactureSpecification.isOnOrder());
			builder.setMaterialBatchId(materialId, materialManufactureSpecification.getBatchId());
			builder.setDeliveryDelay(materialId, materialManufactureSpecification.getDeliveryDelay());
			builder.setDeliveryAmount(materialId, materialManufactureSpecification.getDeliveryAmount());
			builder.setStageAmount(materialId, materialManufactureSpecification.getStageAmount());
		}

		actorContext.releaseOutput(builder.build());
	}

	private void orderMaterial(final MaterialId materialId) {
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		if (materialRec.isOnOrder()) {
			return;
		}

		double requiredAmount = stageCapacity * materialRec.getStageAmount();
		requiredAmount /= batchAssemblyDuration;
		requiredAmount *= materialRec.getDeliveryDelay();

		final List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
		double currentAmount = 0;
		for (final BatchId batchId : batches) {
			currentAmount += materialsDataManager.getBatchAmount(batchId);
		}
		double amountToOrder = requiredAmount - currentAmount;
		if (amountToOrder <= 0) {
			return;
		}

		log("ordering material", materialId);

		amountToOrder = FastMath.ceil(amountToOrder / materialRec.getDeliveryAmount()) * materialRec.getDeliveryAmount();
		final double deliveryTime = materialRec.getDeliveryDelay() + actorContext.getTime();
		final double amount = amountToOrder;
		materialRec.setIsOnOrder(true);

		MaterialReceiptPlanData materialReceiptPlanData = new MaterialReceiptPlanData(materialId, amount, deliveryTime);

		Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
										.setTime(deliveryTime)//
										.setCallbackConsumer((c) -> receiveMaterial(materialId, amount))//
										.setPlanData(materialReceiptPlanData)//
										.build();
		actorContext.addPlan(plan);
	}

	private void orderMaterials() {
		for (final MaterialId materialId : materialRecs.keySet()) {
			orderMaterial(materialId);
		}
	}

	private void planFermentation() {

		final Boolean continueManufature = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE);
		if (!continueManufature) {
			return;
		}
		orderMaterials();

		while (!stagesAtCapacity() && hasSufficientMaterialsForNewStage()) {
			final StageId stageId = materialsDataManager.addStage(materialsProducerId);
			for (final MaterialId materialId : materialRecs.keySet()) {
				final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
				final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).build());
				materialsDataManager.transferMaterialBetweenBatches(materialManufactureSpecification.getBatchId(), newBatchId, materialManufactureSpecification.getStageAmount());
				materialsDataManager.moveBatchToStage(newBatchId, stageId);
			}
			final double batchAssemblyStartTime = FastMath.max(actorContext.getTime(), lastBatchAssemblyEndTime);
			final double fermentationStartTime = batchAssemblyStartTime + batchAssemblyDuration;
			lastBatchAssemblyEndTime = fermentationStartTime;
			final double planTime = fermentationStartTime + fermentationTime;

			log("planning antigen production via", stageId, "for time =", planTime);

			StagePlanData stagePlanData = new StagePlanData(stageId, planTime);
			Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
											.setCallbackConsumer((c) -> endFermentationStage(stageId))//
											.setPlanData(stagePlanData)//
											.setTime(planTime).build();
			actorContext.addPlan(plan);
		}

	}

	private void receiveMaterial(final MaterialId materialId, final double amount) {
		log("receiving material", materialId, amount);
		final MaterialManufactureSpecification materialRec = materialRecs.get(materialId);
		materialRec.setIsOnOrder(false);

		final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).setAmount(amount).build());
		materialsDataManager.transferMaterialBetweenBatches(newBatchId, materialRec.getBatchId(), amount);
		materialsDataManager.removeBatch(newBatchId);
		planFermentation();
	}

	private boolean stagesAtCapacity() {
		final List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		return stages.size() >= stageCapacity;
	}

}
