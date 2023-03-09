package plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyDefinitionInitialization;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsTestPluginFactory;
import plugins.groups.testsupport.TestAuxiliaryGroupPropertyId;
import plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.reports.testsupport.ReportsTestPluginFactory;
import plugins.util.properties.PropertyDefinition;
import util.annotations.UnitTestMethod;

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
	@UnitTestMethod(target = GroupPropertyReport.class, name = "builder", args = {})
	public void testBuilder() {
		// test covered by the consumers-based-tests in this class

		// show that the method doesn't return null
		assertNotNull(GroupPropertyReport.builder());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "addAllProperties", args = { GroupTypeId.class })
	public void testAddAllProperties() {
		// test covered by the consumers-based tests in this class

		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().addAllProperties(null));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "addProperty", args = { GroupTypeId.class,
			GroupPropertyId.class })
	public void testAddProperty() {
		// test covered by the consumers-based tests in this class

		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().addProperty(null,
				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK));
		assertThrows(RuntimeException.class,
				() -> GroupPropertyReport.builder().addProperty(TestGroupTypeId.GROUP_TYPE_1, null));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "build", args = {})
	public void testBuild() {
		// test covered by the consumers-based tests in this class
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "includeNewProperties", args = { boolean.class })
	public void includeNewProperties() {
		// test covered by the consumers-based tests in this class
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {
		// test covered by the consumers-based tests in this class
		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().setReportPeriod(null));
		assertThrows(RuntimeException.class,
				() -> GroupPropertyReport.builder().setReportPeriod(ReportPeriod.END_OF_SIMULATION));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {
		// test covered by the consumers-based tests in this class

		// precondition tests:
		assertThrows(RuntimeException.class, () -> GroupPropertyReport.builder().setReportLabel(null));
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testHourlySelectProperties() {
		testHourlySelectProperties(false);
		testHourlySelectProperties(true);
	}

	private void testHourlySelectProperties(boolean includeNewProperties) {
		
		/*
		 * We will add one agent to move assign property values to groups and
		 * create and remove groups. Report items from the report will be
		 * collected in an output consumer and compared to the expected output.
		 */

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 0, 0), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);// group 0
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);// group 1
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);// group 2
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);// group 3
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);// group 4
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);// group 5

		}));

		//
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 1, 10), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(0),
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
			groupsDataManager.setGroupPropertyValue(new GroupId(3),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 1, 15), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			GroupTypeId groupTypeId = TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1;
			groupsDataManager.addGroupType(groupTypeId);

			/*
			 * Add three groups before we define the group property so that we
			 * can show the report initializing values on first encountering the
			 * new definition because the report skips reporting zero counts
			 */

			groupsDataManager.addGroup(groupTypeId);
			groupsDataManager.addGroup(groupTypeId);
			groupsDataManager.addGroup(groupTypeId);

			GroupPropertyId groupPropertyId = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;
			PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK
					.getPropertyDefinition();
			GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
					GroupPropertyDefinitionInitialization.builder()//
							.setGroupTypeId(groupTypeId)//
							.setPropertyDefinition(propertyDefinition)//
							.setPropertyId(groupPropertyId)//
							.build();
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 2, 3), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(2),
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(4),
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 5, 0), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
			groupsDataManager.setGroupPropertyValue(new GroupId(3),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
			groupsDataManager.setGroupPropertyValue(new GroupId(5),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 5, 16), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(0),
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(5),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		// build the expected output

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_2,TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77, 1));

		if (includeNewProperties) {
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1,TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1,TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1,TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1,TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1,TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
		}

		// build the report
		GroupPropertyReport.Builder builder = GroupPropertyReport.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.includeNewProperties(includeNewProperties);
		builder.setReportPeriod(ReportPeriod.HOURLY);
		builder.addProperty(TestGroupTypeId.GROUP_TYPE_1,
				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		builder.addProperty(TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK);
		GroupPropertyReport groupPropertyReport = builder.build();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 6092832510476200219L, testPluginData).getPlugins();
		plugins.add(ReportsTestPluginFactory.getPluginFromReport(groupPropertyReport::init));

		TestOutputConsumer outputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, outputConsumer);

		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = outputConsumer.getOutputItems(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);
		
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testDailyAllProperties() {

		/*
		 * We will add one agent to move, assign property values to groups and
		 * create and remove groups. Report items from the report will be
		 * collected in an output consumer and compared to the expected output.
		 */

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			GroupId groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(0, groupId.getValue());

			groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
			assertEquals(1, groupId.getValue());

			groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_3);
			assertEquals(2, groupId.getValue());

			groupId = groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_1);
			assertEquals(3, groupId.getValue());

			groupsDataManager.setGroupPropertyValue(new GroupId(3),
					TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);

			groupsDataManager.setGroupPropertyValue(new GroupId(3),
					TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45);

			groupsDataManager.setGroupPropertyValue(new GroupId(3),
					TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5);

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(0));
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17);
			groupsDataManager.setGroupPropertyValue(new GroupId(4),
					TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1),
					TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(4),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 65);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1),
					TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true);
			groupsDataManager.setGroupPropertyValue(new GroupId(4),
					TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(6.0, (c) -> {}));
		
		TestPluginData testPluginData = pluginBuilder.build();

		// create a container to hold expected results
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();
		

		// build the expected output
		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 0.0, 1));		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));


		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));


		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));
		
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1,				TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2,				TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));//
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3,				TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		

		// build the report with all properties selected
		GroupPropertyReport.Builder builder = GroupPropertyReport.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.setReportPeriod(ReportPeriod.DAILY);

		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.addProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		}
		GroupPropertyReport groupPropertyReport = builder.build();

		TestOutputConsumer outputConsumer = new TestOutputConsumer();

		List<Plugin> plugins = GroupsTestPluginFactory.factory(0, 0, 0, 6092832510476200219L, testPluginData).getPlugins();

		plugins.add(ReportsTestPluginFactory.getPluginFromReport(groupPropertyReport::init));

		TestSimulation.executeSimulation(plugins, outputConsumer);

		
		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = outputConsumer.getOutputItems(ReportItem.class);
		
		assertEquals(expectedReportItems, actualReportItems);

	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);

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

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("group property report");

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("day").add("group_type")
			.add("property").add("value").add("group_count").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("day").add("hour")
			.add("group_type").add("property").add("value").add("group_count").build();

}