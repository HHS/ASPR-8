package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.input.GroupTypeIdInput;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslator extends AbstractTranslator<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getGroupTypeId(), GroupTypeId.class);
    }

    @Override
    protected GroupTypeIdInput convertSimObject(GroupTypeId simObject) {
        return GroupTypeIdInput.newBuilder()
                .setGroupTypeId(this.translator.getAnyFromObject(simObject)).build();
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
