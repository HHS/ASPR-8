package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.FilterSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_OrFilter {

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
	@UnitTestConstructor(target = OrFilter.class, args = { Filter.class, Filter.class })
	public void testOrFilter() {

		// precondition test: if either child filter is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			new OrFilter(new TrueFilter(), null);
		});
		assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());

		contractException = assertThrows(ContractException.class, () -> {
			new OrFilter(null, new TrueFilter());
		});
		assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());

	}

	private OrFilter getRandomOrFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Filter a = new TestFilter(randomGenerator.nextInt());
		Filter b = new TestFilter(randomGenerator.nextInt());
		return new OrFilter(a, b);
	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5725831217415880484L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			OrFilter orFilter = getRandomOrFilter(randomGenerator.nextLong());
			assertFalse(orFilter.equals(new Object()));
		}

		// is never equal to null
		for (int i = 0; i < 30; i++) {
			assertFalse(getRandomOrFilter(randomGenerator.nextLong()).equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			OrFilter f = getRandomOrFilter(seed);
			assertTrue(f.equals(f));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			OrFilter f1 = getRandomOrFilter(seed);
			OrFilter f2 = getRandomOrFilter(seed);
			assertFalse(f1 == f2);
			for (int j = 0; j < 10; j++) {
				assertTrue(f1.equals(f2));
				assertTrue(f2.equals(f1));
			}
		}

		// different inputs yield unequal orFilters
		Set<OrFilter> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			OrFilter orFilter = getRandomOrFilter(randomGenerator.nextLong());
			set.add(orFilter);
		}
		assertEquals(100, set.size());

		// The order in which inputs are added does not matter
		OrFilter f1 = new OrFilter(new TestFilter(3), new TestFilter(5));
		OrFilter f2 = new OrFilter(new TestFilter(5), new TestFilter(3));
		assertTrue(f1.equals(f2));
		assertTrue(f2.equals(f1));
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
			return Objects.hash(index);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			TestFilter other = (TestFilter) obj;
			return index == other.index;
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
	@UnitTestMethod(target = OrFilter.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9151412325951204846L);

		// equal objects have equal hash codes, note that argument order does not matter
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			OrFilter f1 = getRandomOrFilter(seed);
			OrFilter f2 = getRandomOrFilter(seed);
			assertEquals(f1, f2);
			assertEquals(f1.hashCode(), f2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();

		for (int i = 0; i < 100; i++) {
			OrFilter orFilter = getRandomOrFilter(randomGenerator.nextLong());
			hashCodes.add(orFilter.hashCode());
		}

		assertEquals(100, hashCodes.size());

		// The order in which inputs are added does not matter
		OrFilter f1 = new OrFilter(new TestFilter(3), new TestFilter(5));
		OrFilter f2 = new OrFilter(new TestFilter(5), new TestFilter(3));
		assertEquals(f1, f2);
		assertEquals(f1.hashCode(), f2.hashCode());
	}

	private OrFilter getAndFilter(int a, int b) {
		return new OrFilter(new TestFilter(a), new TestFilter(b));
	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsContext partitionsContext = null;
		PersonId personId = new PersonId(56);

		// using OR over TestFilters, evaluate should return true if either
		// argument is even
		assertTrue(getAndFilter(20, 5).evaluate(partitionsContext, personId));
		assertTrue(getAndFilter(20, 10).evaluate(partitionsContext, personId));
		assertTrue(getAndFilter(3, 2).evaluate(partitionsContext, personId));
		assertFalse(getAndFilter(3, 5).evaluate(partitionsContext, personId));
		assertFalse(getAndFilter(7, 11).evaluate(partitionsContext, personId));
	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		// Filter sensitivities for the TestFilter class are based on index modulo 2, 3
		// and 5

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(i);
			for (int j = 0; j < 30; j++) {
				TestFilter testFilter2 = new TestFilter(j);
				OrFilter orFilter = new OrFilter(testFilter1, testFilter2);
				Set<FilterSensitivity<?>> actualFilterSensitivities = orFilter.getFilterSensitivities();

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
	@UnitTestMethod(target = OrFilter.class, name = "getFirstFilter", args = {})
	public void testGetFirstFilter() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7492542318245868773L);

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(randomGenerator.nextInt());
			TestFilter testFilter2 = new TestFilter(randomGenerator.nextInt());

			OrFilter orFilter = new OrFilter(testFilter1, testFilter2);
			assertEquals(testFilter1, orFilter.getFirstFilter());
		}
	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "getSecondFilter", args = {})
	public void testGetSecondFilter() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(710120811220020778L);

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter1 = new TestFilter(randomGenerator.nextInt());
			TestFilter testFilter2 = new TestFilter(randomGenerator.nextInt());

			OrFilter orFilter = new OrFilter(testFilter1, testFilter2);
			assertEquals(testFilter2, orFilter.getSecondFilter());
		}

	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "toString", args = {})
	public void testToString() {
		TestFilter testFilter1 = new TestFilter(32);
		TestFilter testFilter2 = new TestFilter(17);
		OrFilter orFilter = new OrFilter(testFilter1, testFilter2);
		String actualValue = orFilter.toString();
		String expectedValue = "OrFilter [a=TestFilter [index=32], b=TestFilter [index=17]]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = OrFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		PartitionsContext partitionsContext = null;

		/*
		 * TestFilters throw an exception when validate is invoked when their index is
		 * negative. We show here that the OrFilter is invoking the validate for both
		 * of its child filters.
		 * 
		 */

		assertDoesNotThrow(() -> {
			OrFilter orFilter = new OrFilter(new TestFilter(0), new TestFilter(3));
			orFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			OrFilter orFilter = new OrFilter(new TestFilter(-1), new TestFilter(3));
			orFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			OrFilter orFilter = new OrFilter(new TestFilter(0), new TestFilter(-1));
			orFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			OrFilter orFilter = new OrFilter(new TestFilter(-1), new TestFilter(-1));
			orFilter.validate(partitionsContext);
		});

	}

}
