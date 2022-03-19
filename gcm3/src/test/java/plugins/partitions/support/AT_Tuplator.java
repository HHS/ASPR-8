package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import plugins.partitions.support.Tuplator.Builder;
import util.RandomGeneratorProvider;

/**
 * Test class for {@link Tuplator}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Tuplator.class)
public class AT_Tuplator {
	

	/**
	 * Tests {@link Tuplator#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7820715406750309229L);
		for (int i = 0; i < 100; i++) {
			Builder builder = Tuplator.builder();
			int dimensionCount = randomGenerator.nextInt(4) + 1;
			int expectedSize = 1;
			for (int j = 0; j < dimensionCount; j++) {
				int dimSize = randomGenerator.nextInt(10) + 1;
				expectedSize *= dimSize;
				builder.addDimension(dimSize);
			}
			int actualSize = builder.build().size();
			assertEquals(expectedSize, actualSize);
		}
	}

	/**
	 * Tests {@link Tuplator#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		// covered by other tests
	}

	/**
	 * Tests {@link Tuplator#dimensions()}
	 */
	@Test
	@UnitTestMethod(name = "dimensions", args = {})
	public void testDimensions() {
		
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7661626069466374878L);
		for (int i = 0; i < 100; i++) {
			Builder builder = Tuplator.builder();
			int dimensionCount = randomGenerator.nextInt(4) + 1;
			for (int j = 0; j < dimensionCount; j++) {
				int dimSize = randomGenerator.nextInt(10) + 1;
				builder.addDimension(dimSize);
			}
			int actualDimensionCount = builder.build().dimensions();
			assertEquals(dimensionCount, actualDimensionCount);
		}
	}

	/**
	 * Tests {@link Tuplator#fillTuple(int, int[])}
	 */
	@Test
	@UnitTestMethod(name = "fillTuple", args = { int.class, int[].class })
	public void testFillTuple() {

		Tuplator tuplator = Tuplator.builder().addDimension(2).addDimension(3).addDimension(5).build();

		int[] tuple = new int[tuplator.dimensions()];

		List<int[]> expectedArrays = new ArrayList<>();

		expectedArrays.add(new int[] { 0, 0, 0 });
		expectedArrays.add(new int[] { 1, 0, 0 });
		expectedArrays.add(new int[] { 0, 1, 0 });
		expectedArrays.add(new int[] { 1, 1, 0 });
		expectedArrays.add(new int[] { 0, 2, 0 });
		expectedArrays.add(new int[] { 1, 2, 0 });
		expectedArrays.add(new int[] { 0, 0, 1 });
		expectedArrays.add(new int[] { 1, 0, 1 });
		expectedArrays.add(new int[] { 0, 1, 1 });
		expectedArrays.add(new int[] { 1, 1, 1 });
		expectedArrays.add(new int[] { 0, 2, 1 });
		expectedArrays.add(new int[] { 1, 2, 1 });
		expectedArrays.add(new int[] { 0, 0, 2 });
		expectedArrays.add(new int[] { 1, 0, 2 });
		expectedArrays.add(new int[] { 0, 1, 2 });
		expectedArrays.add(new int[] { 1, 1, 2 });
		expectedArrays.add(new int[] { 0, 2, 2 });
		expectedArrays.add(new int[] { 1, 2, 2 });
		expectedArrays.add(new int[] { 0, 0, 3 });
		expectedArrays.add(new int[] { 1, 0, 3 });
		expectedArrays.add(new int[] { 0, 1, 3 });
		expectedArrays.add(new int[] { 1, 1, 3 });
		expectedArrays.add(new int[] { 0, 2, 3 });
		expectedArrays.add(new int[] { 1, 2, 3 });
		expectedArrays.add(new int[] { 0, 0, 4 });
		expectedArrays.add(new int[] { 1, 0, 4 });
		expectedArrays.add(new int[] { 0, 1, 4 });
		expectedArrays.add(new int[] { 1, 1, 4 });
		expectedArrays.add(new int[] { 0, 2, 4 });
		expectedArrays.add(new int[] { 1, 2, 4 });

		for (int i = 0; i < tuplator.size(); i++) {
			tuplator.fillTuple(i, tuple);
			assertTrue(Arrays.equals(expectedArrays.get(i), tuple));
		}
		/**
		 * precondition tests
		 */
		assertThrows(IndexOutOfBoundsException.class, ()->tuplator.fillTuple(-2, tuple));
		assertThrows(IndexOutOfBoundsException.class, ()->tuplator.fillTuple(-1, tuple));
		assertThrows(IndexOutOfBoundsException.class, ()->tuplator.fillTuple(tuplator.size(), tuple));
		assertThrows(IndexOutOfBoundsException.class, ()->tuplator.fillTuple(tuplator.size()+1, tuple));
		assertThrows(IllegalArgumentException.class, ()->tuplator.fillTuple(0, null));
		assertThrows(IllegalArgumentException.class, ()->tuplator.fillTuple(0, new int[tuplator.dimensions()-1]));
		assertThrows(IllegalArgumentException.class, ()->tuplator.fillTuple(0, new int[tuplator.dimensions()+1]));
		

	}

}
