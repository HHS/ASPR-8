package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.time.LocalDate;
import java.util.Objects;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An immutable data class that holds 1) the base date aligned to simulation
 * time zero and 2) the simulation start time as a floating point number of
 * days.
 */
@Immutable
public class SimulationState {

    private static class Data {
        private double startTime = 0;
        private LocalDate baseDate = LocalDate.now();
        private boolean locked;

        private Data() {
        }

        private Data(Data data) {
            startTime = data.startTime;
            baseDate = data.baseDate;
            locked = data.locked;
        }

        /**
         * Standard implementation consistent with the {@link #equals(Object)} method
         */
        @Override
        public int hashCode() {
            return Objects.hash(startTime, baseDate);
        }

        /**
         * Two {@link Data} instances are equal if and only if
         * their inputs are equal.
         */
        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Data other = (Data) obj;
            return Double.doubleToLongBits(startTime) == Double.doubleToLongBits(other.startTime)
                    && Objects.equals(baseDate, other.baseDate);
        }
    }

    private final Data data;

    private SimulationState(Data data) {
        this.data = data;
    }

    public static Builder builder() {
        return new Builder(new Data());
    }

    /**
     * Builder class for SimulationTime
     */
    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        private void validateData() {

        }

        /**
         * Builds the SimulationState from the collected data
         */
        public SimulationState build() {
            if (!data.locked) {
                validateData();
            }
            ensureImmutability();
            return new SimulationState(data);
        }

        /**
         * Sets the time (floating point days) of simulation start. Defaults to zero.
         */
        public Builder setStartTime(double startTime) {
            ensureDataMutability();
            data.startTime = startTime;
            return this;
        }

        /**
         * Sets the base date that synchronizes with simulation time zero. Defaults to
         * the current date.
         * 
         * @throws ContractException {@linkplain NucleusError#NULL_BASE_DATE} if the
         *                           base date is null
         */
        public Builder setBaseDate(LocalDate localDate) {
            ensureDataMutability();
            if (localDate == null) {
                throw new ContractException(NucleusError.NULL_BASE_DATE);
            }
            data.baseDate = localDate;
            return this;
        }

        private void ensureDataMutability() {
			if (data.locked) {
				data = new Data(data);
				data.locked = false;
			}
		}

		private void ensureImmutability() {
			if (!data.locked) {
				data.locked = true;
			}
		}
    }

    /**
     * Returns the time (floating point days) of simulation start.
     */
    public double getStartTime() {
        return data.startTime;
    }

    /**
     * Returns the base date that synchronizes with simulation time zero.
     */
    public LocalDate getBaseDate() {
        return data.baseDate;
    }

    /**
	 * Returns the current version of this Simulation Plugin, which is equal to the
	 * version of the GCM Simulation
	 */
	public String getVersion() {
		return StandardVersioning.VERSION;
	}

	/**
	 * Given a version string, returns whether the version is a supported version or
	 * not.
	 */
	public static boolean checkVersionSupported(String version) {
		return StandardVersioning.checkVersionSupported(version);
	}
    
	/**
     * Standard implementation consistent with the {@link #equals(Object)} method
     */
    @Override
    public int hashCode() {
        return Objects.hash(data);
    }

    /**
     * Two {@link SimulationState} instances are equal if and only if
     * their inputs are equal.
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SimulationState other = (SimulationState) obj;
        return Objects.equals(data, other.data);
    }

    /**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
    public Builder toBuilder() {
		return new Builder(data);
	}

}
