package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupTypeIdInput;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslatorSpec extends AbstractTranslatorSpec<GroupTypeIdInput, GroupTypeId> {

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
    public GroupTypeIdInput getDefaultInstanceForInputObject() {
        return GroupTypeIdInput.getDefaultInstance();
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
