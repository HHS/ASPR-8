package plugins.materials;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import nucleus.PluginData;
import nucleus.PluginDataBuilder;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;
import util.wrappers.MultiKey;

/**
 * An immutable container of the initial state materials producers. It contains:
 * <BR>
 * <ul>
 * <li>material producer ids</li>
 * <li>materials producer property definitions</li>
 * <li>materials producer property values</li>
 * <li>materials producer resource levels</li>
 * <li>stage ids</li>
 * <li>batch ids</li>
 * <li>batch property definitions</li>
 * <li>batch property values</li>
 * <li>batch stage assignments</li>
 * </ul>
 *
 * Construction is conducted via the contained builder class. Builder methods
 * can be invoked in any order and relational validation is delayed until
 * build() is invoked.
 *
 *
 */

@Immutable
public final class MaterialsPluginData implements PluginData {

	/**
	 * Builder class for MaterialsInitialization
	 *
	 *
	 */
	public static class Builder implements PluginDataBuilder {
		private Data data;

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

		private Builder(Data data) {
			this.data = data;
		}

		/**
		 * Adds the batch. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the
		 *             batch id is null</li>
		 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
		 *             material id is null</li>
		 *             <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT}
		 *             if the material amount is infinite</li>
		 *             <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT}
		 *             if the material amount is negative</li>
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the materials producer id is null</li>
		 */
		public Builder addBatch(final BatchId batchId, final MaterialId materialId, final double amount, final MaterialsProducerId materialsProducerId) {
			ensureDataMutability();
			validateBatchIdNotNull(batchId);
			validateMaterialIdNotNull(materialId);
			validateBatchAmount(amount);
			validateMaterialsProducerIdNotNull(materialsProducerId);
			data.batchIds.add(batchId);
			data.batchMaterials.put(batchId, materialId);
			data.batchMaterialsProducers.put(batchId, materialsProducerId);
			data.batchAmounts.put(batchId, amount);
			return this;
		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the
		 *             batch id is null</li>
		 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the
		 *             stage id is null</li>
		 */
		public Builder addBatchToStage(final StageId stageId, final BatchId batchId) {
			ensureDataMutability();
			validateStageIdNotNull(stageId);
			validateBatchIdNotNull(batchId);

			Set<BatchId> batches = data.stageBatches.get(stageId);
			if (batches == null) {
				batches = new LinkedHashSet<>();
				data.stageBatches.put(stageId, batches);
			}
			batches.add(batchId);
			data.batchStages.put(batchId, stageId);

			return this;
		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
		 *             material id is null</li>
		 *
		 */
		public Builder addMaterial(final MaterialId materialId) {
			ensureDataMutability();
			validateMaterialIdNotNull(materialId);
			data.materialIds.add(materialId);
			return this;
		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the material producer id is null</li>
		 * 
		 */
		public Builder addMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
			ensureDataMutability();
			validateMaterialsProducerIdNotNull(materialsProducerId);
			data.materialsProducerIds.add(materialsProducerId);
			return this;
		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the
		 *             stage id is null</li>
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the materials producer id is null</li>
		 */
		public Builder addStage(final StageId stageId, final boolean offered, final MaterialsProducerId materialsProducerId) {
			ensureDataMutability();
			validateStageIdNotNull(stageId);
			validateMaterialsProducerIdNotNull(materialsProducerId);
			data.stageIds.add(stageId);
			data.stageMaterialsProducers.put(stageId, materialsProducerId);
			data.stageOffers.put(stageId, offered);
			return this;
		}

		/**
		 * Builds the MaterialsPluginData from the collected inputs
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if a
		 *             batch property is associated with a material id that was
		 *             not properly added</li>
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a batch is added without assigned property values for
		 *             each property definition that lacks a default value</li>
		 * 
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
		 *             if a materials property value is associated with a
		 *             materials producer id that was not properly added</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             materials property value is associated with a materials
		 *             producer property id that was not properly defined</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             materials property value is associated with a value that
		 *             is not compatible with the corresponding property
		 *             definition</li>
		 * 
		 *             <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
		 *             if a materials property is defined without a default
		 *             value and there is not an assigned property value for
		 *             each added materials producer</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
		 *             if a materials resource level is set for a material
		 *             producer id that was not properly added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if a
		 *             batch is associated with at material that was not
		 *             properly added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
		 *             if a batch is associated with at material producer that
		 *             was not properly added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if a
		 *             batch property is associated with batch id that was not
		 *             properly added</li>
		 * 
		 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if a
		 *             batch property is associated with batch property id that
		 *             was not properly defined</li>
		 * 
		 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if a
		 *             batch property value is incompatible with the
		 *             corresponding property definition</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
		 *             if a stage is associated with a materials producer id
		 *             that was not properly added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if a
		 *             batch is associated with a stage id that was not properly
		 *             added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if a
		 *             stage is associated with a batch id that was not properly
		 *             added</li>
		 * 
		 *             <li>{@linkplain MaterialsError#BATCH_ALREADY_STAGED} if a
		 *             batch is associated with more than one stage</li>
		 * 
		 *             <li>{@linkplain MaterialsError#BATCH_STAGED_TO_DIFFERENT_OWNER}
		 *             if a batch is associated with a stage that is not owned
		 *             by the same materials producer as the batch</li>
		 * 
		 *             <li>{@linkplain MaterialsError#NEXT_BATCH_ID_TOO_SMALL}
		 *             if a batch is greater than or equal to the next batch id
		 *             assigned for the entire plugin data</li>
		 * 
		 *             <li>{@linkplain MaterialsError#NEXT_STAGE_ID_TOO_SMALL}
		 *             if a stage is greater than or equal to the next stage id
		 *             assigned for the entire plugin data</li>
		 * 
		 * 
		 */

		public MaterialsPluginData build() {

			if (!data.locked) {
				validateData();
			}
			ensureImmutability();
			return new MaterialsPluginData(data);

		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             batch property id is null</li>
		 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
		 *             material id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the property definition is null</li>
		 *
		 */
		public Builder defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateBatchPropertyIdNotNull(batchPropertyId);
			validateMaterialIdNotNull(materialId);
			validatePropertyDefinitionNotNull(propertyDefinition);
			Map<BatchPropertyId, PropertyDefinition> propertyDefinitionsMap = data.batchPropertyDefinitions.get(materialId);
			if (propertyDefinitionsMap == null) {
				propertyDefinitionsMap = new LinkedHashMap<>();
				data.batchPropertyDefinitions.put(materialId, propertyDefinitionsMap);
			}
			propertyDefinitionsMap.put(batchPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Adds a batch to stage. Duplicate inputs override previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             materials producer property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION}
		 *             if the property definition is null</li>
		 *
		 */
		public Builder defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition) {
			ensureDataMutability();
			validateMaterialsProducerPropertyIdNotNull(materialsProducerPropertyId);
			validatePropertyDefinitionNotNull(propertyDefinition);
			data.materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
			return this;
		}

		/**
		 * Set the batch property value. Duplicate inputs override previous
		 * inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the
		 *             batch id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             batch property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             batch property value is null</li>
		 *
		 */
		public Builder setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue) {
			ensureDataMutability();
			validateBatchIdNotNull(batchId);
			validateBatchPropertyIdNotNull(batchPropertyId);
			validateBatchPropertyValueNotNull(batchPropertyValue);
			Map<BatchPropertyId, Object> propertyMap = data.batchPropertyValues.get(batchId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.batchPropertyValues.put(batchId, propertyMap);
			}
			propertyMap.put(batchPropertyId, batchPropertyValue);
			return this;
		}

		/**
		 * Set the materials producer property value. Duplicate inputs override
		 * previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the materials producer id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
		 *             materials producer property id is null</li>
		 *             <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE} if the
		 *             materials producer property value is null</li>
		 *
		 */
		public Builder setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
				final Object materialsProducerPropertyValue) {
			ensureDataMutability();
			validateMaterialsProducerIdNotNull(materialsProducerId);
			validateMaterialsProducerPropertyIdNotNull(materialsProducerPropertyId);
			validateMaterialsProducerPropertyValueNotNull(materialsProducerPropertyValue);
			Map<MaterialsProducerPropertyId, Object> propertyMap = data.materialsProducerPropertyValues.get(materialsProducerId);
			if (propertyMap == null) {
				propertyMap = new LinkedHashMap<>();
				data.materialsProducerPropertyValues.put(materialsProducerId, propertyMap);
			}
			propertyMap.put(materialsProducerPropertyId, materialsProducerPropertyValue);
			return this;
		}

		/**
		 * Set the materials producer resource value. Duplicate inputs override
		 * previous inputs.
		 *
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
		 *             if the materials producer id is null</li>
		 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
		 *             resource id is null</li>
		 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
		 *             if the resource amount is negative</li>
		 *
		 */
		public Builder setMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
			ensureDataMutability();
			validateMaterialsProducerIdNotNull(materialsProducerId);
			validateResourceIdNotNull(resourceId);
			validateResourceAmount(amount);
			Map<ResourceId, Long> resourceLevelMap = data.materialsProducerResourceLevels.get(materialsProducerId);
			if (resourceLevelMap == null) {
				resourceLevelMap = new LinkedHashMap<>();
				data.materialsProducerResourceLevels.put(materialsProducerId, resourceLevelMap);
			}
			resourceLevelMap.put(resourceId, amount);
			return this;
		}

