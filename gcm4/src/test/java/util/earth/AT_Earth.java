package util.earth;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.vector.Vector3D;

/**
 * Test class for {@link Earth}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Earth.class)
public class AT_Earth {
	

	private static final double TOLERANCE = 0.0001;
	
	/**
	 * Tests {@linkplain Earth#fromLatitude(double)
	 */
	@Test
	@UnitTestMethod(name = "fromLatitude", args = { double.class })
	public void testFromLatitude() {
		for (int i = -89; i < 90; i++) {
			double latitude = i;
			Earth earth = Earth.fromLatitude(latitude);
			assertEquals(Earth.getEffectiveEarthRadius(latitude), earth.getRadius(), 0);
		}
	}

	/**
	 * Tests {@linkplain Earth#fromRadius(double)
	 */
	@Test
	@UnitTestMethod(name = "fromRadius", args = { double.class })
	public void testFromRadius() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3373674409757874366L);
		for (int i = 0; i < 100; i++) {
			double radius = 10_000_000 * randomGenerator.nextDouble();
			Earth earth = Earth.fromRadius(radius);
			assertEquals(radius, earth.getRadius(), 0);
		}
	}

	/**
	 * Tests {@linkplain Earth#fromMeanRadius()
	 */
	@Test
	@UnitTestMethod(name = "fromMeanRadius", args = {})
	public void testFromMeanRadius() {
		Earth earth = Earth.fromMeanRadius();
		assertEquals(Earth.WGS84_MEAN_RADIUS_METERS, earth.getRadius(), 0);
	}

	/**
	 * Tests {@linkplain Earth#getEffectiveEarthRadius(double)
	 */
	@Test
	@UnitTestMethod(name = "getEffectiveEarthRadius", args = { double.class })
	public void testGetEffectiveEarthRadius() {
		Map<Double, Double> expectedValues = new LinkedHashMap<>();

		/*
		 * This is a set of value calculated in a spreadsheet
		 */
		expectedValues.put(-90.0, 6356752.314245);
		expectedValues.put(-89.0, 6356758.79502017);
		expectedValues.put(-88.0, 6356778.22956872);
		expectedValues.put(-87.0, 6356810.59456859);
		expectedValues.put(-86.0, 6356855.8511794);
		expectedValues.put(-85.0, 6356913.94508685);
		expectedValues.put(-84.0, 6356984.80656496);
		expectedValues.put(-83.0, 6357068.35055594);
		expectedValues.put(-82.0, 6357164.47676767);
		expectedValues.put(-81.0, 6357273.06978866);
		expectedValues.put(-80.0, 6357393.99922046);
		expectedValues.put(-79.0, 6357527.11982738);
		expectedValues.put(-78.0, 6357672.27170322);
		expectedValues.put(-77.0, 6357829.28045518);
		expectedValues.put(-76.0, 6357997.95740442);
		expectedValues.put(-75.0, 6358178.09980335);
		expectedValues.put(-74.0, 6358369.49106933);
		expectedValues.put(-73.0, 6358571.90103454);
		expectedValues.put(-72.0, 6358785.08621185);
		expectedValues.put(-71.0, 6359008.79007641);
		expectedValues.put(-70.0, 6359242.74336262);
		expectedValues.put(-69.0, 6359486.66437636);
		expectedValues.put(-68.0, 6359740.259322);
		expectedValues.put(-67.0, 6360003.2226439);
		expectedValues.put(-66.0, 6360275.23738223);
		expectedValues.put(-65.0, 6360555.97554252);
		expectedValues.put(-64.0, 6360845.0984787);
		expectedValues.put(-63.0, 6361142.25728927);
		expectedValues.put(-62.0, 6361447.09322607);
		expectedValues.put(-61.0, 6361759.23811542);
		expectedValues.put(-60.0, 6362078.31479093);
		expectedValues.put(-59.0, 6362403.93753785);
		expectedValues.put(-58.0, 6362735.7125482);
		expectedValues.put(-57.0, 6363073.23838636);
		expectedValues.put(-56.0, 6363416.10646463);
		expectedValues.put(-55.0, 6363763.90152812);
		expectedValues.put(-54.0, 6364116.20214861);
		expectedValues.put(-53.0, 6364472.58122664);
		expectedValues.put(-52.0, 6364832.60650153);
		expectedValues.put(-51.0, 6365195.84106847);
		expectedValues.put(-50.0, 6365561.84390236);
		expectedValues.put(-49.0, 6365930.17038759);
		expectedValues.put(-48.0, 6366300.37285333);
		expectedValues.put(-47.0, 6366672.00111357);
		expectedValues.put(-46.0, 6367044.60301136);
		expectedValues.put(-45.0, 6367417.72496659);
		expectedValues.put(-44.0, 6367790.91252667);
		expectedValues.put(-43.0, 6368163.71091936);
		expectedValues.put(-42.0, 6368535.66560729);
		expectedValues.put(-41.0, 6368906.32284322);
		expectedValues.put(-40.0, 6369275.23022561);
		expectedValues.put(-39.0, 6369641.93725361);
		expectedValues.put(-38.0, 6370005.99588093);
		expectedValues.put(-37.0, 6370366.96106781);
		expectedValues.put(-36.0, 6370724.3913304);
		expectedValues.put(-35.0, 6371077.84928683);
		expectedValues.put(-34.0, 6371426.90219937);
		expectedValues.put(-33.0, 6371771.12251176);
		expectedValues.put(-32.0, 6372110.08838128);
		expectedValues.put(-31.0, 6372443.38420466);
		expectedValues.put(-30.0, 6372770.60113715);
		expectedValues.put(-29.0, 6373091.33760414);
		expectedValues.put(-28.0, 6373405.19980463);
		expectedValues.put(-27.0, 6373711.80220578);
		expectedValues.put(-26.0, 6374010.76802791);
		expectedValues.put(-25.0, 6374301.72971937);
		expectedValues.put(-24.0, 6374584.32942045);
		expectedValues.put(-23.0, 6374858.21941589);
		expectedValues.put(-22.0, 6375123.06257517);
		expectedValues.put(-21.0, 6375378.53278006);
		expectedValues.put(-20.0, 6375624.31533886);
		expectedValues.put(-19.0, 6375860.10738663);
		expectedValues.put(-18.0, 6376085.61827093);
		expectedValues.put(-17.0, 6376300.56992252);
		expectedValues.put(-16.0, 6376504.69721036);
		expectedValues.put(-15.0, 6376697.74828063);
		expectedValues.put(-14.0, 6376879.48487901);
		expectedValues.put(-13.0, 6377049.68265598);
		expectedValues.put(-12.0, 6377208.13145452);
		expectedValues.put(-11.0, 6377354.63557993);
		expectedValues.put(-10.0, 6377489.01405124);
		expectedValues.put(-9.0, 6377611.100834);
		expectedValues.put(-8.0, 6377720.74505387);
		expectedValues.put(-7.0, 6377817.81119097);
		expectedValues.put(-6.0, 6377902.1792545);
		expectedValues.put(-5.0, 6377973.74493746);
		expectedValues.put(-4.0, 6378032.41975114);
		expectedValues.put(-3.0, 6378078.1311394);
		expectedValues.put(-2.0, 6378110.82257225);
		expectedValues.put(-1.0, 6378130.45361891);
		expectedValues.put(0.0, 6378137.0);
		expectedValues.put(1.0, 6378130.45361891);
		expectedValues.put(2.0, 6378110.82257225);
		expectedValues.put(3.0, 6378078.1311394);
		expectedValues.put(4.0, 6378032.41975114);
		expectedValues.put(5.0, 6377973.74493746);
		expectedValues.put(6.0, 6377902.1792545);
		expectedValues.put(7.0, 6377817.81119097);
		expectedValues.put(8.0, 6377720.74505387);
		expectedValues.put(9.0, 6377611.100834);
		expectedValues.put(10.0, 6377489.01405124);
		expectedValues.put(11.0, 6377354.63557993);
		expectedValues.put(12.0, 6377208.13145452);
		expectedValues.put(13.0, 6377049.68265598);
		expectedValues.put(14.0, 6376879.48487901);
		expectedValues.put(15.0, 6376697.74828063);
		expectedValues.put(16.0, 6376504.69721036);
		expectedValues.put(17.0, 6376300.56992252);
		expectedValues.put(18.0, 6376085.61827093);
		expectedValues.put(19.0, 6375860.10738663);
		expectedValues.put(20.0, 6375624.31533886);
		expectedValues.put(21.0, 6375378.53278006);
		expectedValues.put(22.0, 6375123.06257517);
		expectedValues.put(23.0, 6374858.21941589);
		expectedValues.put(24.0, 6374584.32942045);
		expectedValues.put(25.0, 6374301.72971937);
		expectedValues.put(26.0, 6374010.76802791);
		expectedValues.put(27.0, 6373711.80220578);
		expectedValues.put(28.0, 6373405.19980463);
		expectedValues.put(29.0, 6373091.33760414);
		expectedValues.put(30.0, 6372770.60113715);
		expectedValues.put(31.0, 6372443.38420466);
		expectedValues.put(32.0, 6372110.08838128);
		expectedValues.put(33.0, 6371771.12251176);
		expectedValues.put(34.0, 6371426.90219937);
		expectedValues.put(35.0, 6371077.84928683);
		expectedValues.put(36.0, 6370724.3913304);
		expectedValues.put(37.0, 6370366.96106781);
		expectedValues.put(38.0, 6370005.99588093);
		expectedValues.put(39.0, 6369641.93725361);
		expectedValues.put(40.0, 6369275.23022561);
		expectedValues.put(41.0, 6368906.32284322);
		expectedValues.put(42.0, 6368535.66560729);
		expectedValues.put(43.0, 6368163.71091936);
		expectedValues.put(44.0, 6367790.91252667);
		expectedValues.put(45.0, 6367417.72496659);
		expectedValues.put(46.0, 6367044.60301136);
		expectedValues.put(47.0, 6366672.00111357);
		expectedValues.put(48.0, 6366300.37285333);
		expectedValues.put(49.0, 6365930.17038759);
		expectedValues.put(50.0, 6365561.84390236);
		expectedValues.put(51.0, 6365195.84106847);
		expectedValues.put(52.0, 6364832.60650153);
		expectedValues.put(53.0, 6364472.58122664);
		expectedValues.put(54.0, 6364116.20214861);
		expectedValues.put(55.0, 6363763.90152812);
		expectedValues.put(56.0, 6363416.10646463);
		expectedValues.put(57.0, 6363073.23838636);
		expectedValues.put(58.0, 6362735.7125482);
		expectedValues.put(59.0, 6362403.93753785);
		expectedValues.put(60.0, 6362078.31479093);
		expectedValues.put(61.0, 6361759.23811542);
		expectedValues.put(62.0, 6361447.09322607);
		expectedValues.put(63.0, 6361142.25728927);
		expectedValues.put(64.0, 6360845.0984787);
		expectedValues.put(65.0, 6360555.97554252);
		expectedValues.put(66.0, 6360275.23738223);
		expectedValues.put(67.0, 6360003.2226439);
		expectedValues.put(68.0, 6359740.259322);
		expectedValues.put(69.0, 6359486.66437636);
		expectedValues.put(70.0, 6359242.74336262);
		expectedValues.put(71.0, 6359008.79007641);
		expectedValues.put(72.0, 6358785.08621185);
		expectedValues.put(73.0, 6358571.90103454);
		expectedValues.put(74.0, 6358369.49106933);
		expectedValues.put(75.0, 6358178.09980335);
		expectedValues.put(76.0, 6357997.95740442);
		expectedValues.put(77.0, 6357829.28045518);
		expectedValues.put(78.0, 6357672.27170322);
		expectedValues.put(79.0, 6357527.11982738);
		expectedValues.put(80.0, 6357393.99922046);
		expectedValues.put(81.0, 6357273.06978866);
		expectedValues.put(82.0, 6357164.47676767);
		expectedValues.put(83.0, 6357068.35055594);
		expectedValues.put(84.0, 6356984.80656496);
		expectedValues.put(85.0, 6356913.94508685);
		expectedValues.put(86.0, 6356855.8511794);
		expectedValues.put(87.0, 6356810.59456859);
		expectedValues.put(88.0, 6356778.22956872);
		expectedValues.put(89.0, 6356758.79502017);
		expectedValues.put(90.0, 6356752.314245);

		for (Double lat : expectedValues.keySet()) {
			Double expectedRadius = expectedValues.get(lat);
			assertEquals(expectedRadius, Earth.getEffectiveEarthRadius(lat), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Earth#getRadius()
	 */
	@Test
	@UnitTestMethod(name = "getRadius", args = {})
	public void testGetRadius() {
		// covered by testFromRadius
	}

	/**
	 * Tests {@linkplain Earth#getECCFromLatLonAlt(LatLonAlt)
	 */
	@Test
	@UnitTestMethod(name = "getECCFromLatLonAlt", args = { LatLonAlt.class })
	public void testGetECCFromLatLonAlt() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7867550291868680129L);

		for (int i = 0; i < 1000; i++) {
			double radius = randomGenerator.nextDouble() * 6_000_000 + 1_000_000;
			Earth earth = Earth.fromRadius(radius);

			double lat = randomGenerator.nextDouble() * 180 - 90;
			double lon = randomGenerator.nextDouble() * 360 - 180;
			double alt = randomGenerator.nextDouble() * 100_000;

			double coslat = FastMath.cos(FastMath.toRadians(lat));
			double coslon = FastMath.cos(FastMath.toRadians(lon));
			double sinlat = FastMath.sin(FastMath.toRadians(lat));
			double sinlon = FastMath.sin(FastMath.toRadians(lon));
			double distance = radius + alt;
			Vector3D v = new Vector3D(coslat * coslon, coslat * sinlon, sinlat).scale(distance);

			LatLonAlt latLonAlt = new LatLonAlt(lat, lon, alt);

			Vector3D ecc = earth.getECCFromLatLonAlt(latLonAlt);
			assertEquals(v.getX(), ecc.getX(), TOLERANCE);
			assertEquals(v.getY(), ecc.getY(), TOLERANCE);
			assertEquals(v.getZ(), ecc.getZ(), TOLERANCE);
		}

	}

	/**
	 * Tests {@linkplain Earth#getLatLonAlt(Vector3D)
	 */
	@Test
	@UnitTestMethod(name = "getLatLonAlt", args = { Vector3D.class })
	public void testGetLatLonAlt() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1462458115304058705L);

		for (int i = 0; i < 1000; i++) {
			double radius = randomGenerator.nextDouble() * 6_000_000 + 1_000_000;
			Earth earth = Earth.fromRadius(radius);

			double lat = randomGenerator.nextDouble() * 180 - 90;
			double lon = randomGenerator.nextDouble() * 360 - 180;
			double alt = randomGenerator.nextDouble() * 100_000;

			LatLonAlt expectedLatLonAlt = new LatLonAlt(lat, lon, alt);
			Vector3D ecc = earth.getECCFromLatLonAlt(expectedLatLonAlt);

			LatLonAlt actualLatLonAlt = earth.getLatLonAlt(ecc);

			assertEquals(expectedLatLonAlt.getLatitude(), actualLatLonAlt.getLatitude(), TOLERANCE);
			assertEquals(expectedLatLonAlt.getLongitude(), actualLatLonAlt.getLongitude(), TOLERANCE);
			assertEquals(expectedLatLonAlt.getAltitude(), actualLatLonAlt.getAltitude(), TOLERANCE);
		}

	}

	/**
	 * Tests {@linkplain Earth#getECCFromLatLon(LatLon))
	 */
	@Test
	@UnitTestMethod(name = "getECCFromLatLon", args = { LatLon.class })
	public void testGetECCFromLatLon() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4107285998527165778L);

		for (int i = 0; i < 1000; i++) {
			double radius = randomGenerator.nextDouble() * 6_000_000 + 1_000_000;
			Earth earth = Earth.fromRadius(radius);

			double lat = randomGenerator.nextDouble() * 180 - 90;
			double lon = randomGenerator.nextDouble() * 360 - 180;

			double coslat = FastMath.cos(FastMath.toRadians(lat));
			double coslon = FastMath.cos(FastMath.toRadians(lon));
			double sinlat = FastMath.sin(FastMath.toRadians(lat));
			double sinlon = FastMath.sin(FastMath.toRadians(lon));

			Vector3D v = new Vector3D(coslat * coslon, coslat * sinlon, sinlat).scale(radius);

			LatLon latLon = new LatLon(lat, lon);

			Vector3D ecc = earth.getECCFromLatLon(latLon);
			assertEquals(v.getX(), ecc.getX(), TOLERANCE);
			assertEquals(v.getY(), ecc.getY(), TOLERANCE);
			assertEquals(v.getZ(), ecc.getZ(), TOLERANCE);
		}
	}

	/**
	 * Tests {@linkplain Earth#getGroundDistanceFromECC(Vector3D, Vector3D))
	 */
	@Test
	@UnitTestMethod(name = "getGroundDistanceFromECC", args = { Vector3D.class, Vector3D.class })
	public void testGetGroundDistanceFromECC() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1693567521780632468L);

		for (int i = 0; i < 1000; i++) {
			double radius = randomGenerator.nextDouble() * 6_000_000 + 1_000_000;
			Earth earth = Earth.fromRadius(radius);

			double lat1 = randomGenerator.nextDouble() * 180 - 90;
			double lon1 = randomGenerator.nextDouble() * 360 - 180;

			Vector3D ecc1 = earth.getECCFromLatLon(new LatLon(lat1, lon1));

			double lat2 = randomGenerator.nextDouble() * 180 - 90;
			double lon2 = randomGenerator.nextDouble() * 360 - 180;

			Vector3D ecc2 = earth.getECCFromLatLon(new LatLon(lat2, lon2));

			double expectedGroundDistance = ecc1.angle(ecc2) * earth.getRadius();
			double actualGroundDistance = earth.getGroundDistanceFromECC(ecc1, ecc2);

			assertEquals(expectedGroundDistance, actualGroundDistance, 0);

		}
	}

	/**
	 * Tests {@linkplain Earth#getGroundDistanceFromLatLon(LatLon, LatLon))
	 */
	@Test
	@UnitTestMethod(name = "getGroundDistanceFromLatLon", args = { LatLon.class, LatLon.class })
	public void testGetGroundDistanceFromLatLon() {

		// covered by testGetGroundDistanceFromECC()
	}

	/**
	 * Tests {@linkplain Earth#getGroundDistanceFromLatLonAlt(LatLonAlt,
	 * LatLonAlt))
	 */
	@Test
	@UnitTestMethod(name = "getGroundDistanceFromLatLonAlt", args = { LatLonAlt.class, LatLonAlt.class })
	public void testGetGroundDistanceFromLatLonAlt() {

		// covered by testGetGroundDistanceFromECC()
	}
}
