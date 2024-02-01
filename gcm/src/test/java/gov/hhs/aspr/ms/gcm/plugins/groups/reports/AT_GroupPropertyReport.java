package gov.hhs.aspr.ms.gcm.plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestAuxiliaryGroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestAuxiliaryGroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.wrappers.MultiKey;

public class AT_GroupPropertyReport {

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_IncludeGroupProperty() {
		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly included
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// Define a new property to be added at time = 1
		GroupPropertyId newPropertyId = new GroupPropertyId() {
			@Override
			public String toString() {
				return "newPropertyId";
			}
		};

		// have the actor add the new property
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(1)//
																		.build();
			GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
					GroupPropertyDefinitionInitialization	.builder()//
															.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1).setPropertyId(newPropertyId).setPropertyDefinition(propertyDefinition).build();
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();

		// create a set of multi keys to hold the expected property ids that we
		// will explicitly add to the report
		Set<MultiKey> expectedOutput = new LinkedHashSet<>();
		expectedOutput.add(new MultiKey(TestGroupTypeId.GROUP_TYPE_3.toString(), TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK.toString()));
		expectedOutput.add(new MultiKey(TestGroupTypeId.GROUP_TYPE_2.toString(), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK.toString()));
		expectedOutput.add(new MultiKey(TestGroupTypeId.GROUP_TYPE_1.toString(), newPropertyId.toString()));

