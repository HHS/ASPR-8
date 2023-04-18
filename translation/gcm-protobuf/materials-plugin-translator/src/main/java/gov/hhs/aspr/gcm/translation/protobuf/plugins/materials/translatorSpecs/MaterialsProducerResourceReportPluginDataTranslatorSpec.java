package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.reports.support.ReportLabel;

public class MaterialsProducerResourceReportPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<MaterialsProducerResourceReportPluginDataInput, MaterialsProducerResourceReportPluginData> {

    @Override
    protected MaterialsProducerResourceReportPluginData convertInputObject(MaterialsProducerResourceReportPluginDataInput inputObject) {
        MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel());

        builder.setReportLabel(reportLabel);
        return builder.build();
    }

    @Override
    protected MaterialsProducerResourceReportPluginDataInput convertAppObject(MaterialsProducerResourceReportPluginData simObject) {
        MaterialsProducerResourceReportPluginDataInput.Builder builder = MaterialsProducerResourceReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
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
