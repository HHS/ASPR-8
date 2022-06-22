package plugins.globalproperties.support;

import java.util.Optional;

import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

public class GlobalPropertyInitialization {

	private static class Data {
		private GlobalPropertyId globalPropertyId;
		private PropertyDefinition propertyDefinition;
		private Object value;

	}

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
			if (data.globalPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (data.value == null) {
				if (data.propertyDefinition.getDefaultValue().isEmpty()) {
					throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
				}
			}
		}

		public GlobalPropertyInitialization build() {
			try {
				validate();
				return new GlobalPropertyInitialization(data);
			} finally {
				data = new Data();
			}
		}

		public Builder setGlobalPropertyId(GlobalPropertyId globalPropertyId) {
			data.globalPropertyId = globalPropertyId;
			return this;
		}

		public Builder setPropertyDefinition(PropertyDefinition propertyDefinition) {
			data.propertyDefinition = propertyDefinition;
			return this;
		}

		public Builder setValue(Object value) {
			data.value = value;
			return this;
		}
	}

	private final Data data;

	private GlobalPropertyInitialization(Data data) {
		this.data = data;
	}

	public GlobalPropertyId getGlobalPropertyId() {
		return data.globalPropertyId;
	}

	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	public Optional<Object> getValue() {
		return Optional.ofNullable(data.value);
	}
}
