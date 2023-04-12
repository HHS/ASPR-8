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
	}

	private final Data data;

	private SimulationState(Data data) {
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

		private void validate() {

			for (PlanQueueData planQueueData : data.planQueueDatas) {
				if (planQueueData.getArrivalId() >= data.planningQueueArrivalId) {
					throw new ContractException(NucleusError.PLANNING_QUEUE_ARRIVAL_INVALID);
				}
			}

		}

		/**
		 * Builds the SimulationState from the collected data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#PLANNING_QUEUE_ARRIVAL_INVALID}
		 *             if the planning queue arrival id does not exceed the
		 *             arrival id values for all stored PlanQueueData</li>
		 */
		public SimulationState build() {
			try {
				validate();
				return new SimulationState(data);
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

		/**
		 * Adds a PlanQueueData used for plan queue reconstruction
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError#NULL_PLAN_QUEUE_DATA} if the
		 *             plan queue data is null</li>
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

}
