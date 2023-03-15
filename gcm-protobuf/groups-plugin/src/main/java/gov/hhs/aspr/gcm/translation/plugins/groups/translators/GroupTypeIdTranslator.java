package gov.hhs.aspr.gcm.translation.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.groups.input.GroupTypeIdInput;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslator extends Translator<GroupTypeIdInput, GroupTypeId> {

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), GroupTypeId.class);
    }

    @Override
    protected GroupTypeIdInput convertSimObject(GroupTypeId simObject) {
        return GroupTypeIdInput.newBuilder()
                .setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return GroupTypeIdInput.getDescriptor();
    }

    @Override
    public GroupTypeIdInput getDefaultInstanceForInputObject() {
        return GroupTypeIdInput.getDefaultInstance();
    }

    @Override
    public Class<GroupTypeId> getSimObjectClass() {
        return GroupTypeId.class;
    }

    @Override
    public Class<GroupTypeIdInput> getInputObjectClass() {
        return GroupTypeIdInput.class;
    }
}
