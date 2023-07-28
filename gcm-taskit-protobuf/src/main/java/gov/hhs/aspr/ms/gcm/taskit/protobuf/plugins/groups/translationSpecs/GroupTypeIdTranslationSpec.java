package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupTypeIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.groups.support.GroupTypeId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GroupTypeIdInput} and
 * {@linkplain GroupTypeId}
 */
public class GroupTypeIdTranslationSpec extends ProtobufTranslationSpec<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GroupTypeIdInput convertAppObject(GroupTypeId appObject) {
        return GroupTypeIdInput.newBuilder()
                .setId(this.translationEngine.getAnyFromObject(appObject)).build();
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
