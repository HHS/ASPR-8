package util.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestField;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;


public class AT_Vector3D {

	private static final double TOLERANCE = 0.000001;

	@Test
	@UnitTestField(target = Vector3D.class, name = "NORMAL_LENGTH_TOLERANCE")
	public void testNormalLengthTolerance() {
		assertEquals(1E-13, MutableVector2D.NORMAL_LENGTH_TOLERANCE, 0);
	}

	@Test
	@UnitTestField(target = Vector3D.class, name = "PERPENDICULAR_ANGLE_TOLERANCE")
	public void testPerpendicularAngleTolerance() {
		assertEquals(1E-13, MutableVector2D.PERPENDICULAR_ANGLE_TOLERANCE, 0);
	}

	/**
	 * Tests {@linkplain Vector3D#add(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "add", args = { Vector3D.class })
	public void testAdd_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7836660604999880350L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v2 = new Vector3D(x2, y2, z2);

			v1 = v1.add(v2);

			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);
			assertEquals(z1 + z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#add(double, double, double)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "add", args = { double.class, double.class, double.class })
	public void testAdd_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7125211715849393284L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = v1.add(x2, y2, z2);

			// Tests {@linkplain Vector3D#add(double, double, double)}
			assertEquals(x1 + x2, v2.getX(), 0);
			assertEquals(y1 + y2, v2.getY(), 0);
			assertEquals(z1 + z2, v2.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#Vector3D()}
	 */
	@Test
	@UnitTestConstructor(target = Vector3D.class, args = {})
	public void testConstructors_Empty() {

		Vector3D v = new Vector3D();

		assertEquals(0, v.getX(), 0);
		assertEquals(0, v.getY(), 0);
		assertEquals(0, v.getZ(), 0);
	}

	/**
	 * Tests {@linkplain Vector3D#Vector3D(MutableVector3D)}
	 */
	@Test
	@UnitTestConstructor(target = Vector3D.class, args = { MutableVector3D.class })
	public void testConstructors_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6902413344220415519L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			Vector3D v2 = new Vector3D(v);

