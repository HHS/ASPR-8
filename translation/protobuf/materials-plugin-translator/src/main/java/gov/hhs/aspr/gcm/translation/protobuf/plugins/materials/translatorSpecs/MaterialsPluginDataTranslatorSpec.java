package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

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
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.materials.MaterialsPluginData;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyDefinition;

public class MaterialsPluginDataTranslatorSpec
                extends AbstractTranslatorSpec<MaterialsPluginDataInput, MaterialsPluginData> {

        @Override
        protected MaterialsPluginData convertInputObject(MaterialsPluginDataInput inputObject) {
                MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

                for (MaterialIdInput materialIdInput : inputObject.getMaterialIdsList()) {
                        MaterialId materialId = this.translator.convertInputObject(materialIdInput, MaterialId.class);
                        builder.addMaterial(materialId);
                }

                for (MaterialsProducerIdInput materialsProducerIdInput : inputObject.getMaterialsProducerIdsList()) {
                        MaterialsProducerId materialsProducerId = this.translator.convertInputObject(
                                        materialsProducerIdInput,
                                        MaterialsProducerId.class);
                        builder.addMaterialsProducerId(materialsProducerId);
                }

                for (BatchPropertyDefinitionMapInput batchPropertyDefinitionMapInput : inputObject
                                .getBatchPropertyDefinitionsList()) {
                        MaterialId materialId = this.translator.convertInputObject(
                                        batchPropertyDefinitionMapInput.getMaterialId(),
                                        MaterialId.class);
                        for (PropertyDefinitionMapInput propertyDefinitionMapInput : batchPropertyDefinitionMapInput
                                        .getPropertyDefinitionsList()) {
                                BatchPropertyId batchPropertyId = this.translator
                                                .getObjectFromAny(propertyDefinitionMapInput.getPropertyId(),
                                                                BatchPropertyId.class);
                                PropertyDefinition propertyDefinition = this.translator
                                                .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

                                builder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
                        }
                }

                for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject
                                .getMaterialsProducerPropertyDefinitionsList()) {
                        MaterialsProducerPropertyId materialsProducerPropertyId = this.translator
                                        .getObjectFromAny(propertyDefinitionMapInput.getPropertyId(),
                                                        MaterialsProducerPropertyId.class);
                        PropertyDefinition propertyDefinition = this.translator
                                        .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());
                        builder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
                }

                for (MaterialsProducerPropertyValueMapInput materialsProducerPropertyValueMapInput : inputObject
                                .getMaterialsProducerPropertyValuesList()) {
                        MaterialsProducerId materialsProducerId = this.translator.convertInputObject(
                                        materialsProducerPropertyValueMapInput.getMaterialsProducerId(),
                                        MaterialsProducerId.class);
                        for (PropertyValueMapInput propertyValueMapInput : materialsProducerPropertyValueMapInput
                                        .getPropertyValuesList()) {
                                MaterialsProducerPropertyId materialsProducerPropertyId = this.translator
                                                .getObjectFromAny(
                                                                propertyValueMapInput.getPropertyId(),
                                                                MaterialsProducerPropertyId.class);
                                Object value = this.translator
                                                .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                                builder.setMaterialsProducerPropertyValue(materialsProducerId,
                                                materialsProducerPropertyId, value);
                        }
                }

                for (MaterialsProducerResourceLevelMapInput materialsProducerResourceLevelMapInput : inputObject
                                .getMaterialsProducerResourceLevelsList()) {
                        MaterialsProducerId materialsProducerId = this.translator.convertInputObject(
                                        materialsProducerResourceLevelMapInput.getMaterialsProducerId(),
                                        MaterialsProducerId.class);
                        for (ResourceInitializationInput resourceInitializationInput : materialsProducerResourceLevelMapInput
                                        .getResourceLevelsList()) {
                                ResourceId resourceId = this.translator.convertInputObject(
                                                resourceInitializationInput.getResourceId(),
                                                ResourceId.class);
                                long amount = resourceInitializationInput.getAmount();

                                builder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
                        }
                }

                for (BatchMapInput batchMapInput : inputObject.getBatchIdsList()) {
                        BatchId batchId = this.translator.convertInputObject(batchMapInput.getBatchId());
                        MaterialId materialId = this.translator.convertInputObject(batchMapInput.getMaterialId(),
                                        MaterialId.class);
                        double amount = batchMapInput.getAmount();
                        MaterialsProducerId materialsProducerId = this.translator
                                        .convertInputObject(batchMapInput.getMaterialsProducerId(),
                                                        MaterialsProducerId.class);

                        builder.addBatch(batchId, materialId, amount, materialsProducerId);

                        for (PropertyValueMapInput propertyValueMapInput : batchMapInput.getPropertyValuesList()) {
                                BatchPropertyId batchPropertyId = this.translator
                                                .getObjectFromAny(propertyValueMapInput.getPropertyId(),
                                                                BatchPropertyId.class);
                                Object propertyValue = this.translator
                                                .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                                builder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
                        }
                }

                for (StageMapInput stageMapInput : inputObject.getStageIdsList()) {
                        StageId stageId = this.translator.convertInputObject(stageMapInput.getStageId());
                        boolean offered = stageMapInput.getOffered();
                        MaterialsProducerId materialsProducerId = this.translator
                                        .convertInputObject(stageMapInput.getMaterialsProducerId(),
                                                        MaterialsProducerId.class);

                        builder.addStage(stageId, offered, materialsProducerId);

                        for (BatchIdInput batchIdInput : stageMapInput.getBatchesInStageList()) {
                                BatchId batchId = this.translator.convertInputObject(batchIdInput);

                                builder.addBatchToStage(stageId, batchId);
                        }
                }

                return builder.build();
        }

        @Override
        protected MaterialsPluginDataInput convertAppObject(MaterialsPluginData simObject) {
                MaterialsPluginDataInput.Builder builder = MaterialsPluginDataInput.newBuilder();

                for (MaterialId materialId : simObject.getMaterialIds()) {
                        MaterialIdInput materialIdInput = this.translator.convertSimObject(materialId,
                                        MaterialId.class);
                        // add materialIds
                        builder.addMaterialIds(materialIdInput);

                        BatchPropertyDefinitionMapInput.Builder batchPropertyDefinitionMapBuilder = BatchPropertyDefinitionMapInput
                                        .newBuilder().setMaterialId(materialIdInput);
                        for (BatchPropertyId batchPropertyId : simObject.getBatchPropertyIds(materialId)) {
                                PropertyDefinition propertyDefinition = simObject.getBatchPropertyDefinition(materialId,
                                                batchPropertyId);
                                PropertyDefinitionInput propertyDefinitionInput = this.translator
                                                .convertSimObject(propertyDefinition);

                                PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                                                .newBuilder()
                                                .setPropertyDefinition(propertyDefinitionInput)
                                                .setPropertyId(this.translator.getAnyFromObject(batchPropertyId,
                                                                BatchPropertyId.class))
                                                .build();

                                batchPropertyDefinitionMapBuilder.addPropertyDefinitions(propertyDefinitionMapInput);
                        }

                        // add batchPropertyDefinitions
                        builder.addBatchPropertyDefinitions(batchPropertyDefinitionMapBuilder.build());
                }

                for (MaterialsProducerId materialsProducerId : simObject.getMaterialsProducerIds()) {
                        MaterialsProducerIdInput materialsProducerIdInput = this.translator.convertSimObject(
                                        materialsProducerId,
                                        MaterialsProducerId.class);

                        // add materialProducerId
                        builder.addMaterialsProducerIds(materialsProducerIdInput);

                        MaterialsProducerPropertyValueMapInput.Builder materialsProducerPropertyValueMapInput = MaterialsProducerPropertyValueMapInput
                                        .newBuilder().setMaterialsProducerId(materialsProducerIdInput);

                        for (MaterialsProducerPropertyId materialsProducerPropertyId : simObject
                                        .getMaterialsProducerPropertyValues(materialsProducerId).keySet()) {
                                Object value = simObject.getMaterialsProducerPropertyValues(materialsProducerId)
                                                .get(materialsProducerPropertyId);

                                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                                                .newBuilder()
                                                .setPropertyValue(this.translator.getAnyFromObject(value))
                                                .setPropertyId(this.translator.getAnyFromObject(
                                                                materialsProducerPropertyId,
                                                                MaterialsProducerPropertyId.class))
                                                .build();

                                materialsProducerPropertyValueMapInput.addPropertyValues(propertyValueMapInput);
                        }

                        // add materialsproducerpropertyvalues
                        builder.addMaterialsProducerPropertyValues(materialsProducerPropertyValueMapInput.build());

                        MaterialsProducerResourceLevelMapInput.Builder resourceLevelMapBuilder = MaterialsProducerResourceLevelMapInput
                                        .newBuilder().setMaterialsProducerId(materialsProducerIdInput);
                        for (ResourceId resourceId : simObject.getResourceIds()) {
                                long amount = simObject.getMaterialsProducerResourceLevel(materialsProducerId,
                                                resourceId);
                                ResourceIdInput resourceIdInput = this.translator.convertSimObject(resourceId,
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

                for (MaterialsProducerPropertyId materialsProducerPropertyId : simObject
                                .getMaterialsProducerPropertyIds()) {
                        PropertyDefinitionInput propertyDefinitionInput = this.translator
                                        .convertSimObject(simObject.getMaterialsProducerPropertyDefinition(
                                                        materialsProducerPropertyId));

                        PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                                        .newBuilder()
                                        .setPropertyDefinition(propertyDefinitionInput)
                                        .setPropertyId(this.translator.getAnyFromObject(materialsProducerPropertyId,
                                                        MaterialsProducerPropertyId.class))
                                        .build();

                        // add materialsProducerPropertyDefinitions
                        builder.addMaterialsProducerPropertyDefinitions(propertyDefinitionMapInput);
                }

                // batches
                for (BatchId batchId : simObject.getBatchIds()) {
                        BatchMapInput.Builder batchMapBuilder = BatchMapInput.newBuilder();

                        BatchIdInput batchIdInput = this.translator.convertSimObject(batchId);
                        double amount = simObject.getBatchAmount(batchId);
                        MaterialIdInput materialIdInput = this.translator.convertSimObject(
                                        simObject.getBatchMaterial(batchId),
                                        MaterialId.class);
                        MaterialsProducerIdInput materialsProducerIdInput = this.translator
                                        .convertSimObject(simObject.getBatchMaterialsProducer(batchId),
                                                        MaterialsProducerId.class);

                        batchMapBuilder
                                        .setAmount(amount)
                                        .setBatchId(batchIdInput)
                                        .setMaterialId(materialIdInput)
                                        .setMaterialsProducerId(materialsProducerIdInput);

                        for (BatchPropertyId propertyId : simObject.getBatchPropertyValues(batchId).keySet()) {
                                PropertyValueMapInput.Builder batchPropertyValueMap = PropertyValueMapInput
                                                .newBuilder();
                                Object value = simObject.getBatchPropertyValues(batchId).get(propertyId);

                                batchPropertyValueMap
                                                .setPropertyValue(this.translator.getAnyFromObject(value))
                                                .setPropertyId(this.translator.getAnyFromObject(propertyId,
                                                                BatchPropertyId.class));

                                batchMapBuilder.addPropertyValues(batchPropertyValueMap.build());
                        }

                        builder.addBatchIds(batchMapBuilder.build());
                }

                // stages
                for (StageId stageId : simObject.getStageIds()) {

                        StageMapInput.Builder stageMapBuilder = StageMapInput.newBuilder();

                        StageIdInput stageIdInput = this.translator.convertSimObject(stageId);
                        boolean offered = simObject.isStageOffered(stageId);
                        MaterialsProducerIdInput materialsProducerIdInput = this.translator
                                        .convertSimObject(simObject.getStageMaterialsProducer(stageId),
                                                        MaterialsProducerId.class);

                        stageMapBuilder
                                        .setOffered(offered)
                                        .setStageId(stageIdInput)
                                        .setMaterialsProducerId(materialsProducerIdInput);

                        for (BatchId batchId : simObject.getStageBatches(stageId)) {
                                BatchIdInput batchIdInput = this.translator.convertSimObject(batchId);

                                stageMapBuilder.addBatchesInStage(batchIdInput);
                        }

                        builder.addStageIds(stageMapBuilder.build());
                }
                return builder.build();
        }

        @Override
        public MaterialsPluginDataInput getDefaultInstanceForInputObject() {
                return MaterialsPluginDataInput.getDefaultInstance();
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
