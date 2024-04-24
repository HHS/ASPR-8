package gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.PersonPropertiesTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.personproperties.testsupport.TestPersonPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_FunctionalPersonPropertyLabeler {

	@Test
	@UnitTestConstructor(target = FunctionalPersonPropertyLabeler.class, args = { PersonPropertyId.class,
			Function.class })
	public void testFunctionalPersonPropertyLabeler() {

		// The test needs to demonstrate that the function passed into the constructor
		// is actually used as the labeling function. The full set of methods do not
		// need to be tested.

		/*
		 * Have the agent show that the person property labeler produces a label for
		 * each person that is consistent with the function passed to the person
		 * property labeler.
		 */
		Factory factory = PersonPropertiesTestPluginFactory.factory(10, 6445109933336671672L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c
					.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select a property to work with
			PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

			/*
			 * Assign random values to the people so that we can get some variety in the
			 * labels
			 */
			List<PersonId> people = peopleDataManager.getPeople();
			for (PersonId personId : people) {
				personPropertiesDataManager.setPersonPropertyValue(personId, personPropertyId,
						randomGenerator.nextBoolean());
			}

			/*
			 * build a person property labeler with a function that can be tested
			 */
			Function<Object, Object> function = (input) -> {
				Boolean value = (Boolean) input;
				if (value) {
					return "A";
				}
				return "B";
			};

			PersonPropertyLabeler personPropertyLabeler = new FunctionalPersonPropertyLabeler(personPropertyId,
					function);

			/*
			 * Apply the labeler to each person and compare it to the more direct use of the
			 * labeler's function
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
			ContractException contractException = assertThrows(ContractException.class,
					() -> personPropertyLabeler.getCurrentLabel(testPartitionsContext, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class,
					() -> personPropertyLabeler.getCurrentLabel(testPartitionsContext, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());
		});

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
