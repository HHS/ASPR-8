package plugins.personproperties.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.EventFilter;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.SimulationState;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPlugin;
import nucleus.testsupport.runcontinuityplugin.RunContinuityPluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestOutputConsumer;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.people.support.PersonRange;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.events.PersonPropertyDefinitionEvent;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyDefinitionInitialization;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyValueInitialization;
import plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory.Factory;
import plugins.personproperties.testsupport.TestAuxiliaryPersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableBoolean;
import util.wrappers.MutableInteger;

public final class AT_PersonPropertyDataManager {

	/**
	 * Demonstrates that the data manager produces plugin data that reflects its
	 * final state
	 */
	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateFinalization() {
		/*
		 * Plan for test:
		 * 
		 * we will start with three people and two properties, one with a
		 * default and one without
		 * 
		 * 1) generate the necessary plugins
		 * 
		 * 2) generate the initial people properties plugin data state
		 * 
		 * 3)have an actor perform various operations that change the person
		 * property management state
		 * 
		 * 4)get the people properties plugin data that results from the
		 * simulation
		 * 
		 * 5) build the expected plugin data
		 * 
		 * 6)compare the two versions for equality
		 */

		// add the three people
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7333676542014748090L);
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().addPersonRange(new PersonRange(1, 3)).build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		// create three regions
		RegionsPluginData regionsPluginData = RegionsPluginData	.builder()//
																.addRegion(TestRegionId.REGION_1)//
																.addRegion(TestRegionId.REGION_2)//
																.addRegion(TestRegionId.REGION_3)//
																.addPerson(new PersonId(1), TestRegionId.REGION_1)//
																.addPerson(new PersonId(2), TestRegionId.REGION_2)//
																.addPerson(new PersonId(3), TestRegionId.REGION_2)//
																.build();
		Plugin regionsPlugin = RegionsPlugin.builder()//
											.setRegionsPluginData(regionsPluginData)//
											.getRegionsPlugin();

		// create the stochastics plugin
		WellState wellState = WellState	.builder()//
										.setSeed(randomGenerator.nextLong())//
										.build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setMainRNGState(wellState)//
																			.build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		/*
		 * Create the person properties plugin. There will be three properties.
		 * The first two exist in the initial plugin data and third will be
		 * added later. Time tracking will be turned on for prop1 and prop3
		 */
		PersonPropertyId prop1 = new LocalPersonPropertyId(1) {
		};
		PersonPropertyId prop2 = new LocalPersonPropertyId(2) {
		};
		PersonPropertyId prop3 = new LocalPersonPropertyId(3) {
		};

		PropertyDefinition def1 = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(17).build();
		PropertyDefinition def2 = PropertyDefinition.builder().setType(Double.class).setDefaultValue(12.3).build();
		PropertyDefinition def3 = PropertyDefinition.builder().setType(Boolean.class).setDefaultValue(true).build();

		PersonPropertiesPluginData.Builder propBuilder = PersonPropertiesPluginData.builder();
		propBuilder.definePersonProperty(prop1, def1, 1.2, true);
		propBuilder.definePersonProperty(prop2, def2, 0, false);
		propBuilder.setPersonPropertyValue(new PersonId(1), prop1, 18);
		propBuilder.setPersonPropertyTime(new PersonId(1), prop1, 1.5);
		propBuilder.setPersonPropertyTime(new PersonId(2), prop1, 1.3);
		propBuilder.setPersonPropertyValue(new PersonId(3), prop1, 99);
		propBuilder.setPersonPropertyTime(new PersonId(3), prop1, 2.3);
		propBuilder.setPersonPropertyValue(new PersonId(1), prop2, 34.4);
		propBuilder.setPersonPropertyValue(new PersonId(2), prop2, 88.7);

		PersonPropertiesPluginData personPropertiesPluginData = propBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.builder()//
															.setPersonPropertiesPluginData(personPropertiesPluginData)//
															.getPersonPropertyPlugin();

