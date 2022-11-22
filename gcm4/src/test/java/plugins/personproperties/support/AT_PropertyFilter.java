package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.datamanagers.PersonPropertiesDataManager;
import plugins.personproperties.events.PersonPropertyUpdateEvent;
import plugins.personproperties.testsupport.PersonPropertiesActionSupport;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.stochastics.StochasticsDataManager;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

/**
 * Test unit for {@link PropertyFilter}.
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PropertyFilter.class)
public class AT_PropertyFilter {

	

	@Test
	@UnitTestConstructor(args = { PersonPropertyId.class, Equality.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "validate", args = {SimulationContext.class})
	public void testValidate() {
		
		PersonPropertiesActionSupport.testConsumer(100, 7889475921077680704L, (c)->{
			final Filter filter = new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, Equality.EQUAL, 12);
			assertNotNull(filter);
			
			

			ContractException contractException = assertThrows(ContractException.class, () -> new PropertyFilter(TestPersonPropertyId.getUnknownPersonPropertyId(), Equality.EQUAL, 12).validate(c));
			assertEquals(PropertyError.UNKNOWN_PROPERTY_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new PropertyFilter(null, Equality.EQUAL, 12L).validate(c));
			assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, null, 12).validate(c));
			assertEquals(PartitionError.NULL_EQUALITY_OPERATOR, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, Equality.EQUAL, "bad value").validate(c));
			assertEquals(PropertyError.INCOMPATIBLE_VALUE, contractException.getErrorType());

		});

		
	}

	/**
	 * Tests {@link PropertyFilter#getFilterSensitivities()}
	 */
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		Filter filter = new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, Equality.EQUAL, 12);

		Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
		assertNotNull(filterSensitivities);
		assertEquals(filterSensitivities.size(), 1);

		FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
		assertEquals(PersonPropertyUpdateEvent.class, filterSensitivity.getEventClass());
	}

	
	@Test
	@UnitTestMethod(name = "evaluate", args = { SimulationContext.class, PersonId.class })
	public void testEvaluate() {
		
		PersonPropertiesActionSupport.testConsumer(100, 9037413907425227057L, (c)->{
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonPropertiesDataManager personPropertiesDataManager = c.getDataManager(PersonPropertiesDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			
			Filter filter = new PropertyFilter(testPersonPropertyId, Equality.GREATER_THAN, 12);

			for (PersonId personId : peopleDataManager.getPeople()) {
				int value = randomGenerator.nextInt(10) + 7;
				personPropertiesDataManager.setPersonPropertyValue(personId, testPersonPropertyId, value);				
			}

			for (PersonId personId : peopleDataManager.getPeople()) {
				Integer value = personPropertiesDataManager.getPersonPropertyValue(personId, testPersonPropertyId);
				boolean expected = value > 12;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(null, new PersonId(0)));
			assertEquals(NucleusError.NULL_SIMULATION_CONTEXT,contractException.getErrorType());

			/* precondition: if the person id is null */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, null));
			assertEquals(PersonError.NULL_PERSON_ID,contractException.getErrorType());

			/* precondition: if the person id is unknown */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, new PersonId(123412342)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID,contractException.getErrorType());
		});
	}

	@Test
	@UnitTestMethod(name="toString", args={})
	public void testToString() {
		Filter filter = new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, Equality.GREATER_THAN, 12);
		String expectedString = "PropertyFilter [personPropertyId=PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, personPropertyValue=12, equality=GREATER_THAN]";

		assertEquals(expectedString, filter.toString());

	}
}
