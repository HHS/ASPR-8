package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject.TestProtobufComplexTranslatorId;

public class TestProtobufObjectTranslator {
    private TestProtobufObjectTranslator() {
    }

    public static Translator getTranslator() {
        return Translator.builder()
                .setTranslatorId(TestProtobufObjectTranslatorId.TRANSLATOR_ID)
                .addDependency(TestProtobufComplexTranslatorId.TRANSLATOR_ID)
                .setInitializer(translatorContext -> {
                    // translatorContext.getTranslationEngineBuilder(TestTranslationEngine.Builder.class)
                    //         .addTranslationSpec(new TestObjectTranslationSpec());
                })
                .build();
    }
}
