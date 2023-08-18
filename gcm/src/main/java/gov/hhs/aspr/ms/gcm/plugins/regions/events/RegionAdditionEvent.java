package gov.hhs.aspr.ms.gcm.plugins.regions.events;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionError;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event indicating that a region has been added
 */

@Immutable
public class RegionAdditionEvent implements Event {

	private static class Data {
		private RegionId regionId;
		private List<Object> values = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			regionId = data.regionId;
			values.addAll(data.values);
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

		private Data data = new Data();

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
			validate();
			return new RegionAdditionEvent(new Data(data));
		}

		/**
		 * Sets the region id
		 * 
		 * @throws ContractException {@linkplain RegionError#NULL_REGION_ID} if the
		 *                           region id is null
		 */
		public Builder setRegionId(RegionId regionId) {
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
			if (value == null) {
				throw new ContractException(RegionError.NULL_AUXILIARY_DATA);
			}
			data.values.add(value);
			return this;
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

}
