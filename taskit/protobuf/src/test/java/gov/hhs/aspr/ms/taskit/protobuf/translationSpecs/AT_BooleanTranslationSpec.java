package gov.hhs.aspr.ms.taskit.protobuf.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;

public class AT_BooleanTranslationSpec {

    @Test
    @UnitTestConstructor(target = BooleanTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new BooleanTranslationSpec());
    }

    @Test
    @UnitTestForCoverage
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
    @UnitTestForCoverage
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
    @UnitTestMethod(target = BooleanTranslationSpec.class, name = "getAppObjectClass", args = {})
    public void testGetAppObjectClass() {
        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();

        assertEquals(Boolean.class, booleanTranslationSpec.getAppObjectClass());
    }

    @Test
    @UnitTestMethod(target = BooleanTranslationSpec.class, name = "getInputObjectClass", args = {})
    public void testGetInputObjectClass() {
        BooleanTranslationSpec booleanTranslationSpec = new BooleanTranslationSpec();

        assertEquals(BoolValue.class, booleanTranslationSpec.getInputObjectClass());
    }
}