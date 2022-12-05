package lesson.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.FastMath;

import lesson.plugins.model.support.AntigenMaterialRec;
import lesson.plugins.model.support.Material;
import nucleus.ActorContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.StageMaterialsProducerUpdateEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

public final class AntigenProducer {

	private void addMaterialRec(MaterialId materialId, AntigenMaterialRec.Builder builder) {
		BatchConstructionInfo batchConstructionInfo = BatchConstructionInfo	.builder()//
																			.setMaterialId(materialId)//
																			.setMaterialsProducerId(materialsProducerId)//
																			.build();//

		BatchId batchId = materialsDataManager.addBatch(batchConstructionInfo);
		builder.setBatchId(batchId);
		builder.setMaterialId(materialId);
		AntigenMaterialRec antigenMaterialRec = builder.build();
		materialRecs.put(materialId, antigenMaterialRec);
	}

	private AntigenMaterialRec getMaterialRec(MaterialId materialId) {
		return materialRecs.get(materialId);
	}

	private Map<MaterialId, AntigenMaterialRec> materialRecs = new LinkedHashMap<>();

	private int stageOfferCapacity = 5;
	private double fermentationTime = 15.0;
	private double antigenUnits = 200.0;
	private double batchAssemblyDuration = 0.3;
	private double lastBatchAssemblyEndTime;

	private final MaterialsProducerId materialsProducerId;
	private MaterialsDataManager materialsDataManager;
	private ActorContext actorContext;

	public AntigenProducer(MaterialsProducerId materialsProducerId) {
		this.materialsProducerId = materialsProducerId;
	}

	public void init(ActorContext actorContext) {
		this.actorContext = actorContext;
		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);

		addMaterialRec(Material.GROWTH_MEDIUM, AntigenMaterialRec	.builder()//
																	.setDeliveryAmount(3.5)//
																	.setDeliveryDelay(7.0)//
																	.setStageAmount(1.0));//

		addMaterialRec(Material.VIRUS, AntigenMaterialRec	.builder()//
															.setDeliveryAmount(2.0)//
															.setDeliveryDelay(21.0)//
															.setStageAmount(1.0));//

		orderMaterials();

		actorContext.subscribe(materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId), this::handleStageMaterialsProducerUpdateEvent);

	}
	
	private void handleStageMaterialsProducerUpdateEvent(ActorContext actorContext, StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent) {
		System.out.println("AntigenProducer.handleStageMaterialsProducerUpdateEvent()");
		if(actorContext.getTime()>110) {
			System.out.println("AntigenProducer.handleStageMaterialsProducerUpdateEvent()");
		}
		orderMaterials();
	}
	
	

	private void orderMaterials() {
		for (MaterialId materialId : materialRecs.keySet()) {
			orderMaterial(materialId);
		}
	}

	private void orderMaterial(MaterialId materialId) {
		AntigenMaterialRec materialRec = getMaterialRec(materialId);
		if (materialRec.isOnOrder()) {
			return;
		}

		List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		if (stages.size() >= stageOfferCapacity) {
			return;
		}

		double requiredGrowthMedium = (stageOfferCapacity - stages.size()) * materialRec.getStageAmount();
		List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, Material.GROWTH_MEDIUM);
		double currentGrowthMediumLevel = 0;
		for (BatchId batchId : batches) {
			currentGrowthMediumLevel += materialsDataManager.getBatchAmount(batchId);
		}
		double growthMediumToOrder = requiredGrowthMedium - currentGrowthMediumLevel;
		if (growthMediumToOrder <= 0) {
			return;
		}

		growthMediumToOrder = FastMath.ceil(growthMediumToOrder / materialRec.getDeliveryAmount()) * materialRec.getDeliveryAmount();
		double deliveryTime = materialRec.getDeliveryDelay() + actorContext.getTime();
		double amount = growthMediumToOrder;
		materialRec.toggleOnOrder();
		actorContext.addPlan((c) -> receiveMaterial(materialId, amount), deliveryTime);

	}

	private void receiveMaterial(MaterialId materialId, double amount) {
		System.out.println("AntigenProducer.receiveMaterial() at "+actorContext.getTime());
		AntigenMaterialRec materialRec = getMaterialRec(materialId);
		materialRec.toggleOnOrder();

		BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).setAmount(amount).build());
		materialsDataManager.transferMaterialBetweenBatches(newBatchId, materialRec.getBatchId(), amount);
		materialsDataManager.removeBatch(newBatchId);

		planFermentation();

		orderMaterial(materialId);
	}
	
	private boolean stagesAtCapacity() {
		List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		return stages.size() >= stageOfferCapacity;
	}
	
	private boolean hasSufficientMaterialsForNewStage() {
		
		for (MaterialId materialId : materialRecs.keySet()) {
			AntigenMaterialRec antigenMaterialRec = materialRecs.get(materialId);
			double batchAmount = materialsDataManager.getBatchAmount(antigenMaterialRec.getBatchId());
			if (batchAmount < antigenMaterialRec.getStageAmount()) {
				return false;
			}
		}
		return true;
	}

	private void planFermentation() {		

		while (!stagesAtCapacity() && hasSufficientMaterialsForNewStage()) {
				StageId stageId = materialsDataManager.addStage(materialsProducerId);
				for (MaterialId materialId : materialRecs.keySet()) {
					AntigenMaterialRec antigenMaterialRec = materialRecs.get(materialId);
					BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).build());
					materialsDataManager.transferMaterialBetweenBatches(antigenMaterialRec.getBatchId(), newBatchId, antigenMaterialRec.getStageAmount());
					materialsDataManager.moveBatchToStage(newBatchId, stageId);
				}
				double batchAssemblyStartTime = FastMath.max(actorContext.getTime() , lastBatchAssemblyEndTime);
				double fermentationStartTime = batchAssemblyStartTime +  batchAssemblyDuration;
				lastBatchAssemblyEndTime = fermentationStartTime;	
				double planTime = fermentationStartTime+fermentationTime;
				actorContext.addPlan((c) -> endFermentationStage(stageId), planTime);			
		}
	}
	
	private void endFermentationStage(StageId stageId) {
		BatchId batch = materialsDataManager.convertStageToBatch(stageId, Material.ANTIGEN, antigenUnits);
		StageId antigenStage = materialsDataManager.addStage(materialsProducerId);
		materialsDataManager.moveBatchToStage(batch, antigenStage);
		materialsDataManager.setStageOfferState(antigenStage, true);
		System.out.println("AntigenProducer.endFermentationStage() is offering antigen stage " + antigenStage+" at time = "+actorContext.getTime());
	}

}
