package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.reports.input.BatchStatusReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain BatchStatusReportPluginDataInput} and
 * {@linkplain BatchStatusReportPluginData}
 */
public class BatchStatusReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<BatchStatusReportPluginDataInput, BatchStatusReportPluginData> {

    @Override
    protected BatchStatusReportPluginData convertInputObject(BatchStatusReportPluginDataInput inputObject) {
        BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected BatchStatusReportPluginDataInput convertAppObject(BatchStatusReportPluginData appObject) {
        BatchStatusReportPluginDataInput.Builder builder = BatchStatusReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
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
