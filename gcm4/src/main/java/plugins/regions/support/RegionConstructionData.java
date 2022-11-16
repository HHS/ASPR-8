package plugins.regions.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import plugins.util.properties.PropertyError;
import util.errors.ContractException;

public class RegionConstructionData {

	private static class Data {
		private RegionId regionId;
		private List<Object> values = new ArrayList<>();
		private Map<RegionPropertyId, Object> propertyValues = new LinkedHashMap<>();
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	/**
	 * Static builder class for {@link RegionConstructionData}
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

		private Builder(Data data) {
			this.data = data;
		}
		/**
		 * Builds the Region Construction Data from the given inputs.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region
		 *             id was not set</li>
		 */
		public RegionConstructionData build() {
			try {
				validate();
				return new RegionConstructionData(data);
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

		/**
		 * Sets a region property value
		 * 
		 * @throws ContractException
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             region property id is null</li> 
		 * 
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             value is null</li>
		 * 
		 *             <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_VALUE_ASSIGNMENT}
		 *             if the region property was previously set</li>
		 */
		public Builder setRegionPropertyValue(RegionPropertyId regionPropertyId, Object value) {
			if (regionPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			if (data.propertyValues.containsKey(regionPropertyId)) {
				throw new ContractException(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT);
			}
			data.propertyValues.put(regionPropertyId, value);
			return this;
		}

	}

	private final Data data;

	private RegionConstructionData(Data data) {
		this.data = data;
	}

	/**
	 * Returns a non-null region id
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
	 * Returns an unmodifiable map of region property ids to values
	 */
	public Map<RegionPropertyId, Object> getRegionPropertyValues() {
		return Collections.unmodifiableMap(data.propertyValues);
	}
}
