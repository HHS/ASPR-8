package gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.protobuf.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.app.TestComplexAppObject;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.input.TestComplexInputObject;

public class AT_TestProtobufComplexObjectTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();
        complexObjectTranslationSpec.init(protobufTranslationEngine);

        TestComplexAppObject expectedValue = TestObjectUtil.generateTestComplexAppObject();
        TestComplexInputObject inputValue = TestObjectUtil.getComplexInputFromComplexApp(expectedValue);

        TestComplexAppObject actualValue = complexObjectTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();
        complexObjectTranslationSpec.init(protobufTranslationEngine);

        TestComplexAppObject appValue = TestObjectUtil.generateTestComplexAppObject();
        TestComplexInputObject expectedValue = TestObjectUtil.getComplexInputFromComplexApp(appValue);

        TestComplexInputObject actualValue = complexObjectTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();

        assertEquals(TestComplexAppObject.class, complexObjectTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();

        assertEquals(TestComplexInputObject.class, complexObjectTranslationSpec.getInputObjectClass());
    }
}
