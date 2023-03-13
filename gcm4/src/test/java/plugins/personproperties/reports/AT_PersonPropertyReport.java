package plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.ReportContext;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_PersonPropertyReport {

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "builder", args = {})
	public void testBuilder() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();

		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.class, name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {

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
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownIdToExclude)
					.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization2 = PersonPropertyDefinitionInitialization
					.builder().setPersonPropertyId(unknownIdToInclude)
					.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization2);

		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.4, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		// have the actor set a few person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0.7, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 35);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(7), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8), TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, 2.75);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(8), unknownIdToInclude, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(17), unknownIdToExclude, 134);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 17);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1.8, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 59);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(10), TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true);
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2.5, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12);
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

		for (int i = 0; i < populationSize; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
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
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
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
				regionBuilder.setPersonRegion(personId, TestRegionId.REGION_1);
			} else {
				regionBuilder.setPersonRegion(personId, TestRegionId.REGION_2);
			}
		}
		RegionsPluginData regionsPluginData = regionBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);
		plugins.add(regionsPlugin);

		
		//add the report
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(true);
			builder.excludePersonProperty(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK);
			builder.excludePersonProperty(unknownIdToExclude);

			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);

		
		//execute the simulation and gather the report items 
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		//build the expected report items
		TestOutputConsumer expectedOutputConsumer = new TestOutputConsumer();

		// expected report at time 0
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 0, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));

		// expected report at time 1
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_1, unknownIdToInclude, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 1, TestRegionId.REGION_2, unknownIdToInclude, false, 15));

		// expected report at time 2
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, unknownIdToInclude, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_1, unknownIdToInclude, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 2, TestRegionId.REGION_2, unknownIdToInclude, false, 15));

		// expected report at time 3
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, true, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 12, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, unknownIdToInclude, false, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_1, unknownIdToInclude, true, 2));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, false, 15));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 0, 13));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 40, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, 30, 1));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, unknownIdToInclude, false, 14));
		expectedOutputConsumer.accept(getReportItem(ReportPeriod.DAILY, 3, TestRegionId.REGION_2, unknownIdToInclude, true, 1));


		//compare the expected and actual report items
		Map<ReportItem, Integer> expectedReportItems = expectedOutputConsumer.getOutputItems(ReportItem.class);
		Map<ReportItem, Integer> actualReportItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertEquals(expectedReportItems, actualReportItems);
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "build", args = {})
	public void testBuild() {
		PersonPropertyReport.Builder builder = PersonPropertyReport.builder();

		builder.setReportLabel(new SimpleReportLabel(1000));
		builder.setReportPeriod(ReportPeriod.DAILY);

		PersonPropertyReport report = builder.build();

		assertNotNull(report);

		// precondition: null report label
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportPeriod(ReportPeriod.DAILY).build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: null report period
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportLabel(new SimpleReportLabel(1000)).build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(ReportPeriod.DAILY);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that the report labels are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportItem.getReportLabel(), reportLabel);
		}

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {

		// post conditional testing for Hourly, Daily, and End Of Simulation
		// report periods
		testSetReportPeriod_Hourly();
		testSetReportPeriod_Daily();
		testSetReportPeriod_EndOfSim();

		// precondition: report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	private void testSetReportPeriod_Hourly() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that the report periods are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportItem.getReportHeader().getHeaderStrings().get(0), "day");
			assertEquals(reportItem.getReportHeader().getHeaderStrings().get(1), "hour");
		}
	}

	private void testSetReportPeriod_Daily() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.DAILY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that the report periods are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertEquals(reportItem.getReportHeader().getHeaderStrings().get(0), "day");
			assertFalse(reportItem.getReportHeader().getHeaderStrings().contains("hour"));
		}
	}

	private void testSetReportPeriod_EndOfSim() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			// provides an empty task so that the TestSimulation.execute does
			// not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.END_OF_SIMULATION;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that the report periods are what we expect for each report item
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : outputItems.keySet()) {
			assertFalse(reportItem.getReportHeader().getHeaderStrings().contains("day"));
			assertFalse(reportItem.getReportHeader().getHeaderStrings().contains("hour"));
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		testSetDefaultInclusion_False();
		testSetDefaultInclusion_True();
		testSetDefaultInclusion_Unset();
	}

	private void testSetDefaultInclusion_False() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
					}
				}
			}
		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownPersonPropertyId)
																																	.setPropertyDefinition(propertyDefinition).build();

			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(false);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		assertTrue(outputItems.isEmpty());
	}

	private void testSetDefaultInclusion_True() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
					}
				}
			}
		}));

		// create an unknown property id
		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownPersonPropertyId)
																																	.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(true);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that whe find the expected person property ids
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
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
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
					}
				}
			}
		}));

		// create an unknown property id
		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownPersonPropertyId)
																																	.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that when the default inclusion is unset, it defaults to true
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
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
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "includePersonProperty", args = { PersonPropertyId.class })
	public void testIncludePersonProperty() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
					}
				}
			}
		}));

		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownPersonPropertyId)
																																	.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		// tell the builder to include a specific person property id
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(false);
			builder.includePersonProperty(testPersonPropertyId);
			builder.includePersonProperty(unknownPersonPropertyId);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that our report items include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		assertTrue(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().includePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "excludePersonProperty", args = { PersonPropertyId.class })
	public void testExcludePersonProperty() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			for (PersonId personId : peopleDataManager.getPeople()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
					}
				}
			}
		}));

		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownPersonPropertyId)
																																	.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		// tell the builder to exclude a specific person property id
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(() -> {
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.excludePersonProperty(testPersonPropertyId);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		// show that our report items do not include the chosen property id
		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		assertFalse(outputPropertyStrings.contains(testPersonPropertyId.toString()));
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().excludePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
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

	private static final ReportLabel REPORT_LABEL = new SimpleReportLabel("report label");

	private static final ReportHeader REPORT_HOURLY_HEADER = ReportHeader.builder().add("day").add("hour").add("region").add("property").add("value").add("person_count").build();
	private static final ReportHeader REPORT_DAILY_HEADER = ReportHeader.builder().add("day").add("region").add("property").add("value").add("person_count").build();
}
