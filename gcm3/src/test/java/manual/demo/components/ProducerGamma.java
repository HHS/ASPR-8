package manual.demo.components;

import manual.demo.identifiers.Material;
import manual.demo.identifiers.Resource;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
import plugins.materials.support.BatchId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.regions.support.RegionId;

public class ProducerGamma extends AbstractComponent {

	private RegionId regionId;

	private static class ProductionPlan implements Plan {
	}

	@Override
	public void executePlan(final Environment environment, final Plan plan) {
		BatchId batchId = environment.createBatch(Material.MATERIAL_1, 100);
		StageId stageId = environment.createStage();
		environment.moveBatchToStage(batchId, stageId);
		environment.convertStageToResource(stageId, Resource.RESOURCE_1, 10000);
		MaterialsProducerId componentId = (MaterialsProducerId) environment.getCurrentComponentId();
		long amount = environment.getMaterialsProducerResourceLevel(componentId, Resource.RESOURCE_1);
		environment.transferProducedResourceToRegion(componentId, Resource.RESOURCE_1, regionId, amount);
	}

	@Override
	public void init(Environment environment) {
		regionId = environment.getRegionIds().iterator().next();
		for (int i = 0; i < 14; i++) {
			environment.addPlan(new ProductionPlan(), i);
		}
	}

}
