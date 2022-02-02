package plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Random;

import org.junit.jupiter.api.Test;

import nucleus.ReportId;
import nucleus.SimpleReportId;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.components.ComponentPlugin;
import plugins.groups.GroupPlugin;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupMembershipRemovalEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.initialdata.GroupInitialData;
import plugins.groups.support.GroupId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = GroupPopulationReport.class)
public class AT_GroupPopulationReport {

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testHourlyReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent to move people in and out of groups
		pluginBuilder.addAgent("agent");

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(0, 0, 0), (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(3, groupId.getValue());

			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), groupId));

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(1, 1, 15), (c) -> {
			c.resolveEvent(new GroupMembershipRemovalEvent(new PersonId(4), new GroupId(3)));
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(0)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(1, 2, 48), (c) -> {
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(2)));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(1, 5, 14), (c) -> {
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(8), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(9), new GroupId(3)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(getTime(1, 5, 34), (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(4, groupId.getValue());
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(3), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(4)));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// create a container to hold expected results
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		// place the initial data into the expected output consumer
		for (int hour = 0; hour < 24; hour++) {
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0,hour, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0,hour, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0,hour, TestGroupTypeId.GROUP_TYPE_3, 0, 1));
			
			
		}
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,0, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,0, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,0, TestGroupTypeId.GROUP_TYPE_3, 0, 1));
		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,1, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,1, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,1, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,2, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,2, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,3, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,3, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,4, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,4, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,5, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1,5, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		testConsumers(actionPlugin, ReportPeriod.HOURLY, expectedOutputConsumer, 5524610980534223950L);
	}

	/*
	 * Returns the conversion into double valued days
	 * 
	 * preconditions: all entries are non-negative and in their natural ranges
	 */
	private double getTime(int days, int hours, int minutes) {
		return days + (double) hours / 24 + (double) minutes / 1440;
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testDailyReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent to move people in and out of groups
		pluginBuilder.addAgent("agent");

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(3, groupId.getValue());

			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), groupId));

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.1, (c) -> {
			c.resolveEvent(new GroupMembershipRemovalEvent(new PersonId(4), new GroupId(3)));
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(0)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.5, (c) -> {
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(2)));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.7, (c) -> {
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(8), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(9), new GroupId(3)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.8, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(4, groupId.getValue());
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(3), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(4)));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// create a container to hold expected results
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		// place the initial data into the expected output consumer
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		testConsumers(actionPlugin, ReportPeriod.DAILY, expectedOutputConsumer, 4023600052052959521L);
	}

	@Test
	@UnitTestMethod(name = "init", args = {})
	public void testEndOfSimReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		// add an agent to move people in and out of groups
		pluginBuilder.addAgent("agent");

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();
			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(3, groupId.getValue());

			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), groupId));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), groupId));

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1.1, (c) -> {
			c.resolveEvent(new GroupMembershipRemovalEvent(new PersonId(4), new GroupId(3)));
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(0)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2.5, (c) -> {
			c.resolveEvent(new GroupRemovalRequestEvent(new GroupId(2)));

		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.7, (c) -> {
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(8), new GroupId(3)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(9), new GroupId(3)));
		}));

		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(5.8, (c) -> {

			PersonGroupDataView personGroupDataView = c.getDataView(PersonGroupDataView.class).get();

			c.resolveEvent(new GroupCreationEvent(TestGroupTypeId.GROUP_TYPE_1));
			GroupId groupId = personGroupDataView.getLastIssuedGroupId().get();
			assertEquals(4, groupId.getValue());
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(3), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(4), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(5), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(6), new GroupId(4)));
			c.resolveEvent(new GroupMembershipAdditionEvent(new PersonId(7), new GroupId(4)));

		}));

		ActionPlugin actionPlugin = pluginBuilder.build();

		// create a container to hold expected results
		TestReportItemOutputConsumer expectedOutputConsumer = new TestReportItemOutputConsumer();

		// place the initial data into the expected output consumer

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		testConsumers(actionPlugin, ReportPeriod.END_OF_SIMULATION, expectedOutputConsumer, 2753155357216960554L);
	}

	private void testConsumers(ActionPlugin actionPlugin, ReportPeriod reportPeriod, TestReportItemOutputConsumer expectedOutputConsumer, long seed) {

		Random random = new Random(seed);

		// create a list 100 of people

		Builder builder = Simulation.builder();

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

		groupBuilder.addGroup(new GroupId(0), TestGroupTypeId.GROUP_TYPE_1);
		groupBuilder.addGroup(new GroupId(1), TestGroupTypeId.GROUP_TYPE_2);
		groupBuilder.addGroup(new GroupId(2), TestGroupTypeId.GROUP_TYPE_3);

		// add a few of the people to the groups

		groupBuilder.addPersonToGroup(new GroupId(0), new PersonId(0));
		groupBuilder.addPersonToGroup(new GroupId(0), new PersonId(1));
		groupBuilder.addPersonToGroup(new GroupId(0), new PersonId(2));

		groupBuilder.addPersonToGroup(new GroupId(1), new PersonId(1));
		groupBuilder.addPersonToGroup(new GroupId(1), new PersonId(2));
		groupBuilder.addPersonToGroup(new GroupId(1), new PersonId(3));

		builder.addPlugin(GroupPlugin.PLUGIN_ID, new GroupPlugin(groupBuilder.build())::init);

		// add the people plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();

		for (int i = 0; i < 10; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the report plugin
		ReportsInitialData reportsInitialData = ReportsInitialData.builder().addReport(REPORT_ID, () -> new GroupPopulationReport(reportPeriod)::init).build();
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(reportsInitialData)::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, StochasticsPlugin.builder().setSeed(random.nextLong()).build()::init);

		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// add the output consumer for the actual report items
		TestReportItemOutputConsumer actualOutputConsumer = new TestReportItemOutputConsumer();
		builder.setOutputConsumer(actualOutputConsumer);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		if (!actionPlugin.allActionsExecuted()) {
			throw new ContractException(ActionError.ACTION_EXECUTION_FAILURE);
		}

	
		assertEquals(expectedOutputConsumer, actualOutputConsumer);
	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportId(REPORT_ID);

		switch (reportPeriod) {
		case DAILY:
			builder.setReportHeader(REPORT_DAILY_HEADER);
			break;
		case END_OF_SIMULATION:
			builder.setReportHeader(REPORT_EOS_HEADER);
			break;
		case HOURLY:
			builder.setReportHeader(REPORT_HOURLY_HEADER);
			break;
		default:
			throw new RuntimeException("unhandled case " + reportPeriod);

		}

		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	private static final ReportId REPORT_ID = new SimpleReportId("group population property report");

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("Day").add("GroupType").add("PersonCount").add("GroupCount").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("Day").add("Hour").add("GroupType").add("PersonCount").add("GroupCount").build();
	private static final ReportHeader REPORT_EOS_HEADER = ReportHeader.builder().add("GroupType").add("PersonCount").add("GroupCount").build();
}
