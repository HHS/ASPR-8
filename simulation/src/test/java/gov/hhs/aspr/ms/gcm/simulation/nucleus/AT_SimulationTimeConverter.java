package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_SimulationTimeConverter {

	@Test
	@UnitTestConstructor(target = SimulationTimeConverter.class, args = { LocalDateTime.class })
	public void testSimulationTimeConverter() {
		// nothing to test
	}

	/*
	 * asserts the two local date times are less than a micro-second apart
	 */
	private void compareLocalDateTimes(LocalDateTime ldt1, LocalDateTime ldt2) {
		long deltaSeconds = ldt1.toEpochSecond(ZoneOffset.UTC) - ldt2.toEpochSecond(ZoneOffset.UTC);
		assertTrue(FastMath.abs(deltaSeconds) < 2L);
		long deltaNanos = ldt1.getNano() - ldt2.getNano();
		deltaNanos += deltaSeconds * 1_000_000_000;
		assertTrue(deltaNanos < 1000);
	}

	/*
	 * asserts the two sim times are less than a micro-second apart
	 */
	private void compareSimulationTimes(double simTime1, double simTime2) {
		double deltaTime = FastMath.abs(simTime1 - simTime2) * 24 * 3600 * 1_000_000;
		assertTrue(deltaTime < 1.0);
	}

	@Test
	@UnitTestMethod(target = SimulationTimeConverter.class, name = "getLocalDateTime", args = { double.class })
	public void testGetLocalDateTime() {

		SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
				LocalDateTime.of(2024, 6, 23, 5, 12, 30));

		LocalDateTime actualValue = simulationTimeConverter.getLocalDateTime(0.6);
		LocalDateTime expectedValue = LocalDateTime.of(2024, 6, 23, 19, 36, 30);
		compareLocalDateTimes(expectedValue, actualValue);

		actualValue = simulationTimeConverter.getLocalDateTime(10.8);
		expectedValue = LocalDateTime.of(2024, 7, 4, 0, 24, 30);
		compareLocalDateTimes(expectedValue, actualValue);

		actualValue = simulationTimeConverter.getLocalDateTime(-5.234);
		expectedValue = LocalDateTime.of(2024, 6, 17, 23, 35, 32, 400_000_000);
		compareLocalDateTimes(expectedValue, actualValue);

	}

	@Test
	@UnitTestMethod(target = SimulationTimeConverter.class, name = "getSimulationTime", args = { LocalDateTime.class })
	public void testGetSimulationTime() {
		SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(
				LocalDateTime.of(2024, 6, 23, 5, 12, 30));

		double actualValue = simulationTimeConverter.getSimulationTime(LocalDateTime.of(2024, 6, 23, 5, 12, 30));
		double expectedValue = 0;
		compareSimulationTimes(expectedValue, actualValue);

		actualValue = simulationTimeConverter.getSimulationTime(LocalDateTime.of(2024, 6, 27, 1, 36, 30));
		expectedValue = 3.85;
		compareSimulationTimes(expectedValue, actualValue);

		actualValue = simulationTimeConverter.getSimulationTime(LocalDateTime.of(2025, 7, 28, 20, 16, 6));
		expectedValue = 400.6275;
		compareSimulationTimes(expectedValue, actualValue);

	}

	@Test
	public void test2() {
		long expectedYears = 1;
		LocalDateTime ltd1 = LocalDateTime.of(2024, 2, 29, 3, 45, 37);
		LocalDateTime ltd2 = ltd1.plusYears(expectedYears);// .plusHours(24);
		long actualYears = ltd1.until(ltd2, ChronoUnit.YEARS);
		assertEquals(expectedYears, actualYears);
	}

//	@Test
//	public void test3() {
//
//		System.out.println(LocalDateTime.of(1972, 2, 28, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC));
//		System.out.println(LocalDateTime.of(1972, 2, 29, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC));
//		System.out.println(LocalDateTime.of(1972, 3, 1, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC));
//
//		long e2 = LocalDateTime.of(1972, 1, 1, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);
//		long e3 = LocalDateTime.of(1973, 1, 1, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);
//		long e4 = LocalDateTime.of(1974, 1, 1, 0, 0, 0, 0).toEpochSecond(ZoneOffset.UTC);
//
//		System.out.println(e3 - e2);
//		System.out.println(e4 - e3);
//	}

//	@Test
//	public void test4() {
//		double t = 2.0;
//		long epochSeconds = (long) t;
//		System.out.println(epochSeconds);
//		int nanos = (int) ((t - epochSeconds) * 1_000_000_000);
//		System.out.println(nanos);
//
//		if (nanos < 0) {
//			epochSeconds--;
//			nanos += 1_000_000_000;
//		}
//
//		LocalDateTime result = LocalDateTime.ofEpochSecond(epochSeconds, nanos, ZoneOffset.UTC);
//		System.out.println(result);
//	}
//
//	private double getSecondsSinceEpoch(LocalDateTime dateTime) {
//		double result = dateTime.toEpochSecond(ZoneOffset.UTC);
//		double nano = dateTime.getNano() / 1_000_000_000.0;
//		return result + nano;
//	}

	@Test
	public void test() {
		LocalDateTime syncTime = LocalDateTime.of(2024, 1, 1, 0, 0);
		SimulationTimeConverter simulationTimeConverter = new SimulationTimeConverter(syncTime);

		LocalDateTime ltdBirth = LocalDateTime.of(2024, 2, 29, 3, 45, 37);
		double birthTime = simulationTimeConverter.getSimulationTime(ltdBirth);
		System.out.println("birthTime = " + birthTime);

		// predict the person's birth day
		int expectedAge = 1;
		LocalDateTime ltd1 = simulationTimeConverter.getLocalDateTime(birthTime);
		System.out.println("ltd1 = " + ltd1);
		LocalDateTime ltd2 = ltd1.plusYears(expectedAge);
		System.out.println("ltd2 = " + ltd2);
		double eventTime = simulationTimeConverter.getSimulationTime(ltd2);
		System.out.println("eventTime = " + eventTime);
		// get a person's age
		LocalDateTime ltd3 = simulationTimeConverter.getLocalDateTime(birthTime);
		System.out.println("ltd3 = " + ltd3);
		LocalDateTime ltd4 = simulationTimeConverter.getLocalDateTime(eventTime);
		System.out.println("ltd4 = " + ltd4);
		int actualAge = (int) ltd3.until(ltd4, ChronoUnit.YEARS);

		assertEquals(expectedAge, actualAge);

	}

}
