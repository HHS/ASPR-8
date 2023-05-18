package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceLevelsInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.RegionResourceInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.RegionResourceLevelsInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcesPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;

public class ResourcesPluginDataTranslationSpec
        extends ProtobufTranslationSpec<ResourcesPluginDataInput, ResourcesPluginData> {

    @Override
    protected ResourcesPluginData convertInputObject(ResourcesPluginDataInput inputObject) {
        ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

        for (ResourceIdMapInput resourceIdInput : inputObject.getResourceIdsList()) {
            ResourceId resourceId = this.translationEngine.convertObject(resourceIdInput.getResourceId());
            builder
                    .addResource(resourceId, resourceIdInput.getResourceTime())
                    .setResourceTimeTracking(resourceId,
                            resourceIdInput.getResourceTimeTrackingPolicy());
        }

        for (ResourcePropertyDefinitionMapInput resourcePropertyDefinitionMapInput : inputObject
                .getResourcePropertyDefinitionsList()) {
            ResourceId resourceId = this.translationEngine
                    .convertObject(resourcePropertyDefinitionMapInput.getResourceId());

            PropertyDefinitionMapInput propertyDefinitionMapInput = resourcePropertyDefinitionMapInput
                    .getResourcePropertyDefinitionMap();

            ResourcePropertyId resourcePropertyId = this.translationEngine
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translationEngine
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
        }

        for (ResourcePropertyValueMapInput resourcePropertyValueMapInput : inputObject
                .getResourcePropertyValuesList()) {
            ResourceId resourceId = this.translationEngine.convertObject(
                    resourcePropertyValueMapInput.getResourceId());

            PropertyValueMapInput propertyValueMapInput = resourcePropertyValueMapInput
                    .getResourcePropertyValueMap();
            ResourcePropertyId resourcePropertyId = this.translationEngine
                    .getObjectFromAny(propertyValueMapInput.getPropertyId());
            Object propertyValue = this.translationEngine
                    .getObjectFromAny(propertyValueMapInput.getPropertyValue());

            builder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
        }

        for (PersonResourceLevelsInput personResourceLevelsInput : inputObject.getPersonResourceLevelsList()) {
            ResourceId resourceId = this.translationEngine.convertObject(personResourceLevelsInput.getResourceId());

            for (PersonResourceInput personResourceInput : personResourceLevelsInput.getPersonResourceLevelsList()) {
                PersonId personId = this.translationEngine
                        .convertObject(personResourceInput.getPersonId());

                if (personResourceInput.hasAmount()) {
                    builder.setPersonResourceLevel(personId, resourceId, personResourceInput.getAmount());
                }

                if (personResourceInput.hasResourceTime()) {
                    builder.setPersonResourceTime(personId, resourceId, personResourceInput.getResourceTime());
                }
            }
        }

        for (RegionResourceLevelsInput regionResourceLevelsInput : inputObject.getRegionResourceLevelsList()) {
            ResourceId resourceId = this.translationEngine.convertObject(regionResourceLevelsInput.getResourceId());

            for (RegionResourceInput regionResourceInput : regionResourceLevelsInput.getRegionResourceLevelsList()) {
                RegionId regionId = this.translationEngine
                        .convertObject(regionResourceInput.getRegionId());

                builder.setRegionResourceLevel(regionId, resourceId, regionResourceInput.getAmount());
            }
        }

        return builder.build();
    }

    @Override
    protected ResourcesPluginDataInput convertAppObject(ResourcesPluginData appObject) {
        ResourcesPluginDataInput.Builder builder = ResourcesPluginDataInput.newBuilder();

        for (ResourceId resourceId : appObject.getResourceIds()) {
            ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                    ResourceId.class);

            for (ResourcePropertyId resourcePropertyId : appObject.getResourcePropertyIds(resourceId)) {
                ResourcePropertyDefinitionMapInput.Builder resourcePropDefBuilder = ResourcePropertyDefinitionMapInput
                        .newBuilder();

                ResourcePropertyValueMapInput.Builder resourcePropValBuilder = ResourcePropertyValueMapInput
                        .newBuilder();

                PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
                        .convertObject(appObject.getResourcePropertyDefinition(resourceId,
                                resourcePropertyId));

                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                        .setPropertyDefinition(propertyDefinitionInput)
                        .setPropertyId(this.translationEngine
                                .getAnyFromObject(resourcePropertyId))
                        .build();

                resourcePropDefBuilder.setResourcePropertyDefinitionMap(propertyDefInput)
                        .setResourceId(resourceIdInput);

                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                        .setPropertyValue(this.translationEngine
                                .getAnyFromObject(appObject.getResourcePropertyValue(
                                        resourceId, resourcePropertyId)))
                        .setPropertyId(this.translationEngine
                                .getAnyFromObject(resourcePropertyId))
                        .build();

                resourcePropValBuilder
                        .setResourcePropertyValueMap(propertyValueMapInput)
                        .setResourceId(resourceIdInput);

                builder
                        .addResourcePropertyDefinitions(resourcePropDefBuilder.build())
                        .addResourcePropertyValues(resourcePropValBuilder.build());
            }

            ResourceIdMapInput resourceIdMapInput = ResourceIdMapInput.newBuilder()
                    .setResourceId(resourceIdInput)
                    .setResourceTime(appObject.getResourceDefaultTime(resourceId))
                    .setResourceTimeTrackingPolicy(appObject.getResourceTimeTrackingPolicy(resourceId))
                    .build();
            builder.addResourceIds(resourceIdMapInput);

            List<PersonResourceInput.Builder> personResourceInputBuilders = new ArrayList<>();

            List<Long> resourceLevels = appObject.getPersonResourceLevels(resourceId);
            List<Double> resourceTimes = appObject.getPersonResourceTimes(resourceId);

            int maxPersonId = FastMath.max(resourceLevels.size(), resourceTimes.size());

            // prepopulate nulls based on max personId
            for (int i = 0; i < maxPersonId; i++) {
                personResourceInputBuilders.add(null);
            }

            for (int i = 0; i < resourceLevels.size(); i++) {
                if (resourceLevels.get(i) != null) {
                    PersonResourceInput.Builder personResourceInputBuilder = PersonResourceInput.newBuilder()
                            .setAmount(resourceLevels.get(i))
                            .setPersonId(i);

                    personResourceInputBuilders.set(i, personResourceInputBuilder);
                }
            }

            for (int i = 0; i < resourceTimes.size(); i++) {
                if (resourceTimes.get(i) != null) {
                    PersonResourceInput.Builder personResourceInputBuilder = PersonResourceInput
                            .newBuilder();
                    // check for and use existing builder, if there is one
                    if (personResourceInputBuilders.get(i) != null) {
                        personResourceInputBuilder = personResourceInputBuilders.get(i);

                        personResourceInputBuilder.setResourceTime(resourceTimes.get(i));

                    } else {
                        personResourceInputBuilder
                                .setPersonId(i)
                                .setResourceTime(resourceTimes.get(i));
                    }

                    personResourceInputBuilders.set(i, personResourceInputBuilder);
                }
            }

            PersonResourceLevelsInput.Builder personLevelsInput = PersonResourceLevelsInput.newBuilder()
                    .setResourceId(resourceIdInput);

            for (PersonResourceInput.Builder personResourceBuilder : personResourceInputBuilders) {
                if (personResourceBuilder != null) {
                    personLevelsInput.addPersonResourceLevels(personResourceBuilder.build());
                }
            }

            builder.addPersonResourceLevels(personLevelsInput.build());

            for (RegionId regionId : appObject.getRegionIds()) {
                List<ResourceInitialization> regionResourceLevels = appObject.getRegionResourceLevels(regionId);

                if (!regionResourceLevels.isEmpty()) {
                    RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId,
                            RegionId.class);

                    RegionResourceLevelsInput.Builder regionResourceLevelsBuilder = RegionResourceLevelsInput
                            .newBuilder()
                            .setResourceId(resourceIdInput);

                    for (ResourceInitialization resourceInitialization : regionResourceLevels) {
                        RegionResourceInput regionResourceInput = RegionResourceInput.newBuilder()
                                .setRegionId(regionIdInput)
                                .setAmount(resourceInitialization.getAmount())
                                .build();

                        regionResourceLevelsBuilder.addRegionResourceLevels(regionResourceInput);
                    }
                    builder.addRegionResourceLevels(regionResourceLevelsBuilder.build());
                }
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
