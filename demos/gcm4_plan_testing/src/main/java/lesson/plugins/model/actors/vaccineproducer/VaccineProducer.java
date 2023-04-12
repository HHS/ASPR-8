package lesson.plugins.model.actors.vaccineproducer;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.actors.antigenproducer.MaterialReceiptPlanData;
import lesson.plugins.model.actors.antigenproducer.StagePlanData;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialManufactureSpecification;
import lesson.plugins.model.support.Resource;
import nucleus.ActorContext;
import nucleus.Plan;
import nucleus.PlanData;
import nucleus.PrioritizedPlanData;
import plugins.globalproperties.datamanagers.GlobalPropertiesDataManager;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

public final class VaccineProducer {

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

	private final int stageCapacity = 15;

	private final double vaccinePreparationTime = 2.0;

	private final long vaccineUnits = 50;

	private final double batchAssemblyDuration = 0.1;

	private double lastBatchAssemblyEndTime;

	private BatchId antigenBatchId;

	private final double antigenAmountPerBatch = 50;

	private final long vaccineCapacity = 100;

	private final VaccineProducerPluginData vaccineProducerPluginData;

	public VaccineProducer(VaccineProducerPluginData vaccineProducerPluginData) {
		this.vaccineProducerPluginData = vaccineProducerPluginData;
	}

	private void initializeMaterialRecsFromEmpty() {
		BatchConstructionInfo batchConstructionInfo = //
				BatchConstructionInfo	.builder()//
										.setMaterialId(Material.ADJUVANT)//
										.setMaterialsProducerId(materialsProducerId)//
										.build();//
		BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
		MaterialManufactureSpecification materialManufactureSpecification = //
				MaterialManufactureSpecification.builder()//
												.setDeliveryAmount(150.0)//
												.setDeliveryDelay(3.0)//
												.setStageAmount(2.7)//
												.setMaterialId(Material.ADJUVANT)//
												.setBatchId(batchId)//
												.setOnOrder(false)//
												.build();
		materialRecs.put(Material.ADJUVANT, materialManufactureSpecification);

		batchConstructionInfo = BatchConstructionInfo	.builder()//
														.setMaterialId(Material.PRESERVATIVE)//
														.setMaterialsProducerId(materialsProducerId)//
														.build();//
		batchId = materialsDataManager.addBatch(batchConstructionInfo);
		materialManufactureSpecification = MaterialManufactureSpecification	.builder()//
																			.setDeliveryAmount(1000.0)//
																			.setDeliveryDelay(14.0)//
																			.setStageAmount(3.0)//
																			.setMaterialId(Material.PRESERVATIVE)//
																			.setBatchId(batchId)//
																			.setOnOrder(false)//
																			.build();
		materialRecs.put(Material.PRESERVATIVE, materialManufactureSpecification);

		batchConstructionInfo = BatchConstructionInfo	.builder()//
														.setMaterialId(Material.STABILIZER)//
														.setMaterialsProducerId(materialsProducerId)//
														.build();//
		batchId = materialsDataManager.addBatch(batchConstructionInfo);
		materialManufactureSpecification = MaterialManufactureSpecification	.builder()//
																			.setDeliveryAmount(100.0)//
																			.setDeliveryDelay(14.0)//
																			.setStageAmount(1.0)//
																			.setMaterialId(Material.STABILIZER)//
																			.setBatchId(batchId)//
																			.setOnOrder(false)//
																			.build();
		materialRecs.put(Material.STABILIZER, materialManufactureSpecification);

	}

	private void initializeMaterialRecsFromPreviousRun() {
		Set<Material> materials = new LinkedHashSet<>();
		materials.add(Material.ADJUVANT);
		materials.add(Material.PRESERVATIVE);
		materials.add(Material.STABILIZER);

		for (Material material : materials) {

			MaterialManufactureSpecification materialManufactureSpecification = //
					MaterialManufactureSpecification.builder()//
													.setDeliveryAmount(vaccineProducerPluginData.getDeliveryAmount(material))//
													.setDeliveryDelay(vaccineProducerPluginData.getDeliveryDelay(material))//
													.setStageAmount(vaccineProducerPluginData.getStageAmount(material))//
													.setMaterialId(material)//
													.setBatchId(vaccineProducerPluginData.getMaterialBatchId(material))//
													.setOnOrder(vaccineProducerPluginData.getOrderStatus(material))//
													.build();
			materialRecs.put(material, materialManufactureSpecification);
		}

	}

