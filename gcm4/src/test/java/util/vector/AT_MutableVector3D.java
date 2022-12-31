package util.vector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestField;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

/**
 * Test class for {@link MutableVector3D}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableVector3D.class)
public class AT_MutableVector3D {

	private static final double TOLERANCE = 0.000001;

	@Test
	@UnitTestField(target = MutableVector3D.class, name = "NORMAL_LENGTH_TOLERANCE")
	public void testNormalLengthTolerance() {
		assertEquals(1E-13, MutableVector2D.NORMAL_LENGTH_TOLERANCE, 0);
	}

	@Test
	@UnitTestField(target = MutableVector3D.class, name = "PERPENDICULAR_ANGLE_TOLERANCE")
	public void testPerpendicularAngleTolerance() {
		assertEquals(1E-13, MutableVector2D.PERPENDICULAR_ANGLE_TOLERANCE, 0);
	}

	/**
	 * Tests {@linkplain MutableVector3D#add(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "add", args = { MutableVector3D.class })
	public void testAdd_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8344611551021569928L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);
			v1.add(v2);

			// Tests {@linkplain MutableVector3D#add(MutableVector3D)}
			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);
			assertEquals(z1 + z2, v1.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#add(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "add", args = { Vector3D.class })
	public void testAdd_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(487766521597469529L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			v1.add(v2);

			// Tests {@linkplain MutableVector3D#add(Vector3D)}
			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);
			assertEquals(z1 + z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#add(double, double, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "add", args = { double.class, double.class, double.class })
	public void testAdd_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4500585622291483492L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			v1.add(x2, y2, z2);

			// Tests {@linkplain MutableVector3D#add(double, double, double)}
			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);
			assertEquals(z1 + z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#Vector3D()}
	 */
	@Test
	@UnitTestConstructor(target = MutableVector3D.class, args = {})
	public void testConstructors_Empty() {

		MutableVector3D v = new MutableVector3D();

		assertEquals(0, v.getX(), 0);
		assertEquals(0, v.getY(), 0);
		assertEquals(0, v.getZ(), 0);
	}

	/**
	 * Tests {@linkplain MutableVector3D#Vector3D(MutableVector3D)}
	 */
	@Test
	@UnitTestConstructor(target = MutableVector3D.class, args = { MutableVector3D.class })
	public void testConstructors_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(584615761790582524L);

		for (int i = 0; i < 100; i++) {
			MutableVector3D v = new MutableVector3D();

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;
			v = new MutableVector3D(x, y, z);

			MutableVector3D v2 = new MutableVector3D(v);

			assertEquals(x, v2.getX(), 0);
			assertEquals(y, v2.getY(), 0);
			assertEquals(z, v2.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#Vector3D(Vector3D)}
	 */
	@Test
	@UnitTestConstructor(target = MutableVector3D.class, args = { Vector3D.class })
	public void testConstructors_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8962175229980057680L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v = new Vector3D(x, y, z);

			MutableVector3D v2 = new MutableVector3D(v);

			assertEquals(x, v2.getX(), 0);
			assertEquals(y, v2.getY(), 0);
			assertEquals(z, v2.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#Vector3D(double, double, double)}
	 */
	@Test
	@UnitTestConstructor(target = MutableVector3D.class, args = { double.class, double.class, double.class })
	public void testConstructors_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(110008594144948233L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
			assertEquals(z, v.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#addScaled(Vector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "addScaled", args = { Vector3D.class, double.class })
	public void testAddScaled_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2338666481384433285L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v3 = new Vector3D(x2, y2, z2);
			scale = randomGenerator.nextDouble() * 1000 - 500;
			v1.addScaled(v3, scale);

			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
			assertEquals(z1 + z2 * scale, v1.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#addScaled(MutableVector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "addScaled", args = { MutableVector3D.class, double.class })
	public void testAddScaled_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6770082568533230814L);

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

			// Tests {@linkplain MutableVector3D#addScaled(MutableVector3D,
			// double)}
			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
			assertEquals(z1 + z2 * scale, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#angle(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "angle", args = { MutableVector3D.class })
	public void testAngle_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8975718787496880209L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

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
	 * Tests {@linkplain MutableVector3D#angle(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "angle", args = { Vector3D.class })
	public void testAngle_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5742149925489685535L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			double length1 = FastMath.sqrt(x1 * x1 + y1 * y1 + z1 * z1);
			double length2 = FastMath.sqrt(x2 * x2 + y2 * y2 + z2 * z2);
			double dotProduct = x1 * x2 + y1 * y2 + z1 * z2;
			double cosTheta = dotProduct / (length1 * length2);
			double expectedValue = FastMath.acos(cosTheta);

			Vector3D v3 = new Vector3D(x2, y2, z2);
			double actualValue = v1.angle(v3);
			assertEquals(expectedValue, actualValue, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#assign(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "assign", args = { MutableVector3D.class })
	public void testAssign_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2514541920498591972L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);
			v2.assign(v1);

			assertEquals(x1, v2.getX(), TOLERANCE);
			assertEquals(y1, v2.getY(), TOLERANCE);
			assertEquals(z1, v2.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#assign(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "assign", args = { Vector3D.class })
	public void testAssign_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3353166711276217630L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v3 = new Vector3D(x2, y2, z2);
			v1.assign(v3);

			assertEquals(x2, v1.getX(), TOLERANCE);
			assertEquals(y2, v1.getY(), TOLERANCE);
			assertEquals(z2, v1.getZ(), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#assign(double, double, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "assign", args = { double.class, double.class, double.class })
	public void testAssign_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2947081292012315306L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			v1.assign(x2, y2, z2);

			assertEquals(x2, v1.getX(), TOLERANCE);
			assertEquals(y2, v1.getY(), TOLERANCE);
			assertEquals(z2, v1.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#cross(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "cross", args = { MutableVector3D.class })
	public void testCross_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6476733743808996150L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);
			v2.cross(v1);

			double x3 = y2 * z1 - y1 * z2;
			double y3 = x1 * z2 - x2 * z1;
			double z3 = x2 * y1 - x1 * y2;

			// Tests {@linkplain MutableVector3D#cross(MutableVector3D)}
			assertEquals(x3, v2.getX(), TOLERANCE);
			assertEquals(y3, v2.getY(), TOLERANCE);
			assertEquals(z3, v2.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#cross(Vector3D))}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "cross", args = { Vector3D.class })
	public void testCross_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1707507467350285888L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector3D v3 = new Vector3D(x2, y2, z2);

			double x3 = y1 * z2 - y2 * z1;
			double y3 = x2 * z1 - x1 * z2;
			double z3 = x1 * y2 - x2 * y1;

			v1.cross(v3);

			assertEquals(x3, v1.getX(), TOLERANCE);
			assertEquals(y3, v1.getY(), TOLERANCE);
			assertEquals(z3, v1.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#distanceTo(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "distanceTo", args = { MutableVector3D.class })
	public void testDistanceTo_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(459371333183410198L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));

			double actual = v2.distanceTo(v1);

			// Tests {@linkplain MutableVector3D#distanceTo(MutableVector3D)}
			assertEquals(expected, actual, TOLERANCE);

			Vector3D v3 = new Vector3D(x1, y1, z1);
			actual = v2.distanceTo(v3);

			// Tests {@linkplain MutableVector3D#distanceTo(Vector3D)}
			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#distanceTo(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "distanceTo", args = { Vector3D.class })
	public void testDistanceTo_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4822545293429239908L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2));

			double actual = v2.distanceTo(v1);

			// Tests {@linkplain MutableVector3D#distanceTo(MutableVector3D)}
			assertEquals(expected, actual, TOLERANCE);

			Vector3D v3 = new Vector3D(x1, y1, z1);
			actual = v2.distanceTo(v3);

			// Tests {@linkplain MutableVector3D#distanceTo(Vector3D)}
			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#dot(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "dot", args = { Vector3D.class })
	public void testDot_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5394780683515263533L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;
			Vector3D v2 = new Vector3D(x2, y2, z2);

			double expected = x1 * x2 + y1 * y2 + z1 * z2;

			double actual = v1.dot(v2);

			assertEquals(expected, actual, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#dot(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "dot", args = { MutableVector3D.class })
	public void testDot_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8651152995066569737L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double expected = x1 * x2 + y1 * y2 + z1 * z2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#zero()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "zero", args = {})
	public void testZero() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2385233064662346292L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
			assertEquals(z, v.getZ(), 0);

			v.zero();

			assertEquals(0, v.getX(), 0);
			assertEquals(0, v.getY(), 0);
			assertEquals(0, v.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#get(int)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "get", args = { int.class })
	public void testGet() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3920072590253396227L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(x, v.get(0), 0);
			assertEquals(y, v.get(1), 0);
			assertEquals(z, v.get(2), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#getX()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "getX", args = {})
	public void testGetX() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2879010938976412896L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(x, v.getX(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#getY()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "getY", args = {})
	public void testGetY() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7608232687910162616L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#getZ()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "getZ", args = {})
	public void testGetZ() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1724897021905892388L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			assertEquals(z, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#scale(double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "scale", args = { double.class })
	public void testScale() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(306065735500287558L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			double scalar = randomGenerator.nextDouble() * 1000 - 500;

			v.scale(scalar);

			assertEquals(x * scalar, v.getX(), 0);
			assertEquals(y * scalar, v.getY(), 0);
			assertEquals(z * scalar, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#setX(double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "setX", args = { double.class })
	public void testSetX() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5685640957927601005L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			double newX = randomGenerator.nextDouble() * 1000 - 500;

			v.setX(newX);

			assertEquals(newX, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
			assertEquals(z, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#setY(double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "setY", args = { double.class })
	public void testSetY() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9071265328505700087L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			double newY = randomGenerator.nextDouble() * 1000 - 500;

			v.setY(newY);

			assertEquals(x, v.getX(), 0);
			assertEquals(newY, v.getY(), 0);
			assertEquals(z, v.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#setZ(double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "setZ", args = { double.class })
	public void testSetZ() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4186928576633187463L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			double newZ = randomGenerator.nextDouble() * 1000 - 500;

			v.setZ(newZ);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
			assertEquals(newZ, v.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#sub(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "sub", args = { MutableVector3D.class })
	public void testSub_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2474685339056144518L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			v1.sub(v2);

			// Tests {@linkplain MutableVector3D#sub(MutableVector3D)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#sub(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "sub", args = { Vector3D.class })
	public void testSub_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4393443088953985351L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new MutableVector3D(x1, y1, z1);
			Vector3D v2 = new Vector3D(x2, y2, z2);
			v1.sub(v2);

			// Tests {@linkplain MutableVector3D#sub(Vector3D)}
			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#sub(double, double, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "sub", args = { double.class, double.class, double.class })
	public void testSub_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1956097062031880352L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			v1.sub(x2, y2, z2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
			assertEquals(z1 - z2, v1.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#isInfinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isInfinite", args = {})
	public void testIsInfinite() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1333539969014219742L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v = new MutableVector3D();

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setX(Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setX(Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setY(Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setY(Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setZ(Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y, z);
			assertFalse(v.isInfinite());
			v.setZ(Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#isNaN()}
	 * 
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isNaN", args = {})
	public void testIsNaN() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3604098144852879234L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v = new MutableVector3D();

			v.assign(x, y, z);
			assertFalse(v.isNaN());
			v.setX(Double.NaN);
			assertTrue(v.isNaN());

			v.assign(x, y, z);
			assertFalse(v.isNaN());
			v.setY(Double.NaN);
			assertTrue(v.isNaN());

			v.assign(x, y, z);
			assertFalse(v.isNaN());
			v.setZ(Double.NaN);
			assertTrue(v.isNaN());

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#isFinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isFinite", args = {})
	public void testIsFinite() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2810214958316938556L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector3D v = new MutableVector3D();

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setX(Double.NaN);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setX(Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setX(Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setY(Double.NaN);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setY(Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setY(Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setZ(Double.NaN);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setZ(Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y, z);
			assertTrue(v.isFinite());
			v.setZ(Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#squareDistanceTo(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "squareDistanceTo", args = { MutableVector3D.class })
	public void testSquareDistanceTo_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8371430877204504151L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);

			double actual = v2.squareDistanceTo(v1);

			// Tests {@linkplain
			// MutableVector3D#squareDistanceTo(MutableVector3D)}
			assertEquals(expected, actual, 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#squareDistanceTo(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "squareDistanceTo", args = { Vector3D.class })
	public void testSquareDistanceTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9067451765919254892L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2) + (z1 - z2) * (z1 - z2);

			Vector3D v3 = new Vector3D(v2);
			double actual = v1.squareDistanceTo(v3);

			// Tests {@linkplain MutableVector3D#squareDistanceTo(Vector3D)}
			assertEquals(expected, actual, 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#reverse()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "reverse", args = {})
	public void testReverse() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(423952531786020950L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			v.reverse();

			assertEquals(-x, v.getX(), 0);
			assertEquals(-y, v.getY(), 0);
			assertEquals(-z, v.getZ(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#length()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "length", args = {})
	public void testLength() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7468957433345943292L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			double expectedLength = FastMath.sqrt(x * x + y * y + z * z);
			double actualLength = v.length();

			assertEquals(expectedLength, actualLength, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#squareLength()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "squareLength", args = {})
	public void testSquareLength() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4345370822530975467L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			double expectedLength = x * x + y * y + z * z;
			double actualLength = v.squareLength();

			assertEquals(expectedLength, actualLength, 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#toArray()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "toArray", args = {})
	public void testToArray() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2523438676987919308L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			double[] array = v.toArray();

			assertEquals(v.getX(), array[0], 0);
			assertEquals(v.getY(), array[1], 0);
			assertEquals(v.getZ(), array[2], 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#normalize()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "normalize", args = {})
	public void testNormalize() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7788343538322017822L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);
			v.normalize();

			assertEquals(1, v.length(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#isNormal()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isNormal", args = {})
	public void testIsNormal() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5620260804759666952L);

		int activeTestCount = 0;
		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			if (FastMath.abs(v.length() - 1) > MutableVector3D.NORMAL_LENGTH_TOLERANCE) {
				v.normalize();
				assertTrue(v.isNormal());
				activeTestCount++;

				MutableVector3D u = new MutableVector3D(v);
				u.scale(1 - 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());

				u = new MutableVector3D(v);
				u.scale(1 + 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());
			}
		}
		assertTrue(activeTestCount > 90);
	}

	/**
	 * Tests {@linkplain MutableVector3D#isPerpendicularTo(MutableVector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isPerpendicularTo", args = { MutableVector3D.class })
	public void testIsPerpendicularTo_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3973464373436914741L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			MutableVector3D v3 = new MutableVector3D(v1);
			v3.rotateToward(v2, FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new MutableVector3D(v1);
			v3.rotateToward(v2, FastMath.PI / 2 - 2 * MutableVector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new MutableVector3D(v1);
			v3.rotateToward(v2, FastMath.PI / 2 + 2 * MutableVector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}

	}

	/**
	 * Tests {@linkplain MutableVector3D#isPerpendicularTo(Vector3D)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "isPerpendicularTo", args = { Vector3D.class })
	public void testIsPerpendicularTo_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3455017692650645966L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			double z1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x1, y1, z1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			Vector3D v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.PI / 2 - 2 * MutableVector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new Vector3D(v1);
			v3 = v3.rotateToward(new Vector3D(v2), FastMath.PI / 2 + 2 * MutableVector3D.PERPENDICULAR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}

	}

	/**
	 * Tests {@linkplain MutableVector3D#equals(Object)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3526185731258727774L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x, y, z);

			MutableVector3D v2 = new MutableVector3D(x, y, z);

			MutableVector3D v3 = new MutableVector3D(x, y, z);

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
	 * Tests {@linkplain MutableVector3D#hashCode()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8254478139726956208L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v1 = new MutableVector3D(x, y, z);

			MutableVector3D v2 = new MutableVector3D(x, y, z);

			assertEquals(v1.hashCode(), v2.hashCode());

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#toString()}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3891477282762024709L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			double z = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v = new MutableVector3D(x, y, z);

			String expected = "Vector3D [x=" + x + ", y=" + y + ", z=" + z + "]";

			String actual = v.toString();

			assertEquals(expected, actual);
		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#rotateAbout(MutableVector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "rotateAbout", args = { MutableVector3D.class, double.class })
	public void testRotateAbout_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3529177607589452768L);
		for (int i = 0; i < 100; i++) {

			// v1 will be used as a rotator, so we ensure that it has a
			// reasonable length
			MutableVector3D v1 = new MutableVector3D();
			while (v1.length() < 0.0000001) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				double z1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new MutableVector3D(x1, y1, z1);
			}

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector3D v = new MutableVector3D(v2);

			v.rotateAbout(v1, theta);

			// v2 under rotation should have its length preserved
			assertEquals(v2.length(), v.length(), TOLERANCE);

			// v2 under rotation should have its angle to v1 preserved
			assertEquals(v2.angle(v1), v.angle(v1), TOLERANCE);

			// v2 when rotated back should return to its original position
			v.rotateAbout(v1, -theta);
			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain MutableVector3D#rotateAbout(Vector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "rotateAbout", args = { Vector3D.class, double.class })
	public void testRotateAbout_Vector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1368205874815878591L);
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

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector3D v = new MutableVector3D(v2);

			v.rotateAbout(v1, theta);

			// v2 under rotation should have its length preserved
			assertEquals(v2.length(), v.length(), TOLERANCE);

			// v2 under rotation should have its angle to v1 preserved
			assertEquals(v2.angle(v1), v.angle(v1), TOLERANCE);

			// v2 when rotated back should return to its original position
			v.rotateAbout(v1, -theta);
			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#rotateToward(Vector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "rotateToward", args = { Vector3D.class, double.class })
	public void testRotateToward() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5587152698910126450L);

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

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector3D v = new MutableVector3D(v2);

			v.rotateToward(v1, theta);
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
			MutableVector3D a = new MutableVector3D(v2);
			a.cross(v1);

			MutableVector3D b = new MutableVector3D(v);
			b.cross(v1);

			if (a.dot(b) > 0) {
				v.rotateToward(v1, -theta);
			} else {
				v.rotateToward(v1, theta);
			}

			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector3D#rotateToward(MutableVector3D, double)}
	 */
	@Test
	@UnitTestMethod(target = MutableVector3D.class, name = "rotateToward", args = { MutableVector3D.class, double.class })
	public void testRotateToward_MutableVector3D() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6289030470744653680L);

		for (int i = 0; i < 100; i++) {

			// v1 will be used as a rotator, so we ensure that it has a
			// reasonable length
			MutableVector3D v1 = new MutableVector3D();
			while (v1.length() < 0.0000001) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				double z1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new MutableVector3D(x1, y1, z1);
			}

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			double z2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector3D v2 = new MutableVector3D(x2, y2, z2);

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector3D v = new MutableVector3D(v2);

			v.rotateToward(v1, theta);
			double angle = v1.angle(v2);

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
			MutableVector3D a = new MutableVector3D(v2);
			a.cross(v1);

			MutableVector3D b = new MutableVector3D(v);
			b.cross(v1);

			if (a.dot(b) > 0) {
				v.rotateToward(v1, -theta);
			} else {
				v.rotateToward(v1, theta);
			}

			assertEquals(v2.getX(), v.getX(), TOLERANCE);
			assertEquals(v2.getY(), v.getY(), TOLERANCE);
			assertEquals(v2.getZ(), v.getZ(), TOLERANCE);

		}

	}

}
