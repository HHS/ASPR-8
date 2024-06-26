package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestOutputConsumer;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPlugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonRange;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support.PersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTag;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_PersonPropertyInteractionReport {

	@Test
	@UnitTestConstructor(target = PersonPropertyInteractionReport.class, args = { PersonPropertyInteractionReportPluginData.class})
	public void testConstructor() {

		// precondition: report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportPeriod(ReportPeriod.DAILY).build();
			new PersonPropertyInteractionReport(pluginData);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// precondition: report period is null
		contractException = assertThrows(ContractException.class, () -> {
			PersonPropertyInteractionReportPluginData pluginData = PersonPropertyInteractionReportPluginData.builder().setReportLabel(new SimpleReportLabel("label")).build();
			new PersonPropertyInteractionReport(pluginData);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReport.class, name = "init", args = { ReportContext.class }, tags = { UnitTag.INCOMPLETE })
	public void testInit() {
		// TBD
	}

	@Test
	@UnitTestMethod(target = PersonPropertyInteractionReport.class, name = "init", args = { ReportContext.class })
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
		// pluginDataBuilder.addPluginDependency(PersonPropertiesPluginId.PLUGIN_ID);

		// have the actor add a new person property definitions
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(1).build();

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownIdToExclude)
																																	.setPropertyDefinition(propertyDefinition).build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			propertyDefinition = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(false).build();
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization2 = PersonPropertyDefinitionInitialization	.builder().setPersonPropertyId(unknownIdToInclude)
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
		peopleBuilder.addPersonRange(new PersonRange(0, populationSize-1));
		
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
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition(),0,false);
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();

		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = //
				PersonPropertyInteractionReportPluginData	.builder()//
															.setReportLabel(reportLabel).setReportPeriod(hourlyReportPeriod)//
															.addPersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK)//
															.removePersonPropertyId(TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK)//
															.build();

		Plugin personPropertyPlugin = PersonPropertiesPlugin.builder()//
															.setPersonPropertiesPluginData(personPropertiesPluginData)//
															.setPersonPropertyInteractionReportPluginData(personPropertyInteractionReportPluginData)//
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

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugins(plugins)//
																.setProduceSimulationStateOnHalt(true)//
																.setSimulationHaltTime(50)//
																.build()//
																.execute();

		Map<PersonPropertyInteractionReportPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(PersonPropertyInteractionReportPluginData.class);
		assertEquals(1, outputItems.size());
		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData2 = outputItems.keySet().iterator().next();
		assertEquals(personPropertyInteractionReportPluginData, personPropertyInteractionReportPluginData2);

		// Test without producing simulation state

		testOutputConsumer = TestSimulation	.builder()//
											.addPlugins(plugins)//
											.setProduceSimulationStateOnHalt(false)//
											.setSimulationHaltTime(20)//
											.build()//
											.execute();

		outputItems = testOutputConsumer.getOutputItemMap(PersonPropertyInteractionReportPluginData.class);
		assertEquals(0, outputItems.size());
	}

}
