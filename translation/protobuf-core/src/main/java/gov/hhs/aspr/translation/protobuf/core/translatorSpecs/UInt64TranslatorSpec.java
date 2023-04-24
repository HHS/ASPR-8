package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;

public class UInt64TranslatorSpec extends ProtobufTranslatorSpec<UInt64Value, Long> {

    @Override
    protected Long convertInputObject(UInt64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt64Value convertAppObject(Long simObject) {
        return UInt64Value.of(simObject);
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
