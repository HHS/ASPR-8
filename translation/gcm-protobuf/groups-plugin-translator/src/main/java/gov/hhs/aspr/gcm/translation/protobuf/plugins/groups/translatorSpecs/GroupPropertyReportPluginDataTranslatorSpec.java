package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyReportPropertyMap;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.ReportLabel;

public class GroupPropertyReportPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<GroupPropertyReportPluginDataInput, GroupPropertyReportPluginData> {

    @Override
    protected GroupPropertyReportPluginData convertInputObject(GroupPropertyReportPluginDataInput inputObject) {
        GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());
        builder.setReportPeriod(this.translator.convertInputObject(inputObject.getReportPeriod()));

        for(GroupPropertyReportPropertyMap propertyMap : inputObject.getIncludedPropertiesList()) {
            GroupTypeId groupTypeId = this.translator.convertInputObject(propertyMap.getGroupTypeId(), GroupTypeId.class);
            for(GroupPropertyIdInput groupPropertyIdInput : propertyMap.getGroupPropertiesList()) {
                GroupPropertyId groupPropertyId = this.translator.convertInputObject(groupPropertyIdInput, GroupPropertyId.class);

                builder.includeGroupProperty(groupTypeId, groupPropertyId);
            }
        }

        for(GroupPropertyReportPropertyMap propertyMap : inputObject.getExcludedPropertiesList()) {
            GroupTypeId groupTypeId = this.translator.convertInputObject(propertyMap.getGroupTypeId(), GroupTypeId.class);
            for(GroupPropertyIdInput groupPropertyIdInput : propertyMap.getGroupPropertiesList()) {
                GroupPropertyId groupPropertyId = this.translator.convertInputObject(groupPropertyIdInput, GroupPropertyId.class);

                builder.excludeGroupProperty(groupTypeId, groupPropertyId);
            }
        }

        return builder.build();
    }

    @Override
    protected GroupPropertyReportPluginDataInput convertAppObject(GroupPropertyReportPluginData simObject) {
        GroupPropertyReportPluginDataInput.Builder builder = GroupPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput)
                .setReportPeriod(this.translator.convertSimObject(simObject.getReportPeriod()));

        for (GroupTypeId groupTypeId : simObject.getGroupTypeIds()) {
            GroupTypeIdInput groupTypeIdInput = this.translator.convertSimObject(groupTypeId, GroupTypeId.class);

            GroupPropertyReportPropertyMap.Builder groupPropertyReportBuilder = GroupPropertyReportPropertyMap
                    .newBuilder().setGroupTypeId(groupTypeIdInput);
            for (GroupPropertyId groupPropertyId : simObject.getIncludedProperties(groupTypeId)) {
                GroupPropertyIdInput groupPropertyIdInput = this.translator.convertSimObject(groupPropertyId,
                        GroupPropertyId.class);
                groupPropertyReportBuilder.addGroupProperties(groupPropertyIdInput);
            }
            builder.addIncludedProperties(groupPropertyReportBuilder.build());

            groupPropertyReportBuilder = GroupPropertyReportPropertyMap
                    .newBuilder().setGroupTypeId(groupTypeIdInput);
            for (GroupPropertyId groupPropertyId : simObject.getExcludedProperties(groupTypeId)) {
                GroupPropertyIdInput groupPropertyIdInput = this.translator.convertSimObject(groupPropertyId,
                        GroupPropertyId.class);
                groupPropertyReportBuilder.addGroupProperties(groupPropertyIdInput);
            }
            builder.addExcludedProperties(groupPropertyReportBuilder.build());
        }

        return builder.build();
    }

    @Override
    public Class<GroupPropertyReportPluginData> getAppObjectClass() {
        return GroupPropertyReportPluginData.class;
    }

    @Override
    public Class<GroupPropertyReportPluginDataInput> getInputObjectClass() {
        return GroupPropertyReportPluginDataInput.class;
    }

}
