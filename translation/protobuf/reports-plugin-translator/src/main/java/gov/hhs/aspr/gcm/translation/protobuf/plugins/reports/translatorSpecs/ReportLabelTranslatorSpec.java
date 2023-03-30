package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.reports.support.ReportLabel;

public class ReportLabelTranslatorSpec extends AbstractTranslatorSpec<ReportLabelInput, ReportLabel> {

    @Override
    protected ReportLabel convertInputObject(ReportLabelInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getLabel(), getAppObjectClass());
    }

    @Override
    protected ReportLabelInput convertAppObject(ReportLabel simObject) {
        return ReportLabelInput.newBuilder()
                .setLabel(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public ReportLabelInput getDefaultInstanceForInputObject() {
        return ReportLabelInput.getDefaultInstance();
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
