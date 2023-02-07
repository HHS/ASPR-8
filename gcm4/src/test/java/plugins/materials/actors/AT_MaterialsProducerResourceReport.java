package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerConstructionData;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsTestPluginFactory;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public final class AT_MaterialsProducerResourceReport {

	private ReportItem getReportItemFromResourceId(ActorContext agentContext, MaterialsProducerId materialsProducerId,
			ResourceId resourceId, long amount) {

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
	@UnitTestConstructor(target = MaterialsProducerResourceReport.class, args = { ReportLabel.class })
	public void testConstructor() {
		MaterialsProducerResourceReport report = new MaterialsProducerResourceReport(REPORT_LABEL);
		assertNotNull(report);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReport.class, name = "init", args = {
			ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		MaterialsProducerId newMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();

		double actionTime = 0;
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					expectedReportItems.put(getReportItemFromResourceId(c, testMaterialsProducerId, testResourceId, 0L),
							1);
				}
			}

			ResourceId newResourceId = TestResourceId.getUnknownResourceId();
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.addResourceId(newResourceId, TimeTrackingPolicy.TRACK_TIME);

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				expectedReportItems.put(getReportItemFromResourceId(c, testMaterialsProducerId, newResourceId, 0L), 1);
			}

		}));

		// add a new materials producer just before time 20
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(19.5, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
			MaterialsProducerConstructionData.Builder builder = //
					MaterialsProducerConstructionData.builder()//
							.setMaterialsProducerId(newMaterialsProducerId);//
			for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId
					.getPropertiesWithoutDefaultValues()) {
				Object randomPropertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				builder.setMaterialsProducerPropertyValue(testMaterialsProducerPropertyId, randomPropertyValue);
			}

			MaterialsProducerConstructionData materialsProducerConstructionData = builder.build();

			materialsDataManager.addMaterialsProducer(materialsProducerConstructionData);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
				expectedReportItems.put(getReportItemFromResourceId(c, newMaterialsProducerId, resourceId, 0L), 1);
			}

		}));

		for (int i = 0; i < 100; i++) {

			// set a resource value
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class);
				List<MaterialsProducerId> mats = new ArrayList<>(materialsDataManager.getMaterialsProducerIds());

				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				MaterialsProducerId materialsProducerId = mats.get(randomGenerator.nextInt(mats.size()));
				ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
				List<ResourceId> resourceIds = new ArrayList<>(resourcesDataManager.getResourceIds());
				Random random = new Random(randomGenerator.nextLong());

				ResourceId resourceId = resourceIds.get(random.nextInt(resourceIds.size()));

				if (randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(100) + 1;
					StageId stageId = materialsDataManager.addStage(materialsProducerId);
					materialsDataManager.convertStageToResource(stageId, resourceId, amount);
					expectedReportItems.put(getReportItemFromResourceId(c, materialsProducerId, resourceId, amount), 1);
				} else {
					long resourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId,
							resourceId);
					if (resourceLevel > 0) {
						long amount = randomGenerator.nextInt((int) resourceLevel) + 1;
						TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
						materialsDataManager.transferResourceToRegion(materialsProducerId, resourceId, testRegionId,
								amount);
						expectedReportItems
								.put(getReportItemFromResourceId(c, materialsProducerId, resourceId, -amount), 1);
					}
				}
			}));
		}

		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();

		List<Plugin> pluginsToAdd = MaterialsTestPluginFactory.factory(0, 0, 0, 6081341958178733565L, testPluginData)
				.getPlugins();
		pluginsToAdd.add(ReportsTestPluginFactory.getPluginFromReport(new MaterialsProducerResourceReport(REPORT_ID)::init));

		TestSimulation.executeSimulation(pluginsToAdd, outputConsumer);

		assertTrue(outputConsumer.isComplete());
		assertEquals(expectedReportItems, outputConsumer.getOutputItems(ReportItem.class));
	}

	private static ReportItem getReportItem(Object... values) {
		Builder builder = ReportItem.builder().setReportLabel(REPORT_LABEL).setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("report");

	private static final ReportHeader REPORT_HEADER = getReportHeader();

	private static ReportHeader getReportHeader() {
		return ReportHeader.builder()//
				.add("time")//
				.add("resource")//
				.add("materials_producer")//
				.add("action")//
				.add("amount")//
				.build();

	}

}
