package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.support.input.PropertyValueMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceLevelMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceTimeInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.PersonResourceTimeMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.RegionResourceLevelMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceInitializationInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyDefinitionMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourcePropertyValueMapInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.data.input.ResourcesPluginDataInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourceInitialization;
import plugins.resources.support.ResourcePropertyId;
import plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ResourcesPluginDataInput} and
 * {@linkplain ResourcesPluginData}
 */
public class ResourcesPluginDataTranslationSpec
                extends ProtobufTranslationSpec<ResourcesPluginDataInput, ResourcesPluginData> {

        @Override
        protected ResourcesPluginData convertInputObject(ResourcesPluginDataInput inputObject) {
                ResourcesPluginData.Builder builder = ResourcesPluginData.builder();

                for (ResourceIdMapInput resourceIdInput : inputObject.getResourceIdsList()) {
                        ResourceId resourceId = this.translationEngine.convertObject(resourceIdInput.getResourceId());
                        builder.addResource(resourceId, resourceIdInput.getResourceTime(),
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

                for (PersonResourceLevelMapInput personResourceLevelsInput : inputObject
                                .getPersonResourceLevelsList()) {
                        ResourceId resourceId = this.translationEngine
                                        .convertObject(personResourceLevelsInput.getResourceId());

                        for (PersonResourceLevelInput personResourceInput : personResourceLevelsInput
                                        .getPersonResourceLevelsList()) {
                                PersonId personId = new PersonId(personResourceInput.getPersonId());

                                builder.setPersonResourceLevel(personId, resourceId,
                                                personResourceInput.getAmount());

                        }
                }

                for (PersonResourceTimeMapInput personResourceTimesInput : inputObject.getPersonResourceTimesList()) {
                        ResourceId resourceId = this.translationEngine
                                        .convertObject(personResourceTimesInput.getResourceId());

                        for (PersonResourceTimeInput personResourceInput : personResourceTimesInput
                                        .getPersonResourceTimesList()) {
                                PersonId personId = new PersonId(personResourceInput.getPersonId());

                                builder.setPersonResourceTime(personId, resourceId,
                                                personResourceInput.getResourceTime());

                        }
                }

                for (RegionResourceLevelMapInput regionResourceLevelsInput : inputObject
                                .getRegionResourceLevelsList()) {
                        RegionId regionId = this.translationEngine
                                        .convertObject(regionResourceLevelsInput.getRegionId());

                        for (ResourceInitializationInput resourceInitializationInput : regionResourceLevelsInput
                                        .getRegionResourceLevelsList()) {
                                ResourceInitialization resourceInitialization = this.translationEngine
                                                .convertObject(resourceInitializationInput);

                                builder.setRegionResourceLevel(regionId, resourceInitialization.getResourceId(),
                                                resourceInitialization.getAmount());
                        }
                }

                return builder.build();
        }

        @Override
        protected ResourcesPluginDataInput convertAppObject(ResourcesPluginData appObject) {
                ResourcesPluginDataInput.Builder builder = ResourcesPluginDataInput.newBuilder();

                Map<ResourceId, Double> resourceDefaultTimes = appObject.getResourceDefaultTimes();
                Map<ResourceId, Map<ResourcePropertyId, PropertyDefinition>> resourcePropDefs = appObject
                                .getResourcePropertyDefinitions();
                Map<ResourceId, Map<ResourcePropertyId, Object>> resourcePropValues = appObject
                                .getResourcePropertyValues();
                Map<RegionId, Map<ResourceId, Long>> regionResourceLevels = appObject.getRegionResourceLevels();
                Map<ResourceId, List<Long>> personResourceLevels = appObject.getPersonResourceLevels();
                Map<ResourceId, List<Double>> personResourceTimes = appObject.getPersonResourceTimes();

                // Resource Ids
                for (ResourceId resourceId : resourceDefaultTimes.keySet()) {
                        ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);

                        ResourceIdMapInput resourceIdMapInput = ResourceIdMapInput.newBuilder()
                                        .setResourceId(resourceIdInput)
                                        .setResourceTime(appObject.getResourceDefaultTime(resourceId))
                                        .setResourceTimeTrackingPolicy(
                                                        appObject.getResourceTimeTrackingPolicy(resourceId))
                                        .build();

                        builder.addResourceIds(resourceIdMapInput);
                }

                // Resource Property Defs
                for (ResourceId resourceId : resourcePropDefs.keySet()) {
                        ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);

                        for (ResourcePropertyId resourcePropertyId : resourcePropDefs.get(resourceId)
                                        .keySet()) {
                                ResourcePropertyDefinitionMapInput.Builder resourcePropDefBuilder = ResourcePropertyDefinitionMapInput
                                                .newBuilder();

                                PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
                                                .convertObject(appObject.getResourcePropertyDefinition(resourceId,
                                                                resourcePropertyId));

                                PropertyDefinitionMapInput propertyDefInput = PropertyDefinitionMapInput.newBuilder()
                                                .setPropertyDefinition(propertyDefinitionInput)
                                                .setPropertyId(this.translationEngine
                                                                .getAnyFromObject(resourcePropertyId))
                                                .build();

                                resourcePropDefBuilder
                                                .setResourcePropertyDefinitionMap(propertyDefInput)
                                                .setResourceId(resourceIdInput);

                                builder.addResourcePropertyDefinitions(resourcePropDefBuilder.build());

                        }
                }

                // Resource Property Values
                for (ResourceId resourceId : resourcePropValues.keySet()) {
                        ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);

                        for (ResourcePropertyId resourcePropertyId : resourcePropValues.get(resourceId)
                                        .keySet()) {

                                ResourcePropertyValueMapInput.Builder resourcePropValBuilder = ResourcePropertyValueMapInput
                                                .newBuilder();
                                Object propertyValue = appObject
                                                .getResourcePropertyValue(resourceId, resourcePropertyId).get();

                                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput.newBuilder()
                                                .setPropertyValue(this.translationEngine
                                                                .getAnyFromObject(propertyValue))
                                                .setPropertyId(this.translationEngine
                                                                .getAnyFromObject(resourcePropertyId))
                                                .build();

                                resourcePropValBuilder
                                                .setResourcePropertyValueMap(propertyValueMapInput)
                                                .setResourceId(resourceIdInput);

                                builder.addResourcePropertyValues(resourcePropValBuilder.build());
                        }
                }

                // Region Resource Values
                for (RegionId regionId : regionResourceLevels.keySet()) {
                        RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId,
                                        RegionId.class);

                        RegionResourceLevelMapInput.Builder regionResourceLevelsBuilder = RegionResourceLevelMapInput
                                        .newBuilder()
                                        .setRegionId(regionIdInput);

                        for (ResourceId resourceId : regionResourceLevels.get(regionId).keySet()) {
                                Long regionResourceLevel = appObject.getRegionResourceLevel(regionId,
                                                resourceId).get();

                                ResourceInitialization resourceInitialization = new ResourceInitialization(
                                                resourceId,
                                                regionResourceLevel);
                                ResourceInitializationInput resourceInitializationInput = this.translationEngine
                                                .convertObject(resourceInitialization);
                                regionResourceLevelsBuilder
                                                .addRegionResourceLevels(resourceInitializationInput);

                        }

                        builder.addRegionResourceLevels(regionResourceLevelsBuilder.build());
                }

                // Person Resource Levels
                for (ResourceId resourceId : personResourceLevels.keySet()) {
                        ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);
                        List<Long> resourceLevels = personResourceLevels.get(resourceId);

                        List<PersonResourceLevelInput.Builder> personResourceInputBuilders = new ArrayList<>();
                        for (int i = 0; i < resourceLevels.size(); i++) {
                                personResourceInputBuilders.add(null);
                        }

                        for (int i = 0; i < resourceLevels.size(); i++) {
                                if (resourceLevels.get(i) != null) {
                                        PersonResourceLevelInput.Builder personResourceInputBuilder = PersonResourceLevelInput
                                                        .newBuilder()
                                                        .setAmount(resourceLevels.get(i))
                                                        .setPersonId(i);

                                        personResourceInputBuilders.set(i, personResourceInputBuilder);
                                }
                        }

                        PersonResourceLevelMapInput.Builder personLevelsInput = PersonResourceLevelMapInput.newBuilder()
                                        .setResourceId(resourceIdInput);

                        for (PersonResourceLevelInput.Builder personResourceBuilder : personResourceInputBuilders) {
                                if (personResourceBuilder != null) {
                                        personLevelsInput.addPersonResourceLevels(personResourceBuilder.build());
                                }
                        }

                        builder.addPersonResourceLevels(personLevelsInput.build());
                }

                // Person Resource Times
                for (ResourceId resourceId : personResourceTimes.keySet()) {
                        ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                                        ResourceId.class);

                        List<Double> resourceTimes = appObject.getPersonResourceTimes(resourceId);

                        List<PersonResourceTimeInput.Builder> personResourceInputBuilders = new ArrayList<>();
                        for (int i = 0; i < resourceTimes.size(); i++) {
                                personResourceInputBuilders.add(null);
                        }

                        for (int i = 0; i < resourceTimes.size(); i++) {
                                if (resourceTimes.get(i) != null) {
                                        PersonResourceTimeInput.Builder personResourceInputBuilder = PersonResourceTimeInput
                                                        .newBuilder()
                                                        .setPersonId(i)
                                                        .setResourceTime(resourceTimes.get(i));

                                        personResourceInputBuilders.set(i, personResourceInputBuilder);
                                }
                        }

                        PersonResourceTimeMapInput.Builder personLevelsInput = PersonResourceTimeMapInput.newBuilder()
                                        .setResourceId(resourceIdInput);

                        for (PersonResourceTimeInput.Builder personResourceBuilder : personResourceInputBuilders) {
                                if (personResourceBuilder != null) {
                                        personLevelsInput.addPersonResourceTimes(personResourceBuilder.build());
                                }
                        }

                        builder.addPersonResourceTimes(personLevelsInput.build());
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
