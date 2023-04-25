package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.SimpleReportLabelInput;
import plugins.reports.support.SimpleReportLabel;

public class SimpleReportLabelTranslationSpec extends ProtobufTranslationSpec<SimpleReportLabelInput, SimpleReportLabel>{

    @Override
    protected SimpleReportLabel convertInputObject(SimpleReportLabelInput inputObject) {
        return new SimpleReportLabel(this.translatorCore.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleReportLabelInput convertAppObject(SimpleReportLabel appObject) {
       return SimpleReportLabelInput.newBuilder().setValue(this.translatorCore.getAnyFromObject(appObject.getValue())).build();
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
