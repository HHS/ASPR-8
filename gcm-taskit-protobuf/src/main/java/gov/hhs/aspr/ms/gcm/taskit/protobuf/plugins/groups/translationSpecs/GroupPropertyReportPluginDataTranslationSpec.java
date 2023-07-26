package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.reports.input.GroupPropertyReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.reports.input.GroupPropertyReportPropertyMap;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypeIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import plugins.groups.reports.GroupPropertyReportPluginData;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GroupPropertyReportPluginDataInput} and
 * {@linkplain GroupPropertyReportPluginData}
 */
public class GroupPropertyReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<GroupPropertyReportPluginDataInput, GroupPropertyReportPluginData> {

    @Override
    protected GroupPropertyReportPluginData convertInputObject(GroupPropertyReportPluginDataInput inputObject) {
        GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());
        builder.setReportPeriod(this.translationEngine.convertObject(inputObject.getReportPeriod()));

        for (GroupPropertyReportPropertyMap propertyMap : inputObject.getIncludedPropertiesList()) {
            GroupTypeId groupTypeId = this.translationEngine.convertObject(propertyMap.getGroupTypeId());
            for (GroupPropertyIdInput groupPropertyIdInput : propertyMap.getGroupPropertiesList()) {
                GroupPropertyId groupPropertyId = this.translationEngine.convertObject(groupPropertyIdInput);

                builder.includeGroupProperty(groupTypeId, groupPropertyId);
            }
        }

        for (GroupPropertyReportPropertyMap propertyMap : inputObject.getExcludedPropertiesList()) {
            GroupTypeId groupTypeId = this.translationEngine.convertObject(propertyMap.getGroupTypeId());
            for (GroupPropertyIdInput groupPropertyIdInput : propertyMap.getGroupPropertiesList()) {
                GroupPropertyId groupPropertyId = this.translationEngine.convertObject(groupPropertyIdInput);

                builder.excludeGroupProperty(groupTypeId, groupPropertyId);
            }
        }

        return builder.build();
    }

    @Override
    protected GroupPropertyReportPluginDataInput convertAppObject(GroupPropertyReportPluginData appObject) {
        GroupPropertyReportPluginDataInput.Builder builder = GroupPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput)
                .setReportPeriod(this.translationEngine.convertObject(appObject.getReportPeriod()));

        for (GroupTypeId groupTypeId : appObject.getGroupTypeIds()) {
            GroupTypeIdInput groupTypeIdInput = this.translationEngine.convertObjectAsSafeClass(groupTypeId,
                    GroupTypeId.class);

            GroupPropertyReportPropertyMap.Builder groupPropertyReportBuilder = GroupPropertyReportPropertyMap
                    .newBuilder().setGroupTypeId(groupTypeIdInput);
            for (GroupPropertyId groupPropertyId : appObject.getIncludedProperties(groupTypeId)) {
                GroupPropertyIdInput groupPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                        groupPropertyId,
                        GroupPropertyId.class);
                groupPropertyReportBuilder.addGroupProperties(groupPropertyIdInput);
            }
            builder.addIncludedProperties(groupPropertyReportBuilder.build());

            groupPropertyReportBuilder = GroupPropertyReportPropertyMap
                    .newBuilder().setGroupTypeId(groupTypeIdInput);
            for (GroupPropertyId groupPropertyId : appObject.getExcludedProperties(groupTypeId)) {
                GroupPropertyIdInput groupPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                        groupPropertyId,
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
