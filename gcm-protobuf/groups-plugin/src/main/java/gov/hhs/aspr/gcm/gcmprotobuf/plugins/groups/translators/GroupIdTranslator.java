package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.support.GroupId;
import plugins.groups.input.GroupIdInput;

public class GroupIdTranslator extends AbstractTranslator<GroupIdInput, GroupId> {

    @Override
    protected GroupId convertInputObject(GroupIdInput inputObject) {
        return new GroupId(inputObject.getGroupId());
    }

    @Override
    protected GroupIdInput convertSimObject(GroupId simObject) {
        return GroupIdInput.newBuilder().setGroupId(simObject.getValue()).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupIdInput.getDescriptor();
    }

    @Override
    public GroupIdInput getDefaultInstanceForInputObject() {
        return GroupIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupId> getSimObjectClass() {
        return GroupId.class;
    }

    @Override
    public Class<GroupIdInput> getInputObjectClass() {
        return GroupIdInput.class;
    }

}
