package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportLabel;

public class GlobalPropertyReportPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<GlobalPropertyReportPluginDataInput, GlobalPropertyReportPluginData> {

    @Override
    protected GlobalPropertyReportPluginData convertInputObject(GlobalPropertyReportPluginDataInput inputObject) {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (GlobalPropertyIdInput globalPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            GlobalPropertyId globalPropertyId = this.translatorCore.convertObject(globalPropertyIdInput);
            builder.includeGlobalProperty(globalPropertyId);
        }

        for (GlobalPropertyIdInput globalPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            GlobalPropertyId globalPropertyId = this.translatorCore.convertObject(globalPropertyIdInput);
            builder.excludeGlobalProperty(globalPropertyId);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertyReportPluginDataInput convertAppObject(GlobalPropertyReportPluginData simObject) {
        GlobalPropertyReportPluginDataInput.Builder builder = GlobalPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(simObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (GlobalPropertyId globalPropertyId : simObject.getIncludedProperties()) {
            GlobalPropertyIdInput globalPropertyIdInput = this.translatorCore.convertObjectAsSafeClass(globalPropertyId,
                    GlobalPropertyId.class);
            builder.addIncludedProperties(globalPropertyIdInput);
        }

        for (GlobalPropertyId globalPropertyId : simObject.getExcludedProperties()) {
            GlobalPropertyIdInput globalPropertyIdInput = this.translatorCore.convertObjectAsSafeClass(globalPropertyId,
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
