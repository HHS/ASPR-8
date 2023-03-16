package gov.hhs.aspr.gcm.translation.plugins.groups.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupPropertyIdInput;
import plugins.groups.support.GroupPropertyId;

public class GroupPropertyIdTranslatorSpec extends AObjectTranslatorSpec<GroupPropertyIdInput, GroupPropertyId> {

    @Override
    protected GroupPropertyId convertInputObject(GroupPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected GroupPropertyIdInput convertSimObject(GroupPropertyId simObject) {
        return GroupPropertyIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupPropertyIdInput.getDescriptor();
    }

    @Override
    public GroupPropertyIdInput getDefaultInstanceForInputObject() {
        return GroupPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupPropertyId> getSimObjectClass() {
        return GroupPropertyId.class;
    }

    @Override
    public Class<GroupPropertyIdInput> getInputObjectClass() {
        return GroupPropertyIdInput.class;
    }
}