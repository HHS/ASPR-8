package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.input.GroupTypeIdInput;
import plugins.groups.support.GroupTypeId;

public class GroupTypeIdTranslator extends AbstractTranslator<GroupTypeIdInput, GroupTypeId> {
    public class SimpleGroupTypeId implements GroupTypeId {
        Object value;

        public SimpleGroupTypeId(Object value) {
            this.value = value;
        }

        public Object getValue() {
            return this.value;
        }
    }

    @Override
    protected GroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return new SimpleGroupTypeId(this.translator.getObjectFromAny(inputObject.getGroupTypeId()));
    }

    @Override
    protected GroupTypeIdInput convertSimObject(GroupTypeId simObject) {
        SimpleGroupTypeId simpleGroupTypeId = (SimpleGroupTypeId) simObject;
        return GroupTypeIdInput.newBuilder()
                .setGroupTypeId(this.translator.getAnyFromObject(simpleGroupTypeId.getValue())).build();
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
