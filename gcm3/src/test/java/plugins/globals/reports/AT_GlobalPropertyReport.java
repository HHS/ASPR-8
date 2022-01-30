package plugins.globals.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.globals.GlobalPlugin;
import plugins.globals.events.mutation.GlobalPropertyValueAssignmentEvent;
import plugins.globals.initialdata.GlobalInitialData;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.support.SimpleGlobalPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = GlobalPropertyReport.class)
public class AT_GlobalPropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {

		/*
		 * We will add one agent and the global property report to the engine.
		 * We will define a few global properties and the agent will alter
		 * various global properties over time. Report items from the report
		 * will be collected in an output consumer. The expected report items
		 * will be collected in a separate consumer and the consumers will be
		 * compared for equality. The output consumers properly accounts for
		 * report item duplications.
		 */

		Builder builder = Simulation.builder();

		// add the global property definitions
		GlobalInitialData.Builder initialDatabuilder = GlobalInitialData.builder();
		
		GlobalPropertyId globalPropertyId_1 = new SimpleGlobalPropertyId("id_1");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(3).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_1, propertyDefinition);
		
		GlobalPropertyId globalPropertyId_2 = new SimpleGlobalPropertyId("id_2");
		propertyDefinition = PropertyDefinition.builder().setType(Double.class).setDefaultValue(6.78).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_2, propertyDefinition);
		
		GlobalPropertyId globalPropertyId_3 = new SimpleGlobalPropertyId("id_3");
		propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();
		initialDatabuilder.defineGlobalProperty(globalPropertyId_3, propertyDefinition);

		GlobalInitialData globalInitialData = initialDatabuilder.build();
		builder.addPlugin(GlobalPlugin.PLUGIN_ID, new GlobalPlugin(globalInitialData)::init);

		// add the report
		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(REPORT_ID, () -> new GlobalPropertyReport()::init).build();
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);
		
		//add the remaining plugins
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
	

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent and have it assign various global properties at
		// various times
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1,67));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.0, (c) -> {
			// two settings of the same property
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 88.88));
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_3, false));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.0, (c) -> {
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_1,100));
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 3.45));
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_3, true));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3.0, (c) -> {
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_3, false));
			// note the duplicated value
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 99.7));
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 99.7));
			// and now a third setting of the same property to a new value
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_2, 100.0));
			c.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId_3, true));
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		builder.setOutputConsumer(actualOutputConsumer);
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

		/*
		 * Collect the expected report items. Note that order does not matter. *
		 */
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		expectedOutputConsumer.accept(getReportItem(0.0, globalPropertyId_1, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, globalPropertyId_2, 6.78));
		expectedOutputConsumer.accept(getReportItem(0.0, globalPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(0.0,globalPropertyId_1,67));
		expectedOutputConsumer.accept(getReportItem(1.0,globalPropertyId_2, 88.88));
		expectedOutputConsumer.accept(getReportItem(1.0,globalPropertyId_3, false));
		expectedOutputConsumer.accept(getReportItem(2.0,globalPropertyId_1,100));
		expectedOutputConsumer.accept(getReportItem(2.0,globalPropertyId_2, 3.45));
		expectedOutputConsumer.accept(getReportItem(2.0,globalPropertyId_3, true));
		expectedOutputConsumer.accept(getReportItem(3.0,globalPropertyId_3, false));
		expectedOutputConsumer.accept(getReportItem(3.0,globalPropertyId_2, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0,globalPropertyId_2, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0,globalPropertyId_2, 100.0));
		expectedOutputConsumer.accept(getReportItem(3.0,globalPropertyId_3, true));

		assertEquals(expectedOutputConsumer, actualOutputConsumer);
	}

	private static ReportItem getReportItem(Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);
		builder.setReportHeader(REPORT_HEADER);
		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("global property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Property").add("Value").build();
}
