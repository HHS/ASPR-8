package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs.TestProtobufComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputObject;

public class AT_TestProtobufObjectTranslationSpec {
    
    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        TestProtobufObjectTranslationSpec objectTranslationSpec = new TestProtobufObjectTranslationSpec();
        objectTranslationSpec.init(protobufTranslationEngine);

        TestAppObject expectedValue = TestObjectUtil.generateTestAppObject();
        TestInputObject inputValue = TestObjectUtil.getInputFromApp(expectedValue);

        TestAppObject actualValue = objectTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        TestProtobufObjectTranslationSpec objectTranslationSpec = new TestProtobufObjectTranslationSpec();
        objectTranslationSpec.init(protobufTranslationEngine);

        TestAppObject appValue = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedValue = TestObjectUtil.getInputFromApp(appValue);

        TestInputObject actualValue = objectTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        TestProtobufObjectTranslationSpec objectTranslationSpec = new TestProtobufObjectTranslationSpec();

        assertEquals(TestAppObject.class, objectTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        TestProtobufObjectTranslationSpec objectTranslationSpec = new TestProtobufObjectTranslationSpec();

        assertEquals(TestInputObject.class, objectTranslationSpec.getInputObjectClass());
    }
}
