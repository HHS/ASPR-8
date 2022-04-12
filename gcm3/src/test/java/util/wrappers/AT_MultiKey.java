package util.wrappers;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link MultiKey}
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MultiKey.class)
public class AT_MultiKey {

	/**
	 * Tests the equals contract for {@link AT_MultiKey#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEqualsContract() {
		final MultiKey multiKey1 = new MultiKey(3, "B", false);
		final MultiKey multiKey2 = new MultiKey(3, "B", false);
		final MultiKey multiKey3 = new MultiKey(3, "B", false);
		final MultiKey multiKey4 = new MultiKey("X", "X", 17, 44);
		final MultiKey multiKey5 = new MultiKey("A", 2.75, true);
		final MultiKey multiKey6 = new MultiKey();

		/*
		 * Show that what appear as different objects are different
		 */

		assertFalse(multiKey1.equals(multiKey4));
		assertFalse(multiKey1.equals(multiKey5));
		assertFalse(multiKey1.equals(multiKey6));
		assertFalse(multiKey4.equals(multiKey5));
		assertFalse(multiKey4.equals(multiKey6));
		assertFalse(multiKey5.equals(multiKey6));

		/*
		 * Show that we have distinct instances
		 */
		assertNotSame(multiKey1, multiKey2);

		/*
		 * Show that reflexivity holds
		 */
		assertEquals(multiKey1, multiKey1);
		assertEquals(multiKey2, multiKey2);
		assertEquals(multiKey3, multiKey3);
		assertEquals(multiKey4, multiKey4);
		assertEquals(multiKey5, multiKey5);
		assertEquals(multiKey6, multiKey6);
		/*
		 * Show that symmetry holds
		 */
		assertEquals(multiKey1, multiKey2);
		assertEquals(multiKey2, multiKey1);

		/*
		 * Show that transitivity holds
		 */
		assertEquals(multiKey1, multiKey2);
		assertEquals(multiKey2, multiKey3);
		assertEquals(multiKey1, multiKey3);

