package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypesForPersonFilterInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.partitions.input.EqualityInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
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