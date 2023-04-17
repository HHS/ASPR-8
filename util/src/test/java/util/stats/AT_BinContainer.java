package util.stats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.stats.BinContainer.Bin;
import util.stats.BinContainer.Builder;

public class AT_BinContainer {
	/**
	 * Tests {@link BinContainer.Bin#Bin(double, double, int)} construction
	 */
	@Test
	@UnitTestConstructor(target = BinContainer.Bin.class, args = { double.class, double.class, int.class })
	public void testBinConstructor() {
		double lowerBound = 12.5;
		double upperBound = 15.123;
		int count = 55;
		Bin bin = new Bin(lowerBound, upperBound, count);
		assertEquals(lowerBound, bin.getLowerBound(), 0);
		assertEquals(upperBound, bin.getUpperBound(), 0);
		assertEquals(count, bin.getCount());

		// precondition check: if the lower bound exceeds the upper bound

		assertThrows(IllegalArgumentException.class, () -> new Bin(33, 31, 4));
		// precondition check: if the count is negative
		assertThrows(IllegalArgumentException.class, () -> new Bin(30, 40, -1));

	}

	/**
	 * Tests {@link BinContainer#builder(double)} construction
	 */
	@Test
	@UnitTestMethod(target = BinContainer.class, name = "builder", args = { double.class })
	public void testBuilder() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		List<Bin> expected = new ArrayList<>();
		expected.add(new Bin(-12.0, -9.0, 3));
		expected.add(new Bin(-9.0, -6.0, 0));
		expected.add(new Bin(-6.0, -3.0, 0));
		expected.add(new Bin(-3.0, 0.0, 0));
		expected.add(new Bin(0.0, 3.0, 5));
		expected.add(new Bin(3.0, 6.0, 2));
		expected.add(new Bin(6.0, 9.0, 0));
		expected.add(new Bin(9.0, 12.0, 1));

		List<Bin> actual = new ArrayList<>();
		for (int i = 0; i < binContainer.binCount(); i++) {
			actual.add(binContainer.getBin(i));
		}

		assertEquals(expected, actual);
		assertThrows(RuntimeException.class, () -> BinContainer.builder(0));

