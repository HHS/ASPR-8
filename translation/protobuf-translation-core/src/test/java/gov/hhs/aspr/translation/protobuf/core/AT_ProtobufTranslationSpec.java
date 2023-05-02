package gov.hhs.aspr.translation.protobuf.core;

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.google.protobuf.BoolValue;

import gov.hhs.aspr.translation.protobuf.core.translationSpecs.BooleanTranslationSpec;

public class AT_ProtobufTranslationSpec {

    @Test
    void testInit() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine.builder().build();
    
        ProtobufTranslationSpec<BoolValue, Boolean> booleanTranslationSpec = new BooleanTranslationSpec();

        booleanTranslationSpec.init(protobufTranslationEngine);

        assertTrue(booleanTranslationSpec.isInitialized());
    }
    
}
