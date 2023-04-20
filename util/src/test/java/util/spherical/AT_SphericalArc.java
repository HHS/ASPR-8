package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.vector.Vector3D;

public class AT_SphericalArc {

	/**
	 * Tests {@link SphericalArc#SphericalArc(SphericalPoint, SphericalPoint)}
	 */
	@Test
	@UnitTestConstructor(target = SphericalArc.class, args = { SphericalPoint.class, SphericalPoint.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1864570857384195815L);

		double x1 = randomGenerator.nextDouble() * 2 - 1;
		double y1 = randomGenerator.nextDouble() * 2 - 1;
		double z1 = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

		double x2 = randomGenerator.nextDouble() * 2 - 1;
		double y2 = randomGenerator.nextDouble() * 2 - 1;
		double z2 = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPoint2 = new SphericalPoint(new Vector3D(x2, y2, z2));

		assertThrows(NullPointerException.class, () -> new SphericalArc(sphericalPoint1, null));
		assertThrows(NullPointerException.class, () -> new SphericalArc(null, sphericalPoint2));
		assertThrows(NullPointerException.class, () -> new SphericalArc(null, null));
		assertThrows(MalformedSphericalArcException.class, () -> new SphericalArc(sphericalPoint1, sphericalPoint1));

	}

	/**
	 * Tests {@link SphericalArc#getLength()}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "getLength", args = {})
	public void testGetLength() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(675015272753785672L);
		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;
			Vector3D v1 = new Vector3D(x1, y1, z1);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);
			assertEquals(v1.angle(v2), sphericalArc.getLength(), Vector3D.NORMAL_LENGTH_TOLERANCE);

		}

	}

	/**
	 * Tests {@link SphericalArc#distanceTo(SphericalPoint)}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "distanceTo", args = { SphericalPoint.class })
	public void testDistanceTo() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8163625096438805694L);

		double x = randomGenerator.nextDouble() * 2 - 1;
		double y = randomGenerator.nextDouble() * 2 - 1;
		double z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointA = new SphericalPoint(new Vector3D(x, y, z));
		x = randomGenerator.nextDouble() * 2 - 1;
		y = randomGenerator.nextDouble() * 2 - 1;
		z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointB = new SphericalPoint(new Vector3D(x, y, z));

		SphericalArc sphericalArc1 = new SphericalArc(sphericalPointA, sphericalPointB);

		/*
		 * Precondition test
		 * 
		 */

		assertThrows(NullPointerException.class, () -> sphericalArc1.distanceTo(null));

		for (int i = 0; i < 100; i++) {
			// Generate two randomized points and generate a arc

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;
			Vector3D v1 = new Vector3D(x1, y1, z1);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);

			/*
			 * Our strategy will be to create a randomized third point where we
			 * can surmise the distance without having to use the same
			 * algorithm. This will have us start with one of the points, then
			 * rotate it toward the other point and finally rotate that away
			 * from the plane containing the arc. Two case will be used: first
			 * we will consider points that are closer to the body of the arc
			 * than the either end point, second we will consider points that
			 * are closer to one of the end points.
			 */

			/*
			 * First, generate a point that is closer to the arc between the
			 * points than to either end point.
			 */

			double angle = v2.angle(v1);
			angle *= randomGenerator.nextDouble();
			/*
			 * v3 is a point on the arc between the end points
			 */
			Vector3D v3 = v1.rotateToward(v2, angle);
			/*
			 * perp is a randomized vector that is perpendicular to the plane
			 * containing the arc
			 */
			Vector3D perp = v1.cross(v2).normalize();
			if (randomGenerator.nextBoolean()) {
				perp = perp.reverse();
			}
			/*
			 * We now rotate v3 out of the plane by some random angle [-90, 90)
			 * degrees. This angle will be the expected distance to the arc.
			 */
			angle = (randomGenerator.nextDouble() - 0.5) * FastMath.PI;

			/*
			 * The absolute value of this angle will be the expected distance
			 * from the arc to v3
			 */
			double expectedDistance = FastMath.abs(angle);

			v3 = v3.rotateToward(perp, angle);

			double actualDistance = sphericalArc.distanceTo(new SphericalPoint(v3));

			assertEquals(expectedDistance, actualDistance, 1E-10, Integer.toString(i));

			/*
			 * Next, generate a point that is closer to one of the end points.
			 * We rotate v1 toward v2 by [angle,2PI-angle) radians.
			 */
			angle = v2.angle(v1);
			angle += randomGenerator.nextDouble() * (2 * FastMath.PI - angle);
			/*
			 * v3 is a point on the arc that does not lie between the end points
			 */
			v3 = v1.rotateToward(v2, angle);

