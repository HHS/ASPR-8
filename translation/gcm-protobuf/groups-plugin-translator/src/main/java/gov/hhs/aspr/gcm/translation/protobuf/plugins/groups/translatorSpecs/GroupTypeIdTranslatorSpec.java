package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupTypeIdInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslatorSpec extends AbstractProtobufTranslatorSpec<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), GroupTypeId.class);
    }

    @Override
    protected GroupTypeIdInput convertAppObject(GroupTypeId simObject) {
        return GroupTypeIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
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
