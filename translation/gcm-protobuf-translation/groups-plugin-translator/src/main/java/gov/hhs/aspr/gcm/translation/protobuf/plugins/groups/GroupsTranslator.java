package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupTypeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.SimpleGroupTypeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.TestGroupPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.TestGroupTypeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class GroupsTranslator {
    
    private GroupsTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext.getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);
                    
                    translationEngineBuilder.addTranslatorSpec(new GroupsPluginDataTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new GroupIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new GroupTypeIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new GroupPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestGroupTypeIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new TestGroupPropertyIdTranslationSpec());
                    translationEngineBuilder.addTranslatorSpec(new SimpleGroupTypeIdTranslationSpec());

                    if (withReport) {
                        translationEngineBuilder.addTranslatorSpec(new GroupPropertyReportPluginDataTranslationSpec());
                    }

                    translationEngineBuilder.addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator getTranslatorWithReport() {
        return builder(true).build();
    }

    public static Translator getTranslator() {
        return builder(false).build();
    }
}
