package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.gcm.translation.plugins.properties.testsupport.input.TestMessage;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;

public class TestMessageTranslatorSpec extends AbstractTranslatorSpec<TestMessage, TestMessageSimObject> {

    @Override
    protected TestMessageSimObject convertInputObject(TestMessage inputObject) {
        TestMessageSimObject simObject = new TestMessageSimObject();

        simObject.setLayer1((Layer1SimObject) this.translator.convertInputObject(inputObject.getLayer1()));

        return simObject;
    }

    @Override
    protected TestMessage convertAppObject(TestMessageSimObject simObject) {
        Layer1 layer1 = (Layer1) this.translator.convertSimObject(simObject.getLayer1());

        TestMessage testMessage = TestMessage.newBuilder().setLayer1(layer1).build();

        return testMessage;
    }

    @Override
    public TestMessage getDefaultInstanceForInputObject() {
        return TestMessage.getDefaultInstance();
    }

    @Override
    public Class<TestMessageSimObject> getAppObjectClass() {
        return TestMessageSimObject.class;
    }

    @Override
    public Class<TestMessage> getInputObjectClass() {
        return TestMessage.class;
    }

}
