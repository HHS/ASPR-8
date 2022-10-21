package plugins.materials.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;


@UnitTest(target = StageId.class)
public class AT_StageId  {

	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 10; i++) {
			StageId StageId = new StageId(i);
			assertEquals(i, StageId.getValue());
		}

	}

	@Test
	@UnitTestMethod(name = "compareTo", args = { StageId.class })
	public void testCompareTo() {
		for (int i = 0; i < 10; i++) {
			StageId stageA = new StageId(i);
			for (int j = 0; j < 10; j++) {
				StageId stageB = new StageId(j);
				int comparisonValue = stageA.compareTo(stageB);
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
			StageId stageA = new StageId(i);
			for (int j = 0; j < 10; j++) {
				StageId stageB = new StageId(j);				
				if (i == j) {
					assertEquals(stageA,stageB);
				} else {
					assertNotEquals(stageA,stageB);
				}
			}
		}
	}

	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 10; i++) {
			StageId stage = new StageId(i);
			assertEquals(i, stage.getValue());
		}
	}

	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 10; i++) {
			StageId stage = new StageId(i);
			assertEquals(i, stage.hashCode());
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 10; i++) {
			StageId stage = new StageId(i);
			assertEquals(Integer.toString(i), stage.toString());
		}
	}


}
