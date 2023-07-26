package gov.hhs.aspr.ms.taskit.core.testsupport.testobject;

import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.TestComplexObjectTranslatorId;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs.TestObjectTranslationSpec;

public class TestObjectTranslator {
    private TestObjectTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestObjectTranslatorId.TRANSLATOR_ID)
                .addDependency(TestComplexObjectTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                            .addTranslationSpec(new TestObjectTranslationSpec());
                })
                .build();
    }
}
