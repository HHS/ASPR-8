package gov.hhs.aspr.gcm.gcmprotobuf.plugins.groups.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.gcmprotobuf.core.AbstractTranslator;
import plugins.groups.input.GroupPropertyIdInput;
import plugins.groups.testsupport.TestGroupPropertyId;

public class TestGroupPropertyIdTranslator extends AbstractTranslator<GroupPropertyIdInput, TestGroupPropertyId> {

    @Override
    protected TestGroupPropertyId convertInputObject(GroupPropertyIdInput inputObject) {
        return TestGroupPropertyId
                .valueOf(this.translator.getObjectFromAny(inputObject.getGroupPropertyId()).toString());
    }

    @Override
    protected GroupPropertyIdInput convertSimObject(TestGroupPropertyId simObject) {
        return GroupPropertyIdInput.newBuilder().setGroupPropertyId(this.translator.getAnyFromObject(simObject.name()))
                .build();
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
    public Class<TestGroupPropertyId> getSimObjectClass() {
        return TestGroupPropertyId.class;
    }

    @Override
    public Class<GroupPropertyIdInput> getInputObjectClass() {
        return GroupPropertyIdInput.class;
    }

}
