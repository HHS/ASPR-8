package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.input.TestInputEnum;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_TestProtobufEnumTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestProtobufEnumTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestProtobufEnumTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();
        protobufTranslationEngine.init();

        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();
        enumTranslationSpec.init(protobufTranslationEngine);

        TestAppEnum expectedValue = TestAppEnum.TEST1;
        TestInputEnum inputValue = TestInputEnum.TEST1;

        TestAppEnum actualValue = enumTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestForCoverage
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();
        protobufTranslationEngine.init();

        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();
        enumTranslationSpec.init(protobufTranslationEngine);

        TestAppEnum appValue = TestAppEnum.TEST2;
        TestInputEnum expectedValue = TestInputEnum.TEST2;

        TestInputEnum actualValue = enumTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = TestProtobufEnumTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();

        assertEquals(TestAppEnum.class, enumTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = TestProtobufEnumTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();

        assertEquals(TestInputEnum.class, enumTranslationSpec.getInputObjectClass());
    }
}
