package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.datamanagers.ResourcesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourcePropertyInitialization;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport.ResourcesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport.TestResourcePropertyId;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_ResourceReport {

	@Test
	@UnitTestConstructor(target = ResourceReport.class, args = { ResourceReportPluginData.class})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = ResourceReport.class, name = "init", args = { ReportContext.class }, tags = UnitTag.INCOMPLETE)
	public void testInit() {
		// incomplete test
	}

	@Test
	@UnitTestMethod(target = ResourceReport.class, name = "init", args = { ReportContext.class }, tags = UnitTag.INCOMPLETE)
	public void testInit_State() {
		// Test with producing simulation state

		int initialPopulation = 20;

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * create an agent and have it assign various resource properties at
		 * various times
		 */

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_3,
					TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, "A");
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_2,
					TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 45);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 36.7);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_4,
					TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_2,
					TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, false);

			// add new property definitions
			for (AT_ResourcePropertyReport.TestAuxiliaryResourcePropertyId propertyId : AT_ResourcePropertyReport.TestAuxiliaryResourcePropertyId.values()) {
				TestResourceId testResourceId = propertyId.getTestResourceId();
				PropertyDefinition propertyDefinition = propertyId.getPropertyDefinition();
				ResourcePropertyInitialization resourcePropertyInitialization = ResourcePropertyInitialization.builder()
						.setResourceId(testResourceId).setResourcePropertyId(propertyId)
						.setPropertyDefinition(propertyDefinition).build();
				resourcesDataManager.defineResourceProperty(resourcePropertyInitialization);
			}

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_4,
					TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true);

			// note the duplicated value
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5);

			// and now a third setting of the same property to a new value
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 100);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 60);

			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_1,
					AT_ResourcePropertyReport.TestAuxiliaryResourcePropertyId.AUX_RESOURCE_PROPERTY_1_BOOLEAN_MUTABLE, true);
			resourcesDataManager.setResourcePropertyValue(TestResourceId.RESOURCE_2,
					AT_ResourcePropertyReport.TestAuxiliaryResourcePropertyId.AUX_RESOURCE_PROPERTY_2_INTEGER_MUTABLE, 137);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		ResourcesTestPluginFactory.Factory factory = ResourcesTestPluginFactory.factory(initialPopulation, 8914112012010329946L, testPluginData);
		ResourceReportPluginData resourceReportPluginData = ResourceReportPluginData.builder()
				.setReportLabel(REPORT_LABEL)
				.setReportPeriod(ReportPeriod.DAILY)
				.setDefaultInclusion(true)
				.excludeResource(TestResourceId.RESOURCE_2)
				.build();
		factory.setResourceReportPluginData(resourceReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		Map<ResourceReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(ResourceReportPluginData.class);
		assertEquals(1, outputItems.size());
		ResourceReportPluginData resourceReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(resourceReportPluginData, resourceReportPluginData2);

		// Test without producing simulation state

		testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		outputItems = testOutputConsumer.getOutputItemMap(ResourceReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("resource property report");

}