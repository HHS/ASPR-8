package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyReportPluginDataInput;
import plugins.reports.support.ReportLabel;
import plugins.resources.reports.ResourcePropertyReportPluginData;

public class ResourcePropertyReportPluginDataTranslatorSpec
        extends ProtobufTranslationSpec<ResourcePropertyReportPluginDataInput, ResourcePropertyReportPluginData> {

    @Override
    protected ResourcePropertyReportPluginData convertInputObject(ResourcePropertyReportPluginDataInput inputObject) {
        ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected ResourcePropertyReportPluginDataInput convertAppObject(ResourcePropertyReportPluginData appObject) {
        ResourcePropertyReportPluginDataInput.Builder builder = ResourcePropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public Class<ResourcePropertyReportPluginData> getAppObjectClass() {
        return ResourcePropertyReportPluginData.class;
    }

    @Override
    public Class<ResourcePropertyReportPluginDataInput> getInputObjectClass() {
        return ResourcePropertyReportPluginDataInput.class;
    }

}
