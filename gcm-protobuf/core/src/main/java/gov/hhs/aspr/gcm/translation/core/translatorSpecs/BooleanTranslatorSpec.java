package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;

public class BooleanTranslatorSpec extends AbstractTranslatorSpec<BoolValue, Boolean> {

    @Override
    protected Boolean convertInputObject(BoolValue inputObject) {
        return inputObject.getValue();
    }

    @Override
    protected BoolValue convertAppObject(Boolean simObject) {
        return BoolValue.of(simObject);
    }

    @Override
    public BoolValue getDefaultInstanceForInputObject() {
        return BoolValue.getDefaultInstance();
    }

    @Override
    public Class<Boolean> getAppObjectClass() {
        return Boolean.class;
    }

    @Override
    public Class<BoolValue> getInputObjectClass() {
        return BoolValue.class;
    }
}