package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link IntId}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = IntId.class)
public class AT_IntId {
	/**
	 * Tests {@link IntId#getValue()}
	 */
	@Test
	@UnitTestMethod(name = "getValue", args = {})
	public void testGetValue() {
		for (int i = 0; i < 1000; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(i, personId.getValue());
		}
	}

	/**
	 * Tests {@link IntId#IntId(int))}
	 */
	@Test
	@UnitTestConstructor(args = { int.class })
	public void testConstructor() {
		for (int i = 0; i < 100; i++) {
			int value = i - 50;
			IntId intId = new IntId(value);
			assertNotNull(intId);
			assertEquals(value, intId.getValue());
		}
	}

	/**
	 * Tests {@link IntId#compareTo(IntId))}
	 */
	@Test
	@UnitTestMethod(name = "compareTo", args = { IntId.class })
	public void testCompareTo() {
		int testSize = 30;
		List<PersonId> personIds = new ArrayList<>();
		for (int i = 0; i < testSize; i++) {
			personIds.add(new PersonId(i));
		}

		for (int i = 0; i < testSize; i++) {
			PersonId personIdI = personIds.get(i);
			for (int j = 0; j < testSize; j++) {
				PersonId personIdJ = personIds.get(j);
				int comparisonResult = personIdI.compareTo(personIdJ);
				if (i < j) {
					assertTrue(comparisonResult < 0);
				} else {
					if (i == j) {
						assertTrue(comparisonResult == 0);
					} else {
						assertTrue(comparisonResult > 0);
					}
				}
			}
		}
	}

	/**
	 * Tests {@link IntId#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		for (int i = 0; i < 1000; i++) {
			PersonId personId = new PersonId(i);
			assertEquals(personId.getValue(), personId.hashCode());
		}
	}

	/**
	 * Tests {@link IntId#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		int testSize = 30;
		List<PersonId> personIds = new ArrayList<>();
		for (int i = 0; i < testSize; i++) {
			personIds.add(new PersonId(i));
		}

		for (int i = 0; i < testSize; i++) {
			PersonId personIdI = personIds.get(i);
			for (int j = 0; j < testSize; j++) {
				PersonId personIdJ = personIds.get(j);
				if (i == j) {
					assertTrue(personIdI.equals(personIdJ));
					assertEquals(personIdI.hashCode(), personIdJ.hashCode());
				} else {
					assertFalse(personIdI.equals(personIdJ));
				}

			}
		}

	}

	/**
	 * Test {@link IntId#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals(Integer.toString(i), new IntId(i).toString());
		}
	}

}
