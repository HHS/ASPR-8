package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.groups.support.GroupId;

public class GroupIdTranslatorSpec extends AbstractTranslatorSpec<GroupIdInput, GroupId> {

    @Override
    protected GroupId convertInputObject(GroupIdInput inputObject) {
        return new GroupId(inputObject.getId());
    }

    @Override
    protected GroupIdInput convertAppObject(GroupId simObject) {
        return GroupIdInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public GroupIdInput getDefaultInstanceForInputObject() {
        return GroupIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupId> getAppObjectClass() {
        return GroupId.class;
    }

    @Override
    public Class<GroupIdInput> getInputObjectClass() {
        return GroupIdInput.class;
    }

}
