package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.Int64Value;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Long to a
 * Protobuf {@link Int64Value} type and vice versa
 */
public class LongTranslationSpec extends ProtobufTranslationSpec<Int64Value, Long> {

    @Override
    protected Long convertInputObject(Int64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int64Value convertAppObject(Long appObject) {
        return Int64Value.of(appObject);
    }

    @Override
    public Class<Long> getAppObjectClass() {
        return Long.class;
    }

    @Override
    public Class<Int64Value> getInputObjectClass() {
        return Int64Value.class;
    }
}
