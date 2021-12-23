package plugins.regions.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.regions.RegionPlugin;
import plugins.regions.events.mutation.RegionPropertyValueAssignmentEvent;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.support.SimpleRegionId;
import plugins.regions.support.SimpleRegionPropertyId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionPropertyReport.class)
public class AT_RegionPropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {		

		/*
		 * We will add three regions, one agent and the region
		 * property report to the engine. The regions will have a few
		 * properties and the agent will alter various region properties
		 * over time. Report items from the report will be collected in an
		 * output consumer. The expected report items will be collected in a
		 * separate consumer and the consumers will be compared for equality.
		 * The output consumers properly accounts for report item duplications.
		 */

		EngineBuilder engineBuilder = Engine.builder();

		
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();
		
		// add regions A, B and C
		RegionId regionA = new SimpleRegionId("Region_A");
		regionBuilder.setRegionComponentInitialBehaviorSupplier(regionA, () -> (c) -> {
		});
		RegionId regionB = new SimpleRegionId("Region_B");
		regionBuilder.setRegionComponentInitialBehaviorSupplier(regionB, () -> (c) -> {
		});
		RegionId regionC = new SimpleRegionId("Region_C");
		regionBuilder.setRegionComponentInitialBehaviorSupplier(regionC, () -> (c) -> {
		});


		//add the region properties
		RegionPropertyId prop_age = new SimpleRegionPropertyId("prop_age");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		regionBuilder.defineRegionProperty(prop_age, propertyDefinition);

		RegionPropertyId prop_infected = new SimpleRegionPropertyId("prop_infected");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build();
		regionBuilder.defineRegionProperty(prop_infected, propertyDefinition);

		RegionPropertyId prop_length = new SimpleRegionPropertyId("prop_length");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(10.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty( prop_length, propertyDefinition);

		RegionPropertyId prop_height = new SimpleRegionPropertyId("prop_height");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(5.0).setType(Double.class).build();
		regionBuilder.defineRegionProperty( prop_height, propertyDefinition);

		RegionPropertyId prop_policy = new SimpleRegionPropertyId("prop_policy");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue("start").setType(String.class).build();
		regionBuilder.defineRegionProperty( prop_policy, propertyDefinition);

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the report
		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(REPORT_ID, () -> new RegionPropertyReport()::init).build();
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);

		// add remaining plugins
		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(8833508541323194123L).build())::init);
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent and have it assign various region properties at
		// various times
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionC, prop_policy, "move"));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.0, (c) -> {
			// two settings of the same property
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionA, prop_age, 45));
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionA, prop_age, 46));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.0, (c) -> {
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionA, prop_age, 100));
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionB, prop_height, 13.6));
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionC, prop_policy, "hold"));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3.0, (c) -> {
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionC, prop_policy, "terminate"));
			// note the duplicated value
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionB, prop_height, 99.7));
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionB, prop_height, 99.7));
			// and now a third setting of the same property to a new value
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionB, prop_height, 100.0));
			c.resolveEvent(new RegionPropertyValueAssignmentEvent(regionB, prop_length, 60.0));
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		engineBuilder.setOutputConsumer(actualOutputConsumer);
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		/*
		 * Collect the expected report items. Note that order does not matter.		 * 
		 */
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, prop_age, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, prop_infected, false));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, prop_height, 5.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, prop_length, 10.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_policy, "start"));
		expectedOutputConsumer.accept(getReportItem(1.0, regionA, prop_age, 45));
		expectedOutputConsumer.accept(getReportItem(1.0, regionA, prop_age, 46));
		expectedOutputConsumer.accept(getReportItem(2.0, regionA, prop_age, 100));
		expectedOutputConsumer.accept(getReportItem(2.0, regionB, prop_height, 13.6));
		expectedOutputConsumer.accept(getReportItem(2.0, regionC, prop_policy, "hold"));
		expectedOutputConsumer.accept(getReportItem(3.0, regionC, prop_policy, "terminate"));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, prop_height, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, prop_height, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, prop_height, 100.0));
		expectedOutputConsumer.accept(getReportItem(3.0, regionB, prop_length, 60.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_policy, "move"));		
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, prop_length, 10.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, prop_height, 5.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionA, prop_policy, "start"));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, prop_age, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, prop_infected, false));
		expectedOutputConsumer.accept(getReportItem(0.0, regionB, prop_policy, "start"));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_age, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_infected, false));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_length, 10.0));
		expectedOutputConsumer.accept(getReportItem(0.0, regionC, prop_height, 5.0));
		
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

	

	private static final ReportId REPORT_ID = new SimpleReportId("region property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Region").add("Property").add("Value").build();
}
