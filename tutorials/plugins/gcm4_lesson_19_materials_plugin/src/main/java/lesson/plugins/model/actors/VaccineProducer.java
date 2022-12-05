package lesson.plugins.model.actors;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import lesson.plugins.model.support.Material;
import nucleus.ActorContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import util.wrappers.MutableDouble;

public final class VaccineProducer {
	private final MaterialsProducerId materialsProducerId;
	private MaterialsDataManager materialsDataManager;

	public VaccineProducer(MaterialsProducerId materialsProducerId) {
		this.materialsProducerId = materialsProducerId;
	}

	public void init(ActorContext actorContext) {

		materialsDataManager = actorContext.getDataManager(MaterialsDataManager.class);
		actorContext.subscribe(materialsDataManager.getEventFilterForStageOfferUpdateEvent(), this::handleStageOfferUpdateEvent);

	}
	
	private boolean isCapturableStage(StageOfferUpdateEvent stageOfferUpdateEvent) {
		if (stageOfferUpdateEvent.isCurrentOfferState()) {
			StageId stageId = stageOfferUpdateEvent.getStageId();			
			List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
			double antigenLevel = 0;
			for(BatchId batchId : batches) {
				MaterialId material = materialsDataManager.getBatchMaterial(batchId);
				if(material.equals(Material.ANTIGEN)) {
					antigenLevel +=
					materialsDataManager.getBatchAmount(batchId);
				}
			}
			return antigenLevel>0;			
		}
		return false;
		
	}
	
	
	private void captureStage(StageId stageId) {
		System.out.println("VaccineProducer.captureStage() "+stageId);
		materialsDataManager.transferOfferedStage(stageId, materialsProducerId);
		List<BatchId> batches = materialsDataManager.getStageBatches(stageId);
		for(BatchId batchId : batches) {
			MaterialId material = materialsDataManager.getBatchMaterial(batchId);
			if(material.equals(Material.ANTIGEN)) {
				materialsDataManager.moveBatchToInventory(batchId);
			}
		}
		materialsDataManager.removeStage(stageId, true);
	}

	private void handleStageOfferUpdateEvent(ActorContext actorContext, StageOfferUpdateEvent stageOfferUpdateEvent) {
		if(isCapturableStage(stageOfferUpdateEvent)) {
			captureStage(stageOfferUpdateEvent.getStageId());
			reportInventory();
		}
	}
	
	private void reportInventory() {
		System.out.println("VaccineProducer.reportInventory()");	
		Map<MaterialId, MutableDouble> inventory = new LinkedHashMap<>();
		for(Material material : Material.values()) {
			inventory.put(material, new MutableDouble());
		}
			
		for(BatchId batchId : materialsDataManager.getInventoryBatches(materialsProducerId)) {
			MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);
			double batchAmount = materialsDataManager.getBatchAmount(batchId);
			inventory.get(materialId).increment(batchAmount);
		}
		for(Material material : Material.values()) {
			MutableDouble mutableDouble = inventory.get(material);
			if(mutableDouble.getValue()>0) {
				System.out.println(material+" -> "+mutableDouble.getValue());
			}
		}
	}
}
