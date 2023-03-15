package nucleus;

import java.time.LocalDate;

import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An immutable data class that holds 1) the base date aligned to simulation
 * time zero and 2) the simulation start time as a floating point number of
 * days.
 * 
 *
 *
 */
@Immutable
public class SimulationTime {

	private static class Data {
		private double startTime = 0;
		private LocalDate baseDate = LocalDate.now();
		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [startTime=");
			builder.append(startTime);
			builder.append(", baseDate=");
			builder.append(baseDate);
			builder.append("]");
			return builder.toString();
		}
		
	}

	private final Data data;

	private SimulationTime(Data data) {
		this.data = data;
	}

	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for SimulationTime
	 *
	 */
	public static class Builder {
		private Data data = new Data();

		public SimulationTime build() {
			try {

				return new SimulationTime(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the time (floating point days) of simulation start. Defaults to
		 * zero.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NEGATIVE_START_TIME} if the
		 *             start time is negative</li>
		 */
		public Builder setStartTime(double startTime) {
			if (startTime < 0) {
				throw new ContractException(NucleusError.NEGATIVE_START_TIME);
			}
			data.startTime = startTime;
			return this;
		}

		/**
		 * Sets the base date that synchronizes with simulation time zero.
		 * Defaults to the current date.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_BASE_DATE} if the base
		 *             date is null</li>
		 */
		public Builder setBaseDate(LocalDate localDate) {
			if (localDate == null) {
				throw new ContractException(NucleusError.NULL_BASE_DATE);
			}
			data.baseDate = localDate;
			return this;
		}
	}

	/**
	 * Returns the time (floating point days) of simulation start.
	 * 
	 */
	public double getStartTime() {
		return data.startTime;
	}

	/**
	 * Returns the base date that synchronizes with simulation time zero.
	 * 
	 */
	public LocalDate getBaseDate() {
		return data.baseDate;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("SimulationTime [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	

}
