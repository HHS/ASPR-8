package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class SimulationTimeConverter {
	private final double baseSecondsSinceEpoch;
	private final double billion = 1_000_000_000.0;
	private final double billionith = 1.0 / billion;
	private final int secondsPerDay = 86400;

	public SimulationTimeConverter(LocalDateTime baseDateTime) {
		baseSecondsSinceEpoch = getSecondsSinceEpoch(baseDateTime);
	}

	/*
	 * Returns the number of seconds since the epoch
	 */
	private double getSecondsSinceEpoch(LocalDateTime dateTime) {
		double secondsPortion = dateTime.toEpochSecond(ZoneOffset.UTC);
		double nanoPortion = dateTime.getNano() * billionith;
		return secondsPortion + nanoPortion;
	}

	public double getSimulationTime(LocalDateTime dateTime) {
		return (getSecondsSinceEpoch(dateTime) - baseSecondsSinceEpoch) / secondsPerDay;
	}

	public LocalDateTime getLocalDateTime(double simulationTime) {
		double t = baseSecondsSinceEpoch + simulationTime * secondsPerDay;
		long epochSeconds = (long) t;
		int nanos = (int) ((t - epochSeconds) * billion);

		/*
		 * The nanos will only be negative if the simulation time occurs before 1970 and
		 * thus the value of t is also negative. We move backward by an extra second and
		 * then forward by the conjugate of the nanos.
		 */
		if (nanos < 0) {
			epochSeconds--;
			nanos += billion;
		}
		LocalDateTime result = LocalDateTime.ofEpochSecond(epochSeconds, nanos, ZoneOffset.UTC);
		return result;
	}

}
