package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.input.GroupTypeIdInput;
import plugins.groups.testsupport.TestGroupTypeId;

public class TestGroupTypeIdTranslator extends AbstractTranslator<GroupTypeIdInput, TestGroupTypeId> {

    @Override
    protected TestGroupTypeId convertInputObject(GroupTypeIdInput inputObject) {
        return TestGroupTypeId.valueOf(this.translator.getObjectFromAny(inputObject.getGroupTypeId()).toString());
    }

    @Override
    protected GroupTypeIdInput convertSimObject(TestGroupTypeId simObject) {
        return GroupTypeIdInput.newBuilder().setGroupTypeId(this.translator.getAnyFromObject(simObject.name())).build();
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
    public Class<TestGroupTypeId> getSimObjectClass() {
        return TestGroupTypeId.class;
    }

    @Override
    public Class<GroupTypeIdInput> getInputObjectClass() {
        return GroupTypeIdInput.class;
    }

}
