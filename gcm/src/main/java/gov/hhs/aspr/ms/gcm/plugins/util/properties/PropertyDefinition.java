package gov.hhs.aspr.ms.gcm.plugins.util.properties;

import java.util.Optional;

import net.jcip.annotations.ThreadSafe;
import util.errors.ContractException;

/**
 * A thread-safe, immutable class that defines a property, but does not indicate
 * the role that property is playing or the identifier of the property.
 */
@ThreadSafe
public final class PropertyDefinition {

	public static Builder builder() {
		return new Builder();
	}

	private static class Data {

		private Class<?> type = null;

		private boolean propertyValuesAreMutable = true;

		private Object defaultValue = null;

		public Data() {
		}

		public Data(Data data) {
			type = data.type;
			propertyValuesAreMutable = data.propertyValuesAreMutable;
			defaultValue = data.defaultValue;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
			result = prime * result + (propertyValuesAreMutable ? 1231 : 1237);
			result = prime * result + type.hashCode();
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
			if (defaultValue == null) {
				if (other.defaultValue != null) {
					return false;
				}
			} else if (!defaultValue.equals(other.defaultValue)) {
				return false;
			}
			if (propertyValuesAreMutable != other.propertyValuesAreMutable) {
				return false;
			}
			if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Builder class for {@linkplain PropertyDefinition}
	 */
	public static class Builder {

		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.type == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_TYPE);
			}

			if (data.defaultValue != null) {
				if (!data.type.isInstance(data.defaultValue)) {
					throw new ContractException(PropertyError.INCOMPATIBLE_DEFAULT_VALUE);
				}
			}
		}

		/**
		 * Builds the property definition
		 * 
		 * @throws ContractException
		 *                           <ul><li>{@linkplain PropertyError#NULL_PROPERTY_TYPE}
		 *                           if the class type of the definition is not assigned
		 *                           or null</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_DEFAULT_VALUE}if
		 *                           the default value is not null and the class type is
		 *                           not a super-type of the default value</li></ul>
		 */
		public PropertyDefinition build() {
			validate();
			return new PropertyDefinition(new Data(data));
		}

		/**
		 * Sets the class type. Value must be set by client.
		 */
		public Builder setType(final Class<?> type) {
			data.type = type;
			return this;
		}

		/**
		 * Sets property value mutability during simulation run time. Default value is
		 * true.
		 */
		public Builder setPropertyValueMutability(boolean propertyValuesAreMutable) {
			data.propertyValuesAreMutable = propertyValuesAreMutable;
			return this;
		}

		/**
		 * Sets the default value for property values of this definition. Value must be
		 * set(non-null) by client.
		 */
		public Builder setDefaultValue(Object defaultValue) {
			data.defaultValue = defaultValue;
			return this;
		}

	}

	private final Data data;

	private PropertyDefinition(Data data) {
		this.data = data;
	}

	/**
	 * Returns the Optional<Object> containing default value for the property
	 * definition. Null values are allowed as a convenience. Any property definition
	 * that has a null default value must have corresponding property value
	 * assignments within plugin initial data that cover all cases. Property
	 * definitions for dynamically generated relationships MUST contain non-null
	 * default values since they are created after data initialization of the
	 * plugins.
	 */
	public Optional<Object> getDefaultValue() {
		if (data.defaultValue == null) {
			return Optional.empty();
		}
		return Optional.of(data.defaultValue);
	}

	/**
	 * Returns that class type of this definition. It is used to ensure that all
	 * values assigned to properties have a predictable type from the modeler's
	 * perspective. Property assignments are descendant class tolerant. For example,
	 * a property having a defined type of Number, would accept values that are
	 * Double, Integer or any other descendant type.
	 */
	public Class<?> getType() {
		return data.type;
	}

	/**
	 * The modeler may define a property such that all associated property values
	 * must be equal to the default value of this property definition. Any attempt
	 * to assign a value to a property so defined will result in an error. This can
	 * be used to ensure that some variables remain constant throughout the run of a
	 * simulation instance. Returns true if and only if the value of the property
	 * must remain constant.
	 */
	public boolean propertyValuesAreMutable() {
		return data.propertyValuesAreMutable;
	}

	/**
	 * Standard string representation in the form: PropertyDefinition
	 * [type=someType,mapOption=mapOption, constantPropertyValues=true,
	 * defaultValue=someValue, timeTrackingPolicy=policy]
	 */
	@Override
	public String toString() {
		StringBuilder builder2 = new StringBuilder();
		builder2.append("PropertyDefinition [type=");
		builder2.append(data.type);
		builder2.append(", propertyValuesAreMutable=");
		builder2.append(data.propertyValuesAreMutable);
		builder2.append(", defaultValue=");
		builder2.append(data.defaultValue);
		builder2.append("]");
		return builder2.toString();
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
		if (!(obj instanceof PropertyDefinition)) {
			return false;
		}
		PropertyDefinition other = (PropertyDefinition) obj;

		if (!data.equals(other.data)) {
			return false;
		}
		return true;
	}

}