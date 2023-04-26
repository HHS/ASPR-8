package gov.hhs.aspr.translation.core.testsupport.testobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexTranslatorId;

public class TestTranslator {
    public static Translator getTestTranslator() {
        return Translator.builder()
                .setTranslatorId(TestTranslatorId.TRANSLATOR_ID)
                .addDependency(TestComplexTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                            .addTranslatorSpec(new TestObjectTranslationSpec());
                })
                .build();
    }
}
