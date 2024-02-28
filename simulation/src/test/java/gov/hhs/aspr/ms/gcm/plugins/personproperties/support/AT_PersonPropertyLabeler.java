package gov.hhs.aspr.ms.gcm.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.events.PersonPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_PersonPropertyLabeler {

	private static class LocalPersonPropertyLabeler extends PersonPropertyLabeler {
		private final Function<Object, Object> labelingFunction;

		public LocalPersonPropertyLabeler(PersonPropertyId personPropertyId, Function<Object, Object> labelingFunction) {
			super(personPropertyId);
			this.labelingFunction = labelingFunction;
		}

		@Override
		protected Object getLabelFromValue(Object value) {
			return labelingFunction.apply(value);
		}
	}

	@Test
	@UnitTestConstructor(target = PersonPropertyLabeler.class, args = { PersonPropertyId.class})
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK;
		PersonPropertyLabeler personPropertyLabeler = new LocalPersonPropertyLabeler(personPropertyId, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = personPropertyLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonPropertyUpdateEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonPropertyUpdateEvent.class, labelerSensitivity.getEventClass());

		/*
		 * Show that the sensitivity will return the person id from a
		 * PersonPropertyUpdateEvent if the event matches the person property
		 * id.
		 */
		PersonId personId = new PersonId(56);
		PersonPropertyUpdateEvent personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, false, true);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personPropertyUpdateEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

		/*
		 * Show that the sensitivity will return an empty optional of person id
		 * from a PersonPropertyUpdateEvent if the event does not match the
		 * person property id.
		 */

		personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		personPropertyUpdateEvent = new PersonPropertyUpdateEvent(personId, personPropertyId, false, true);
		optional = labelerSensitivity.getPersonId(personPropertyUpdateEvent);
		assertFalse(optional.isPresent());

	}

	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "getCurrentLabel", args = { PartitionsContext.class, PersonId.class })
	public void testGetCurrentLabel() {
		/*
		 * Have the agent show that the person property labeler produces a label
		 * for each person that is consistent with the function passed to the
		 * person property labeler.
		 */
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 6445109933336671672L, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select a property to work with
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			/*
			 * Assign random values to the people so that we can get some
			 * variety in the labels
			 */
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, randomGenerator.nextBoolean());
			}

			/*
			 * build a person property labeler with a function that can be
			 * tested
			 */
			Function<Object, Object> function = (input) -> {
				Boolean value = (Boolean) input;
				if (value) {
					return "A";
				}
				return "B";
			};

			PersonPropertyLabeler personPropertyLabeler = new LocalPersonPropertyLabeler(personPropertyId, function);

			/*
			 * Apply the labeler to each person and compare it to the more
			 * direct use of the labeler's function
			 */
			for (PersonId personId : people) {

				// get the person's property value and apply the function
				// directly
				Boolean value = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				Object expectedLabel = function.apply(value);

				// get the label from the person id
				Object actualLabel = personPropertyLabeler.getCurrentLabel(testPartitionsContext, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}

			// precondition tests

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getCurrentLabel(testPartitionsContext, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getCurrentLabel(testPartitionsContext, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "getId", args = {})
	public void testGetId() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertEquals(testPersonPropertyId, new LocalPersonPropertyLabeler(testPersonPropertyId, (c) -> null).getId());
		}
	}
	 
	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertEquals(testPersonPropertyId, new LocalPersonPropertyLabeler(testPersonPropertyId, (c) -> null).getPersonPropertyId());
		}
	}

	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "getPastLabel", args = { PartitionsContext.class, Event.class })
	public void testGetPastLabel() {
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 770141763380713425L, (c) -> {
			
			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);
			
			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select a property to work with
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;

			/*
			 * Assign random values to the people so that we can get some
			 * variety in the labels
			 */
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, randomGenerator.nextInt(100));
			}

			/*
			 * build a person property labeler with a function that can be
			 * tested
			 */
			Function<Object, Object> function = (g) -> {
				Integer integer = (Integer) g;
				return integer;
			};

			PersonPropertyLabeler personPropertyLabeler = new LocalPersonPropertyLabeler(personPropertyId, function);

			/*
			 * Apply the labeler to each person and compare it to the more
			 * direct use of the labeler's function
			 */
			for (PersonId personId : people) {
				int newValue = randomGenerator.nextInt(1000);
				int oldValue = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);

				if (newValue == oldValue) {
					newValue += 1;
				}

				personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId, newValue);
				Object expectedLabel = function.apply(oldValue);

				// get the label
				Object actualLabel = personPropertyLabeler.getPastLabel(testPartitionsContext, new PersonPropertyUpdateEvent(personId, personPropertyId, oldValue, newValue));

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);

			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
	
	
	@Test
	@UnitTestMethod(target = PersonPropertyLabeler.class, name = "toString", args = {})
	public void testToString() {
		
		for(TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			LocalPersonPropertyLabeler localPersonPropertyLabeler = new LocalPersonPropertyLabeler(testPersonPropertyId,(value)->null);
			String actualValue = localPersonPropertyLabeler.toString();
			String expectedValue ="PersonPropertyLabeler [personPropertyId="+testPersonPropertyId+"]";
			assertEquals(expectedValue, actualValue);
		}
		
	}

}