			assertEquals(x, v2.getX(), 0);
			assertEquals(y, v2.getY(), 0);
			assertEquals(z, v2.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#Vector3D(Vector3D)}
	 */
	@Test
	@UnitTestConstructor(target = Vector3D.class, args = { Vector3D.class })
	public void testConstructors_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5764958408005452265L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			Vector3D v2 = new Vector3D(v);

			assertEquals(x, v2.getX(), 0);
			assertEquals(y, v2.getY(), 0);
			assertEquals(z, v2.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#Vector3D(double, double, double)}
	 */
	@Test
	@UnitTestConstructor(target = Vector3D.class, args = { double.class, double.class, double.class })
	public void testConstructors_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4063392888561806538L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
			assertEquals(z, v.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#addScaled(Vector3D, double)}
	 * 
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "addScaled", args = { Vector3D.class, double.class })
	public void testAddScaled() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3690133693531615503L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			v1.addScaled(v2, scale);

			// Tests {@linkplain Vector3D#addScaled(MutableVector3D, double)}
			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
			assertEquals(z1 + z2 * scale, v1.getZ(), 0);

			v1.assign(x1, y1, z1);
			Vector3D v3 = new Vector3D(x2, y2, z2);
			scale = randomGenerator.nextDouble() * 1000 - 500;
			v1.addScaled(v3, scale);

			// Tests {@linkplain Vector3D#addScaled(Vector3D, double)}
			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
			assertEquals(z1 + z2 * scale, v1.getZ(), 0);
		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector3D#angle(Vector3D)}
	 * 
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "angle", args = { Vector3D.class })
	public void testAngle() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(951350942348320391L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double length1 = FastMath.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
			double length2 = FastMath.sqrt(x2 * x2 + y2 * y2 + z2 * z2);
			double dotProduct = x1 * x2 + y1 * y2 + z1 * z2;
			double cosTheta = dotProduct / (length1 * length2);
			double expectedValue = FastMath.acos(cosTheta);

			double actualValue = v1.angle(v2);

			assertEquals(expectedValue, actualValue, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#cross(Vector3D))}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "cross", args = { Vector3D.class })
	public void testCross() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6840870992153579167L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);
			v2.cross(v1);

			double x3 = y2 * z1 - y1 * z2;
			double y3 = x1 * z2 - x2 * z1;
			double z3 = x2 * y1 - x1 * y2;

			assertEquals(x3, v2.getX(), TOLERANCE);
			assertEquals(y3, v2.getY(), TOLERANCE);
			assertEquals(z3, v2.getZ(), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#distanceTo(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "distanceTo", args = { Vector3D.class })
	public void testDistanceTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7530267238221574008L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));

			double actual = v2.distanceTo(v1);

			// Tests {@linkplain MutableVector3D#distanceTo(MutableVector3D)}
			assertEquals(expected, actual, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#dot(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "dot", args = { Vector3D.class })
	public void testDot() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7381648601624753148L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double expected = x1 * x2 + y1 * y2 + z1 * z2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#get(int)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "get", args = { int.class })
	public void testGet() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5822494443076549477L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			assertEquals(x, v.get(0), 0);
			assertEquals(y, v.get(1), 0);
			assertEquals(z, v.get(2), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#getX()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "getX", args = {})
	public void testGetX() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5619096689466232458L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			assertEquals(x, v.getX(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#getY()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "getY", args = {})
	public void testGetY() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9011643938864970700L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#getZ()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "getZ", args = {})
	public void testGetZ() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8418814888059666319L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			assertEquals(z, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#scale(double)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "scale", args = { double.class })
	public void testScale() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(788959907719176256L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			double scalar = randomGenerator.nextDouble() * 1000 - 500;

			v = v.scale(scalar);

			assertEquals(x * scalar, v.getX(), 0);
			assertEquals(y * scalar, v.getY(), 0);
			assertEquals(z * scalar, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#sub(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "sub", args = { Vector3D.class })
	public void testSub_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7254476776881600886L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			v1 = v1.sub(v2);

			// Tests {@linkplain Vector3D#sub(MutableVector3D)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);

			v1 = new Vector3D(x1, y1, z1);
			v1 = v1.sub(x2, y2, z2);

			// Tests {@linkplain Vector3D#sub(double, double, double)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#sub(double, double, double)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "sub", args = { double.class, double.class, double.class })
	public void testSub_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6577953305162290972L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			v1 = v1.sub(v2);

			// Tests {@linkplain Vector3D#sub(MutableVector3D)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);

			v1 = new Vector3D(x1, y1, z1);
			v1 = v1.sub(x2, y2, z2);

			// Tests {@linkplain Vector3D#sub(double, double, double)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#isInfinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "isInfinite", args = {})
	public void testIsInfinite() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7403620333466856112L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			assertFalse(v.isInfinite());

			v = new Vector3D(Double.POSITIVE_INFINITY, y, z);
			assertTrue(v.isInfinite());

			v = new Vector3D(Double.NEGATIVE_INFINITY, y, z);
			assertTrue(v.isInfinite());

			v = new Vector3D(x, Double.POSITIVE_INFINITY, z);
			assertTrue(v.isInfinite());

			v = new Vector3D(x, Double.NEGATIVE_INFINITY, z);
			assertTrue(v.isInfinite());

			v = new Vector3D(x, y, Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v = new Vector3D(x, y, Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());
		}
	}

	/**
	 * Tests {@linkplain Vector3D#isNaN()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "isNaN", args = {})
	public void testIsNaN() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2792478867330703600L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v = new Vector3D(x, y, z);
			assertFalse(v.isNaN());

			v = new Vector3D(Double.NaN, y, z);
			assertTrue(v.isNaN());

			v = new Vector3D(x, Double.NaN, z);
			assertTrue(v.isNaN());

			v = new Vector3D(x, y, Double.NaN);
			assertTrue(v.isNaN());
		}
	}

	/**
	 * Tests {@linkplain Vector3D#isFinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "isFinite", args = {})
	public void testIsFinite() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5468039318586500858L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			assertTrue(v.isFinite());

			v = new Vector3D(Double.NaN, y, z);
			assertFalse(v.isFinite());

			v = new Vector3D(Double.NEGATIVE_INFINITY, y, z);
			assertFalse(v.isFinite());

			v = new Vector3D(Double.POSITIVE_INFINITY, y, z);
			assertFalse(v.isFinite());

			v = new Vector3D(x, Double.NaN, z);
			assertFalse(v.isFinite());

			v = new Vector3D(x, Double.NEGATIVE_INFINITY, z);
			assertFalse(v.isFinite());

			v = new Vector3D(x, Double.POSITIVE_INFINITY, z);
			assertFalse(v.isFinite());

			v = new Vector3D(x, y, Double.NaN);
			assertFalse(v.isFinite());

			v = new Vector3D(x, y, Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v = new Vector3D(x, y, Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

		}
	}

	/**
	 * Tests {@linkplain Vector3D#squareDistanceTo(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "squareDistanceTo", args = { Vector3D.class })
	public void testSquareDistanceTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4408285762517228447L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);

			double actual = v2.squareDistanceTo(v1);

			assertEquals(expected, actual, 0);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#reverse()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "reverse", args = {})
	public void testReverse() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1607695966329330649L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			v = v.reverse();

			assertEquals(-x, v.getX(), 0);
			assertEquals(-y, v.getY(), 0);
			assertEquals(-z, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#length()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "length", args = {})
	public void testLength() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(892356071941813021L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			double expectedLength = FastMath.sqrt(x * x + y * y + z * z);
			double actualLength = v.length();

			assertEquals(expectedLength, actualLength, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#squareLength()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "squareLength", args = {})
	public void testSquareLength() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5116359435826650990L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			double expectedLength = x * x + y * y + z * z;
			double actualLength = v.squareLength();

			assertEquals(expectedLength, actualLength, 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#toArray()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "toArray", args = {})
	public void testToArray() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1585465023735434312L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			double[] array = v.toArray();

			assertEquals(v.getX(), array[0], 0);
			assertEquals(v.getY(), array[1], 0);
			assertEquals(v.getZ(), array[2], 0);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#normalize()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "normalize", args = {})
	public void testNormalize() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7753068915847520635L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);
			v = v.normalize();

			assertEquals(1, v.length(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain Vector3D#isNormal()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "isNormal", args = {})
	public void testIsNormal() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8273142075158529624L);

		int activeTestCount = 0;
		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			if (FastMath.abs(v.length() - 1) > Vector3D.NORMAL_LENGTH_TOLERANCE) {
				v = v.normalize();
				assertTrue(v.isNormal());
				activeTestCount++;

				Vector3D u = new Vector3D(v);
				u = u.scale(1 - 2 * Vector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());

				u = new Vector3D(v);
				u = u.scale(1 + 2 * Vector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());
			}
		}
		assertTrue(activeTestCount > 90);
	}

	/**
	 * Tests {@linkplain Vector3D#isPerpendicularTo(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "isPerpendicularTo", args = { Vector3D.class })
	public void testIsPerpendicularTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7136200979729764353L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			Vector3D v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.PI / 2 - 2 * Vector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.PI / 2 + 2 * Vector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}

	}

	/**
	 * Tests {@linkplain Vector3D#equals(Object)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(713907792984443541L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x, y, z);

			Vector3D v2 = new Vector3D(x, y, z);

			Vector3D v3 = new Vector3D(x, y, z);

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
	 * Tests {@linkplain Vector3D#hashCode()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1626510424735965103L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v1 = new Vector3D(x, y, z);

			Vector3D v2 = new Vector3D(x, y, z);

			assertEquals(v1.hashCode(), v2.hashCode());

		}
	}

	/**
	 * Tests {@linkplain Vector3D#toString()}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8190009794791481605L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			String expected = "Vector3D [x=" + x + ", y=" + y + ", z=" + z + "]";

			String actual = v.toString();

			assertEquals(expected, actual);
		}
	}

	/**
	 * Tests {@linkplain Vector3D#rotateAbout(Vector3D, double)}
	 * 
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "rotateAbout", args = { Vector3D.class, double.class })
	public void testRotateAbout() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1807065951741732731L);

		for (int i = 0; i < 100; i++) {

			// v1 will be used as a rotator, so we ensure that it has a
			// reasonable length
			Vector3D v1 = new Vector3D();
			while (v1.length() < 0.0000001) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				double z1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector3D(x1, y1, z1);
			}

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			Vector3D v = new Vector3D(v2);

			v = v.rotateAbout(v1, theta);

			// v2 under rotation should have its length preserved
			assertEquals(v2.length(), v.length(), TOLERANCE);

			// v2 under rotation should have its angle to v1 preserved
			assertEquals(v2.angle(v1), v.angle(v1), TOLERANCE);

			// v2 when rotated back should return to its original position
			v = v.rotateAbout(v1, -theta);
			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}
	}

	/**
	 * 
	 * Tests {@linkplain Vector3D#rotateToward(Vector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = Vector3D.class, name = "rotateToward", args = { Vector3D.class, double.class })
	public void testRotateToward() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(540427496183068832L);

		for (int i = 0; i < 100; i++) {

			// v1 will be used as a rotator, so we ensure that it has a
			// reasonable length
			Vector3D v1 = new Vector3D();
			while (v1.length() < 0.0000001) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				double z1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector3D(x1, y1, z1);
			}

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			Vector3D v = new Vector3D(v2);

			v = v.rotateToward(v1, theta);
			double angle = v2.angle(v1);

			// v2 under rotation should have its length preserved
			assertEquals(v2.length(), v.length(), TOLERANCE);

			// v2 under rotation should have its angle to v1 changed by theta
			double expectedAngle = theta - angle;
			while (expectedAngle < 0) {
				expectedAngle += FastMath.PI * 2;
			}
			while (expectedAngle > 2 * FastMath.PI) {
				expectedAngle -= 2 * FastMath.PI;
			}
			while (expectedAngle > FastMath.PI) {
				expectedAngle = 2 * FastMath.PI - expectedAngle;
			}

			double actualAngle = v.angle(v1);

			assertEquals(expectedAngle, actualAngle, TOLERANCE);

			// v2 when rotated back should return to its original position
			Vector3D a = new Vector3D(v2);
			a = a.cross(v1);

			Vector3D b = new Vector3D(v);
			b = b.cross(v1);

			if (a.dot(b) > 0) {
				v = v.rotateToward(v1, -theta);
			} else {
				v = v.rotateToward(v1, theta);
			}

			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}
	}

}
