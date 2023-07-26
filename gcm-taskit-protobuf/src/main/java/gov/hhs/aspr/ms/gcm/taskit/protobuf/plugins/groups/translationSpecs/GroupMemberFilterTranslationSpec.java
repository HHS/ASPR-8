package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupMemberFilterInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.support.GroupMemberFilter;

public class GroupMemberFilterTranslationSpec
        extends ProtobufTranslationSpec<GroupMemberFilterInput, GroupMemberFilter> {

    @Override
    protected GroupMemberFilter convertInputObject(GroupMemberFilterInput inputObject) {
        return new GroupMemberFilter(this.translationEngine.convertObject(inputObject.getGroupId()));
    }

    @Override
    protected GroupMemberFilterInput convertAppObject(GroupMemberFilter appObject) {
        GroupIdInput groupIdInput = this.translationEngine.convertObject(appObject.getGroupId());
        return GroupMemberFilterInput.newBuilder().setGroupId(groupIdInput).build();
    }

    @Override
    public Class<GroupMemberFilter> getAppObjectClass() {
        return GroupMemberFilter.class;
    }

    @Override
    public Class<GroupMemberFilterInput> getInputObjectClass() {
        return GroupMemberFilterInput.class;
    }

}
