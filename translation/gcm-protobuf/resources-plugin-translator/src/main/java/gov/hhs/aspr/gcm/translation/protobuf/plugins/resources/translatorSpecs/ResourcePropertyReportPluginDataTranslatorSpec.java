package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourcePropertyReportPluginDataInput;
import plugins.reports.support.ReportLabel;
import plugins.resources.reports.ResourcePropertyReportPluginData;

public class ResourcePropertyReportPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<ResourcePropertyReportPluginDataInput, ResourcePropertyReportPluginData> {

    @Override
    protected ResourcePropertyReportPluginData convertInputObject(ResourcePropertyReportPluginDataInput inputObject) {
        ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected ResourcePropertyReportPluginDataInput convertAppObject(ResourcePropertyReportPluginData simObject) {
        ResourcePropertyReportPluginDataInput.Builder builder = ResourcePropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(simObject.getReportLabel(),
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
