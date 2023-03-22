package gov.hhs.aspr.gcm.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;

public class UInt32TranslatorSpec extends AbstractTranslatorSpec<UInt32Value, Integer> {

    @Override
    protected Integer convertInputObject(UInt32Value inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected UInt32Value convertAppObject(Integer simObject) {
        return UInt32Value.of(simObject);
    }

    @Override
    public UInt32Value getDefaultInstanceForInputObject() {
        return UInt32Value.getDefaultInstance();
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