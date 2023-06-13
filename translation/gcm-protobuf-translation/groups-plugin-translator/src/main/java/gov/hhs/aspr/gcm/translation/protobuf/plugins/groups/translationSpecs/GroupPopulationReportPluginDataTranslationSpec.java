package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPopulationReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.reports.GroupPopulationReportPluginData;
import plugins.reports.support.ReportLabel;

public class GroupPopulationReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<GroupPopulationReportPluginDataInput, GroupPopulationReportPluginData> {

    @Override
    protected GroupPopulationReportPluginData convertInputObject(GroupPopulationReportPluginDataInput inputObject) {
        return GroupPopulationReportPluginData.builder()
                .setReportLabel(this.translationEngine.convertObject(inputObject.getReportLabel()))
                .setReportPeriod(this.translationEngine.convertObject(inputObject.getReportPeriod()))
                .build();
    }

    @Override
    protected GroupPopulationReportPluginDataInput convertAppObject(GroupPopulationReportPluginData appObject) {
        return GroupPopulationReportPluginDataInput.newBuilder()
                .setReportLabel((ReportLabelInput) this.translationEngine
                        .convertObjectAsSafeClass(appObject.getReportLabel(), ReportLabel.class))
                .setReportPeriod(this.translationEngine.convertObject(appObject.getReportPeriod()))
                .build();
    }

    @Override
    public Class<GroupPopulationReportPluginData> getAppObjectClass() {
        return GroupPopulationReportPluginData.class;
    }

    @Override
    public Class<GroupPopulationReportPluginDataInput> getInputObjectClass() {
        return GroupPopulationReportPluginDataInput.class;
    }

}
