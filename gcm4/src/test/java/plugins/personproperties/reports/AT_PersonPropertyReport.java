package plugins.personproperties.reports;

import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.ReportContext;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.*;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

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
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) ->{
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

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(5, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
			// provides an empty task so that the TestSimulation.execute does not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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

		// post conditional testing for Hourly, Daily, and End Of Simulation report periods
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
			// provides an empty task so that the TestSimulation.execute does not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
			// provides an empty task so that the TestSimulation.execute does not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.DAILY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
			// provides an empty task so that the TestSimulation.execute does not throw an exception
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.END_OF_SIMULATION;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the report period
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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
	}

	private void testSetDefaultInclusion_False() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
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
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization.builder()
					.setPersonPropertyId(unknownPersonPropertyId)
					.setPropertyDefinition(propertyDefinition)
					.build();

			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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
//		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
//			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
//			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
//			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
//			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
//
//			for (PersonId personId : peopleDataManager.getPeople()) {
//				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
//					if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
//						Object personProperty = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
//						personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, personProperty);
//					}
//				}
//			}
//		}));

		PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonId personId = peopleDataManager.getPeople().get(0);
			personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, 2);
		}));

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().definePersonProperty(unknownPersonPropertyId, propertyDefinition).build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData).setPersonPropertiesPluginData(personPropertiesPluginData);
		List<Plugin> plugins = factory.getPlugins();

		// set the default inclusion to false
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(false);
			builder.includePersonProperty(unknownPersonPropertyId);
			return builder.build()::init;
		}).build();
		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);
		plugins.add(reportsPlugin);
		TestOutputConsumer testOutputConsumer = new TestOutputConsumer();
		TestSimulation.executeSimulation(plugins, testOutputConsumer);

		Map<ReportItem, Integer> outputItems = testOutputConsumer.getOutputItems(ReportItem.class);
		Set<String> outputPropertyStrings = new LinkedHashSet<>();
		for (ReportItem reportItem : outputItems.keySet()) {
			outputPropertyStrings.add(reportItem.getValue(3));
		}
		System.out.println(unknownPersonPropertyId.toString());
		System.out.println(outputPropertyStrings);
		assertTrue(outputPropertyStrings.contains(unknownPersonPropertyId.toString()));
	}

	@Test
	@UnitTestMethod(target = PersonPropertyReport.Builder.class, name = "includePersonProperty", args = { PersonPropertyId.class })
	public void testIncludePersonProperty() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();

		// create a test actor plan where we set several person property values
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) ->{
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

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		// tell the builder to include a specific person property id
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
			PersonPropertyReport.Builder builder = PersonPropertyReport.builder();
			builder.setReportLabel(reportLabel);
			builder.setReportPeriod(hourlyReportPeriod);
			builder.setDefaultInclusion(false);
			builder.includePersonProperty(testPersonPropertyId);
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
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) ->{
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

		ReportLabel reportLabel = new SimpleReportLabel(1000);
		ReportPeriod hourlyReportPeriod = ReportPeriod.HOURLY;
		TestPluginData testPluginData = pluginDataBuilder.build();
		PersonPropertiesTestPluginFactory.Factory factory = PersonPropertiesTestPluginFactory.factory(30, 1174198461656549476L, testPluginData);
		List<Plugin> plugins = factory.getPlugins();
		TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		// tell the builder to exclude a specific person property id
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().addReport(()->{
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

		// precondition: person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyReport.builder().excludePersonProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}
}