	private void captureStage(final StageId stageId) {

		materialsDataManager.transferOfferedStage(stageId, materialsProducerId);
		final List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
		for (final BatchId batchId : batches) {
			final MaterialId material = materialsDataManager.getBatchMaterial(batchId);
			if (material.equals(Material.ANTIGEN)) {
				final double amount = materialsDataManager.getBatchAmount(batchId);
				log("adding", amount + "to antigent batch from captured stage");
				materialsDataManager.transferMaterialBetweenBatches(batchId, antigenBatchId, amount);
			}
		}
		materialsDataManager.removeStage(stageId, true);
	}

	private void endVaccinePreparation(final StageId stageId) {
		log("producing vaccine from stage", stageId, "yielding", vaccineUnits, "of vaccine");
		materialsDataManager.convertStageToResource(stageId, Resource.VACCINE, vaccineUnits);
		planVaccinePrepartion();
	}

	private void handleStageOfferUpdateEvent(final ActorContext actorContext, final StageOfferUpdateEvent stageOfferUpdateEvent) {
		log("handling stage offer update event for stage", stageOfferUpdateEvent.stageId());
		if (isCapturableStage(stageOfferUpdateEvent)) {
			captureStage(stageOfferUpdateEvent.stageId());
			planVaccinePrepartion();
		}
	}

	private boolean hasSufficientMaterialsForNewStage() {
		boolean result = true;
		for (final MaterialId materialId : materialRecs.keySet()) {
			final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
			final double batchAmount = materialsDataManager.getBatchAmount(materialManufactureSpecification.getBatchId());
			if (batchAmount < materialManufactureSpecification.getStageAmount()) {
				log("insufficient " + materialId);
				result &= false;
			}
		}

		boolean sufficientAntigen = materialsDataManager.getBatchAmount(antigenBatchId) >= antigenAmountPerBatch;
		if (!sufficientAntigen) {
			log("insufficient antigen level");
		}
		result &= sufficientAntigen;
		if (result) {
			log("has sufficient materials for new stage =", result);
		}

		return result;

	}

