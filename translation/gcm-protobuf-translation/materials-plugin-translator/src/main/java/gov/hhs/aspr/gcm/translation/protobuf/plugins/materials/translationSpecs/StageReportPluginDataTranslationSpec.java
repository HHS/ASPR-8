package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.materials.reports.StageReportPluginData;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain StageReportPluginDataInput} and
 * {@linkplain StageReportPluginData}
 */
public class StageReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<StageReportPluginDataInput, StageReportPluginData> {

    @Override
    protected StageReportPluginData convertInputObject(StageReportPluginDataInput inputObject) {
        StageReportPluginData.Builder builder = StageReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected StageReportPluginDataInput convertAppObject(StageReportPluginData appObject) {
        StageReportPluginDataInput.Builder builder = StageReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public Class<StageReportPluginData> getAppObjectClass() {
        return StageReportPluginData.class;
    }

    @Override
    public Class<StageReportPluginDataInput> getInputObjectClass() {
        return StageReportPluginDataInput.class;
    }

}
