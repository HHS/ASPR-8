package lesson.plugins.model.actors.contactmanager;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;
import nucleus.NucleusError;
import nucleus.PlanData;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.PrioritizedPlanData;
import util.errors.ContractException;

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
		 * Adds a plan data.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain NucleusError.NULL_PLAN_DATA}</li> if the
		 *             plan data is null
		 */
		public Builder addPrioritizedPlanData(final PrioritizedPlanData prioritizedPlanData) {
			ensureDataMutability();
			validatePlanDataNotNull(prioritizedPlanData);
			data.prioritizedPlanDatas.add(prioritizedPlanData);
			return this;
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

		private final List<PrioritizedPlanData> prioritizedPlanDatas = new ArrayList<>();
		private int minInfectiousPeriod;
		private int maxInfectiousPeriod;
		private double infectionInterval;
		private double communityContactRate;
		private boolean locked;

		

		private Data() {
		}

		private Data(Data data) {
			prioritizedPlanDatas.addAll(data.prioritizedPlanDatas);
			minInfectiousPeriod = data.minInfectiousPeriod;
			maxInfectiousPeriod = data.maxInfectiousPeriod;
			infectionInterval = data.infectionInterval;
			communityContactRate = data.communityContactRate;
			locked = data.locked;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [prioritizedPlanDatas=");
			builder.append(prioritizedPlanDatas);
			builder.append(", minInfectiousPeriod=");
			builder.append(minInfectiousPeriod);
			builder.append(", maxInfectiousPeriod=");
			builder.append(maxInfectiousPeriod);
			builder.append(", infectionInterval=");
			builder.append(infectionInterval);
			builder.append(", communityContactRate=");
			builder.append(communityContactRate);
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

	private static void validatePlanDataNotNull(final PrioritizedPlanData prioritizedPlanData) {
		if (prioritizedPlanData == null) {
			throw new ContractException(NucleusError.NULL_PLAN_DATA);
		}
	}

	

	private final Data data;

	private ContactManagerPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the {@link PlanData} objects that are instances of the give class
	 * reference
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain NucleusError#NULL_CLASS_REFERENCE}</li> if
	 *             the class reference is null
	 * 
	 */
	public List<PrioritizedPlanData> getPrioritizedPlanDatas(final Class<?> classReference) {
		validateClassReferenceNotNull(classReference);
		List<PrioritizedPlanData> result = new ArrayList<>();
		for (PrioritizedPlanData prioritizedPlanData : data.prioritizedPlanDatas) {
			if (classReference.isAssignableFrom(prioritizedPlanData.getPlanData().getClass())) {
				result.add(prioritizedPlanData);
			}
		}

		return result;
	}

	private static void validateClassReferenceNotNull(final Class<?> classReference) {
		if (classReference == null) {
			throw new ContractException(NucleusError.NULL_CLASS_REFERENCE);
		}

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
