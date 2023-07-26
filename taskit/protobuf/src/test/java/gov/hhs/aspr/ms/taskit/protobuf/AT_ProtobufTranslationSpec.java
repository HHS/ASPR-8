package gov.hhs.aspr.ms.taskit.protobuf;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.ms.taskit.core.TranslationEngine;
import gov.hhs.aspr.ms.taskit.protobuf.translationSpecs.BooleanTranslationSpec;
import util.annotations.UnitTestMethod;

public class AT_ProtobufTranslationSpec {

    @Test
    @UnitTestMethod(target = ProtobufTranslationSpec.class, name = "init", args = { TranslationEngine.class })
    public void testInit() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine.builder().build();

        ProtobufTranslationSpec<BoolValue, Boolean> booleanTranslationSpec = new BooleanTranslationSpec();

        booleanTranslationSpec.init(protobufTranslationEngine);

        assertTrue(booleanTranslationSpec.isInitialized());
    }

}
