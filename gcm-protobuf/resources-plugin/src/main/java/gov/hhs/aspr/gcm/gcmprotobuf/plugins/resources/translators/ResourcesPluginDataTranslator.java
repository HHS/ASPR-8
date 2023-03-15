package gov.hhs.aspr.gcm.gcmprotobuf.plugins.resources.translators;

import java.util.List;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;
import plugins.properties.input.PropertyDefinitionInput;
import plugins.properties.input.PropertyDefinitionMapInput;
import plugins.properties.input.PropertyValueMapInput;
import plugins.properties.input.TimeTrackingPolicyInput;
import plugins.regions.input.RegionIdInput;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.input.PersonResourceLevelsInput;
import plugins.resources.input.RegionResourceLevelsInput;
import plugins.resources.input.ResourceIdInput;
import plugins.resources.input.ResourceInitializationInput;
import plugins.resources.input.ResourcePropertyDefinitionMapInput;
import plugins.resources.input.ResourcePropertyValueMapInput;
import plugins.resources.input.ResourceTimeTrackingPolicyMapInput;
import plugins.resources.input.ResourcesPluginDataInput;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class ResourcesPluginDataTranslator extends AbstractTranslator<ResourcesPluginDataInput, ResourcesPluginData> {

    @Override
    protected ResourcesPluginData convertInputObject(ResourcesPluginDataInput inputObject) {
        ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

        for (ResourceIdInput resourceIdInput : inputObject.getResourceIdsList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourceIdInput, ResourceId.class);
            builder.addResource(resourceId);
        }

        for (ResourcePropertyDefinitionMapInput resourcePropertyDefinitionMapInput : inputObject
                .getResourcePropertyDefinitionsList()) {
            ResourceId resourceId = this.translator
                    .convertInputObject(resourcePropertyDefinitionMapInput.getResourceId(), ResourceId.class);

            PropertyDefinitionMapInput propertyDefinitionMapInput = resourcePropertyDefinitionMapInput
                    .getResourcePropertyDefinitionMap();

            ResourcePropertyId resourcePropertyId = this.translator
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId(), ResourcePropertyId.class);
            PropertyDefinition propertyDefinition = this.translator
                    .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
        }

        for (ResourcePropertyValueMapInput resourcePropertyValueMapInput : inputObject
                .getResourcePropertyValuesList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourcePropertyValueMapInput.getResourceId(),
                    ResourceId.class);

            PropertyValueMapInput propertyValueMapInput = resourcePropertyValueMapInput.getResourcePropertyValueMap();
            ResourcePropertyId resourcePropertyId = this.translator
                    .getObjectFromAny(propertyValueMapInput.getPropertyId(), ResourcePropertyId.class);
            Object propertyValue = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
        }

        for (PersonResourceLevelsInput personResourceLevelsInput : inputObject.getPersonResourceLevelsList()) {
            PersonId personId = this.translator.convertInputObject(personResourceLevelsInput.getPersonId());

            for (ResourceInitializationInput resourceInitializationInput : personResourceLevelsInput
                    .getResourceLevelsList()) {
                ResourceId resourceId = this.translator.convertInputObject(resourceInitializationInput.getResourceId(),
                        ResourceId.class);
                Long amount = resourceInitializationInput.getAmount();

                builder.setPersonResourceLevel(personId, resourceId, amount);
            }
        }

        for (RegionResourceLevelsInput regionResourceLevelsInput : inputObject.getRegionResourceLevelsList()) {
            RegionId regionId = this.translator.convertInputObject(regionResourceLevelsInput.getRegionId(),
                    RegionId.class);

            for (ResourceInitializationInput resourceInitializationInput : regionResourceLevelsInput
                    .getResourceLevelsList()) {
                ResourceId resourceId = this.translator.convertInputObject(resourceInitializationInput.getResourceId(),
                        ResourceId.class);
                Long amount = resourceInitializationInput.getAmount();

                builder.setRegionResourceLevel(regionId, resourceId, amount);
            }
        }

        for (ResourceTimeTrackingPolicyMapInput resourceTimeTrackingPolicyMapInput : inputObject
                .getResourceTimeTrackingPoliciesList()) {
            ResourceId resourceId = this.translator.convertInputObject(
                    resourceTimeTrackingPolicyMapInput.getResourceId(),
                    ResourceId.class);
            TimeTrackingPolicy timeTrackingPolicy = this.translator
                    .convertInputEnum(resourceTimeTrackingPolicyMapInput.getTimeTrackingPolicy());

            builder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
        }

        return builder.build();
    }

    @Override
    protected ResourcesPluginDataInput convertSimObject(ResourcesPluginData simObject) {
        ResourcesPluginDataInput.Builder builder = ResourcesPluginDataInput.newBuilder();

        for (ResourceId resourceId : simObject.getResourceIds()) {
            ResourceIdInput resourceIdInput = this.translator.convertSimObject(resourceId, ResourceId.class);

            for (ResourcePropertyId resourcePropertyId : simObject.getResourcePropertyIds(resourceId)) {
                ResourcePropertyDefinitionMapInput.Builder resourcePropDefBuilder = ResourcePropertyDefinitionMapInput
                        .newBuilder();

                ResourcePropertyValueMapInput.Builder resourcePropValBuilder = ResourcePropertyValueMapInput
                        .newBuilder();

                PropertyDefinitionInput propertyDefinitionInput = this.translator
                        .convertSimObject(simObject.getResourcePropertyDefinition(resourceId, resourcePropertyId));

                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                        .setPropertyDefinition(propertyDefinitionInput)
                        .setPropertyId(this.translator.getAnyFromObject(
                                resourcePropertyId,
                                ResourcePropertyId.class))
                        .build();

                resourcePropDefBuilder.setResourcePropertyDefinitionMap(propertyDefInput)
                        .setResourceId(resourceIdInput);

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                        .setPropertyValue(this.translator
                                .getAnyFromObject(simObject.getResourcePropertyValue(resourceId, resourcePropertyId)))
                        .setPropertyId(this.translator.getAnyFromObject(
                                resourcePropertyId,
                                ResourcePropertyId.class))
                        .build();

                resourcePropValBuilder
                        .setResourcePropertyValueMap(propertyValueMapInput)
                        .setResourceId(resourceIdInput);

                builder.addResourcePropertyDefinitions(resourcePropDefBuilder.build());
                builder.addResourcePropertyValues(resourcePropValBuilder.build());
            }

            TimeTrackingPolicyInput timeTrackingPolicyInput = this.translator
                    .convertSimObject(simObject.getPersonResourceTimeTrackingPolicy(resourceId));
            ResourceTimeTrackingPolicyMapInput resourceTimeTrackingPolicyMapInput = ResourceTimeTrackingPolicyMapInput
                    .newBuilder()
                    .setResourceId(resourceIdInput)
                    .setTimeTrackingPolicy(timeTrackingPolicyInput)
                    .build();

            builder.addResourceTimeTrackingPolicies(resourceTimeTrackingPolicyMapInput);

            builder.addResourceIds(resourceIdInput);
        }

        for (int i = 0; i < simObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);

            List<ResourceInitialization> personResourceLevels = simObject.getPersonResourceLevels(personId);

            if (!personResourceLevels.isEmpty()) {
                PersonIdInput personIdInput = this.translator.convertSimObject(personId);
                PersonResourceLevelsInput.Builder personResourceLevelsBuilder = PersonResourceLevelsInput.newBuilder()
                        .setPersonId(personIdInput);

                for (ResourceInitialization resourceInitialization : personResourceLevels) {
                    ResourceInitializationInput resourceInitializationInput = this.translator
                            .convertSimObject(resourceInitialization);

                    personResourceLevelsBuilder.addResourceLevels(resourceInitializationInput);
                }

                builder.addPersonResourceLevels(personResourceLevelsBuilder.build());
            }
        }

        for (RegionId regionId : simObject.getRegionIds()) {
            List<ResourceInitialization> regionResourceLevels = simObject.getRegionResourceLevels(regionId);

            if (!regionResourceLevels.isEmpty()) {
                RegionIdInput regionIdInput = this.translator.convertSimObject(regionId, RegionId.class);

                RegionResourceLevelsInput.Builder regionResourceLevelsBuilder = RegionResourceLevelsInput.newBuilder()
                        .setRegionId(regionIdInput);

                for (ResourceInitialization resourceInitialization : regionResourceLevels) {
                    ResourceInitializationInput resourceInitializationInput = this.translator
                            .convertSimObject(resourceInitialization);

                    regionResourceLevelsBuilder.addResourceLevels(resourceInitializationInput);
                }
                builder.addRegionResourceLevels(regionResourceLevelsBuilder.build());
            }
        }

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return ResourcesPluginDataInput.getDescriptor();
    }

    @Override
    public ResourcesPluginDataInput getDefaultInstanceForInputObject() {
        return ResourcesPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<ResourcesPluginData> getSimObjectClass() {
        return ResourcesPluginData.class;
    }

    @Override
    public Class<ResourcesPluginDataInput> getInputObjectClass() {
        return ResourcesPluginDataInput.class;
    }

}