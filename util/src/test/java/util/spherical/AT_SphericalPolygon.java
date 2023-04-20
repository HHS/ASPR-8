package util.spherical;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;
import util.earth.Earth;
import util.earth.LatLon;
import util.random.RandomGeneratorProvider;
import util.vector.Vector3D;

public class AT_SphericalPolygon {

	private static SphericalPoint generateRandomizedSphericalPoint(RandomGenerator randomGenerator) {
		double x = randomGenerator.nextDouble() * 2 - 1;
		double y = randomGenerator.nextDouble() * 2 - 1;
		double z = randomGenerator.nextDouble() * 2 - 1;
		return new SphericalPoint(new Vector3D(x, y, z));
	}

	private static LocalBuilder localBuilder() {
		return new LocalBuilder();
	}

	private static class LocalBuilder {
		private Earth earth = Earth.fromMeanRadius();
		private SphericalPolygon.Builder builder = SphericalPolygon.builder();

		public LocalBuilder add(double lat, double lon) {
			builder.addSphericalPoint(new SphericalPoint(earth.getECCFromLatLon(new LatLon(lat, lon))));
			return this;
		}

		public SphericalPolygon build() {
			return builder.build();
		}
	}

	@Test
	@UnitTestMethod(target = SphericalPolygon.Builder.class, name = "addSphericalPoint", args = { SphericalPoint.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddSphericalPoint() {
		// covered by testBuilder()
	}

	@Test
	@UnitTestMethod(target = SphericalPolygon.Builder.class, name = "build", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testBuild() {
		// covered by testBuilder()
	}

	@Test
	@UnitTestMethod(target = SphericalPolygon.Builder.class, name = "setUseSearchTree", args = { boolean.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testSetUseSearchTree() {
		// covered by testBuilder()
	}

	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "builder", args = {})
	public void testBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5068048243871963894L);

		// Show that an empty set of vertices will throw a
		// MalformedSphericalPolygonException

		assertThrows(MalformedSphericalPolygonException.class, () -> {
			SphericalPolygon.builder().build();
		});

		// Show that a single vertex will throw a
		// MalformedSphericalPolygonException

		assertThrows(MalformedSphericalPolygonException.class, () -> {
			SphericalPolygon.builder()//
							.addSphericalPoint(generateRandomizedSphericalPoint(randomGenerator))//
							.build();
		});

		// Show that two vertices will throw a
		// MalformedSphericalPolygonException
		assertThrows(MalformedSphericalPolygonException.class, () -> {
			SphericalPolygon.builder()//
							.addSphericalPoint(generateRandomizedSphericalPoint(randomGenerator))//
							.addSphericalPoint(generateRandomizedSphericalPoint(randomGenerator))//
							.build();

		});

		// Show that null vertices will throw a
		// MalformedSphericalPolygonException
		assertThrows(MalformedSphericalPolygonException.class, () -> {
			SphericalPolygon.builder()//
							.addSphericalPoint(null)//
							.addSphericalPoint(generateRandomizedSphericalPoint(randomGenerator))//
							.addSphericalPoint(generateRandomizedSphericalPoint(randomGenerator))//
							.build();
		});

		// Show that a crossing edges will throw a
		// MalformedSphericalPolygonException
		assertThrows(MalformedSphericalPolygonException.class, () -> localBuilder()//
																					.add(0, 0)//
																					.add(30, 0)//
																					.add(0, 20)//
																					.add(30, 20)//
																					.build());

	}

	private SphericalPolygon generateSphericalPolygon(RandomGenerator randomGenerator, Chirality chirality) {

		switch (chirality) {
		case RIGHT_HANDED:
			return localBuilder()//
									.add(38.69724712, -101.5135275)//
									.add(37.92632876, -99.96309844)//
									.add(37.96933031, -97.32288686)//
									.add(39.35733124, -95.45661273)//
									.add(38.35690025, -98.8421335)//
									.add(39.46582205, -98.42992687)//
									.add(39.16371461, -97.35415071)//
									.add(39.66032273, -96.34105164)//
									.add(40.80930453, -96.82301542)//
									.add(40.53524646, -99.62446993)//
									.add(39.72191785, -101.7482263)//
									.build();//
		case LEFT_HANDED:
			return localBuilder()//
									.add(39.72191785, -101.7482263)//
									.add(40.53524646, -99.62446993)//
									.add(40.80930453, -96.82301542)//
									.add(39.66032273, -96.34105164)//
									.add(39.16371461, -97.35415071)//
									.add(39.46582205, -98.42992687)//
									.add(38.35690025, -98.8421335)//
									.add(39.35733124, -95.45661273)//
									.add(37.96933031, -97.32288686)//
									.add(37.92632876, -99.96309844)//
									.add(38.69724712, -101.5135275)//
									.build();//
		default:
			throw new RuntimeException("unhandled case");
		}

	}

	/**
	 * Tests {@link SphericalPolygon#containsPosition(SphericalPoint)}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "containsPosition", args = { SphericalPoint.class })
	public void testContainsPosition() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6065504530416184047L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		// Determine the centroid of the points of the polygon
		List<SphericalTriangle> sphericalTriangles = sphericalPolygon.getSphericalTriangles();

		Vector3D centroid = new Vector3D();
		for (SphericalPoint sphericalPoint : sphericalPolygon.getSphericalPoints()) {
			centroid = centroid.add(sphericalPoint.getPosition());
		}
		centroid.normalize();

		// Determine the largest angle to the vertices from the centroid
		double maxAngle = 0;
		for (SphericalPoint sphericalPoint : sphericalPolygon.getSphericalPoints()) {
			maxAngle = FastMath.max(maxAngle, centroid.angle(sphericalPoint.getPosition()));
		}

		/*
		 * Generate a large cluster of points in the general region of the
		 * polygon. For each test point, compare the result of the polygon
		 * intersection with the target point with the intersection of the
		 * polygon's triangles with the target point.
		 */
		Vector3D north = new Vector3D(0, 0, 1);
		int testCount = 10000;
		int hitCount = 0;
		for (int i = 0; i < testCount; i++) {
			double radiusAngle = FastMath.sqrt(randomGenerator.nextDouble()) * maxAngle;
			double rotationAngle = randomGenerator.nextDouble() * 2 * FastMath.PI;
			Vector3D v = centroid.rotateToward(north, radiusAngle).rotateAbout(centroid, rotationAngle);

			SphericalPoint sphericalPoint = new SphericalPoint(v);

			boolean actual = sphericalPolygon.containsPosition(sphericalPoint);
			boolean expected = false;
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				expected |= sphericalTriangle.contains(sphericalPoint);
			}
			if (expected) {
				hitCount++;
			}
			assertEquals(expected, actual);
		}

		/*
		 * Show that a reasonable number of the test points fell inside the
		 * polygon and outside the polygon.
		 */
		assertTrue(hitCount > testCount / 10);
		assertTrue(hitCount < 9 * testCount / 10);
	}

