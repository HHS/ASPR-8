package util.geolocator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.earth.Earth;
import util.earth.LatLon;
import util.earth.LatLonAlt;
import util.geolocator.GeoLocator.Builder;
import util.random.RandomGeneratorProvider;
import util.vector.Vector3D;

/**
 * Test class for {@link GeoLocatorO}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = GeoLocator.class)
public class AT_GeoLocator {



	private static LatLon generateRandomizedLatLon(RandomGenerator randomGenerator, double lat, double lon, double radiusKilometers) {
		Earth earth = Earth.fromMeanRadius();
		Vector3D center = earth.getECCFromLatLon(new LatLon(lat, lon));
		Vector3D north = new Vector3D(0, 0, 1);
		double distance = FastMath.sqrt(randomGenerator.nextDouble()) * radiusKilometers * 1000;
		double angle = distance / earth.getRadius();
		double rotationAngle = randomGenerator.nextDouble() * 2 * FastMath.PI;
		Vector3D v = center.rotateToward(north, angle).rotateAbout(center, rotationAngle);
		LatLonAlt latLonAlt = earth.getLatLonAlt(v);
		return new LatLon(latLonAlt);
	}

	private static List<LatLon> generateLocations(RandomGenerator randomGenerator, double lat, double lon, double radiusKilometers, int count) {
		List<LatLon> result = new ArrayList<>();
		for (int i = 0; i < count; i++) {
			LatLon latLon = generateRandomizedLatLon(randomGenerator, lat, lon, radiusKilometers);

			result.add(latLon);
		}
		return result;
	}

	private GeoLocator<LatLon> generateGeoLocator(List<LatLon> locations) {
		Builder<LatLon> builder = GeoLocator.builder();
		locations.forEach(location -> builder.addLocation(location.getLatitude(), location.getLongitude(), location));
		return builder.build();
	}

	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2026864228657861590L);
		List<LatLon> locations = generateLocations(randomGenerator, 35, 128, 50, 100);
		generateGeoLocator(locations);
	}

	@Test
	@UnitTestMethod(name = "getLocations", args = { double.class, double.class, double.class })
	public void testGetLocations() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(1240444416174704003L);

		// Generate 100 random locations in a 50 kilometer radius region
		double lat = 35;
		double lon = 128;
		double radiusKilometers = 50;
		List<LatLon> locations = generateLocations(randomGenerator, lat, lon, radiusKilometers, 100);

		// Create a GeoLocator from the generated locations
		GeoLocator<LatLon> geoLocator = generateGeoLocator(locations);

		int testCount = 100;

		// search random spots in that region with a 10 kilometer search radius
		double searchRadiusKilometers = 10;
		Earth earth = Earth.fromMeanRadius();
		for (int i = 0; i < testCount; i++) {
			LatLon latLon = generateRandomizedLatLon(randomGenerator, lat, lon, radiusKilometers);

			// Determine the expected locations that fall within the search
			// radius
			Set<LatLon> expectedLocations = locations.stream().filter(location -> {
				return earth.getGroundDistanceFromLatLon(latLon, location) <= searchRadiusKilometers * 1000;
			}).collect(Collectors.toCollection(LinkedHashSet::new));

			// Get the locations from the GeoLocator
			Set<LatLon> actualLocations = geoLocator.getLocations(latLon.getLatitude(), latLon.getLongitude(), searchRadiusKilometers).stream().collect(Collectors.toCollection(LinkedHashSet::new));

			// compare the two sets
			assertEquals(expectedLocations, actualLocations);
		}
	}

	@Test
	@UnitTestMethod(name = "getNearestLocation", args = { double.class, double.class })
	public void testGetNearestLocation() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4915853160875930674L);

		// Generate 100 random locations in a 50 kilometer radius region
		double lat = 35;
		double lon = 128;
		double radiusKilometers = 50;
		List<LatLon> locations = generateLocations(randomGenerator, lat, lon, radiusKilometers, 100);

		// Create a GeoLocator from the generated locations
		GeoLocator<LatLon> geoLocator = generateGeoLocator(locations);

		int testCount = 100;

		// search random spots in that region with a 10 kilometer search radius

		Earth earth = Earth.fromMeanRadius();
		for (int i = 0; i < testCount; i++) {
			LatLon latLon = generateRandomizedLatLon(randomGenerator, lat, lon, radiusKilometers);

			// Determine the expected locations that fall within the search
			// radius
			LatLon expectedLocation = null;
			double lowestDistance = Double.POSITIVE_INFINITY;
			for (LatLon location : locations) {
				double distance = earth.getGroundDistanceFromLatLon(latLon, location);
				if (distance < lowestDistance) {
					lowestDistance = distance;
					expectedLocation = location;
				}
			}

			// Get the locations from the GeoLocator
			Optional<LatLon> actual = geoLocator.getNearestLocation(latLon.getLatitude(), latLon.getLongitude());
			assertTrue(actual.isPresent());
			LatLon actualLocation = actual.get();

			// compare the two sets
			assertEquals(expectedLocation, actualLocation);
		}
	}

	@Test
	@UnitTestMethod(name = "getPrioritizedLocations", args = { double.class, double.class, double.class })
	public void testGetPrioritizedLocations() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3451435198166238489L);

		// Generate 100 random locations in a 50 kilometer radius region
		double lat = 35;
		double lon = 128;
		double radiusKilometers = 50;
		List<LatLon> locations = generateLocations(randomGenerator, lat, lon, radiusKilometers, 100);

		// Create a GeoLocator from the generated locations
		GeoLocator<LatLon> geoLocator = generateGeoLocator(locations);

		int testCount = 100;

		// search random spots in that region with a 10 kilometer search radius
		double searchRadiusKilometers = 10;
		Earth earth = Earth.fromMeanRadius();
		for (int i = 0; i < testCount; i++) {
			LatLon latLon = generateRandomizedLatLon(randomGenerator, lat, lon, radiusKilometers);

			// Determine the expected locations that fall within the search
			// radius
			List<Pair<LatLon, Double>> expectedLocations = new ArrayList<>();
			for (LatLon location : locations) {
				double distance = earth.getGroundDistanceFromLatLon(latLon, location) / 1000;
				if (distance <= searchRadiusKilometers) {
					Pair<LatLon, Double> pair = new Pair<>(location, distance);
					expectedLocations.add(pair);
				}
			}

			Collections.sort(expectedLocations, new Comparator<Pair<LatLon, Double>>() {
				@Override
				public int compare(Pair<LatLon, Double> pair1, Pair<LatLon, Double> pair2) {
					return Double.compare(pair1.getSecond(), pair2.getSecond());
				}
			});

			// Get the locations from the GeoLocator
			List<Pair<LatLon, Double>> actualLocations = geoLocator.getPrioritizedLocations(latLon.getLatitude(), latLon.getLongitude(), searchRadiusKilometers).stream().collect(Collectors.toCollection(ArrayList::new));

			// compare the two sets
			assertEquals(expectedLocations, actualLocations);
		}
	}
	
	@Test
	@UnitTestMethod(target = GeoLocator.Builder.class,name = "build", args = {}, tags= {UnitTag.LOCAL_PROXY})
	public void testBuild() {
		//test is covered by the tests associated with the GeoLocator rather than the builder class	
	}
	
	@Test
	@UnitTestMethod(target = GeoLocator.Builder.class,name = "addLocation", args = {double.class, double.class, Object.class}, tags= {UnitTag.LOCAL_PROXY})
	public void testAddLocation() {
		//test is covered by the tests associated with the GeoLocator rather than the builder class	
	}

	
}
