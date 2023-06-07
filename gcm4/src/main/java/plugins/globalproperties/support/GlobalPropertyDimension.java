package plugins.globalproperties.support;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import nucleus.Dimension;
import nucleus.DimensionContext;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Dimension implementation for setting a global property to a list of values in
 * a global properties plugin data.
 */
public final class GlobalPropertyDimension implements Dimension {
	private static class Data {
		private GlobalPropertyId globalPropertyId;
		private List<Object> values = new ArrayList<>();
		private double assignmentTime;

		private Data() {
		}

		private Data(Data data) {
			globalPropertyId = data.globalPropertyId;
			values.addAll(data.values);
			assignmentTime = data.assignmentTime;
		}

		@Override
		public int hashCode() {
			return Objects.hash(globalPropertyId, values, assignmentTime);
		}

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
			return Objects.equals(globalPropertyId, other.globalPropertyId)
					&& Objects.equals(values, other.values)
					&& assignmentTime == other.assignmentTime;
		}

	}

	/**
	 * Returns a new builder for GlobalPropertyDimension
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for GlobalPropertyDimension
	 *
	 */
	public static class Builder {

		private Data data = new Data();

		/**
		 * Returns the GlobalPropertyDimension from the collected data.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the global property id was not assigned</li>
		 * 
		 * 
		 */
		public GlobalPropertyDimension build() {
			validate();
			return new GlobalPropertyDimension(new Data(data));
		}

		private void validate() {
			if (data.globalPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
		}

		/**
		 * Sets the global property for the dimension. Defaults to null.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the global property id is null</li>
		 * 
		 */
		public Builder setGlobalPropertyId(GlobalPropertyId globalPropertyId) {
			validateGlobalPropertyId(globalPropertyId);
			data.globalPropertyId = globalPropertyId;
			return this;
		}

		/**
		 * Adds a value to the dimension.
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the value is null</li>
		 * 
		 */
		public Builder addValue(Object value) {
			validateValue(value);
			data.values.add(value);
			return this;
		}

		/**
		 * Sets the assignment time. Defaults to zero.
		 */
		public Builder setAssignmentTime(double assignmentTime) {
			data.assignmentTime = assignmentTime;
			return this;
		}

	}

	private final Data data;

	private GlobalPropertyDimension(Data data) {
		this.data = data;
	}

	@Override
	public List<String> getExperimentMetaData() {
		List<String> result = new ArrayList<>();
		result.add(data.globalPropertyId.toString());
		return result;
	}

	@Override
	public int levelCount() {
		return data.values.size();
	}

	@Override
	public List<String> executeLevel(DimensionContext dimensionContext, int level) {
		GlobalPropertiesPluginData.Builder builder = dimensionContext.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
		Object value = data.values.get(level);
		builder.setGlobalPropertyValue(data.globalPropertyId, value, data.assignmentTime);
		List<String> result = new ArrayList<>();
		result.add(value.toString());
		return result;
	}

	/**
	 * Returns the global property id for this dimension
	 */
	public GlobalPropertyId getGlobalPropertyId() {
		return data.globalPropertyId;
	}

	/**
	 * Returns the ordered list of global property values for this dimension
	 */
	public List<Object> getValues() {
		return new ArrayList<>(data.values);
	}

	/**
	 * Returns the assignment time for the global property value
	 */
	public double getAssignmentTime() {
		return data.assignmentTime;
	}

	private static void validateValue(Object value) {
		if (value == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validateGlobalPropertyId(GlobalPropertyId globalPropertyId) {
		if (globalPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}

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
		GlobalPropertyDimension other = (GlobalPropertyDimension) obj;
		return Objects.equals(data, other.data);
	}

}
