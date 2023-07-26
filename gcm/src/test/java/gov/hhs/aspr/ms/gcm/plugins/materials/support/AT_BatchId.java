package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_BatchId {

	@Test
	@UnitTestConstructor(target = BatchId.class,args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			BatchId BatchId = new BatchId(i);
			assertEquals(i, BatchId.getValue());
		}

	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "compareTo", args = { BatchId.class })
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
	@UnitTestMethod(target = BatchId.class,name = "equals", args = { Object.class })
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
	@UnitTestMethod(target = BatchId.class,name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(i, batch.getValue());
		}
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(i, batch.hashCode());
		}
	}

	@Test
	@UnitTestMethod(target = BatchId.class,name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			BatchId batch = new BatchId(i);
			assertEquals(Integer.toString(i), batch.toString());
		}
	}
}
