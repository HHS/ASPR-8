package gov.hhs.aspr.ms.gcm.nucleus;

import java.util.function.Consumer;

public class Plan<T> {

	private static class Data<K> {
		private double time;
		private Consumer<K> callbackConsumer;
		private boolean active = true;

		public Data() {
		}

		public Data(Data<K> data) {
			time = data.time;
			callbackConsumer = data.callbackConsumer;
			active = data.active;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [time=");
			builder.append(time);
			builder.append(", callbackConsumer=");
			builder.append(callbackConsumer);
			builder.append(", active=");
			builder.append(active);
			builder.append("]");
			return builder.toString();
		}

	}

	/**
	 * Returns a new instance of the Builder class
	 */
	public static <K> Builder<K> builder(Class<K> classReference) {
		return new Builder<K>();
	}

	public static class Builder<K> {
		private Data<K> data = new Data<>();

		private Builder() {

		}

		/**
		 * Constructs a new EventLabel from the collected data.
		 */
		public Plan<K> build() {
			return new Plan<>(new Data<>(data));
		}

		/**
		 * Sets the time of the plan.
		 */
		public Builder<K> setTime(double time) {
			data.time = time;
			return this;
		}

		/**
		 * Sets the active state for the plan. The planning queue continues execution
		 * while there are active plans present. Passive plans should be used for
		 * recurring, non-event driven tasks that do not require the continued execution
		 * of the simulation.Defaults to true;
		 */
		public Builder<K> setActive(boolean active) {
			data.active = active;
			return this;
		}

		/**
		 * Sets the required call back behavior for this plan. The call back is executed
		 * when the plan reaches the top of the queue and the simulation's time is set
		 * to the plan's time. No default is allowed.
		 */
		public Builder<K> setCallbackConsumer(Consumer<K> callbackConsumer) {
			data.callbackConsumer = callbackConsumer;
			return this;
		}
	}

	private final Data<T> data;

	private Plan(Data<T> data) {
		this.data = data;
	}

	public double getTime() {
		return data.time;
	}

	public Consumer<T> getCallbackConsumer() {
		return data.callbackConsumer;
	}

	public boolean isActive() {
		return data.active;
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("Plan [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

}
