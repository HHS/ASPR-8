package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.simobjects.Group;
import plugins.groups.input.GroupIdInput;
import plugins.groups.input.GroupInput;
import plugins.groups.input.GroupTypeIdInput;

public class GroupTranslator extends AbstractTranslator<GroupInput, Group> {

    @Override
    protected Group convertInputObject(GroupInput inputObject) {
        Group group = new Group();

        group.setGroupId(this.translator.convertInputObject(inputObject.getGroupId()));
        group.setGroupTypeId(this.translator.convertInputObject(inputObject.getGroupTypeId()));

        return group;
    }

    @Override
    protected GroupInput convertSimObject(Group simObject) {
        GroupInput.Builder builder = GroupInput.newBuilder();

        builder.setGroupId((GroupIdInput) this.translator.convertSimObject(simObject.getGroupId()))
                .setGroupTypeId((GroupTypeIdInput) this.translator.convertSimObject(simObject.getGroupTypeId()));

        return builder.build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupInput.getDescriptor();
    }

    @Override
    public GroupInput getDefaultInstanceForInputObject() {
        return GroupInput.getDefaultInstance();
    }

    @Override
    public Class<Group> getSimObjectClass() {
        return Group.class;
    }

    @Override
    public Class<GroupInput> getInputObjectClass() {
        return GroupInput.class;
    }

}
