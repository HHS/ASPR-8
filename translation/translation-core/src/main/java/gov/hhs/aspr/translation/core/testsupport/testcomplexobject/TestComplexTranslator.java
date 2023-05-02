package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.translationSpecs.TestComplexObjectTranslationSpec;

public class TestComplexTranslator {
    private TestComplexTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestComplexTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                            .addTranslationSpec(new TestComplexObjectTranslationSpec());
                })
                .build();
    }
}
