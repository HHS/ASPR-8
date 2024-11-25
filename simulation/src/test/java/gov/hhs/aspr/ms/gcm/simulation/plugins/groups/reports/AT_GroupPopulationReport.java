package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

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

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		
		assertEquals(expectedReportItems, actualReportItems);

		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(REPORT_HOURLY_HEADER, reportHeader);
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

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);

		assertEquals(expectedReportItems, actualReportItems);

		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(REPORT_DAILY_HEADER, reportHeader);
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

		Map<ReportItem, Integer> expectedReportItems = expectedConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);

		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(REPORT_EOS_HEADER, reportHeader);
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

		groupBuilder.associatePersonToGroup(new GroupId(0), new PersonId(0));
		groupBuilder.associatePersonToGroup(new GroupId(0), new PersonId(1));
		groupBuilder.associatePersonToGroup(new GroupId(0), new PersonId(2));

		groupBuilder.associatePersonToGroup(new GroupId(1), new PersonId(1));
		groupBuilder.associatePersonToGroup(new GroupId(1), new PersonId(2));
		groupBuilder.associatePersonToGroup(new GroupId(1), new PersonId(3));
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
		Map<GroupPopulationReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(GroupPopulationReportPluginData.class);
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
		outputItems = testOutputConsumer.getOutputItemMap(GroupPopulationReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);

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
