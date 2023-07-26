package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.FloatValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Float to a
 * Protobuf {@link FloatValue} type and vice versa
 */
public class FloatTranslationSpec extends ProtobufTranslationSpec<FloatValue, Float> {

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
