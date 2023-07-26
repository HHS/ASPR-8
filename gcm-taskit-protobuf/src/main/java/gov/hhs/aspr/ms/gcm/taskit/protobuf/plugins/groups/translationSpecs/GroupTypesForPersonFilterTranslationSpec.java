package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypesForPersonFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.support.input.EqualityInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.groups.support.GroupTypesForPersonFilter;
import plugins.partitions.support.Equality;

public class GroupTypesForPersonFilterTranslationSpec
        extends ProtobufTranslationSpec<GroupTypesForPersonFilterInput, GroupTypesForPersonFilter> {

    @Override
    protected GroupTypesForPersonFilter convertInputObject(GroupTypesForPersonFilterInput inputObject) {
        Equality equality = this.translationEngine.convertObject(inputObject.getEquality());
        int groupTypeCount = inputObject.getGroupTypeCount();

        return new GroupTypesForPersonFilter(equality, groupTypeCount);
    }

    @Override
    protected GroupTypesForPersonFilterInput convertAppObject(GroupTypesForPersonFilter appObject) {
        EqualityInput equality = this.translationEngine.convertObjectAsSafeClass(appObject.getEquality(), Equality.class);
        int groupTypeCount = appObject.getGroupTypeCount();

        return GroupTypesForPersonFilterInput.newBuilder()
                .setEquality(equality)
                .setGroupTypeCount(groupTypeCount)
                .build();
    }

    @Override
    public Class<GroupTypesForPersonFilter> getAppObjectClass() {
        return GroupTypesForPersonFilter.class;
    }

    @Override
    public Class<GroupTypesForPersonFilterInput> getInputObjectClass() {
        return GroupTypesForPersonFilterInput.class;
    }

}
