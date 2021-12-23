package plugins.partitions.testsupport.attributes.support;

import net.jcip.annotations.ThreadSafe;
import util.ContractException;

/**
 * A thread-safe, immutable class that defines an attribute, but does not
 * indicate the role that attribute is playing or the identifier of the
 * attribute.
 *
 * @author Shawn Hatch
 *
 */
@ThreadSafe
public final class AttributeDefinition {

	public static Builder builder() {
		return new Builder();
	}

	private static class Scaffold {

		private Class<?> type = null;

		private Object defaultValue = null;

	}

	/**
	 * Builder class for {@linkplain AttributeDefinition}
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static class Builder {

		private Scaffold scaffold = new Scaffold();

		private Builder() {
		}

		/**
		 * Builds the attribute definition
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain AttributeError#NULL_ATTRIBUTE_TYPE} if
		 *             the class type of the definition is not assigned or
		 *             null</li>
		 * 
		 *             <li>{@linkplain AttributeError#NULL_DEFAULT_VALUE}if the
		 *             default value null</li>
		 * 
		 *             <li>{@linkplain AttributeError#INCOMPATIBLE_DEFAULT_VALUE}if
		 *             the class type is not a super-type of the default
		 *             value</li>
		 * 
		 */
		public AttributeDefinition build() {
			try {
				return new AttributeDefinition(scaffold);
			} finally {
				scaffold = new Scaffold();
			}
		}

		/**
		 * Sets the class type. Value must be set by client.
		 */
		public Builder setType(final Class<?> type) {
			scaffold.type = type;
			return this;
		}

		/**
		 * Sets the default value for the attribute.
		 */
		public Builder setDefaultValue(Object defaultValue) {
			scaffold.defaultValue = defaultValue;
			return this;
		}

	}

	private final Class<?> type;

	private final Object defaultValue;

	private AttributeDefinition(Scaffold scaffold) {

		if (scaffold.type == null) {
			throw new ContractException(AttributeError.NULL_ATTRIBUTE_TYPE);
		}

		if (scaffold.defaultValue == null) {
			throw new ContractException(AttributeError.NULL_DEFAULT_VALUE);
		}

		if (!scaffold.type.isInstance(scaffold.defaultValue)) {
			throw new ContractException(AttributeError.INCOMPATIBLE_DEFAULT_VALUE);
		}

		this.type = scaffold.type;

		this.defaultValue = scaffold.defaultValue;

	}

	/**
	 * Returns the default value.
	 */
	public Object getDefaultValue() {
		return defaultValue;
	}

	/**
	 * Returns that class type of this definition.
	 *
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
	 * Attribute definitions are equal if they have the same type and default
	 * value.
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
	 * Standard string representation in the form:
	 * 
	 * AttributeDefinition
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