package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.TestProtobufComplexObjectTranslatorId;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs.TestProtobufObjectTranslationSpec;

public class TestProtobufObjectTranslator {
    private TestProtobufObjectTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestProtobufObjectTranslatorId.TRANSLATOR_ID)
                .addDependency(TestProtobufComplexObjectTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class)
                            .addTranslationSpec(new TestProtobufObjectTranslationSpec());
                })
                .build();
    }
}