		/*
		 * Create the plugin data for the report using the same explicit
		 * property ids
		 */
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		builder.setReportLabel(new SimpleReportLabel("report label"));
		builder.setReportPeriod(ReportPeriod.HOURLY);
		builder.setDefaultInclusion(false);
		TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
		builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK;
		builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		builder.includeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, newPropertyId);
		GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

		// use the factory to set up the necessary plugins, remembering to add
		// the report plugin data
		GroupsTestPluginFactory.Factory factory = //
				GroupsTestPluginFactory//
										.factory(30, 3.0, 10.0, 5029722593563249954L, testPluginData)//
										.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

		// execute the simulation with an output consumer
		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		// show that the report items have the chosen property ids
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		Set<MultiKey> actualOutput = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			actualOutput.add(new MultiKey(reportItem.getValue(2), reportItem.getValue(3)));
		}
		assertEquals(expectedOutput, actualOutput);
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_ExcludeGroupProperty() {

		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the explicitly excluded
		 * properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// Define two new properties to be added at time = 1, one that will be
		// included and one that will be explicitly excluded
		GroupPropertyId newPropertyId1 = new GroupPropertyId() {
			@Override
			public String toString() {
				return "newPropertyId1";
			}
		};
		GroupPropertyId newPropertyId2 = new GroupPropertyId() {
			@Override
			public String toString() {
				return "newPropertyId2";
			}
		};

		// have the actor add the new properties
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(1)//
																		.build();
			GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
					GroupPropertyDefinitionInitialization	.builder()//
															.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_1)//
															.setPropertyId(newPropertyId1)//
															.setPropertyDefinition(propertyDefinition)//
															.build();

			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);

			propertyDefinition = PropertyDefinition	.builder()//
													.setType(Double.class)//
													.setDefaultValue(88.0)//
													.build();
			groupPropertyDefinitionInitialization = //
					GroupPropertyDefinitionInitialization	.builder()//
															.setGroupTypeId(TestGroupTypeId.GROUP_TYPE_2)//
															.setPropertyId(newPropertyId2)//
															.setPropertyDefinition(propertyDefinition)//
															.build();

			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();

		// create a set of multi keys to hold the expected property ids that we
		// will implicitly add to the report
		Set<MultiKey> expectedOutput = new LinkedHashSet<>();
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			if (testGroupPropertyId == TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK) {
				continue;
			}
			if (testGroupPropertyId == TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK) {
				continue;
			}
			expectedOutput.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId().toString(), testGroupPropertyId.toString()));
		}
		expectedOutput.add(new MultiKey(TestGroupTypeId.GROUP_TYPE_2.toString(), newPropertyId2.toString()));

		/*
		 * Create the plugin data for the report using the same explicitly
		 * excluded property ids
		 */
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		builder.setReportLabel(new SimpleReportLabel("report label"));
		builder.setReportPeriod(ReportPeriod.DAILY);
		builder.setDefaultInclusion(true);
		TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
		builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		testGroupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK;
		builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		builder.excludeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, newPropertyId1);
		GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

		// use the factory to set up the necessary plugins, remembering to add
		// the report plugin data
		GroupsTestPluginFactory.Factory factory = //
				GroupsTestPluginFactory//
										.factory(30, 3.0, 10.0, 5029722593563249954L, testPluginData)//
										.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

		// execute the simulation with an output consumer
		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		// show that the report items have the chosen property ids
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		Set<MultiKey> actualOutput = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			actualOutput.add(new MultiKey(reportItem.getValue(1), reportItem.getValue(2)));
		}

		assertEquals(expectedOutput, actualOutput);
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_DefaultInclusion() {

		/*
		 * This test shows that the report produces report items with the
		 * correct selected properties as a function of the default property
		 * inclusion policy
		 */

		/*
		 * We will explicitly include and exclude one property and gather the
		 * remaining properties in a set
		 */
		TestGroupPropertyId includedPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		TestGroupPropertyId excludedPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK;

		Set<TestGroupPropertyId> middlePropertyIds = new LinkedHashSet<>();
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			middlePropertyIds.add(testGroupPropertyId);
		}
		middlePropertyIds.remove(includedPropertyId);
		middlePropertyIds.remove(excludedPropertyId);

		// There are three possibilities for setting the default inclusion
		// policy
		enum DefaultInclusionPolicy {
			TRUE, FALSE, UNSPECIFIED
		}
		;

		for (DefaultInclusionPolicy defaultInclusionPolicy : DefaultInclusionPolicy.values()) {
			// build the report plugin data
			GroupPropertyReportPluginData.Builder reportBuilder = GroupPropertyReportPluginData.builder();
			reportBuilder.setReportPeriod(ReportPeriod.DAILY);
			reportBuilder.setReportLabel(new SimpleReportLabel("report label"));
			switch (defaultInclusionPolicy) {
			case FALSE:
				reportBuilder.setDefaultInclusion(false);
				break;
			case TRUE:
				reportBuilder.setDefaultInclusion(true);
				break;
			default:
				// do nothing
			}

			reportBuilder.includeGroupProperty(includedPropertyId.getTestGroupTypeId(), includedPropertyId);
			reportBuilder.excludeGroupProperty(excludedPropertyId.getTestGroupTypeId(), excludedPropertyId);
			GroupPropertyReportPluginData groupPropertyReportPluginData = reportBuilder.build();

			/*
			 * Add a single test actor plan that does nothing so that we can use
			 * the factory for convenience
			 */
			TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			}));
			TestPluginData testPluginData = pluginDataBuilder.build();

			// build the global plugin using the report plugin data and the
			// standard global plugin data build
			GroupsTestPluginFactory.Factory factory = //
					GroupsTestPluginFactory//
											.factory(30, 3.0, 10.0, 274849177891016889L, testPluginData)//
											.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

			TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																	.addPlugins(factory.getPlugins())//
																	.build()//
																	.execute();

			// gather from the report items the property ids that were actually
			// included in the report
			Set<TestGroupPropertyId> actualPropertyIds = new LinkedHashSet<>();
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
			for (ReportItem reportItem : outputItems.keySet()) {
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.valueOf(reportItem.getValue(2));
				actualPropertyIds.add(testGroupPropertyId);
			}

			// build the expected property ids based on the policy
			Set<TestGroupPropertyId> expectedPropertyIds = new LinkedHashSet<>();
			expectedPropertyIds.add(includedPropertyId);

			switch (defaultInclusionPolicy) {
			case FALSE:
				// only the single included property
				break;
			default:
				expectedPropertyIds.addAll(middlePropertyIds);
				break;
			}

			// show that the property id sets are equals
			assertEquals(expectedPropertyIds, actualPropertyIds);
		}

	}

	@Test
	@UnitTestConstructor(target = GroupPropertyReport.class, args = { GroupPropertyReportPluginData.class })
	public void testConstructor() {
		// construction is covered by the other tests

		/*
		 * Due to this being a periodic report with a super constructor, it is
		 * not possible for the report to throw a Contract exception when given
		 * a null GroupPropertyReportPluginData.
		 */

		// precondition test: if the GroupPropertyReportPluginData is null
		assertThrows(NullPointerException.class, () -> new GroupPropertyReport(null));
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
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ReportPeriod() {
		/*
		 * This test shows that the report produces report items with the
		 * correct report period time values in the header and body lines of the
		 * report.
		 */
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			// add a test actor so that we can use the factory
			TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			}));
			TestPluginData testPluginData = pluginDataBuilder.build();

			/*
			 * create the report with the report period
			 */
			GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
			builder.setReportLabel(new SimpleReportLabel("report label"));
			builder.setReportPeriod(reportPeriod);
			GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

			// use the factory to set up the necessary plugins, remembering to
			// add
			// the report plugin data
			GroupsTestPluginFactory.Factory factory = //
					GroupsTestPluginFactory//
											.factory(30, 3.0, 10.0, 5029722593563249954L, testPluginData)//
											.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

			// execute the simulation with an output consumer
			TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																	.addPlugins(factory.getPlugins())//
																	.build()//
																	.execute();
			// show that the report items have the chosen property ids
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);

			for (ReportItem reportItem : outputItems.keySet()) {
				ReportHeader reportHeader = reportItem.getReportHeader();
				switch (reportPeriod) {
				case DAILY:
					assertEquals(REPORT_DAILY_HEADER, reportHeader);
					assertDoesNotThrow(() -> Integer.parseInt(reportItem.getValue(0)));
					assertDoesNotThrow(() -> TestGroupTypeId.valueOf(reportItem.getValue(1)));
					break;
				case END_OF_SIMULATION:
					assertEquals(REPORT_END_OF_SIMULATION_HEADER, reportHeader);
					assertDoesNotThrow(() -> TestGroupTypeId.valueOf(reportItem.getValue(0)));
					break;
				case HOURLY:
					assertEquals(REPORT_HOURLY_HEADER, reportHeader);
					assertDoesNotThrow(() -> Integer.parseInt(reportItem.getValue(0)));
					assertDoesNotThrow(() -> Integer.parseInt(reportItem.getValue(1)));
					assertDoesNotThrow(() -> TestGroupTypeId.valueOf(reportItem.getValue(2)));
					break;
				default:
					throw new RuntimeException("unhandled case " + reportPeriod);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ReportLabel() {

		/*
		 * This test shows that the report produces report items with the
		 * correct report label.
		 */
		for (int i = 0; i < 5; i++) {

			ReportLabel reportLabel = new SimpleReportLabel(i);

			// add a test actor so that we can use the factory
			TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			}));
			TestPluginData testPluginData = pluginDataBuilder.build();

			/*
			 * create the report with the report period
			 */
			GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(ReportPeriod.DAILY);
			GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

			// use the factory to set up the necessary plugins, remembering to
			// add
			// the report plugin data
			GroupsTestPluginFactory.Factory factory = //
					GroupsTestPluginFactory//
											.factory(30, 3.0, 10.0, 5029722593563249954L, testPluginData)//
											.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

			// execute the simulation with an output consumer
			TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																	.addPlugins(factory.getPlugins())//
																	.build()//
																	.execute();
			// show that the report items have the chosen property ids
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
			assertFalse(outputItems.isEmpty());
			for (ReportItem reportItem : outputItems.keySet()) {
				assertEquals(reportLabel, reportItem.getReportLabel());
			}
		}
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
			groupsDataManager.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45);
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
			PropertyDefinition propertyDefinition = TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK.getPropertyDefinition();
			GroupPropertyDefinitionInitialization groupPropertyDefinitionInitialization = //
					GroupPropertyDefinitionInitialization	.builder()//
															.setGroupTypeId(groupTypeId)//
															.setPropertyDefinition(propertyDefinition)//
															.setPropertyId(groupPropertyId)//
															.build();
			groupsDataManager.defineGroupProperty(groupPropertyDefinitionInitialization);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 2, 3), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(2), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 5, 0), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
			groupsDataManager.setGroupPropertyValue(new GroupId(5), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(getTime(0, 5, 16), (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(0), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(5), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		// build the expected output

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 3));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 123, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.HOURLY, 0, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 77, 1));

		if (includeNewProperties) {
			expectedOutputConsumer.accept(
					getReportItem(ReportPeriod.HOURLY, 0, 2, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(
					getReportItem(ReportPeriod.HOURLY, 0, 3, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(
					getReportItem(ReportPeriod.HOURLY, 0, 4, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(
					getReportItem(ReportPeriod.HOURLY, 0, 5, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
			expectedOutputConsumer.accept(
					getReportItem(ReportPeriod.HOURLY, 0, 6, TestAuxiliaryGroupTypeId.GROUP_AUX_TYPE_1, TestAuxiliaryGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 3));
		}

		// build the report
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.setDefaultInclusion(includeNewProperties);
		builder.setReportPeriod(ReportPeriod.HOURLY);
		for (TestGroupPropertyId testGroupPropertyId : TestGroupPropertyId.values()) {
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
		}
		builder.includeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		builder.includeGroupProperty(TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK);

		GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 0, 0, 6092832510476200219L, testPluginData).setGroupPropertyReportPluginData(groupPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_Content() {
		testDailyAllProperties();
		testHourlySelectProperties(false);
		testHourlySelectProperties(true);
	}

	private void testDailyAllProperties() {

		/*
		 * Test for a daily report that includes all property ids.
		 * 
		 * 
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

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45);

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5);

		}));

		// have the agent add a new group of type 1 with three people
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1.1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(0));
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 65);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.8, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(6.0, (c) -> {
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		// create a container to hold expected results
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		// build the expected output
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 4, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 5, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_1, TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 127, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 0.0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0, 1));//
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_1_BOOLEAN_IMMUTABLE_NO_TRACK, false, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK, 0, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 6, TestGroupTypeId.GROUP_TYPE_3, TestGroupPropertyId.GROUP_PROPERTY_3_3_DOUBLE_IMMUTABLE_NO_TRACK, 0.0, 1));

		// build the report with all properties selected
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		builder.setReportLabel(REPORT_LABEL);
		builder.setReportPeriod(ReportPeriod.DAILY);

		GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 0, 0, 6092832510476200219L, testPluginData).setGroupPropertyReportPluginData(groupPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(factory.getPlugins())//
																.build()//
																.execute();

		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);

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

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_ReportHeader() {

		/*
		 * This test shows that the report produces report items with the
		 * correct report period time values in the header and body lines of the
		 * report.
		 */
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			// add a test actor so that we can use the factory
			TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
			pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			}));
			TestPluginData testPluginData = pluginDataBuilder.build();

			/*
			 * create the report with the report period
			 */
			GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
			builder.setReportLabel(new SimpleReportLabel("report label"));
			builder.setReportPeriod(reportPeriod);
			GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

			// use the factory to set up the necessary plugins, remembering to
			// add
			// the report plugin data
			GroupsTestPluginFactory.Factory factory = //
					GroupsTestPluginFactory//
											.factory(30, 3.0, 10.0, 5029722593563249954L, testPluginData)//
											.setGroupPropertyReportPluginData(groupPropertyReportPluginData);

			// execute the simulation with an output consumer
			TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																	.addPlugins(factory.getPlugins())//
																	.build()//
																	.execute();

			// show that the report items have the chosen property ids
			Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);

			for (ReportItem reportItem : outputItems.keySet()) {
				ReportHeader reportHeader = reportItem.getReportHeader();
				switch (reportPeriod) {
				case DAILY:
					assertEquals(REPORT_DAILY_HEADER, reportHeader);
					break;
				case END_OF_SIMULATION:
					assertEquals(REPORT_END_OF_SIMULATION_HEADER, reportHeader);
					break;
				case HOURLY:
					assertEquals(REPORT_HOURLY_HEADER, reportHeader);
					break;
				default:
					throw new RuntimeException("unhandled case " + reportPeriod);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// have the agent add new groups and set property values
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

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, true);

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK, 45);

			groupsDataManager.setGroupPropertyValue(new GroupId(3), TestGroupPropertyId.GROUP_PROPERTY_1_3_DOUBLE_MUTABLE_NO_TRACK, 16.5);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.removeGroup(new GroupId(0));
			groupsDataManager.addGroup(TestGroupTypeId.GROUP_TYPE_2);
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 17);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK, 800.0);

		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(5.7, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			groupsDataManager.setGroupPropertyValue(new GroupId(1), TestGroupPropertyId.GROUP_PROPERTY_2_1_BOOLEAN_MUTABLE_TRACK, false);
			groupsDataManager.setGroupPropertyValue(new GroupId(4), TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 65);
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		// build the report with all properties selected
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		builder.setReportLabel(REPORT_LABEL)
				.setReportPeriod(ReportPeriod.DAILY)
				.setDefaultInclusion(true)
				.excludeGroupProperty(TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK)
				.includeGroupProperty(TestGroupTypeId.GROUP_TYPE_2, TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK);

		GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

		Factory factory = GroupsTestPluginFactory.factory(0, 0, 0, 6092832510476200219L, testPluginData).setGroupPropertyReportPluginData(groupPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		// show that the plugin data persists after simulation
		Map<GroupPropertyReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(GroupPropertyReportPluginData.class);
		assertEquals(1, outputItems.size());
		GroupPropertyReportPluginData groupPropertyReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(groupPropertyReportPluginData, groupPropertyReportPluginData2);

		// Test without producing simulation

		testOutputConsumer = TestSimulation	.builder()//
				.addPlugins(factory.getPlugins())//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		// show that when the simulation state is not being produced, there is no output plugin data+
		outputItems = testOutputConsumer.getOutputItemMap(GroupPropertyReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("group property report");

	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("day").add("group_type").add("property").add("value").add("group_count").build();
	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("day").add("hour").add("group_type").add("property").add("value").add("group_count").build();
	private static final ReportHeader REPORT_END_OF_SIMULATION_HEADER = ReportHeader.builder().add("group_type").add("property").add("value").add("group_count").build();

}
