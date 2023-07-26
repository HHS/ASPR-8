package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ReportLabelInput} and
 * {@linkplain ReportLabel}
 */
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
