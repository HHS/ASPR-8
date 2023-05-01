package gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.Layer1;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.input.TestMessage;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.Layer1SimObject;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.testsupport.simobjects.TestMessageSimObject;

public class TestMessageTranslationSpec extends ProtobufTranslationSpec<TestMessage, TestMessageSimObject> {

    @Override
    protected TestMessageSimObject convertInputObject(TestMessage inputObject) {
        TestMessageSimObject appObject = new TestMessageSimObject();

        appObject.setLayer1((Layer1SimObject) this.translationEngine.convertObject(inputObject.getLayer1()));

        return appObject;
    }

    @Override
    protected TestMessage convertAppObject(TestMessageSimObject appObject) {
        Layer1 layer1 = (Layer1) this.translationEngine.convertObject(appObject.getLayer1());

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