			/*
			 * We now rotate v3 out of the plane by some random angle [-90, 90)
			 * degrees.
			 */
			angle = (randomGenerator.nextDouble() - 0.5) * FastMath.PI;

			v3 = v3.rotateToward(perp, angle);

			/*
			 * We expect the distance from v3 to the arc will be the smallest
			 * angle from v3 to either v1 or v2.
			 */
			expectedDistance = FastMath.min(v3.angle(v1), v3.angle(v2));

			actualDistance = sphericalArc.distanceTo(new SphericalPoint(v3));

			assertEquals(expectedDistance, actualDistance, Vector3D.NORMAL_LENGTH_TOLERANCE, Integer.toString(i));

		}
	}

	/**
	 * Tests {@link SphericalArc#getInterSection(SphericalArc)}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "getInterSection", args = { SphericalArc.class })
	public void testGetInterSection() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(22569187792469557L);

		double x = randomGenerator.nextDouble() * 2 - 1;
		double y = randomGenerator.nextDouble() * 2 - 1;
		double z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointA = new SphericalPoint(new Vector3D(x, y, z));
		x = randomGenerator.nextDouble() * 2 - 1;
		y = randomGenerator.nextDouble() * 2 - 1;
		z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointB = new SphericalPoint(new Vector3D(x, y, z));

		SphericalArc sphericalArc = new SphericalArc(sphericalPointA, sphericalPointB);

		/*
		 * Precondition test
		 */

		assertThrows(NullPointerException.class, () -> sphericalArc.getInterSection(null));

		/*
		 * Initialize a counter for the number of crossed arcs that are in the
		 * test. This will help show that the test encounters a variety of
		 * crossing and non-crossing arcs.
		 */
		int positiveCrossCount = 0;
		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v1 = new Vector3D(x1, y1, z1);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc12 = new SphericalArc(sphericalPoint1, sphericalPoint2);

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			double x4 = randomGenerator.nextDouble() * 2 - 1;
			double y4 = randomGenerator.nextDouble() * 2 - 1;
			double z4 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v4 = new Vector3D(x4, y4, z4);
			SphericalPoint sphericalPoint4 = new SphericalPoint(v4);

			SphericalArc sphericalArc34 = new SphericalArc(sphericalPoint3, sphericalPoint4);

			/*
			 * Derive the vectors that are normal to the planes of each arc
			 */
			Vector3D p12 = v1.cross(v2);

			Vector3D p34 = v3.cross(v4);

			/*
			 * If the arcs cross, then from the perspective of each arc, the
			 * other arc's end points must lie on opposite sides of the arc.
			 * This will mean that the dot product of the normal for an arc with
			 * each end points of the other arc will have opposite signs. Thus
			 * the product of the two dot products cannot be positive.
			 */

			boolean arc12SplitsArc34 = p12.dot(v3) * p12.dot(v4) <= 0;
			boolean arc342SplitsArc12 = p34.dot(v1) * p34.dot(v2) <= 0;
			boolean expectIntersection = arc12SplitsArc34 && arc342SplitsArc12;
			if (expectIntersection) {
				positiveCrossCount++;
				SphericalPoint interSectionPoint = sphericalArc12.getInterSection(sphericalArc34);

				assertNotNull(interSectionPoint, Integer.toString(i));

				// we will now show that the intersection point is on both arcs

				Vector3D intersectionPosition = interSectionPoint.getPosition();

				// is the intersection on the plane of both arcs?
				Vector3D perp = v1.cross(v2);
				assertEquals(FastMath.PI / 2, intersectionPosition.angle(perp), 1E-10, Integer.toString(i));

			}

		}
		// show that the number of crossed arcs that were tested is reasonably
		// high
		assertTrue(positiveCrossCount > 10);

	}

	/**
	 * Tests {@link SphericalArc#getSphericalPoint(int)}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "getSphericalPoint", args = { int.class })
	public void testGetSphericalPoint() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4795695625801637985L);

		for (int i = 0; i < 100; i++) {
			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint0 = new SphericalPoint(new Vector3D(x0, y0, z0));

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint0, sphericalPoint1);

			SphericalPoint actualSphericalPoint0 = sphericalArc.getSphericalPoint(0);
			assertEquals(sphericalPoint0, actualSphericalPoint0);

			SphericalPoint actualSphericalPoint1 = sphericalArc.getSphericalPoint(1);
			assertEquals(sphericalPoint1, actualSphericalPoint1);

			// precondition tests
			assertThrows(IndexOutOfBoundsException.class, () -> sphericalArc.getSphericalPoint(-1));
			assertThrows(IndexOutOfBoundsException.class, () -> sphericalArc.getSphericalPoint(2));
		}

	}

	/**
	 * Tests {@link SphericalArc#intersectsArc(SphericalArc)}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "intersectsArc", args = { SphericalArc.class })
	public void testIntersectsArc() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7924664054024487057L);

		double x = randomGenerator.nextDouble() * 2 - 1;
		double y = randomGenerator.nextDouble() * 2 - 1;
		double z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointA = new SphericalPoint(new Vector3D(x, y, z));
		x = randomGenerator.nextDouble() * 2 - 1;
		y = randomGenerator.nextDouble() * 2 - 1;
		z = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPointB = new SphericalPoint(new Vector3D(x, y, z));

		SphericalArc sphericalArc = new SphericalArc(sphericalPointA, sphericalPointB);

		/*
		 * Precondition test
		 */

		assertThrows(NullPointerException.class, () -> sphericalArc.intersectsArc(null));

		/*
		 * Initialize a counter for the number of crossed arcs that are in the
		 * test. This will help show that the test encounters a variety of
		 * crossing and non-crossing arcs.
		 */
		int positiveCrossCount = 0;
		for (int i = 0; i < 100; i++) {

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v1 = new Vector3D(x1, y1, z1);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc12 = new SphericalArc(sphericalPoint1, sphericalPoint2);

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			double x4 = randomGenerator.nextDouble() * 2 - 1;
			double y4 = randomGenerator.nextDouble() * 2 - 1;
			double z4 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v4 = new Vector3D(x4, y4, z4);
			SphericalPoint sphericalPoint4 = new SphericalPoint(v4);

			SphericalArc sphericalArc34 = new SphericalArc(sphericalPoint3, sphericalPoint4);

			/*
			 * Derive the vectors that are normal to the planes of each arc
			 */
			Vector3D p12 = v1.cross(v2);

			Vector3D p34 = v3.cross(v4);

			/*
			 * If the arcs cross, then from the perspective of each arc, the
			 * other arc's end points must lie on opposite sides of the arc.
			 * This will mean that the dot product of the normal for an arc with
			 * each end points of the other arc will have opposite signs. Thus
			 * the product of the two dot products cannot be positive.
			 */

			boolean arc12SplitsArc34 = p12.dot(v3) * p12.dot(v4) <= 0;
			boolean arc342SplitsArc12 = p34.dot(v1) * p34.dot(v2) <= 0;
			boolean expected = arc12SplitsArc34 && arc342SplitsArc12;
			if (expected) {
				positiveCrossCount++;
			}
			boolean actual = sphericalArc12.intersectsArc(sphericalArc34);

			assertEquals(expected, actual);

		}
		// show that the number of crossed arcs that were tested is reasonably
		// high
		assertTrue(positiveCrossCount > 10);

	}

	/**
	 * Tests {@link SphericalArc#getChirality(SphericalPoint)}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "getChirality", args = { SphericalPoint.class })
	public void testGetChirality() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5793327393482219366L);
		for (int i = 0; i < 100; i++) {
			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v1 = new Vector3D(x1, y1, z1);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v2 = new Vector3D(x2, y2, z2);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			// precondition test
			assertThrows(NullPointerException.class, () -> sphericalArc.getChirality(null));

			Vector3D perp = v1.cross(v2);
			Chirality expectedChirality;
			if (perp.dot(v3) >= 0) {
				expectedChirality = Chirality.RIGHT_HANDED;
			} else {
				expectedChirality = Chirality.LEFT_HANDED;
			}

			Chirality actualChirality = sphericalArc.getChirality(sphericalPoint3);
			assertEquals(expectedChirality, actualChirality);
		}
	}

	/**
	 * Tests {@link SphericalArc#toString()}
	 */
	@Test
	@UnitTestMethod(target = SphericalArc.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8407732758361446047L);

		for (int i = 0; i < 100; i++) {
			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint0 = new SphericalPoint(new Vector3D(x0, y0, z0));

			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint0, sphericalPoint1);

			StringBuilder sb = new StringBuilder();
			sb.append("SphericalArc [sphericalPoints=[");
			sb.append(sphericalPoint0);
			sb.append(", ");
			sb.append(sphericalPoint1);
			sb.append("]]");

			String expected = sb.toString();

			String actual = sphericalArc.toString();

			assertEquals(expected, actual);

		}

	}

}
