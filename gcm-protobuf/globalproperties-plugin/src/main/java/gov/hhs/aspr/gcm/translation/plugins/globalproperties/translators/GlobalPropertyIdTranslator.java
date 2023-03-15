package gov.hhs.aspr.gcm.translation.plugins.globalproperties.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.GlobalPropertyIdInput;
import plugins.globalproperties.support.GlobalPropertyId;

public class GlobalPropertyIdTranslator extends AObjectTranslatorSpec<GlobalPropertyIdInput, GlobalPropertyId> {

    @Override
    protected GlobalPropertyId convertInputObject(GlobalPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected GlobalPropertyIdInput convertSimObject(GlobalPropertyId simObject) {
        return GlobalPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject))
                .build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GlobalPropertyIdInput.getDescriptor();
    }

    @Override
    public GlobalPropertyIdInput getDefaultInstanceForInputObject() {
        return GlobalPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<GlobalPropertyId> getSimObjectClass() {
        return GlobalPropertyId.class;
    }

    @Override
    public Class<GlobalPropertyIdInput> getInputObjectClass() {
        return GlobalPropertyIdInput.class;
    }

}
