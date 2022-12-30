package util.earth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

/**
 * Test class for {@link LatLon}
 * 
 * @author Shawn Hatch
 *
 */

public class AT_LatLon {

	private static final double TOLERANCE = 0.0001;

	/**
	 * Tests {@link LatLon#getLatitude()}
	 */
	@Test
	@UnitTestMethod(target = LatLon.class, name = "getLatitude", args = {})
	public void testGetLatitude() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(50223838619731639L);
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
	@UnitTestMethod(target = LatLon.class, name = "getLongitude", args = {})
	public void testGetLongitude() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(62285879847543313L);
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
	@UnitTestMethod(target = LatLon.class, name = "toString", args = {})
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
	@UnitTestMethod(target = LatLon.class, name = "hashCode", args = {})
	public void testHashCode() {

		// Show equal objects have equal hash codes
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2874707211911558639L);
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
	@UnitTestMethod(target = LatLon.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7754088364991626671L);
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
	@UnitTestConstructor(target = LatLon.class, args = { double.class, double.class })
	public void testConstructor_Doubles() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4045297957712951231L);
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
	@UnitTestConstructor(target = LatLon.class, args = { LatLonAlt.class })
	public void testConstructor_LatLonAlt() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5811539292023379121L);
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
