package plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.GroupsPluginData;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.support.PersonId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_GroupPopulationReport {

	@Test
	@UnitTestConstructor(target = GroupPopulationReport.class, args = { GroupPopulationReportPluginData.class })
	public void testConstructor() {

		/*
		 * Nothing to test -- it is not possible to pass a null report label nor
		 * a null report period
		 */

	}

	@Test
	@UnitTestMethod(target = GroupPopulationReport.class, name = "init", args = { ReportContext.class })
	public void testHourlyReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 0, 0), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(3, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(4), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6), groupId);
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 1, 15), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4), new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 2, 48), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 5, 14), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9), new GroupId(3));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 5, 34), (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(3), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(4));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 6, 40), (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addGroupType(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1);
			GroupId groupId = groupsDataManager.addGroup(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1);
			assertEquals(5, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(4), new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(5), new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(6), new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(5));

		}));

		TestPluginData testPluginData = pluginBuilder.build();

		// place the initial data into the expected output consumer
		TestOutputConsumer expectedConsumer = new TestOutputConsumer();

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		for (int hour = 1; hour < 24; hour++) {
			expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
			expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
			expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_3, 0, 1));
		}

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 2, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 2, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 2, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 3, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 3, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 4, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 4, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 5, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 5, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 6, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 6, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 7, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 7, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.HOURLY, 1, 7, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, 4, 1));

		GroupPopulationReportPluginData groupPopulationReportPluginData = GroupPopulationReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.HOURLY).build(); // (REPORT_LABEL,
																																																// ReportPeriod.HOURLY);

		Factory factory = GroupsTestPluginFactory	.factory(10, 0, 3, 5524610980534223950L, testPluginData)//
													.setGroupsPluginData(getGroupsPluginData())//
													.setGroupPopulationReportPluginData(groupPopulationReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItems(ReportItem.class);

		assertEquals(expectedReportItems, actualReportItems);

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
	@UnitTestMethod(target = GroupPopulationReport.class, name = "init", args = { ReportContext.class })
	public void testDailyReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);

			assertEquals(3, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(4), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6), groupId);

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4), new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9), new GroupId(3));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());

			groupsDataManager.addPersonToGroup(new PersonId(3), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(4));

		}));

		TestPluginData testPluginData = pluginBuilder.build();

		// create a container to hold expected results
		TestOutputConsumer expectedConsumer = new TestOutputConsumer();

		// place the initial data into the expected output consumer

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, 3, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, 3, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, 0, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, 2, 1));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		GroupPopulationReportPluginData groupPopulationReportPluginData = GroupPopulationReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.DAILY).build(); // (REPORT_LABEL,
																																																// ReportPeriod.HOURLY);

		Factory factory = GroupsTestPluginFactory	.factory(10, 0, 3, 4023600052052959521L, testPluginData)//
													.setGroupsPluginData(getGroupsPluginData())//
													.setGroupPopulationReportPluginData(groupPopulationReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItems(ReportItem.class);

		assertEquals(expectedReportItems, actualReportItems);

	}

	@Test
	@UnitTestMethod(target = GroupPopulationReport.class, name = "init", args = { ReportContext.class })
	public void testEndOfSimReport() {

		/*
		 * We will add one agent to move people in and out of groups and create
		 * and remove groups. Report items from the report will be collected in
		 * an output consumer and compared to the expected output.
		 */

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(3, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(4), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6), groupId);
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4), new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9), new GroupId(3));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(3), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6), new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(4));
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		// place the initial data into the expected output consumer
		// create a container to hold expected results
		TestOutputConsumer expectedConsumer = new TestOutputConsumer();
		expectedConsumer.accept(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_1, 5, 2));
		expectedConsumer.accept(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_2, 3, 1));

		GroupPopulationReportPluginData groupPopulationReportPluginData = GroupPopulationReportPluginData	.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.END_OF_SIMULATION)
																											.build();

		Factory factory = GroupsTestPluginFactory	.factory(10, 0, 3, 6092832510476200219L, testPluginData)//
													.setGroupsPluginData(getGroupsPluginData())//
													.setGroupPopulationReportPluginData(groupPopulationReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);
	}

	private GroupsPluginData getGroupsPluginData() {

		// add the group plugin
		GroupsPluginData.Builder groupBuilder = GroupsPluginData.builder();
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
		return groupBuilder.build();
	}

	@Test
	@UnitTestMethod(target = GroupPopulationReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(3, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(4), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5), groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6), groupId);
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8), new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9), new GroupId(3));
		}));

		GroupPopulationReportPluginData groupPopulationReportPluginData = GroupPopulationReportPluginData.builder().setReportLabel(REPORT_LABEL).setReportPeriod(ReportPeriod.HOURLY).build();

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = GroupsTestPluginFactory	.factory(10, 0, 3, 4023600052052959521L, testPluginData)//
													.setGroupsPluginData(getGroupsPluginData())//
													.setGroupPopulationReportPluginData(groupPopulationReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.setProduceSimulationStateOnHalt(true)//
																.setSimulationHaltTime(20)//
																.build()//
																.execute();

		// show that the output plugin data is similar to the input plugin data
		Map<GroupPopulationReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItems(GroupPopulationReportPluginData.class);
		assertEquals(1, outputItems.size());
		GroupPopulationReportPluginData groupPopulationReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(groupPopulationReportPluginData, groupPopulationReportPluginData2);

		// Test without producing simulation

		testOutputConsumer = TestSimulation	.builder()//
											.addPlugins(factory.getPlugins())//
											.setProduceSimulationStateOnHalt(false)//
											.setSimulationHaltTime(20)//
											.build()//
											.execute();

		// show that when the simulation state is not being produced, there is
		// no output plugin data+
		outputItems = testOutputConsumer.getOutputItems(GroupPopulationReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);

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

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("group population property report");

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("day").add("group_type").add("person_count").add("group_count").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("day").add("hour").add("group_type").add("person_count").add("group_count").build();
	private static final ReportHeader REPORT_EOS_HEADER = ReportHeader.builder().add("group_type").add("person_count").add("group_count").build();
}
