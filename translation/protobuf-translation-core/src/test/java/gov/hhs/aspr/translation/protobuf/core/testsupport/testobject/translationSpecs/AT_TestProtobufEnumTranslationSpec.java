package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;

public class AT_TestProtobufEnumTranslationSpec {
    
    @Test
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
    public void getAppObjectClass() {
        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();

        assertEquals(TestAppEnum.class, enumTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        TestProtobufEnumTranslationSpec enumTranslationSpec = new TestProtobufEnumTranslationSpec();

        assertEquals(TestInputEnum.class, enumTranslationSpec.getInputObjectClass());
    }
}
