package plugins.personproperties;

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
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.DataManagerContext;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.testplugin.ScenarioPlanCompletionObserver;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.PersonDataManager;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.support.PersonPropertyError;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.support.PersonPropertyInitialization;
import plugins.personproperties.testsupport.PersonPropertiesActionSupport;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.regions.RegionPlugin;
import plugins.regions.RegionPluginData;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.stochastics.StochasticsDataManager;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;
import util.wrappers.MutableInteger;

@UnitTest(target = PersonPropertiesDataManager.class)
public final class AT_PersonPropertyDataManager {

	@Test
	@UnitTestMethod(name = "getPeopleWithPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPeopleWithPropertyValue() {

		PersonPropertiesActionSupport.testConsumer(100, 7917315534360369845L, (c) -> {

			// establish data views
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = personDataManager.getPeople();
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

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPersonCountForPropertyValue() {

		PersonPropertiesActionSupport.testConsumer(100, 686456599634987511L, (c) -> {

			// establish data views
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = personDataManager.getPeople();
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
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
	public void testGetPersonPropertyDefinition() {

		PersonPropertiesActionSupport.testConsumer(0, 138806179316502662L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			// show that the person property definitions match expectations
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				PropertyDefinition expectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(testPersonPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// precondition tests

			// if the person property id is null
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyDefinition(null));
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyDefinition(TestPersonPropertyId.getUnknownPersonPropertyId()));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		PersonPropertiesActionSupport.testConsumer(0, 8485097765777963229L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			EnumSet<TestPersonPropertyId> expectedPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
			Set<PersonPropertyId> actualPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
			assertEquals(expectedPropertyIds, actualPropertyIds);
		});
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyTime", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyTime() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// show that all person property times are 0 for the time-tracked
		// properties
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
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

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK, randomGenerator.nextInt());
			}
		}));

		// Set property 6 for all people at time 2
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			RandomGenerator randomGenerator = c.getDataManager(StochasticsDataManager.class).get().getRandomGenerator();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK, randomGenerator.nextDouble());
			}
		}));

		// show that the person property times agree with the times above
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(3, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
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
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

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
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(personId, unknownPersonPropertyId));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property does not have time tracking turned on in
			// the associated property definition
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyTime(personId, untrackedPersonPropertyId));
			assertEquals(PersonPropertyError.PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		PersonPropertiesActionSupport.testConsumers(10, 6980289425630085602L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyValue() {

		PersonPropertiesActionSupport.testConsumer(10, 816143115345188642L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create a container to hold expectations
			Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();

			// assign random values for property 2 for all the people
			List<PersonId> people = personDataManager.getPeople();
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
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(personId, unknownPersonPropertyId));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestConstructor(args = { Context.class, PersonPropertiesPluginData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertiesDataManager(null));
		assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		PersonPropertiesActionSupport.testConsumer(20, 7153865371557964932L, (c) -> {
			// show that a negative growth causes an exception
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.expandCapacity(-1));
			assertEquals(PersonError.NEGATIVE_GROWTH_PROJECTION, contractException.getErrorType());
		});
		// use manual tests for non-negative growth
	}

	@Test
	@UnitTestMethod(name = "personPropertyIdExists", args = { PersonPropertyId.class })
	public void testPersonPropertyIdExists() {

		PersonPropertiesActionSupport.testConsumer(0, 4797443283568888200L, (c) -> {
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				assertTrue(personPropertiesDataManager.personPropertyIdExists(testPersonPropertyId));
			}
			assertFalse(personPropertiesDataManager.personPropertyIdExists(TestPersonPropertyId.getUnknownPersonPropertyId()));
		});

	}

	@Test
	@UnitTestMethod(name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		// create some containers to hold the expected and actual observations
		// for later comparison
		Set<MultiKey> expectedObservations = new LinkedHashSet<>();
		Set<MultiKey> actualObservations = new LinkedHashSet<>();

		// add an agent that will observe changes to all person properties

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				EventLabel<PersonPropertyUpdateEvent> eventLabel = PersonPropertyUpdateEvent.getEventLabelByProperty(c, testPersonPropertyId);
				c.subscribe(eventLabel, (c2, e) -> {
					actualObservations.add(new MultiKey(e.getPersonId(), e.getPersonPropertyId(), e.getPreviousPropertyValue(), e.getCurrentPropertyValue()));
				});
			}
		}));

		/*
		 * Add an agent that will alter person property values and record the
		 * corresponding expected observations.
		 */
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
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
			List<PersonId> people = personDataManager.getPeople();

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
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
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
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the person property id is unknown
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, unknownPersonPropertyId, value));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the property value is null
			contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, null));
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

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
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);

		PersonPropertiesActionSupport.testConsumers(10, 2321272063791878719L, testPlugin);

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonPropertyUpdateEventLabelers() {

		/*
		 * For each labeler, show that the labeler was previously added,
		 * presumably by the resolver.
		 */

		PersonPropertiesActionSupport.testConsumer(100, 4585617051924828596L, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			EventLabeler<PersonPropertyUpdateEvent> eventLabelerForRegionAndProperty = PersonPropertyUpdateEvent.getEventLabelerForRegionAndProperty(regionDataManager);
			assertNotNull(eventLabelerForRegionAndProperty);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForRegionAndProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});

		PersonPropertiesActionSupport.testConsumer(100, 3679887899361025474L, (c) -> {
			EventLabeler<PersonPropertyUpdateEvent> eventLabelerForPersonAndProperty = PersonPropertyUpdateEvent.getEventLabelerForPersonAndProperty();
			assertNotNull(eventLabelerForPersonAndProperty);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForPersonAndProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());
		});

		PersonPropertiesActionSupport.testConsumer(100, 7374053088167649497L, (c) -> {
			EventLabeler<PersonPropertyUpdateEvent> eventLabelerForProperty = PersonPropertyUpdateEvent.getEventLabelerForProperty();
			assertNotNull(eventLabelerForProperty);
			ContractException contractException = assertThrows(ContractException.class, () -> c.addEventLabeler(eventLabelerForProperty));
			assertEquals(NucleusError.DUPLICATE_LABELER_ID_IN_EVENT_LABELER, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonPropertyDataManagerInitialization() {

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			people.add(new PersonId(i));
		}

		// create a random generator
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2693836950854697940L);

		Builder builder = Simulation.builder();

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.getPartitionsPlugin());

		// add the people plugin
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		PeoplePluginData peoplePluginData = peopleBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);
		builder.addPlugin(peoplePlugin);

		// add the person property plugin
		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}
		for (PersonId personId : people) {
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				if(randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertyBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
		builder.addPlugin(personPropertyPlugin);

		// add the regions plugin
		RegionPluginData.Builder regionBuilder = RegionPluginData.builder();

		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionBuilder.setPersonRegion(personId, randomRegionId);
		}
		RegionPluginData regionPluginData = regionBuilder.build();
		Plugin regionPlugin = RegionPlugin.getRegionPlugin(regionPluginData);

		builder.addPlugin(regionPlugin);

		// add the report plugin
		ReportsPluginData reportsPluginData = ReportsPluginData.builder().build();
		Plugin reportPlugin = ReportsPlugin.getReportPlugin(reportsPluginData);
		builder.addPlugin(reportPlugin);

		// add the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
		builder.addPlugin(stochasticsPlugin);

		// add the action plugin
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Add an agent that will show that the person property data view is
		 * properly initialized from the person property initial data
		 */

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c2) -> {
			// get the person property data view
			PersonPropertiesDataManager personPropertiesDataManager = c2.getDataManager(PersonPropertiesDataManager.class).get();
			PersonDataManager personDataManager = c2.getDataManager(PersonDataManager.class).get();

			// show that the property ids are correct
			assertEquals(personPropertiesPluginData.getPersonPropertyIds(), personPropertiesDataManager.getPersonPropertyIds());

			// show that the property definitions are correct
			for (PersonPropertyId personPropertyId : personPropertiesPluginData.getPersonPropertyIds()) {
				PropertyDefinition expectedPropertyDefinition = personPropertiesPluginData.getPersonPropertyDefinition(personPropertyId);
				PropertyDefinition actualPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				assertEquals(expectedPropertyDefinition, actualPropertyDefinition);
			}

			// show that the person property values are set to the default
			// values
			List<PersonId> personIds = personDataManager.getPeople();
			assertTrue(personIds.size() > 0);
			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					Object expectedValue = personPropertiesPluginData.getPersonPropertyValue(personId, testPersonPropertyId);
					Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(expectedValue, actualValue);

					boolean timeTrackingOn = testPersonPropertyId.getPropertyDefinition().getTimeTrackingPolicy().equals(TimeTrackingPolicy.TRACK_TIME);
					if (timeTrackingOn) {
						assertEquals(0.0, personPropertiesDataManager.getPersonPropertyTime(personId, testPersonPropertyId));
					}
				}
			}
		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		builder.addPlugin(testPlugin);

		// build and execute the engine
		ScenarioPlanCompletionObserver scenarioPlanCompletionObserver = new ScenarioPlanCompletionObserver();
		builder.setOutputConsumer(scenarioPlanCompletionObserver::handleOutput).build().execute();

		// show that all actions were executed
		assertTrue(scenarioPlanCompletionObserver.allPlansExecuted());

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonAdditionEvent() {

		PersonPropertiesActionSupport.testConsumer(100, 4771130331997762252L, (c) -> {
			// establish data views
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			// get the random generator for use later
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// add a person with some person property auxiliary data
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();

			// create a container to hold expectations
			Map<PersonPropertyId, Object> expectedPropertyValues = new LinkedHashMap<>();

			// set the expectation to the default values of all the properties
			Set<PersonPropertyId> personPropertyIds = personPropertiesDataManager.getPersonPropertyIds();
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				PropertyDefinition personPropertyDefinition = personPropertiesDataManager.getPersonPropertyDefinition(personPropertyId);
				Object value = personPropertyDefinition.getDefaultValue().get();
				expectedPropertyValues.put(personPropertyId, value);
			}

			// set two properties to random values and record them in the
			// expected data
			int iValue = randomGenerator.nextInt();
			personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, iValue);

			double dValue = randomGenerator.nextDouble();
			personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue));
			expectedPropertyValues.put(TestPersonPropertyId.PERSON_PROPERTY_3_DOUBLE_MUTABLE_NO_TRACK, dValue);
			personBuilder.add(TestRegionId.REGION_1);
			PersonConstructionData personConstructionData = personBuilder.build();
			// add the person and get its person id
			PersonId personId = personDataManager.addPerson(personConstructionData);

			// show that the person exists
			assertTrue(personDataManager.personExists(personId));

			// show that the person has the correct property values
			for (PersonPropertyId personPropertyId : personPropertyIds) {
				Object expectedValue = expectedPropertyValues.get(personPropertyId);
				Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				assertEquals(expectedValue, actualValue);
			}

		});

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a person property value that is
		 * not compatible with the corresponding property definition
		 */
		PersonPropertiesActionSupport.testConsumer(100, 5194635938533128930L, (c) -> {
			// add a person with some person property auxiliary data
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45));
				PersonConstructionData constructionData = personBuilder.build();
				personDataManager.addPerson(constructionData);
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a null person property value
		 */
		PersonPropertiesActionSupport.testConsumer(100, 4349734439660163798L, (c) -> {
			// add a person with some person property auxiliary data
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, null));
				PersonConstructionData constructionData = personBuilder.build();
				personDataManager.addPerson(constructionData);
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

		});

		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has an unknown person property id
		 */
		PersonPropertiesActionSupport.testConsumer(100, 2152152824636786936L, (c) -> {
			// add a person with some person property auxiliary data
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.getUnknownPersonPropertyId(), false));
				PersonConstructionData constructionData = personBuilder.build();
				personDataManager.addPerson(constructionData);
			});
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

		});
		/*
		 * precondition test: if the event contains a
		 * PersonPropertyInitialization that has a null person property id
		 */
		PersonPropertiesActionSupport.testConsumer(100, 8379070211267955743L, (c) -> {
			// add a person with some person property auxiliary data
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// if the event contains a PersonPropertyInitialization that has a
			// null person property id
			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(null, false));
				PersonConstructionData constructionData = personBuilder.build();
				personDataManager.addPerson(constructionData);
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testBulkPersonAdditionEvent() {
		PersonPropertiesActionSupport.testConsumer(0, 2547218192811543040L, (c) -> {
			// establish data views
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			// get the random generator for use later
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// set the number of people we will add
			int bulkPersonCount = 20;

			/*
			 * Create a container to hold expectations. Note that we choose to
			 * not assign about half of the property values so that we can
			 * demonstrate that default values are being used correctly.
			 */
			Map<PersonId, Map<PersonPropertyId, Object>> expectedPropertyValues = new LinkedHashMap<>();
			Map<PersonId, Map<PersonPropertyId, Boolean>> shouldBeAssigned = new LinkedHashMap<>();

			/*
			 * for each person, set the expectations randomly to either the
			 * default value of the property or a new randomized value
			 */

			for (int i = 0; i < bulkPersonCount; i++) {
				PersonId personId = new PersonId(i);
				Map<PersonPropertyId, Object> propertyValueMap = new LinkedHashMap<>();
				expectedPropertyValues.put(personId, propertyValueMap);

				Map<PersonPropertyId, Boolean> propertyAssignmentMap = new LinkedHashMap<>();
				shouldBeAssigned.put(personId, propertyAssignmentMap);

				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					if (randomGenerator.nextBoolean()) {
						Object value = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
						propertyValueMap.put(testPersonPropertyId, value);
						propertyAssignmentMap.put(testPersonPropertyId, true);
					} else {
						PropertyDefinition personPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
						Object value = personPropertyDefinition.getDefaultValue().get();
						propertyValueMap.put(testPersonPropertyId, value);
						propertyAssignmentMap.put(testPersonPropertyId, false);
					}

				}
			}

			// Construct the bulk add event from the expected values
			BulkPersonConstructionData.Builder bulkBuilder = BulkPersonConstructionData.builder();
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();

			for (PersonId personId : expectedPropertyValues.keySet()) {
				Map<PersonPropertyId, Object> propertyValueMap = expectedPropertyValues.get(personId);
				Map<PersonPropertyId, Boolean> propertyAssignmentMap = shouldBeAssigned.get(personId);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					Boolean shouldAssignProperty = propertyAssignmentMap.get(testPersonPropertyId);
					if (shouldAssignProperty) {
						Object value = propertyValueMap.get(testPersonPropertyId);
						personBuilder.add(new PersonPropertyInitialization(testPersonPropertyId, value));
					}
				}

				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));

				bulkBuilder.add(personBuilder.build());
			}

			// add the people via the bulk creation event
			personDataManager.addBulkPeople(bulkBuilder.build());

			// show that the people exist
			for (PersonId personId : expectedPropertyValues.keySet()) {
				assertTrue(personDataManager.personExists(personId));
			}

			// show that the people have the correct property values
			for (PersonId personId : expectedPropertyValues.keySet()) {
				Map<PersonPropertyId, Object> propertyValueMap = expectedPropertyValues.get(personId);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
					Object expectedValue = propertyValueMap.get(testPersonPropertyId);
					Object actualValue = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);
					assertEquals(expectedValue, actualValue);
				}
			}

			// precondition tests
			// if the event contains a PersonPropertyInitialization that has a
			// null person property id
			ContractException contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(null, false));
				bulkBuilder.add(personBuilder.build());
				personDataManager.addBulkPeople(bulkBuilder.build());
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has an
			// unknown person property id
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.getUnknownPersonPropertyId(), false));
				bulkBuilder.add(personBuilder.build());
				personDataManager.addBulkPeople(bulkBuilder.build());
			});
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// null person property value
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, null));
				bulkBuilder.add(personBuilder.build());
				personDataManager.addBulkPeople(bulkBuilder.build());
			});
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_VALUE, contractException.getErrorType());

			// if the event contains a PersonPropertyInitialization that has a
			// person property value that is not compatible with the
			// corresponding property definition
			contractException = assertThrows(ContractException.class, () -> {
				personBuilder.add(TestRegionId.getRandomRegionId(randomGenerator));
				personBuilder.add(new PersonPropertyInitialization(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, 45));
				bulkBuilder.add(personBuilder.build());
				personDataManager.addBulkPeople(bulkBuilder.build());
			});
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonImminentRemovalEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Have the agent remove a person and show that their properties remain
		 * during the current span of this agent's activation
		 */

		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PersonId personId = new PersonId(0);

			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			assertTrue(personDataManager.personExists(personId));

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			// Set the property value to a non-default value.
			Integer expectedPropertyValue = 999;
			personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, expectedPropertyValue);

			// remove the person
			personDataManager.removePerson(personId);

			// show that the property value is still present
			Object actualPropertyValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
			assertEquals(expectedPropertyValue, actualPropertyValue);

		}));

		// Have the agent now show that these person properties are no longer
		// available
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			PersonId personId = new PersonId(0);

			// show that the person does not exist
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			assertFalse(personDataManager.personExists(personId));

			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();

			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			ContractException contractException = assertThrows(ContractException.class, () -> personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		}));

		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getTestPlugin(testPluginData);
		PersonPropertiesActionSupport.testConsumers(10, 2020442537537236753L, testPlugin);

	}

	//
	// 3100440347097616280L
	// 4627357002700907595L
	// 3525837507821440138L
	// 3404160152913070084L
	// 8802528032031272978L
	// 7460910928660168768L
	// 1498052576475289605L
	// 3969826324474876300L
	// 1426493903052832076L
	// 15986402242167215L
	// 6427460339373497496L
	// 2948423500320063643L
	// 4844530530516661042L
	// 3419491894809824507L
	// 598790801002069301L
	// 1814619428719379985L
	// 1992913840461043389L
	// 2470473626116352915L
	// 1697643618060460580L
	// 4289247820442564478L
	// 5464618450744160243L
	// 6590208175805030029L
	// 2042914967398742326L
}
