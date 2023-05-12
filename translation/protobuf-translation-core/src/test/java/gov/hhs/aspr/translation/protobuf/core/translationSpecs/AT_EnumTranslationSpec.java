package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufEnumTranslationSpec;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestForCoverage;

public class AT_EnumTranslationSpec {

    @Test
    @UnitTestConstructor(target = EnumTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new EnumTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        EnumTranslationSpec enumTranslationSpec = new EnumTranslationSpec();
        enumTranslationSpec.init(protobufTranslationEngine);

        TestAppEnum expectedValue = TestAppEnum.TEST1;
        WrapperEnumValue inputValue = WrapperEnumValue.newBuilder()
                .setEnumTypeUrl(TestInputEnum.TEST1.getDescriptorForType().getFullName())
                .setValue(TestInputEnum.TEST1.name()).build();

        TestAppEnum actualValue = (TestAppEnum) enumTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);

        // precondition
        // type url is well formed
        assertThrows(RuntimeException.class, () -> {
            WrapperEnumValue badInputValue = WrapperEnumValue.newBuilder()
                    .setEnumTypeUrl(BoolValue.getDefaultInstance().getDescriptorForType().getFullName())
                    .setValue(TestInputEnum.TEST1.name()).build();

            enumTranslationSpec.convertInputObject(badInputValue);
        });
    }

    @Test
    @UnitTestForCoverage
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .build();
        protobufTranslationEngine.init();

        EnumTranslationSpec enumTranslationSpec = new EnumTranslationSpec();
        enumTranslationSpec.init(protobufTranslationEngine);

        TestAppEnum appValue = TestAppEnum.TEST2;
        WrapperEnumValue expectedValue = WrapperEnumValue.newBuilder()
                .setEnumTypeUrl(TestInputEnum.TEST2.getDescriptorForType().getFullName())
                .setValue(TestInputEnum.TEST2.name()).build();

        WrapperEnumValue actualValue = enumTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = EnumTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        EnumTranslationSpec enumTranslationSpec = new EnumTranslationSpec();

        assertEquals(Enum.class, enumTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = EnumTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        EnumTranslationSpec enumTranslationSpec = new EnumTranslationSpec();

        assertEquals(WrapperEnumValue.class, enumTranslationSpec.getInputObjectClass());
    }
}