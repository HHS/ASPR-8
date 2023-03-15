package gov.hhs.aspr.gcm.gcmprotobuf.plugins.materials.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.materials.input.StageIdInput;
import plugins.materials.support.StageId;


public class StageIdTranslator extends AbstractTranslator<StageIdInput, StageId> {

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