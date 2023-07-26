package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.groups.support.GroupId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain GroupIdInput} and
 * {@linkplain GroupId}
 */
public class GroupIdTranslationSpec extends ProtobufTranslationSpec<GroupIdInput, GroupId> {

    @Override
    protected GroupId convertInputObject(GroupIdInput inputObject) {
        return new GroupId(inputObject.getId());
    }

    @Override
    protected GroupIdInput convertAppObject(GroupId appObject) {
        return GroupIdInput.newBuilder().setId(appObject.getValue()).build();
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