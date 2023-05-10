package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.TimeTrackingPolicyInput;
import plugins.regions.RegionsPluginData;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionMembershipInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class RegionsPluginDataTranslationSpec
        extends ProtobufTranslationSpec<RegionsPluginDataInput, RegionsPluginData> {

    @Override
    protected RegionsPluginData convertInputObject(RegionsPluginDataInput inputObject) {
        RegionsPluginData.Builder builder = RegionsPluginData.builder();

        // add regions
        for (RegionIdInput regionIdInput : inputObject.getRegionIdsList()) {
            RegionId regionId = this.translationEngine.convertObject(regionIdInput);

            builder.addRegion(regionId);
        }

        // define regions
        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getRegionPropertyDefinitionsList()) {
            RegionPropertyId regionPropertyId = this.translationEngine
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translationEngine
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineRegionProperty(regionPropertyId, propertyDefinition);
        }

        // add region property values
        for (RegionPropertyValueMapInput regionPropertyValueMapInput : inputObject.getRegionPropertyValuesList()) {
            RegionId regionId = this.translationEngine.convertObject(regionPropertyValueMapInput.getRegionId());
            for (PropertyValueMapInput propertyValueMapInput : regionPropertyValueMapInput.getPropertyValueMapList()) {
                RegionPropertyId regionPropertyId = this.translationEngine
                        .getObjectFromAny(propertyValueMapInput.getPropertyId());
                Object regionPropertyValue = this.translationEngine
                        .getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
            }
        }

        // assign people to regions
        for (RegionMembershipInput regionMembershipInput : inputObject.getPersonRegionsList()) {
            PersonId personId = this.translationEngine.convertObject(regionMembershipInput.getPersonId());
            RegionId regionId = this.translationEngine.convertObject(regionMembershipInput.getRegionId());

            builder.setPersonRegion(personId, regionId);
        }

        TimeTrackingPolicy timeTrackingPolicy = this.translationEngine
                .convertObject(inputObject.getRegionArrivalTimeTrackingPolicy());
        builder.setPersonRegionArrivalTracking(timeTrackingPolicy);

        return builder.build();
    }

    @Override
    protected RegionsPluginDataInput convertAppObject(RegionsPluginData appObject) {
        RegionsPluginDataInput.Builder builder = RegionsPluginDataInput.newBuilder();

        // add regions
        for (RegionId regionId : appObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId, RegionId.class);
            builder.addRegionIds(regionIdInput);
        }

        // add region property definitions
        for (RegionPropertyId regionPropertyId : appObject.getRegionPropertyIds()) {
            PropertyDefinitionInput propertyDefinitionInput = this.translationEngine
                    .convertObject(appObject.getRegionPropertyDefinition(regionPropertyId));

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyId(this.translationEngine.getAnyFromObject(regionPropertyId))
                    .setPropertyDefinition(propertyDefinitionInput)
                    .build();

            builder.addRegionPropertyDefinitions(propertyDefinitionMapInput);
        }

        for (RegionId regionId : appObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId, RegionId.class);

            for (RegionPropertyId regionPropertyId : appObject.getRegionPropertyValues(regionId).keySet()) {
                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(this.translationEngine.getAnyFromObject(regionPropertyId))
                        .setPropertyValue(this.translationEngine
                                .getAnyFromObject(appObject.getRegionPropertyValues(regionId).get(regionPropertyId)))
                        .build();

                RegionPropertyValueMapInput regionPropertyValueMapInput = RegionPropertyValueMapInput
                        .newBuilder()
                        .setRegionId(regionIdInput)
                        .addPropertyValueMap(propertyValueMapInput)
                        .build();

                builder.addRegionPropertyValues(regionPropertyValueMapInput);
            }
        }

        for (int i = 0; i < appObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);

            if (appObject.getPersonRegion(personId).isPresent()) {
                RegionId regionId = appObject.getPersonRegion(personId).get();
                RegionMembershipInput.Builder regionMembershipBuilder = RegionMembershipInput.newBuilder();
                PersonIdInput personIdInput = this.translationEngine.convertObject(personId);
                RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId, RegionId.class);
                regionMembershipBuilder.setPersonId(personIdInput).setRegionId(regionIdInput);

                builder.addPersonRegions(regionMembershipBuilder.build());

            }
        }

        TimeTrackingPolicyInput timeTrackingPolicyInput = this.translationEngine
                .convertObject(appObject.getPersonRegionArrivalTrackingPolicy());

        builder.setRegionArrivalTimeTrackingPolicy(timeTrackingPolicyInput);

        return builder.build();
    }

    @Override
    public Class<RegionsPluginData> getAppObjectClass() {
        return RegionsPluginData.class;
    }

    @Override
    public Class<RegionsPluginDataInput> getInputObjectClass() {
        return RegionsPluginDataInput.class;
    }

}
