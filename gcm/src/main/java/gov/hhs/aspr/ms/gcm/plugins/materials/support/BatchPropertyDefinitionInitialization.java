package gov.hhs.aspr.ms.gcm.plugins.materials.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.util.Pair;

import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * A class for defining a batch property with an associated property id and
 * property values for extant batches.
 */
@Immutable
public final class BatchPropertyDefinitionInitialization {

	private static class Data {
		MaterialId materialId;
		BatchPropertyId batchPropertyId;
		PropertyDefinition propertyDefinition;
		List<Pair<BatchId, Object>> propertyValues = new ArrayList<>();

		public Data() {
		}

		public Data(Data data) {
			materialId = data.materialId;
			batchPropertyId = data.batchPropertyId;
			propertyDefinition = data.propertyDefinition;
			propertyValues.addAll(data.propertyValues);

		}
	}

	private final Data data;

	private BatchPropertyDefinitionInitialization(Data data) {
		this.data = data;
	}

	/**
	 * Returns a new builder
	 */
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder class for a BatchPropertyDefinitionInitialization
	 */
	public final static class Builder {
		private Builder() {
		}

		private Data data = new Data();

		private void validate() {
			if (data.propertyDefinition == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
			}

			if (data.batchPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}

			if (data.materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}

			Class<?> type = data.propertyDefinition.getType();
			for (Pair<BatchId, Object> pair : data.propertyValues) {
				Object value = pair.getSecond();
				if (!type.isAssignableFrom(value.getClass())) {
					String message = "Definition Type " + type.getName() + " is not compatible with value = " + value;
					throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, message);
				}
			}
		}

		/**
		 * Constructs the BatchPropertyDefinitionInitialization from the collected data
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *                           if no property definition was assigned to the
		 *                           builder</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
		 *                           no property id was assigned to the builder</li>
		 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
		 *                           if a collected property value is incompatible with
		 *                           the property definition</li>
		 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
		 *                           no material id was assigned to the builder</li>
		 *                           </ul>
		 */
		public BatchPropertyDefinitionInitialization build() {
			validate();
			return new BatchPropertyDefinitionInitialization(new Data(data));
		}

		/**
		 * Sets the batch property id
		 * 
		 * @throws ContractException {@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *                           property id is null
		 */
		public Builder setPropertyId(BatchPropertyId batchPropertyId) {
			if (batchPropertyId == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_ID);
			}
			data.batchPropertyId = batchPropertyId;
			return this;
		}

		/**
		 * Sets the material id
		 * 
		 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIAL_ID} if the
		 *                           material id is null
		 */
		public Builder setMaterialId(MaterialId materialId) {
			if (materialId == null) {
				throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
			}
			data.materialId = materialId;
			return this;
		}

		/**
		 * Sets the property definition
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
		 * Adds a property value
		 * 
		 * @throws ContractException
		 *                           <ul>
		 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
		 *                           the batch id is null</li>
		 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
		 *                           if the property value is null</li>
		 *                           </ul>
		 */
		public Builder addPropertyValue(BatchId batchId, Object value) {
			if (batchId == null) {
				throw new ContractException(MaterialsError.NULL_BATCH_ID);
			}

			if (value == null) {
				throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
			}

			data.propertyValues.add(new Pair<>(batchId, value));
			return this;
		}

	}

	/**
	 * Returns the (non-null)property id.
	 */
	public BatchPropertyId getPropertyId() {
		return data.batchPropertyId;
	}

	/**
	 * Returns the (non-null) material id.
	 */
	public MaterialId getMaterialId() {
		return data.materialId;
	}

	/**
	 * Returns the (non-null)property definition.
	 */
	public PropertyDefinition getPropertyDefinition() {
		return data.propertyDefinition;
	}

	/**
	 * Returns the list of (batchId,value) pairs collected by the builder in the
	 * order of their addition. All pairs have non-null entries and the values are
	 * compatible with the contained property definition. Duplicate assignments of
	 * values to the same batch may be present.
	 */
	public List<Pair<BatchId, Object>> getPropertyValues() {
		return Collections.unmodifiableList(data.propertyValues);
	}

}
