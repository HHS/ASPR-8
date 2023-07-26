package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupsForPersonFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.EqualityInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.support.GroupsForPersonFilter;
import plugins.partitions.support.Equality;

public class GroupsForPersonFilterTranslationSpec
        extends ProtobufTranslationSpec<GroupsForPersonFilterInput, GroupsForPersonFilter> {

    @Override
    protected GroupsForPersonFilter convertInputObject(GroupsForPersonFilterInput inputObject) {
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        int groupCount = inputObject.getGroupCount();

        return new GroupsForPersonFilter(equality, groupCount);
    }

    @Override
    protected GroupsForPersonFilterInput convertAppObject(GroupsForPersonFilter appObject) {
        EqualityInput equalityInput = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(), Equality.class);
        int groupCount = appObject.getGroupCount();

        return GroupsForPersonFilterInput.newBuilder()
                .setEquality(equalityInput)
                .setGroupCount(groupCount)
                .build();
    }

    @Override
    public Class<GroupsForPersonFilter> getAppObjectClass() {
        return GroupsForPersonFilter.class;
    }

    @Override
    public Class<GroupsForPersonFilterInput> getInputObjectClass() {
        return GroupsForPersonFilterInput.class;
    }

}
