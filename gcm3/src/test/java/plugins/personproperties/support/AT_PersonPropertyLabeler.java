package plugins.personproperties.support;

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

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.partitions.support.LabelerSensitivity;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyChangeObservationEvent;
import plugins.personproperties.testsupport.PersonPropertiesActionSupport;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.stochastics.StochasticsDataManager;

@UnitTest(target = PersonPropertyLabeler.class)
public class AT_PersonPropertyLabeler {

	@Test
	@UnitTestConstructor(args = { PersonPropertyId.class, Function.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getLabelerSensitivities", args = { PersonPropertyId.class, Function.class, Object.class })
	public void testGetLabelerSensitivities() {

		/*
		 * Get the labeler sensitivities and show that they are consistent with
		 * their documented behaviors.
		 */

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_4_BOOLEAN_MUTABLE_TRACK;
		PersonPropertyLabeler personPropertyLabeler = new PersonPropertyLabeler(personPropertyId, (c) -> null);

		Set<LabelerSensitivity<?>> labelerSensitivities = personPropertyLabeler.getLabelerSensitivities();

		// show that there is exactly one sensitivity
		assertEquals(1, labelerSensitivities.size());

		// show that the sensitivity is associated with
		// PersonPropertyChangeObservationEvent
		LabelerSensitivity<?> labelerSensitivity = labelerSensitivities.iterator().next();
		assertEquals(PersonPropertyChangeObservationEvent.class, labelerSensitivity.getEventClass());

		/*
		 * Show that the sensitivity will return the person id from a
		 * PersonCompartmentChangeObservationEvent if the event matches the
		 * person property id.
		 */
		PersonId personId = new PersonId(56);
		PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, false, true);
		Optional<PersonId> optional = labelerSensitivity.getPersonId(personPropertyChangeObservationEvent);
		assertTrue(optional.isPresent());
		assertEquals(personId, optional.get());

		/*
		 * Show that the sensitivity will return an empty optional of person id
		 * from a PersonCompartmentChangeObservationEvent if the event does not
		 * match the person property id.
		 */

		personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, false, true);
		optional = labelerSensitivity.getPersonId(personPropertyChangeObservationEvent);
		assertFalse(optional.isPresent());

	}

	

	@Test
	@UnitTestMethod(name = "getLabel", args = { SimulationContext.class, PersonId.class })
	public void testGetLabel() {
		/*
		 * Have the agent show that the person property labeler produces a label
		 * for each person that is consistent with the function passed to the
		 * compartment labeler.
		 */
		PersonPropertiesActionSupport.testConsumer(10, 6445109933336671672L, (c) -> {
			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select a property to work with
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			/*
			 * Assign random values to the people so that we can get some
			 * variety in the labels
			 */
			List<PersonId> people = personDataManager.getPeople();
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

			PersonPropertyLabeler personPropertyLabeler = new PersonPropertyLabeler(personPropertyId, function);

			/*
			 * Apply the labeler to each person and compare it to the more
			 * direct use of the labeler's function
			 */
			for (PersonId personId : people) {

				// get the person's compartment and apply the function directly
				Boolean value = personPropertiesDataManager.getPersonPropertyValue(personId, personPropertyId);
				Object expectedLabel = function.apply(value);

				// get the label from the person id
				Object actualLabel = personPropertyLabeler.getLabel(c, personId);

				// show that the two labels are equal
				assertEquals(expectedLabel, actualLabel);				

			}

			// precondition tests

			// if the person does not exist
			ContractException contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getLabel(c, new PersonId(-1)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> personPropertyLabeler.getLabel(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name = "getDimension", args = {})
	public void testGetDimension() {
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			assertEquals(testPersonPropertyId, new PersonPropertyLabeler(testPersonPropertyId, (c) -> null).getDimension());
		}
	}

}
