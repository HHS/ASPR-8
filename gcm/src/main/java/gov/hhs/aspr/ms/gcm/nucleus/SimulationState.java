package nucleus;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
public class SimulationState {

    private static class Data {
        private double startTime = 0;
        private LocalDate baseDate = LocalDate.now();
        private long planningQueueArrivalId;
        private List<PlanQueueData> planQueueDatas = new ArrayList<>();

        public Data() {
        }

        public Data(Data data) {
            startTime = data.startTime;
            baseDate = data.baseDate;
            planningQueueArrivalId = data.planningQueueArrivalId;
            planQueueDatas.addAll(data.planQueueDatas);
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((baseDate == null) ? 0 : baseDate.hashCode());
            result = prime * result + ((planQueueDatas == null) ? 0 : planQueueDatas.hashCode());
            result = prime * result + (int) (planningQueueArrivalId ^ (planningQueueArrivalId >>> 32));
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
            if (planQueueDatas == null) {
                if (other.planQueueDatas != null) {
                    return false;
                }
            } else if (!planQueueDatas.equals(other.planQueueDatas)) {
                return false;
            }
            if (planningQueueArrivalId != other.planningQueueArrivalId) {
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
     *
     */
    public static class Builder {
        private Data data;

        private Builder(Data data) {
            this.data = data;
        }

        private void validate() {

            for (PlanQueueData planQueueData : data.planQueueDatas) {
                if (planQueueData.getTime() < data.startTime) {
                    throw new ContractException(NucleusError.PLANNING_QUEUE_TIME);

                }
                if (planQueueData.getArrivalId() >= data.planningQueueArrivalId) {
                    throw new ContractException(NucleusError.PLANNING_QUEUE_ARRIVAL_INVALID);
                }
            }

        }

        /**
         * Builds the SimulationState from the collected data
         * 
         * @throws ContractException
         *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_ARRIVAL_INVALID}
         *                           if the planning queue arrival id does not exceed
         *                           the
         *                           arrival id values for all stored PlanQueueData</li>
         * 
         *                           <li>{@linkplain NucleusError#PLANNING_QUEUE_TIME}
         *                           if the
         *                           simulation start time is exceeded by any time value
         *                           stored for a plan</li>
         * 
         * 
         */
        public SimulationState build() {
            validate();
            return new SimulationState(new Data(data));
        }

        /**
         * Sets the time (floating point days) of simulation start. Defaults to
         * zero.
         * 
         */
        public Builder setStartTime(double startTime) {
            data.startTime = startTime;
            return this;
        }

        /**
         * Sets the base date that synchronizes with simulation time zero.
         * Defaults to the current date.
         * 
         * @throws ContractException
         *                           <li>{@linkplain NucleusError#NULL_BASE_DATE} if the
         *                           base
         *                           date is null</li>
         */
        public Builder setBaseDate(LocalDate localDate) {
            if (localDate == null) {
                throw new ContractException(NucleusError.NULL_BASE_DATE);
            }
            data.baseDate = localDate;
            return this;
        }

        /**
         * Adds a PlanQueueData used for plan queue reconstruction
         * 
         * @throws ContractException
         *                           <li>{@linkplain NucleusError#NULL_PLAN_QUEUE_DATA}
         *                           if the
         *                           plan queue data is null</li>
         */
        public Builder addPlanQueueData(PlanQueueData planQueueData) {
            if (planQueueData == null) {
                throw new ContractException(NucleusError.NULL_PLAN_QUEUE_DATA);
            }
            data.planQueueDatas.add(planQueueData);
            return this;
        }

        /**
         * Sets the next arrival id available to the planning queue
         */
        public Builder setPlanningQueueArrivalId(long planningQueueArrivalId) {
            data.planningQueueArrivalId = planningQueueArrivalId;
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

    /**
     * Returns the list of PlanQueueData objects.
     * 
     */
    public List<PlanQueueData> getPlanQueueDatas() {
        return new ArrayList<>(data.planQueueDatas);
    }

    /**
     * Returns the planning queue arrival id that should be used as the first
     * free arrival id.
     */
    public long getPlanningQueueArrivalId() {
        return data.planningQueueArrivalId;
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
