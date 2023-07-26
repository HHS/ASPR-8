package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.translationSpecs.PropertyDefinitionTranslationSpec;
import gov.hhs.aspr.translation.core.TranslationSpec;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Properties Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * PropertiesPlugin (PropertyDefiniton, TimeTrackingPolicy)
 */
public class PropertiesTranslator {

    private PropertiesTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new PropertyDefinitionTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(PropertiesTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
