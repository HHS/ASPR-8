package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
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

public class AT_NotFilter {

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
	@UnitTestConstructor(target = NotFilter.class, args = { Filter.class})
	public void testNotFilter() {

		// precondition test: if the child filter is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			new NotFilter(null);
		});
		assertEquals(PartitionError.NULL_FILTER, contractException.getErrorType());
	}

	private NotFilter getRandomNotFilter(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		Filter a = new TestFilter(randomGenerator.nextInt());
		return new NotFilter(a);
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5725831217415880484L);

		// is never equal to null
		for (int i = 0; i < 30; i++) {
			assertFalse(getRandomNotFilter(randomGenerator.nextLong()).equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			NotFilter f = getRandomNotFilter(seed);
			assertTrue(f.equals(f));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			NotFilter f1 = getRandomNotFilter(seed);
			NotFilter f2 = getRandomNotFilter(seed);
			for (int j = 0; j < 10; j++) {
				assertTrue(f1.equals(f2));
				assertTrue(f2.equals(f1));
			}
		}

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
	@UnitTestMethod(target = NotFilter.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9151412325951204846L);

		// equal objects have equal hash codes, note that argument order does not matter
		for (int i = 0; i < 30; i++) {
			int seed = randomGenerator.nextInt();
			NotFilter f1 = new NotFilter(new TestFilter(seed));
			NotFilter f2 = new NotFilter(new TestFilter(seed));
			assertEquals(f1, f2);
			assertEquals(f1.hashCode(), f2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();

		for (int i = 0; i < 100; i++) {
			TestFilter f = new TestFilter(randomGenerator.nextInt());
			NotFilter notFilter = new NotFilter(f);
			hashCodes.add(notFilter.hashCode());
		}

		assertTrue(hashCodes.size() > 95);

	}

	private NotFilter getNotFilter(int a) {
		return new NotFilter(new TestFilter(a));
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		PartitionsContext partitionsContext = null;
		PersonId personId = new PersonId(56);

		// using NOT over TestFilters, evaluate should return the opposite of the test
		// filter
		assertFalse(getNotFilter(20).evaluate(partitionsContext, personId));
		assertTrue(getNotFilter(17).evaluate(partitionsContext, personId));
		assertTrue(getNotFilter(3).evaluate(partitionsContext, personId));
		assertFalse(getNotFilter(8).evaluate(partitionsContext, personId));
		assertFalse(getNotFilter(0).evaluate(partitionsContext, personId));
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		// Filter sensitivities for the TestFilter class are based on index modulo 2, 3
		// and 5

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter = new TestFilter(i);
			NotFilter notFilter = new NotFilter(testFilter);
			Set<FilterSensitivity<?>> actualFilterSensitivities = notFilter.getFilterSensitivities();

			Set<FilterSensitivity<?>> expectedFilterSensitivities = new LinkedHashSet<>();
			if (i % 2 == 0) {
				expectedFilterSensitivities.add(FILTER_SENSITIVITY_2);
			}
			if (i % 3 == 0) {
				expectedFilterSensitivities.add(FILTER_SENSITIVITY_3);
			}
			if (i % 5 == 0) {
				expectedFilterSensitivities.add(FILTER_SENSITIVITY_5);
			}
			assertEquals(expectedFilterSensitivities, actualFilterSensitivities);

		}
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "getSubFilter", args = {})
	public void testGetSubFilter() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7492542318245868773L);

		for (int i = 0; i < 30; i++) {
			TestFilter testFilter = new TestFilter(randomGenerator.nextInt());
			NotFilter notFilter = new NotFilter(testFilter);
			assertEquals(testFilter, notFilter.getSubFilter());
		}
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "toString", args = {})
	public void testToString() {
		TestFilter testFilter = new TestFilter(32);
		NotFilter notFilter = new NotFilter(testFilter);
		String actualValue = notFilter.toString();
		String expectedValue = "NotFilter [a=TestFilter [index=32]]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(target = NotFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		PartitionsContext partitionsContext = null;

		/*
		 * TestFilters throw an exception when validate is invoked when their index is
		 * negative. We show here that the NotFilter is invoking the validate for its
		 * child filter.
		 * 
		 */

		assertDoesNotThrow(() -> {
			NotFilter notFilter = new NotFilter(new TestFilter(0));
			notFilter.validate(partitionsContext);
		});

		assertThrows(RuntimeException.class, () -> {
			NotFilter notFilter = new NotFilter(new TestFilter(-1));
			notFilter.validate(partitionsContext);
		});

	}

}
