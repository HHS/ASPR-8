package gov.hhs.aspr.ms.gcm.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_AndFilter {

	private final static FilterSensitivity<Type2Event> FILTER_SENSITIVITY_2 = new FilterSensitivity<>(Type2Event.class,
			(c, e) -> {
				if (e.getPersonId().getValue() % 2 == 0) {
					return Optional.of(e.getPersonId());
				}
				return Optional.empty();
			});

	private final static FilterSensitivity<Type3Event> FILTER_SENSITIVITY_3 = new FilterSensitivity<>(Type3Event.class,
			(c, e) -> {
				if (e.getPersonId().getValue() % 2 == 0) {
					return Optional.of(e.getPersonId());
				}
				return Optional.empty();
			});

	private final static FilterSensitivity<Type5Event> FILTER_SENSITIVITY_5 = new FilterSensitivity<>(Type5Event.class,
			(c, e) -> {
				if (e.getPersonId().getValue() % 2 == 0) {
					return Optional.of(e.getPersonId());
				}
				return Optional.empty();
			});

	private static abstract class Type0Event implements Event {
		private final PersonId personid;

		public PersonId getPersonId() {
			return personid;
		}

		public Type0Event(PersonId personid) {
			this.personid = personid;
		}
	}

	private final static class Type2Event extends Type0Event {

		public Type2Event(PersonId personid) {
			super(personid);
		}
	}

	private final static class Type3Event extends Type0Event {

		public Type3Event(PersonId personid) {
			super(personid);
		}
	}

	private final static class Type5Event extends Type0Event {

		public Type5Event(PersonId personid) {
			super(personid);
		}
	}

	@Test
	@UnitTestConstructor(target = AndFilter.class, args = { Filter.class, Filter.class })
	public void testAndFilter() {

		// precondition test: if either child filter is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			new AndFilter(new TrueFilter(), null);
		});
		assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			new AndFilter(null, new TrueFilter());
		});
		assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());

	}

	private AndFilter getRandomAndFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Filter a = new TestFilter(randomGenerator.nextInt());
		Filter b = new TestFilter(randomGenerator.nextInt());
		return new AndFilter(a, b);
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5725831217415880484L);

		// is never equal to null
		for (int i = 0; i < 30; i++) {
			assertFalse(getRandomAndFilter(randomGenerator.nextLong()).equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AndFilter f = getRandomAndFilter(seed);
			assertTrue(f.equals(f));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AndFilter f1 = getRandomAndFilter(seed);
			AndFilter f2 = getRandomAndFilter(seed);
			for (int j = 0; j < 10; j++) {
				assertTrue(f1.equals(f2));
				assertTrue(f2.equals(f1));
			}
		}

		AndFilter f1 = new AndFilter(new TestFilter(3), new TestFilter(5));
		AndFilter f2 = new AndFilter(new TestFilter(5), new TestFilter(3));
		AndFilter f3 = new AndFilter(new TestFilter(3), new TestFilter(4));

		// return true when arguments are equal in some order
		assertEquals(f1, f2);

		// returns false when filter inputs are different in any order
		assertNotEquals(f1, f3);
		assertNotEquals(f2, f3);

	}

	private final static class TestFilter extends Filter {
		private final int index;

		public TestFilter(int index) {
			this.index = index;
		}

		@Override
		public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
			return index % 2 == 0;
		}

		@Override
		public void validate(PartitionsContext partitionsContext) {
			if (index < 0) {
				throw new RuntimeException();
			}
		}

		@Override
		public Set<FilterSensitivity<?>> getFilterSensitivities() {
			Set<FilterSensitivity<?>> result = new LinkedHashSet<>();

			if (index % 2 == 0) {
				result.add(FILTER_SENSITIVITY_2);
			}
			if (index % 3 == 0) {
				result.add(FILTER_SENSITIVITY_3);
			}
			if (index % 5 == 0) {
				result.add(FILTER_SENSITIVITY_5);
			}

			return result;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + index;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof TestFilter)) {
				return false;
			}
			TestFilter other = (TestFilter) obj;
			if (index != other.index) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("TestFilter [index=");
			builder.append(index);
			builder.append("]");
			return builder.toString();
		}

	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9151412325951204846L);

		// equal objects have equal hash codes, note that argument order does not matter
		for (int i = 0; i < 30; i++) {
			int seed1 = randomGenerator.nextInt();
			int seed2 = randomGenerator.nextInt();
			AndFilter f12 = new AndFilter(new TestFilter(seed1), new TestFilter(seed2));
			AndFilter f21 = new AndFilter(new TestFilter(seed2), new TestFilter(seed1));
			assertEquals(f12, f21);
			assertEquals(f12.hashCode(), f21.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();

		for (int i = 0; i < 100; i++) {
			TestFilter f1 = new TestFilter(randomGenerator.nextInt());
			TestFilter f2 = new TestFilter(randomGenerator.nextInt());
			AndFilter andFilter = new AndFilter(f1, f2);
			hashCodes.add(andFilter.hashCode());
		}

		assertTrue(hashCodes.size() > 95);

	}

	private AndFilter getAndFilter(int a, int b) {
		return new AndFilter(new TestFilter(a), new TestFilter(b));
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsContext partitionsContext = null;
		PersonId personId = new PersonId(56);

		// using AND over TestFilters, evaluate should return true if and only if both
		// arguments are even
		assertFalse(getAndFilter(20, 5).evaluate(partitionsContext, personId));
		assertTrue(getAndFilter(20, 10).evaluate(partitionsContext, personId));
		assertFalse(getAndFilter(3, 2).evaluate(partitionsContext, personId));
		assertFalse(getAndFilter(3, 5).evaluate(partitionsContext, personId));
		assertTrue(getAndFilter(0, 4).evaluate(partitionsContext, personId));
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		// Filter sensitivities for the TestFilter class are based on index modulo 2, 3
		// and 5

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(i);
			for (int j = 0; j < 30; j++) {
				TestFilter testFilter2 = new TestFilter(j);
				AndFilter andFilter = new AndFilter(testFilter1, testFilter2);
				Set<FilterSensitivity<?>> actualFilterSensitivities = andFilter.getFilterSensitivities();

				Set<FilterSensitivity<?>> expectedFilterSensitivities = new LinkedHashSet<>();
				if (i % 2 == 0 || j % 2 == 0) {
					expectedFilterSensitivities.add(FILTER_SENSITIVITY_2);
				}
				if (i % 3 == 0 || j % 3 == 0) {
					expectedFilterSensitivities.add(FILTER_SENSITIVITY_3);
				}
				if (i % 5 == 0 || j % 5 == 0) {
					expectedFilterSensitivities.add(FILTER_SENSITIVITY_5);
				}
				assertEquals(expectedFilterSensitivities, actualFilterSensitivities);
			}
		}
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getFirstFilter", args = {})
	public void testGetFirstFilter() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7492542318245868773L);

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(randomGenerator.nextInt());
			TestFilter testFilter2 = new TestFilter(randomGenerator.nextInt());

			AndFilter andFilter = new AndFilter(testFilter1, testFilter2);
			assertEquals(testFilter1, andFilter.getFirstFilter());
		}
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getSecondFilter", args = {})
	public void testGetSecondFilter() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(710120811220020778L);

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(randomGenerator.nextInt());
			TestFilter testFilter2 = new TestFilter(randomGenerator.nextInt());

			AndFilter andFilter = new AndFilter(testFilter1, testFilter2);
			assertEquals(testFilter2, andFilter.getSecondFilter());
		}

	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "toString", args = {})
	public void testToString() {
		TestFilter testFilter1 = new TestFilter(32);
		TestFilter testFilter2 = new TestFilter(17);
		AndFilter andFilter = new AndFilter(testFilter1, testFilter2);
		String actualValue = andFilter.toString();
		String expectedValue = "AndFilter [a=TestFilter [index=32], b=TestFilter [index=17]]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		PartitionsContext partitionsContext = null;

		/*
		 * TestFilters throw an exception when validate is invoked when their index is
		 * negative. We show here that the AndFilter is invoking the validate for both
		 * of its child filters.
		 * 
		 */

		assertDoesNotThrow(() -> {
			AndFilter andFilter = new AndFilter(new TestFilter(0), new TestFilter(3));
			andFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			AndFilter andFilter = new AndFilter(new TestFilter(-1), new TestFilter(3));
			andFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			AndFilter andFilter = new AndFilter(new TestFilter(0), new TestFilter(-1));
			andFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			AndFilter andFilter = new AndFilter(new TestFilter(-1), new TestFilter(-1));
			andFilter.validate(partitionsContext);
		});

	}

}
