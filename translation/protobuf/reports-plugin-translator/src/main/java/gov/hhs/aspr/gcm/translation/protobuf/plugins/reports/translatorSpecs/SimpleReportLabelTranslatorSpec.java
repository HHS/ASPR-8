package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.SimpleReportLabelInput;
import plugins.reports.support.SimpleReportLabel;

public class SimpleReportLabelTranslatorSpec extends AbstractTranslatorSpec<SimpleReportLabelInput, SimpleReportLabel>{

    @Override
    protected SimpleReportLabel convertInputObject(SimpleReportLabelInput inputObject) {
        return new SimpleReportLabel(this.translator.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleReportLabelInput convertAppObject(SimpleReportLabel simObject) {
       return SimpleReportLabelInput.newBuilder().setValue(this.translator.getAnyFromObject(simObject.getValue())).build();
    }

    @Override
    public SimpleReportLabelInput getDefaultInstanceForInputObject() {
       return SimpleReportLabelInput.getDefaultInstance();
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
