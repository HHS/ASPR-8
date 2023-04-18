package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class UInt64TranslatorSpec extends AbstractProtobufTranslatorSpec<UInt64Value, Long> {

    @Override
    protected Long convertInputObject(UInt64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt64Value convertAppObject(Long simObject) {
        return UInt64Value.of(simObject);
    }

    @Override
    public UInt64Value getDefaultInstanceForInputObject() {
        return UInt64Value.getDefaultInstance();
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
