package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.google.protobuf.Any;
import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.input.WrapperEnumValue;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs.TestProtobufComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputObject;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufEnumTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufObjectTranslationSpec;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * TranslationSpec that defines how to convert from any Java Object to a
 * Protobuf {@link Any} type and vice versa
 */
public class AT_AnyTranslationSpec {

    @Test
    @UnitTestConstructor(target = AnyTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new AnyTranslationSpec());
    }

    @Test
    @UnitTestMethod(target = AnyTranslationSpec.class, name = "convertInputObject", args = { Any.class })
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();
        anyTranslationSpec.init(protobufTranslationEngine);

        Integer expectedValue = 100;
        Int32Value int32Value = Int32Value.of(expectedValue);

        Any any = Any.pack(int32Value);

        Object obj = anyTranslationSpec.convertInputObject(any);

        assertEquals(expectedValue, obj);

        // preconditions
        // the typeurl of the any is malformed
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            Any badAny = Any.newBuilder().setTypeUrl("badTypeUrl").build();
            anyTranslationSpec.convertInputObject(badAny);
        });

        assertEquals("Malformed type url", runtimeException.getMessage());

        // the type url is set to a value that doesn't correspond to a Message Type
        runtimeException = assertThrows(RuntimeException.class, () -> {
            Any badAny = Any.newBuilder().setTypeUrl("/" + TestInputEnum.TEST1.getDescriptorForType().getFullName())
                    .build();
            anyTranslationSpec.convertInputObject(badAny);
        });

        assertEquals("Message is not assignable from " + TestInputEnum.class.getName(), runtimeException.getMessage());

        // the typeurl doesn't match the class of the packed message
        // this is tested in the test: testUnpackMessage
    }

    @Test
    @UnitTestMethod(target = AnyTranslationSpec.class, name = "unpackMessage", args = { Any.class, Class.class })
    public void testUnpackMessage() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .addTranslationSpec(new TestProtobufObjectTranslationSpec())
                .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec())
                .build();

        protobufTranslationEngine.init();
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();
        anyTranslationSpec.init(protobufTranslationEngine);

        Integer expectedValue = 100;
        Int32Value int32Value = Int32Value.of(expectedValue);

        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            Any badAny = Any.pack(int32Value);
            anyTranslationSpec.unpackMessage(badAny, TestInputObject.class);
        });

        assertEquals("Unable To unpack any type to given class: " + TestInputObject.class.getName(),
                runtimeException.getMessage());
        assertEquals(InvalidProtocolBufferException.class, runtimeException.getCause().getClass());
    }

    @Test
    @UnitTestMethod(target = AnyTranslationSpec.class, name = "convertAppObject", args = { Object.class })
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .addTranslationSpec(new TestProtobufEnumTranslationSpec())
                .build();

        protobufTranslationEngine.init();
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();
        anyTranslationSpec.init(protobufTranslationEngine);

        // app object coverted into any
        Integer value = 100;
        Int32Value expectedValue = Int32Value.of(value);

        Any expectedAny = Any.pack(expectedValue);

        Any actualAny = anyTranslationSpec.convertAppObject(value);

        assertEquals(expectedAny, actualAny);

        // app enum converted into any by wrapping it in a WrapperEnumValue
        TestAppEnum appValue = TestAppEnum.TEST1;
        TestInputEnum expecetedValue = TestInputEnum.TEST1;

        WrapperEnumValue wrapperEnumValue = WrapperEnumValue.newBuilder()
                .setEnumTypeUrl(TestInputEnum.getDescriptor().getFullName())
                .setValue(expecetedValue.name())
                .build();

        expectedAny = Any.pack(wrapperEnumValue);

        actualAny = anyTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedAny, actualAny);

        // by calling covert on an object that was already converted
        // this case is specifcally used for
        // ProtobufTranslationEngine.testGetAnyFromObjectAsSafeClass
        actualAny = anyTranslationSpec.convertAppObject(wrapperEnumValue);

        assertEquals(expectedAny, actualAny);
    }

    @Test
    @UnitTestMethod(target = AnyTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();

        assertEquals(Object.class, anyTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = AnyTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        AnyTranslationSpec anyTranslationSpec = new AnyTranslationSpec();

        assertEquals(Any.class, anyTranslationSpec.getInputObjectClass());
    }
}
