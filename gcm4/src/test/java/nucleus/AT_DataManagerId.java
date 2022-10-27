package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = DataManagerId.class)
public final class AT_DataManagerId {

	@UnitTestMethod(name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new DataManagerId(i).getValue());
		}
	}

	@UnitTestMethod(name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("DataManagerId [id=" + i + "]", new DataManagerId(i).toString());
		}
	}

	@UnitTestMethod(name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		// show equal objects have equal hashcodes
		for (int i = 0; i < 10; i++) {
			DataManagerId a = new DataManagerId(i);
			DataManagerId b = new DataManagerId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}

		// show that hash codes are dispersed
		Set<Integer> hashcodes = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			hashcodes.add(new DataManagerId(i).hashCode());
		}
		assertEquals(1000, hashcodes.size());

	}

	@UnitTestMethod(name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		// show data manager ids are equal if and only if they have the same
		// base int
		// value
		for (int i = 0; i < 10; i++) {
			DataManagerId a = new DataManagerId(i);
			for (int j = 0; j < 10; j++) {
				DataManagerId b = new DataManagerId(j);
				if (i == j) {
					assertEquals(a, b);
				} else {
					assertNotEquals(a, b);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "compareTo", args = { DataManagerId.class })
	public void testCompareTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8041307094727012939L);
		for (int i = 0; i < 100; i++) {
			int a = randomGenerator.nextInt();
			int b = randomGenerator.nextInt();

			int expectedComparison = Integer.compare(a, b);
			int actualComparison = new DataManagerId(a).compareTo(new DataManagerId(b));
			assertEquals(expectedComparison, actualComparison);
		}

	}

	@Test
	@UnitTestConstructor( args = { int.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7051188045588654915L);
		for (int i = 0; i < 100; i++) {
			int expectedIdValue = randomGenerator.nextInt();
			DataManagerId dataManagerId = new DataManagerId(expectedIdValue);
			int actualIdValue = dataManagerId.getValue();
			assertEquals(expectedIdValue, actualIdValue);
		}
	}

}
