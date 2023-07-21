package lesson.plugins.model.actors.vaccinator;

import java.util.LinkedHashMap;
import java.util.Map;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
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
			// nothing to validate
		}

	}

	private static class Data {
		private final Map<RegionId, Double> vaccinationSchedules = new LinkedHashMap<>();
		private final Map<RegionId, Long> availableVaccines = new LinkedHashMap<>();
		private int infectionPersonCountThreshold;
		private int infectedPersonCount;
		private boolean manufactureStarted;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {

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
			builder.append(", locked=");
			builder.append(locked);
			builder.append("]");
			return builder.toString();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((availableVaccines == null) ? 0 : availableVaccines.hashCode());
			result = prime * result + infectedPersonCount;
			result = prime * result + infectionPersonCountThreshold;
			result = prime * result + (locked ? 1231 : 1237);
			result = prime * result + (manufactureStarted ? 1231 : 1237);
			result = prime * result + ((vaccinationSchedules == null) ? 0 : vaccinationSchedules.hashCode());
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
			if (availableVaccines == null) {
				if (other.availableVaccines != null) {
					return false;
				}
			} else if (!availableVaccines.equals(other.availableVaccines)) {
				return false;
			}
			if (infectedPersonCount != other.infectedPersonCount) {
				return false;
			}
			if (infectionPersonCountThreshold != other.infectionPersonCountThreshold) {
				return false;
			}
			if (locked != other.locked) {
				return false;
			}
			if (manufactureStarted != other.manufactureStarted) {
				return false;
			}
			if (vaccinationSchedules == null) {
				if (other.vaccinationSchedules != null) {
					return false;
				}
			} else if (!vaccinationSchedules.equals(other.vaccinationSchedules)) {
				return false;
			}
			return true;
		}
		
		

	}

	/**
	 * Returns a Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
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

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
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
		if (result == null) {
			result = 0.0;
		}
		return result;
	}

	public Long getAvailableVaccine(RegionId regionId) {
		Long result = data.availableVaccines.get(regionId);
		if (result == null) {
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
		if (!(obj instanceof VaccinatorPluginData)) {
			return false;
		}
		VaccinatorPluginData other = (VaccinatorPluginData) obj;
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
