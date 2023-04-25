package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class UInt64TranslationSpec extends ProtobufTranslationSpec<UInt64Value, Long> {

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
