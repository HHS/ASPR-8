package gov.hhs.aspr.gcm.translation.plugins.materials.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.StageIdInput;
import plugins.materials.support.StageId;


public class StageIdTranslator extends Translator<StageIdInput, StageId> {

    @Override
    protected StageId convertInputObject(StageIdInput inputObject) {
        return new StageId(inputObject.getId());
    }

    @Override
    protected StageIdInput convertSimObject(StageId simObject) {
        return StageIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return StageIdInput.getDescriptor();
    }

    @Override
    public StageIdInput getDefaultInstanceForInputObject() {
        return StageIdInput.getDefaultInstance();
    }

    @Override
    public Class<StageId> getSimObjectClass() {
        return StageId.class;
    }

    @Override
    public Class<StageIdInput> getInputObjectClass() {
        return StageIdInput.class;
    }

}