		/*
		 * Show equal objects have equal hash codes
		 */
		assertEquals(multiKey1.hashCode(), multiKey2.hashCode());
		assertEquals(multiKey2.hashCode(), multiKey3.hashCode());

	}

	/**
	 * Tests {@link MultiKey#getKey(int)}
	 */
	@Test
	@UnitTestMethod(name = "getKey", args = { int.class })
	public void testGetKey() {

		MultiKey multiKey = new MultiKey(3, "B", false);
		assertEquals(multiKey.getKey(0), new Integer(3));
		assertEquals(multiKey.getKey(1), "B");
		assertEquals(multiKey.getKey(2), false);
		assertEquals(3, multiKey.size());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals(multiKey.getKey(0), "X");
		assertEquals(multiKey.getKey(1), "X");
		assertEquals(multiKey.getKey(2), new Integer(17));
		assertEquals(multiKey.getKey(3), new Integer(44));
		assertEquals(4, multiKey.size());

		multiKey = new MultiKey("A", 2.75, true);
		assertEquals(multiKey.getKey(0), "A");
		assertEquals(multiKey.getKey(1), new Double(2.75));
		assertEquals(multiKey.getKey(2), true);
		assertEquals(3, multiKey.size());

		// precondition tests
		assertThrows(ArrayIndexOutOfBoundsException.class, () -> new MultiKey(1, 2, 3).getKey(3));

	}

	/**
	 * Tests {@link MultiKey#getKeys()}
	 */
	@Test
	@UnitTestMethod(name = "getKeys", args = {})
	public void testGetKeys() {
		MultiKey multiKey = new MultiKey(3, "B", false);
		Object[] expectedKeys = new Object[] { 3, "B", false };
		assertArrayEquals(expectedKeys, multiKey.getKeys());

		multiKey = new MultiKey("X", "X", 17, 44);
		expectedKeys = new Object[] { "X", "X", 17, 44 };
		assertArrayEquals(expectedKeys, multiKey.getKeys());

		multiKey = new MultiKey("A", 2.75, true);
		expectedKeys = new Object[] { "A", 2.75, true };
		assertArrayEquals(expectedKeys, multiKey.getKeys());
	}

	/**
	 * Tests {@link MultiKey#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", target = MultiKey.class, args = {})
	public void testMultiKeyBuilder_Constructor() {
		/*
		 * We will show that the MultiKeyBuilder produces the expected MultiKey
		 * values and is reusable.
		 */

		MultiKey builderMultiKey = MultiKey	.builder()//
											.addKey(3)//
											.addKey("B")//
											.addKey(false)//
											.build();//

		assertEquals(builderMultiKey.getKey(0), new Integer(3));
		assertEquals(builderMultiKey.getKey(1), "B");
		assertEquals(builderMultiKey.getKey(2), false);
		assertEquals(3, builderMultiKey.size());
		MultiKey constructorMultiKey = new MultiKey(3, "B", false);
		assertEquals(constructorMultiKey, builderMultiKey);

		builderMultiKey = MultiKey	.builder().addKey("X")//
									.addKey("X")//
									.addKey(17)//
									.addKey(44)//
									.build();//
		assertEquals(builderMultiKey.getKey(0), "X");
		assertEquals(builderMultiKey.getKey(1), "X");
		assertEquals(builderMultiKey.getKey(2), new Integer(17));
		assertEquals(builderMultiKey.getKey(3), new Integer(44));
		assertEquals(4, builderMultiKey.size());
		constructorMultiKey = new MultiKey("X", "X", 17, 44);
		assertEquals(constructorMultiKey, builderMultiKey);

		builderMultiKey = MultiKey	.builder()//
									.addKey("A")//
									.addKey(2.75)//
									.addKey(true)//
									.build();//

		assertEquals(builderMultiKey.getKey(0), "A");
		assertEquals(builderMultiKey.getKey(1), new Double(2.75));
		assertEquals(builderMultiKey.getKey(2), true);
		assertEquals(3, builderMultiKey.size());
		constructorMultiKey = new MultiKey("A", 2.75, true);
		assertEquals(constructorMultiKey, builderMultiKey);

	}

	/**
	 * Tests {@link MultiKey.MultiKeyBuilder#addKey(Object)}
	 */
	@Test
	@UnitTestMethod(name = "addKey", target = MultiKey.Builder.class, args = { Object.class })
	public void testMultiKeyBuilder_AddKey() {
		// covered by testMultiKeyBuilder_Constructor
	}

	/**
	 * Tests {@link MultiKey.MultiKeyBuilder#build()}
	 */
	@Test
	@UnitTestMethod(name = "build", target = MultiKey.Builder.class, args = {})
	public void testMultiKeyBuilder_Build() {
		// covered by testMultiKeyBuilder_Constructor
	}

	/**
	 * Tests {@link MultiKey#MultiKey(Object...)}
	 */
	@Test
	@UnitTestConstructor(args = { Object[].class })
	public void testConstructor() {
		/*
		 * We will show that the MultiKey constructor produces the expected
		 * MultiKey
		 */

		MultiKey multiKey = new MultiKey(3, "B", false);
		assertEquals(multiKey.getKey(0), new Integer(3));
		assertEquals(multiKey.getKey(1), "B");
		assertEquals(multiKey.getKey(2), false);
		assertEquals(3, multiKey.size());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals(multiKey.getKey(0), "X");
		assertEquals(multiKey.getKey(1), "X");
		assertEquals(multiKey.getKey(2), new Integer(17));
		assertEquals(multiKey.getKey(3), new Integer(44));
		assertEquals(4, multiKey.size());

		multiKey = new MultiKey("A", 2.75, true);
		assertEquals(multiKey.getKey(0), "A");
		assertEquals(multiKey.getKey(1), new Double(2.75));
		assertEquals(multiKey.getKey(2), true);
		assertEquals(3, multiKey.size());
	}

	/**
	 * Tests {@link MultiKey#size()}
	 */
	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		MultiKey multiKey = new MultiKey();
		assertEquals(0, multiKey.size());

		multiKey = new MultiKey("D");
		assertEquals(1, multiKey.size());

		multiKey = new MultiKey(4.5, 12);
		assertEquals(2, multiKey.size());

		multiKey = new MultiKey(3, "B", false);
		assertEquals(3, multiKey.size());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals(4, multiKey.size());

	}

	/**
	 * Tests {@link MultiKey#toKeyString()}
	 */
	@Test
	@UnitTestMethod(name = "toKeyString", args = {})
	public void testToKeyString() {
		MultiKey multiKey = new MultiKey();
		assertEquals("[]", multiKey.toKeyString());

		multiKey = new MultiKey("D");
		assertEquals("[D]", multiKey.toKeyString());

		multiKey = new MultiKey(4.5, 12);
		assertEquals("[4.5, 12]", multiKey.toKeyString());

		multiKey = new MultiKey(3, "B", false);
		assertEquals("[3, B, false]", multiKey.toKeyString());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals("[X, X, 17, 44]", multiKey.toKeyString());
	}

	/**
	 * Tests {@link MultiKey#toTabString()}
	 */
	@Test
	@UnitTestMethod(name = "toTabString", args = {})
	public void testToTabString() {
		MultiKey multiKey = new MultiKey();
		assertEquals("", multiKey.toTabString());

		multiKey = new MultiKey("D");
		assertEquals("D", multiKey.toTabString());

		multiKey = new MultiKey(4.5, 12);

		assertEquals("4.5\t12", multiKey.toTabString());

		multiKey = new MultiKey(3, "B", false);
		assertEquals("3\tB\tfalse", multiKey.toTabString());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals("X\tX\t17\t44", multiKey.toTabString());
	}

	/**
	 * Tests {@link MultiKey#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MultiKey multiKey = new MultiKey();
		assertEquals("MultiKey [objects=[]]", multiKey.toString());

		multiKey = new MultiKey("D");
		assertEquals("MultiKey [objects=[D]]", multiKey.toString());

		multiKey = new MultiKey(4.5, 12);
		assertEquals("MultiKey [objects=[4.5, 12]]", multiKey.toString());

		multiKey = new MultiKey(3, "B", false);
		assertEquals("MultiKey [objects=[3, B, false]]", multiKey.toString());

		multiKey = new MultiKey("X", "X", 17, 44);
		assertEquals("MultiKey [objects=[X, X, 17, 44]]", multiKey.toString());
	}

	/**
	 * Tests {@link MultiKey#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		MultiKey multiKey1 = new MultiKey(3, "B", false);
		MultiKey multiKey2 = new MultiKey(3, "B", false);
		MultiKey multiKey3 = new MultiKey(3, "A", false);
		MultiKey multiKey4 = new MultiKey(3, "b", false);

		// reflexive
		assertEquals(multiKey1, multiKey1);
		assertEquals(multiKey2, multiKey2);
		assertEquals(multiKey3, multiKey3);
		assertEquals(multiKey4, multiKey4);

		// symmetric
		assertEquals(multiKey1, multiKey2);
		assertEquals(multiKey2, multiKey1);

		// transitive -- no need

		// unequal
		assertNotEquals(multiKey1, multiKey3);
		assertNotEquals(multiKey1, multiKey4);
		assertNotEquals(multiKey2, multiKey3);
		assertNotEquals(multiKey2, multiKey4);
		assertNotEquals(multiKey3, multiKey4);

	}

	/**
	 * Tests {@link MultiKey#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		MultiKey multiKey1 = new MultiKey(3, "B", false);
		MultiKey multiKey2 = new MultiKey(3, "B", false);
		assertEquals(multiKey1.hashCode(), multiKey2.hashCode());
	}
}
