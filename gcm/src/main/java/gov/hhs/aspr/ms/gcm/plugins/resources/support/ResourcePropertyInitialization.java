package gov.hhs.aspr.ms.gcm.plugins.resources.support;

import java.util.Optional;

import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.util.errors.ContractException;

/**
 * Immutable data class that represents the addition of a resource property
 */
public class ResourcePropertyInitialization {

	private static class Data {
		private ResourceId resourceId;
		private ResourcePropertyId resourcePropertyId;
		private PropertyDefinition propertyDefinition;
		private Object value;

		public Data() {
		}

		public Data(Data data) {
			resourceId = data.resourceId;
			resourcePropertyId = data.resourcePropertyId;
			propertyDefinition = data.propertyDefinition;
			value = data.value;
		}

	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}
			if (data.resourcePropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (data.resourceId == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_ID);
			}
			if (data.value == null) {
				if (data.propertyDefinition.getDefaultValue().isEmpty()) {
					throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
				}
			} else {
				if (!data.propertyDefinition.getType().isAssignableFrom(data.value.getClass())) {
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE);
				}
			}
		}

		/**
		 * Returns the ResourcePropertyInitialization formed from the inputs.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if no property definition was provided</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           no property id was provided</li>
		 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
		 *                           no resource id was provided</li>
		 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *                           if no property value was provided and the property
		 *                           definition does not contain a default value</li>
		 *                           </ul>
		 */
		public ResourcePropertyInitialization build() {
			validate();
			return new ResourcePropertyInitialization(new Data(data));
		}

		/**
		 * Sets the resource id.
		 *
		 * @throws ContractException {@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *                           resource id is null
		 */
		public Builder setResourceId(ResourceId resourceId) {
			if (resourceId == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_ID);
			}
			data.resourceId = resourceId;
			return this;
		}

		/**
		 * Sets the resource property id.
		 * 
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *                           property id is null
		 */
		public Builder setResourcePropertyId(ResourcePropertyId resourcePropertyId) {
			if (resourcePropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.resourcePropertyId = resourcePropertyId;
			return this;
		}

		/**
		 * Sets the resource property definition.
		 * 
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if the property definition is null
		 */
		public Builder setPropertyDefinition(PropertyDefinition propertyDefinition) {
			if (propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}
			data.propertyDefinition = propertyDefinition;
			return this;
		}

		/**
		 * Sets the value of the global property that overrides any default value
		 * provided by the property definition
		 * 
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_VALUE} if
		 *                           the value is null
		 */
		public Builder setValue(Object value) {
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			data.value = value;
			return this;
		}
	}

	private final Data data;

	private ResourcePropertyInitialization(Data data) {
		this.data = data;
	}

	/**
	 * Returns the resource property id
	 */
	public ResourcePropertyId getResourcePropertyId() {
		return data.resourcePropertyId;
	}

	/**
	 * Returns the resource id
	 */
	public ResourceId getResourceId() {
		return data.resourceId;
	}

	/**
	 * Returns the property definition
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the property value
	 */
	public Optional<Object> getValue() {
		return Optional.ofNullable(data.value);
	}
}
