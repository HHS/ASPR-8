package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.reports.support.ReportLabel;

public class ReportLabelTranslationSpec extends ProtobufTranslationSpec<ReportLabelInput, ReportLabel> {

    @Override
    protected ReportLabel convertInputObject(ReportLabelInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getLabel());
    }

    @Override
    protected ReportLabelInput convertAppObject(ReportLabel appObject) {
        return ReportLabelInput.newBuilder()
                .setLabel(this.translationEngine.getAnyFromObject(appObject)).build();
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
