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
import util.vector.Vector3D;

public class AT_SphericalTriangle {

	/**
	 * Tests
	 * {@link SphericalTriangle#SphericalTriangle(SphericalPoint, SphericalPoint, SphericalPoint)}
	 */
	@Test
	@UnitTestConstructor(target = SphericalTriangle.class, args = { SphericalPoint.class, SphericalPoint.class, SphericalPoint.class })
	public void testConstructor() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1516981394291616444L);

		double x1 = randomGenerator.nextDouble() * 2 - 1;
		double y1 = randomGenerator.nextDouble() * 2 - 1;
		double z1 = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

		double x2 = randomGenerator.nextDouble() * 2 - 1;
		double y2 = randomGenerator.nextDouble() * 2 - 1;
		double z2 = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPoint2 = new SphericalPoint(new Vector3D(x2, y2, z2));

		double x3 = randomGenerator.nextDouble() * 2 - 1;
		double y3 = randomGenerator.nextDouble() * 2 - 1;
		double z3 = randomGenerator.nextDouble() * 2 - 1;

		SphericalPoint sphericalPoint3 = new SphericalPoint(new Vector3D(x3, y3, z3));

		assertThrows(MalformedSphericalTriangleException.class, () -> new SphericalTriangle(null, sphericalPoint2, sphericalPoint3));

		assertThrows(MalformedSphericalTriangleException.class, () -> new SphericalTriangle(sphericalPoint1, null, sphericalPoint3));

		assertThrows(MalformedSphericalTriangleException.class, () -> new SphericalTriangle(sphericalPoint1, sphericalPoint2, null));
	}

	/**
	 * Tests {@link SphericalTriangle#contains(SphericalPoint)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "contains", args = { SphericalPoint.class })
	public void testContains() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9196011045677894651L);
		int containsCount = 0;
		for (int i = 0; i < 1000; i++) {
			// Generate a randomized spherical triangle
			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint2 = new SphericalPoint(new Vector3D(x2, y2, z2));

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint3 = new SphericalPoint(new Vector3D(x3, y3, z3));

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			/*
			 * Find the center of the triangle and use the triangle's radius to
			 * generate a point that is nearby, but may be either inside or
			 * outside the triangle.
			 */

			Vector3D centroid = sphericalTriangle.getCentroid();

			double radius = sphericalTriangle.getRadius();

			Vector3D north = new Vector3D(0, 0, 1);

			Vector3D v = north.rotateAbout(centroid, randomGenerator.nextDouble() * 2 * FastMath.PI);

			v = centroid.rotateToward(v, randomGenerator.nextDouble() * radius);

			SphericalPoint testPoint = new SphericalPoint(v);

			/*
			 * From the perspective of each arc of the triangle, the test point
			 * is either right handed or left handed. Since the arcs are ordered
			 * such that a->b, b->c and c->a, we see that the chirality of the
			 * test point will be the same for all arcs if and only if the test
			 * point is inside the triangle.
			 */
			Chirality chirality0 = sphericalTriangle.getSphericalArc(0).getChirality(testPoint);
			Chirality chirality1 = sphericalTriangle.getSphericalArc(1).getChirality(testPoint);
			Chirality chirality2 = sphericalTriangle.getSphericalArc(2).getChirality(testPoint);

			boolean expected = chirality0 == chirality1 && chirality0 == chirality2;
			if (expected) {
				containsCount++;
			}

			boolean actual = sphericalTriangle.contains(testPoint);

			assertEquals(expected, actual);

		}
		/*
		 * We show that there are at least a few hits and misses
		 */
		assertTrue(containsCount > 100);
		assertTrue(containsCount < 900);

	}

	/**
	 * Tests {@link SphericalTriangle#distanceTo(SphericalPoint)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "distanceTo", args = { SphericalPoint.class })
	public void testDistanceTo() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7990273909659377244L);
		int containsCount = 0;
		for (int i = 0; i < 1000; i++) {
			// Generate a randomized spherical triangle
			double x1 = randomGenerator.nextDouble() * 2 - 1;
			double y1 = randomGenerator.nextDouble() * 2 - 1;
			double z1 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint1 = new SphericalPoint(new Vector3D(x1, y1, z1));

			double x2 = randomGenerator.nextDouble() * 2 - 1;
			double y2 = randomGenerator.nextDouble() * 2 - 1;
			double z2 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint2 = new SphericalPoint(new Vector3D(x2, y2, z2));

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			SphericalPoint sphericalPoint3 = new SphericalPoint(new Vector3D(x3, y3, z3));

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			/*
			 * Find the center of the triangle and use the triangle's radius to
			 * generate a point that is nearby, but may be either inside or
			 * outside the triangle.
			 */

			Vector3D centroid = sphericalTriangle.getCentroid();

			double radius = sphericalTriangle.getRadius();

			Vector3D north = new Vector3D(0, 0, 1);

			Vector3D v = north.rotateAbout(centroid, randomGenerator.nextDouble() * 2 * FastMath.PI);

			v = centroid.rotateToward(v, randomGenerator.nextDouble() * radius * 2);

			SphericalPoint testPoint = new SphericalPoint(v);

			double expectedDistance = 0;
			if (!sphericalTriangle.contains(testPoint)) {
				expectedDistance = Double.POSITIVE_INFINITY;
				expectedDistance = FastMath.min(expectedDistance, sphericalTriangle.getSphericalArc(0).distanceTo(testPoint));
				expectedDistance = FastMath.min(expectedDistance, sphericalTriangle.getSphericalArc(1).distanceTo(testPoint));
				expectedDistance = FastMath.min(expectedDistance, sphericalTriangle.getSphericalArc(2).distanceTo(testPoint));
			} else {
				containsCount++;
			}
			double actualDistance = sphericalTriangle.distanceTo(testPoint);

			assertEquals(expectedDistance, actualDistance, 1E-10);

		}
		/*
		 * We show that there are at least a few hits and misses
		 */
		assertTrue(containsCount > 100);
		assertTrue(containsCount < 900);

	}

	/**
	 * Tests {@link SphericalTriangle#getArea()}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getArea", args = {})
	public void testGetArea() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2718440263756572706L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle
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

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			/*
			 * Determine the angle for each of the vertices
			 */
			double angle1 = getAngle(v1, v2, v3);
			double angle2 = getAngle(v2, v3, v1);
			double angle3 = getAngle(v3, v1, v2);
			double expectedArea = angle1 + angle2 + angle3 - FastMath.PI;

			double actualArea = sphericalTriangle.getArea();
			assertEquals(expectedArea, actualArea, 1E-10);
		}

	}

	/*
	 * Returns the angle formed at v2 from v1 and v3
	 */
	private static double getAngle(Vector3D v1, Vector3D v2, Vector3D v3) {
		return v1.cross(v2).cross(v2).angle(v3.cross(v2).cross(v2));
	}

	/**
	 * Tests {@link SphericalTriangle#getCentroid()}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getCentroid", args = {})
	public void testGetCentroid() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8983815604783536421L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle
			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v0 = new Vector3D(x0, y0, z0);
			SphericalPoint sphericalPoint0 = new SphericalPoint(v0);

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

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint0, sphericalPoint1, sphericalPoint2);

			Vector3D actualCentroid = sphericalTriangle.getCentroid();

			/*
			 * The centroid is expected to be the intersection of the
			 * perpendicular bisectors of the sides of the triangle. For each
			 * arc, we calculate the midpoint of the arc and the normal of the
			 * arc. The cross product of these will be the normal to the plane
			 * containing the centroid and the midpoint of the arc. The cross
			 * product of these normals will be the centroid, or its opposite.
			 */

			Vector3D p1 = v0.normalize().add(v2.normalize()).cross(v0.cross(v2));
			Vector3D p2 = v1.normalize().add(v2.normalize()).cross(v1.cross(v2));
			Vector3D expectedCentroid = p1.cross(p2);

			// We may need to reverse the expected centroid, depending on which
			// side of the v0->v1 arc the v2 point falls.
			boolean reverse = v0.cross(v1).dot(v2) < 0;
			if (reverse) {
				expectedCentroid = expectedCentroid.reverse();
			}
			// Finally, we must normalize the expected centroid
			expectedCentroid = expectedCentroid.normalize();
			assertTrue(equalVectors(expectedCentroid, actualCentroid));

		}
	}

	private static boolean equalVectors(Vector3D v1, Vector3D v2) {
		return FastMath.abs(v1.getX() - v2.getX()) < 1E-10 && FastMath.abs(v1.getY() - v2.getY()) < 1E-10 && FastMath.abs(v1.getZ() - v2.getZ()) < 1E-10;
	}

	/**
	 * Tests {@link SphericalTriangle#getChirality()}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getChirality", args = {})
	public void testGetChirality() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8318155766608307004L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle
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

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);
			Chirality expectedChirality = sphericalArc.getChirality(sphericalPoint3);

			Chirality actualChirality = sphericalTriangle.getChirality();

			assertEquals(expectedChirality, actualChirality);

		}
	}

	/**
	 * Tests {@link SphericalTriangle#getRadius()}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getRadius", args = {})
	public void testGetRadius() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(858839356108815818L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle
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

			double x3 = randomGenerator.nextDouble() * 2 - 1;
			double y3 = randomGenerator.nextDouble() * 2 - 1;
			double z3 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v3 = new Vector3D(x3, y3, z3);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			Vector3D centroid = sphericalTriangle.getCentroid();

			double expectedRadius = centroid.angle(v1);
			double actualRadius = sphericalTriangle.getRadius();
			assertEquals(expectedRadius, actualRadius, 1E-10);
		}
	}

	/**
	 * Tests {@link SphericalTriangle#getSphericalArc(int)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getSphericalArc", args = { int.class })
	public void testGetSphericalArc() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7281272754332690959L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle

			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v0 = new Vector3D(x0, y0, z0);
			SphericalPoint sphericalPoint0 = new SphericalPoint(v0);

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

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint0, sphericalPoint1, sphericalPoint2);

			SphericalArc sphericalArc0 = new SphericalArc(sphericalPoint0, sphericalPoint1);
			SphericalArc sphericalArc1 = new SphericalArc(sphericalPoint1, sphericalPoint2);
			SphericalArc sphericalArc2 = new SphericalArc(sphericalPoint2, sphericalPoint0);

			assertTrue(equalArcs(sphericalArc0, sphericalTriangle.getSphericalArc(0)));
			assertTrue(equalArcs(sphericalArc1, sphericalTriangle.getSphericalArc(1)));
			assertTrue(equalArcs(sphericalArc2, sphericalTriangle.getSphericalArc(2)));

		}
	}

	private boolean equalArcs(SphericalArc sphericalArc1, SphericalArc sphericalArc2) {
		return equalPoints(sphericalArc1.getSphericalPoint(0), sphericalArc2.getSphericalPoint(0)) && equalPoints(sphericalArc1.getSphericalPoint(1), sphericalArc2.getSphericalPoint(1));
	}

	private boolean equalPoints(SphericalPoint sphericalPoint1, SphericalPoint sphericalPoint2) {
		Vector3D position1 = sphericalPoint1.getPosition();
		Vector3D position2 = sphericalPoint2.getPosition();

		return FastMath.abs(position1.getX() - position2.getX()) < 1E-10 && FastMath.abs(position1.getY() - position2.getY()) < 1E-10 && FastMath.abs(position1.getZ() - position2.getZ()) < 1E-10;
	}

	/**
	 * Tests {@link SphericalTriangle#getSphericalPoint(int)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "getSphericalPoint", args = { int.class })
	public void testGetSphericalPoint() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5799569940995229412L);

		for (int i = 0; i < 100; i++) {
			// Generate a randomized spherical triangle

			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v0 = new Vector3D(x0, y0, z0);
			SphericalPoint sphericalPoint0 = new SphericalPoint(v0);

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

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint0, sphericalPoint1, sphericalPoint2);

			assertEquals(sphericalPoint0, sphericalTriangle.getSphericalPoint(0));
			assertEquals(sphericalPoint1, sphericalTriangle.getSphericalPoint(1));
			assertEquals(sphericalPoint2, sphericalTriangle.getSphericalPoint(2));
		}
	}

	/**
	 * Tests {@link SphericalTriangle#intersects(SphericalTriangle)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "intersects", args = { SphericalTriangle.class })
	public void testIntersects_Triangle() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3736978332919299911L);

		int intersectionCount = 0;
		for (int i = 0; i < 1000; i++) {
			// Generate two randomized spherical triangles

			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v0 = new Vector3D(x0, y0, z0);
			SphericalPoint sphericalPoint0 = new SphericalPoint(v0);

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

			SphericalTriangle sphericalTriangle1 = new SphericalTriangle(sphericalPoint0, sphericalPoint1, sphericalPoint2);

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

			double x5 = randomGenerator.nextDouble() * 2 - 1;
			double y5 = randomGenerator.nextDouble() * 2 - 1;
			double z5 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v5 = new Vector3D(x5, y5, z5);
			SphericalPoint sphericalPoint5 = new SphericalPoint(v5);

			SphericalTriangle sphericalTriangle2 = new SphericalTriangle(sphericalPoint3, sphericalPoint4, sphericalPoint5);

			// Does any point of either triangle lie inside the other triangle?
			boolean expected = false;
			for (int j = 0; j < 3; j++) {
				expected |= sphericalTriangle1.contains(sphericalTriangle2.getSphericalPoint(j));
				expected |= sphericalTriangle2.contains(sphericalTriangle1.getSphericalPoint(j));
			}

			// Does any arc of either triangle cross the other triangles
			for (int j = 0; j < 3; j++) {
				expected |= sphericalTriangle1.intersects(sphericalTriangle2.getSphericalArc(j));
				expected |= sphericalTriangle2.intersects(sphericalTriangle1.getSphericalArc(j));
			}
			if (expected) {
				intersectionCount++;
			}

			boolean actual = sphericalTriangle1.intersects(sphericalTriangle2);

			assertEquals(expected, actual);

		}

		assertTrue(intersectionCount > 100);
		assertTrue(intersectionCount < 900);
	}

	/**
	 * Tests {@link SphericalTriangle#intersects(SphericalArc)}
	 */
	@Test
	@UnitTestMethod(target = SphericalTriangle.class, name = "intersects", args = { SphericalArc.class })
	public void testIntersects_Arc() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2722369694139955276L);

		int intersectionCount = 0;
		for (int i = 0; i < 1000; i++) {
			// Generate a randomized spherical triangle and a randomize
			// spherical arc

			double x0 = randomGenerator.nextDouble() * 2 - 1;
			double y0 = randomGenerator.nextDouble() * 2 - 1;
			double z0 = randomGenerator.nextDouble() * 2 - 1;

			Vector3D v0 = new Vector3D(x0, y0, z0);
			SphericalPoint sphericalPoint0 = new SphericalPoint(v0);

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

			SphericalTriangle sphericalTriangle = new SphericalTriangle(sphericalPoint0, sphericalPoint1, sphericalPoint2);

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

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint3, sphericalPoint4);

			// Does any point of the arc lie inside the triangle?
			boolean expected = false;
			for (int j = 0; j < 2; j++) {
				expected |= sphericalTriangle.contains(sphericalArc.getSphericalPoint(j));
			}

			// Does any arc of the triangle cross the arc?
			for (int j = 0; j < 3; j++) {
				expected |= sphericalTriangle.getSphericalArc(j).intersectsArc(sphericalArc);
			}
			if (expected) {
				intersectionCount++;
			}

			boolean actual = sphericalTriangle.intersects(sphericalArc);

			assertEquals(expected, actual);

		}
		assertTrue(intersectionCount > 100);
		assertTrue(intersectionCount < 900);
	}
}
