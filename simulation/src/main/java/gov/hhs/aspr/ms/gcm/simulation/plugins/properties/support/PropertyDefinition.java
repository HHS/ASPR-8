package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support;

import java.util.Objects;
import java.util.Optional;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A thread-safe, immutable class that defines a property, but does not indicate
 * the role that property is playing or the identifier of the property.
 */
@ThreadSafe
public final class PropertyDefinition {

	public static Builder builder() {
		return new Builder(new Data());
	}

	private static class Data {

		private Class<?> type = null;

		private boolean propertyValuesAreMutable = true;

		private Object defaultValue = null;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			type = data.type;
			propertyValuesAreMutable = data.propertyValuesAreMutable;
			defaultValue = data.defaultValue;
			locked = data.locked;
		}

		/**
    	 * Standard implementation consistent with the {@link #equals(Object)} method
    	 */
		@Override
		public int hashCode() {
			return Objects.hash(type, propertyValuesAreMutable, defaultValue);
		}

		/**
    	 * Two {@link Data} instances are equal if and only if
    	 * their inputs are equal.
    	 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Data other = (Data) obj;
			return Objects.equals(type, other.type) && propertyValuesAreMutable == other.propertyValuesAreMutable
					&& Objects.equals(defaultValue, other.defaultValue);
		}

	}

	/**
	 * Builder class for {@linkplain PropertyDefinition}
	 */
	public static class Builder {

		private Data data;

		private Builder(Data data) {
			this.data = data;
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
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_TYPE}
		 *                           if the class type of the definition is not assigned
		 *                           or null</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_DEFAULT_VALUE}if
		 *                           the default value is not null and the class type is
		 *                           not a super-type of the default value</li>
		 *                           </ul>
		 */
		public PropertyDefinition build() {
			if (!data.locked) {
				validate();
			}
			ensureImmutability();
			return new PropertyDefinition(data);
		}

		/**
		 * Sets the class type. Value must be set by client.
		 */
		public Builder setType(final Class<?> type) {
			ensureDataMutability();
			data.type = type;
			return this;
		}

		/**
		 * Sets property value mutability during simulation run time. Default value is
		 * true.
		 */
		public Builder setPropertyValueMutability(boolean propertyValuesAreMutable) {
			ensureDataMutability();
			data.propertyValuesAreMutable = propertyValuesAreMutable;
			return this;
		}

		/**
		 * Sets the default value for property values of this definition. Value must be
		 * set(non-null) by client.
		 */
		public Builder setDefaultValue(Object defaultValue) {
			ensureDataMutability();
			data.defaultValue = defaultValue;
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

	private final Data data;

	private PropertyDefinition(Data data) {
		this.data = data;
	}

	/**
	 * Returns the Optional containing default value for the property definition.
	 * Null values are allowed as a convenience. Any property definition that has a
	 * null default value must have corresponding property value assignments within
	 * plugin initial data that cover all cases. Property definitions for
	 * dynamically generated relationships MUST contain non-null default values
	 * since they are created after data initialization of the plugins.
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

	/**
	 * Standard implementation consistent with the {@link #equals(Object)} method
	 */
	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

	/**
	 * Two {@link PropertyDefinition} instances are equal if and only if
	 * their inputs are equal.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PropertyDefinition other = (PropertyDefinition) obj;
		return Objects.equals(data, other.data);
	}

	public Builder toBuilder() {
		return new Builder(data);
	}

}