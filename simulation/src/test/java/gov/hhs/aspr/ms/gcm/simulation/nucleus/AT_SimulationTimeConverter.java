package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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
}
