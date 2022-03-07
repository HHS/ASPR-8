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
 * Test class for {@link MutableVector2D}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableVector2D.class)
public class AT_MutableVector2D {

	private static final double TOLERANCE = 0.000001;



	/**
	 * Tests {@linkplain MutableVector2D#add(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { MutableVector2D.class })
	public void testAdd_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(747753114709649380L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			v1.add(v2);

			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);

			v1 = new MutableVector2D(x1, y1);
			v1.add(x2, y2);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#add(double, double)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { double.class, double.class })
	public void testAdd_DoubleDouble() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1765120437173433394L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new MutableVector2D(x1, y1);
			v1.add(x2, y2);

			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#add(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "add", args = { Vector2D.class })
	public void testAdd_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1775748119067564239L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new MutableVector2D(x1, y1);
			Vector2D v3 = new Vector2D(x2, y2);
			v1.add(v3);

			// Tests {@linkplain MutableVector2D#add(Vector2D)}
			assertEquals(x1 + x2, v1.getX(), 0);
			assertEquals(y1 + y2, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#Vector2D()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructors_Empty() {

		MutableVector2D v = new MutableVector2D();

		assertEquals(0, v.getX(), 0);
		assertEquals(0, v.getY(), 0);

	}

	/**
	 * Tests {@linkplain MutableVector2D#Vector2D(MutableVector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { MutableVector2D.class })
	public void testConstructors_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4953024764558110367L);

		for (int i = 0; i < 100; i++) {

			MutableVector2D v = new MutableVector2D();

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			v = new MutableVector2D(x, y);

			MutableVector2D v2 = new MutableVector2D(v);

			// Tests {@linkplain MutableVector2D#Vector2D(MutableVector2D)}
			assertEquals(v.getX(), v2.getX(), 0);
			assertEquals(v.getY(), v2.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#MutableVector2D(Vector2D)}
	 */
	@Test
	@UnitTestConstructor(args = { Vector2D.class })
	public void testConstructors_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6697539793873697342L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v3 = new Vector2D(x, y);

