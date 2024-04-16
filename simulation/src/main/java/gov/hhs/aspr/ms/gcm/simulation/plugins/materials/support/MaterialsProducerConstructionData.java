package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class MaterialsProducerConstructionData {

	private static class Data {
		private MaterialsProducerId materialsProducerId;
		private List<Object> values = new ArrayList<>();
		private Map<MaterialsProducerPropertyId, Object> propertyValues = new LinkedHashMap<>();
		private Map<ResourceId, Long> resourceLevels = new LinkedHashMap<>();

		public Data() {
		}

		public Data(Data data) {
			materialsProducerId = data.materialsProducerId;
			values.addAll(data.values);
			propertyValues.putAll(data.propertyValues);
			resourceLevels.putAll(data.resourceLevels);
		}
	}

	/**
	 * Returns a new Builder instance
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Static builder class for {@link MaterialsProducerConstructionData}
	 */
	public static class Builder {
		private Data data = new Data();

		private Builder() {
		}

		private void validate() {
			if (data.materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
		}

		/**
		 * Builds the MaterialsProducerConstructionData from the given inputs.
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *                           if the materials producer id was not set
		 */
		public MaterialsProducerConstructionData build() {
			validate();
			return new MaterialsProducerConstructionData(new Data(data));
		}

		/**
		 * Sets the materials producer id
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *                           if the materials producer id is null
		 */
		public Builder setMaterialsProducerId(MaterialsProducerId materialsProducerId) {
			if (materialsProducerId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
			}
			data.materialsProducerId = materialsProducerId;
			return this;
		}

		/**
		 * Adds an auxiliary value to be used by observers of materials producer
		 * addition
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_AUXILIARY_DATA} if
		 *                           the value is null
		 */
		public Builder addValue(Object value) {
			if (value == null) {
				throw new ContractException(MaterialsError.NULL_AUXILIARY_DATA);
			}
			data.values.add(value);
			return this;
		}

		/**
		 * Sets a materials producer property value
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           the materials producer property id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the value is null</li>
		 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_VALUE_ASSIGNMENT}
		 *                           if the materials producer property was previously
		 *                           set</li>
		 *                           </ul>
		 */
		public Builder setMaterialsProducerPropertyValue(MaterialsProducerPropertyId materialsProducerPropertyId,
				Object value) {
			if (materialsProducerPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}
			if (data.propertyValues.containsKey(materialsProducerPropertyId)) {
				throw new ContractException(PropertyError.DUPLICATE_PROPERTY_VALUE_ASSIGNMENT);
			}
			data.propertyValues.put(materialsProducerPropertyId, value);
			return this;
		}

		/**
		 * Sets a materials producer resource level
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
		 *                           the resource id is null</li>
		 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *                           if the level is negative</li>
		 *                           <li>{@linkplain ResourceError#DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT}
		 *                           if the resource level was previously set</li>
		 *                           </ul>
		 */
		public Builder setResourceLevel(ResourceId resourceId, long level) {

			if (resourceId == null) {
				throw new ContractException(ResourceError.NULL_RESOURCE_ID);
			}
			if (level < 0) {
				throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
			}
			if (data.resourceLevels.containsKey(resourceId)) {
				throw new ContractException(ResourceError.DUPLICATE_REGION_RESOURCE_LEVEL_ASSIGNMENT);
			}
			data.resourceLevels.put(resourceId, level);
			return this;
		}

	}

	private final Data data;

	private MaterialsProducerConstructionData(Data data) {
		this.data = data;
	}

	/**
	 * Returns a non-null materials producer id
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return data.materialsProducerId;
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
	 * Returns an unmodifiable map of materials producer property ids to values
	 */
	public Map<MaterialsProducerPropertyId, Object> getMaterialsProducerPropertyValues() {
		return Collections.unmodifiableMap(data.propertyValues);
	}

	/**
	 * Returns an unmodifiable map of materials producer resource levels
	 */
	public Map<ResourceId, Long> getResourceLevels() {
		return Collections.unmodifiableMap(data.resourceLevels);
	}
}
