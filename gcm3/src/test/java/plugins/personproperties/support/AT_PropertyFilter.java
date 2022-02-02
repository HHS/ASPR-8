package plugins.personproperties.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.testsupport.PersonPropertiesActionSupport;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.support.PropertyError;
import plugins.stochastics.StochasticsDataView;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test unit for {@link PropertyFilter}.
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PropertyFilter.class)
public class AT_PropertyFilter {

	

	/**
	 * Tests
	 * {@link PropertyFilter#PropertyFilter(Context, PersonPropertyId, Equality, Object)}
	 */
	@Test
	@UnitTestConstructor(args = { Context.class, PersonPropertyId.class, Equality.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	/**
	 * Tests
	 * {@link PropertyFilter#PropertyFilter(Context, PersonPropertyId, Equality, Object)}
	 */
	@Test
	@UnitTestMethod(name = "validate", args = {})
	public void testValidate() {
		
		PersonPropertiesActionSupport.testConsumer(100, 7889475921077680704L, (c)->{
			final Filter filter = new PropertyFilter(TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK, Equality.EQUAL, 12);
			assertNotNull(filter);
			
			

			ContractException contractException = assertThrows(ContractException.class, () -> new PropertyFilter(TestPersonPropertyId.getUnknownPersonPropertyId(), Equality.EQUAL, 12).validate(c));
			assertEquals(PersonPropertyError.UNKNOWN_PERSON_PROPERTY_ID, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new PropertyFilter(null, Equality.EQUAL, 12L).validate(c));
			assertEquals(PersonPropertyError.NULL_PERSON_PROPERTY_ID, contractException.getErrorType());

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
		assertEquals(PersonPropertyChangeObservationEvent.class, filterSensitivity.getEventClass());
	}

	/**
	 * Tests {@link PropertyFilter#evaluate(Context, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {
		
		PersonPropertiesActionSupport.testConsumer(100, 9037413907425227057L, (c)->{
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonPropertyDataView personPropertyDataView = c.getDataView(PersonPropertyDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			TestPersonPropertyId testPersonPropertyId = TestPersonPropertyId.PERSON_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
			
			Filter filter = new PropertyFilter(testPersonPropertyId, Equality.GREATER_THAN, 12);

			for (PersonId personId : personDataView.getPeople()) {
				int value = randomGenerator.nextInt(10) + 7;
				c.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, testPersonPropertyId, value));;
			}

			for (PersonId personId : personDataView.getPeople()) {
				Integer value = personPropertyDataView.getPersonPropertyValue(personId, testPersonPropertyId);
				boolean expected = value > 12;
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			ContractException contractException = assertThrows(ContractException.class, () -> filter.evaluate(null, new PersonId(0)));
			assertEquals(NucleusError.NULL_CONTEXT,contractException.getErrorType());

			/* precondition: if the person id is null */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, null));
			assertEquals(PersonError.NULL_PERSON_ID,contractException.getErrorType());

			/* precondition: if the person id is unknown */
			contractException = assertThrows(ContractException.class, () -> filter.evaluate(c, new PersonId(123412342)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID,contractException.getErrorType());
		});
	}
}
