package util.earth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.vector.Vector3D;

/**
 * Test class for {@link LatLonAlt}
 * 
 * @author Shawn Hatch
 *
 */
public class AT_LatLonAlt {

	private static final double TOLERANCE = 0.0001;

	/**
	 * Tests {@link LatLonAlt#getLatitude()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "getLatitude", args = {})
	public void testGetLatitude() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7062845400521947521L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = 35;
			double altitude = 1000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#getLongitude()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "getLongitude", args = {})
	public void testGetLongitude() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9178151003715988391L);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = 1000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#getAltitude()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "getAltitude", args = {})
	public void testGetAltitude() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3210941165573781662L);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = 128;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(altitude, latLonAlt.getAltitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLonAlt#toString()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "toString", args = {})
	public void testToString() {
		LatLonAlt latLonAlt = new LatLonAlt(35, 128, 1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, 128, 1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, -128, 1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=-128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, -128, 1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=-128.0, altitude=1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, 128, -1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, 128, -1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(35, -128, -1000);
		assertEquals("LatLonAlt [latitude=35.0, longitude=-128.0, altitude=-1000.0]", latLonAlt.toString());

		latLonAlt = new LatLonAlt(-35, -128, -1000);
		assertEquals("LatLonAlt [latitude=-35.0, longitude=-128.0, altitude=-1000.0]", latLonAlt.toString());
	}

	/**
	 * Tests {@link LatLonAlt#hashCode()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "hashCode", args = {})
	public void testHashCode() {

		// Show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2791517325027305404L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt1 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt2 = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(latLonAlt1.hashCode(), latLonAlt2.hashCode());
		}

	}

	/**
	 * Tests {@link LatLonAlt#equals(Object)}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3854552470387902715L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt1 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt2 = new LatLonAlt(latitude, longitude, altitude);
			LatLonAlt latLonAlt3 = new LatLonAlt(latitude, longitude, altitude);

			// reflexive
			assertEquals(latLonAlt1, latLonAlt1);

			// associative
			assertEquals(latLonAlt1, latLonAlt2);
			assertEquals(latLonAlt2, latLonAlt1);

			// transitive
			assertEquals(latLonAlt1, latLonAlt3);
			assertEquals(latLonAlt2, latLonAlt3);
		}
	}

	/**
	 * Tests {@link LatLonAlt#LatLonAlt(LatLon)}
	 */
	@Test
	@UnitTestConstructor(target = LatLonAlt.class, args = { LatLon.class })
	public void testConstructor_LatLon() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7719374094024568257L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;

			LatLon latLon = new LatLon(latitude, longitude);
			LatLonAlt latLonAlt = new LatLonAlt(latLon);

			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
			assertEquals(0, latLonAlt.getAltitude(), 0);
		}

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> {
			Vector3D v = null;
			new LatLonAlt(v);
		});

	}

	/**
	 * Tests {@link LatLonAlt#LatLonAlt(LatLon)}
	 * 
	 * Tests {@link LatLonAlt#LatLonAlt(Vector3D)}
	 * 
	 * Tests {@link LatLonAlt#LatLonAlt(double, double, double)}
	 */
	@Test
	@UnitTestConstructor(target = LatLonAlt.class, args = { Vector3D.class })
	public void testConstructor_Vector3D() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1423864170984280158L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;

			Vector3D v = new Vector3D(latitude, longitude, altitude);
			LatLonAlt latLonAlt = new LatLonAlt(v);

			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
			assertEquals(altitude, latLonAlt.getAltitude(), TOLERANCE);
		}

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> {
			Vector3D v = null;
			new LatLonAlt(v);
		});

		assertThrows(RuntimeException.class, () -> new LatLonAlt(new Vector3D(-91, 0, 1000)));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(new Vector3D(91, 0, 1000)));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(new Vector3D(0, 181, 1000)));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(new Vector3D(0, -181, 1000)));

	}

	/**
	 * Tests {@link LatLonAlt#LatLonAlt(double, double, double)}
	 */
	@Test
	@UnitTestConstructor(target = LatLonAlt.class, args = { double.class, double.class, double.class })
	public void testConstructor_Doubles() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5395751744049862772L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;

			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			assertEquals(latitude, latLonAlt.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLonAlt.getLongitude(), TOLERANCE);
			assertEquals(altitude, latLonAlt.getAltitude(), TOLERANCE);

		}

		assertThrows(RuntimeException.class, () -> new LatLonAlt(-91, 0, 1000));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(91, 0, 1000));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(0, 181, 1000));

		assertThrows(RuntimeException.class, () -> new LatLonAlt(0, -181, 1000));

	}

	/**
	 * Tests {@link LatLonAlt#toVector3D()}
	 */
	@Test
	@UnitTestMethod(target = LatLonAlt.class, name = "toVector3D", args = {})
	public void testToVector3D() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1014093707230518248L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			Vector3D v = latLonAlt.toVector3D();
			assertEquals(latLonAlt.getLatitude(), v.getX(), TOLERANCE);
			assertEquals(latLonAlt.getLongitude(), v.getY(), TOLERANCE);
			assertEquals(latLonAlt.getAltitude(), v.getZ(), TOLERANCE);
		}
	}

}
