package gov.hhs.aspr.ms.gcm.nucleus;

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

		public Data() {
		}

		public Data(Data data) {
			time = data.time;
			active = data.active;
			key = data.key;
			planData = data.planData;
			planner = data.planner;
			plannerId = data.plannerId;
			arrivalId = data.arrivalId;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + (active ? 1231 : 1237);
			result = prime * result + (int) (arrivalId ^ (arrivalId >>> 32));
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			result = prime * result + ((planData == null) ? 0 : planData.hashCode());
			result = prime * result + ((planner == null) ? 0 : planner.hashCode());
			result = prime * result + plannerId;
			long temp;
			temp = Double.doubleToLongBits(time);
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
			if (active != other.active) {
				return false;
			}
			if (arrivalId != other.arrivalId) {
				return false;
			}
			if (key == null) {
				if (other.key != null) {
					return false;
				}
			} else if (!key.equals(other.key)) {
				return false;
			}
			if (planData == null) {
				if (other.planData != null) {
					return false;
				}
			} else if (!planData.equals(other.planData)) {
				return false;
			}
			if (planner != other.planner) {
				return false;
			}
			if (plannerId != other.plannerId) {
				return false;
			}
			if (Double.doubleToLongBits(time) != Double.doubleToLongBits(other.time)) {
				return false;
			}
			return true;
		}
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
		 *             <li>{@linkplain NucleusError#NULL_PLAN_DATA} if the plan
		 *             data is null</li>
		 *             <li>{@linkplain NucleusError#NULL_PLANNER} if the planner
		 *             type is null</li>
		 */
		public PlanQueueData build() {
			validate();
			return new PlanQueueData(new Data(data));
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
		if (!(obj instanceof PlanQueueData)) {
			return false;
		}
		PlanQueueData other = (PlanQueueData) obj;
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
