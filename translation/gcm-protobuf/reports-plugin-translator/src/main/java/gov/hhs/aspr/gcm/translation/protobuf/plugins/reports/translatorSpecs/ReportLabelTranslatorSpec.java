package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.reports.support.ReportLabel;

public class ReportLabelTranslatorSpec extends ProtobufTranslatorSpec<ReportLabelInput, ReportLabel> {

    @Override
    protected ReportLabel convertInputObject(ReportLabelInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getLabel());
    }

    @Override
    protected ReportLabelInput convertAppObject(ReportLabel simObject) {
        return ReportLabelInput.newBuilder()
                .setLabel(this.translatorCore.getAnyFromObject(simObject)).build();
    }

    @Override
    public Class<ReportLabel> getAppObjectClass() {
        return ReportLabel.class;
    }

    @Override
    public Class<ReportLabelInput> getInputObjectClass() {
        return ReportLabelInput.class;
    }

}
