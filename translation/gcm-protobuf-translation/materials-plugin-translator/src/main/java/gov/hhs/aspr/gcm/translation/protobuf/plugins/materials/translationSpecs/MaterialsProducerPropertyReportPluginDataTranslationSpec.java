package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.reports.support.ReportLabel;

public class MaterialsProducerPropertyReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<MaterialsProducerPropertyReportPluginDataInput, MaterialsProducerPropertyReportPluginData> {

    @Override
    protected MaterialsProducerPropertyReportPluginData convertInputObject(MaterialsProducerPropertyReportPluginDataInput inputObject) {
        MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translationEnine.convertObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected MaterialsProducerPropertyReportPluginDataInput convertAppObject(MaterialsProducerPropertyReportPluginData appObject) {
        MaterialsProducerPropertyReportPluginDataInput.Builder builder = MaterialsProducerPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEnine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public Class<MaterialsProducerPropertyReportPluginData> getAppObjectClass() {
        return MaterialsProducerPropertyReportPluginData.class;
    }

    @Override
    public Class<MaterialsProducerPropertyReportPluginDataInput> getInputObjectClass() {
        return MaterialsProducerPropertyReportPluginDataInput.class;
    }

}
