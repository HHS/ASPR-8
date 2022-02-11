package plugins.compartments.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.events.mutation.CompartmentPropertyValueAssignmentEvent;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.support.SimpleCompartmentId;
import plugins.compartments.support.SimpleCompartmentPropertyId;
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
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentPropertyReport.class)
public class AT_CompartmentPropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {		

		/*
		 * We will add three compartments, one agent and the compartment
		 * property report to the engine. The compartments will have a few
		 * properties and the agent will alter various compartment properties
		 * over time. Report items from the report will be collected in an
		 * output consumer. The expected report items will be collected in a
		 * separate consumer and the consumers will be compared for equality.
		 * The output consumers properly account for report item duplications.
		 */

		Builder builder = Simulation.builder();

		// add the test compartments and their property definitions
		CompartmentInitialData.Builder compartmentBuilder = CompartmentInitialData.builder();
		// compartment A and its properties
		CompartmentId compartmentA = new SimpleCompartmentId("Compartment_A");
		compartmentBuilder.setCompartmentInitialBehaviorSupplier(compartmentA, () -> (c) -> {
		});

		CompartmentPropertyId prop_A_age = new SimpleCompartmentPropertyId("prop_A_age");
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setDefaultValue(3).setType(Integer.class).build();
		compartmentBuilder.defineCompartmentProperty(compartmentA, prop_A_age, propertyDefinition);

		CompartmentPropertyId prop_A_infected = new SimpleCompartmentPropertyId("prop_A_infected");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(false).setType(Boolean.class).build();
		compartmentBuilder.defineCompartmentProperty(compartmentA, prop_A_infected, propertyDefinition);

		// compartment B and its properties
		CompartmentId compartmentB = new SimpleCompartmentId("Compartment_B");
		compartmentBuilder.setCompartmentInitialBehaviorSupplier(compartmentB, () -> (c) -> {
		});

		CompartmentPropertyId prop_B_length = new SimpleCompartmentPropertyId("prop_B_length");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(10.0).setType(Double.class).build();
		compartmentBuilder.defineCompartmentProperty(compartmentB, prop_B_length, propertyDefinition);

		CompartmentPropertyId prop_B_height = new SimpleCompartmentPropertyId("prop_B_height");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue(5.0).setType(Double.class).build();
		compartmentBuilder.defineCompartmentProperty(compartmentB, prop_B_height, propertyDefinition);

		// compartment C and its properties
		CompartmentId compartmentC = new SimpleCompartmentId("Compartment_C");
		compartmentBuilder.setCompartmentInitialBehaviorSupplier(compartmentC, () -> (c) -> {
		});

		CompartmentPropertyId prop_C_policy = new SimpleCompartmentPropertyId("prop_C_policy");
		propertyDefinition = PropertyDefinition.builder().setDefaultValue("start").setType(String.class).build();
		compartmentBuilder.defineCompartmentProperty(compartmentC, prop_C_policy, propertyDefinition);

		builder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

		// add the report
		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(REPORT_ID, () -> new CompartmentPropertyReport()::init).build();
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);

		// add remaining plugins
		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(1120153212673715272L).build()::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// create an agent and have it assign various compartment properties at
		// various times
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentC, prop_C_policy, "move"));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.0, (c) -> {
			// two settings of the same property
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentA, prop_A_age, 45));
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentA, prop_A_age, 46));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.0, (c) -> {
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentA, prop_A_age, 100));
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentB, prop_B_height, 13.6));
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentC, prop_C_policy, "hold"));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3.0, (c) -> {
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentC, prop_C_policy, "terminate"));
			// note the duplicated value
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentB, prop_B_height, 99.7));
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentB, prop_B_height, 99.7));
			// and now a third setting of the same property to a new value
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentB, prop_B_height, 100.0));
			c.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentB, prop_B_length, 60.0));
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
		 * Collect the expected report items. Note that order does not matter.		 * 
		 */
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentA, prop_A_age, 3));
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentA, prop_A_infected, false));
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentB, prop_B_height, 5.0));
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentB, prop_B_length, 10.0));
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentC, prop_C_policy, "start"));
		expectedOutputConsumer.accept(getReportItem(1.0, compartmentA, prop_A_age, 45));
		expectedOutputConsumer.accept(getReportItem(1.0, compartmentA, prop_A_age, 46));
		expectedOutputConsumer.accept(getReportItem(2.0, compartmentA, prop_A_age, 100));
		expectedOutputConsumer.accept(getReportItem(2.0, compartmentB, prop_B_height, 13.6));
		expectedOutputConsumer.accept(getReportItem(2.0, compartmentC, prop_C_policy, "hold"));
		expectedOutputConsumer.accept(getReportItem(3.0, compartmentC, prop_C_policy, "terminate"));
		expectedOutputConsumer.accept(getReportItem(3.0, compartmentB, prop_B_height, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, compartmentB, prop_B_height, 99.7));
		expectedOutputConsumer.accept(getReportItem(3.0, compartmentB, prop_B_height, 100.0));
		expectedOutputConsumer.accept(getReportItem(3.0, compartmentB, prop_B_length, 60.0));
		expectedOutputConsumer.accept(getReportItem(0.0, compartmentC, prop_C_policy, "move"));
		
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

	

	private static final ReportId REPORT_ID = new SimpleReportId("compartment property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Compartment").add("Property").add("Value").build();
}
