package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyDefinitionMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.input.PropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionMembershipInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionMembershipInput.RegionPersonInfo;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyValueMapInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionsPluginDataInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyDefinition;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionsPluginDataInput} and
 * {@linkplain RegionsPluginData}
 */
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
        boolean trackRegionArrivalTimes = inputObject.getTrackRegionArrivalTimes();
        builder.setPersonRegionArrivalTracking(trackRegionArrivalTimes);

        for (RegionMembershipInput regionMembershipInput : inputObject.getPersonRegionsList()) {
            RegionId regionId = this.translationEngine.convertObject(regionMembershipInput.getRegionId());

            for (RegionPersonInfo regionPersonInfo : regionMembershipInput.getPeopleList()) {
                PersonId personId = new PersonId(regionPersonInfo.getPersonId());
                if (trackRegionArrivalTimes) {
                    builder.addPerson(personId, regionId, regionPersonInfo.getArrivalTime());
                } else {
                    builder.addPerson(personId, regionId);
                }
            }
        }

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

        boolean trackRegionArrivalTimes = appObject.getPersonRegionArrivalTrackingPolicy();

        builder.setTrackRegionArrivalTimes(trackRegionArrivalTimes);

        Map<RegionIdInput, List<RegionPersonInfo>> regionMembershipMap = new LinkedHashMap<>();
        for (int i = 0; i < appObject.getPersonCount(); i++) {
            PersonId personId = new PersonId(i);

            RegionId regionId = appObject.getPersonRegion(personId).get();
            RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId, RegionId.class);
            List<RegionPersonInfo> peopleInRegion = regionMembershipMap.get(regionIdInput);

            if (peopleInRegion == null) {
                peopleInRegion = new ArrayList<>();

                regionMembershipMap.put(regionIdInput, peopleInRegion);
            }

            RegionPersonInfo.Builder regionPersonInfoBuilder = RegionPersonInfo.newBuilder()
                    .setPersonId(i);
            if (trackRegionArrivalTimes) {
                // can safely assume this because the person region exists
                regionPersonInfoBuilder.setArrivalTime(appObject.getPersonRegionArrivalTime(personId).get());
            }

            peopleInRegion.add(regionPersonInfoBuilder.build());
        }

        for (RegionIdInput regionIdInput : regionMembershipMap.keySet()) {
            RegionMembershipInput.Builder regionMembershipBuilder = RegionMembershipInput.newBuilder();

            regionMembershipBuilder.setRegionId(regionIdInput).addAllPeople(regionMembershipMap.get(regionIdInput));

            builder.addPersonRegions(regionMembershipBuilder.build());
        }

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
