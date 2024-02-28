package gov.hhs.aspr.ms.gcm.nucleus;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class SimulationTimeConverter {
	
	private final LocalDateTime baseDateTime;
	private final double baseSecondsSinceEpoch;
	private final double billion = 1_000_000_000.0;
	private final double billionith = 1.0/billion;
	
	public SimulationTimeConverter(LocalDateTime baseDateTime){		
		this.baseDateTime = baseDateTime;
		baseSecondsSinceEpoch = getSecondsSinceEpoch(baseDateTime);
	}
	
	/*
	 * Returns the number of seconds since the epoch
	 */
	private double getSecondsSinceEpoch(LocalDateTime dateTime) {
		double result = dateTime.getNano();
		result *= billionith;
		result += dateTime.toEpochSecond(ZoneOffset.UTC);
		return result;
	}

	public double getSimulationTime(LocalDateTime dateTime) {
		return (getSecondsSinceEpoch(dateTime) - baseSecondsSinceEpoch)/86400;
	}
	
	public LocalDateTime getLocalDateTime(double simulationTime) {
		double t = getSecondsSinceEpoch(baseDateTime);
		t += simulationTime*86400;
		long epochSeconds =  (long)t;
		int nanos = (int)((t-epochSeconds)*1_000_000_000.0);
		LocalDateTime result = LocalDateTime.ofEpochSecond(epochSeconds,nanos, ZoneOffset.UTC);
		return result;
	}
	

	
}
