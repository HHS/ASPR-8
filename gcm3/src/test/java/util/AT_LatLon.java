package util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import util.earth.LatLon;
import util.earth.LatLonAlt;

/**
 * Test class for {@link LatLon}
 * 
 * @author Shawn Hatch
 *
 */

@UnitTest(target = LatLon.class)
public class AT_LatLon {

	private static final double TOLERANCE = 0.0001;


	/**
	 * Tests {@link LatLon#getLatitude()}
	 */
	@Test
	@UnitTestMethod(name = "getLatitude", args = {})
	public void testGetLatitude() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(50223838619731639L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = 35;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLon#getLongitude()}
	 */

	@Test
	@UnitTestMethod(name = "getLongitude", args = {})
	public void testGetLongitude() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(62285879847543313L);
		for (int i = 0; i < 100; i++) {
			double latitude = 35;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}
	}

	/**
	 * Tests {@link LatLon#toString()}
	 */
	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		LatLon latLon = new LatLon(35, 128);
		assertEquals("LatLon [latitude=35.0, longitude=128.0]", latLon.toString());

		latLon = new LatLon(-35, 128);
		assertEquals("LatLon [latitude=-35.0, longitude=128.0]", latLon.toString());

		latLon = new LatLon(35, -128);
		assertEquals("LatLon [latitude=35.0, longitude=-128.0]", latLon.toString());

		latLon = new LatLon(-35, -128);
		assertEquals("LatLon [latitude=-35.0, longitude=-128.0]", latLon.toString());
	}

	/**
	 * Tests {@link LatLon#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {

		// Show equal objects have equal hash codes
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(2874707211911558639L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon1 = new LatLon(latitude, longitude);
			LatLon latLon2 = new LatLon(latitude, longitude);
			assertEquals(latLon1.hashCode(), latLon2.hashCode());
		}

	}

	/**
	 * Tests {@link LatLon#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7754088364991626671L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon1 = new LatLon(latitude, longitude);
			LatLon latLon2 = new LatLon(latitude, longitude);
			LatLon latLon3 = new LatLon(latitude, longitude);

			// reflexive
			assertEquals(latLon2, latLon1);

			// associative
			assertEquals(latLon1, latLon2);
			assertEquals(latLon2, latLon1);

			// transitive
			assertEquals(latLon1, latLon3);
			assertEquals(latLon2, latLon3);
		}
	}

	/**
	 * Tests {@link LatLon#LatLon(double, double)}
	 */
	@Test
	@UnitTestConstructor(args = { double.class, double.class })
	public void testConstructor_Doubles() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(4045297957712951231L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			LatLon latLon = new LatLon(latitude, longitude);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}

		// pre-condition tests

		assertThrows(RuntimeException.class, () -> new LatLon(-91, 0));

		assertThrows(RuntimeException.class, () -> new LatLon(91, 0));

		assertThrows(RuntimeException.class, () -> new LatLon(0, 181));

		assertThrows(RuntimeException.class, () -> new LatLon(0, -181));

	}

	/**
	 * Tests {@link LatLon#LatLon(LatLonAlt)}
	 */
	@Test
	@UnitTestConstructor(args = { LatLonAlt.class })
	public void testConstructor_LatLonAlt() {

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(5811539292023379121L);
		for (int i = 0; i < 100; i++) {
			double latitude = randomGenerator.nextDouble() * 180 - 90;
			double longitude = randomGenerator.nextDouble() * 360 - 180;
			double altitude = randomGenerator.nextDouble() * 10000 - 5000;
			LatLonAlt latLonAlt = new LatLonAlt(latitude, longitude, altitude);
			LatLon latLon = new LatLon(latLonAlt);
			assertEquals(latitude, latLon.getLatitude(), TOLERANCE);
			assertEquals(longitude, latLon.getLongitude(), TOLERANCE);
		}

		// pre-condition tests
		assertThrows(RuntimeException.class, () -> new LatLon(null));

	}

}
