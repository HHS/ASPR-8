package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Integer to a
 * Protobuf {@link UInt32Value} type and vice versa
 */
public class UIntegerTranslationSpec extends ProtobufTranslationSpec<UInt32Value, Integer> {

    @Override
    protected Integer convertInputObject(UInt32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt32Value convertAppObject(Integer appObject) {
        return UInt32Value.of(appObject);
    }

    @Override
    public Class<Integer> getAppObjectClass() {
        return Integer.class;
    }

    @Override
    public Class<UInt32Value> getInputObjectClass() {
        return UInt32Value.class;
    }
}