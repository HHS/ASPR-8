package plugins.util.properties.arraycontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.util.properties.arraycontainers.IntValueContainer.IntValueType;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_IntValueContainer {

	/**
	 * Test for {@link IntValueContainer#getValueAsByte(int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getValueAsByte", args = { int.class })
	public void testGetValueAsByte() {
		long defaultValue = 123;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		byte[] bytes = new byte[highIndex];
		for (int i = 0; i < bytes.length; i++) {
			byte b = (byte) (i % 256 - 128);
			bytes[i] = b;
		}
		for (int i = 0; i < bytes.length; i++) {
			intValueContainer.setByteValue(i, bytes[i]);
		}

		for (int i = 0; i < bytes.length; i++) {
			assertEquals(intValueContainer.getValueAsByte(i), bytes[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsByte(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsByte(-1));

		// if the value to return is not compatible with byte
		intValueContainer.setIntValue(highIndex, 240);
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsByte(highIndex));

	}

	/**
	 * Test for {@link IntValueContainer#getValueAsInt(int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getValueAsInt", args = { int.class })
	public void testGetValueAsInt() {
		long defaultValue = 9546754;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		int[] ints = new int[highIndex];
		for (int i = 0; i < ints.length; i++) {
			ints[i] = i;
		}
		for (int i = 0; i < ints.length; i++) {
			intValueContainer.setIntValue(i, ints[i]);
		}

		for (int i = 0; i < ints.length; i++) {
			assertEquals(intValueContainer.getValueAsInt(i), ints[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsInt(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsInt(-1));

		// if the value to return is not compatible with int
		intValueContainer.setLongValue(highIndex, 535445345543L);
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsInt(highIndex));
	}

	/**
	 * Test for {@link IntValueContainer#getValueAsLong(int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getValueAsLong", args = { int.class })
	public void testGetValueAsLong() {
		long defaultValue = 9546754;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		long[] longs = new long[highIndex];
		for (int i = 0; i < longs.length; i++) {
			longs[i] = 745644534457456L + i;
		}
		for (int i = 0; i < longs.length; i++) {
			intValueContainer.setLongValue(i, longs[i]);
		}

		for (int i = 0; i < longs.length; i++) {
			assertEquals(intValueContainer.getValueAsLong(i), longs[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsLong(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsLong(-1));

		// if the value to return is not compatible with long -- can't fail
		// since all values are compatible with long.

	}

	/**
	 * Test for {@link IntValueContainer#getValueAsShort(int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getValueAsShort", args = { int.class })
	public void testGetValueAsShort() {
		short defaultValue = 30467;
		IntValueContainer intValueContainer = new IntValueContainer(defaultValue);
		int highIndex = 1000;

		short[] shorts = new short[highIndex];
		for (int i = 0; i < shorts.length; i++) {
			short s = (short) (i % (256 * 256) - 128 * 256);
			shorts[i] = s;
		}
		for (int i = 0; i < shorts.length; i++) {
			intValueContainer.setShortValue(i, shorts[i]);
		}

		for (int i = 0; i < shorts.length; i++) {
			assertEquals(intValueContainer.getValueAsShort(i), shorts[i]);
		}

		// show that the default value is returned for indices that have not yet
		// had value assignments
		for (int i = 0; i < 5; i++) {
			assertEquals(intValueContainer.getValueAsShort(i + highIndex), defaultValue);
		}

		// pre-condition tests

		// if index < 0
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsShort(-1));

		// if the value to return is not compatible with short
		intValueContainer.setIntValue(highIndex, 40000);
		assertThrows(RuntimeException.class, () -> intValueContainer.getValueAsShort(highIndex));

	}

	/**
	 * Test for {@link IntValueContainer#setCapacity(int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "setCapacity", args = { int.class })
	public void testSetCapacity() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		int expectedCapacity = 5;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 15;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 50;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

		expectedCapacity = 1000;
		intValueContainer.setCapacity(expectedCapacity);
		assertTrue(intValueContainer.getCapacity() >= expectedCapacity);

	}

	/**
	 * Test for {@link IntValueContainer#getCapacity()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getCapacity", args = {})
	public void testGetCapacity() {

		IntValueContainer intValueContainer = new IntValueContainer(0);

		assertTrue(intValueContainer.getCapacity() >= 0);

		intValueContainer.setIntValue(1, 1234);
		assertTrue(intValueContainer.getCapacity() >= 1);

		intValueContainer.setIntValue(34, 364);
		assertTrue(intValueContainer.getCapacity() >= 34);

		intValueContainer.setIntValue(10, 154);
		assertTrue(intValueContainer.getCapacity() >= 10);

		intValueContainer.setIntValue(137, 2526);
		assertTrue(intValueContainer.getCapacity() >= 137);

		intValueContainer.setLongValue(1000, 1234534234234234234L);
		assertTrue(intValueContainer.getCapacity() >= 1000);

	}

	/**
	 * Test for {@link IntValueContainer#setLongValue(int, long)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "setLongValue", args = { int.class, long.class })
	public void testSetLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l = 523423463534562345L;
		intValueContainer.setLongValue(0, l);
		assertEquals(l, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l2 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.setLongValue(-1, l2));

	}

	/**
	 * Test for {@link IntValueContainer#setIntValue(int, int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "setIntValue", args = { int.class, int.class })
	public void testSetIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i = 70000;
		intValueContainer.setIntValue(0, i);
		assertEquals(i, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i2 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.setIntValue(-1, i2));
	}

	/**
	 * Test for {@link IntValueContainer#setShortValue(int, short)}
	 */

	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "setShortValue", args = { int.class, short.class })
	public void testSetShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s = 300;
		intValueContainer.setShortValue(0, s);
		assertEquals(s, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s2 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.setShortValue(-1, s2));

	}

	/**
	 * Test for {@link IntValueContainer#setByteValue(int, byte)}
	 */

	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "setByteValue", args = { int.class, byte.class })
	public void testSetByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b = 5;
		intValueContainer.setByteValue(0, b);
		assertEquals(b, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b2 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.setByteValue(-1, b2));

	}

	/**
	 * Test for {@link IntValueContainer#incrementIntValue(int, int)}
	 */

	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "incrementIntValue", args = { int.class, int.class })
	public void testIncrementIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i1 = 70000;
		intValueContainer.setIntValue(0, i1);
		int i2 = 2000;
		intValueContainer.incrementIntValue(0, i2);
		assertEquals(i1 + i2, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.incrementIntValue(-1, i3));
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.incrementIntValue(0, i3));
	}

	/**
	 * Test for {@link IntValueContainer#incrementLongValue(int, long)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "incrementLongValue", args = { int.class, long.class })
	public void testIncrementLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l1 = 523423463534562345L;
		intValueContainer.setLongValue(0, l1);
		long l2 = 66457456456456456L;
		intValueContainer.incrementLongValue(0, l2);
		assertEquals(l1 + l2, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.incrementLongValue(-1, l3));
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.incrementLongValue(0, l3));
	}

	/**
	 * Test for {@link IntValueContainer#incrementShortValue(int, short)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "incrementShortValue", args = { int.class, short.class })
	public void testIncrementShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s1 = 300;
		intValueContainer.setShortValue(0, s1);
		short s2 = 100;
		intValueContainer.incrementShortValue(0, s2);
		assertEquals(s1 + s2, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.incrementShortValue(-1, s3));
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.incrementShortValue(0, s3));
	}

	/**
	 * Test for {@link IntValueContainer#incrementByteValue(int, byte)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "incrementByteValue", args = { int.class, byte.class })
	public void testIncrementByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b1 = 5;
		intValueContainer.setByteValue(0, b1);
		byte b2 = 12;
		intValueContainer.incrementByteValue(0, b2);
		assertEquals(b1 + b2, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.incrementByteValue(-1, b3));
		intValueContainer.setLongValue(0, Long.MAX_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.incrementByteValue(0, b3));
	}

	/**
	 * Test for {@link IntValueContainer#decrementByteValue(int, byte)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "decrementByteValue", args = { int.class, byte.class })
	public void testDecrementByteValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// byte value
		byte b1 = 5;
		intValueContainer.setByteValue(0, b1);

		byte b2 = 12;
		intValueContainer.decrementByteValue(0, b2);
		assertEquals(b1 - b2, intValueContainer.getValueAsByte(0));

		// pre-condition tests
		byte b3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.decrementByteValue(-1, b3));
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.decrementByteValue(0, b3));
	}

	/**
	 * Test for {@link IntValueContainer#decrementShortValue(int, short)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "decrementShortValue", args = { int.class, short.class })
	public void testDecrementShortValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// short value
		short s1 = 300;
		intValueContainer.setShortValue(0, s1);
		short s2 = 100;
		intValueContainer.decrementShortValue(0, s2);
		assertEquals(s1 - s2, intValueContainer.getValueAsShort(0));

		// pre-condition tests
		short s3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.decrementShortValue(-1, s3));
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.decrementShortValue(0, s3));
	}

	/**
	 * Test for {@link IntValueContainer#decrementIntValue(int, int)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "decrementIntValue", args = { int.class, int.class })
	public void testDecrementIntValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// int value
		int i1 = 70000;
		intValueContainer.setIntValue(0, i1);
		int i2 = 2000;
		intValueContainer.decrementIntValue(0, i2);
		assertEquals(i1 - i2, intValueContainer.getValueAsInt(0));

		// pre-condition tests
		int i3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.decrementIntValue(-1, i3));
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.decrementIntValue(0, i3));
	}

	/**
	 * Test for {@link IntValueContainer#decrementLongValue(int, long)}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "decrementLongValue", args = { int.class, long.class })
	public void testDecrementLongValue() {
		IntValueContainer intValueContainer = new IntValueContainer(0);

		// long value
		long l1 = 523423463534562345L;
		intValueContainer.setLongValue(0, l1);
		long l2 = 66457456456456456L;
		intValueContainer.decrementLongValue(0, l2);
		assertEquals(l1 - l2, intValueContainer.getValueAsLong(0));

		// pre-condition tests
		long l3 = 1;
		assertThrows(RuntimeException.class, () -> intValueContainer.decrementLongValue(-1, l3));
		intValueContainer.setLongValue(0, Long.MIN_VALUE);

		assertThrows(ArithmeticException.class, () -> intValueContainer.decrementLongValue(0, l3));
	}

	/**
	 * Test for {@link IntValueContainer#IntValueContainer(long)}
	 */
	@Test
	@UnitTestConstructor(target = IntValueContainer.class, args = { long.class })
	public void testConstructor_Long() {
		IntValueContainer intValueContainer = new IntValueContainer(12);
		assertNotNull(intValueContainer);
	}

	/**
	 * Test for {@link IntValueContainer#IntValueContainer(long, int)}
	 */
	@Test
	@UnitTestConstructor(target = IntValueContainer.class, args = { long.class, int.class })
	public void testConstructor_LongInt() {
		IntValueContainer intValueContainer = new IntValueContainer(12, 0);
		assertNotNull(intValueContainer);

		intValueContainer = new IntValueContainer(12, 30);
		assertNotNull(intValueContainer);

		assertThrows(NegativeArraySizeException.class, () -> new IntValueContainer(12, -1));

	}

	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsByte()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getDefaultValueAsByte", args = {})
	public void testGetDefaultValueAsByte() {
		byte expected = 120;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		byte actual = intValueContainer.getDefaultValueAsByte();
		assertEquals(expected, actual);

		// pre-condition tests

		// default short
		assertThrows(RuntimeException.class, () -> new IntValueContainer(30000).getDefaultValueAsByte());

		// default int
		assertThrows(RuntimeException.class, () -> new IntValueContainer(120000).getDefaultValueAsByte());

		// default long
		assertThrows(RuntimeException.class, () -> new IntValueContainer(123124235123234234L).getDefaultValueAsByte());

	}

	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsShort()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getDefaultValueAsShort", args = {})
	public void testGetDefaultValueAsShort() {
		short expected = 32000;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		short actual = intValueContainer.getDefaultValueAsShort();
		assertEquals(expected, actual);

		// pre-condition tests

		// default int
		assertThrows(RuntimeException.class, () -> new IntValueContainer(120000).getDefaultValueAsShort());

		// default long
		assertThrows(RuntimeException.class, () -> new IntValueContainer(123124235123234234L).getDefaultValueAsShort());
	}

	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsInt()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getDefaultValueAsInt", args = {})
	public void testGetDefaultValueAsInt() {
		int expected = 52000;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		int actual = intValueContainer.getDefaultValueAsInt();
		assertEquals(expected, actual);

		// pre-condition tests

		// default long
		assertThrows(RuntimeException.class, () -> new IntValueContainer(123124235123234234L).getDefaultValueAsInt());
	}

	/**
	 * Test for {@link IntValueContainer#getDefaultValueAsLong()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getDefaultValueAsLong", args = {})
	public void testGetDefaultValueAsLong() {
		long expected = 364534534534534345L;
		IntValueContainer intValueContainer = new IntValueContainer(expected);
		long actual = intValueContainer.getDefaultValueAsLong();
		assertEquals(expected, actual);

		// pre-condition tests -- none
	}

	/**
	 * Test for {@link IntValueContainer#getIntValueType()}
	 */
	@Test
	@UnitTestMethod(target = IntValueContainer.class, name = "getIntValueType", args = {})
	public void testGetIntValueType() {
		IntValueContainer intValueContainer = new IntValueContainer(0);
		assertEquals(IntValueType.BYTE, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(0, 1);
		assertEquals(IntValueType.BYTE, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(1, 130);
		assertEquals(IntValueType.SHORT, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(2, 70000);
		assertEquals(IntValueType.INT, intValueContainer.getIntValueType());

		intValueContainer.setLongValue(3, 123123123123123123L);
		assertEquals(IntValueType.LONG, intValueContainer.getIntValueType());

		intValueContainer.setIntValue(4, 1);
		assertEquals(IntValueType.LONG, intValueContainer.getIntValueType());

	}

}