	/**
	 * Tests {@link SphericalPolygon#getChirality()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getChirality", args = {})
	public void testGetChirality() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5040370953904598541L);

		for (Chirality chirality : Chirality.values()) {
			SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, chirality);
			assertEquals(chirality, sphericalPolygon.getChirality());
		}
	}

	/**
	 * Tests {@link SphericalPolygon#getCentroid()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getCentroid", args = {})
	public void testGetCentroid() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3996062030446397343L);

		for (Chirality chirality : Chirality.values()) {
			SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, chirality);
			Vector3D v = new Vector3D();
			for (SphericalPoint sphericalPoint : sphericalPolygon.getSphericalPoints()) {
				v = v.add(sphericalPoint.getPosition());
			}
			SphericalPoint expected = new SphericalPoint(v);

			SphericalPoint actual = sphericalPolygon.getCentroid();

			for (int index = 0; index < 3; index++) {
				double expectedCoordinateValue = expected.getPosition().get(index);
				double actualCoordinateValue = actual.getPosition().get(index);
				double delta = FastMath.abs(expectedCoordinateValue - actualCoordinateValue);
				assertTrue(delta < Vector3D.NORMAL_LENGTH_TOLERANCE);
			}

		}
	}

	/**
	 * Tests {@link SphericalPolygon#getRadius()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getRadius", args = {})
	public void testGetRadius() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7740289660333498822L);

		for (Chirality chirality : Chirality.values()) {
			SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, chirality);

			SphericalPoint centroid = sphericalPolygon.getCentroid();

			double expectedRadius = Double.NEGATIVE_INFINITY;
			for (SphericalPoint sphericalPoint : sphericalPolygon.getSphericalPoints()) {
				expectedRadius = FastMath.max(expectedRadius, sphericalPoint.getPosition().angle(centroid.getPosition()));
			}

			double actualRadius = sphericalPolygon.getRadius();

			assertEquals(expectedRadius, actualRadius, Vector3D.NORMAL_LENGTH_TOLERANCE);

		}
	}

	/**
	 * Tests {@link SphericalPolygon#getSphericalArcs()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getSphericalArcs", args = {})
	public void testGetSphericalArcs() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2507505792488258607L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		// Get the spherical points from the polygon
		List<SphericalPoint> sphericalPoints = sphericalPolygon.getSphericalPoints();

		/*
		 * Generate arcs from the points and wrap them so that they can be
		 * compared to the arcs from the polygon. Note that SphericalArc does
		 * not provide an equals() implementation.
		 */
		Set<SphericalArcWrapper> expected = new LinkedHashSet<>();
		for (int i = 0; i < sphericalPoints.size(); i++) {
			int j = (i + 1) % sphericalPoints.size();
			SphericalPoint sphericalPoint1 = sphericalPoints.get(i);
			SphericalPoint sphericalPoint2 = sphericalPoints.get(j);
			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);
			expected.add(new SphericalArcWrapper(sphericalArc));
		}

		Set<SphericalArcWrapper> actual = sphericalPolygon.getSphericalArcs().stream().map(arc -> new SphericalArcWrapper(arc)).collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expected, actual);
	}

	private static class SphericalArcWrapper {
		private final Vector3D position1;
		private final Vector3D position2;

		public SphericalArcWrapper(SphericalArc sphericalArc) {
			position1 = sphericalArc.getSphericalPoint(0).getPosition();
			position2 = sphericalArc.getSphericalPoint(1).getPosition();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((position1 == null) ? 0 : position1.hashCode());
			result = prime * result + ((position2 == null) ? 0 : position2.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (!(obj instanceof SphericalArcWrapper)) {
				return false;
			}
			SphericalArcWrapper other = (SphericalArcWrapper) obj;
			if (position1 == null) {
				if (other.position1 != null) {
					return false;
				}
			} else if (!position1.equals(other.position1)) {
				return false;
			}
			if (position2 == null) {
				if (other.position2 != null) {
					return false;
				}
			} else if (!position2.equals(other.position2)) {
				return false;
			}
			return true;
		}
	}

	/**
	 * Tests {@link SphericalPolygon#getSphericalPoints()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getSphericalPoints", args = {})
	public void testGetSphericalPoints() {
		Earth earth = Earth.fromMeanRadius();

		List<SphericalPoint> expected = new ArrayList<>();

		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(38.69724712, -101.5135275))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(37.92632876, -99.96309844))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(37.96933031, -97.32288686))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(39.35733124, -95.45661273))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(38.35690025, -98.8421335))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(39.46582205, -98.42992687))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(39.16371461, -97.35415071))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(39.66032273, -96.34105164))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(40.80930453, -96.82301542))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(40.53524646, -99.62446993))));
		expected.add(new SphericalPoint(earth.getECCFromLatLon(new LatLon(39.72191785, -101.7482263))));

		SphericalPolygon.Builder builder = SphericalPolygon.builder();
		for (SphericalPoint sphericalPoint : expected) {
			builder.addSphericalPoint(sphericalPoint);
		}

		SphericalPolygon sphericalPolygon = builder.build();

		List<SphericalPoint> actual = sphericalPolygon.getSphericalPoints();

		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link SphericalPolygon#getSphericalTriangles()}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "getSphericalTriangles", args = {})
	public void testGetSphericalTriangles() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6353521777153484531L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		List<SphericalTriangle> sphericalTriangles = sphericalPolygon.getSphericalTriangles();

		Vector3D north = new Vector3D(0, 0, 1);
		Vector3D center = sphericalPolygon.getCentroid().getPosition();

		// Generate a bunch of points that are in the general area of the
		// polygon. If the triangles and the polygon agree in all cases, we
		// conclude that the triangles do indeed cover the polygon.
		int intersectionCount = 0;
		for (int i = 0; i < 1000; i++) {
			Vector3D v = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius());
			v = v.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint = new SphericalPoint(v);

			boolean expected = false;
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					expected = true;
					break;
				}
			}
			if (expected) {
				intersectionCount++;
			}

			boolean actual = sphericalPolygon.containsPosition(sphericalPoint);

			assertEquals(expected, actual);
		}

		assertTrue(intersectionCount > 100);
		assertTrue(intersectionCount < 900);

	}

	/**
	 * Tests {@link SphericalPolygon#intersects(SphericalArc)}
	 * 
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "intersects", args = { SphericalArc.class })
	public void testIntersects_Arcs() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2244326004441352995L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		List<SphericalTriangle> sphericalTriangles = sphericalPolygon.getSphericalTriangles();

		Vector3D north = new Vector3D(0, 0, 1);
		Vector3D center = sphericalPolygon.getCentroid().getPosition();

		// Generate a bunch of arcs that are in the general area of the
		// polygon. If the triangles and the polygon agree in all cases, we
		// conclude that the triangles do indeed cover the polygon.
		int intersectionCount = 0;
		for (int i = 0; i < 1000; i++) {
			Vector3D v1 = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
			v1 = v1.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			Vector3D v2 = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
			v2 = v2.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			SphericalArc sphericalArc = new SphericalArc(sphericalPoint1, sphericalPoint2);

			boolean expected = false;
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				if (sphericalTriangle.intersects(sphericalArc)) {
					expected = true;
					break;
				}
			}
			if (expected) {
				intersectionCount++;
			}

			boolean actual = sphericalPolygon.intersects(sphericalArc);

			assertEquals(expected, actual);

		}
		assertTrue(intersectionCount > 100);
		assertTrue(intersectionCount < 900);

	}

	/**
	 * Tests {@link SphericalPolygon#intersects(SphericalTriangle)}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "intersects", args = { SphericalTriangle.class })
	public void testIntersects_Triangles() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5946152543146292840L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		List<SphericalTriangle> sphericalTriangles = sphericalPolygon.getSphericalTriangles();

		Vector3D north = new Vector3D(0, 0, 1);
		Vector3D center = sphericalPolygon.getCentroid().getPosition();

		// Generate a bunch of arcs that are in the general area of the
		// polygon. If the triangles and the polygon agree in all cases, we
		// conclude that the triangles do indeed cover the polygon.
		int intersectionCount = 0;
		for (int i = 0; i < 1000; i++) {
			Vector3D v1 = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
			v1 = v1.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint1 = new SphericalPoint(v1);

			Vector3D v2 = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
			v2 = v2.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint2 = new SphericalPoint(v2);

			Vector3D v3 = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
			v3 = v3.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
			SphericalPoint sphericalPoint3 = new SphericalPoint(v3);

			SphericalTriangle testTriangle = new SphericalTriangle(sphericalPoint1, sphericalPoint2, sphericalPoint3);

			boolean expected = false;
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				if (sphericalTriangle.intersects(testTriangle)) {
					expected = true;
					break;
				}
			}
			if (expected) {
				intersectionCount++;
			}

			boolean actual = sphericalPolygon.intersects(testTriangle);

			assertEquals(expected, actual);

		}
		assertTrue(intersectionCount > 100);
		assertTrue(intersectionCount < 900);

	}

	/**
	 * Tests {@link SphericalPolygon#intersects(SphericalPolygon)}
	 */
	@Test
	@UnitTestMethod(target = SphericalPolygon.class, name = "intersects", args = { SphericalPolygon.class })
	public void testIntersects_Polygons() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7005502162196438809L);

		SphericalPolygon sphericalPolygon = generateSphericalPolygon(randomGenerator, Chirality.LEFT_HANDED);

		List<SphericalTriangle> sphericalTriangles = sphericalPolygon.getSphericalTriangles();

		Vector3D north = new Vector3D(0, 0, 1);
		Vector3D center = sphericalPolygon.getCentroid().getPosition();

		// Generate a bunch of arcs that are in the general area of the
		// polygon. If the triangles and the polygon agree in all cases, we
		// conclude that the triangles do indeed cover the polygon.
		int intersectionCount = 0;
		int wellformedCount = 0;
		for (int i = 0; i < 1000; i++) {
			SphericalPolygon.Builder builder = SphericalPolygon.builder();

			for (int j = 0; j < 4; j++) {
				Vector3D v = center.rotateToward(north, FastMath.sqrt(randomGenerator.nextDouble()) * sphericalPolygon.getRadius() * 2);
				v = v.rotateAbout(center, randomGenerator.nextDouble() * 2 * FastMath.PI);
				builder.addSphericalPoint(new SphericalPoint(v));
			}
			boolean wellFormed;
			SphericalPolygon testPolygon = null;
			try {
				testPolygon = builder.build();
				wellFormed = true;
			} catch (MalformedSphericalPolygonException e) {
				wellFormed = false;
			}

			if (wellFormed) {
				wellformedCount++;
				List<SphericalTriangle> testTriangles = testPolygon.getSphericalTriangles();

				boolean expected = false;
				for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
					for (SphericalTriangle testTriangle : testTriangles) {
						if (sphericalTriangle.intersects(testTriangle)) {
							expected = true;
							break;
						}
					}
				}
				if (expected) {
					intersectionCount++;
				}
				boolean actual = sphericalPolygon.intersects(testPolygon);
				assertEquals(expected, actual);
			}

		}
		assertTrue(wellformedCount > 500);
		assertTrue(intersectionCount > 0);
		assertTrue(intersectionCount < wellformedCount);
	}

}
