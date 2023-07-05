package plugins.partitions.support.filters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.partitions.support.FilterSensitivity;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionsContext;
import plugins.people.support.PersonId;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_AndFilter {

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
		Filter a;
		Filter b;
		if (randomGenerator.nextBoolean()) {
			a = new TrueFilter();
		} else {
			a = new FalseFilter();
		}
		if (randomGenerator.nextBoolean()) {
			b = new TrueFilter();
		} else {
			b = new FalseFilter();
		}

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
		

		 
		AndFilter tt = new AndFilter(new TrueFilter(), new TrueFilter());
		AndFilter tf = new AndFilter(new TrueFilter(), new FalseFilter());
		AndFilter ft = new AndFilter(new FalseFilter(), new TrueFilter());
		AndFilter ff = new AndFilter(new FalseFilter(), new FalseFilter());

		//return true when arguments are equal in some order
		assertEquals(tf, ft);
		
		// returns false when filter inputs are different in any order
		assertNotEquals(tt, tf);
		assertNotEquals(tt, ft);
		assertNotEquals(tt, ff);		
		assertNotEquals(tf, ff);
		assertNotEquals(ft, ff);
		
		

	}

	private static class TestFilter extends Filter {
		private final int hashCode;

		public TestFilter(int hashCode) {
			this.hashCode = hashCode;
		}

		@Override
		public boolean evaluate(PartitionsContext partitionsContext, PersonId personId) {
			return false;
		}

		@Override
		public void validate(PartitionsContext partitionsContext) {
			// do nothing
		}

		@Override
		public Set<FilterSensitivity<?>> getFilterSensitivities() {
			return new LinkedHashSet<>();
		}

		@Override
		public int hashCode() {

			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof TestFilter)) {
				return false;
			}
			TestFilter other = (TestFilter) obj;
			return hashCode == other.hashCode;
		}

		@Override
		public String toString() {
			return "TestFilter[" + hashCode + "]";
		}

	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9151412325951204846L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			AndFilter f1 = getRandomAndFilter(seed);
			AndFilter f2 = getRandomAndFilter(seed);
			assertEquals(f1, f2);
			assertEquals(f1.hashCode(), f2.hashCode());
		}

		AndFilter tf = new AndFilter(new TrueFilter(), new FalseFilter());
		AndFilter ft = new AndFilter(new FalseFilter(), new TrueFilter());


		//argument order does not matter 
		assertEquals(tf, ft);
		assertEquals(tf.hashCode(), ft.hashCode());

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();

		for (int i = 0; i < 100; i++) {
			TestFilter f = new TestFilter(randomGenerator.nextInt());
			hashCodes.add(f.hashCode());
		}
		
		assertTrue(hashCodes.size()>95);

	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "evaluate", args = { PartitionsContext.class, PersonId.class })
	public void testEvaluate() {
		//fail();
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		//fail();
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getFirstFilter", args = {})
	public void testGetFirstFilter() {
		//fail();
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "getSecondFilter", args = {})
	public void testGetSecondFilter() {
		//fail();
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "toString", args = {})
	public void testToString() {
		//fail();
	}

	@Test
	@UnitTestMethod(target = AndFilter.class, name = "validate", args = { PartitionsContext.class })
	public void testValidate() {
		//fail();
	}

}
