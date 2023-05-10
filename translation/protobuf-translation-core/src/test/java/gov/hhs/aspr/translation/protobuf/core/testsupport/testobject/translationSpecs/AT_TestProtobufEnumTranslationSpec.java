package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestProtobufEnumTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestProtobufEnumTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestProtobufEnumTranslationSpec());
    }

    @Test
    @UnitTestMethod(target = TestProtobufEnumTranslationSpec.class, name = "convertInputObject", args = {
            TestInputEnum.class })
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
    @UnitTestMethod(target = TestProtobufEnumTranslationSpec.class, name = "convertAppObject", args = {
            TestAppEnum.class })
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
