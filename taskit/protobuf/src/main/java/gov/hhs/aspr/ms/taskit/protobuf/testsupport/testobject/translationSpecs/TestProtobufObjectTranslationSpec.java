package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.translationSpecs;

import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.taskit.protobuf.testsupport.testcomplexobject.input.TestComplexInputObject;
import gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.input.TestInputObject;

public class TestProtobufObjectTranslationSpec extends ProtobufTranslationSpec<TestInputObject, TestAppObject> {
    @Override
    protected TestAppObject convertInputObject(TestInputObject inputObject) {
        TestAppObject testAppObject = new TestAppObject();

        testAppObject.setBool(inputObject.getBool());
        testAppObject.setInteger(inputObject.getInteger());
        testAppObject.setString(inputObject.getString());
        testAppObject
                .setTestComplexAppObject(this.translationEngine.convertObject(inputObject.getTestComplexInputObject()));

        return testAppObject;
    }

    @Override
    protected TestInputObject convertAppObject(TestAppObject appObject) {
        TestInputObject testInputObject = TestInputObject.newBuilder()
                .setBool(appObject.isBool())
                .setInteger(appObject.getInteger())
                .setString(appObject.getString())
                .setTestComplexInputObject((TestComplexInputObject) this.translationEngine
                        .convertObject(appObject.getTestComplexAppObject()))
                .build();

        return testInputObject;
    }

    @Override
    public Class<TestAppObject> getAppObjectClass() {
        return TestAppObject.class;
    }

    @Override
    public Class<TestInputObject> getInputObjectClass() {
        return TestInputObject.class;
    }
}