	public void init(final ActorContext actorContext) {

		this.actorContext = actorContext;

		materialsProducerId = vaccineProducerPluginData.getMaterialsProducerId();

		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		globalPropertiesDataManager = actorContext.getDataManager(GlobalPropertiesDataManager.class);

		if (actorContext.stateRecordingIsScheduled()) {
			actorContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		actorContext.subscribe(materialsDataManager.getEventFilterForStageOfferUpdateEvent(), this::handleStageOfferUpdateEvent);
		actorContext.subscribe(globalPropertiesDataManager.getEventFilterForGlobalPropertyUpdateEvent(GlobalProperty.MANUFACTURE_VACCINE), (c, e) -> planVaccinePrepartion());

		if (actorContext.getTime() == 0) {
			initializeMaterialRecsFromEmpty();

			final BatchConstructionInfo batchConstructionInfo = //
					BatchConstructionInfo	.builder()//
											.setMaterialId(Material.ANTIGEN)//
											.setMaterialsProducerId(materialsProducerId)//
											.setAmount(1000000)//
											.build();//
			antigenBatchId = materialsDataManager.addBatch(batchConstructionInfo);

			planVaccinePrepartion();
		} else {
			initializeMaterialRecsFromPreviousRun();
			antigenBatchId = vaccineProducerPluginData.getAntigenBatchId();
			lastBatchAssemblyEndTime = vaccineProducerPluginData.getLastBatchAssemblyEndTime();

			List<PrioritizedPlanData> prioritizedPlanDatas = vaccineProducerPluginData.getPrioritizedPlanDatas(MaterialReceiptPlanData.class);
			for (PrioritizedPlanData prioritizedPlanData : prioritizedPlanDatas) {
				MaterialReceiptPlanData materialReceiptPlanData = prioritizedPlanData.getPlanData();
				Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
												.setTime(materialReceiptPlanData.getDeliveryTime())//
												.setCallbackConsumer((c) -> receiveMaterial(materialReceiptPlanData.getMaterialId(), materialReceiptPlanData.getAmount()))//
												.setPlanData(materialReceiptPlanData)//
												.setPriority(prioritizedPlanData.getPriority()).build();
				actorContext.addPlan(plan);
			}

			prioritizedPlanDatas = vaccineProducerPluginData.getPrioritizedPlanDatas(StagePlanData.class);
			for (PrioritizedPlanData prioritizedPlanData : prioritizedPlanDatas) {
				StagePlanData stagePlanData = prioritizedPlanData.getPlanData();
				Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
												.setTime(stagePlanData.getPlanTime())//
												.setCallbackConsumer((c) -> endVaccinePreparation(stagePlanData.getStageId()))//
												.setPlanData(stagePlanData)//
												.setPriority(prioritizedPlanData.getPriority()).build();
				actorContext.addPlan(plan);
			}
		}

	}

	private void recordSimulationState(ActorContext actorContext) {
		VaccineProducerPluginData.Builder builder = VaccineProducerPluginData.builder();
		List<PrioritizedPlanData> prioritizedPlanDatas = actorContext.getTerminalActorPlanDatas(PlanData.class);
		for (PrioritizedPlanData prioritizedPlanData : prioritizedPlanDatas) {
			builder.addPrioritizedPlanData(prioritizedPlanData);
		}
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
		builder.setAntigenBatchId(antigenBatchId);

		actorContext.releaseOutput(builder.build());
	}

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

	private void planVaccinePrepartion() {
		final Boolean continueManufature = globalPropertiesDataManager.getGlobalPropertyValue(GlobalProperty.MANUFACTURE_VACCINE);
		if (!continueManufature) {
			return;
		}
		orderMaterials();

		while (!stagesAtCapacity() && hasSufficientMaterialsForNewStage() && vaccineLevelBelowCapacity()) {
			final StageId stageId = materialsDataManager.addStage(materialsProducerId);
			for (final MaterialId materialId : materialRecs.keySet()) {
				final MaterialManufactureSpecification materialManufactureSpecification = materialRecs.get(materialId);
				final BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).build());
				materialsDataManager.transferMaterialBetweenBatches(materialManufactureSpecification.getBatchId(), newBatchId, materialManufactureSpecification.getStageAmount());
				materialsDataManager.moveBatchToStage(newBatchId, stageId);
			}

			BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(Material.ANTIGEN).build());
			materialsDataManager.transferMaterialBetweenBatches(antigenBatchId, newBatchId, antigenAmountPerBatch);
			materialsDataManager.moveBatchToStage(newBatchId, stageId);

			final double batchAssemblyStartTime = FastMath.max(actorContext.getTime(), lastBatchAssemblyEndTime);
			final double fermentationStartTime = batchAssemblyStartTime + batchAssemblyDuration;
			lastBatchAssemblyEndTime = fermentationStartTime;
			final double planTime = fermentationStartTime + vaccinePreparationTime;

			StagePlanData stagePlanData = new StagePlanData(stageId, planTime);
			log("planning vaccine production via", stageId, "for time =", planTime);
			Plan<ActorContext> plan = Plan	.builder(ActorContext.class)//
											.setCallbackConsumer((c) -> endVaccinePreparation(stageId))//
											.setPlanData(stagePlanData)//
											.setTime(planTime)//
											.build();

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

		planVaccinePrepartion();
	}

	private boolean stagesAtCapacity() {
		final List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		boolean result = stages.size() >= stageCapacity;
		log("stages at capacity =", result);
		return result;
	}

	private boolean vaccineLevelBelowCapacity() {
		final long vaccineCount = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, Resource.VACCINE);
		return vaccineCount <= vaccineCapacity;
	}

}
