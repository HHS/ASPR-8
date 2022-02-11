package plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.MaterialsProducerPropertyValueAssignmentEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.reports.ReportId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerPropertyReport.class)
public final class AT_MaterialsProducerPropertyReport {

	private ReportItem getReportItemFromPropertyId(AgentContext agentContext, MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {

		MaterialsDataView materialsDataView = agentContext.getDataView(MaterialsDataView.class).get();
		Object propertyValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);

		ReportItem reportItem = getReportItem(//
				agentContext.getTime(), //
				materialsProducerId, //
				materialsProducerPropertyId, //
				propertyValue //
		);//

		return reportItem;
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		double actionTime = 0;
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					expectedOutputConsumer.accept(getReportItemFromPropertyId(c, testMaterialsProducerId, testMaterialsProducerPropertyId));
				}
			}
		}));

		for (int i = 0; i < 100; i++) {

			// set a property value
			pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(actionTime++, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				c.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue));
				expectedOutputConsumer.accept(getReportItemFromPropertyId(c, testMaterialsProducerId, testMaterialsProducerPropertyId));

			}));

		}

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(8759226038479000135L, actionPluginInitializer, actualOutputConsumer, new MaterialsProducerPropertyReport()::init);

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
		ReportHeader.Builder builder = ReportHeader	.builder()//
													.add("Time")//
													.add("MaterialsProducer")//
													.add("Property")//
													.add("Value");//
		return builder.build();
	}

}