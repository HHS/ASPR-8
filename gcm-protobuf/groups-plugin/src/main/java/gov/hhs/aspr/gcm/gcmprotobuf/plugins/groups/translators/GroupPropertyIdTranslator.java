package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.input.GroupPropertyIdInput;
import plugins.groups.support.GroupPropertyId;

public class GroupPropertyIdTranslator extends AbstractTranslator<GroupPropertyIdInput, GroupPropertyId> {

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
