package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_BooleanTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();
        booleanTranslationSpec.init(protobufTranslationEngine);

        Boolean expectedValue = false;
        BoolValue inputValue = BoolValue.of(false);

        Boolean actualValue = booleanTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();
        booleanTranslationSpec.init(protobufTranslationEngine);

        Boolean appValue = false;
        BoolValue expectedValue = BoolValue.of(false);

        BoolValue actualValue = booleanTranslationSpec.convertAppObject(appValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();

        assertEquals(Boolean.class, booleanTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();

        assertEquals(BoolValue.class, booleanTranslationSpec.getInputObjectClass());
    }
}