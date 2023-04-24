package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

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
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class RegionsPluginDataTranslatorSpec extends ProtobufTranslatorSpec<RegionsPluginDataInput, RegionsPluginData> {

    @Override
    protected RegionsPluginData convertInputObject(RegionsPluginDataInput inputObject) {
        RegionsPluginData.Builder builder = RegionsPluginData.builder();

        // add regions
        for (RegionIdInput regionIdInput : inputObject.getRegionIdsList()) {
            RegionId regionId = this.translatorCore.convertObject(regionIdInput);

            builder.addRegion(regionId);
        }

        // define regions
        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getRegionPropertyDefinitionsList()) {
            RegionPropertyId regionPropertyId = this.translatorCore
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId());
            PropertyDefinition propertyDefinition = this.translatorCore
                    .convertObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineRegionProperty(regionPropertyId, propertyDefinition);
        }

        // add region property values
        for (RegionPropertyValueMapInput regionPropertyValueMapInput : inputObject.getRegionPropertyValuesList()) {
            RegionId regionId = this.translatorCore.convertObject(regionPropertyValueMapInput.getRegionId());
            for (PropertyValueMapInput propertyValueMapInput : regionPropertyValueMapInput.getPropertyValueMapList()) {
                RegionPropertyId regionPropertyId = this.translatorCore
                        .getObjectFromAny(propertyValueMapInput.getPropertyId());
                Object regionPropertyValue = this.translatorCore.getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
            }
        }

        // assign people to regions
        for (RegionMembershipInput regionMembershipInput : inputObject.getPersonRegionsList()) {
            PersonId personId = this.translatorCore.convertObject(regionMembershipInput.getPersonId());
            RegionId regionId = this.translatorCore.convertObject(regionMembershipInput.getRegionId());

            builder.setPersonRegion(personId, regionId);
        }

        TimeTrackingPolicy timeTrackingPolicy = this.translatorCore
                .convertObject(inputObject.getRegionArrivalTimeTrackingPolicy());
        builder.setPersonRegionArrivalTracking(timeTrackingPolicy);

        return builder.build();
    }

    @Override
    protected RegionsPluginDataInput convertAppObject(RegionsPluginData simObject) {
        RegionsPluginDataInput.Builder builder = RegionsPluginDataInput.newBuilder();

        // add regions
        for (RegionId regionId : simObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translatorCore.convertObjectAsSafeClass(regionId, RegionId.class);
            builder.addRegionIds(regionIdInput);
        }

        // add region property definitions
        for (RegionPropertyId regionPropertyId : simObject.getRegionPropertyIds()) {
            PropertyDefinitionInput propertyDefinitionInput = this.translatorCore
                    .convertObject(simObject.getRegionPropertyDefinition(regionPropertyId));

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyId(this.translatorCore.getAnyFromObject(regionPropertyId, RegionPropertyId.class))
                    .setPropertyDefinition(propertyDefinitionInput)
                    .build();

            builder.addRegionPropertyDefinitions(propertyDefinitionMapInput);
        }

        for (RegionId regionId : simObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translatorCore.convertObjectAsSafeClass(regionId, RegionId.class);

            for (RegionPropertyId regionPropertyId : simObject.getRegionPropertyValues(regionId).keySet()) {
                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(this.translatorCore.getAnyFromObject(regionPropertyId, RegionPropertyId.class))
                        .setPropertyValue(this.translatorCore
                                .getAnyFromObject(simObject.getRegionPropertyValues(regionId).get(regionPropertyId)))
                        .build();

                RegionPropertyValueMapInput regionPropertyValueMapInput = RegionPropertyValueMapInput
                        .newBuilder()
                        .setRegionId(regionIdInput)
                        .addPropertyValueMap(propertyValueMapInput)
                        .build();

                builder.addRegionPropertyValues(regionPropertyValueMapInput);
            }
        }

        for (int i = 0; i < simObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);

            if (simObject.getPersonRegion(personId).isPresent()) {
                RegionId regionId = simObject.getPersonRegion(personId).get();
                RegionMembershipInput.Builder regionMembershipBuilder = RegionMembershipInput.newBuilder();
                PersonIdInput personIdInput = this.translatorCore.convertObject(personId);
                RegionIdInput regionIdInput = this.translatorCore.convertObjectAsSafeClass(regionId, RegionId.class);
                regionMembershipBuilder.setPersonId(personIdInput).setRegionId(regionIdInput);

                builder.addPersonRegions(regionMembershipBuilder.build());

            }
        }

        TimeTrackingPolicyInput timeTrackingPolicyInput = this.translatorCore
                .convertObject(simObject.getPersonRegionArrivalTrackingPolicy());

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
