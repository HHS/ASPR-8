package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.Int32Value;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Integer to a
 * Protobuf {@link Int32Value} type and vice versa
 */
public class IntegerTranslationSpec extends ProtobufTranslationSpec<Int32Value, Integer> {

    @Override
    protected Integer convertInputObject(Int32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int32Value convertAppObject(Integer appObject) {
        return Int32Value.of(appObject);
    }

    @Override
    public Class<Integer> getAppObjectClass() {
        return Integer.class;
    }

    @Override
    public Class<Int32Value> getInputObjectClass() {
        return Int32Value.class;
    }
}
