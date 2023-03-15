package gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.input.TestMessage;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.simobjects.TestMessageSimObject;

public class TestMessageTranslator extends AObjectTranslatorSpec<TestMessage, TestMessageSimObject> {

    @Override
    protected TestMessageSimObject convertInputObject(TestMessage inputObject) {
        TestMessageSimObject simObject = new TestMessageSimObject();

        simObject.setLayer1((Layer1SimObject) this.translator.convertInputObject(inputObject.getLayer1()));

        return simObject;
    }

    @Override
    protected TestMessage convertSimObject(TestMessageSimObject simObject) {
        Layer1 layer1 = (Layer1) this.translator.convertSimObject(simObject.getLayer1());

        TestMessage testMessage = TestMessage.newBuilder().setLayer1(layer1).build();

        return testMessage;
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return TestMessage.getDescriptor();
    }

    @Override
    public TestMessage getDefaultInstanceForInputObject() {
        return TestMessage.getDefaultInstance();
    }

    @Override
    public Class<TestMessageSimObject> getSimObjectClass() {
        return TestMessageSimObject.class;
    }

    @Override
    public Class<TestMessage> getInputObjectClass() {
        return TestMessage.class;
    }

}
