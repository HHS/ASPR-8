package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class DoubleTranslatorSpec extends AbstractProtobufTranslatorSpec<DoubleValue, Double> {

    @Override
    protected Double convertInputObject(DoubleValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected DoubleValue convertAppObject(Double simObject) {
        return DoubleValue.of(simObject);
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
