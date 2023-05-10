package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportLabel;

public class GlobalPropertyReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<GlobalPropertyReportPluginDataInput, GlobalPropertyReportPluginData> {

    @Override
    protected GlobalPropertyReportPluginData convertInputObject(GlobalPropertyReportPluginDataInput inputObject) {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (GlobalPropertyIdInput globalPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            GlobalPropertyId globalPropertyId = this.translationEngine.convertObject(globalPropertyIdInput);
            builder.includeGlobalProperty(globalPropertyId);
        }

        for (GlobalPropertyIdInput globalPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            GlobalPropertyId globalPropertyId = this.translationEngine.convertObject(globalPropertyIdInput);
            builder.excludeGlobalProperty(globalPropertyId);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertyReportPluginDataInput convertAppObject(GlobalPropertyReportPluginData appObject) {
        GlobalPropertyReportPluginDataInput.Builder builder = GlobalPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (GlobalPropertyId globalPropertyId : appObject.getIncludedProperties()) {
            GlobalPropertyIdInput globalPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                    globalPropertyId,
                    GlobalPropertyId.class);
            builder.addIncludedProperties(globalPropertyIdInput);
        }

        for (GlobalPropertyId globalPropertyId : appObject.getExcludedProperties()) {
            GlobalPropertyIdInput globalPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                    globalPropertyId,
                    GlobalPropertyId.class);
            builder.addExcludedProperties(globalPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public Class<GlobalPropertyReportPluginData> getAppObjectClass() {
        return GlobalPropertyReportPluginData.class;
    }

    @Override
    public Class<GlobalPropertyReportPluginDataInput> getInputObjectClass() {
        return GlobalPropertyReportPluginDataInput.class;
    }

}
