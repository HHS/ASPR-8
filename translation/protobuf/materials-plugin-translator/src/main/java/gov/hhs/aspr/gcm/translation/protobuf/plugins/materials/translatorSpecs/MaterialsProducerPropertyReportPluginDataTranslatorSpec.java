package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.reports.support.ReportLabel;

public class MaterialsProducerPropertyReportPluginDataTranslatorSpec
        extends AbstractTranslatorSpec<MaterialsProducerPropertyReportPluginDataInput, MaterialsProducerPropertyReportPluginData> {

    @Override
    protected MaterialsProducerPropertyReportPluginData convertInputObject(MaterialsProducerPropertyReportPluginDataInput inputObject) {
        MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected MaterialsProducerPropertyReportPluginDataInput convertAppObject(MaterialsProducerPropertyReportPluginData simObject) {
        MaterialsProducerPropertyReportPluginDataInput.Builder builder = MaterialsProducerPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);

        builder.setReportLabel(reportLabelInput);
        return builder.build();
    }

    @Override
    public MaterialsProducerPropertyReportPluginDataInput getDefaultInstanceForInputObject() {
        return MaterialsProducerPropertyReportPluginDataInput.getDefaultInstance();
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
