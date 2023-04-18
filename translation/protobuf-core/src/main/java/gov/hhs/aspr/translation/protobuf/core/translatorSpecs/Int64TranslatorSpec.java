package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.Int64Value;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class Int64TranslatorSpec extends AbstractProtobufTranslatorSpec<Int64Value, Long> {

    @Override
    protected Long convertInputObject(Int64Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected Int64Value convertAppObject(Long simObject) {
        return Int64Value.of(simObject);
    }

    @Override
    public Int64Value getDefaultInstanceForInputObject() {
        return Int64Value.getDefaultInstance();
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