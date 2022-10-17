package plugins.regions.events;

import java.util.ArrayList;
import java.util.List;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import util.errors.ContractException;

/**
 * An event indicating that a region has been added
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class RegionAdditionEvent implements Event {

	private static class Data {
		private RegionId regionId;
		private List<Object> values = new ArrayList<>();
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final Data data;

	/**
	 * Builder class for {@link RegionAdditionEvent}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Data data = new Data();

		private void validate() {
			if (data.regionId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
		}

		/**
		 * Builds the Region addition event from the inputs
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id was not set</li>
		 */
		public RegionAdditionEvent build() {
			try {
				validate();
				return new RegionAdditionEvent(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the region id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id is null</li>
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
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_AUXILIARY_DATA} if the
		 *             value is null</li>
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
