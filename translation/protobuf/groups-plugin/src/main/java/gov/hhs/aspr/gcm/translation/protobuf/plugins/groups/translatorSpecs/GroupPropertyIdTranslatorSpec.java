package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.groups.support.GroupPropertyId;

public class GroupPropertyIdTranslatorSpec extends AbstractTranslatorSpec<GroupPropertyIdInput, GroupPropertyId> {

    @Override
    protected GroupPropertyId convertInputObject(GroupPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected GroupPropertyIdInput convertAppObject(GroupPropertyId simObject) {
        return GroupPropertyIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public GroupPropertyIdInput getDefaultInstanceForInputObject() {
        return GroupPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupPropertyId> getAppObjectClass() {
        return GroupPropertyId.class;
    }

    @Override
    public Class<GroupPropertyIdInput> getInputObjectClass() {
        return GroupPropertyIdInput.class;
    }
}
