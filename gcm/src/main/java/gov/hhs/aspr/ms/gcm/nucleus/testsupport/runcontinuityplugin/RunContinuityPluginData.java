package gov.hhs.aspr.ms.gcm.nucleus.testsupport.runcontinuityplugin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginDataBuilder;
import net.jcip.annotations.Immutable;

/**
 * An immutable container of the initial state of continuity properties.
 */
@Immutable
public class RunContinuityPluginData implements PluginData {

	private static class Data {

		private List<Pair<Double, Consumer<ActorContext>>> consumers = new ArrayList<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {			
			consumers.addAll(data.consumers);
			locked = data.locked;
		}

	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Builder class for RunContinuityPluginData
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

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

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Builds the {@linkplain RunContinuityPluginData} from the collected data.
		 */
		public RunContinuityPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new RunContinuityPluginData(data);
		}

		/**
		 * Schedules a context consumer
		 */
		public Builder addContextConsumer(final double time, final Consumer<ActorContext> consumer) {
			ensureDataMutability();
			data.consumers.add(new Pair<>(time, consumer));
			return this;
		}


		private void validateData() {
			// do nothing
		}

	}

	private final Data data;

	private RunContinuityPluginData(Data data) {
		this.data = data;
	}

	/**
	 * Returns the list scheduled consumers
	 */
	public List<Pair<Double, Consumer<ActorContext>>> getConsumers() {
		return new ArrayList<>(data.consumers);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	/**
	 * Returns true if the completion count is greater than or equal to the number
	 * of contained consumers.
	 */
	public boolean allPlansComplete() {
		return data.consumers.isEmpty();
	}

}
