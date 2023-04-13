package nucleus;

import util.errors.ContractException;

/**
 * 
 * Class supporting serialization of plans
 *
 *
 */
public final class PlanQueueData {

	private static class Data {
		private double time;
		private boolean active = true;
		private Object key;
		private PlanData planData;
		private Planner planner;
		private int plannerId;
		private long arrivalId;
	}

	/**
	 * Static constructor for the builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for PlanQueueData
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {

		}

		private void validate() {
			if (data.time < 0) {
				throw new ContractException(NucleusError.NEGATIVE_TIME);
			}

			if (data.planData == null) {
				throw new ContractException(NucleusError.NULL_PLAN_DATA);
			}

			if (data.planner == null) {
				throw new ContractException(NucleusError.NULL_PLANNER);
			}
		}

		/**
		 * 
		 * Returns the PlanQueueData built from the contributed data.
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain NucleusError#NEGATIVE_TIME} if the plan
		 *             time is negative</li>
		 *             <li>{@linkplain NucleusError#NULL_PLAN_DATA} if the plan
		 *             data is null</li>
		 *             <li>{@linkplain NucleusError#NULL_PLANNER} if the planner
		 *             type is null</li>
		 */
		public PlanQueueData build() {
			try {
				validate();
				return new PlanQueueData(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Set the plan time. Defaults to zero.
		 */
		public Builder setTime(double time) {
			data.time = time;
			return this;
		}

		/**
		 * Set the active state. Defaults to true.
		 */
		public Builder setActive(boolean active) {
			data.active = active;
			return this;
		}

		/**
		 * Set the plan's key. Defaults to null.
		 */
		public Builder setKey(Object key) {
			data.key = key;
			return this;
		}

		/**
		 * Set the plan's data. Defaults to null.
		 */
		public Builder setPlanData(PlanData planData) {
			data.planData = planData;
			return this;
		}

		/**
		 * Set the plan's planner type. Defaults to null.
		 */
		public Builder setPlanner(Planner planner) {
			data.planner = planner;
			return this;
		}

		/**
		 * Set the plan's planner id. Defaults to zero.
		 */
		public Builder setPlannerId(int plannerId) {
			data.plannerId = plannerId;
			return this;
		}

		/**
		 * Set the plan's queue arrival id. Defaults to zero.
		 */
		public Builder setArrivalId(long arrivalId) {
			data.arrivalId = arrivalId;
			return this;
		}

	}

	private final Data data;

	
	private PlanQueueData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the time for the plan 
	 */
	public double getTime() {
		return data.time;
	}

	/**
	 * Returns the active state for the plan 
	 */
	public boolean isActive() {
		return data.active;
	}
	/**
	 * Returns the key for the plan 
	 */
	public Object getKey() {
		return data.key;
	}
	/**
	 * Returns the plan data for the plan 
	 */
	public PlanData getPlanData() {
		return data.planData;
	}
	/**
	 * Returns the planner type for the plan
	 */
	public Planner getPlanner() {
		return data.planner;
	}
	/**
	 * Returns the planner id for the plan
	 */
	public int getPlannerId() {
		return data.plannerId;
	}
	/**
	 * Returns the arrival id for the plan
	 */
	public long getArrivalId() {
		return data.arrivalId;
	}

}
