package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import util.spherical.Chirality;
import util.vector.MutableVector2D;
import util.vector.MutableVector3D;
import util.vector.Vector2D;

/**
 * Test class for {@link Vector2D}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Vector2D.class)
public class AT_Vector2D {

	private static final double TOLERANCE = 0.000001;

	/**
	 * Tests {@linkplain Vector2D#add(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { Vector2D.class })
	public void testAdd_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7048654979720366561L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			Vector2D v3 = v1.add(v2);

			assertEquals(x1 + x2, v3.getX(), 0);
			assertEquals(y1 + y2, v3.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#add(double, double)}
	 * 
	 * Tests {@linkplain Vector2D#add(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double.class, double.class })
	public void testAdd_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7043233391431344104L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = v1.add(x2, y2);

			assertEquals(x1 + x2, v2.getX(), 0);
			assertEquals(y1 + y2, v2.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructors_Empty() {
		Vector2D v = new Vector2D();
		assertEquals(0, v.getX(), 0);
		assertEquals(0, v.getY(), 0);
	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(MutableVector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { MutableVector2D.class })
	public void testConstructors_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7256970997578026212L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x, y);

			Vector2D v2 = new Vector2D(v1);

			assertEquals(v2.getX(), x, 0);
			assertEquals(v2.getY(), y, 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(Vector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { Vector2D.class })
	public void testConstructors_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2499300329261423411L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(v1);

			assertEquals(v2.getX(), x, 0);
			assertEquals(v2.getY(), y, 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#Vector2D(double, double)}
	 */
	@Test
	@UnitTestConstructor(args = { double.class, double.class })
	public void testConstructors_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6200717978363268201L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x, y);

			assertEquals(v1.getX(), x, 0);
			assertEquals(v1.getY(), y, 0);
		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#addScaled(Vector2D, double)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "addScaled", args = { Vector2D.class, double.class })
	public void testAddScaled() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5477630435621881354L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v3 = v1.addScaled(v2, scale);

			assertEquals(x1 + x2 * scale, v3.getX(), 0);
			assertEquals(y1 + y2 * scale, v3.getY(), 0);
		}

	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#angle(Vector2D)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "angle", args = { Vector2D.class })
	public void testAngle() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5443394431789515021L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double length1 = FastMath.sqrt(x1 * x1 + y1 * y1);
			double length2 = FastMath.sqrt(x2 * x2 + y2 * y2);
			double dotProduct = x1 * x2 + y1 * y2;
			double cosTheta = dotProduct / (length1 * length2);
			double expectedValue = FastMath.acos(cosTheta);

			double actualValue = v1.angle(v2);

			assertEquals(expectedValue, actualValue, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#cross(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "cross", args = { Vector2D.class })
	public void testCross() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2072134709060069627L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);
			int actual = v2.cross(v1);

			double zComponentOf3DCrossProduct = (x2 * y1) - (x1 * y2);
			int expected;
			if (zComponentOf3DCrossProduct < 0) {
				expected = -1;
			} else {
				expected = 1;
			}
			assertEquals(expected, actual);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#distanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "distanceTo", args = { Vector2D.class })
	public void testDistanceTo() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8108866891186001273L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

			double actual = v2.distanceTo(v1);

			assertEquals(expected, actual, TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain Vector2D#dot(Vector2D))}
	 */
	@Test
	@UnitTestMethod(name = "dot", args = { Vector2D.class })
	public void testDot() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5455897510253329686L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = x1 * x2 + y1 * y2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#get(int)}
	 */
	@Test
	@UnitTestMethod(name = "get", args = { int.class })
	public void testGet() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1280663175219265421L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(x, v.get(0), 0);
			assertEquals(y, v.get(1), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#getX()}
	 */
	@Test
	@UnitTestMethod(name = "getX", args = {})
	public void testGetX() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5972054182695362046L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(x, v.getX(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#getY()}
	 */
	@Test
	@UnitTestMethod(name = "getY", args = {})
	public void testGetY() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7269151178334818715L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#scale(double)}
	 */
	@Test
	@UnitTestMethod(name = "scale", args = { double.class })
	public void testScale() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6482923353153373662L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double scalar = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = v.scale(scalar);

			assertEquals(x * scalar, v1.getX(), 0);
			assertEquals(y * scalar, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#sub(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { Vector2D.class })
	public void testSub_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2615726850696406829L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			v1 = v1.sub(v2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain Vector2D#sub(double, double)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { double.class, double.class })
	public void testSub_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(594158034033440503L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new Vector2D(x1, y1);
			v1 = v1.sub(x2, y2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#isInfinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isInfinite", args = {})
	public void testIsInfinite() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2322202277366389607L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			assertFalse(v.isInfinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, y);
			assertTrue(v.isInfinite());

			v = new Vector2D(x, Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, y);
			assertTrue(v.isInfinite());

			v = new Vector2D(x, Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());
		}
	}

	/**
	 * Tests {@linkplain Vector2D#isNaN()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isNaN", args = {})
	public void testIsNaN() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4021866300032499888L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			assertFalse(v.isNaN());

			v = new Vector2D(x, Double.NaN);
			assertTrue(v.isNaN());

			v = new Vector2D(Double.NaN, y);
			assertTrue(v.isNaN());

			v = new Vector2D(Double.NaN, Double.NaN);
			assertTrue(v.isNaN());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#isFinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isFinite", args = {})
	public void testIsFinite() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2864965851085774662L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v = new Vector2D(x, y);
			assertTrue(v.isFinite());

			v = new Vector2D(Double.NaN, y);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, y);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, y);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(x, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NaN, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#squareDistanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "squareDistanceTo", args = { Vector2D.class })
	public void testSquareDistanceTo() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(972530617533616033L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

			double actual = v2.squareDistanceTo(v1);

			assertEquals(expected, actual, 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#reverse()}
	 */
	@Test
	@UnitTestMethod(name = "reverse", args = {})
	public void testReverse() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2281430038941666208L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			Vector2D v1 = v.reverse();

			assertEquals(-x, v1.getX(), 0);
			assertEquals(-y, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#length()}
	 */
	@Test
	@UnitTestMethod(name = "length", args = {})
	public void testLength() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4831419001108209090L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double expectedLength = FastMath.sqrt(x * x + y * y);
			double actualLength = v.length();

			assertEquals(expectedLength, actualLength, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#squareLength()}
	 */
	@Test
	@UnitTestMethod(name = "squareLength", args = {})
	public void testSquareLength() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1646469899835942461L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double expectedLength = x * x + y * y;
			double actualLength = v.squareLength();

			assertEquals(expectedLength, actualLength, 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#toArray()}
	 */
	@Test
	@UnitTestMethod(name = "toArray", args = {})
	public void testToArray() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8359854340367149037L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			double[] array = v.toArray();

			assertEquals(v.getX(), array[0], 0);
			assertEquals(v.getY(), array[1], 0);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#normalize()}
	 */
	@Test
	@UnitTestMethod(name = "normalize", args = {})
	public void testNormalize() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2056526750467808604L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);
			Vector2D v1 = v.normalize();

			assertEquals(1, v1.length(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#equals(Object))}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1772313867718129269L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(x, y);

			Vector2D v3 = new Vector2D(x, y);

			// reflexive
			assertEquals(v1, v1);

			// symetric
			assertEquals(v1, v2);
			assertEquals(v2, v1);

			// transitive
			assertEquals(v1, v2);
			assertEquals(v2, v3);
			assertEquals(v3, v1);

		}
	}

	/**
	 * Tests {@linkplain Vector2D#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4728150157129004474L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x, y);

			Vector2D v2 = new Vector2D(x, y);

			assertEquals(v1.hashCode(), v2.hashCode());

		}
	}

	/**
	 * Tests {@linkplain Vector2D#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6250827979384795121L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			String expected = "Vector2D [x=" + x + ", y=" + y + "]";

			String actual = v.toString();

			assertEquals(expected, actual);
		}
	}

	/**
	 * Tests {@linkplain Vector2D#rotate(double)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "rotate", args = { double.class })
	public void testRotate() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3324225447982087438L);

		for (int i = 0; i < 100; i++) {
			// ensure that v1 is not too close to a zero vector
			Vector2D v1 = new Vector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector2D(x1, y1);
			}

			// Copy v1 and rotate the copy
			Vector2D v2 = new Vector2D(v1);
			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;
			v2 = v2.rotate(theta);

			// v1 under rotation should have its length preserved
			assertEquals(v1.length(), v2.length(), TOLERANCE);

			// v2 and v1 should have theta as their angle
			double expectedAngle = theta;
			while (expectedAngle < 0) {
				expectedAngle += 2 * FastMath.PI;
			}
			while (expectedAngle > FastMath.PI) {
				expectedAngle = 2 * FastMath.PI - expectedAngle;
			}
			assertEquals(expectedAngle, v2.angle(v1), TOLERANCE);

			// v2 and v1 should be oriented correctly
			if (theta < FastMath.PI) {
				assertEquals(1, v1.cross(v2));
			} else {
				assertEquals(-1, v1.cross(v2));
			}

			// v2 when rotated back should return to its original position
			Vector2D v3 = v2.rotate(-theta);
			assertEquals(v1.getX(), v3.getX(), TOLERANCE);
			assertEquals(v1.getY(), v3.getY(), TOLERANCE);
		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector2D#rotateToward(Vector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "rotateToward", args = { Vector2D.class, double.class })
	public void testRotateToward() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4005002637954997121L);

		for (int i = 0; i < 100; i++) {

			// Ensure that v1 is not too close to the zero vector
			Vector2D v1 = new Vector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector2D(x1, y1);
			}

			// Ensure that v2 is not too close to the zero vector
			Vector2D v2 = new Vector2D();
			while (v2.length() < TOLERANCE) {
				double x2 = randomGenerator.nextDouble() * 1000 - 500;
				double y2 = randomGenerator.nextDouble() * 1000 - 500;
				v2 = new Vector2D(x2, y2);
			}

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			Vector2D v3 = new Vector2D(v2);
			v3.rotateToward(v1, theta);

			// Rotation toward another vector is equivalent to plain rotation
			// with a possible sign change due to the relative orientation of
			// the two vectors.
			Vector2D v4 = new Vector2D(v2);
			if (v2.cross(v1) > 0) {
				v4.rotate(theta);
			} else {
				v4.rotate(-theta);
			}

			assertEquals(v4.getX(), v3.getX(), TOLERANCE);
			assertEquals(v4.getY(), v3.getY(), TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain Vector2D#perpendicularRotation(Chirality)}
	 */
	@Test
	@UnitTestMethod(name = "perpendicularRotation", args = { Chirality.class })
	public void testPerpendicularRotation() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4389418071564368339L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			Vector2D v2 = new Vector2D(x2, y2);

			v2 = v1.perpendicularRotation(Chirality.LEFT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points up
			assertEquals(1, v2.cross(v1));

			v2 = v1.perpendicularRotation(Chirality.RIGHT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points down
			assertEquals(-1, v2.cross(v1));

		}

	}

	/**
	 * Tests {@linkplain Vector2D#isNormal()}
	 */
	@Test
	@UnitTestMethod(name = "isNormal", args = {})
	public void testIsNormal() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3582310914345350872L);

		int activeTestCount = 0;
		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v = new Vector2D(x, y);

			if (FastMath.abs(v.length() - 1) > MutableVector3D.NORMAL_LENGTH_TOLERANCE) {
				v = v.normalize();
				assertTrue(v.isNormal());
				activeTestCount++;

				Vector2D u = v.scale(1 - 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());

				u = v.scale(1 + 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());
			}
		}
		assertTrue(activeTestCount > 90);
	}

	/**
	 * Tests {@linkplain Vector2D#isPerpendicularTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "isPerpendicularTo", args = { Vector2D.class })
	public void testIsPerpendicularTo() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8574565110919873045L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			Vector2D v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.PI / 2 - 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new Vector2D(v1);
			v3 = v3.rotateToward(new Vector2D(v2), FastMath.PI / 2 + 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}
	}
}
