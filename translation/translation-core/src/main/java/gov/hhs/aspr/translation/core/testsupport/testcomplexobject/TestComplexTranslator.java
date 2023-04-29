package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;

public class TestComplexTranslator {
    private TestComplexTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestComplexTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                            .addTranslatorSpec(new TestComplexObjectTranslationSpec());
                })
                .build();
    }
}
