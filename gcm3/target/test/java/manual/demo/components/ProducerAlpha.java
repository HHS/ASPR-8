package manual.demo.components;

import manual.demo.identifiers.Material;
import manual.demo.identifiers.MaterialsProducers;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.materials.support.BatchId;
import plugins.materials.support.StageId;

public class ProducerAlpha extends AbstractComponent {

	@Override
	public void init(Environment environment) {
		BatchId batchId = environment.createBatch(Material.MATERIAL_1, 15);
		StageId stageId = environment.createStage();
		environment.moveBatchToStage(batchId, stageId);
		environment.setStageOffer(stageId, true);
		environment.transferOfferedStageToMaterialsProducer(stageId, MaterialsProducers.PRODUCER_BETA);
	}

}
