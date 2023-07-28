package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GroupPropertyIdInput} and
 * {@linkplain GroupPropertyId}
 */
public class GroupPropertyIdTranslationSpec extends ProtobufTranslationSpec<GroupPropertyIdInput, GroupPropertyId> {

    @Override
    protected GroupPropertyId convertInputObject(GroupPropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GroupPropertyIdInput convertAppObject(GroupPropertyId appObject) {
        return GroupPropertyIdInput.newBuilder()
                .setId(this.translationEngine.getAnyFromObject(appObject)).build();
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
