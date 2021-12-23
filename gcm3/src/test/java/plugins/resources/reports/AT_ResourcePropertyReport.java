package plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.resources.ResourcesPlugin;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.initialdata.ResourceInitialData;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = ResourcePropertyReport.class)
public class AT_ResourcePropertyReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testInit() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8914112012010329946L);
		int initialPopulation = 20;

		// create a list of people
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		EngineBuilder engineBuilder = Engine.builder();

		// add the resources plugin
		ResourceInitialData.Builder resourcesBuilder = ResourceInitialData.builder();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			resourcesBuilder.addResource(testResourceId);
			resourcesBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}

		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			TestResourceId testResourceId = testResourcePropertyId.getTestResourceId();
			PropertyDefinition propertyDefinition = testResourcePropertyId.getPropertyDefinition();
			Object propertyValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
			resourcesBuilder.defineResourceProperty(testResourceId, testResourcePropertyId, propertyDefinition);
			resourcesBuilder.setResourcePropertyValue(testResourceId, testResourcePropertyId, propertyValue);
		}

		engineBuilder.addPlugin(ResourcesPlugin.PLUGIN_ID, new ResourcesPlugin(resourcesBuilder.build())::init);

		// add the partitions plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentsBuilder = CompartmentInitialData.builder();
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentsBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> new ActionAgent(testCompartmentId)::init);
		}

		for (PersonId personId : people) {
			compartmentsBuilder.setPersonCompartment(personId, TestCompartmentId.getRandomCompartmentId(randomGenerator));
		}

		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentsBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionsBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> new ActionAgent(testRegionId)::init);
		}
		for (PersonId personId : people) {
			regionsBuilder.setPersonRegion(personId, TestRegionId.getRandomRegionId(randomGenerator));
		}

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionsBuilder.build())::init);

		// add the report plugin

		ReportsInitialData.Builder reportsBuilder = ReportsInitialData.builder();
		reportsBuilder.addReport(REPORT_ID, () -> new ResourcePropertyReport()::init);
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsBuilder.build())::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(randomGenerator.nextLong()).build())::init);

		/*
		 * We will add three compartments, one agent and the compartment
		 * property report to the engine. The compartments will have a few
		 * properties and the agent will alter various compartment properties
		 * over time. Report items from the report will be collected in an
		 * output consumer. The expected report items will be collected in a
		 * separate consumer and the consumers will be compared for equality.
		 * The output consumers properly account for report item duplications.
		 */

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// // create an agent and have it assign various resource properties at
		// // various times
		pluginBuilder.addAgent("agent");

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0.0, (c) -> {
			/*
			 * note that this is time 0 and should show that property initial
			 * values are still reported correctly
			 */
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, "A"));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.0, (c) -> {
			// two settings of the same property
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 45));
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 36.7));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.0, (c) -> {
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true));
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, false));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3.0, (c) -> {
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true));

			// note the duplicated value
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5));
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5));

			// and now a third setting of the same property to a new value
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 100));
			c.resolveEvent(new ResourcePropertyValueAssignmentEvent(TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 60));
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
		 * Collect the expected report items. Note that order does not matter. *
		 */
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 1673029105));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 0.9762970538942173));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 1818034648));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, 319183829));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_INTEGER_IMMUTABLE, 704893369));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_5, TestResourcePropertyId.ResourceProperty_5_1_DOUBLE_IMMUTABLE, 0.7547798894049567));
		expectedOutputConsumer.accept(getReportItem(0.0, TestResourceId.RESOURCE_3, TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE, "A"));
		expectedOutputConsumer.accept(getReportItem(1.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_2_INTEGER_MUTABLE, 45));
		expectedOutputConsumer.accept(getReportItem(1.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 36.7));
		expectedOutputConsumer.accept(getReportItem(2.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(2.0, TestResourceId.RESOURCE_2, TestResourcePropertyId.ResourceProperty_2_1_BOOLEAN_MUTABLE, false));
		expectedOutputConsumer.accept(getReportItem(3.0, TestResourceId.RESOURCE_4, TestResourcePropertyId.ResourceProperty_4_1_BOOLEAN_MUTABLE, true));
		expectedOutputConsumer.accept(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5));
		expectedOutputConsumer.accept(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE, 2.5));
		expectedOutputConsumer.accept(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 100));
		expectedOutputConsumer.accept(getReportItem(3.0, TestResourceId.RESOURCE_1, TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE, 60));

		
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

	private static final ReportId REPORT_ID = new SimpleReportId("resource property report");

	private static final ReportHeader REPORT_HEADER = ReportHeader.builder().add("Time").add("Resource").add("Property").add("Value").build();
}
