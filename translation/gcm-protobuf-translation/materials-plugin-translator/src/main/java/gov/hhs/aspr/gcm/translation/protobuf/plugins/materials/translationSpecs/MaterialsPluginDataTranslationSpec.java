package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchPropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerResourceLevelMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceInitializationInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.MaterialsPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyDefinition;

public class MaterialsPluginDataTranslationSpec
		extends ProtobufTranslationSpec<MaterialsPluginDataInput, MaterialsPluginData> {

	@Override
	protected MaterialsPluginData convertInputObject(MaterialsPluginDataInput inputObject) {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		builder.setNextBatchRecordId(inputObject.getNextBatchRecordId());
		builder.setNextStageRecordId(inputObject.getNextStageRecordId());

		for (MaterialIdInput materialIdInput : inputObject.getMaterialIdsList()) {
			MaterialId materialId = this.translationEngine.convertObject(materialIdInput);
			builder.addMaterial(materialId);
		}

		for (MaterialsProducerIdInput materialsProducerIdInput : inputObject.getMaterialsProducerIdsList()) {
			MaterialsProducerId materialsProducerId = this.translationEngine.convertObject(materialsProducerIdInput);
			builder.addMaterialsProducerId(materialsProducerId);
		}

		for (BatchPropertyDefinitionMapInput batchPropertyDefinitionMapInput : inputObject
				.getBatchPropertyDefinitionsList()) {
			MaterialId materialId = this.translationEngine
					.convertObject(batchPropertyDefinitionMapInput.getMaterialId());
			for (PropertyDefinitionMapInput propertyDefinitionMapInput : batchPropertyDefinitionMapInput
					.getPropertyDefinitionsList()) {
				BatchPropertyId batchPropertyId = this.translationEngine
						.getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
				PropertyDefinition propertyDefinition = this.translationEngine
						.convertObject(propertyDefinitionMapInput.getPropertyDefinition());

				builder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			}
		}

		for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject
				.getMaterialsProducerPropertyDefinitionsList()) {
			MaterialsProducerPropertyId materialsProducerPropertyId = this.translationEngine
					.getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
			PropertyDefinition propertyDefinition = this.translationEngine
					.convertObject(propertyDefinitionMapInput.getPropertyDefinition());
			builder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		}

		for (MaterialsProducerPropertyValueMapInput materialsProducerPropertyValueMapInput : inputObject
				.getMaterialsProducerPropertyValuesList()) {
			MaterialsProducerId materialsProducerId = this.translationEngine
					.convertObject(materialsProducerPropertyValueMapInput.getMaterialsProducerId());
			for (PropertyValueMapInput propertyValueMapInput : materialsProducerPropertyValueMapInput
					.getPropertyValuesList()) {
				MaterialsProducerPropertyId materialsProducerPropertyId = this.translationEngine
						.getObjectFromAny(propertyValueMapInput.getPropertyId());
				Object value = this.translationEngine.getObjectFromAny(propertyValueMapInput.getPropertyValue());

				builder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, value);
			}
		}

		for (MaterialsProducerResourceLevelMapInput materialsProducerResourceLevelMapInput : inputObject
				.getMaterialsProducerResourceLevelsList()) {
			MaterialsProducerId materialsProducerId = this.translationEngine
					.convertObject(materialsProducerResourceLevelMapInput.getMaterialsProducerId());
			for (ResourceInitializationInput resourceInitializationInput : materialsProducerResourceLevelMapInput
					.getResourceLevelsList()) {
				ResourceId resourceId = this.translationEngine
						.convertObject(resourceInitializationInput.getResourceId());
				long amount = resourceInitializationInput.getAmount();

				builder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
			}
		}

		for (BatchMapInput batchMapInput : inputObject.getBatchIdsList()) {
			BatchId batchId = this.translationEngine.convertObject(batchMapInput.getBatchId());
			MaterialId materialId = this.translationEngine.convertObject(batchMapInput.getMaterialId());
			double amount = batchMapInput.getAmount();
			MaterialsProducerId materialsProducerId = this.translationEngine
					.convertObject(batchMapInput.getMaterialsProducerId());

			builder.addBatch(batchId, materialId, amount, materialsProducerId);

			for (PropertyValueMapInput propertyValueMapInput : batchMapInput.getPropertyValuesList()) {
				BatchPropertyId batchPropertyId = this.translationEngine
						.getObjectFromAny(propertyValueMapInput.getPropertyId());
				Object propertyValue = this.translationEngine
						.getObjectFromAny(propertyValueMapInput.getPropertyValue());

				builder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
			}
		}

		for (StageMapInput stageMapInput : inputObject.getStageIdsList()) {
			StageId stageId = this.translationEngine.convertObject(stageMapInput.getStageId());
			boolean offered = stageMapInput.getOffered();
			MaterialsProducerId materialsProducerId = this.translationEngine
					.convertObject(stageMapInput.getMaterialsProducerId());

			builder.addStage(stageId, offered, materialsProducerId);

			for (BatchIdInput batchIdInput : stageMapInput.getBatchesInStageList()) {
				BatchId batchId = this.translationEngine.convertObject(batchIdInput);

				builder.addBatchToStage(stageId, batchId);
			}
		}

		return builder.build();
	}

	@Override
	protected MaterialsPluginDataInput convertAppObject(MaterialsPluginData appObject) {
		MaterialsPluginDataInput.Builder builder = MaterialsPluginDataInput.newBuilder();

		builder.setNextBatchRecordId(appObject.getNextBatchRecordId());
		builder.setNextStageRecordId(appObject.getNextStageRecordId());

		for (MaterialId materialId : appObject.getMaterialIds()) {
			MaterialIdInput materialIdInput = this.translationEngine.convertObjectAsSafeClass(materialId,
					MaterialId.class);
			// add materialIds
			builder.addMaterialIds(materialIdInput);

			BatchPropertyDefinitionMapInput.Builder batchPropertyDefinitionMapBuilder = BatchPropertyDefinitionMapInput
					.newBuilder().setMaterialId(materialIdInput);
			for (BatchPropertyId batchPropertyId : appObject.getBatchPropertyIds(materialId)) {
				PropertyDefinition propertyDefinition = appObject.getBatchPropertyDefinition(materialId,
						batchPropertyId);
				PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
						.convertObject(propertyDefinition);

				PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput.newBuilder()
						.setPropertyDefinition(propertyDefinitionInput)
						.setPropertyId(this.translationEngine.getAnyFromObject(batchPropertyId)).build();

				batchPropertyDefinitionMapBuilder.addPropertyDefinitions(propertyDefinitionMapInput);
			}

			// add batchPropertyDefinitions
			builder.addBatchPropertyDefinitions(batchPropertyDefinitionMapBuilder.build());
		}

		for (MaterialsProducerId materialsProducerId : appObject.getMaterialsProducerIds()) {
			MaterialsProducerIdInput materialsProducerIdInput = this.translationEngine
					.convertObjectAsSafeClass(materialsProducerId, MaterialsProducerId.class);

			// add materialProducerId
			builder.addMaterialsProducerIds(materialsProducerIdInput);

			MaterialsProducerPropertyValueMapInput.Builder materialsProducerPropertyValueMapInput = MaterialsProducerPropertyValueMapInput
					.newBuilder()
					.setMaterialsProducerId(materialsProducerIdInput);

			for (MaterialsProducerPropertyId materialsProducerPropertyId : appObject
					.getMaterialsProducerPropertyValues(materialsProducerId).keySet()) {
				Object value = appObject.getMaterialsProducerPropertyValues(materialsProducerId)
						.get(materialsProducerPropertyId);

				PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
						.setPropertyValue(this.translationEngine.getAnyFromObject(value))
						.setPropertyId(this.translationEngine.getAnyFromObject(materialsProducerPropertyId)).build();

				materialsProducerPropertyValueMapInput.addPropertyValues(propertyValueMapInput);
			}

			// add materialsproducerpropertyvalues
			builder.addMaterialsProducerPropertyValues(materialsProducerPropertyValueMapInput.build());

			MaterialsProducerResourceLevelMapInput.Builder resourceLevelMapBuilder = MaterialsProducerResourceLevelMapInput
					.newBuilder().setMaterialsProducerId(materialsProducerIdInput);
			for (ResourceId resourceId : appObject.getResourceIds()) {
				long amount = appObject.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);

				ResourceInitializationInput resourceInitializationInput = ResourceInitializationInput.newBuilder()
						.setAmount(amount).setResourceId(this.translationEngine.getAnyFromObject(resourceId)).build();

				resourceLevelMapBuilder.addResourceLevels(resourceInitializationInput);
			}

			// add materialsproducerresourcelevels
			builder.addMaterialsProducerResourceLevels(resourceLevelMapBuilder.build());
		}

		for (MaterialsProducerPropertyId materialsProducerPropertyId : appObject.getMaterialsProducerPropertyIds()) {
			PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
					.convertObject(appObject.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId));

			PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput.newBuilder()
					.setPropertyDefinition(propertyDefinitionInput)
					.setPropertyId(this.translationEngine.getAnyFromObject(materialsProducerPropertyId)).build();

			// add materialsProducerPropertyDefinitions
			builder.addMaterialsProducerPropertyDefinitions(propertyDefinitionMapInput);
		}

		// batches
		for (BatchId batchId : appObject.getBatchIds()) {
			BatchMapInput.Builder batchMapBuilder = BatchMapInput.newBuilder();

			BatchIdInput batchIdInput = this.translationEngine.convertObject(batchId);
			double amount = appObject.getBatchAmount(batchId);
			MaterialIdInput materialIdInput = this.translationEngine
					.convertObjectAsSafeClass(appObject.getBatchMaterial(batchId), MaterialId.class);
			MaterialsProducerIdInput materialsProducerIdInput = this.translationEngine
					.convertObjectAsSafeClass(appObject.getBatchMaterialsProducer(batchId), MaterialsProducerId.class);

			batchMapBuilder.setAmount(amount).setBatchId(batchIdInput).setMaterialId(materialIdInput)
					.setMaterialsProducerId(materialsProducerIdInput);

			for (BatchPropertyId propertyId : appObject.getBatchPropertyValues(batchId).keySet()) {
				PropertyValueMapInput.Builder batchPropertyValueMap = PropertyValueMapInput.newBuilder();
				Object value = appObject.getBatchPropertyValues(batchId).get(propertyId);

				batchPropertyValueMap.setPropertyValue(this.translationEngine.getAnyFromObject(value))
						.setPropertyId(this.translationEngine.getAnyFromObject(propertyId));

				batchMapBuilder.addPropertyValues(batchPropertyValueMap.build());
			}

			builder.addBatchIds(batchMapBuilder.build());
		}

		// stages
		for (StageId stageId : appObject.getStageIds()) {

			StageMapInput.Builder stageMapBuilder = StageMapInput.newBuilder();

			StageIdInput stageIdInput = this.translationEngine.convertObject(stageId);
			boolean offered = appObject.isStageOffered(stageId);
			MaterialsProducerIdInput materialsProducerIdInput = this.translationEngine
					.convertObjectAsSafeClass(appObject.getStageMaterialsProducer(stageId), MaterialsProducerId.class);

			stageMapBuilder.setOffered(offered).setStageId(stageIdInput)
					.setMaterialsProducerId(materialsProducerIdInput);

			for (BatchId batchId : appObject.getStageBatches(stageId)) {
				BatchIdInput batchIdInput = this.translationEngine.convertObject(batchId);

				stageMapBuilder.addBatchesInStage(batchIdInput);
			}

			builder.addStageIds(stageMapBuilder.build());
		}
		return builder.build();
	}

	@Override
	public Class<MaterialsPluginData> getAppObjectClass() {
		return MaterialsPluginData.class;
	}

	@Override
	public Class<MaterialsPluginDataInput> getInputObjectClass() {
		return MaterialsPluginDataInput.class;
	}

}
