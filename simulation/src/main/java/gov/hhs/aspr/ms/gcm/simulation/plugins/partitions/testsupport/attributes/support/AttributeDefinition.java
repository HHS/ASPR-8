package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support;

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
		return new Builder(new Data());
	}

	private static class Data {

		private Class<?> type = null;

		private Object defaultValue = null;

		private boolean locked;

		private Data() {
		}

		private Data(Data data) {
			type = data.type;
			defaultValue = data.defaultValue;
			locked = data.locked;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
			result = prime * result + ((type == null) ? 0 : type.hashCode());
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
			if (type == null) {
				if (other.type != null) {
					return false;
				}
			} else if (!type.equals(other.type)) {
				return false;
			}
			return true;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Data [type=");
			builder.append(type);
			builder.append(", defaultValue=");
			builder.append(defaultValue);
			builder.append("]");
			return builder.toString();
		}

		
	}

	/**
	 * Builder class for {@linkplain AttributeDefinition}
	 */
	public static class Builder {

		private Data data;

		private Builder(Data data) {
			this.data = data;
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
			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new AttributeDefinition(data);
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
		 * Sets the default value for the attribute.
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

		private void validateData() {
			if (data.type == null) {
				throw new ContractException(AttributeError.NULL_ATTRIBUTE_TYPE);
			}
	
			if (data.defaultValue == null) {
				throw new ContractException(AttributeError.NULL_DEFAULT_VALUE);
			}
	
			if (!data.type.isInstance(data.defaultValue)) {
				throw new ContractException(AttributeError.INCOMPATIBLE_DEFAULT_VALUE);
			}
		}

	}

	private final Data data;

	private AttributeDefinition(Data data) {
		this.data = data;
	}

	/**
	 * Returns the default value.
	 */
	public Object getDefaultValue() {
		return data.defaultValue;
	}

	/**
	 * Returns that class type of this definition.
	 */
	public Class<?> getType() {
		return data.type;
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
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AttributeDefinition other = (AttributeDefinition) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "AttributeDefinition [data=" + data + "]";
	}
	
	/**
	 * Returns a new builder instance that is pre-filled with the current state of
	 * this instance.
	 */
	public Builder toBuilder() {
		return new Builder(data);
	}

}