package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import java.util.List;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.TimeTrackingPolicyInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceLevelsInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.RegionResourceLevelsInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceInitializationInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceTimeTrackingPolicyMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class ResourcesPluginDataTranslatorSpec
                extends ProtobufTranslationSpec<ResourcesPluginDataInput, ResourcesPluginData> {

        @Override
        protected ResourcesPluginData convertInputObject(ResourcesPluginDataInput inputObject) {
                ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

                for (ResourceIdInput resourceIdInput : inputObject.getResourceIdsList()) {
                        ResourceId resourceId = this.translatorCore.convertObject(resourceIdInput);
                        builder.addResource(resourceId);
                }

                for (ResourcePropertyDefinitionMapInput resourcePropertyDefinitionMapInput : inputObject
                                .getResourcePropertyDefinitionsList()) {
                        ResourceId resourceId = this.translatorCore
                                        .convertObject(resourcePropertyDefinitionMapInput.getResourceId());

                        PropertyDefinitionMapInput propertyDefinitionMapInput = resourcePropertyDefinitionMapInput
                                        .getResourcePropertyDefinitionMap();

                        ResourcePropertyId resourcePropertyId = this.translatorCore
                                        .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
                        PropertyDefinition propertyDefinition = this.translatorCore
                                        .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

                        builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
                }

                for (ResourcePropertyValueMapInput resourcePropertyValueMapInput : inputObject
                                .getResourcePropertyValuesList()) {
                        ResourceId resourceId = this.translatorCore.convertObject(
                                        resourcePropertyValueMapInput.getResourceId());

                        PropertyValueMapInput propertyValueMapInput = resourcePropertyValueMapInput
                                        .getResourcePropertyValueMap();
                        ResourcePropertyId resourcePropertyId = this.translatorCore
                                        .getObjectFromAny(propertyValueMapInput.getPropertyId());
                        Object propertyValue = this.translatorCore
                                        .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                        builder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
                }

                for (PersonResourceLevelsInput personResourceLevelsInput : inputObject.getPersonResourceLevelsList()) {
                        PersonId personId = this.translatorCore.convertObject(personResourceLevelsInput.getPersonId());

                        for (ResourceInitializationInput resourceInitializationInput : personResourceLevelsInput
                                        .getResourceLevelsList()) {
                                ResourceId resourceId = this.translatorCore.convertObject(
                                                resourceInitializationInput.getResourceId());
                                Long amount = resourceInitializationInput.getAmount();

                                builder.setPersonResourceLevel(personId, resourceId, amount);
                        }
                }

                for (RegionResourceLevelsInput regionResourceLevelsInput : inputObject.getRegionResourceLevelsList()) {
                        RegionId regionId = this.translatorCore.convertObject(regionResourceLevelsInput.getRegionId());

                        for (ResourceInitializationInput resourceInitializationInput : regionResourceLevelsInput
                                        .getResourceLevelsList()) {
                                ResourceId resourceId = this.translatorCore.convertObject(
                                                resourceInitializationInput.getResourceId());
                                Long amount = resourceInitializationInput.getAmount();

                                builder.setRegionResourceLevel(regionId, resourceId, amount);
                        }
                }

                for (ResourceTimeTrackingPolicyMapInput resourceTimeTrackingPolicyMapInput : inputObject
                                .getResourceTimeTrackingPoliciesList()) {
                        ResourceId resourceId = this.translatorCore.convertObject(
                                        resourceTimeTrackingPolicyMapInput.getResourceId());
                        TimeTrackingPolicy timeTrackingPolicy = this.translatorCore
                                        .convertObject(resourceTimeTrackingPolicyMapInput.getTimeTrackingPolicy());

                        builder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
                }

                return builder.build();
        }

        @Override
        protected ResourcesPluginDataInput convertAppObject(ResourcesPluginData appObject) {
                ResourcesPluginDataInput.Builder builder = ResourcesPluginDataInput.newBuilder();

                for (ResourceId resourceId : appObject.getResourceIds()) {
                        ResourceIdInput resourceIdInput = this.translatorCore.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);

                        for (ResourcePropertyId resourcePropertyId : appObject.getResourcePropertyIds(resourceId)) {
                                ResourcePropertyDefinitionMapInput.Builder resourcePropDefBuilder = ResourcePropertyDefinitionMapInput
                                                .newBuilder();

                                ResourcePropertyValueMapInput.Builder resourcePropValBuilder = ResourcePropertyValueMapInput
                                                .newBuilder();

                                PropertyDefinitionInput propertyDefinitionInput = this.translatorCore
                                                .convertObject(appObject.getResourcePropertyDefinition(resourceId,
                                                                resourcePropertyId));

                                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                                                .setPropertyDefinition(propertyDefinitionInput)
                                                .setPropertyId(this.translatorCore.getAnyFromObject(resourcePropertyId))
                                                .build();

                                resourcePropDefBuilder.setResourcePropertyDefinitionMap(propertyDefInput)
                                                .setResourceId(resourceIdInput);

                                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                                                .setPropertyValue(this.translatorCore
                                                                .getAnyFromObject(appObject.getResourcePropertyValue(
                                                                                resourceId, resourcePropertyId)))
                                                .setPropertyId(this.translatorCore.getAnyFromObject(resourcePropertyId))
                                                .build();

                                resourcePropValBuilder
                                                .setResourcePropertyValueMap(propertyValueMapInput)
                                                .setResourceId(resourceIdInput);

                                builder.addResourcePropertyDefinitions(resourcePropDefBuilder.build());
                                builder.addResourcePropertyValues(resourcePropValBuilder.build());
                        }

                        TimeTrackingPolicyInput timeTrackingPolicyInput = this.translatorCore
                                        .convertObject(appObject.getPersonResourceTimeTrackingPolicy(resourceId));
                        ResourceTimeTrackingPolicyMapInput resourceTimeTrackingPolicyMapInput = ResourceTimeTrackingPolicyMapInput
                                        .newBuilder()
                                        .setResourceId(resourceIdInput)
                                        .setTimeTrackingPolicy(timeTrackingPolicyInput)
                                        .build();

                        builder.addResourceTimeTrackingPolicies(resourceTimeTrackingPolicyMapInput);

                        builder.addResourceIds(resourceIdInput);
                }

                for (int i = 0; i < appObject.getPersonCount(); i++) {
                        PersonId personId = new PersonId(i);

                        List<ResourceInitialization> personResourceLevels = appObject.getPersonResourceLevels(personId);

                        if (!personResourceLevels.isEmpty()) {
                                PersonIdInput personIdInput = this.translatorCore.convertObject(personId);
                                PersonResourceLevelsInput.Builder personResourceLevelsBuilder = PersonResourceLevelsInput
                                                .newBuilder()
                                                .setPersonId(personIdInput);

                                for (ResourceInitialization resourceInitialization : personResourceLevels) {
                                        ResourceInitializationInput resourceInitializationInput = this.translatorCore
                                                        .convertObject(resourceInitialization);

                                        personResourceLevelsBuilder.addResourceLevels(resourceInitializationInput);
                                }

                                builder.addPersonResourceLevels(personResourceLevelsBuilder.build());
                        }
                }

                for (RegionId regionId : appObject.getRegionIds()) {
                        List<ResourceInitialization> regionResourceLevels = appObject.getRegionResourceLevels(regionId);

                        if (!regionResourceLevels.isEmpty()) {
                                RegionIdInput regionIdInput = this.translatorCore.convertObjectAsSafeClass(regionId,
                                                RegionId.class);

                                RegionResourceLevelsInput.Builder regionResourceLevelsBuilder = RegionResourceLevelsInput
                                                .newBuilder()
                                                .setRegionId(regionIdInput);

                                for (ResourceInitialization resourceInitialization : regionResourceLevels) {
                                        ResourceInitializationInput resourceInitializationInput = this.translatorCore
                                                        .convertObject(resourceInitialization);

                                        regionResourceLevelsBuilder.addResourceLevels(resourceInitializationInput);
                                }
                                builder.addRegionResourceLevels(regionResourceLevelsBuilder.build());
                        }
                }

                return builder.build();
        }

        

        @Override
        public Class<ResourcesPluginData> getAppObjectClass() {
                return ResourcesPluginData.class;
        }

        @Override
        public Class<ResourcesPluginDataInput> getInputObjectClass() {
                return ResourcesPluginDataInput.class;
        }

}
