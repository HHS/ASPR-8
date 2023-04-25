package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.support.GroupPropertyId;

public class GroupPropertyIdTranslationSpec extends ProtobufTranslationSpec<GroupPropertyIdInput, GroupPropertyId> {

    @Override
    protected GroupPropertyId convertInputObject(GroupPropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GroupPropertyIdInput convertAppObject(GroupPropertyId appObject) {
        return GroupPropertyIdInput.newBuilder()
                .setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