			MutableVector2D v = new MutableVector2D(v3);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#Vector2D(double, double)}
	 */
	@Test
	@UnitTestConstructor(args = { double.class, double.class })
	public void testConstructors_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(675407316550380164L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#addScaled(MutableVector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "addScaled", args = { MutableVector2D.class, double.class })
	public void testAddScaled_MutableVector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6215331323321750938L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			v1.addScaled(v2, scale);

			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#addScaled(Vector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "addScaled", args = { Vector2D.class, double.class })
	public void testAddScaled_Vector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1981525905399256435L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			double scale = randomGenerator.nextDouble() * 1000 - 500;

			v1.addScaled(v2, scale);

			assertEquals(x1 + x2 * scale, v1.getX(), 0);
			assertEquals(y1 + y2 * scale, v1.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#angle(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "angle", args = { Vector2D.class })
	public void testAngle_Vector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3234050965506605506L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

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
	 * Tests {@linkplain MutableVector2D#angle(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "angle", args = { MutableVector2D.class })
	public void testAngle_MutableVector2D() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6724319230891603465L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

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
	 * Tests {@linkplain MutableVector2D#assign(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "assign", args = { MutableVector2D.class })
	public void testAssign_MutableVector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4060103816892775107L);

		// Tests {@linkplain MutableVector2D#assign(MutableVector2D)}
		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);
			v2.assign(v1);

			assertEquals(x1, v2.getX(), TOLERANCE);
			assertEquals(y1, v2.getY(), TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#assign(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "assign", args = { Vector2D.class })
	public void testAssign_Vector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5850595925938665305L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);
			v2.assign(v1);

			assertEquals(x1, v2.getX(), TOLERANCE);
			assertEquals(y1, v2.getY(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#assign(double, double)}
	 */
	@Test
	@UnitTestMethod(name = "assign", args = { double.class, double.class })
	public void testAssign_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1722582342083801695L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1.assign(x2, y2);

			assertEquals(x2, v1.getX(), TOLERANCE);
			assertEquals(y2, v1.getY(), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#cross(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "cross", args = { MutableVector2D.class })
	public void testCross_MutableVector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5493200042200712993L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);
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
	 * Tests {@linkplain MutableVector2D#cross(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "cross", args = { Vector2D.class })
	public void testCross_Vector() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2743696133291921471L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);
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
	 * Tests {@linkplain MutableVector2D#distanceTo(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "distanceTo", args = { MutableVector2D.class })
	public void testDistanceTo_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4615581309644565423L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

			double actual = v2.distanceTo(v1);

			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#distanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "distanceTo", args = { Vector2D.class })
	public void testDistanceTo_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7649019140878027553L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = FastMath.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

			double actual = v2.distanceTo(v1);

			assertEquals(expected, actual, TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#dot(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "dot", args = { MutableVector2D.class })
	public void testDot_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5487028293556391537L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = x1 * x2 + y1 * y2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);

		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#dot(Vector2D))}
	 */
	@Test
	@UnitTestMethod(name = "dot", args = { Vector2D.class })
	public void testDot_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7050351241341904105L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = x1 * x2 + y1 * y2;

			double actual = v2.dot(v1);

			assertEquals(expected, actual, TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#zero()}
	 */
	@Test
	@UnitTestMethod(name = "zero", args = {})
	public void testZero() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6725468903284973938L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			assertEquals(x, v.getX(), 0);
			assertEquals(y, v.getY(), 0);

			v.zero();

			assertEquals(0, v.getX(), 0);
			assertEquals(0, v.getY(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#get(int)}
	 */
	@Test
	@UnitTestMethod(name = "get", args = { int.class })
	public void testGet() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5651219733795356716L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			assertEquals(x, v.get(0), 0);
			assertEquals(y, v.get(1), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#getX()}
	 */
	@Test
	@UnitTestMethod(name = "getX", args = {})
	public void testGetX() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2694122746003169803L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			assertEquals(x, v.getX(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#getY()}
	 */
	@Test
	@UnitTestMethod(name = "getY", args = {})
	public void testGetY() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(452804940306359312L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#scale(double)}
	 */
	@Test
	@UnitTestMethod(name = "scale", args = { double.class })
	public void testScale() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7342985176084803934L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			double scalar = randomGenerator.nextDouble() * 1000 - 500;

			v.scale(scalar);

			assertEquals(x * scalar, v.getX(), 0);
			assertEquals(y * scalar, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#setX(double)}
	 */
	@Test
	@UnitTestMethod(name = "setX", args = { double.class })
	public void testSetX() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7594665551747971351L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			double newX = randomGenerator.nextDouble() * 1000 - 500;

			v.setX(newX);

			assertEquals(newX, v.getX(), 0);
			assertEquals(y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#setY(double)}
	 */
	@Test
	@UnitTestMethod(name = "setY", args = { double.class })
	public void testSetY() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3745398517861233669L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			double newY = randomGenerator.nextDouble() * 1000 - 500;

			v.setY(newY);

			assertEquals(x, v.getX(), 0);
			assertEquals(newY, v.getY(), 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#sub(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { MutableVector2D.class })
	public void testSub_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6934284701754808487L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			v1.sub(v2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#sub(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { Vector2D.class })
	public void testSub_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5759597061407181797L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v2 = new Vector2D(x2, y2);

			v1.sub(v2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#sub(double, double)}
	 */
	@Test
	@UnitTestMethod(name = "sub", args = { double.class, double.class })
	public void testSub_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4488004263175079178L);

		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			v1 = new MutableVector2D(x1, y1);
			v1.sub(x2, y2);

			assertEquals(x1 - x2, v1.getX(), 0);
			assertEquals(y1 - y2, v1.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#isInfinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isInfinite", args = {})
	public void testIsInfinite() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(852808991624264230L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v = new MutableVector2D();

			v.assign(x, y);
			assertFalse(v.isInfinite());
			v.setX(Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y);
			assertFalse(v.isInfinite());
			v.setX(Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y);
			assertFalse(v.isInfinite());
			v.setY(Double.POSITIVE_INFINITY);
			assertTrue(v.isInfinite());

			v.assign(x, y);
			assertFalse(v.isInfinite());
			v.setY(Double.NEGATIVE_INFINITY);
			assertTrue(v.isInfinite());
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#isNaN()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isNaN", args = {})
	public void testIsNaN() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6344060856602552434L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v = new MutableVector2D();

			v.assign(x, y);
			assertFalse(v.isNaN());
			v.setX(Double.NaN);
			assertTrue(v.isNaN());

			v.assign(x, y);
			assertFalse(v.isNaN());
			v.setY(Double.NaN);
			assertTrue(v.isNaN());
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#isFinite()}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "isFinite", args = {})
	public void testIsFinite() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8677841486644718998L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v = new MutableVector2D();

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setX(Double.NaN);
			assertFalse(v.isFinite());

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setX(Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setX(Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setY(Double.NaN);
			assertFalse(v.isFinite());

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setY(Double.NEGATIVE_INFINITY);
			assertFalse(v.isFinite());

			v.assign(x, y);
			assertTrue(v.isFinite());
			v.setY(Double.POSITIVE_INFINITY);
			assertFalse(v.isFinite());

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#squareDistanceTo(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "squareDistanceTo", args = { MutableVector2D.class })
	public void testSquareDistanceTo_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3118694974463156175L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

			double actual = v2.squareDistanceTo(v1);

			assertEquals(expected, actual, 0);

		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#squareDistanceTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "squareDistanceTo", args = { Vector2D.class })
	public void testSquareDistanceTo_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1850329192838045338L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			Vector2D v1 = new Vector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			double expected = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

			double actual = v2.squareDistanceTo(v1);

			assertEquals(expected, actual, 0);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#reverse()}
	 */
	@Test
	@UnitTestMethod(name = "reverse", args = {})
	public void testReverse() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6922804374672425062L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			v.reverse();

			assertEquals(-x, v.getX(), 0);
			assertEquals(-y, v.getY(), 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#length()}
	 */
	@Test
	@UnitTestMethod(name = "length", args = {})
	public void testLength() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4939922046347545520L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			double expectedLength = FastMath.sqrt(x * x + y * y);
			double actualLength = v.length();

			assertEquals(expectedLength, actualLength, TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#squareLength()}
	 */
	@Test
	@UnitTestMethod(name = "squareLength", args = {})
	public void testSquareLength() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(3766758455538226142L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			double expectedLength = x * x + y * y;
			double actualLength = v.squareLength();

			assertEquals(expectedLength, actualLength, 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#toArray()}
	 */
	@Test
	@UnitTestMethod(name = "toArray", args = {})
	public void testToArray() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6552417458281120706L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			double[] array = v.toArray();

			assertEquals(v.getX(), array[0], 0);
			assertEquals(v.getY(), array[1], 0);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#normalize()}
	 */
	@Test
	@UnitTestMethod(name = "normalize", args = {})
	public void testNormalize() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4958663962485721256L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);
			v.normalize();

			assertEquals(1, v.length(), TOLERANCE);

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6650742161199839711L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x, y);

			MutableVector2D v2 = new MutableVector2D(x, y);

			MutableVector2D v3 = new MutableVector2D(x, y);

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
	 * Tests {@linkplain MutableVector2D#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(6711537897020009773L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x, y);

			MutableVector2D v2 = new MutableVector2D(x, y);

			assertEquals(v1.hashCode(), v2.hashCode());

		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7265036568767953542L);

		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			String expected = "Vector2D [x=" + x + ", y=" + y + "]";

			String actual = v.toString();

			assertEquals(expected, actual);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#rotate(double)}
	 * 
	 */
	@Test
	@UnitTestMethod(name = "rotate", args = { double.class })
	public void testRotate() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(679273489159068423L);

		for (int i = 0; i < 100; i++) {
			// ensure that v1 is not too close to a zero vector
			MutableVector2D v1 = new MutableVector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new MutableVector2D(x1, y1);
			}

			// Copy v1 and rotate the copy
			MutableVector2D v2 = new MutableVector2D(v1);
			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;
			v2.rotate(theta);

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
			v2.rotate(-theta);
			assertEquals(v1.getX(), v2.getX(), TOLERANCE);
			assertEquals(v1.getY(), v2.getY(), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain MutableVector2D#rotateToward(MutableVector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "rotateToward", args = { MutableVector2D.class, double.class })
	public void testRotateToward_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7377823496934970629L);

		for (int i = 0; i < 100; i++) {

			// Ensure that v1 is not too close to the zero vector
			MutableVector2D v1 = new MutableVector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new MutableVector2D(x1, y1);
			}

			// Ensure that v2 is not too close to the zero vector
			MutableVector2D v2 = new MutableVector2D();
			while (v2.length() < TOLERANCE) {
				double x2 = randomGenerator.nextDouble() * 1000 - 500;
				double y2 = randomGenerator.nextDouble() * 1000 - 500;
				v2 = new MutableVector2D(x2, y2);
			}

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector2D v3 = new MutableVector2D(v2);
			v3.rotateToward(v1, theta);

			// Rotation toward another vector is equivalent to plain rotation
			// with a possible sign change due to the relative orientation of
			// the two vectors.
			MutableVector2D v4 = new MutableVector2D(v2);
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
	 * Tests {@linkplain MutableVector2D#rotateToward(Vector2D, double)}
	 */
	@Test
	@UnitTestMethod(name = "rotateToward", args = { Vector2D.class, double.class })
	public void testRotateToward_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8015075822914071291L);

		for (int i = 0; i < 100; i++) {

			// Ensure that v1 is not too close to the zero vector
			Vector2D v1 = new Vector2D();
			while (v1.length() < TOLERANCE) {
				double x1 = randomGenerator.nextDouble() * 1000 - 500;
				double y1 = randomGenerator.nextDouble() * 1000 - 500;
				v1 = new Vector2D(x1, y1);
			}

			// Ensure that v2 is not too close to the zero vector
			MutableVector2D v2 = new MutableVector2D();
			while (v2.length() < TOLERANCE) {
				double x2 = randomGenerator.nextDouble() * 1000 - 500;
				double y2 = randomGenerator.nextDouble() * 1000 - 500;
				v2 = new MutableVector2D(x2, y2);
			}

			double theta = randomGenerator.nextDouble() * 2 * FastMath.PI;

			MutableVector2D v3 = new MutableVector2D(v2);
			v3.rotateToward(v1, theta);

			// Rotation toward another vector is equivalent to plain rotation
			// with a possible sign change due to the relative orientation of
			// the two vectors.
			MutableVector2D v4 = new MutableVector2D(v2);
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
	 * Tests {@linkplain MutableVector2D#isPerpendicularTo(Vector2D)}
	 */
	@Test
	@UnitTestMethod(name = "isPerpendicularTo", args = { Vector2D.class })
	public void testIsPerpendicularTo_Vector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1251112927236624067L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

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

	/**
	 * Tests {@linkplain MutableVector2D#isPerpendicularTo(MutableVector2D)}
	 */
	@Test
	@UnitTestMethod(name = "isPerpendicularTo", args = { MutableVector2D.class })
	public void testIsPerpendicularTo_MutableVector2D() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(1953671451170035329L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v2 = new MutableVector2D(x2, y2);

			MutableVector2D v3 = new MutableVector2D(v1);
			v3.rotateToward(v2, FastMath.toRadians(90));

			assertTrue(v1.isPerpendicularTo(v3));

			v3 = new MutableVector2D(v1);
			v3.rotateToward(v2, FastMath.PI / 2 - 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));

			v3 = new MutableVector2D(v1);
			v3.rotateToward(v2, FastMath.PI / 2 + 2 * MutableVector3D.PERPENDICUALR_ANGLE_TOLERANCE);
			assertFalse(v1.isPerpendicularTo(v3));
		}

	}

	/**
	 * Tests {@linkplain MutableVector2D#isNormal()}
	 */
	@Test
	@UnitTestMethod(name = "isNormal", args = {})
	public void testIsNormal() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(366173807860588361L);

		int activeTestCount = 0;
		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 1000 - 500;
			double y = randomGenerator.nextDouble() * 1000 - 500;

			MutableVector2D v = new MutableVector2D(x, y);

			if (FastMath.abs(v.length() - 1) > MutableVector3D.NORMAL_LENGTH_TOLERANCE) {
				v.normalize();
				assertTrue(v.isNormal());
				activeTestCount++;

				MutableVector2D u = new MutableVector2D(v);
				u.scale(1 - 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());

				u = new MutableVector2D(v);
				u.scale(1 + 2 * MutableVector3D.NORMAL_LENGTH_TOLERANCE);
				assertFalse(u.isNormal());
			}
		}
		assertTrue(activeTestCount > 90);
	}

	/**
	 * Tests {@linkplain MutableVector2D#perpendicularRotation(Chirality)}
	 */
	@Test
	@UnitTestMethod(name = "perpendicularRotation", args = { Chirality.class })
	public void testPerpendicularRotation() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(437585069656161491L);

		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 1000 - 500;
			double y1 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v1 = new MutableVector2D(x1, y1);

			double x2 = randomGenerator.nextDouble() * 1000 - 500;
			double y2 = randomGenerator.nextDouble() * 1000 - 500;
			MutableVector2D v2 = new MutableVector2D(x2, y2);

			v2.assign(v1);
			v2.perpendicularRotation(Chirality.LEFT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points up
			assertEquals(1, v2.cross(v1));

			v2.assign(v1);
			v2.perpendicularRotation(Chirality.RIGHT_HANDED);
			// v2 should be perpendicular to v1
			assertEquals(FastMath.PI / 2, v2.angle(v1), TOLERANCE);
			// v2 is clockwise of v1, so the cross product points down
			assertEquals(-1, v2.cross(v1));

		}

	}

}
