package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.FloatValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class FloatTranslatorSpec extends ProtobufTranslatorSpec<FloatValue, Float> {

    @Override
    protected Float convertInputObject(FloatValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected FloatValue convertAppObject(Float appObject) {
        return FloatValue.of(appObject);
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
