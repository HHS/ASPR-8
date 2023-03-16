package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import plugins.groups.support.GroupId;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;

public class GroupIdTranslatorSpec extends AObjectTranslatorSpec<GroupIdInput, GroupId> {

    @Override
    protected GroupId convertInputObject(GroupIdInput inputObject) {
        return new GroupId(inputObject.getId());
    }

    @Override
    protected GroupIdInput convertSimObject(GroupId simObject) {
        return GroupIdInput.newBuilder().setId(simObject.getValue()).build();
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
