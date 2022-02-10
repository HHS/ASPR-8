package plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.ProducedResourceTransferToRegionEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.events.mutation.StageToResourceConversionEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerResourceReport.class)
public final class AT_MaterialsProducerResourceReport {

	private ReportItem getReportItemFromResourceId(AgentContext agentContext, MaterialsProducerId materialsProducerId, ResourceId resourceId, long amount) {

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
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();
		
		
		RandomGenerator rg = SeedProvider.getRandomGenerator(8635270533185454765L);

		double actionTime = 0;
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					expectedOutputConsumer.accept(getReportItemFromResourceId(c, testMaterialsProducerId, testResourceId, 0L));
				}
			}
		}));
		
		List<TestMaterialsProducerId> producerIds = new ArrayList<>();
		for (int i = 0; i < 100; i++) {
			TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(rg);
			producerIds.add(testMaterialsProducerId);
		}

		for (TestMaterialsProducerId testMaterialsProducerId : producerIds) {

			// set a resource value
			pluginBuilder.addAgentActionPlan(testMaterialsProducerId, new AgentActionPlan(actionTime++, (c) -> {
				MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				
				if(randomGenerator.nextBoolean()) {
					long amount = randomGenerator.nextInt(100)+1;
					c.resolveEvent(new StageCreationEvent());
					StageId stageId = materialsDataView.getLastIssuedStageId().get();					
					c.resolveEvent(new StageToResourceConversionEvent(stageId, testResourceId, amount));
					expectedOutputConsumer.accept(getReportItemFromResourceId(c, testMaterialsProducerId, testResourceId, amount));	
				}else {
					long resourceLevel = materialsDataView.getMaterialsProducerResourceLevel(testMaterialsProducerId, testResourceId);
					if(resourceLevel>0) {
					long amount = randomGenerator.nextInt((int)resourceLevel)+1;
					TestRegionId testRegionId = TestRegionId.getRandomRegionId(randomGenerator);
					c.resolveEvent(new ProducedResourceTransferToRegionEvent(testMaterialsProducerId, testResourceId, testRegionId, amount));
					expectedOutputConsumer.accept(getReportItemFromResourceId(c, testMaterialsProducerId, testResourceId, -amount));
					}
				}
			}));
		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8759226038479000135L, actionPluginInitializer, actualOutputConsumer, new MaterialsProducerResourceReport()::init);

		assertEquals(expectedOutputConsumer, actualOutputConsumer);
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
							.add("Time")//
							.add("Resource")//
							.add("MaterialsProducer")//
							.add("Action")//
							.add("Amount")//
							.build();

	}

}
