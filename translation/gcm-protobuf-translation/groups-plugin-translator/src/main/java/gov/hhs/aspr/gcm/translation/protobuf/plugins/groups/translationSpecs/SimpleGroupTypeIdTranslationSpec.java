package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.SimpleGroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.simobjects.SimpleGroupTypeId;

public class SimpleGroupTypeIdTranslationSpec
        extends ProtobufTranslationSpec<SimpleGroupTypeIdInput, SimpleGroupTypeId> {

    @Override
    protected SimpleGroupTypeId convertInputObject(SimpleGroupTypeIdInput inputObject) {
        return new SimpleGroupTypeId(inputObject.getValue());
    }

    @Override
    protected SimpleGroupTypeIdInput convertAppObject(SimpleGroupTypeId appObject) {
        return SimpleGroupTypeIdInput.newBuilder().setValue(appObject.getValue().toString()).build();
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
