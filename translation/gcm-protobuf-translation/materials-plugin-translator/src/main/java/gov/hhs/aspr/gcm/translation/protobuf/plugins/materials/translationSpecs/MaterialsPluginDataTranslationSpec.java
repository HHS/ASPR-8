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
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
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

                for (MaterialIdInput materialIdInput : inputObject.getMaterialIdsList()) {
                        MaterialId materialId = this.translationEnine.convertObject(materialIdInput);
                        builder.addMaterial(materialId);
                }

                for (MaterialsProducerIdInput materialsProducerIdInput : inputObject.getMaterialsProducerIdsList()) {
                        MaterialsProducerId materialsProducerId = this.translationEnine
                                        .convertObject(materialsProducerIdInput);
                        builder.addMaterialsProducerId(materialsProducerId);
                }

                for (BatchPropertyDefinitionMapInput batchPropertyDefinitionMapInput : inputObject
                                .getBatchPropertyDefinitionsList()) {
                        MaterialId materialId = this.translationEnine
                                        .convertObject(batchPropertyDefinitionMapInput.getMaterialId());
                        for (PropertyDefinitionMapInput propertyDefinitionMapInput : batchPropertyDefinitionMapInput
                                        .getPropertyDefinitionsList()) {
                                BatchPropertyId batchPropertyId = this.translationEnine
                                                .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
                                PropertyDefinition propertyDefinition = this.translationEnine
                                                .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

                                builder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
                        }
                }

                for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject
                                .getMaterialsProducerPropertyDefinitionsList()) {
                        MaterialsProducerPropertyId materialsProducerPropertyId = this.translationEnine
                                        .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
                        PropertyDefinition propertyDefinition = this.translationEnine
                                        .convertObject(propertyDefinitionMapInput.getPropertyDefinition());
                        builder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
                }

                for (MaterialsProducerPropertyValueMapInput materialsProducerPropertyValueMapInput : inputObject
                                .getMaterialsProducerPropertyValuesList()) {
                        MaterialsProducerId materialsProducerId = this.translationEnine.convertObject(
                                        materialsProducerPropertyValueMapInput.getMaterialsProducerId());
                        for (PropertyValueMapInput propertyValueMapInput : materialsProducerPropertyValueMapInput
                                        .getPropertyValuesList()) {
                                MaterialsProducerPropertyId materialsProducerPropertyId = this.translationEnine
                                                .getObjectFromAny(propertyValueMapInput.getPropertyId());
                                Object value = this.translationEnine
                                                .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                                builder.setMaterialsProducerPropertyValue(materialsProducerId,
                                                materialsProducerPropertyId, value);
                        }
                }

                for (MaterialsProducerResourceLevelMapInput materialsProducerResourceLevelMapInput : inputObject
                                .getMaterialsProducerResourceLevelsList()) {
                        MaterialsProducerId materialsProducerId = this.translationEnine.convertObject(
                                        materialsProducerResourceLevelMapInput.getMaterialsProducerId());
                        for (ResourceInitializationInput resourceInitializationInput : materialsProducerResourceLevelMapInput
                                        .getResourceLevelsList()) {
                                ResourceId resourceId = this.translationEnine
                                                .convertObject(resourceInitializationInput.getResourceId());
                                long amount = resourceInitializationInput.getAmount();

                                builder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
                        }
                }

                for (BatchMapInput batchMapInput : inputObject.getBatchIdsList()) {
                        BatchId batchId = this.translationEnine.convertObject(batchMapInput.getBatchId());
                        MaterialId materialId = this.translationEnine.convertObject(batchMapInput.getMaterialId());
                        double amount = batchMapInput.getAmount();
                        MaterialsProducerId materialsProducerId = this.translationEnine
                                        .convertObject(batchMapInput.getMaterialsProducerId());

                        builder.addBatch(batchId, materialId, amount, materialsProducerId);

                        for (PropertyValueMapInput propertyValueMapInput : batchMapInput.getPropertyValuesList()) {
                                BatchPropertyId batchPropertyId = this.translationEnine
                                                .getObjectFromAny(propertyValueMapInput.getPropertyId());
                                Object propertyValue = this.translationEnine
                                                .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                                builder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
                        }
                }

                for (StageMapInput stageMapInput : inputObject.getStageIdsList()) {
                        StageId stageId = this.translationEnine.convertObject(stageMapInput.getStageId());
                        boolean offered = stageMapInput.getOffered();
                        MaterialsProducerId materialsProducerId = this.translationEnine
                                        .convertObject(stageMapInput.getMaterialsProducerId());

                        builder.addStage(stageId, offered, materialsProducerId);

                        for (BatchIdInput batchIdInput : stageMapInput.getBatchesInStageList()) {
                                BatchId batchId = this.translationEnine.convertObject(batchIdInput);

                                builder.addBatchToStage(stageId, batchId);
                        }
                }

                return builder.build();
        }

        @Override
        protected MaterialsPluginDataInput convertAppObject(MaterialsPluginData appObject) {
                MaterialsPluginDataInput.Builder builder = MaterialsPluginDataInput.newBuilder();

                for (MaterialId materialId : appObject.getMaterialIds()) {
                        MaterialIdInput materialIdInput = this.translationEnine.convertObjectAsSafeClass(materialId,
                                        MaterialId.class);
                        // add materialIds
                        builder.addMaterialIds(materialIdInput);

                        BatchPropertyDefinitionMapInput.Builder batchPropertyDefinitionMapBuilder = BatchPropertyDefinitionMapInput
                                        .newBuilder().setMaterialId(materialIdInput);
                        for (BatchPropertyId batchPropertyId : appObject.getBatchPropertyIds(materialId)) {
                                PropertyDefinition propertyDefinition = appObject.getBatchPropertyDefinition(materialId,
                                                batchPropertyId);
                                PropertyDefinitionInput propertyDefinitionInput = this.translationEnine
                                                .convertObject(propertyDefinition);

                                PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                                                .newBuilder()
                                                .setPropertyDefinition(propertyDefinitionInput)
                                                .setPropertyId(this.translationEnine.getAnyFromObject(batchPropertyId))
                                                .build();

                                batchPropertyDefinitionMapBuilder.addPropertyDefinitions(propertyDefinitionMapInput);
                        }

                        // add batchPropertyDefinitions
                        builder.addBatchPropertyDefinitions(batchPropertyDefinitionMapBuilder.build());
                }

                for (MaterialsProducerId materialsProducerId : appObject.getMaterialsProducerIds()) {
                        MaterialsProducerIdInput materialsProducerIdInput = this.translationEnine.convertObjectAsSafeClass(
                                        materialsProducerId,
                                        MaterialsProducerId.class);

                        // add materialProducerId
                        builder.addMaterialsProducerIds(materialsProducerIdInput);

                        MaterialsProducerPropertyValueMapInput.Builder materialsProducerPropertyValueMapInput = MaterialsProducerPropertyValueMapInput
                                        .newBuilder().setMaterialsProducerId(materialsProducerIdInput);

                        for (MaterialsProducerPropertyId materialsProducerPropertyId : appObject
                                        .getMaterialsProducerPropertyValues(materialsProducerId).keySet()) {
                                Object value = appObject.getMaterialsProducerPropertyValues(materialsProducerId)
                                                .get(materialsProducerPropertyId);

                                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                                                .newBuilder()
                                                .setPropertyValue(this.translationEnine.getAnyFromObject(value))
                                                .setPropertyId(this.translationEnine.getAnyFromObject(
                                                                materialsProducerPropertyId))
                                                .build();

                                materialsProducerPropertyValueMapInput.addPropertyValues(propertyValueMapInput);
                        }

                        // add materialsproducerpropertyvalues
                        builder.addMaterialsProducerPropertyValues(materialsProducerPropertyValueMapInput.build());

                        MaterialsProducerResourceLevelMapInput.Builder resourceLevelMapBuilder = MaterialsProducerResourceLevelMapInput
                                        .newBuilder().setMaterialsProducerId(materialsProducerIdInput);
                        for (ResourceId resourceId : appObject.getResourceIds()) {
                                long amount = appObject.getMaterialsProducerResourceLevel(materialsProducerId,
                                                resourceId);
                                ResourceIdInput resourceIdInput = this.translationEnine.convertObjectAsSafeClass(resourceId,
                                                ResourceId.class);

                                ResourceInitializationInput resourceInitializationInput = ResourceInitializationInput
                                                .newBuilder()
                                                .setAmount(amount)
                                                .setResourceId(resourceIdInput)
                                                .build();

                                resourceLevelMapBuilder.addResourceLevels(resourceInitializationInput);
                        }

                        // add materialsproducerresourcelevels
                        builder.addMaterialsProducerResourceLevels(resourceLevelMapBuilder.build());
                }

                for (MaterialsProducerPropertyId materialsProducerPropertyId : appObject
                                .getMaterialsProducerPropertyIds()) {
                        PropertyDefinitionInput propertyDefinitionInput = this.translationEnine
                                        .convertObject(appObject.getMaterialsProducerPropertyDefinition(
                                                        materialsProducerPropertyId));

                        PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                                        .newBuilder()
                                        .setPropertyDefinition(propertyDefinitionInput)
                                        .setPropertyId(this.translationEnine.getAnyFromObject(materialsProducerPropertyId))
                                        .build();

                        // add materialsProducerPropertyDefinitions
                        builder.addMaterialsProducerPropertyDefinitions(propertyDefinitionMapInput);
                }

                // batches
                for (BatchId batchId : appObject.getBatchIds()) {
                        BatchMapInput.Builder batchMapBuilder = BatchMapInput.newBuilder();

                        BatchIdInput batchIdInput = this.translationEnine.convertObject(batchId);
                        double amount = appObject.getBatchAmount(batchId);
                        MaterialIdInput materialIdInput = this.translationEnine.convertObjectAsSafeClass(
                                        appObject.getBatchMaterial(batchId),
                                        MaterialId.class);
                        MaterialsProducerIdInput materialsProducerIdInput = this.translationEnine
                                        .convertObjectAsSafeClass(appObject.getBatchMaterialsProducer(batchId),
                                                        MaterialsProducerId.class);

                        batchMapBuilder
                                        .setAmount(amount)
                                        .setBatchId(batchIdInput)
                                        .setMaterialId(materialIdInput)
                                        .setMaterialsProducerId(materialsProducerIdInput);

                        for (BatchPropertyId propertyId : appObject.getBatchPropertyValues(batchId).keySet()) {
                                PropertyValueMapInput.Builder batchPropertyValueMap = PropertyValueMapInput
                                                .newBuilder();
                                Object value = appObject.getBatchPropertyValues(batchId).get(propertyId);

                                batchPropertyValueMap
                                                .setPropertyValue(this.translationEnine.getAnyFromObject(value))
                                                .setPropertyId(this.translationEnine.getAnyFromObject(propertyId));

                                batchMapBuilder.addPropertyValues(batchPropertyValueMap.build());
                        }

                        builder.addBatchIds(batchMapBuilder.build());
                }

                // stages
                for (StageId stageId : appObject.getStageIds()) {

                        StageMapInput.Builder stageMapBuilder = StageMapInput.newBuilder();

                        StageIdInput stageIdInput = this.translationEnine.convertObject(stageId);
                        boolean offered = appObject.isStageOffered(stageId);
                        MaterialsProducerIdInput materialsProducerIdInput = this.translationEnine
                                        .convertObjectAsSafeClass(appObject.getStageMaterialsProducer(stageId),
                                                        MaterialsProducerId.class);

                        stageMapBuilder
                                        .setOffered(offered)
                                        .setStageId(stageIdInput)
                                        .setMaterialsProducerId(materialsProducerIdInput);

                        for (BatchId batchId : appObject.getStageBatches(stageId)) {
                                BatchIdInput batchIdInput = this.translationEnine.convertObject(batchId);

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