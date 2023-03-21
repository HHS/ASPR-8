package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import com.google.protobuf.FloatValue;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;

public class FloatTranslatorSpec extends AObjectTranslatorSpec<FloatValue, Float> {

    @Override
    protected Float convertInputObject(FloatValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected FloatValue convertAppObject(Float simObject) {
        return FloatValue.of(simObject);
    }

    @Override
    public FloatValue getDefaultInstanceForInputObject() {
        return FloatValue.getDefaultInstance();
    }

    @Override
    public Class<Float> getAppObjectClass() {
        return Float.class;
    }

    @Override
    public Class<FloatValue> getInputObjectClass() {
        return FloatValue.class;
    }
}
