package gov.hhs.aspr.ms.gcm.plugins.partitions.testsupport.attributes.support;

import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.ThreadSafe;

/**
 * A thread-safe, immutable class that defines an attribute, but does not
 * indicate the role that attribute is playing or the identifier of the
 * attribute.
 */
@ThreadSafe
public final class AttributeDefinition {

	public static Builder builder() {
		return new Builder();
	}

	private static class Data {

		private Class<?> type = null;

		private Object defaultValue = null;

		public Data() {
		}

		public Data(Data data) {
			type = data.type;
			defaultValue = data.defaultValue;
		}

	}

	/**
	 * Builder class for {@linkplain AttributeDefinition}
	 */
	public static class Builder {

		private Data data = new Data();

		private Builder() {
		}

		/**
		 * Builds the attribute definition
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain AttributeError#NULL_ATTRIBUTE_TYPE}
		 *                           if the class type of the definition is not assigned
		 *                           or null</li>
		 *                           <li>{@linkplain AttributeError#NULL_DEFAULT_VALUE}if
		 *                           the default value null</li>
		 *                           <li>{@linkplain AttributeError#INCOMPATIBLE_DEFAULT_VALUE}if
		 *                           the class type is not a super-type of the default
		 *                           value</li>
		 *                           </ul>
		 */
		public AttributeDefinition build() {
			return new AttributeDefinition(new Data(data));
		}

		/**
		 * Sets the class type. Value must be set by client.
		 */
		public Builder setType(final Class<?> type) {
			data.type = type;
			return this;
		}

		/**
		 * Sets the default value for the attribute.
		 */
		public Builder setDefaultValue(Object defaultValue) {
			data.defaultValue = defaultValue;
			return this;
		}

	}

	private final Class<?> type;

	private final Object defaultValue;

	private AttributeDefinition(Data data) {

		if (data.type == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_TYPE);
		}

		if (data.defaultValue == null) {
			throw new ContractException(AttributeError.NULL_DEFAULT_VALUE);
		}

		if (!data.type.isInstance(data.defaultValue)) {
			throw new ContractException(AttributeError.INCOMPATIBLE_DEFAULT_VALUE);
		}

		this.type = data.type;

		this.defaultValue = data.defaultValue;

	}

	/**
	 * Returns the default value.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns that class type of this definition.
	 */
	public Class<?> getType() {
		return type;
	}

	/**
	 * Boilerplate implementation that uses all fields.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}

	/**
	 * Attribute definitions are equal if they have the same type and default value.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeDefinition other = (AttributeDefinition) obj;

		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;

		if (type == null) {
			return other.type == null;
		} else
			return type.equals(other.type);
	}

	/**
	 * Standard string representation in the form: AttributeDefinition
	 * [type=someType,mapOption=mapOption,defaultValue=someValue]
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("AttributeDefinition [type=");
		builder.append(type);
		builder.append(", defaultValue=");
		builder.append(defaultValue);
		builder.append("]");
		return builder.toString();
	}

}