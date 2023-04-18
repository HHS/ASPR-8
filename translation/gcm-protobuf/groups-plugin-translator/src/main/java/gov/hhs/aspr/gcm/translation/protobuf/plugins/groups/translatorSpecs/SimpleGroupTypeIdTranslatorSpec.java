package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.SimpleGroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.simobjects.SimpleGroupTypeId;

public class SimpleGroupTypeIdTranslatorSpec extends AbstractProtobufTranslatorSpec<SimpleGroupTypeIdInput, SimpleGroupTypeId> {

    @Override
    protected SimpleGroupTypeId convertInputObject(SimpleGroupTypeIdInput inputObject) {
        return new SimpleGroupTypeId(inputObject.getValue());
    }

    @Override
    protected SimpleGroupTypeIdInput convertAppObject(SimpleGroupTypeId simObject) {
        return SimpleGroupTypeIdInput.newBuilder().setValue(simObject.getValue().toString()).build();
    }

    @Override
    public Class<SimpleGroupTypeId> getAppObjectClass() {
        return SimpleGroupTypeId.class;
    }

    @Override
    public Class<SimpleGroupTypeIdInput> getInputObjectClass() {
        return SimpleGroupTypeIdInput.class;
    }

}
