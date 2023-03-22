package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.SimpleGroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.plugins.groups.simobjects.SimpleGroupTypeId;

public class SimpleGroupTypeIdTranslatorSpec extends AbstractTranslatorSpec<SimpleGroupTypeIdInput, SimpleGroupTypeId> {

    @Override
    protected SimpleGroupTypeId convertInputObject(SimpleGroupTypeIdInput inputObject) {
        return new SimpleGroupTypeId(inputObject.getValue());
    }

    @Override
    protected SimpleGroupTypeIdInput convertAppObject(SimpleGroupTypeId simObject) {
        return SimpleGroupTypeIdInput.newBuilder().setValue(simObject.getValue().toString()).build();
    }

    @Override
    public SimpleGroupTypeIdInput getDefaultInstanceForInputObject() {
        return SimpleGroupTypeIdInput.getDefaultInstance();
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
