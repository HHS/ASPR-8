package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.SimpleReportLabelInput;
import plugins.reports.support.SimpleReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain SimpleReportLabelInput} and
 * {@linkplain SimpleReportLabel}
 */
public class SimpleReportLabelTranslationSpec
        extends ProtobufTranslationSpec<SimpleReportLabelInput, SimpleReportLabel> {

    @Override
    protected SimpleReportLabel convertInputObject(SimpleReportLabelInput inputObject) {
        return new SimpleReportLabel(this.translationEngine.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleReportLabelInput convertAppObject(SimpleReportLabel appObject) {
        return SimpleReportLabelInput.newBuilder()
                .setValue(this.translationEngine.getAnyFromObject(appObject.getValue())).build();
    }

    @Override
    public Class<SimpleReportLabel> getAppObjectClass() {
        return SimpleReportLabel.class;
    }

    @Override
    public Class<SimpleReportLabelInput> getInputObjectClass() {
        return SimpleReportLabelInput.class;
    }

}
