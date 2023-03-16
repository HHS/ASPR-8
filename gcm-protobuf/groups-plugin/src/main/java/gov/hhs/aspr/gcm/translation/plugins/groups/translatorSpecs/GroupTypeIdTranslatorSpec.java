package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupTypeIdInput;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslatorSpec extends AObjectTranslatorSpec<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), GroupTypeId.class);
    }

    @Override
    protected GroupTypeIdInput convertSimObject(GroupTypeId simObject) {
        return GroupTypeIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupTypeIdInput.getDescriptor();
    }

    @Override
    public GroupTypeIdInput getDefaultInstanceForInputObject() {
        return GroupTypeIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupTypeId> getSimObjectClass() {
        return GroupTypeId.class;
    }

    @Override
    public Class<GroupTypeIdInput> getInputObjectClass() {
        return GroupTypeIdInput.class;
    }
}