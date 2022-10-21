package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = BatchId.class)
public class AT_BatchId {

	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			BatchId BatchId = new BatchId(i);
			assertEquals(i, BatchId.getValue());
		}

	}

	@Test
	@UnitTestMethod(name = "compareTo", args = { BatchId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			BatchId batchA = new BatchId(i);
			for (int j = 0; j < 10; j++) {
				BatchId batchB = new BatchId(j);
				int comparisonValue = batchA.compareTo(batchB);
				if (i < j) {
					assertTrue(comparisonValue < 0);
				} else if (i > j) {
					assertTrue(comparisonValue > 0);
				} else {
					assertTrue(comparisonValue == 0);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		for (int i = 0; i < 10; i++) {
			BatchId batchA = new BatchId(i);
			for (int j = 0; j < 10; j++) {
				BatchId batchB = new BatchId(j);				
				if (i == j) {
					assertEquals(batchA,batchB);
				} else {
					assertNotEquals(batchA,batchB);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(i, batch.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(i, batch.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(Integer.toString(i), batch.toString());
		}
	}
}
