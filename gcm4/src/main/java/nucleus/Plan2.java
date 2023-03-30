package nucleus;

import java.util.function.Consumer;

import util.errors.ContractException;

public class Plan2<T> {

	private static class Data<K> {
		private double time;
		private Consumer<K> callbackConsumer;
		private long priority;
		private boolean active = true;
		private PlanData planData;
		private Object key;
	}
	
	/**
	 * Returns a new instance of the Builder class
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_EVENT_CLASS } if the class
	 *             reference is null</li>
	 */
	public static <K> Builder<K> builder() {
		
		return new Builder<K>();
	}

	public static class Builder<K> {
		private Data<K> data = new Data<>();

		private Builder() {
			
		}

		/**
		 * Constructs a new EventLabel from the collected data.
		 * 
		 */
		public Plan2<K> build() {
			try {
				return new Plan2<>(data);
			} finally {
				data = new Data<>();
			}
		}

		/**
		 * Sets the time of the plan.
		 * 
		 */
		public Builder<K> setTime(double time) {
			data.time = time;
			return this;
		}

		/**
		 * Sets the relative priority in the planning queue. Only operates
		 * during the initialization phase of the simulation before time begins
		 * to flow and is used to re-establish the planning queue from a
		 * previous simulation run. Defaults to -1;
		 * 
		 */
		public Builder<K> setPriority(long priority) {
			data.priority = priority;
			return this;
		}

		/**
		 * Sets the active state for the plan. The planning queue continues
		 * execution while there are active plans present. Passive plans should
		 * be used for recurring, non-event driven tasks that do not require the
		 * continued execution of the simulation.Defaults to true;
		 * 
		 */
		public Builder<K> setActive(boolean active) {
			data.active = active;
			return this;
		}

		/**
		 * Sets the auxiliary plan data that will be used when the simulation is
		 * recording the content of the planning queue for use in a continued
		 * simulation. A non-null value is required when adding a plan that is
		 * scheduled at or after the simulation halt time when the simulation
		 * has been instructed to record state on halt. Defaults to false;
		 * 
		 */
		public Builder<K> setPlanData(PlanData planData) {
			data.planData = planData;
			return this;
		}

		/**
		 * Sets the key value for this plan. The key value is optional and
		 * should only be used if the plan may be cancelled or retrieved before
		 * the plan time since it incurs a significant overhead memory cost.
		 * Defaults to null.
		 * 
		 */
		public Builder<K> setKey(Object key) {
			data.key = key;
			return this;
		}

		/**
		 * Sets the required call back behavior for this plan. The call back is
		 * executed when the plan reaches the top of the queue and the
		 * simulation's time is set to the plan's time. No default is allowed.
		 * 
		 */
		public Builder<K> setCallbackConsumer(Consumer<K> callbackConsumer) {
			data.callbackConsumer = callbackConsumer;
			return this;
		}
	}

	private final Data<T> data;

	private Plan2(Data<T> data) {
		this.data = data;
	}

	public double getTime() {
		return data.time;
	}

	public Consumer<T> getCallbackConsumer() {
		return data.callbackConsumer;
	}

	public long getPriority() {
		return data.priority;
	}

	public boolean isActive() {
		return data.active;
	}

	public PlanData getPlanData() {
		return data.planData;
	}

	public Object getKey() {
		return data.key;
	}

}
