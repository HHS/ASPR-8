package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.events;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a region has been added
 */
@Immutable
public class RegionAdditionEvent implements Event {

	private static class Data {
		private RegionId regionId;
		private List<Object> values = new ArrayList<>();
		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			regionId = data.regionId;
			values.addAll(data.values);
			locked = data.locked;
		}
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private final Data data;

	/**
	 * Builder class for {@link RegionAdditionEvent}
	 */
	public static class Builder {

		private Data data;

		private Builder(Data data) {
			this.data = data;
		}

		private void validate() {
			if (data.regionId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
		}

		/**
		 * Builds the Region addition event from the inputs
		 * 
		 * @throws ContractException {@linkplain RegionError#NULL_REGION_ID} if the
		 *                           region id was not set
		 */
		public RegionAdditionEvent build() {
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new RegionAdditionEvent(data);
		}

		/**
		 * Sets the region id
		 * 
		 * @throws ContractException {@linkplain RegionError#NULL_REGION_ID} if the
		 *                           region id is null
		 */
		public Builder setRegionId(RegionId regionId) {
			ensureDataMutability();
			if (regionId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
			data.regionId = regionId;
			return this;
		}

		/**
		 * Adds an auxiliary value to be used by observers of region addition
		 * 
		 * @throws ContractException {@linkplain RegionError#NULL_AUXILIARY_DATA} if the
		 *                           value is null
		 */
		public Builder addValue(Object value) {
			ensureDataMutability();
			if (value == null) {
				throw new ContractException(RegionError.NULL_AUXILIARY_DATA);
			}
			data.values.add(value);
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
	}

	private RegionAdditionEvent(Data data) {
		this.data = data;
	}

	/**
	 * Returns the region id.
	 */
	public RegionId getRegionId() {
		return data.regionId;
	}

	/**
	 * Returns the (non-null) auxiliary objects that are instances of the given
	 * class in the order of their addition to the builder.
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> getValues(Class<T> c) {
		List<T> result = new ArrayList<>();
		for (Object value : data.values) {
			if (c.isAssignableFrom(value.getClass())) {
				result.add((T) value);
			}
		}
		return result;
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}
