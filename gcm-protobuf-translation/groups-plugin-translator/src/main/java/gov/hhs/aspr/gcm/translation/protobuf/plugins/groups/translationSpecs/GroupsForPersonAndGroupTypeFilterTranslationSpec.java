package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypeIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupsForPersonAndGroupTypeFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.support.GroupTypeId;
import plugins.groups.support.GroupsForPersonAndGroupTypeFilter;
import plugins.partitions.support.Equality;

public class GroupsForPersonAndGroupTypeFilterTranslationSpec
        extends ProtobufTranslationSpec<GroupsForPersonAndGroupTypeFilterInput, GroupsForPersonAndGroupTypeFilter> {

    @Override
    protected GroupsForPersonAndGroupTypeFilter convertInputObject(GroupsForPersonAndGroupTypeFilterInput inputObject) {
        GroupTypeId groupTypeId = this.translationEngine.convertObject(inputObject.getGroupTypeId());
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        int groupCount = inputObject.getGroupCount();

        return new GroupsForPersonAndGroupTypeFilter(groupTypeId, equality, groupCount);
    }

    @Override
    protected GroupsForPersonAndGroupTypeFilterInput convertAppObject(GroupsForPersonAndGroupTypeFilter appObject) {
        GroupTypeIdInput groupTypeIdInput = this.translationEngine.convertObjectAsSafeClass(appObject.getGroupTypeId(),
                GroupTypeId.class);
        EqualityInput equalityInput = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(), Equality.class);
        int groupCount = appObject.getGroupCount();

        return GroupsForPersonAndGroupTypeFilterInput.newBuilder()
                .setEquality(equalityInput)
                .setGroupTypeId(groupTypeIdInput)
                .setGroupCount(groupCount)
                .build();
    }

    @Override
    public Class<GroupsForPersonAndGroupTypeFilter> getAppObjectClass() {
        return GroupsForPersonAndGroupTypeFilter.class;
    }

    @Override
    public Class<GroupsForPersonAndGroupTypeFilterInput> getInputObjectClass() {
        return GroupsForPersonAndGroupTypeFilterInput.class;
    }

}
