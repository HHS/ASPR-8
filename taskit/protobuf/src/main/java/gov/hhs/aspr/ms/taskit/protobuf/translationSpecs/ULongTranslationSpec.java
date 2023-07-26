package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;

/**
 * TranslationSpec that defines how to convert from any Java Long to a
 * Protobuf {@link UInt64Value} type and vice versa
 */
public class ULongTranslationSpec extends ProtobufTranslationSpec<UInt64Value, Long> {

    @Override
    protected Long convertInputObject(UInt64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt64Value convertAppObject(Long appObject) {
        return UInt64Value.of(appObject);
    }

    @Override
    public Class<Long> getAppObjectClass() {
        return Long.class;
    }

    @Override
    public Class<UInt64Value> getInputObjectClass() {
        return UInt64Value.class;
    }
}
