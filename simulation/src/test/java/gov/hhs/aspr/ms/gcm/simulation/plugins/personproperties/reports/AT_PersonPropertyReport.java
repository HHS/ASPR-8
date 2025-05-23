package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_PersonPropertyReport {
	@Test
	@UnitTestConstructor(target = PersonPropertyReport.class, args = {
			PersonPropertyReportPluginData.class }, tags = {})
	public void testConstructor() {
		// construction is covered by the other tests

		/*
		 * Due to this being a periodic report with a super constructor, it is not
		 * possible for the report to throw a Contract exception when given a null
		 * PersonPropertyReportPluginData.
		 */

		// precondition test: if the PersonPropertyReportPluginData is null
		assertThrows(NullPointerException.class, () -> new PersonPropertyReport(null));
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_Content() {

		/*
		 * This test covers only the general content of the report by exercising a
		 * variety of person property changes over time.
		 */

		List<Plugin> plugins = new ArrayList<>();
		PersonPropertyId unknownIdToExclude = new PersonPropertyId() {
			@Override
			public String toString() {
				return "unknownIdToExclude";
			}
		};
		PersonPropertyId unknownIdToInclude = new PersonPropertyId() {
			@Override
			public String toString() {
				return "unknownIdToInclude";
			}
		};

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the actor add a new person property definitions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownIdToExclude).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization2 = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownIdToInclude).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization2);

		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.4, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.7, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8), unknownIdToInclude, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(17), unknownIdToExclude, 134);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 17);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.8, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 59);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), unknownIdToInclude, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), unknownIdToExclude, 99);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(11), unknownIdToInclude, true);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod hourlyReportPeriod = ReportPeriod.DAILY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		plugins.add(testPlugin);

		int populationSize = 30;

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		peopleBuilder.addPersonRange(new PersonRange(0, populationSize - 1));

		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		plugins.add(peoplePlugin);

		// add the first three TestPersonPropertyId values and their definitions
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		Set<TestPersonPropertyId> basePropertyIds = new LinkedHashSet<>();
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
		for (TestPersonPropertyId testPersonPropertyId : basePropertyIds) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), 0, false);
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.setDefaultInclusion(true);
		builder.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
		builder.excludePersonProperty(unknownIdToExclude);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		Plugin personPropertyPlugin = PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
				.getPersonPropertyPlugin();

		plugins.add(personPropertyPlugin);

		/*
		 * Add two region - even people in region 1 and odd people in region 2
		 */
		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();

		regionBuilder.addRegion(TestRegionId.REGION_1);
		regionBuilder.addRegion(TestRegionId.REGION_2);
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = peoplePluginData.getPersonIds().get(i);
			if (i % 2 == 0) {
				regionBuilder.addPerson(personId, TestRegionId.REGION_1);
			} else {
				regionBuilder.addPerson(personId, TestRegionId.REGION_2);
			}
		}
		RegionsPluginData regionsPluginData = regionBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
		plugins.add(regionsPlugin);

		// execute the simulation and gather the report items

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();

		// build the expected report items
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		// expected report at time 0
		// expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0,
		// TestRegionId.REGION_1,
		// TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		// expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0,
		// TestRegionId.REGION_1,
		// TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		// expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0,
		// TestRegionId.REGION_2,
		// TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		// expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0,
		// TestRegionId.REGION_2,
		// TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));

		// expected report at time 1
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1, unknownIdToInclude, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2, unknownIdToInclude, false, 15));

		// expected report at time 2
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, unknownIdToInclude, false, 14));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, unknownIdToInclude, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30, 1));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, unknownIdToInclude, false, 15));

		// expected report at time 3
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12, 2));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, unknownIdToInclude, false, 13));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, unknownIdToInclude, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2,
				TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30, 1));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, unknownIdToInclude, false, 14));
		expectedOutputConsumer
				.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, unknownIdToInclude, true, 1));

		// compare the expected and actual report items
		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItemMap(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testInit_ReportLabel() {
		/*
		 * This test shows that the report produces report items with the correct report
		 * labels.
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(ReportPeriod.DAILY);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory//
				.factory(30, 1174198461656549476L, testPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData);//

		List<Plugin> plugins = factory.getPlugins();

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();
		// show that the report labels are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertFalse(outputItems.isEmpty());
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportItem.getReportLabel(), reportLabel);
		}

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testInit_ReportHeader() {
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {
			testInit_ReportHeader(reportPeriod);
		}
	}

	private void testInit_ReportHeader(ReportPeriod reportPeriod) {
		/*
		 * This test shows that the report produces report items with the correct report
		 * headers.
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = REPORT_LABEL;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(reportPeriod);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)//
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);
		List<Plugin> plugins = factory.getPlugins();

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();

		// show that the report labels are what we expect for each report item
		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();

		ReportHeader expectedReportHeader;
		switch (reportPeriod) {
		case DAILY:
			expectedReportHeader = REPORT_DAILY_HEADER;
			break;
		case END_OF_SIMULATION:
			expectedReportHeader = REPORT_END_OF_SIMULATION_HEADER;
			break;
		case HOURLY:
			expectedReportHeader = REPORT_HOURLY_HEADER;
			break;
		default:
			throw new RuntimeException("unhandled case " + reportPeriod);

		}

		assertEquals(expectedReportHeader, reportHeader);

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {
			UnitTag.INCOMPLETE })
	public void testInit_ReportPeriod() {
		/*
		 * This test shows that the report produces report items with the correct report
		 * periods.
		 */

		// post conditional testing for Hourly, Daily, and End Of Simulation
		// report periods
		testSetReportPeriod_Hourly();
		testSetReportPeriod_Daily();
		testSetReportPeriod_EndOfSim();
	}

	private void testSetReportPeriod_Hourly() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = REPORT_LABEL;
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();

		// show that the report periods are what we expect for each report item
		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(reportHeader.getHeaderStrings().get(0), "day");
		assertEquals(reportHeader.getHeaderStrings().get(1), "hour");
	}

	private void testSetReportPeriod_Daily() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.DAILY;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();

		// show that the report periods are what we expect for each report item
		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertEquals(reportHeader.getHeaderStrings().get(0), "day");
		assertFalse(reportHeader.getHeaderStrings().contains("hour"));
	}

	private void testSetReportPeriod_EndOfSim() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.END_OF_SIMULATION;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();

		// show that the report periods are what we expect for each report item
		ReportHeader reportHeader = testOutputConsumer.getOutputItem(ReportHeader.class).get();
		assertFalse(reportHeader.getHeaderStrings().contains("day"));
		assertFalse(reportHeader.getHeaderStrings().contains("hour"));
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_DefaultInclusion() {
		/*
		 * This test shows that the report produces report items with the correct
		 * selected properties as a function of the default property inclusion policy
		 */

		testSetDefaultInclusion_False();
		testSetDefaultInclusion_True();
		testSetDefaultInclusion_Unset();
	}

	private void testSetDefaultInclusion_False() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId,
								personProperty);
					}
				}
			}
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);

			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownPersonPropertyId).setPropertyDefinition(propertyDefinition)
					.build();

			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.setDefaultInclusion(false);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)//
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		List<Plugin> plugins = factory.getPlugins();

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertTrue(outputItems.isEmpty());
	}

	private void testSetDefaultInclusion_True() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId,
								personProperty);
					}
				}
			}
		}));

		// create an unknown property id
		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownPersonPropertyId).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.setDefaultInclusion(true);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();

		// show that whe find the expected person property ids
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertFalse(outputItems.isEmpty());
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertTrue(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		}
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));
	}

	private void testSetDefaultInclusion_Unset() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId,
								personProperty);
					}
				}
			}
		}));

		// create an unknown property id
		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownPersonPropertyId).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();

		// show that when the default inclusion is unset, it defaults to true
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertFalse(outputItems.isEmpty());
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertTrue(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		}
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_IncludePersonProperty() {

		/*
		 * This test shows that the report produces report items with the correct
		 * selected properties as a function of the explicitly included properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId,
								personProperty);
					}
				}
			}
		}));

		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownPersonPropertyId).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.setDefaultInclusion(false);
		builder.includePersonProperty(testPersonPropertyId);
		builder.includePersonProperty(unknownPersonPropertyId);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();

		// show that our report items include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertFalse(outputItems.isEmpty());
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		assertTrue(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));

	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = {})
	public void testInit_ExcludePersonProperty() {
		/*
		 * This test shows that the report produces report items with the correct
		 * selected properties as a function of the explicitly excluded properties
		 */

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId,
								personProperty);
					}
				}
			}
		}));

		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownPersonPropertyId).setPropertyDefinition(propertyDefinition)
					.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.excludePersonProperty(testPersonPropertyId);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = //
				PersonPropertiesTestPluginFactory//
						.factory(30, 1174198461656549476L, testPluginData)
						.setPersonPropertyReportPluginData(personPropertyReportPluginData);

		List<Plugin> plugins = factory.getPlugins();

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.build()//
				.execute();

		// show that our report items do not include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItemMap(ReportItem.class);
		assertFalse(outputItems.isEmpty());
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		assertFalse(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));
	}

	private static ReportItem getReportItem(ReportPeriod reportPeriod, Object... values) {
		ReportItem.Builder builder = ReportItem.builder();
		builder.setReportLabel(REPORT_LABEL);

		for (Object value : values) {
			builder.addValue(value);
		}
		return builder.build();
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class })
	public void testInit_State() {
		// Test with producing simulation output

		List<Plugin> plugins = new ArrayList<>();
		PersonPropertyId unknownIdToExclude = new PersonPropertyId() {
			@Override
			public String toString() {
				return "unknownIdToExclude";
			}
		};
		PersonPropertyId unknownIdToInclude = new PersonPropertyId() {
			@Override
			public String toString() {
				return "unknownIdToInclude";
			}
		};

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// have the actor add two new person property definitions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class)
					.setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder()//
					.setPersonPropertyId(unknownIdToExclude).setPropertyDefinition(propertyDefinition)//
					.build();

			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			propertyDefinition = PropertyDefinition.builder()//
					.setType(Boolean.class).setDefaultValue(false)//
					.build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization2 = //
					PersonPropertyDefinitionInitialization.builder()//
							.setPersonPropertyId(unknownIdToInclude).setPropertyDefinition(propertyDefinition)//
							.build();

			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization2);

		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.4, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.7, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8),
					TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8), unknownIdToInclude, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(17), unknownIdToExclude, 134);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 17);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.8, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 59);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10),
					TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6),
					TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), unknownIdToInclude, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), unknownIdToExclude, 99);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(11), unknownIdToInclude, true);
		}));

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod hourlyReportPeriod = ReportPeriod.DAILY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		plugins.add(testPlugin);

		int populationSize = 30;

		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		peopleBuilder.addPersonRange(new PersonRange(0, populationSize - 1));

		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		plugins.add(peoplePlugin);

		// add the first three TestPersonPropertyId values and their definitions
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		Set<TestPersonPropertyId> basePropertyIds = new LinkedHashSet<>();
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
		basePropertyIds.add(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
		for (TestPersonPropertyId testPersonPropertyId : basePropertyIds) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId,
					testPersonPropertyId.getPropertyDefinition(), 0, false);
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		builder.setReportPeriod(hourlyReportPeriod);
		builder.setDefaultInclusion(true);
		builder.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
		builder.excludePersonProperty(unknownIdToExclude);
		PersonPropertyReportPluginData personPropertyReportPluginData = builder.build();

		Plugin personPropertyPlugin = PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
				.getPersonPropertyPlugin();

		plugins.add(personPropertyPlugin);

		/*
		 * Add two region - even people in region 1 and odd people in region 2
		 */
		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();

		regionBuilder.addRegion(TestRegionId.REGION_1);
		regionBuilder.addRegion(TestRegionId.REGION_2);
		for (int i = 0; i < populationSize; i++) {
			PersonId personId = peoplePluginData.getPersonIds().get(i);
			if (i % 2 == 0) {
				regionBuilder.addPerson(personId, TestRegionId.REGION_1);
			} else {
				regionBuilder.addPerson(personId, TestRegionId.REGION_2);
			}
		}
		RegionsPluginData regionsPluginData = regionBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
		plugins.add(regionsPlugin);

		// execute the simulation and gather the report items

		TestOutputConsumer testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.setProduceSimulationStateOnHalt(true)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		Map<PersonPropertyReportPluginData, Integer> outputItems = testOutputConsumer
				.getOutputItemMap(PersonPropertyReportPluginData.class);
		assertEquals(1, outputItems.size());
		PersonPropertyReportPluginData personPropertyReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(personPropertyReportPluginData, personPropertyReportPluginData2);

		// Test without producing simulation state

		testOutputConsumer = TestSimulation.builder()//
				.addPlugins(plugins)//
				.setProduceSimulationStateOnHalt(false)//
				.setSimulationHaltTime(20)//
				.build()//
				.execute();

		outputItems = testOutputConsumer.getOutputItemMap(PersonPropertyReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("report label");

	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().setReportLabel(REPORT_LABEL).add("day").add("hour").add("region")
			.add("property").add("value").add("person_count").build();
	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().setReportLabel(REPORT_LABEL).add("day").add("region")
			.add("property").add("value").add("person_count").build();
	private static final ReportHeader REPORT_END_OF_SIMULATION_HEADER = ReportHeader.builder().setReportLabel(REPORT_LABEL).add("region")
			.add("property").add("value").add("person_count").build();
}
