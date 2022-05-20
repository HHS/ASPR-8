package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.SimpleReportId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = MaterialsProducerResourceReport.class)
public final class AT_MaterialsProducerResourceReport {

	private ReportItem getReportItemFromResourceId(ActorContext agentContext, MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount) {

		String actionName;
		if (amount < 0) {
			actionName = "Removed";
		} else {
			actionName = "Added";
		}

		long reportedAmount = FastMath.abs(amount);

		return getReportItem(agentContext.getTime(), resourceId, materialsProducerId, actionName, reportedAmount);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {
		Set<ReportItem> expectedReportItems = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		RandomGenerator rg = RandomGeneratorProvider.getRandomGenerator(8635270533185454765L);

		double actionTime = 0;
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					expectedReportItems.add(getReportItemFromResourceId(c, testMaterialsProducerId, testResourceId, 0L));
				}
			}

			ResourceId newResourceId = TestResourceId.getUnknownResourceId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceId(newResourceId, TimeTrackingPolicy.TRACK_TIME);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				expectedReportItems.add(getReportItemFromResourceId(c, testMaterialsProducerId, newResourceId, 0L));
			}

		}));

		List<TestMaterialsProducerId> producerIds = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(rg);
			producerIds.add(testMaterialsProducerId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : producerIds) {

			// set a resource value
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				List<ResourceId> resourceIds = new ArrayList<>(resourcesDataManager.getResourceIds());
				Random random = new Random(randomGenerator.nextLong());
				
				ResourceId resourceId = resourceIds.get(random.nextInt(resourceIds.size()));

				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(100) + 1;
					StageId stageId = materialsDataManager.addStage(testMaterialsProducerId);
					materialsDataManager.convertStageToResource(stageId, resourceId, amount);
					expectedReportItems.add(getReportItemFromResourceId(c, testMaterialsProducerId, resourceId, amount));
				} else {
					long resourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(testMaterialsProducerId, resourceId);
					if (resourceLevel > 0) {
						long amount = randomGenerator.nextInt((int) resourceLevel) + 1;
						TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
						materialsDataManager.transferResourceToRegion(testMaterialsProducerId, resourceId, testRegionId, amount);
						expectedReportItems.add(getReportItemFromResourceId(c, testMaterialsProducerId, resourceId, -amount));
					}
				}
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Set<ReportItem> actualReportItems = MaterialsActionSupport.testConsumers(8759226038479000135L, testPlugin, new MaterialsProducerResourceReport(REPORT_ID)::init);

		assertEquals(expectedReportItems, actualReportItems);
	}

	private static ReportItem getReportItem(Object... values) {
		Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("report");

	private static final ReportHeader REPORT_HEADER = getReportHeader();

	private static ReportHeader getReportHeader() {
		return ReportHeader	.builder()//
							.add("time")//
							.add("resource")//
							.add("materials_producer")//
							.add("action")//
							.add("amount")//
							.build();

	}

}
