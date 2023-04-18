package gov.hhs.aspr.translation.protobuf.core.translatorSpecs;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;

public class BooleanTranslatorSpec extends AbstractProtobufTranslatorSpec<BoolValue, Boolean> {

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