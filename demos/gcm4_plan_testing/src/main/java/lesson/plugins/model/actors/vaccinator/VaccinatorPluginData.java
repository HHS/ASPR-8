package lesson.plugins.model.actors.vaccinator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.jcip.annotations.Immutable;
import nucleus.NucleusError;
import nucleus.PlanData;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import nucleus.PrioritizedPlanData;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.errors.ContractException;

/**
 * An immutable container of the initial state of actor plans
 * 
 *
 */
@Immutable
public final class VaccinatorPluginData implements PluginData {

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
		public VaccinatorPluginData build() {
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new VaccinatorPluginData(data);
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
		 * Sets the vaccine schedule time for the given region
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
		 *             region id is null
		 */
		public Builder setVaccinationSchedule(RegionId regionId, double time) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			data.vaccinationSchedules.put(regionId, time);
			return this;
		}

		/**
		 * Sets the avaiable vaccine count for the the given region
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
		 *             region id is null
		 */
		public Builder setAvailableVaccines(RegionId regionId, long count) {
			ensureDataMutability();
			validateRegionIdNotNull(regionId);
			data.availableVaccines.put(regionId, count);
			return this;
		}

		/**
		 * Sets the infection Person Count Threshold
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
		 *             region id is null
		 */
		public Builder setInfectionPersonCountThreshold(int infectionPersonCountThreshold) {
			ensureDataMutability();
			data.infectionPersonCountThreshold = infectionPersonCountThreshold;
			return this;
		}

		/**
		 * Sets the infected Person Count Threshold
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
		 *             region id is null
		 */
		public Builder setInfectedPersonCount(int infectedPersonCount) {
			ensureDataMutability();
			data.infectedPersonCount = infectedPersonCount;
			return this;
		}

		/**
		 * Sets the manufactureStarted state
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID}</li> if the
		 *             region id is null
		 */
		public Builder setManufactureStarted(boolean manufactureStarted) {
			ensureDataMutability();
			data.manufactureStarted = manufactureStarted;
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
			//nothing to validate
		}

	}

	private static class Data {
		private final Map<RegionId, Double> vaccinationSchedules = new LinkedHashMap<>();
		private final Map<RegionId, Long> availableVaccines = new LinkedHashMap<>();
		private int infectionPersonCountThreshold;
		private int infectedPersonCount;
		private boolean manufactureStarted;
		private final List<PrioritizedPlanData> prioritizedPlanDatas = new ArrayList<>();

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			prioritizedPlanDatas.addAll(data.prioritizedPlanDatas);
			vaccinationSchedules.putAll(data.vaccinationSchedules);
			availableVaccines.putAll(data.availableVaccines);
			infectionPersonCountThreshold = data.infectionPersonCountThreshold;
			infectedPersonCount = data.infectedPersonCount;
			manufactureStarted = data.manufactureStarted;

			locked = data.locked;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [vaccinationSchedules=");
			builder.append(vaccinationSchedules);
			builder.append(", availableVaccines=");
			builder.append(availableVaccines);
			builder.append(", infectionPersonCountThreshold=");
			builder.append(infectionPersonCountThreshold);
			builder.append(", infectedPersonCount=");
			builder.append(infectedPersonCount);
			builder.append(", manufactureStarted=");
			builder.append(manufactureStarted);
			builder.append(", prioritizedPlanDatas=");
			builder.append(prioritizedPlanDatas);
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

	private static void validatePlanDataNotNull(final PrioritizedPlanData prioritizedPlanData) {
		if (prioritizedPlanData == null) {
			throw new ContractException(NucleusError.NULL_PLAN_DATA);
		}
	}

	private static void validateRegionIdNotNull(final RegionId regionId) {
		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}
	}

	private final Data data;

	private VaccinatorPluginData(final Data data) {
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
		builder2.append("VaccinatorPluginData [data=");
		builder2.append(data);
		builder2.append("]");
		return builder2.toString();
	}

	public Double getVaccinationSchedule(RegionId regionId) {
		Double result = data.vaccinationSchedules.get(regionId);
		if(result == null) {
			result = 0.0;
		}
		return result;
	}

	public Long getAvailableVaccine(RegionId regionId) {
		Long result = data.availableVaccines.get(regionId);
		if(result == null) {
			result = 0L;
		}
		return result;
	}

	public int getInfectionPersonCountThreshold() {
		return data.infectionPersonCountThreshold;
	}

	public int getInfectedPersonCount() {
		return data.infectedPersonCount;
	}

	public boolean isManufactureStarted() {
		return data.manufactureStarted;
	}

}
