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
import util.wrappers.MutableDouble;

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

	private Map<MaterialId, AntigenMaterialRec> materialRecs = new LinkedHashMap<>();
	private int stageCapacity = 25;
	private double fermentationTime = 15.0;
	private double antigenUnits = 200.0;
	private double batchAssemblyDuration = 0.25;
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
																	.setDeliveryAmount(35.0)//
																	.setDeliveryDelay(7.0)//
																	.setStageAmount(1.0));//

		addMaterialRec(Material.VIRUS, AntigenMaterialRec	.builder()//
															.setDeliveryAmount(100.0)//
															.setDeliveryDelay(21.0)//
															.setStageAmount(1.0));//

		planFermentation();

		actorContext.subscribe(materialsDataManager.getEventFilterForStageMaterialsProducerUpdateEvent_BySource(materialsProducerId), this::handleStageMaterialsProducerUpdateEvent);

	}
	
	private void handleStageMaterialsProducerUpdateEvent(ActorContext actorContext, StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent) {
		planFermentation();
	}

	private void orderMaterials() {
		for (MaterialId materialId : materialRecs.keySet()) {
			orderMaterial(materialId);
		}
	}

	private void orderMaterial(MaterialId materialId) {
		AntigenMaterialRec materialRec = materialRecs.get(materialId);
		if (materialRec.isOnOrder()) {
			return;
		}
		

		
		double requiredAmount = materialRec.getStageAmount();
		requiredAmount /= batchAssemblyDuration;
		requiredAmount *= materialRec.getDeliveryDelay();
		
		List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
		double currentAmount = 0;
		for (BatchId batchId : batches) {
			currentAmount += materialsDataManager.getBatchAmount(batchId);
		}
		double amountToOrder = requiredAmount - currentAmount;
		if (amountToOrder <= 0) {
			return;
		}

		amountToOrder = FastMath.ceil(amountToOrder / materialRec.getDeliveryAmount()) * materialRec.getDeliveryAmount();
		double deliveryTime = materialRec.getDeliveryDelay() + actorContext.getTime();
		double amount = amountToOrder;
		materialRec.toggleOnOrder();
		
		System.out.println(actorContext.getTime()+"\t"+"Ordering "+materialId+"("+amount+")");
		actorContext.addPlan((c) -> receiveMaterial(materialId, amount), deliveryTime);

	}

	private void receiveMaterial(MaterialId materialId, double amount) {
		
		AntigenMaterialRec materialRec = materialRecs.get(materialId);
		materialRec.toggleOnOrder();

		BatchId newBatchId = materialsDataManager.addBatch(BatchConstructionInfo.builder().setMaterialsProducerId(materialsProducerId).setMaterialId(materialId).setAmount(amount).build());
		materialsDataManager.transferMaterialBetweenBatches(newBatchId, materialRec.getBatchId(), amount);
		materialsDataManager.removeBatch(newBatchId);
		System.out.println(actorContext.getTime()+"\t"+"Received "+materialId+"("+amount+")");
		planFermentation();

		
	}
	
	private boolean stagesAtCapacity() {
		List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
		return stages.size() >= stageCapacity;
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
		
		orderMaterials();
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
		reportState();
	}
	
	private void endFermentationStage(StageId stageId) {
		
		BatchId batch = materialsDataManager.convertStageToBatch(stageId, Material.ANTIGEN, antigenUnits);
		StageId antigenStage = materialsDataManager.addStage(materialsProducerId);
		materialsDataManager.moveBatchToStage(batch, antigenStage);
		materialsDataManager.setStageOfferState(antigenStage, true);
		System.out.println(actorContext.getTime()+"\t"+" offering stage "+antigenStage);
		reportState();
	}
	
	private void reportState() {
		
//		System.out.println("State at time = "+actorContext.getTime());	
//		Map<MaterialId, MutableDouble> inventory = new LinkedHashMap<>();
//		for(Material material : Material.values()) {
//			inventory.put(material, new MutableDouble());
//		}
//			
//		for(BatchId batchId : materialsDataManager.getInventoryBatches(materialsProducerId)) {
//			MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
//			double batchAmount = materialsDataManager.getBatchAmount(batchId);
//			inventory.get(materialId).increment(batchAmount);
//		}
//		for(Material material : Material.values()) {
//			MutableDouble mutableDouble = inventory.get(material);
//			if(mutableDouble.getValue()>0) {
//				System.out.println("\t"+material+" = "+mutableDouble.getValue());
//			}
//		}
//		List<StageId> stages = materialsDataManager.getStages(materialsProducerId);
//		List<StageId> offeredStages = materialsDataManager.getOfferedStages(materialsProducerId);
//		int nonOfferedStageCount = stages.size()-offeredStages.size();
//		int offeredStageCount = offeredStages.size();
//		System.out.println("\t"+"stages = "+nonOfferedStageCount+":"+offeredStageCount);
		
	}

}
