package gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.DoubleValue;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;

public class DoubleTranslatorSpec extends AbstractTranslatorSpec<DoubleValue, Double> {

    @Override
    protected Double convertInputObject(DoubleValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected DoubleValue convertAppObject(Double simObject) {
        return DoubleValue.of(simObject);
    }

    @Override
    public DoubleValue getDefaultInstanceForInputObject() {
        return DoubleValue.getDefaultInstance();
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
