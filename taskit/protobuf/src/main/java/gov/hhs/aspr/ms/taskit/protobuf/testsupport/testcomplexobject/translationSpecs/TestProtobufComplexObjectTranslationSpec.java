package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testcomplexobject.translationSpecs;

import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.taskit.protobuf.testsupport.testcomplexobject.input.TestComplexInputObject;

public class TestProtobufComplexObjectTranslationSpec
        extends ProtobufTranslationSpec<TestComplexInputObject, TestComplexAppObject> {

    @Override
    protected TestComplexAppObject convertInputObject(TestComplexInputObject inputObject) {
        TestComplexAppObject testComplexAppObject = new TestComplexAppObject();

        testComplexAppObject.setNumEntities(inputObject.getNumEntities());
        testComplexAppObject.setStartTime(inputObject.getStartTime());
        testComplexAppObject.setTestString(inputObject.getTestString());

        return testComplexAppObject;
    }

    @Override
    protected TestComplexInputObject convertAppObject(TestComplexAppObject appObject) {
        return TestComplexInputObject.newBuilder().setNumEntities(appObject.getNumEntities())
                .setStartTime(appObject.getStartTime())
                .setTestString(appObject.getTestString()).build();
    }

    @Override
    public Class<TestComplexAppObject> getAppObjectClass() {
        return TestComplexAppObject.class;
    }

    @Override
    public Class<TestComplexInputObject> getInputObjectClass() {
        return TestComplexInputObject.class;
    }

}
