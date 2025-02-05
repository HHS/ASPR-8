package gov.hhs.aspr.ms.gcm.simulation.nucleus;

import java.time.LocalDate;

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

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseDate == null) ? 0 : baseDate.hashCode());
            long temp;
            temp = Double.doubleToLongBits(startTime);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Data)) {
                return false;
            }
            Data other = (Data) obj;
            if (baseDate == null) {
                if (other.baseDate != null) {
                    return false;
                }
            } else if (!baseDate.equals(other.baseDate)) {
                return false;
            }
            if (Double.doubleToLongBits(startTime) != Double.doubleToLongBits(other.startTime)) {
                return false;
            }
            return true;
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
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((data == null) ? 0 : data.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SimulationState)) {
            return false;
        }
        SimulationState other = (SimulationState) obj;
        if (data == null) {
            if (other.data != null) {
                return false;
            }
        } else if (!data.equals(other.data)) {
            return false;
        }
        return true;
    }

    /**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
    public Builder toBuilder() {
		return new Builder(data);
	}

}
