package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.materials.reports.input.MaterialsProducerResourceReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain MaterialsProducerResourceReportPluginDataInput} and
 * {@linkplain MaterialsProducerResourceReportPluginData}
 */
public class MaterialsProducerResourceReportPluginDataTranslationSpec
        extends
        ProtobufTranslationSpec<MaterialsProducerResourceReportPluginDataInput, MaterialsProducerResourceReportPluginData> {

    @Override
    protected MaterialsProducerResourceReportPluginData convertInputObject(
            MaterialsProducerResourceReportPluginDataInput inputObject) {
        MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected MaterialsProducerResourceReportPluginDataInput convertAppObject(
            MaterialsProducerResourceReportPluginData appObject) {
        MaterialsProducerResourceReportPluginDataInput.Builder builder = MaterialsProducerResourceReportPluginDataInput
                .newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public Class<MaterialsProducerResourceReportPluginData> getAppObjectClass() {
        return MaterialsProducerResourceReportPluginData.class;
    }

    @Override
    public Class<MaterialsProducerResourceReportPluginDataInput> getInputObjectClass() {
        return MaterialsProducerResourceReportPluginDataInput.class;
    }

}
