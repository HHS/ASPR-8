package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class UInt32TranslatorSpec extends AbstractProtobufTranslatorSpec<UInt32Value, Integer> {

    @Override
    protected Integer convertInputObject(UInt32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt32Value convertAppObject(Integer simObject) {
        return UInt32Value.of(simObject);
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