package plugins.personproperties.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.DataView;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import nucleus.NucleusError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.CompartmentPlugin;
import plugins.compartments.initialdata.CompartmentInitialData;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.initialdata.PersonPropertyInitialData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.PropertiesPlugin;
import plugins.properties.support.PropertyDefinition;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.datacontainers.StochasticsDataView;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.MutableInteger;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyDataManager.class)
public final class AT_PersonPropertyDataManager {

	private void testConsumer(int initialPopulation, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, pluginBuilder.build());
	}

	private void testConsumers(int initialPopulation, long seed, ActionPlugin actionPlugin) {

		EngineBuilder engineBuilder = Engine.builder();

		// add the person property plugin
		PersonPropertyInitialData.Builder personPropertyBuilder = PersonPropertyInitialData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}

		engineBuilder.addPlugin(PersonPropertiesPlugin.PLUGIN_ID, new PersonPropertiesPlugin(personPropertyBuilder.build())::init);

		// add the people plugin
		engineBuilder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);
		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		engineBuilder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		engineBuilder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the compartments plugin
		CompartmentInitialData.Builder compartmentBuilder = CompartmentInitialData.builder();

		// add the compartments
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			compartmentBuilder.setCompartmentInitialBehaviorSupplier(testCompartmentId, () -> (c2) -> {
			});
		}

		// assign people to compartments
		TestCompartmentId testCompartmentId = TestCompartmentId.COMPARTMENT_1;
		for (PersonId personId : people) {
			compartmentBuilder.setPersonCompartment(personId, testCompartmentId.next());
		}

		engineBuilder.addPlugin(CompartmentPlugin.PLUGIN_ID, new CompartmentPlugin(compartmentBuilder.build())::init);

		// add the regions plugin
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();

		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c2) -> {
			});
		}

		// assign people to regions
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionBuilder.setPersonRegion(personId, testRegionId.next());
		}

		engineBuilder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the component plugin
		engineBuilder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		engineBuilder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		engineBuilder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);

		// add the action plugin
		engineBuilder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		engineBuilder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	/*
	 * Returns a person property manager that has been initialized with the
	 * property ids and definitions found in the person property data view
	 * contained in the context. PERSON PROPERTY VALUES ARE NOT TRANSFERRED.
	 */
	private PersonPropertyDataManager getPersonPropertyDataManager(Context context) {
		PersonPropertyDataManager result = new PersonPropertyDataManager(context);
		PersonPropertyDataView personPropertyDataView = context.getDataView(PersonPropertyDataView.class).get();

		for (PersonPropertyId personPropertyId : personPropertyDataView.getPersonPropertyIds()) {
			PropertyDefinition personPropertyDefinition = personPropertyDataView.getPersonPropertyDefinition(personPropertyId);
			result.definePersonProperty(personPropertyId, personPropertyDefinition);
		}

		return result;
	}

	@Test
	@UnitTestMethod(name = "getPeopleWithPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPeopleWithPropertyValue() {
		testConsumer(100, 7060179502758732949L, (c) -> {

			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = personDataView.getPeople();
			Map<Integer, Set<PersonId>> expectedValuesToPeople = new LinkedHashMap<>();
			for (int i = 0; i < 3; i++) {
				expectedValuesToPeople.put(i, new LinkedHashSet<>());
			}

			int defaultValue = (Integer) personPropertyDataManager.getPersonPropertyDefinition(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK).getDefaultValue().get();
			for (PersonId personId : people) {
				int value = randomGenerator.nextInt(3);
				// leave out the people with the default value to show that we
				// can still retrieve them by the value
				if (value != defaultValue) {
					personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				}
				expectedValuesToPeople.get(value).add(personId);
			}

			// show that the proper people are returned for each value
			for (Integer value : expectedValuesToPeople.keySet()) {
				List<PersonId> actualPeople = personPropertyDataManager.getPeopleWithPropertyValue(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				Set<PersonId> expectedPeople = expectedValuesToPeople.get(value);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCountForPropertyValue", args = { PersonPropertyId.class, Object.class })
	public void testGetPersonCountForPropertyValue() {
		testConsumer(100, 6619827209354169712L, (c) -> {

			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Assign random values of 1, 2 or 3 for property 2 to all people.
			 * Build a structure to hold expected results.
			 */
			List<PersonId> people = personDataView.getPeople();
			Map<Integer, MutableInteger> expectedValuesToPeople = new LinkedHashMap<>();
			for (int i = 0; i < 3; i++) {
				expectedValuesToPeople.put(i, new MutableInteger());
			}

			int defaultValue = (Integer) personPropertyDataManager.getPersonPropertyDefinition(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK).getDefaultValue().get();
			for (PersonId personId : people) {
				int value = randomGenerator.nextInt(3);
				// leave out the people with the default value to show that we
				// can still retrieve them by the value
				if (value != defaultValue) {
					personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				}
				expectedValuesToPeople.get(value).increment();
			}

			// show that the proper counts are returned for each value
			for (Integer value : expectedValuesToPeople.keySet()) {
				int actualCount = personPropertyDataManager.getPersonCountForPropertyValue(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				MutableInteger mutableInteger = expectedValuesToPeople.get(value);
				assertEquals(mutableInteger.getValue(), actualCount);
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyDefinition", args = { PersonPropertyId.class })
	public void testGetPersonPropertyDefinition() {

		testConsumer(100, 4984897488257030056L, (c) -> {

			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				PropertyDefinition expectectedPropertyDefinition = testPersonPropertyId.getPropertyDefinition();
				PropertyDefinition actualPropertyDefinition = personPropertyDataManager.getPersonPropertyDefinition(testPersonPropertyId);
				assertEquals(expectectedPropertyDefinition, actualPropertyDefinition);
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyIds", args = {})
	public void testGetPersonPropertyIds() {
		testConsumer(100, 195846320342030259L, (c) -> {

			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// show that the correct property ids are retrieved.
			EnumSet<TestPersonPropertyId> expectedPropertyIds = EnumSet.allOf(TestPersonPropertyId.class);
			Set<PersonPropertyId> actualPropertyIds = personPropertyDataManager.getPersonPropertyIds();
			assertEquals(expectedPropertyIds, actualPropertyIds);

		});
	}

	/*
	 * A data view that allows the sharing of a person property manager instance
	 * amongst the agents.
	 */
	private static class TestDataView implements DataView {
		PersonPropertyDataManager personPropertyDataManager;
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyTime", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyTime() {

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");

		pluginBuilder.addDataView(new TestDataView());

		// Have the agent initialize the person property data manager that we
		// will test and make it available to the other action plans
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {
			TestDataView testDataView = c.getDataView(TestDataView.class).get();
			testDataView.personPropertyDataManager = getPersonPropertyDataManager(c);
		}));

		// show that all person property times are 0 for the time-tracked
		// properties
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			PersonPropertyDataManager personPropertyDataManager = c.getDataView(TestDataView.class).get().personPropertyDataManager;

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {
				double personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
			}
		}));

		// Set property 5 for all people at time 1
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			PersonPropertyDataManager personPropertyDataManager = c.getDataView(TestDataView.class).get().personPropertyDataManager;
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			for (PersonId personId : people) {
				personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK, randomGenerator.nextInt());
			}
		}));

		// Set property 6 for all people at time 2
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(2, (c) -> {
			PersonPropertyDataManager personPropertyDataManager = c.getDataView(TestDataView.class).get().personPropertyDataManager;
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			RandomGenerator randomGenerator = c.getDataView(StochasticsDataView.class).get().getRandomGenerator();
			for (PersonId personId : people) {
				personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK, randomGenerator.nextDouble());
			}
		}));

		// show that the person property times agree with the times above
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(3, (c) -> {
			PersonPropertyDataManager personPropertyDataManager = c.getDataView(TestDataView.class).get().personPropertyDataManager;
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {
				double personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK);
				assertEquals(0.0, personPropertyTime);
				personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_5_INTEGER_MUTABLE_TRACK);
				assertEquals(1.0, personPropertyTime);
				personPropertyTime = personPropertyDataManager.getPersonPropertyTime(personId, TestPersonPropertyId.PERSON_PROPERTY_6_DOUBLE_MUTABLE_TRACK);
				assertEquals(2.0, personPropertyTime);
			}
		}));

		testConsumers(10, 2926733502185652450L, pluginBuilder.build());

	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class })
	public void testGetPersonPropertyValue() {

		testConsumer(10, 3301271612384036841L, (c) -> {

			// initialize a person property manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a container to hold expectations
			Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();

			// assign random values for property 2 for all people
			for (PersonId personId : people) {
				int value = randomGenerator.nextInt();
				personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				expectedValues.put(personId, value);
			}

			// show that we can get the values as expected
			for (PersonId personId : people) {
				Integer actualValue = personPropertyDataManager.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
				Integer expectedValue = expectedValues.get(personId);
				assertEquals(expectedValue, actualValue);
			}

		});

	}

	@Test
	@UnitTestMethod(name = "handlePersonRemoval", args = { PersonId.class })
	public void testHandlePersonRemoval() {
		testConsumer(10, 3297843984763292386L, (c) -> {
			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			/*
			 * Add a property definition that will not get mapped to a
			 * primitive. Any property that can be treated as a primitive will
			 * not result in the value being removed and we do not care what
			 * value is retained. All we are trying to show here is that object
			 * references are dropped if the person is removed.
			 */
			PersonPropertyId personPropertyId = new PersonPropertyId() {
			};
			PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(String.class).setDefaultValue("A").build();
			personPropertyDataManager.definePersonProperty(personPropertyId, propertyDefinition);

			// set the property value to "B" for all people
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			for (PersonId personId : people) {
				personPropertyDataManager.setPersonPropertyValue(personId, personPropertyId, "B");
			}

			// remove the people
			for (PersonId personId : people) {
				personPropertyDataManager.handlePersonRemoval(personId);
			}

			// show that the value reverts back to the default
			for (PersonId personId : people) {
				String actualValue = personPropertyDataManager.getPersonPropertyValue(personId, personPropertyId);
				assertEquals("A", actualValue);
			}

		});

	}

	@Test
	@UnitTestConstructor(args = { Context.class, PersonPropertyInitialData.class })
	public void testConstructor() {
		ContractException contractException = assertThrows(ContractException.class, () -> new PersonPropertyDataManager(null));
		assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(name = "expandCapacity", args = { int.class })
	public void testExpandCapacity() {
		// no test -- best tested manually

	}

	@Test
	@UnitTestMethod(name = "personPropertyIdExists", args = { PersonPropertyId.class })
	public void testPersonPropertyIdExists() {

		testConsumer(100, 6715517670945136962L, (c) -> {

			// get an initialized person property data manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// show that the correct property ids are retrieved.
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {				
				assertTrue(personPropertyDataManager.personPropertyIdExists(testPersonPropertyId));
			}
			assertFalse(personPropertyDataManager.personPropertyIdExists(TestPersonPropertyId.getUnknownPersonPropertyId()));
		});

	}

	@Test
	@UnitTestMethod(name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {
		testConsumer(10, 2383009119877353072L, (c) -> {

			// initialize a person property manager
			PersonPropertyDataManager personPropertyDataManager = getPersonPropertyDataManager(c);

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// create a container to hold expectations
			Map<PersonId, Integer> expectedValues = new LinkedHashMap<>();

			// assign random values for property 2 for all people
			for (PersonId personId : people) {
				int value = randomGenerator.nextInt();
				personPropertyDataManager.setPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, value);
				expectedValues.put(personId, value);
			}

			// show that we can get the values as expected
			for (PersonId personId : people) {
				Integer actualValue = personPropertyDataManager.getPersonPropertyValue(personId, TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK);
				Integer expectedValue = expectedValues.get(personId);
				assertEquals(expectedValue, actualValue);
			}

		});
	}

}