		/**
		 * Sets the next available batch id. This value needs to exceed all
		 * extant batch ids. If the nextBatchRecordId is not set explicitly, the
		 * nextBatchRecordId is assigned to either zero or the next integer
		 * value that exceeds the highest valued batch added to this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NEGATIVE_BATCH_ID} if the
		 *             next batch record id is negative</li>
		 * 
		 */
		public Builder setNextBatchRecordId(int nextBatchRecordId) {
			ensureDataMutability();
			validateBatchIdValue(nextBatchRecordId);
			data.nextBatchRecordId = nextBatchRecordId;
			return this;
		}

		/**
		 * Sets the next available stage id. This value needs to exceed all
		 * extant stage ids. If the nextStageRecordId is not set explicitly, the
		 * nextStageRecordId is assigned to either zero or the next integer
		 * value that exceeds the highest valued batch added to this builder.
		 * 
		 * @throws ContractException
		 *             <li>{@linkplain MaterialsError#NEGATIVE_BATCH_ID} if the
		 *             next stage record id is negative</li>
		 * 
		 */
		public Builder setNextStageRecordId(int nextStageRecordId) {
			ensureDataMutability();
			validateStageIdValue(nextStageRecordId);
			data.nextStageRecordId = nextStageRecordId;
			return this;
		}

