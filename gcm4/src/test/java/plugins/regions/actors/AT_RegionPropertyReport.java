package plugins.regions.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionConstructionData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDefinitionInitialization;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.regions.testsupport.RegionsTestPluginFactory;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportItem;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTag;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public class AT_RegionPropertyReport {

	@Test
	@UnitTestConstructor(target = RegionPropertyReport.class, args = { ReportLabel.class, RegionPropertyId[].class })
	public void testConstructor() {
		RegionPropertyReport regionPropertyReport = new RegionPropertyReport(REPORT_LABEL);

		// Show not null when given 0 RegionPropertyIds
		assertNotNull(regionPropertyReport);

		RegionPropertyId prop_age = new SimpleRegionPropertyId("prop_age");
		RegionPropertyId prop_infected = new SimpleRegionPropertyId("prop_infected");
		RegionPropertyId prop_length = new SimpleRegionPropertyId("prop_length");
		RegionPropertyId prop_height = new SimpleRegionPropertyId("prop_height");
		RegionPropertyId prop_policy = new SimpleRegionPropertyId("prop_policy");
		RegionPropertyId prop_vaccine = new SimpleRegionPropertyId("prop_vaccine");

		regionPropertyReport = new RegionPropertyReport(REPORT_LABEL, prop_age, prop_infected, prop_length, prop_height,
				prop_policy, prop_vaccine);

		// Show not null when given 1 or more RegionPropertyIds
		assertNotNull(regionPropertyReport);
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testInit() {

		
		/*
		 * We will add three regions, one agent and the region property report
		 * to the engine. The regions will have a few properties and the agent
		 * will alter various region properties over time. Report items from the
		 * report will be collected in an output consumer. The expected report
		 * items will be collected in a separate consumer and the consumers will
		 * be compared for equality. The output consumers properly accounts for
		 * report item duplications.
		 */
		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();

		// add regions A, B and C
		RegionId regionA = new SimpleRegionId("Region_A");
		regionBuilder.addRegion(regionA);
		RegionId regionB = new SimpleRegionId("Region_B");
		regionBuilder.addRegion(regionB);
		RegionId regionC = new SimpleRegionId("Region_C");
		regionBuilder.addRegion(regionC);

		RegionId regionD = new SimpleRegionId("Region_D");

		// add the region properties
		RegionPropertyId prop_age = new SimpleRegionPropertyId("prop_age");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class)
				.build();
		regionBuilder.defineRegionProperty(prop_age, propertyDefinition);

		RegionPropertyId prop_infected = new SimpleRegionPropertyId("prop_infected");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build();
		regionBuilder.defineRegionProperty(prop_infected, propertyDefinition);

		RegionPropertyId prop_length = new SimpleRegionPropertyId("prop_length");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(10.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty(prop_length, propertyDefinition);

		RegionPropertyId prop_height = new SimpleRegionPropertyId("prop_height");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(5.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty(prop_height, propertyDefinition);

		RegionPropertyId prop_policy = new SimpleRegionPropertyId("prop_policy");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue("start").setType(String.class).build();
		regionBuilder.defineRegionProperty(prop_policy, propertyDefinition);

		RegionPropertyId prop_vaccine = new SimpleRegionPropertyId("prop_vaccine");
		RegionsPluginData regionsPluginData = regionBuilder.build();

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create an agent and have it assign various region properties at
		// various times
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "move");
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.0, (c) -> {
			// two settings of the same property
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 45);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 45);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.0, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionA, prop_age, 100);
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 13.6);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "hold");
			RegionConstructionData regionConstructionData = RegionConstructionData.builder().setRegionId(regionD)
					.build();
			regionsDataManager.addRegion(regionConstructionData);

			PropertyDefinition def = PropertyDefinition.builder().setDefaultValue(0).setType(Integer.class).build();
			RegionPropertyDefinitionInitialization regionPropertyDefinitionInitialization = RegionPropertyDefinitionInitialization
					.builder().setPropertyDefinition(def)
					.setRegionPropertyId(prop_vaccine).build();
			regionsDataManager.defineRegionProperty(regionPropertyDefinitionInitialization);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.0, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setRegionPropertyValue(regionC, prop_policy, "terminate");
			regionsDataManager.setRegionPropertyValue(regionA, prop_vaccine, 5);

			// note the duplicated value

			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 99.7);
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 99.7);

			// and now a third setting of the same property to a new value
			regionsDataManager.setRegionPropertyValue(regionB, prop_height, 100.0);
			regionsDataManager.setRegionPropertyValue(regionB, prop_length, 60.0);

			regionsDataManager.setRegionPropertyValue(regionD, prop_height, 70.0);
			regionsDataManager.setRegionPropertyValue(regionD, prop_length, 45.0);
		}));

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		expectedReportItems.put(getReportItem(0.0, regionA, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(1.0, regionA, prop_age, 45), 2);
		expectedReportItems.put(getReportItem(2.0, regionA, prop_age, 100), 1);
		expectedReportItems.put(getReportItem(2.0, regionB, prop_height, 13.6), 1);
		expectedReportItems.put(getReportItem(2.0, regionC, prop_policy, "hold"), 1);
		expectedReportItems.put(getReportItem(3.0, regionC, prop_policy, "terminate"), 1);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_height, 99.7), 2);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_height, 100.0), 1);
		expectedReportItems.put(getReportItem(3.0, regionB, prop_length, 60.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_policy, "move"), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionA, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionB, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(0.0, regionC, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionA, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(2.0, regionB, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(2.0, regionC, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(3.0, regionA, prop_vaccine, 5), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_age, 3), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_infected, false), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_length, 10.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_height, 5.0), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_policy, "start"), 1);
		expectedReportItems.put(getReportItem(2.0, regionD, prop_vaccine, 0), 1);
		expectedReportItems.put(getReportItem(3.0, regionD, prop_height, 70.0), 1);
		expectedReportItems.put(getReportItem(3.0, regionD, prop_length, 45.0), 1);

		TestPluginData testPluginData = pluginBuilder.build();

		TestSimulationOutputConsumer outputConsumer = new TestSimulationOutputConsumer();
		List<Plugin> pluginsToAdd = RegionsTestPluginFactory.factory(0, 3656508960291338287L, TimeTrackingPolicy.TRACK_TIME, testPluginData).setRegionsPluginData(regionsPluginData).getPlugins();
		pluginsToAdd.add(ReportsTestPluginFactory.getPluginFromReport(new RegionPropertyReport(REPORT_LABEL)::init));

		TestSimulation.executeSimulation(pluginsToAdd, outputConsumer);

		assertTrue(outputConsumer.isComplete());
		assertEquals(expectedReportItems, outputConsumer.getOutputItems(ReportItem.class));
	}

	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("region property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Region").add("Property")
			.add("Value").build();
}
