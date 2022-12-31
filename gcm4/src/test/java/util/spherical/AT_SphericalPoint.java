package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.vector.MutableVector3D;
import util.vector.Vector3D;

/**
 * Test class for {@link SphericalPoint}
 * 
 * @author Shawn Hatch
 *
 */
public class AT_SphericalPoint {

	/**
	 * Tests {@link SphericalPoint#SphericalPoint(MutableVector3D)}
	 */
	@Test
	@UnitTestConstructor(target = SphericalPoint.class, args = { MutableVector3D.class })
	public void testConstructors_MutableVector3D() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8382813671696213293L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			double length = FastMath.sqrt(x * x + y * y + z * z);
			SphericalPoint sphericalPoint = new SphericalPoint(new MutableVector3D(x, y, z));

			assertTrue(FastMath.abs(x / length - sphericalPoint.getPosition().getX()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(y / length - sphericalPoint.getPosition().getY()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(z / length - sphericalPoint.getPosition().getZ()) < Vector3D.NORMAL_LENGTH_TOLERANCE);

		}

	}

	/**
	 * Tests {@link SphericalPoint#SphericalPoint(Vector3D)}
	 */
	@Test
	@UnitTestConstructor(target = SphericalPoint.class, args = { Vector3D.class })
	public void testConstructors_Vector3D() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1115082964305662816L);

		for (int i = 0; i < 100; i++) {
			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			double length = FastMath.sqrt(x * x + y * y + z * z);
			SphericalPoint sphericalPoint = new SphericalPoint(new Vector3D(x, y, z));

			assertTrue(FastMath.abs(x / length - sphericalPoint.getPosition().getX()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(y / length - sphericalPoint.getPosition().getY()) < Vector3D.NORMAL_LENGTH_TOLERANCE);
			assertTrue(FastMath.abs(z / length - sphericalPoint.getPosition().getZ()) < Vector3D.NORMAL_LENGTH_TOLERANCE);

			assertThrows(MalformedSphericalPointException.class, () -> new SphericalPoint(new Vector3D(0, 0, 0)));
			assertThrows(MalformedSphericalPointException.class, () -> new SphericalPoint(new MutableVector3D(0, 0, 0)));

		}

		// precondition tests

	}

	/**
	 * Tests {@link SphericalPoint#getPosition()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPoint.class, name = "getPosition", args = {})
	public void testGetPosition() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4303335398336843747L);
		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v = new Vector3D(x, y, z);
			SphericalPoint sphericalPoint = new SphericalPoint(v);
			v = v.normalize();
			Vector3D u = sphericalPoint.getPosition();
			assertEquals(v, u);
		}

	}

	/**
	 * Tests {@link SphericalPoint#toString()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPoint.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4890458493568164342L);
		for (int i = 0; i < 100; i++) {

			double x = randomGenerator.nextDouble() * 2 - 1;
			double y = randomGenerator.nextDouble() * 2 - 1;
			double z = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v = new Vector3D(x, y, z);
			SphericalPoint sphericalPoint = new SphericalPoint(v);
			v = v.normalize();

			String expected = v.toString();
			expected = "SphericalPoint [position=" + expected + "]";

			String actual = sphericalPoint.toString();
			assertEquals(expected, actual);
		}

	}

}
