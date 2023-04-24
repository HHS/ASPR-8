package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class DoubleTranslatorSpec extends ProtobufTranslatorSpec<DoubleValue, Double> {

    @Override
    protected Double convertInputObject(DoubleValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected DoubleValue convertAppObject(Double appObject) {
        return DoubleValue.of(appObject);
    }

    @Override
    public Class<Double> getAppObjectClass() {
        return Double.class;
    }

    @Override
    public Class<DoubleValue> getInputObjectClass() {
        return DoubleValue.class;
    }
}
