package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.SimpleReportLabelInput;
import plugins.reports.support.SimpleReportLabel;

public class SimpleReportLabelTranslatorSpec extends ProtobufTranslatorSpec<SimpleReportLabelInput, SimpleReportLabel>{

    @Override
    protected SimpleReportLabel convertInputObject(SimpleReportLabelInput inputObject) {
        return new SimpleReportLabel(this.translatorCore.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleReportLabelInput convertAppObject(SimpleReportLabel simObject) {
       return SimpleReportLabelInput.newBuilder().setValue(this.translatorCore.getAnyFromObject(simObject.getValue())).build();
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