		assertThrows(RuntimeException.class, () -> BinContainer.builder(-0.5));
		assertThrows(RuntimeException.class, () -> {
			Builder builder2 = BinContainer.builder(5);
			builder2.addValue(13, -1);
		});
	}

	@Test
	@UnitTestMethod(target = BinContainer.Builder.class, name = "addValue", args = { double.class, int.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddValue() {
		// covered by testBuilder()
	}

	@Test
	@UnitTestMethod(target = BinContainer.Builder.class, name = "build", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testBuild() {
		// covered by testBuilder()
	}

	/**
	 * Tests {@link BinContainer#binCount()}
	 */
	@Test
	@UnitTestMethod(target = BinContainer.class, name = "binCount", args = {})
	public void testBinCount() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		assertEquals(8, binContainer.binCount());
	}

	/**
	 * Tests {@link BinContainer#getBin(int)}
	 */
	@Test
	@UnitTestMethod(target = BinContainer.class, name = "getBin", args = { int.class })
	public void testGetBin() {
		BinContainer.Builder builder = BinContainer.builder(3);
		builder.addValue(2.3, 5);
		builder.addValue(3, 2);
		builder.addValue(10, 1);
		builder.addValue(11, 0);
		builder.addValue(-10, 3);
		BinContainer binContainer = builder.build();

		assertEquals(new Bin(-12.0, -9.0, 3), binContainer.getBin(0));
		assertEquals(new Bin(-9.0, -6.0, 0), binContainer.getBin(1));
		assertEquals(new Bin(-6.0, -3.0, 0), binContainer.getBin(2));
		assertEquals(new Bin(-3.0, 0.0, 0), binContainer.getBin(3));
		assertEquals(new Bin(0.0, 3.0, 5), binContainer.getBin(4));
		assertEquals(new Bin(3.0, 6.0, 2), binContainer.getBin(5));
		assertEquals(new Bin(6.0, 9.0, 0), binContainer.getBin(6));
		assertEquals(new Bin(9.0, 12.0, 1), binContainer.getBin(7));

		// precondition tests
		assertThrows(RuntimeException.class, () -> binContainer.getBin(-1));
		assertThrows(RuntimeException.class, () -> binContainer.getBin(1000000));

	}

	/**
	 * Tests {@link BinContainer.Bin#toString()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "toString", args = {})
	public void testBinToString() {
		BinContainer binContainer = BinContainer.builder(3)//
												.addValue(2.1, 5)//
												.addValue(5.6, 1)//
												.addValue(12.67, 3)//
												.build();//

		List<String> expectedStrings = new ArrayList<>();
		expectedStrings.add("Bin [lowerBound=0.0, upperBound=3.0, count=5]");
		expectedStrings.add("Bin [lowerBound=3.0, upperBound=6.0, count=1]");
		expectedStrings.add("Bin [lowerBound=6.0, upperBound=9.0, count=0]");
		expectedStrings.add("Bin [lowerBound=9.0, upperBound=12.0, count=0]");
		expectedStrings.add("Bin [lowerBound=12.0, upperBound=15.0, count=3]");

		List<String> actualStrings = new ArrayList<>();
		for (int i = 0; i < binContainer.binCount(); i++) {
			actualStrings.add(binContainer.getBin(i).toString());
		}
		assertEquals(expectedStrings, actualStrings);
	}

	/**
	 * Tests {@link BinContainer.Bin#hashCode()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "hashCode", args = {})
	public void testBinHashCode() {
		BinContainer binContainer1 = BinContainer	.builder(3).addValue(2.1, 5)//
													.addValue(5.6, 1)//
													.addValue(12.67, 3)//
													.build();//

		BinContainer binContainer2 = BinContainer	.builder(3).addValue(2.1, 5)//
													.addValue(5.6, 1)//
													.addValue(12.67, 3)//
													.build();//

		assertEquals(binContainer1.binCount(), binContainer2.binCount());

		for (int i = 0; i < binContainer1.binCount(); i++) {
			Bin bin1 = binContainer1.getBin(i);
			Bin bin2 = binContainer2.getBin(i);
			assertEquals(bin1, bin2);
			assertEquals(bin1.hashCode(), bin2.hashCode());
		}

	}

	/**
	 * Tests {@link BinContainer.Bin#equals()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "equals", args = { Object.class })
	public void testBinEquals() {
		BinContainer binContainer1 = BinContainer	.builder(3).addValue(2.1, 5)//
													.addValue(5.6, 1)//
													.addValue(6.3, 1)//
													.addValue(12.67, 3)//
													.build();//

		BinContainer binContainer2 = BinContainer	.builder(3).addValue(2.1, 5)//
													.addValue(5.6, 1)//
													.addValue(6.3, 1)//
													.addValue(12.67, 3)//
													.build();//

		assertEquals(binContainer1.binCount(), binContainer2.binCount());

		int binCount = binContainer1.binCount();
		for (int i = 0; i < binCount; i++) {
			Bin bin1 = binContainer1.getBin(i);
			Bin bin2 = binContainer2.getBin(i);

			assertEquals(bin1, bin2);
			assertEquals(bin1, bin1);
			assertNotEquals(bin1, null);
			assertNotEquals(bin1, new Object());
		}
		for (int i = 0; i < binCount; i++) {
			for (int j = i + 1; j < binCount; j++) {
				Bin bin1 = binContainer1.getBin(i);
				Bin bin2 = binContainer1.getBin(j);
				assertNotEquals(bin1, bin2);
			}
		}
	}

	/**
	 * Tests {@link BinContainer.Bin#getLowerBound()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "getLowerBound", args = {})
	public void testGetLowerBound() {
		BinContainer binContainer = BinContainer.builder(3)//
												.addValue(2.1, 5)//
												.addValue(5.6, 1)//
												.addValue(6.3, 1)//
												.addValue(12.67, 3)//
												.build();//

		assertEquals(0.0, binContainer.getBin(0).getLowerBound(), 0);
		assertEquals(3.0, binContainer.getBin(1).getLowerBound(), 0);
		assertEquals(6.0, binContainer.getBin(2).getLowerBound(), 0);
		assertEquals(9.0, binContainer.getBin(3).getLowerBound(), 0);
		assertEquals(12.0, binContainer.getBin(4).getLowerBound(), 0);

	}

	/**
	 * Tests {@link BinContainer.Bin#getUpperBound()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "getUpperBound", args = {})
	public void testGetUpperBound() {
		BinContainer binContainer = BinContainer.builder(3)//
												.addValue(2.1, 5)//
												.addValue(5.6, 1)//
												.addValue(6.3, 1)//
												.addValue(12.67, 3)//
												.build();//

		assertEquals(3.0, binContainer.getBin(0).getUpperBound(), 0);
		assertEquals(6.0, binContainer.getBin(1).getUpperBound(), 0);
		assertEquals(9.0, binContainer.getBin(2).getUpperBound(), 0);
		assertEquals(12.0, binContainer.getBin(3).getUpperBound(), 0);
		assertEquals(15.0, binContainer.getBin(4).getUpperBound(), 0);
	}

	/**
	 * Tests {@link BinContainer.Bin#getCount()}
	 */
	@Test
	@UnitTestMethod(target = Bin.class, name = "getCount", args = {})
	public void testGetCount() {
		BinContainer binContainer = BinContainer.builder(3)//
												.addValue(2.1, 5)//
												.addValue(5.6, 1)//
												.addValue(6.3, 1)//
												.addValue(12.67, 3)//
												.build();//

		assertEquals(5, binContainer.getBin(0).getCount());
		assertEquals(1, binContainer.getBin(1).getCount());
		assertEquals(1, binContainer.getBin(2).getCount());
		assertEquals(0, binContainer.getBin(3).getCount());
		assertEquals(3, binContainer.getBin(4).getCount());
	}

}
