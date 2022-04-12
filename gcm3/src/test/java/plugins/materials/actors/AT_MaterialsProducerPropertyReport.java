package plugins.materials.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.ActorContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.SimpleReportId;
import plugins.stochastics.StochasticsDataManager;

@UnitTest(target = MaterialsProducerPropertyReport.class)
public final class AT_MaterialsProducerPropertyReport {

	private ReportItem getReportItemFromPropertyId(ActorContext agentContext, MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {

		MaterialsDataManager materialsDataManager = agentContext.getDataManager(MaterialsDataManager.class).get();
		Object propertyValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);

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
		
		Set<ReportItem> expectedReportItems = new LinkedHashSet<>();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		double actionTime = 0;
		

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(actionTime++, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
					expectedReportItems.add(getReportItemFromPropertyId(c, testMaterialsProducerId, testMaterialsProducerPropertyId));
				}
			}
		}));

		for (int i = 0; i < 100; i++) {

			// set a property value
			pluginBuilder.addTestActorPlan("agent", new TestActorPlan(actionTime++, (c) -> {
				MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				TestMaterialsProducerId testMaterialsProducerId = TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator);
				TestMaterialsProducerPropertyId testMaterialsProducerPropertyId = TestMaterialsProducerPropertyId.getRandomMutableMaterialsProducerPropertyId(randomGenerator);
				Object propertyValue = testMaterialsProducerPropertyId.getRandomPropertyValue(randomGenerator);
				materialsDataManager.setMaterialsProducerPropertyValue(testMaterialsProducerId, testMaterialsProducerPropertyId, propertyValue);
				expectedReportItems.add(getReportItemFromPropertyId(c, testMaterialsProducerId, testMaterialsProducerPropertyId));

			}));

		}

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		Set<ReportItem> actualReportItems = MaterialsActionSupport.testConsumers(8759226038479000135L, testPlugin,  new MaterialsProducerPropertyReport(REPORT_ID)::init);

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
		ReportHeader.Builder builder = ReportHeader	.builder()//
													.add("time")//
													.add("materials_producer")//
													.add("property")//
													.add("value");//
		return builder.build();
	}

}