		TestPluginData.Builder testPluginBuilder = TestPluginData.builder();
		testPluginBuilder.addTestActorPlan("actor", new TestActorPlan(2.6, (c) -> {
			// remove a person
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			peopleDataManager.removePerson(new PersonId(1));
			// update some values

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(2), prop1, 66);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(3), prop2, 100.5);
		}));

		testPluginBuilder.addTestActorPlan("actor", new TestActorPlan(3.4, (c) -> {
			// introduce a new property
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = //
					PersonPropertyDefinitionInitialization	.builder()//
															.setPersonPropertyId(prop3)//
															.setPropertyDefinition(def3)//
															.setTrackTimes(true)//
															.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
			// add a person
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData personConstructionData = //
					PersonConstructionData	.builder()//
											.add(TestRegionId.REGION_3)//
											.add(new PersonPropertyValueInitialization(prop2, 456.6))//
											.build();
			peopleDataManager.addPerson(personConstructionData);

			// update some values
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4), prop3, false);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4), prop1, 13);

		}));

		testPluginBuilder.addTestActorPlan("actor", new TestActorPlan(6.7, (c) -> {
			// update some values
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			personPropertiesDataManager.setPersonPropertyValue(new PersonId(2), prop1, 17);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(2), prop3, false);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(3), prop2, 123.31);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(3), prop3, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4), prop1, 88);

		}));

		TestPluginData testPluginData = testPluginBuilder.build();

		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		/*
		 * Run the simulation starting on day 2.5 and get the actual person
		 * properties plugin data that results from the data manager mutations
		 */
		SimulationState simulationState = SimulationState.builder().setStartTime(2.5).build();

		TestOutputConsumer testOutputConsumer = TestSimulation	.builder()//
																.addPlugin(peoplePlugin)//
																.addPlugin(regionsPlugin)//
																.addPlugin(stochasticsPlugin)//
																.addPlugin(personPropertyPlugin)//
																.addPlugin(testPlugin)//
																.setProduceSimulationStateOnHalt(true)//
																.setSimulationState(simulationState)//
																.setSimulationHaltTime(10.0)//
																.build()//
																.execute();

		Map<PersonPropertiesPluginData, Integer> outputItems = testOutputConsumer.getOutputItemMap(PersonPropertiesPluginData.class);
		assertTrue(outputItems.size() == 1);
		PersonPropertiesPluginData actualPersonPropertiesPluginData = outputItems.keySet().iterator().next();

		/*
		 * Generate the expected person properties plugin data
		 */
		propBuilder = PersonPropertiesPluginData.builder();
		propBuilder.definePersonProperty(prop1, def1, 1.2, true);
		propBuilder.definePersonProperty(prop2, def2, 0, false);
		propBuilder.definePersonProperty(prop3, def3, 3.4, true);
		propBuilder.setPersonPropertyTime(new PersonId(2), prop1, 6.7);
		propBuilder.setPersonPropertyValue(new PersonId(2), prop2, 88.7);
		propBuilder.setPersonPropertyValue(new PersonId(2), prop3, false);
		propBuilder.setPersonPropertyTime(new PersonId(2), prop3, 6.7);
		propBuilder.setPersonPropertyValue(new PersonId(3), prop1, 99);
		propBuilder.setPersonPropertyTime(new PersonId(3), prop1, 2.3);
		propBuilder.setPersonPropertyValue(new PersonId(3), prop2, 123.31);
		propBuilder.setPersonPropertyTime(new PersonId(3), prop3, 6.7);
		propBuilder.setPersonPropertyValue(new PersonId(4), prop1, 88);
		propBuilder.setPersonPropertyTime(new PersonId(4), prop1, 6.7);
		propBuilder.setPersonPropertyValue(new PersonId(4), prop2, 456.6);
		propBuilder.setPersonPropertyValue(new PersonId(4), prop3, false);
		// the following property time is extraneous, but should not effect
		// equality
		propBuilder.setPersonPropertyTime(new PersonId(4), prop3, 3.4);
		PersonPropertiesPluginData expectedPersonPropertiesPluginData = propBuilder.build();

		// compare the expected and actual plugin datas
		assertEquals(expectedPersonPropertiesPluginData, actualPersonPropertiesPluginData);

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPeopleWithPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPeopleWithPropertyValue() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 7917315534360369845L, (c) -> {

			// establish data views
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = peopleDataManager.getPeople();
			Map<Integer, Set<PersonId>> expectedValuesToPeople = new LinkedHashMap<>();
			for (int i = 0; i < 3; i++) {
				expectedValuesToPeople.put(i, new LinkedHashSet<>());
			}

			for (PersonId personId : people) {
				int value = randomGenerator.nextInt(3);
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				expectedValuesToPeople.get(value).add(personId);
			}

			// show that the proper people are returned for each value
			for (Integer value : expectedValuesToPeople.keySet()) {
				List<PersonId> actualPeople = personPropertiesDataManager.getPeopleWithPropertyValue(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				Set<PersonId> expectedPeople = expectedValuesToPeople.get(value);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPersonCountForPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPersonCountForPropertyValue() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 686456599634987511L, (c) -> {

			// establish data views
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = peopleDataManager.getPeople();
			Map<Integer, MutableInteger> expectedValuesToPeople = new LinkedHashMap<>();
			for (int i = 0; i < 3; i++) {
				expectedValuesToPeople.put(i, new MutableInteger());
			}

			for (PersonId personId : people) {
				int value = randomGenerator.nextInt(3);
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				expectedValuesToPeople.get(value).increment();
			}

			// show that the proper counts are returned for each value
			for (Integer value : expectedValuesToPeople.keySet()) {
				int actualCount = personPropertiesDataManager.getPersonCountForPropertyValue(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				MutableInteger mutableInteger = expectedValuesToPeople.get(value);
				assertEquals(mutableInteger.getValue(), actualCount);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
	public void testGetPersonPropertyDefinition() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(0, 138806179316502662L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			// show that the person property definitions match expectations
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(testPersonPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// precondition tests

			// if the person property id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyDefinition(null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		Factory factory = PersonPropertiesTestPluginFactory.factory(0, 8485097765777963229L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			EnumSet<TestPersonPropertyId> expectedPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
			Set<PersonPropertyId> actualPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
			assertEquals(expectedPropertyIds, actualPropertyIds);
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPersonPropertyTime", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that all person property times are 0 for the time-tracked
		// properties
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				double personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
			}
		}));

		// Set property 5 for all people at time 1
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK, randomGenerator.nextInt());
			}
		}));

		// Set property 6 for all people at time 2
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).getRandomGenerator();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK, randomGenerator.nextDouble());
			}
		}));

		// show that the person property times agree with the times above
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				double personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK);
				assertEquals(1.0, personPropertyTime);
				personPropertyTime = personPropertiesDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK);
				assertEquals(2.0, personPropertyTime);
			}
		}));

		// precondition tests
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(4, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			PersonId personId = new PersonId(0);
			PersonId unknownPersonId = new PersonId(100000);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK;
			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();
			PersonPropertyId untrackedPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(null, personPropertyId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(unknownPersonId, personPropertyId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person property id is null
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(personId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(personId, unknownPersonPropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

			// if the person property does not have time tracking turned on in
			// the associated property definition
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(personId, untrackedPersonPropertyId));
			assertEquals(PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 6980289425630085602L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyValue() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 816143115345188642L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a container to hold expectations
			Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();

			// assign random values for property 2 for all the people
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				int value = randomGenerator.nextInt();
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				expectedValues.put(personId, value);
			}

			// show that the values retrieved match expectations
			for (PersonId personId : people) {
				Integer expectedValue = expectedValues.get(personId);
				Integer actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
				assertEquals(expectedValue, actualValue);
			}

			// precondition tests
			PersonId personId = new PersonId(0);
			PersonId unknownPersonId = new PersonId(100000);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(null, personPropertyId));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(unknownPersonId, personPropertyId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person property id is null
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(personId, null));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(personId, unknownPersonPropertyId));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestConstructor(target = PersonPropertiesDataManager.class, args = { PersonPropertiesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertiesDataManager(null));
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		Factory factory = PersonPropertiesTestPluginFactory.factory(20, 7153865371557964932L, (c) -> {
			// show that a negative growth causes an exception
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "personPropertyIdExists", args = { PersonPropertyId.class })
	public void testPersonPropertyIdExists() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(0, 4797443283568888200L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				assertTrue(personPropertiesDataManager.personPropertyIdExists(testPersonPropertyId));
			}
			assertFalse(personPropertiesDataManager.personPropertyIdExists(TestPersonPropertyId.getUnknownPersonPropertyId()));
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers to hold the expected and actual observations
		// for later comparison
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add an agent that will observe changes to all person properties

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent();
			c.subscribe(eventFilter, (c2, e) -> {
				actualObservations.add(new MultiKey(e.personId(), e.personPropertyId(), e.previousPropertyValue(), e.currentPropertyValue()));
			});

		}));

		/*
		 * Add an agent that will alter person property values and record the
		 * corresponding expected observations.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select all the property ids that are mutable
			Set<TestPersonPropertyId> mutableProperties = new LinkedHashSet<>();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean mutable = testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable();
				if (mutable) {
					mutableProperties.add(testPersonPropertyId);
				}
			}

			// get the people
			List<PersonId> people = peopleDataManager.getPeople();

			// set all their mutable property values, recording the expected
			// observations
			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : mutableProperties) {

					// determine the new and current values
					Object newValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					Object currentValue = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);

					// record the expected observation
					expectedObservations.add(new MultiKey(personId, testPersonPropertyId, currentValue, newValue));

					// update the person property
					personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, newValue);

					// show that the value changed
					Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(newValue, actualValue);
				}
			}
		}));

		// have the agent perform precondition checks
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {
			PersonId personId = new PersonId(0);
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PersonPropertyId immutablePersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_7_BOOLEAN_IMMUTABLE_NO_TRACK;
			Object value = true;

			PersonId unknownPersonId = new PersonId(100000);
			PersonPropertyId unknownPersonPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();
			Object incompatibleValue = 12;

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(null, personPropertyId, value));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(unknownPersonId, personPropertyId, value));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person property id is null
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, null, value));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, value));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

			// if the property value is null
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, null));
			assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

			// if the property value is not compatible with the corresponding
			// property definition
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, incompatibleValue));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

			// if the corresponding property definition marks the property as
			// immutable
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, immutablePersonPropertyId, value));
			assertEquals(PropertyError.IMMUTABLE_VALUE, contractException.getErrorType());

		}));

		// have the observer show that the expected observations were actually
		// observed

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(3, (c) -> {
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 2321272063791878719L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Demonstrates that the data manager's initial state reflects its plugin
	 * data
	 */
	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateInitialization() {

		int totalPeople = 10;

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < totalPeople; i++) {
			people.add(new PersonId(i));
		}
		long seed = 2693836950854697940L;
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesTestPluginFactory.getStandardPersonPropertiesPluginData(people, seed);
		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Add an agent that will show that the person property data view is
		 * properly initialized from the person property initial data
		 */

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c2) -> {
			// get the person property data view
			PersonPropertiesDataManager personPropertiesDataManager = c2.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);

			// show that the property ids are correct
			assertEquals(personPropertiesPluginData.getPersonPropertyIds(), personPropertiesDataManager.getPersonPropertyIds());

			// show that the property definitions are correct
			for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
				PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// show that the person property values are set to the default
			// values for those properties that have default values
			List<PersonId> personIds = peopleDataManager.getPeople();
			assertTrue(personIds.size() > 0);

			for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
				PropertyDefinition propertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
				Optional<Object> optional = propertyDefinition.getDefaultValue();
				Object defaultValue = null;
				if (optional.isPresent()) {
					defaultValue = optional.get();
				}
				List<Object> propertyValues = personPropertiesPluginData.getPropertyValues(personPropertyId);

				for (PersonId personId : people) {

					Object expectedValue = null;
					if (personId.getValue() < propertyValues.size()) {
						expectedValue = propertyValues.get(personId.getValue());
					}
					if (expectedValue == null) {
						expectedValue = defaultValue;
					}

					Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();

		Factory factory = PersonPropertiesTestPluginFactory	.factory(totalPeople, seed, testPluginData)//
															.setPersonPropertiesPluginData(personPropertiesPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonAdditionEvent() {

		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 4771130331997762252L, (c) -> {
			// establish data views
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			// get the random generator for use later
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// add a person with some person property auxiliary data
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();

			// create a container to hold expectations
			Map<PersonPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();

			// set the expectation to the default values of all the properties,
			// for those that have defaults
			Set<PersonPropertyId> personPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
			for (PersonPropertyId personPropertyId : personPropertyIds) {

				PropertyDefinition personPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				if (personPropertyDefinition.getDefaultValue().isPresent()) {
					Object value = personPropertyDefinition.getDefaultValue().get();
					expectedPropertyValues.put(personPropertyId, value);
				}
			}

			// set two properties to random values and record them in the
			// expected data
			int iValue = randomGenerator.nextInt();
			personBuilder.add(new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue);

			double dValue = randomGenerator.nextDouble();
			personBuilder.add(new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue);

			// ensure that non-defaulted properties get a value assignment
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.getPropertiesWithoutDefaultValues()) {
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				personBuilder.add(new PersonPropertyValueInitialization(testPersonPropertyId, value));
				expectedPropertyValues.put(testPersonPropertyId, value);

			}

			personBuilder.add(TestRegionId.REGION_1);
			PersonConstructionData personConstructionData = personBuilder.build();

			// add the person and get its person id
			PersonId personId = peopleDataManager.addPerson(personConstructionData);

			// show that the person exists
			assertTrue(peopleDataManager.personExists(personId));

			// show that the person has the correct property values
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				Object expectedValue = expectedPropertyValues.get(personPropertyId);
				Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				assertEquals(expectedValue, actualValue);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a person property value that is
		 * not compatible with the corresponding property definition
		 */
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(100, 5194635938533128930L, (c) -> {
				// add a person with some person property auxiliary data
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45));
				PersonConstructionData constructionData = personBuilder.build();
				peopleDataManager.addPerson(constructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a null person property value
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(100, 4349734439660163798L, (c) -> {
				// add a person with some person property auxiliary data
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyValueInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, null));
				PersonConstructionData constructionData = personBuilder.build();
				peopleDataManager.addPerson(constructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has an unknown person property id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(100, 2152152824636786936L, (c) -> {
				// add a person with some person property auxiliary data
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyValueInitialization(TestPersonPropertyId.getUnknownPersonPropertyId(), false));
				PersonConstructionData constructionData = personBuilder.build();
				peopleDataManager.addPerson(constructionData);

			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a null person property id
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(100, 8379070211267955743L, (c) -> {
				// add a person with some person property auxiliary data
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

				// if the event contains a PersonPropertyInitialization that has
				// a
				// null person property id
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyValueInitialization(null, false));
				PersonConstructionData constructionData = personBuilder.build();
				peopleDataManager.addPerson(constructionData);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testPersonRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Have the actor remove a person and show that their properties remain
		 * during the current span of this agent's activation
		 */

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonId personId = new PersonId(0);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			assertTrue(peopleDataManager.personExists(personId));

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			// Set the property value to a non-default value.
			Integer expectedPropertyValue = 999;
			personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, expectedPropertyValue);

			// remove the person
			peopleDataManager.removePerson(personId);

			// show that the property value is still present
			Object actualPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			assertEquals(expectedPropertyValue, actualPropertyValue);

		}));

		// Have the actor now show that these person properties are no longer
		// available
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PersonId personId = new PersonId(0);

			// show that the person does not exist
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			assertFalse(peopleDataManager.personExists(personId));

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 2020442537537236753L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "definePersonProperty", args = { PersonPropertyDefinitionInitialization.class })
	public void testDefinePersonProperty() {

		/*
		 * Show that the PropertyDefinitionInitialization is handled correctly
		 * when default values EXIST on the property definition
		 */
		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 3100440347097616280L, (c) -> {
			double planTime = 1;
			MutableBoolean trackTimes = new MutableBoolean();
			for (TestAuxiliaryPersonPropertyId auxPropertyId : TestAuxiliaryPersonPropertyId.values()) {

				c.addPlan((c2) -> {
					PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);
					PersonPropertiesDataManager personPropertiesDataManager = c2.getDataManager(PersonPropertiesDataManager.class);
					PropertyDefinition expectedPropertyDefinition = auxPropertyId.getPropertyDefinition();
					PersonPropertyDefinitionInitialization propertyDefinitionInitialization = //
							PersonPropertyDefinitionInitialization	.builder()//
																	.setPersonPropertyId(auxPropertyId)//
																	.setPropertyDefinition(expectedPropertyDefinition)//
																	.setTrackTimes(trackTimes.getValue()).build();

					personPropertiesDataManager.definePersonProperty(propertyDefinitionInitialization);

					// show that the definition was added
					PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(auxPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

					// show that the property has the correct initial value
					// show that the property has the correct initial time
					Object expectedValue = expectedPropertyDefinition.getDefaultValue().get();
					double expectedTime = c2.getTime();
					for (PersonId personId : peopleDataManager.getPeople()) {

						Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, auxPropertyId);
						assertEquals(expectedValue, actualValue);

						if (trackTimes.getValue()) {
							double actualTime = personPropertiesDataManager.getPersonPropertyTime(personId, auxPropertyId);
							assertEquals(expectedTime, actualTime);
						}
					}
					trackTimes.setValue(!trackTimes.getValue());

				}, planTime++);
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		/*
		 * Show that the PropertyDefinitionInitialization is handled correctly
		 * when default values DO NOT EXIST on the property definition
		 * 
		 */

		factory = PersonPropertiesTestPluginFactory.factory(10, 3969826324474876300L, (c) -> {
			double planTime = 1;

			MutableBoolean trackTimes = new MutableBoolean();
			for (TestAuxiliaryPersonPropertyId auxPropertyId : TestAuxiliaryPersonPropertyId.values()) {

				c.addPlan((c2) -> {
					StochasticsDataManager stochasticsDataManager = c2.getDataManager(StochasticsDataManager.class);
					RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
					PeopleDataManager peopleDataManager = c2.getDataManager(PeopleDataManager.class);
					PersonPropertiesDataManager personPropertiesDataManager = c2.getDataManager(PersonPropertiesDataManager.class);
					PropertyDefinition expectedPropertyDefinition = auxPropertyId.getPropertyDefinition();
					/*
					 * All of the TestAuxiliaryPersonPropertyId associated
					 * property definitions have default values. We will copy
					 * the property definition, but leave the default out.
					 */
					expectedPropertyDefinition = PropertyDefinition	.builder()//
																	.setDefaultValue(expectedPropertyDefinition.getDefaultValue().get())//
																	.setPropertyValueMutability(expectedPropertyDefinition.propertyValuesAreMutable())//
																	.setType(expectedPropertyDefinition.getType())//
																	.build();

					Map<PersonId, Object> expectedPropertyValues = new LinkedHashMap<>();

					PersonPropertyDefinitionInitialization.Builder defBuilder = //
							PersonPropertyDefinitionInitialization	.builder()//
																	.setPersonPropertyId(auxPropertyId)//
																	.setPropertyDefinition(expectedPropertyDefinition).setTrackTimes(trackTimes.getValue());

					//
					expectedPropertyDefinition.getType();
					for (PersonId personId : peopleDataManager.getPeople()) {
						Object randomPropertyValue = auxPropertyId.getRandomPropertyValue(randomGenerator);
						defBuilder.addPropertyValue(personId, randomPropertyValue);
						expectedPropertyValues.put(personId, randomPropertyValue);
					}

					PersonPropertyDefinitionInitialization propertyDefinitionInitialization = defBuilder.build();

					personPropertiesDataManager.definePersonProperty(propertyDefinitionInitialization);

					// show that the definition was added
					PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(auxPropertyId);
					assertEquals(expectedPropertyDefinition, actualPropertyDefinition);

					// show that the property has the correct initial value
					// show that the property has the correct initial time

					double expectedTime = c2.getTime();
					for (PersonId personId : peopleDataManager.getPeople()) {
						Object expectedValue = expectedPropertyValues.get(personId);

						Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, auxPropertyId);
						assertEquals(expectedValue, actualValue);

						if (trackTimes.getValue()) {
							double actualTime = personPropertiesDataManager.getPersonPropertyTime(personId, auxPropertyId);
							assertEquals(expectedTime, actualTime);
						}
					}
					trackTimes.setValue(!trackTimes.getValue());
				}, planTime++);

			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(0, 4627357002700907595L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.definePersonProperty(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_DEFINITION_INITIALIZATION, contractException.getErrorType());

		// if the person property already exists
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(0, 8802528032031272978L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = TestAuxiliaryPersonPropertyId.PERSON_AUX_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK.getPropertyDefinition();
				PersonPropertyDefinitionInitialization propertyDefinitionInitialization = //
						PersonPropertyDefinitionInitialization	.builder()//
																.setPersonPropertyId(personPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();
				personPropertiesDataManager.definePersonProperty(propertyDefinitionInitialization);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.DUPLICATE_PROPERTY_DEFINITION, contractException.getErrorType());

		/*
		 * if the property definition has no default value and there is no
		 * included value assignment for some extant person
		 */
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(0, 1498052576475289605L, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PersonPropertyId personPropertyId = TestAuxiliaryPersonPropertyId.PERSON_AUX_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
				PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

				// get the minimum set of properties that we will need to
				// initialize
				// for each new person
				List<TestPersonPropertyId> requiredPropertyIds = new ArrayList<>();
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					if (testPersonPropertyId.getPropertyDefinition().getDefaultValue().isEmpty()) {
						requiredPropertyIds.add(testPersonPropertyId);
					}
				}

				PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();

				// add a first person
				personBuilder.add(TestRegionId.REGION_1);
				for (TestPersonPropertyId testPersonPropertyId : requiredPropertyIds) {
					Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(testPersonPropertyId, value);
					personBuilder.add(personPropertyValueInitialization);
				}
				PersonConstructionData personConstructionData = personBuilder.build();
				PersonId personId1 = peopleDataManager.addPerson(personConstructionData);

				// add a second person
				personBuilder.add(TestRegionId.REGION_2);
				for (TestPersonPropertyId testPersonPropertyId : requiredPropertyIds) {
					Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					PersonPropertyValueInitialization personPropertyValueInitialization = new PersonPropertyValueInitialization(testPersonPropertyId, value);
					personBuilder.add(personPropertyValueInitialization);
				}
				personConstructionData = personBuilder.build();
				peopleDataManager.addPerson(personConstructionData);

				/*
				 * define a new property without a default value and only set
				 * the value for one of the two people in the population.
				 * 
				 * only assign a value to one person
				 */
				PersonPropertyDefinitionInitialization propertyDefinitionInitialization = //
						PersonPropertyDefinitionInitialization	.builder()//
																.setPersonPropertyId(personPropertyId)//
																.setPropertyDefinition(propertyDefinition)//

																.addPropertyValue(personId1, 12)//
																.build();

				personPropertiesDataManager.definePersonProperty(propertyDefinitionInitialization);

			});

			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();

		});
		assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyUpdateEvent", args = { PersonPropertyId.class })
	public void testGetEventFilterForPersonPropertyUpdateEvent_property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		/*
		 * have an observer subscribe to every person property id
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
				EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(propertyId);
				assertNotNull(eventFilter);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.currentPropertyValue());
					actualObservations.add(multiKey);
				});
			}
		}));

		/*
		 * have an actor change every person's properties for all those
		 * properties that can be changed over three distinct times
		 */
		for (int i = 1; i < 2; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						for (PersonId personId : peopleDataManager.getPeople()) {
							Object randomPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
							personPropertiesDataManager.setPersonPropertyValue(personId, propertyId, randomPropertyValue);
							MultiKey multiKey = new MultiKey(c.getTime(), personId, propertyId, randomPropertyValue);
							expectedObservations.add(multiKey);
						}
					}
				}
			}));
		}

		/*
		 * have the observer show that the expected and actual observations are
		 * equal
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 5585766374187295381L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(0, 6844554554783464142L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(0, 334179992057034848L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(TestPersonPropertyId.getUnknownPersonPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyUpdateEvent", args = { PersonId.class, PersonPropertyId.class })
	public void testGetEventFilterForPersonPropertyUpdateEvent_person_property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<Pair<PersonId, TestPersonPropertyId>> selectedPairs = new LinkedHashSet<>();

		/*
		 * have an actor determine which people and property pairs will be
		 * observed
		 */
		pluginBuilder.addTestActorPlan("selector", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				if (randomGenerator.nextDouble() < 0.6) {
					continue;
				}
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (!propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						continue;
					}
					selectedPairs.add(new Pair<>(personId, propertyId));
				}
			}
		}));

		/*
		 * have an observer subscribe to the selected (person, property) pairs
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (Pair<PersonId, TestPersonPropertyId> pair : selectedPairs) {
				PersonId personId = pair.getFirst();
				TestPersonPropertyId propertyId = pair.getSecond();

				EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personId, propertyId);
				assertNotNull(eventFilter);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.currentPropertyValue());
					actualObservations.add(multiKey);
				});
			}
		}));

		/*
		 * Have an actor change every person's properties for all those
		 * properties that can be changed over three distinct times.
		 */
		for (int i = 1; i < 2; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						for (PersonId personId : peopleDataManager.getPeople()) {
							Object randomPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
							personPropertiesDataManager.setPersonPropertyValue(personId, propertyId, randomPropertyValue);
							Pair<PersonId, TestPersonPropertyId> pair = new Pair<>(personId, propertyId);
							if (selectedPairs.contains(pair)) {
								MultiKey multiKey = new MultiKey(c.getTime(), personId, propertyId, randomPropertyValue);
								expectedObservations.add(multiKey);
							}
						}
					}
				}
			}));
		}

		/*
		 * have the observer show that the expected and actual observations are
		 * equal
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(50, 752337695044384521L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 7436809263151926252L, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				List<PersonId> people = peopleDataManager.getPeople();
				assertTrue(people.size() > 0);
				PersonId personId = people.get(0);
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 5042142105400574982L, (c) -> {
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				List<PersonId> people = peopleDataManager.getPeople();
				assertTrue(people.size() > 0);
				PersonId personId = people.get(0);
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personId, TestPersonPropertyId.getUnknownPersonPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 2414428612890791850L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PersonId nullPersonId = null;
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(nullPersonId, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

		// precondition test: if the person id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 6438595550119080771L, (c) -> {
				PersonId personId = new PersonId(1000000);
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(personId, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

	}

	private static class LocalPersonPropertyId implements PersonPropertyId {
		private final int id;

		public LocalPersonPropertyId(int id) {
			this.id = id;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + id;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof LocalPersonPropertyId)) {
				return false;
			}
			LocalPersonPropertyId other = (LocalPersonPropertyId) obj;
			if (id != other.id) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("LocalPersonPropertyId [id=");
			builder.append(id);
			builder.append("]");
			return builder.toString();
		}

	}

	private void testPropertyUpdateEvent_previous(TestPersonPropertyId testPersonPropertyId, List<Object> chosenValues, List<Object> sourceValues, long seed) {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		int planTime = 0;

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(planTime++, (c) -> {
			// set a bunch of random values
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				int index = randomGenerator.nextInt(sourceValues.size());
				Object value = sourceValues.get(index);
				personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, value);
			}
		}));

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(planTime++, (c) -> {
			// subscribe to every chosen value
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (int i = 0; i < chosenValues.size(); i++) {
				EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(testPersonPropertyId, chosenValues.get(i), false);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.getPreviousPropertyValue());
					actualObservations.add(multiKey);
				});
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(planTime++, (c) -> {
			// set a bunch of random values
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				int index = randomGenerator.nextInt(sourceValues.size());
				Object value = sourceValues.get(index);
				Object previousValue = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);
				personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, value);
				if (chosenValues.contains(previousValue)) {
					MultiKey multiKey = new MultiKey(c.getTime(), personId, testPersonPropertyId, previousValue);
					expectedObservations.add(multiKey);
				}
			}
		}));

		// show that we only get the subscribed events
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(planTime++, (c) -> {
			assertTrue(expectedObservations.size() >= sourceValues.size() / 4);
			assertEquals(expectedObservations, actualObservations);
		}));

		// run the sim with 50 people
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(50, seed, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	private void testPropertyUpdateEvent_current(TestPersonPropertyId testPersonPropertyId, List<Object> chosenValues, List<Object> sourceValues, long seed) {

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		int planTime = 0;

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(planTime++, (c) -> {
			// subscribe to every chosen value
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (int i = 0; i < chosenValues.size(); i++) {
				EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(testPersonPropertyId, chosenValues.get(i), true);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.getCurrentPropertyValue());
					actualObservations.add(multiKey);
				});
			}
		}));

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(planTime++, (c) -> {
			// set a bunch of random values
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				int index = randomGenerator.nextInt(sourceValues.size());
				Object value = sourceValues.get(index);
				personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, value);
				if (chosenValues.contains(value)) {
					MultiKey multiKey = new MultiKey(c.getTime(), personId, testPersonPropertyId, value);
					expectedObservations.add(multiKey);
				}
			}
		}));

		// show that we only get the subscribed events
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(planTime++, (c) -> {
			assertTrue(expectedObservations.size() >= sourceValues.size() / 4);
			assertEquals(expectedObservations, actualObservations);
		}));

		// run the sim with 50 people
		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(50, seed, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyUpdateEvent", args = { PersonPropertyId.class, Object.class, boolean.class })
	public void testGetEventFilterForPersonPropertyUpdateEvent_propertyId_object() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8796864982253772625L);

		// get testPropertyIds to use
		List<TestPersonPropertyId> testPersonPropertyIds = new ArrayList<>();

		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			if (testPersonPropertyId.getPropertyDefinition().propertyValuesAreMutable()) {
				testPersonPropertyIds.add(testPersonPropertyId);
			}
		}

		// set and subscribe to test actor plans for each testPropertyId
		for (TestPersonPropertyId testPersonPropertyId : testPersonPropertyIds) {

			// generate 50 random values
			List<Object> sourceValues = new ArrayList<>();
			for (int i = 0; i < 50; i++) {
				Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
				if (!sourceValues.contains(value)) {
					sourceValues.add(value);
				}
			}

			// pick out unique values to subscribe to
			List<Object> chosenValues = new ArrayList<>();
			for (int i = 0; i < sourceValues.size(); i++) {
				Object value = sourceValues.get(i);
				if (i % 2 == 0) {
					chosenValues.add(value);
				}
			}

			testPropertyUpdateEvent_current(testPersonPropertyId, chosenValues, sourceValues, randomGenerator.nextLong());
			testPropertyUpdateEvent_previous(testPersonPropertyId, chosenValues, sourceValues, randomGenerator.nextLong());
		}

		// precondition tests

		// precondition test: if the person property id is null

		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory = PersonPropertiesTestPluginFactory.factory(50, 7212207259440375049L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(null, 1, true);
			});
			TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory = PersonPropertiesTestPluginFactory.factory(50, 7580223995144844140L, (c) -> {
				PersonPropertyId unknownPropertyId = TestPersonPropertyId.getUnknownPersonPropertyId();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(unknownPropertyId, 1, true);
			});
			TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the property value is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory = PersonPropertiesTestPluginFactory.factory(50, 451632169807459388L, (c) -> {
				TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK;
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(testPersonPropertyId, null, true);
			});
			TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyDefinitionEvent", args = {})
	public void testGetEventFilterForPersonPropertyDefinitionEvent() {
		//

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		/*
		 * have an observer subscribe to person property definition events
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			EventFilter<PersonPropertyDefinitionEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyDefinitionEvent();
			assertNotNull(eventFilter);
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.personPropertyId());
				actualObservations.add(multiKey);
			});

		}));

		/*
		 * Have an actor add several new person property definitions at various
		 * times.
		 */

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(0)//
																	.build();
		IntStream.range(1, 4).forEach((i) -> {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PersonPropertyId personPropertyId = new LocalPersonPropertyId(i);

				PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = //

						PersonPropertyDefinitionInitialization	.builder()//
																.setPersonPropertyId(personPropertyId)//
																.setPropertyDefinition(propertyDefinition)//
																.build();
				personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);
				expectedObservations.add(new MultiKey((double) i, personPropertyId));

			}));
		});

		/*
		 * have the observer show that the expected and actual observations are
		 * equal
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 6462842714052608355L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyUpdateEvent", args = { RegionId.class, PersonPropertyId.class })
	public void testGetEventFilterForPersonPropertyUpdateEvent_region_property() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();
		Set<Pair<RegionId, TestPersonPropertyId>> selectedPairs = new LinkedHashSet<>();

		/*
		 * have an actor determine which region and property pairs will be
		 * observed
		 */
		pluginBuilder.addTestActorPlan("selector", new TestActorPlan(0, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			Set<RegionId> regionIds = regionsDataManager.getRegionIds();
			for (RegionId regionId : regionIds) {
				if (randomGenerator.nextDouble() < 0.6) {
					continue;
				}
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (!propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						continue;
					}
					selectedPairs.add(new Pair<>(regionId, propertyId));
				}
			}
		}));

		/*
		 * have an observer subscribe to the selected (person, property) pairs
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			for (Pair<RegionId, TestPersonPropertyId> pair : selectedPairs) {
				RegionId regionId = pair.getFirst();
				TestPersonPropertyId propertyId = pair.getSecond();
				EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(regionId, propertyId);
				assertNotNull(eventFilter);
				c.subscribe(eventFilter, (c2, e) -> {
					MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.currentPropertyValue());
					actualObservations.add(multiKey);
				});
			}
		}));

		/*
		 * Have an actor change every person's properties for all those
		 * properties that can be changed over three distinct times.
		 */
		for (int i = 1; i < 2; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						for (PersonId personId : peopleDataManager.getPeople()) {
							Object randomPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
							personPropertiesDataManager.setPersonPropertyValue(personId, propertyId, randomPropertyValue);
							RegionId regionId = regionsDataManager.getPersonRegion(personId);
							Pair<RegionId, TestPersonPropertyId> pair = new Pair<>(regionId, propertyId);

							if (selectedPairs.contains(pair)) {
								MultiKey multiKey = new MultiKey(c.getTime(), personId, propertyId, randomPropertyValue);
								expectedObservations.add(multiKey);
							}
						}
					}
				}
			}));
		}

		/*
		 * have the observer show that the expected and actual observations are
		 * equal
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(100, 2659336653501353916L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

		// precondition test: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 6900159997685687591L, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(regionId, null);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the person property id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 7580223995144844140L, (c) -> {
				RegionId regionId = TestRegionId.REGION_1;
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(regionId, TestPersonPropertyId.getUnknownPersonPropertyId());
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

		// precondition test: if the region id is null
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 451632169807459388L, (c) -> {
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				RegionId nullRegionId = null;
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(nullRegionId, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

		// precondition test: if the person id is not known
		contractException = assertThrows(ContractException.class, () -> {
			Factory factory2 = PersonPropertiesTestPluginFactory.factory(10, 558207030058239684L, (c) -> {
				RegionId regionId = TestRegionId.getUnknownRegionId();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent(regionId, TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK);
			});
			TestSimulation.builder().addPlugins(factory2.getPlugins()).build().execute();
		});
		assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "getEventFilterForPersonPropertyUpdateEvent", args = {})
	public void testGetEventFilterForPersonPropertyUpdateEvent() {
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		/*
		 * have an observer subscribe to every person property id
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			EventFilter<PersonPropertyUpdateEvent> eventFilter = personPropertiesDataManager.getEventFilterForPersonPropertyUpdateEvent();
			assertNotNull(eventFilter);
			c.subscribe(eventFilter, (c2, e) -> {
				MultiKey multiKey = new MultiKey(c.getTime(), e.personId(), e.personPropertyId(), e.currentPropertyValue());
				actualObservations.add(multiKey);
			});

		}));

		/*
		 * have an actor change every person's properties for all those
		 * properties that can be changed over three distinct times
		 */
		for (int i = 1; i < 2; i++) {
			pluginBuilder.addTestActorPlan("actor", new TestActorPlan(i, (c) -> {
				StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
				RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
				PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
				PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
				for (TestPersonPropertyId propertyId : TestPersonPropertyId.values()) {
					if (propertyId.getPropertyDefinition().propertyValuesAreMutable()) {
						for (PersonId personId : peopleDataManager.getPeople()) {
							Object randomPropertyValue = propertyId.getRandomPropertyValue(randomGenerator);
							personPropertiesDataManager.setPersonPropertyValue(personId, propertyId, randomPropertyValue);
							MultiKey multiKey = new MultiKey(c.getTime(), personId, propertyId, randomPropertyValue);
							expectedObservations.add(multiKey);
						}
					}
				}
			}));
		}

		/*
		 * have the observer show that the expected and actual observations are
		 * equal
		 */
		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(4, (c) -> {
			assertTrue(expectedObservations.size() > 0);
			assertEquals(expectedObservations, actualObservations);
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 3804034702019855460L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	/**
	 * Demonstrates that the data manager exhibits run continuity. The state of
	 * the data manager is not effected by repeatedly starting and stopping the
	 * simulation.
	 */
	@Test
	@UnitTestMethod(target = PersonPropertiesDataManager.class, name = "init", args = { DataManagerContext.class })
	public void testStateContinuity() {
		
		

		/*
		 * Note that we are not testing the content of the plugin datas -- that
		 * is covered by the other state tests. We show here only that the
		 * resulting plugin data state is the same without regard to how we
		 * break up the run.
		 */

		Set<PersonPropertiesPluginData> pluginDatas = new LinkedHashSet<>();
		pluginDatas.add(testStateContinuity(1));
		pluginDatas.add(testStateContinuity(5));
		pluginDatas.add(testStateContinuity(10));

		assertEquals(1, pluginDatas.size());

	}

	/*
	 * Returns the person properties plugin data resulting from several person
	 * property events over several days. Attempts to stop and start the
	 * simulation by the given number of increments.
	 */
	private PersonPropertiesPluginData testStateContinuity(int incrementCount) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2767991670068250768L);
		
		/*
		 * Build the RunContinuityPluginData with several context consumers that
		 * will add people, person properties and set some person properties
		 */
		RunContinuityPluginData.Builder continuityBuilder = RunContinuityPluginData.builder();

		// Add a few people
		continuityBuilder.addContextConsumer(0.5, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);

			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_2).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_3).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_4).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_3).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_2).build());
			peopleDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());

		});

		// define a few person properties
		continuityBuilder.addContextConsumer(1.2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = //
					PersonPropertyDefinitionInitialization	.builder()//
															.setPersonPropertyId(testPersonPropertyId)//
															.setPropertyDefinition(testPersonPropertyId.getPropertyDefinition())//
															.setTrackTimes(true)//
															.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			personPropertyDefinitionInitialization = //
					PersonPropertyDefinitionInitialization	.builder()//
															.setPersonPropertyId(testPersonPropertyId)//
															.setPropertyDefinition(testPersonPropertyId.getPropertyDefinition())//
															.setTrackTimes(true)//
															.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

		});

		// set some person properties
		continuityBuilder.addContextConsumer(1.8, (c) -> {

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(0), testPersonPropertyId, true);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(3), testPersonPropertyId, true);

			testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(4), testPersonPropertyId, 14);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(6), testPersonPropertyId, 88);

		});

		// define another property without a default, add a few more people
		continuityBuilder.addContextConsumer(2.05, (c) -> {
			
			
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_9_DOUBLE_IMMUTABLE_NO_TRACK;
					PersonPropertyDefinitionInitialization	.Builder builder =
					PersonPropertyDefinitionInitialization	.builder();//
					builder.setPersonPropertyId(testPersonPropertyId);//
					builder.setPropertyDefinition(testPersonPropertyId.getPropertyDefinition());//
					builder.setTrackTimes(true);//
			for(PersonId personId : peopleDataManager.getPeople()) {
				builder.addPropertyValue(personId, testPersonPropertyId.getRandomPropertyValue(randomGenerator));
			}				
															
			PersonPropertyDefinitionInitialization personPropertyDefinitionInitialization = builder.build();
			personPropertiesDataManager.definePersonProperty(personPropertyDefinitionInitialization);

			PersonConstructionData personConstructionData = PersonConstructionData.builder()//
					.add(TestRegionId.REGION_1)//
					.add(new PersonPropertyValueInitialization(testPersonPropertyId, 2.7))//
					.build();
			
			peopleDataManager.addPerson(personConstructionData);
			
			personConstructionData = PersonConstructionData.builder()//
					.add(TestRegionId.REGION_2)//
					.add(new PersonPropertyValueInitialization(testPersonPropertyId, 4.7))//
					.build();
			
			peopleDataManager.addPerson(personConstructionData);
			
			personConstructionData = PersonConstructionData.builder()//
					.add(TestRegionId.REGION_3)//
					.add(new PersonPropertyValueInitialization(testPersonPropertyId, 8.9))//
					.build();
			
			peopleDataManager.addPerson(personConstructionData);

		});

		// set some more properties
		continuityBuilder.addContextConsumer(4.2, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(5), testPersonPropertyId, false);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9), testPersonPropertyId, true);

			testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(2), testPersonPropertyId, 124);
			personPropertiesDataManager.setPersonPropertyValue(new PersonId(9), testPersonPropertyId, 555);
		});

		RunContinuityPluginData runContinuityPluginData = continuityBuilder.build();

		// Build an empty people plugin data for time zero
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();

		// Build a regions plugin data with just a few regions
		RegionsPluginData.Builder regionsBuilder = RegionsPluginData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionsBuilder.addRegion(testRegionId);
		}
		RegionsPluginData regionsPluginData = regionsBuilder.build();

		// build an empty person properties plugin data
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();

		// build the initial simulation state data -- time starts at zero
		SimulationState simulationState = SimulationState.builder().build();

		/*
		 * Run the simulation in one day increments until all the plans in the
		 * run continuity plugin data have been executed
		 */
		double haltTime = 0;
		double maxTime = Double.NEGATIVE_INFINITY;
		for (Pair<Double, Consumer<ActorContext>> pair : runContinuityPluginData.getConsumers()) {
			Double time = pair.getFirst();
			maxTime = FastMath.max(maxTime, time);
		}
		double timeIncrement = maxTime / incrementCount;
		while (!runContinuityPluginData.allPlansComplete()) {
			haltTime += timeIncrement;

			// build the run continuity plugin
			Plugin runContinuityPlugin = RunContinuityPlugin.builder()//
															.setRunContinuityPluginData(runContinuityPluginData)//
															.build();

			// build the people plugin
			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

			// build the regions plugin
			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

			// build the person properties plugin
			Plugin personPropertyPlugin = PersonPropertiesPlugin.builder().setPersonPropertiesPluginData(personPropertiesPluginData).getPersonPropertyPlugin();

			TestOutputConsumer outputConsumer = new TestOutputConsumer();

			// execute the simulation so that it produces a people plugin data
			Simulation simulation = Simulation	.builder()//
												.addPlugin(peoplePlugin)//
												.addPlugin(runContinuityPlugin)//
												.addPlugin(regionsPlugin)//
												.addPlugin(personPropertyPlugin).setSimulationHaltTime(haltTime)//
												.setRecordState(true)//
												.setOutputConsumer(outputConsumer)//
												.setSimulationState(simulationState)//
												.build();//
			simulation.execute();

			// retrieve the people plugin data
			peoplePluginData = outputConsumer.getOutputItem(PeoplePluginData.class).get();

			// retrieve the simulation state
			simulationState = outputConsumer.getOutputItem(SimulationState.class).get();

			// retrieve the region plugin data
			regionsPluginData = outputConsumer.getOutputItem(RegionsPluginData.class).get();

			// retrieve the person properties plugin data
			personPropertiesPluginData = outputConsumer.getOutputItem(PersonPropertiesPluginData.class).get();

			// retrieve the run continuity plugin data
			runContinuityPluginData = outputConsumer.getOutputItem(RunContinuityPluginData.class).get();
		}

		return personPropertiesPluginData;

	}

}
