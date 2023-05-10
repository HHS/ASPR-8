package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.UInt64Value;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_ULongTranslationSpec {

    @Test
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
    public void getAppObjectClass() {
        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();

        assertEquals(Long.class, booleanTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        ULongTranslationSpec booleanTranslationSpec = new ULongTranslationSpec();

        assertEquals(UInt64Value.class, booleanTranslationSpec.getInputObjectClass());
    }
}
