package plugins.groups.actors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.Experiment;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.ExperimentPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import plugins.reports.testsupport.TestReportItemOutputConsumer;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupPopulationReport.class)
public class AT_GroupPopulationReport {

	@Test
	@UnitTestMethod(name = "init", args = {ActorContext.class})
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
			groupsDataManager.addPersonToGroup(new PersonId(4),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6),groupId);
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 1, 15), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4),new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 2, 48), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 5, 14), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup( new PersonId(7),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9),new GroupId(3));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 5, 34), (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(3),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(4));

		}));
		
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(1, 6, 40), (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addGroupType(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1);
			GroupId groupId = groupsDataManager.addGroup(TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1);
			assertEquals(5, groupId.getValue());			
			groupsDataManager.addPersonToGroup(new PersonId(4),new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(5),new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(6),new GroupId(5));
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(5));

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		
		

		// place the initial data into the expected output consumer
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		for (int hour = 0; hour < 24; hour++) {
			expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_1, 3, 2), 1);
			expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);
			expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 0, hour, TestGroupTypeId.GROUP_TYPE_3, 0, 1), 1);
		}
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 1, TestGroupTypeId.GROUP_TYPE_3, 0, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 2, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 2, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 3, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 3, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 4, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 4, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 5, TestGroupTypeId.GROUP_TYPE_1, 5, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 5, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 6, TestGroupTypeId.GROUP_TYPE_1, 5, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 6, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.HOURLY, 1, 6, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, 4, 1), 1);

		
		Map<ReportItem, Integer> actualReportItems = testConsumers(testPlugin, ReportPeriod.HOURLY, 5524610980534223950L);
		
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
	@UnitTestMethod(name = "init", args = {ActorContext.class})
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
			groupsDataManager.addPersonToGroup(new PersonId(4),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6),groupId);

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4),new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9),new GroupId(3) );
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());

			groupsDataManager.addPersonToGroup(new PersonId(3),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(4));

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		// create a container to hold expected results
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();

		// place the initial data into the expected output consumer

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_1, 3, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 0, TestGroupTypeId.GROUP_TYPE_3, 0, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, 0, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, 2, 1), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, 5, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		Map<ReportItem, Integer> actualReportItems = testConsumers(testPlugin, ReportPeriod.DAILY, 4023600052052959521L);
		assertEquals(expectedReportItems, actualReportItems);
	}

	@Test
	@UnitTestMethod(name = "init", args = {ActorContext.class})
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
			groupsDataManager.addPersonToGroup(new PersonId(4),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(5),groupId);
			groupsDataManager.addPersonToGroup(new PersonId(6),groupId);
		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removePersonFromGroup(new PersonId(4),new GroupId(3));
			groupsDataManager.removeGroup(new GroupId(0));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(2));

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(8),new GroupId(3));
			groupsDataManager.addPersonToGroup(new PersonId(9),new GroupId(3));
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(4, groupId.getValue());
			groupsDataManager.addPersonToGroup(new PersonId(3),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(4),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(5),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(6),new GroupId(4));
			groupsDataManager.addPersonToGroup(new PersonId(7),new GroupId(4));
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		

		// place the initial data into the expected output consumer
		Map<ReportItem, Integer> expectedReportItems = new LinkedHashMap<>();
		expectedReportItems.put(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_1, 5, 2), 1);
		expectedReportItems.put(getReportItem(ReportPeriod.END_OF_SIMULATION, TestGroupTypeId.GROUP_TYPE_2, 3, 1), 1);

		Map<ReportItem, Integer> actualReportItems = testConsumers(testPlugin, ReportPeriod.END_OF_SIMULATION, 2753155357216960554L);

		assertEquals(expectedReportItems, actualReportItems);
	}

	private Map<ReportItem, Integer> testConsumers(Plugin testPlugin, ReportPeriod reportPeriod, long seed) {
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			people.add(new PersonId(i));
		}
		Random random = new Random(seed);

		Experiment.Builder builder = Experiment.builder();

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
		GroupsPluginData groupsPluginData = groupBuilder.build();
		Plugin groupPlugin = GroupsPlugin.getGroupPlugin(groupsPluginData);
		builder.addPlugin(groupPlugin);

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}		
		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		builder.addPlugin(peoplePlugin);

		// add the report plugin
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> new GroupPopulationReport(REPORT_ID, reportPeriod)::init).build();
		Plugin reportPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(random.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		builder.addPlugin(testPlugin);

		// add the output consumer for the actual report items
		ExperimentPlanCompletionObserver experimentPlanCompletionObserver = new ExperimentPlanCompletionObserver();
		TestReportItemOutputConsumer testReportItemOutputConsumer = new TestReportItemOutputConsumer();

		builder.addExperimentContextConsumer(testReportItemOutputConsumer::init);
		builder.addExperimentContextConsumer(experimentPlanCompletionObserver::init);
		builder.reportProgressToConsole(false);
		

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).isPresent());
		assertTrue(experimentPlanCompletionObserver.getActionCompletionReport(0).get().isComplete());

		return testReportItemOutputConsumer.getReportItems().get(0);
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

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("day").add("group_type").add("person_count").add("group_count").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("day").add("hour").add("group_type").add("person_count").add("group_count").build();
	private static final ReportHeader REPORT_EOS_HEADER = ReportHeader.builder().add("group_type").add("person_count").add("group_count").build();
}
