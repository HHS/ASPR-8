package plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.groups.GroupPlugin;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportItem.Builder;
import plugins.reports.support.ReportPeriod;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupPropertyReport.class)
public class AT_GroupPropertyReport {

	// 4869313312961558350L

	/*
	 * Returns the conversion into double valued days
	 * 
	 * preconditions: all entries are non-negative and in their natural ranges
	 */
	private double getTime(int days, int hours, int minutes) {
		return days + (double) hours / 24 + (double) minutes / 1440;
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "addAllProperties", args = { GroupTypeId.class })
	public void testAddAllProperties() {
		// test covered by the consumers-based tests in this class

		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().addAllProperties(null));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "addProperty", args = { GroupTypeId.class, GroupPropertyId.class })
	public void testAddProperty() {
		// test covered by the consumers-based tests in this class

		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().addProperty(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().addProperty(TestGroupTypeId.GROUP_TYPE_1, null));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "build", args = {})
	public void testBuild() {
		// test covered by the consumers-based tests in this class
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class, GroupPropertyId.class })
	public void testSetReportPeriod() {
		// test covered by the consumers-based tests in this class
		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().setReportPeriod(null));
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().setReportPeriod(ReportPeriod.END_OF_SIMULATION));
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testHourlySelectProperties() {

		/*
		 * We will add one agent to move assign property values to groups and
		 * create and remove groups. Report items from the report will be
		 * collected in an output consumer and compared to the expected output.
		 */

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent to move people in and out of groups
		pluginBuilder.addAgent("agent");

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 0, 0), (c) -> {
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));

		}));

		//
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 1, 10), (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 2, 3), (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(2), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 5, 0), (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(5), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 5, 16), (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(5), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77));
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// create a container to hold expected results
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		// build the expected output

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 0, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 0, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77, 1));

		// build the report with all properties selected
		GroupPropertyReport.Builder builder = GroupPropertyReport.builder();
		builder.setReportPeriod(ReportPeriod.HOURLY);

		builder.addProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		builder.addProperty(TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK);

		GroupPropertyReport groupPropertyReport = builder.build();

		testConsumers(actionPlugin, groupPropertyReport, expectedOutputConsumer, 6092832510476200219L);

	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testDailyAllProperties() {

		/*
		 * We will add one agent to move assign property values to groups and
		 * create and remove groups. Report items from the report will be
		 * collected in an output consumer and compared to the expected output.
		 */

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent to move people in and out of groups
		pluginBuilder.addAgent("agent");

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_3));

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(3, groupId.getValue());
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5));
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.1, (c) -> {
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(0)));
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_2));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.5, (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.7, (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 65));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.8, (c) -> {
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true));
			c.resolveEvent(new GroupPropertyValueAssignmentEvent(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127));
		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// create a container to hold expected results
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		// build the expected output
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		// build the report with all properties selected
		GroupPropertyReport.Builder builder = GroupPropertyReport.builder();
		builder.setReportPeriod(ReportPeriod.DAILY);

		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.addProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		}
		GroupPropertyReport groupPropertyReport = builder.build();

		testConsumers(actionPlugin, groupPropertyReport, expectedOutputConsumer, 6092832510476200219L);

	}

	private void testConsumers(ActionPlugin actionPlugin, GroupPropertyReport groupPropertyReport, TestReportItemOutputConsumer expectedOutputConsumer, long seed) {

		EngineBuilder engineBuilder = Engine.builder();

		// add the group plugin
		GroupInitialData.Builder groupBuilder = GroupInitialData.builder();
		// add group types
		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			groupBuilder.addGroupTypeId(testGroupTypeId);
		}
		// define group properties
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			groupBuilder.defineGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId, testGroupPropertyId.getPropertyDefinition());
		}

		engineBuilder.addPlugin(GroupPlugin.PLUGIN_ID, new GroupPlugin(groupBuilder.build())::init);

		// add the people plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the report plugin

		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(REPORT_ID, () -> groupPropertyReport::init).build();
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);

		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// add the output consumer for the actual report items
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		engineBuilder.setOutputConsumer(actualOutputConsumer);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

		assertEquals(expectedOutputConsumer, actualOutputConsumer);
	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);

		switch (reportPeriod) {
		case DAILY:
			builder.setReportHeader(REPORT_DAILY_HEADER);
			break;

		case HOURLY:
			builder.setReportHeader(REPORT_HOURLY_HEADER);
			break;
		case END_OF_SIMULATION:// fall through
		default:
			throw new RuntimeException("unhandled case " + reportPeriod);

		}

		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("group property report");

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("Day").add("GroupType").add("Property").add("Value").add("GroupCount").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("Day").add("Hour").add("GroupType").add("Property").add("Value").add("GroupCount").build();

}
