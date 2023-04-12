package lesson.plugins.model.actors.contactmanager;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;

/**
 * An immutable container of the initial state of actor plans
 * 
 *
 */
@Immutable
public final class ContactManagerPluginData implements PluginData {

	/**
	 * Builder class for ActorPluginData
	 * 
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Returns the {@link ActorPluginData} from the collected information
		 * supplied to this builder. Clears the builder's state.
		 *
		 * 
		 */
		public ContactManagerPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new ContactManagerPluginData(data);
		}

		

		/**
		 * Sets the minInfectiousPeriod.
		 * 
		 */
		public Builder setMinInfectiousPeriod(int minInfectiousPeriod) {
			ensureDataMutability();			
			data.minInfectiousPeriod = minInfectiousPeriod;
			return this;
		}
		/**
		 * Sets the maxInfectiousPeriod.
		 * 
		 */
		public Builder setMaxInfectiousPeriod(int maxInfectiousPeriod) {
			ensureDataMutability();			
			data.maxInfectiousPeriod = maxInfectiousPeriod;
			return this;
		}
		/**
		 * Sets the infectionInterval.
		 * 
		 */
		public Builder setInfectionInterval(double infectionInterval) {
			ensureDataMutability();			
			data.infectionInterval = infectionInterval;
			return this;
		}
		
		/**
		 * Sets the communityContactRate.
		 * 
		 */
		public Builder setCommunityContactRate(double communityContactRate) {
			ensureDataMutability();			
			data.communityContactRate = communityContactRate;
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

		private void validateData() {
			// nothing to validate
		}

	}

	private static class Data {
		private int minInfectiousPeriod;
		private int maxInfectiousPeriod;
		private double infectionInterval;
		private double communityContactRate;
		private boolean locked;

		

		private Data() {
		}

		private Data(Data data) {			
			minInfectiousPeriod = data.minInfectiousPeriod;
			maxInfectiousPeriod = data.maxInfectiousPeriod;
			infectionInterval = data.infectionInterval;
			communityContactRate = data.communityContactRate;
			locked = data.locked;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [minInfectiousPeriod=");
			builder.append(minInfectiousPeriod);
			builder.append(", maxInfectiousPeriod=");
			builder.append(maxInfectiousPeriod);
			builder.append(", infectionInterval=");
			builder.append(infectionInterval);
			builder.append(", communityContactRate=");
			builder.append(communityContactRate);
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

		
		

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	private ContactManagerPluginData(final Data data) {
		this.data = data;
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	@Override
	public PluginDataBuilder getEmptyBuilder() {
		return builder();
	}

	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("ContactManagerPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}
	
	public int getMinInfectiousPeriod() {
		return data.minInfectiousPeriod;
	}

	public int getMaxInfectiousPeriod() {
		return data.maxInfectiousPeriod;
	}

	public double getInfectionInterval() {
		return data.infectionInterval;
	}

	public double getCommunityContactRate() {
		return data.communityContactRate;
	}

}
