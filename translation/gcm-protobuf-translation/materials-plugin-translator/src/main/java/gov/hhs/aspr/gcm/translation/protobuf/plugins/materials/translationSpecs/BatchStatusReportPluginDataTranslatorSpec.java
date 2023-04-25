package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchStatusReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.reports.support.ReportLabel;

public class BatchStatusReportPluginDataTranslatorSpec
        extends ProtobufTranslationSpec<BatchStatusReportPluginDataInput, BatchStatusReportPluginData> {

    @Override
    protected BatchStatusReportPluginData convertInputObject(BatchStatusReportPluginDataInput inputObject) {
        BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected BatchStatusReportPluginDataInput convertAppObject(BatchStatusReportPluginData appObject) {
        BatchStatusReportPluginDataInput.Builder builder = BatchStatusReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public Class<BatchStatusReportPluginData> getAppObjectClass() {
        return BatchStatusReportPluginData.class;
    }

    @Override
    public Class<BatchStatusReportPluginDataInput> getInputObjectClass() {
        return BatchStatusReportPluginDataInput.class;
    }

}
