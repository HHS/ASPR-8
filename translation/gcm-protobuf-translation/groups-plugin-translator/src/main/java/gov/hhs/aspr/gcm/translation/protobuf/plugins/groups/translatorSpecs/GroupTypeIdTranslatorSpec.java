package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslatorSpec extends ProtobufTranslatorSpec<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GroupTypeIdInput convertAppObject(GroupTypeId appObject) {
        return GroupTypeIdInput.newBuilder()
                .setId(this.translatorCore.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<GroupTypeId> getAppObjectClass() {
        return GroupTypeId.class;
    }

    @Override
    public Class<GroupTypeIdInput> getInputObjectClass() {
        return GroupTypeIdInput.class;
    }
}
