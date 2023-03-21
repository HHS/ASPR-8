package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import com.google.protobuf.UInt32Value;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;

public class UInt32TranslatorSpec extends AObjectTranslatorSpec<UInt32Value, Integer> {

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