package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.annotations.UnitTestForCoverage;

public class AT_ULongTranslationSpec {

    @Test
    @UnitTestConstructor(target = ULongTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new ULongTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();
        booleanTranslationSpec.init(protobufTranslationEngine);

        Long expectedValue = 100L;
        UInt64Value inputValue = UInt64Value.of(expectedValue);

        Long actualValue = booleanTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestForCoverage
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();
        booleanTranslationSpec.init(protobufTranslationEngine);

        Long appValue = 100L;
        UInt64Value expectedValue = UInt64Value.of(appValue);

        UInt64Value actualValue = booleanTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    @UnitTestMethod(target = ULongTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();

        assertEquals(Long.class, booleanTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = ULongTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();

        assertEquals(UInt64Value.class, booleanTranslationSpec.getInputObjectClass());
    }
}
