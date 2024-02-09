package gov.hhs.aspr.ms.gcm.nucleus;

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

        public Data() {
        }

        public Data(Data data) {
            startTime = data.startTime;
            baseDate = data.baseDate;
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

        private void validate() {

        }

        /**
         * Builds the SimulationState from the collected data
         * 
         * @throws ContractException
         *                           <ul>
         *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_ARRIVAL_INVALID}
         *                           if the planning queue arrival id does not exceed
         *                           the arrival id values for all stored
         *                           PlanQueueData</li>
         *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_TIME}
         *                           if the simulation start time is exceeded by any
         *                           time value stored for a plan</li>
         *                           </ul>
         */
        public SimulationState build() {
            validate();
            return new SimulationState(new Data(data));
        }

        /**
         * Sets the time (floating point days) of simulation start. Defaults to zero.
         */
        public Builder setStartTime(double startTime) {
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
            if (localDate == null) {
                throw new ContractException(NucleusError.NULL_BASE_DATE);
            }
            data.baseDate = localDate;
            return this;
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

}
