package plugins.regions.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import net.jcip.annotations.Immutable;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * A class for defining a person property with an associated property id
 * and property values for extant people.
 * 
 */
@Immutable
public final class RegionPropertyDefinitionInitialization {

	private static class Data {
		RegionPropertyId regionPropertyId;
		PropertyDefinition propertyDefinition;
		List<Pair<RegionId, Object>> propertyValues = new ArrayList<>();
	}

	private final Data data;

	private RegionPropertyDefinitionInitialization(Data data) {
		this.data = data;
	}
	
	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for a PropertyDefinitionInitialization
	 * 
	 */
	public final static class Builder {
		
		private Builder() {}

		private Data data = new Data();

		private void validate() {
			if (data.propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}

			if (data.regionPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}

			Class<?> type = data.propertyDefinition.getType();
			for (Pair<RegionId, Object> pair : data.propertyValues) {
				Object value = pair.getSecond();
				if (!type.isAssignableFrom(value.getClass())) {
					String message = "Definition Type " + type.getName() + " is not compatible with value = " + value;
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, message);
				}
			}
		}

		/**
		 * Constructs the PersonPropertyDefinitionInitialization from the collected
		 * data
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if no property definition was assigned to the
		 *             builder</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if no
		 *             property id was assigned to the builder</li>
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             collected property value is incompatible with the
		 *             property definition</li>
		 */
		public RegionPropertyDefinitionInitialization build() {
			try {
				validate();
				return new RegionPropertyDefinitionInitialization(data);
			} finally {
				data = new Data();
			}
		}

		/**
		 * Sets the property id
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             property id is null</li>
		 */
		public Builder setRegionPropertyId(RegionPropertyId regionPropertyId) {
			if (regionPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.regionPropertyId = regionPropertyId;
			return this;
		}

		/**
		 * Sets the property definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the property definition is null</li>
		 */
		public Builder setPropertyDefinition(PropertyDefinition propertyDefinition) {
			if (propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}
			data.propertyDefinition = propertyDefinition;
			return this;
		}

		/**
		 * Adds a property value
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_OWNER} if the
		 *             property owner is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             property value is null</li>
		 */

		public Builder addPropertyValue(RegionId regionId, Object value) {
			if (regionId == null) {
				throw new ContractException(RegionError.NULL_REGION_ID);
			}
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}

			data.propertyValues.add(new Pair<>(regionId, value));
			return this;
		}

	}

	/**
	 * Returns the (non-null) region property id.
	 */
	public RegionPropertyId getRegionPropertyId() {
		return data.regionPropertyId;
	}

	/**
	 * Returns the (non-null) property definition.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the list of (region,value) pairs collected by the builder in the
	 * order of their addition. All pairs have non-null entries and the values
	 * are compatible with the contained property definition. Duplicate
	 * assignments of values to the same owner may be present.
	 */
	public List<Pair<RegionId, Object>> getPropertyValues() {
		return Collections.unmodifiableList(data.propertyValues);
	}

}
