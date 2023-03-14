package gov.hhs.aspr.gcm.gcmprotobuf.plugins.regions.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.people.input.PersonIdInput;
import plugins.people.support.PersonId;
import plugins.properties.input.PropertyDefinitionInput;
import plugins.properties.input.PropertyDefinitionMapInput;
import plugins.properties.input.PropertyValueMapInput;
import plugins.properties.input.TimeTrackingPolicyInput;
import plugins.regions.RegionsPluginData;
import plugins.regions.input.RegionIdInput;
import plugins.regions.input.RegionMembershipInput;
import plugins.regions.input.RegionPropertyValueMapInput;
import plugins.regions.input.RegionsPluginDataInput;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.TimeTrackingPolicy;

public class RegionsPluginDataTranslator extends AbstractTranslator<RegionsPluginDataInput, RegionsPluginData> {

    @Override
    protected RegionsPluginData convertInputObject(RegionsPluginDataInput inputObject) {
        RegionsPluginData.Builder builder = RegionsPluginData.builder();

        // add regions
        for (RegionIdInput regionIdInput : inputObject.getRegionIdsList()) {
            RegionId regionId = this.translator.convertInputObject(regionIdInput, RegionId.class);

            builder.addRegion(regionId);
        }

        // define regions
        for (PropertyDefinitionMapInput propertyDefinitionMapInput : inputObject.getRegionPropertyDefinitionsList()) {
            RegionPropertyId regionPropertyId = this.translator
                    .getObjectFromAny(propertyDefinitionMapInput.getPropertyId(), RegionPropertyId.class);
            PropertyDefinition propertyDefinition = this.translator
                    .convertInputObject(propertyDefinitionMapInput.getPropertyDefinition());

            builder.defineRegionProperty(regionPropertyId, propertyDefinition);
        }

        // add region property values
        for (RegionPropertyValueMapInput regionPropertyValueMapInput : inputObject.getRegionPropertyValuesList()) {
            RegionId regionId = this.translator.convertInputObject(regionPropertyValueMapInput.getRegionId(),
                    RegionId.class);
            for (PropertyValueMapInput propertyValueMapInput : regionPropertyValueMapInput.getPropertyValueMapList()) {
                RegionPropertyId regionPropertyId = this.translator
                        .getObjectFromAny(propertyValueMapInput.getPropertyId(), RegionPropertyId.class);
                Object regionPropertyValue = this.translator.getObjectFromAny(propertyValueMapInput.getPropertyValue());

                builder.setRegionPropertyValue(regionId, regionPropertyId, regionPropertyValue);
            }
        }

        // assign people to regions
        for (RegionMembershipInput regionMembershipInput : inputObject.getPersonRegionsList()) {
            PersonId personId = this.translator.convertInputObject(regionMembershipInput.getPersonId());
            RegionId regionId = this.translator.convertInputObject(regionMembershipInput.getRegionId(), RegionId.class);

            builder.setPersonRegion(personId, regionId);
        }

        TimeTrackingPolicy timeTrackingPolicy = this.translator
                .convertInputEnum(inputObject.getRegionArrivalTimeTrackingPolicy());
        builder.setPersonRegionArrivalTracking(timeTrackingPolicy);

        return builder.build();
    }

    @Override
    protected RegionsPluginDataInput convertSimObject(RegionsPluginData simObject) {
        RegionsPluginDataInput.Builder builder = RegionsPluginDataInput.newBuilder();

        // add regions
        for (RegionId regionId : simObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translator.convertSimObject(regionId, RegionId.class);
            builder.addRegionIds(regionIdInput);
        }

        // add region property definitions
        for (RegionPropertyId regionPropertyId : simObject.getRegionPropertyIds()) {
            PropertyDefinitionInput propertyDefinitionInput = this.translator
                    .convertSimObject(simObject.getRegionPropertyDefinition(regionPropertyId));

            PropertyDefinitionMapInput propertyDefinitionMapInput = PropertyDefinitionMapInput
                    .newBuilder()
                    .setPropertyId(this.translator.getAnyFromObject(regionPropertyId, RegionPropertyId.class))
                    .setPropertyDefinition(propertyDefinitionInput)
                    .build();

            builder.addRegionPropertyDefinitions(propertyDefinitionMapInput);
        }

        for (RegionId regionId : simObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translator.convertSimObject(regionId, RegionId.class);

            for (RegionPropertyId regionPropertyId : simObject.getRegionPropertyValues(regionId).keySet()) {
                PropertyValueMapInput propertyValueMapInput = PropertyValueMapInput
                        .newBuilder()
                        .setPropertyId(this.translator.getAnyFromObject(regionPropertyId, RegionPropertyId.class))
                        .setPropertyValue(this.translator
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
                PersonIdInput personIdInput = this.translator.convertSimObject(personId);
                RegionIdInput regionIdInput = this.translator.convertSimObject(regionId, RegionId.class);
                regionMembershipBuilder.setPersonId(personIdInput).setRegionId(regionIdInput);

                builder.addPersonRegions(regionMembershipBuilder.build());

            }
        }

        TimeTrackingPolicyInput timeTrackingPolicyInput = this.translator
                .convertSimObject(simObject.getPersonRegionArrivalTrackingPolicy());

        builder.setRegionArrivalTimeTrackingPolicy(timeTrackingPolicyInput);

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return RegionsPluginDataInput.getDescriptor();
    }

    @Override
    public RegionsPluginDataInput getDefaultInstanceForInputObject() {
        return RegionsPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<RegionsPluginData> getSimObjectClass() {
        return RegionsPluginData.class;
    }

    @Override
    public Class<RegionsPluginDataInput> getInputObjectClass() {
        return RegionsPluginDataInput.class;
    }

}