		private void validateData() {

			for (final MaterialId materialId : data.batchPropertyDefinitions.keySet()) {
				if (!data.materialIds.contains(materialId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId + " in batch property definitions");
				}
			}

			for (final MaterialsProducerId materialsProducerId : data.materialsProducerPropertyValues.keySet()) {
				if (!data.materialsProducerIds.contains(materialsProducerId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId + " in materials producer property values");
				}
				final Map<MaterialsProducerPropertyId, Object> propMap = data.materialsProducerPropertyValues.get(materialsProducerId);
				for (final MaterialsProducerPropertyId materialsProducerPropertyId : propMap.keySet()) {
					final PropertyDefinition propertyDefinition = data.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
					if (propertyDefinition == null) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, materialsProducerId + " in materials producer property values");
					}
					final Object propertyValue = propMap.get(materialsProducerPropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, materialsProducerId + ": " + materialsProducerPropertyId + ": " + propertyValue);
					}
				}
			}

			/*
			 * For every materials producer property definition that has a null
			 * default value, ensure that all corresponding materials producer
			 * property values are not null and repair the definition.
			 */

			for (final MaterialsProducerPropertyId materialsProducerPropertyId : data.materialsProducerPropertyDefinitions.keySet()) {
				final PropertyDefinition propertyDefinition = data.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
				if (!propertyDefinition.getDefaultValue().isPresent()) {
					for (final MaterialsProducerId materialsProducerId : data.materialsProducerIds) {
						Object propertyValue = null;
						final Map<MaterialsProducerPropertyId, Object> propertyValueMap = data.materialsProducerPropertyValues.get(materialsProducerId);
						if (propertyValueMap != null) {
							propertyValue = propertyValueMap.get(materialsProducerPropertyId);

						}
						if (propertyValue == null) {
							throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, materialsProducerPropertyId);
						}
					}
				}
			}

			for (final MaterialsProducerId materialsProducerId : data.materialsProducerResourceLevels.keySet()) {
				if (!data.materialsProducerIds.contains(materialsProducerId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId + " in materials producer resource levels");
				}
			}

			for (final BatchId batchId : data.batchMaterials.keySet()) {
				final MaterialId materialId = data.batchMaterials.get(batchId);
				if (!data.materialIds.contains(materialId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId + " in batch addition of " + batchId);
				}
			}

			for (final BatchId batchId : data.batchMaterialsProducers.keySet()) {
				final MaterialsProducerId materialsProducerId = data.batchMaterialsProducers.get(batchId);
				if (!data.materialsProducerIds.contains(materialsProducerId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId + " in batch addition of " + batchId);
				}
			}

			for (final BatchId batchId : data.batchPropertyValues.keySet()) {
				if (!data.batchIds.contains(batchId)) {
					throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId + " in batch property values");
				}

				final MaterialId materialId = data.batchMaterials.get(batchId);
				final Map<BatchPropertyId, PropertyDefinition> defMap = data.batchPropertyDefinitions.get(materialId);

				final Map<BatchPropertyId, Object> propMap = data.batchPropertyValues.get(batchId);
				for (final BatchPropertyId batchPropertyId : propMap.keySet()) {
					if (defMap == null) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, batchPropertyId + " in batch property values");
					}
					final PropertyDefinition propertyDefinition = defMap.get(batchPropertyId);
					if (propertyDefinition == null) {
						throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, batchPropertyId + " in batch property values");
					}
					final Object propertyValue = propMap.get(batchPropertyId);
					if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
						throw new ContractException(PropertyError.INCOMPATIBLE_VALUE, batchId + ": " + batchPropertyId + ": " + propertyValue);
					}

				}

			}

			/*
			 * establish two maps to aid in checking batch property coverage for
			 * those property definitions that do not have defaults
			 */
			Map<MaterialId, Map<BatchPropertyId, Integer>> nonDefaultBatchPropertiesMap = new LinkedHashMap<>();
			Map<MaterialId, boolean[]> nonDefaultBatchCheckArrayMap = new LinkedHashMap<>();

			for (final MaterialId materialId : data.materialIds) {
				Map<BatchPropertyId, Integer> nonDefaultBatchProperties = new LinkedHashMap<>();
				nonDefaultBatchPropertiesMap.put(materialId, nonDefaultBatchProperties);
				final Map<BatchPropertyId, PropertyDefinition> propertyDefinitionMap = data.batchPropertyDefinitions.get(materialId);
				if (propertyDefinitionMap != null) {
					for (final BatchPropertyId batchPropertyId : propertyDefinitionMap.keySet()) {
						final PropertyDefinition propertyDefinition = propertyDefinitionMap.get(batchPropertyId);
						if (!propertyDefinition.getDefaultValue().isPresent()) {
							nonDefaultBatchProperties.put(batchPropertyId, nonDefaultBatchProperties.size());
						}
					}
				}
				nonDefaultBatchCheckArrayMap.put(materialId, new boolean[nonDefaultBatchProperties.size()]);
			}

			/*
			 * Ensure that each batch has property value assignments for every
			 * relevant property definition that does not have a default value
			 */

			if (data.nextBatchRecordId < 0) {
				for (final BatchId batchId : data.batchIds) {
					data.nextBatchRecordId = FastMath.max(data.nextBatchRecordId, batchId.getValue());
				}
				data.nextBatchRecordId++;
			} else {
				for (final BatchId batchId : data.batchIds) {
					if (batchId.getValue() >= data.nextBatchRecordId) {
						throw new ContractException(MaterialsError.NEXT_BATCH_ID_TOO_SMALL);
					}
				}
			}

			for (final BatchId batchId : data.batchIds) {

				final MaterialId materialId = data.batchMaterials.get(batchId);
				Map<BatchPropertyId, Integer> propertyIndexMap = nonDefaultBatchPropertiesMap.get(materialId);
				boolean[] checkArray = nonDefaultBatchCheckArrayMap.get(materialId);
				// clear the check array
				for (int i = 0; i < checkArray.length; i++) {
					checkArray[i] = false;
				}

				// fill the check array
				final Map<BatchPropertyId, Object> propMap = data.batchPropertyValues.get(batchId);
				if (propMap != null) {
					for (final BatchPropertyId batchPropertyId : propMap.keySet()) {
						Integer checkIndex = propertyIndexMap.get(batchPropertyId);
						if (checkIndex != null) {
							checkArray[checkIndex] = true;
						}
					}
				}
				// show the check array contains no false values
				for (int i = 0; i < checkArray.length; i++) {
					if (!checkArray[i]) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
					}
				}
			}

			if (data.nextStageRecordId < 0) {
				for (final StageId stageId : data.stageIds) {
					data.nextStageRecordId = FastMath.max(data.nextStageRecordId, stageId.getValue());
				}
				data.nextStageRecordId++;
			} else {
				for (final StageId stageId : data.stageIds) {
					if (stageId.getValue() >= data.nextStageRecordId) {
						throw new ContractException(MaterialsError.NEXT_STAGE_ID_TOO_SMALL);
					}
				}
			}

			for (final StageId stageId : data.stageMaterialsProducers.keySet()) {

				final MaterialsProducerId materialsProducerId = data.stageMaterialsProducers.get(stageId);
				if (!data.materialsProducerIds.contains(materialsProducerId)) {
					throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, stageId + " in stage additions");
				}
			}

			for (final StageId stageId : data.stageBatches.keySet()) {
				if (!data.stageIds.contains(stageId)) {
					throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId + " in batch additions to stages");
				}
				final Set<BatchId> batches = data.stageBatches.get(stageId);
				for (final BatchId batchId : batches) {
					if (!data.batchIds.contains(batchId)) {
						throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, stageId + ": " + batchId + " in batch additions to stages");
					}
				}
			}

			for (final BatchId batchId : data.batchStages.keySet()) {
				if (!data.batchIds.contains(batchId)) {
					throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId + " in batch additions to stages");
				}
				final StageId stageId = data.batchStages.get(batchId);
				if (!data.stageIds.contains(stageId)) {
					throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId + " in batch additions to stages");
				}
			}

			for (final StageId stageId : data.stageBatches.keySet()) {
				final Set<BatchId> batches = data.stageBatches.get(stageId);
				for (final BatchId batchId : batches) {
					final StageId linkedStageId = data.batchStages.get(batchId);
					if (!linkedStageId.equals(stageId)) {
						throw new ContractException(MaterialsError.BATCH_ALREADY_STAGED, batchId + " has been assigned to multiple stages");
					}
				}
			}
			for (final BatchId batchId : data.batchStages.keySet()) {
				final StageId stageId = data.batchStages.get(batchId);
				final MaterialsProducerId batchMaterialsProducerId = data.batchMaterialsProducers.get(batchId);
				final MaterialsProducerId stageMaterialsProducerId = data.stageMaterialsProducers.get(stageId);
				if (!batchMaterialsProducerId.equals(stageMaterialsProducerId)) {
					throw new ContractException(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER, stageId + ": " + batchId);
				}
			}

		}

	}

	private static class Data {
		/*
		 * The following members are arranged in dependency order to make
		 * reviewing a bit easier
		 */
		private final Set<MaterialsProducerId> materialsProducerIds;

		private final Set<MaterialId> materialIds;

		private final Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> batchPropertyDefinitions;

		private final Map<MaterialsProducerPropertyId, PropertyDefinition> materialsProducerPropertyDefinitions;

		private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Object>> materialsProducerPropertyValues;

		private final Map<MaterialsProducerPropertyId, Object> emptyMaterialsProducerPropertyValuesMap = Collections.unmodifiableMap(new LinkedHashMap<>());

		private final Map<MaterialsProducerId, Map<ResourceId, Long>> materialsProducerResourceLevels;

		private final Set<BatchId> batchIds;

		private final Map<BatchId, MaterialId> batchMaterials;

		private final Map<BatchId, Double> batchAmounts;

		private final Map<BatchId, MaterialsProducerId> batchMaterialsProducers;

		private final Map<BatchId, Map<BatchPropertyId, Object>> batchPropertyValues;
		private final Map<BatchPropertyId, Object> emptyBatchPropertyValues;

		private final Set<StageId> stageIds;

		private final Map<StageId, Boolean> stageOffers;

		private final Map<StageId, MaterialsProducerId> stageMaterialsProducers;

		private final Map<StageId, Set<BatchId>> stageBatches;

		private final Map<BatchId, StageId> batchStages;

		private int nextBatchRecordId = -1;

		private int nextStageRecordId = -1;

		private boolean locked;

		private boolean compareBatchPropertyValues(Data other) {
			for (BatchId batchId : batchIds) {
				MaterialId materialId = batchMaterials.get(batchId);
				for (BatchPropertyId batchPropertyId : batchPropertyDefinitions.get(materialId).keySet()) {
					Object propertyValue = null;

					Map<BatchPropertyId, Object> map = batchPropertyValues.get(batchId);
					if (map != null) {
						propertyValue = map.get(batchPropertyId);
					}
					if (propertyValue == null) {
						PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
						propertyValue = propertyDefinition.getDefaultValue().get();
					}

					Object otherPropertyValue = null;
					map = other.batchPropertyValues.get(batchId);
					if (map != null) {
						otherPropertyValue = map.get(batchPropertyId);
					}
					if (otherPropertyValue == null) {
						PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
						otherPropertyValue = propertyDefinition.getDefaultValue().get();
					}
					if (!propertyValue.equals(otherPropertyValue)) {
						return false;
					}
				}
			}
			return true;
		}

		private boolean compareProducerPropertyValues(Data other) {

			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
					Object propertyValue = null;
					Map<MaterialsProducerPropertyId, Object> map = materialsProducerPropertyValues.get(materialsProducerId);
					if (map != null) {
						propertyValue = map.get(materialsProducerPropertyId);
					}
					if (propertyValue == null) {
						PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
						propertyValue = propertyDefinition.getDefaultValue().get();
					}
					Object otherPropertyValue = null;
					map = other.materialsProducerPropertyValues.get(materialsProducerId);
					if (map != null) {
						otherPropertyValue = map.get(materialsProducerPropertyId);
					}
					if (otherPropertyValue == null) {
						PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
						otherPropertyValue = propertyDefinition.getDefaultValue().get();
					}
					if (!propertyValue.equals(otherPropertyValue)) {
						return false;
					}
				}
			}
			return true;
		}

		private int getProducerPropertyValuesHashCode() {
			final int prime = 31;
			int result = 1;
			for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
				for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
					Object propertyValue = null;
					Map<MaterialsProducerPropertyId, Object> map = materialsProducerPropertyValues.get(materialsProducerId);
					if (map != null) {
						propertyValue = map.get(materialsProducerPropertyId);
					}
					if (propertyValue == null) {
						PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
						propertyValue = propertyDefinition.getDefaultValue().get();
					}
					int subResult = 1;
					subResult = subResult * prime + materialsProducerId.hashCode();
					subResult = subResult * prime + materialsProducerPropertyId.hashCode();
					subResult = subResult * prime + propertyValue.hashCode();

					result += subResult;
				}
			}
			return result;
		}

		private int getBatchPropertyValuesHashCode() {
			final int prime = 31;
			int result = 0;

			for (BatchId batchId : batchIds) {
				MaterialId materialId = batchMaterials.get(batchId);
				Map<BatchPropertyId, PropertyDefinition> defMap = batchPropertyDefinitions.get(materialId);
				if (defMap != null) {
					for (BatchPropertyId batchPropertyId : defMap.keySet()) {
						Object propertyValue = null;

						Map<BatchPropertyId, Object> valueMap = batchPropertyValues.get(batchId);
						if (valueMap != null) {
							propertyValue = valueMap.get(batchPropertyId);
						}
						if (propertyValue == null) {
							PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
							propertyValue = propertyDefinition.getDefaultValue().get();
						}

						int subResult = 1;
						subResult = subResult * prime + batchId.hashCode();
						subResult = subResult * prime + batchPropertyId.hashCode();
						subResult = subResult * prime + propertyValue.hashCode();

						result += subResult;
					}
				}
			}
			return result;
		}

		public Data() {
			materialsProducerIds = new LinkedHashSet<>();

			materialIds = new LinkedHashSet<>();

			batchPropertyDefinitions = new LinkedHashMap<>();

			materialsProducerPropertyDefinitions = new LinkedHashMap<>();

			materialsProducerPropertyValues = new LinkedHashMap<>();

			materialsProducerResourceLevels = new LinkedHashMap<>();

			batchIds = new LinkedHashSet<>();

			batchMaterials = new LinkedHashMap<>();

			batchAmounts = new LinkedHashMap<>();

			batchMaterialsProducers = new LinkedHashMap<>();

			batchPropertyValues = new LinkedHashMap<>();

			stageIds = new LinkedHashSet<>();

			stageOffers = new LinkedHashMap<>();

			stageMaterialsProducers = new LinkedHashMap<>();

			stageBatches = new LinkedHashMap<>();

			batchStages = new LinkedHashMap<>();

			emptyBatchPropertyValues = Collections.unmodifiableMap(new LinkedHashMap<>());
		}

		public Data(Data data) {

			materialsProducerIds = new LinkedHashSet<>(data.materialsProducerIds);

			materialIds = new LinkedHashSet<>(data.materialIds);

			batchPropertyDefinitions = new LinkedHashMap<>();
			for (MaterialId materialId : data.batchPropertyDefinitions.keySet()) {
				Map<BatchPropertyId, PropertyDefinition> map = data.batchPropertyDefinitions.get(materialId);
				Map<BatchPropertyId, PropertyDefinition> newMap = new LinkedHashMap<>(map);
				batchPropertyDefinitions.put(materialId, newMap);
			}

			materialsProducerPropertyDefinitions = new LinkedHashMap<>(data.materialsProducerPropertyDefinitions);

			materialsProducerPropertyValues = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : data.materialsProducerPropertyValues.keySet()) {
				Map<MaterialsProducerPropertyId, Object> map = data.materialsProducerPropertyValues.get(materialsProducerId);
				Map<MaterialsProducerPropertyId, Object> newMap = new LinkedHashMap<>(map);
				materialsProducerPropertyValues.put(materialsProducerId, newMap);
			}

			materialsProducerResourceLevels = new LinkedHashMap<>();
			for (MaterialsProducerId materialsProducerId : data.materialsProducerResourceLevels.keySet()) {
				Map<ResourceId, Long> map = data.materialsProducerResourceLevels.get(materialsProducerId);
				Map<ResourceId, Long> newMap = new LinkedHashMap<>(map);
				materialsProducerResourceLevels.put(materialsProducerId, newMap);
			}

			batchIds = new LinkedHashSet<>(data.batchIds);

			batchMaterials = new LinkedHashMap<>(data.batchMaterials);

			batchAmounts = new LinkedHashMap<>(data.batchAmounts);

			batchMaterialsProducers = new LinkedHashMap<>(data.batchMaterialsProducers);

			batchPropertyValues = new LinkedHashMap<>();
			for (BatchId batchId : data.batchPropertyValues.keySet()) {
				Map<BatchPropertyId, Object> map = data.batchPropertyValues.get(batchId);
				Map<BatchPropertyId, Object> newMap = new LinkedHashMap<>(map);
				batchPropertyValues.put(batchId, newMap);
			}

			stageIds = new LinkedHashSet<>(data.stageIds);

			stageOffers = new LinkedHashMap<>(data.stageOffers);

			stageMaterialsProducers = new LinkedHashMap<>(data.stageMaterialsProducers);

			stageBatches = new LinkedHashMap<>();
			for (StageId stageId : data.stageBatches.keySet()) {
				Set<BatchId> set = data.stageBatches.get(stageId);
				Set<BatchId> newSet = new LinkedHashSet<>(set);
				stageBatches.put(stageId, newSet);
			}

			batchStages = new LinkedHashMap<>(data.batchStages);

			emptyBatchPropertyValues = Collections.unmodifiableMap(new LinkedHashMap<>());

			locked = data.locked;
		}

		/*
		 * This is not a boiler plate hash code contract. See notes below
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;

			result = prime * result + batchAmounts.hashCode();
			result = prime * result + batchIds.hashCode();
			result = prime * result + batchMaterials.hashCode();
			result = prime * result + batchMaterialsProducers.hashCode();
			result = prime * result + batchPropertyDefinitions.hashCode();
			result = prime * result + batchStages.hashCode();
			result = prime * result + materialIds.hashCode();
			result = prime * result + materialsProducerIds.hashCode();
			result = prime * result + materialsProducerPropertyDefinitions.hashCode();
			result = prime * result + stageBatches.hashCode();
			result = prime * result + stageIds.hashCode();
			result = prime * result + stageMaterialsProducers.hashCode();
			result = prime * result + stageOffers.hashCode();

			result = prime * result + getBatchPropertyValuesHashCode();
			result = prime * result + getProducerPropertyValuesHashCode();
			result = prime * result + getProducerResourceLevelsHashCode();

			return result;
		}

		private boolean compareProducerResourceLevels(Data other) {

			for (MaterialsProducerId materialsProducerId : materialsProducerResourceLevels.keySet()) {
				Set<MultiKey> nonZeroValues = new LinkedHashSet<>();
				Map<ResourceId, Long> map = materialsProducerResourceLevels.get(materialsProducerId);
				if (map != null) {
					for (ResourceId resourceId : map.keySet()) {
						Long level = map.get(resourceId);
						if (level > 0) {
							nonZeroValues.add(new MultiKey(resourceId, level));
						}
					}
				}

				Set<MultiKey> otherNonZeroValues = new LinkedHashSet<>();
				map = other.materialsProducerResourceLevels.get(materialsProducerId);
				if (map != null) {
					for (ResourceId resourceId : map.keySet()) {
						Long level = map.get(resourceId);
						if (level > 0) {
							otherNonZeroValues.add(new MultiKey(resourceId, level));
						}
					}
				}
				if (!nonZeroValues.equals(otherNonZeroValues)) {
					return false;
				}
			}
			return true;
		}

		private int getProducerResourceLevelsHashCode() {
			final int prime = 31;
			int result = 1;
			for (MaterialsProducerId materialsProducerId : materialsProducerResourceLevels.keySet()) {
				Map<ResourceId, Long> map = materialsProducerResourceLevels.get(materialsProducerId);
				if (map != null) {
					for (ResourceId resourceId : map.keySet()) {
						Long level = map.get(resourceId);
						if (level > 0) {
							int subResult = 1;
							subResult = subResult * prime + materialsProducerId.hashCode();
							subResult = subResult * prime + resourceId.hashCode();
							subResult = subResult * prime + level.hashCode();
							result += subResult;
						}
					}
				}
			}
			return result;
		}

		/*
		 * This is not a boiler plate equals contract. See notes below
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (!(obj instanceof Data)) {
				return false;
			}
			Data other = (Data) obj;

			/*
			 * We exclude the following fields:
			 * 
			 * emptyBatchPropertyValues -- just an empty list
			 * 
			 * emptyMaterialsProducerPropertyValuesMap -- just an empty map
			 * 
			 * locked -- should be locked when equals is invoked
			 */

			// most of the fields use normal comparison
			if (!batchAmounts.equals(other.batchAmounts)) {
				return false;
			}

			if (!batchIds.equals(other.batchIds)) {
				return false;
			}

			if (!batchMaterials.equals(other.batchMaterials)) {
				return false;
			}

			if (!batchMaterialsProducers.equals(other.batchMaterialsProducers)) {
				return false;
			}

			if (!batchPropertyDefinitions.equals(other.batchPropertyDefinitions)) {
				return false;
			}

			if (!batchStages.equals(other.batchStages)) {
				return false;
			}

			if (!materialIds.equals(other.materialIds)) {
				return false;
			}

			if (!materialsProducerIds.equals(other.materialsProducerIds)) {
				return false;
			}

			if (!materialsProducerPropertyDefinitions.equals(other.materialsProducerPropertyDefinitions)) {
				return false;
			}

			if (!stageBatches.equals(other.stageBatches)) {
				return false;
			}

			if (!stageIds.equals(other.stageIds)) {
				return false;
			}

			if (!stageMaterialsProducers.equals(other.stageMaterialsProducers)) {
				return false;
			}

			if (!stageOffers.equals(other.stageOffers)) {
				return false;
			}

			/*
			 * The remaining fields require special handling so that default
			 * property/resource values and times are ignored. Note that we will
			 * rely on the previous equalities being true.
			 */

			if (!compareBatchPropertyValues(other)) {
				return false;
			}
			if (!compareProducerPropertyValues(other)) {
				return false;
			}

			if (!compareProducerResourceLevels(other)) {
				return false;
			}
			return true;
		}

	}

	/**
	 * Returns a new builder instance
	 */
	public static Builder builder() {
		return new Builder(new Data());
	}

	private static void validateBatchAmount(final double amount) {
		if (!Double.isFinite(amount)) {
			throw new ContractException(MaterialsError.NON_FINITE_MATERIAL_AMOUNT, amount);
		}
		if (amount < 0) {
			throw new ContractException(MaterialsError.NEGATIVE_MATERIAL_AMOUNT, amount);
		}

	}

	private static void validateBatchExists(final Data data, final Object batchId) {

		if (!data.batchIds.contains(batchId)) {
			throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId);
		}
	}

	private static void validateBatchIdNotNull(final BatchId batchId) {
		if (batchId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_ID);
		}
	}

	private static void validateBatchPropertyIdNotNull(final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateBatchPropertyIsDefined(final Data data, final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		validateBatchPropertyIdNotNull(batchPropertyId);
		final Map<BatchPropertyId, PropertyDefinition> map = data.batchPropertyDefinitions.get(materialId);
		if (map == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, batchPropertyId);
		}
		final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, batchPropertyId);
		}
	}

	private static void validateBatchPropertyValueNotNull(final Object batchPropertyValue) {
		if (batchPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validateMaterialExists(final Data data, final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
		if (!data.materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private static void validateMaterialIdNotNull(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
	}

	private static void validateMaterialsProducerExists(final Data data, final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		if (!data.materialsProducerIds.contains(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private static void validateMaterialsProducerIdNotNull(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
	}

	private static void validateMaterialsProducerPropertyIdNotNull(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

	private static void validateMaterialsProducerPropertyValueNotNull(final Object materialsProducerPropertyValue) {
		if (materialsProducerPropertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private static void validatePropertyDefinitionNotNull(final PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private static void validateResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	private static void validateResourceIdNotNull(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
	}

	private static void validateStageExists(final Data data, final StageId stageId) {
		validateStageIdNotNull(stageId);
		if (!data.stageIds.contains(stageId)) {
			throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	private static void validateBatchIdValue(int batchIdValue) {
		if (batchIdValue < 0) {
			throw new ContractException(MaterialsError.NEGATIVE_BATCH_ID, batchIdValue);
		}
	}

	private static void validateStageIdValue(int stageIdValue) {
		if (stageIdValue < 0) {
			throw new ContractException(MaterialsError.NEGATIVE_STAGE_ID, stageIdValue);
		}
	}

	private static void validateStageIdNotNull(final StageId stageId) {
		if (stageId == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_ID);
		}
	}

	private final Data data;

	private MaterialsPluginData(final Data data) {
		this.data = data;
	}

	/**
	 * Returns the material amount for the given batch.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 */
	public Double getBatchAmount(final BatchId batchId) {
		validateBatchIdNotNull(batchId);
		validateBatchExists(data, batchId);
		return data.batchAmounts.get(batchId);
	}

	/**
	 * Returns the collected batch ids.
	 *
	 */
	public Set<BatchId> getBatchIds() {
		return new LinkedHashSet<>(data.batchIds);
	}

	/**
	 * Returns the material type for the given batch id.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBatchMaterial(final BatchId batchId) {
		validateBatchIdNotNull(batchId);
		validateBatchExists(data, batchId);
		return (T) data.batchMaterials.get(batchId);
	}

	/**
	 * Returns the materials producer id for the given batch id.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> T getBatchMaterialsProducer(final BatchId batchId) {
		validateBatchIdNotNull(batchId);
		validateBatchExists(data, batchId);
		return (T) data.batchMaterialsProducers.get(batchId);
	}

	/**
	 * Returns the property definition for the given batch property id and
	 * material id
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the batch
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             batch property id is unknown</li>
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		validateMaterialExists(data, materialId);
		validateBatchPropertyIsDefined(data, materialId, batchPropertyId);

		final Map<BatchPropertyId, PropertyDefinition> map = data.batchPropertyDefinitions.get(materialId);
		final PropertyDefinition propertyDefinition = map.get(batchPropertyId);
		return propertyDefinition;
	}

	/**
	 * Returns the property ids associated with the given material id
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		validateMaterialExists(data, materialId);
		final Set<T> result = new LinkedHashSet<>();
		final Map<BatchPropertyId, PropertyDefinition> map = data.batchPropertyDefinitions.get(materialId);
		if (map != null) {
			final Set<BatchPropertyId> batchPropertyIds = map.keySet();
			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				result.add((T) batchPropertyId);
			}
		}
		return result;
	}

	/**
	 * Returns a map of the property values associated collected for the given
	 * batch
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 */
	public Map<BatchPropertyId, Object> getBatchPropertyValues(final BatchId batchId) {
		validateBatchIdNotNull(batchId);
		validateBatchExists(data, batchId);
		final Map<BatchPropertyId, Object> map = data.batchPropertyValues.get(batchId);
		if (map != null) {
			return Collections.unmodifiableMap(map);
		}
		return data.emptyBatchPropertyValues;
	}

	/**
	 * Returns the collected material ids
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialId> Set<T> getMaterialIds() {
		final Set<T> result = new LinkedHashSet<>(data.materialIds.size());
		for (final MaterialId materialId : data.materialIds) {
			result.add((T) materialId);
		}
		return result;
	}

	/**
	 * Returns the collected material producer ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		final Set<T> result = new LinkedHashSet<>(data.materialsProducerIds.size());
		for (final MaterialsProducerId materialsProducerId : data.materialsProducerIds) {
			result.add((T) materialsProducerId);
		}
		return result;
	}

	/**
	 * Returns the property definition associated with the given materials
	 * producer property id
	 *
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             materials producer property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             materials producer property id is unknown</li>
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerPropertyIdNotNull(materialsProducerPropertyId);

		final PropertyDefinition propertyDefinition = data.materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, materialsProducerPropertyId);
		}
		return propertyDefinition;
	}

	/**
	 * Returns the materials producer property ids
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		final Set<T> result = new LinkedHashSet<>();
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : data.materialsProducerPropertyDefinitions.keySet()) {
			result.add((T) materialsProducerPropertyId);
		}
		return result;
	}

	/**
	 * Returns a map of property id to value collected for the given materials
	 * producer
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 */
	public Map<MaterialsProducerPropertyId, Object> getMaterialsProducerPropertyValues(final MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerExists(data, materialsProducerId);
		final Map<MaterialsProducerPropertyId, Object> map = data.materialsProducerPropertyValues.get(materialsProducerId);
		if (map == null) {
			return data.emptyMaterialsProducerPropertyValuesMap;
		}
		return Collections.unmodifiableMap(map);
	}

	/**
	 * Returns the resource level the given materials producer id and resource.
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public Long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		validateMaterialsProducerExists(data, materialsProducerId);
		validateResourceIdNotNull(resourceId);
		Long result = null;
		final Map<ResourceId, Long> map = data.materialsProducerResourceLevels.get(materialsProducerId);
		if (map != null) {
			result = map.get(resourceId);
		}
		if (result == null) {
			result = 0L;
		}
		return result;
	}

	/**
	 * Returns the collected resource ids
	 */
	public Set<ResourceId> getResourceIds() {
		final Set<ResourceId> result = new LinkedHashSet<>();
		for (final MaterialsProducerId materialsProducerId : data.materialsProducerResourceLevels.keySet()) {
			final Map<ResourceId, Long> map = data.materialsProducerResourceLevels.get(materialsProducerId);
			result.addAll(map.keySet());
		}
		return result;
	}

	/**
	 * Returns the batch ids that are assigned to the given stage id.
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 */
	public Set<BatchId> getStageBatches(final StageId stageId) {
		validateStageExists(data, stageId);
		final Set<BatchId> result = new LinkedHashSet<>();
		final Set<BatchId> set = data.stageBatches.get(stageId);
		if (set != null) {
			result.addAll(set);
		}
		return result;
	}

	/**
	 * Returns the collected stage ids
	 */
	public Set<StageId> getStageIds() {
		return new LinkedHashSet<>(data.stageIds);
	}

	/**
	 * Returns the materials producer id associated with the given stage id.
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getStageMaterialsProducer(final StageId stageId) {
		validateStageExists(data, stageId);
		return (T) data.stageMaterialsProducers.get(stageId);
	}

	/**
	 * Returns the offer state of the given stage id.
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 */
	public Boolean isStageOffered(final StageId stageId) {
		validateStageExists(data, stageId);
		return data.stageOffers.get(stageId);
	}

	@Override
	public PluginDataBuilder getCloneBuilder() {
		return new Builder(data);
	}

	/**
	 * Returns the next available batch id.
	 */
	public int getNextBatchRecordId() {
		return data.nextBatchRecordId;
	}

	/**
	 * Returns the next available stage id.
	 */
	public int getNextStageRecordId() {
		return data.nextStageRecordId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (!(o instanceof MaterialsPluginData))
			return false;
		MaterialsPluginData that = (MaterialsPluginData) o;
		return data.equals(that.data);
	}

	@Override
	public int hashCode() {
		return Objects.hash(data);
	}
}
