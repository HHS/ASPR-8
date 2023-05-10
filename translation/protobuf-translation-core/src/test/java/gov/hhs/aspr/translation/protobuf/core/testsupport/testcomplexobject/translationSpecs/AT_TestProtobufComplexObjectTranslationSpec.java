package gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.protobuf.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.app.TestComplexAppObject;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.input.TestComplexInputObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestMethodForCoverage;

public class AT_TestProtobufComplexObjectTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestProtobufComplexObjectTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestProtobufComplexObjectTranslationSpec());
    }

    @Test
    @UnitTestMethodForCoverage
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
    @UnitTestMethodForCoverage
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
    @UnitTestMethod(target = TestProtobufComplexObjectTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();

        assertEquals(TestComplexAppObject.class, complexObjectTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestProtobufComplexObjectTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestProtobufComplexObjectTranslationSpec complexObjectTranslationSpec = new TestProtobufComplexObjectTranslationSpec();

        assertEquals(TestComplexInputObject.class, complexObjectTranslationSpec.getInputObjectClass());
    }
}
