package gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.translationSpecs.TestProtobufComplexObjectTranslationSpec;

public class TestProtobufComplexObjectTranslator {
    private TestProtobufComplexObjectTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestProtobufComplexObjectTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class)
                            .addTranslationSpec(new TestProtobufComplexObjectTranslationSpec());
                })
                .build();
    }
}
