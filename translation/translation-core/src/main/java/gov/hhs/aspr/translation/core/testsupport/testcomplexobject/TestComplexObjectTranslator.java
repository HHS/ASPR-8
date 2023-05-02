package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.translationSpecs.TestComplexObjectTranslationSpec;

public class TestComplexObjectTranslator {
    private TestComplexObjectTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestComplexObjectTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                            .addTranslationSpec(new TestComplexObjectTranslationSpec());
                })
                .build();
    }
}
