package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.TestMessage;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;

public class TestMessageTranslatorSpec extends ProtobufTranslatorSpec<TestMessage, TestMessageSimObject> {

    @Override
    protected TestMessageSimObject convertInputObject(TestMessage inputObject) {
        TestMessageSimObject appObject = new TestMessageSimObject();

        appObject.setLayer1((Layer1SimObject) this.translatorCore.convertObject(inputObject.getLayer1()));

        return appObject;
    }

    @Override
    protected TestMessage convertAppObject(TestMessageSimObject appObject) {
        Layer1 layer1 = (Layer1) this.translatorCore.convertObject(appObject.getLayer1());

        TestMessage testMessage = TestMessage.newBuilder().setLayer1(layer1).build();

        return testMessage;
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
