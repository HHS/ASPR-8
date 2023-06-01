package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupMemberFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupTypeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupTypesForPersonFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupsForPersonAndGroupTypeFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupsForPersonFilterTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.GroupsPluginDataTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.TestGroupPropertyIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translationSpecs.TestGroupTypeIdTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

/**
 * Translator for the Groups Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * GroupsPluginData
 */
public class GroupsTranslator {

    private GroupsTranslator() {
    }

    private static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    translationEngineBuilder
                            .addTranslationSpec(new GroupsPluginDataTranslationSpec())
                            .addTranslationSpec(new GroupIdTranslationSpec())
                            .addTranslationSpec(new GroupTypeIdTranslationSpec())
                            .addTranslationSpec(new GroupPropertyIdTranslationSpec())
                            .addTranslationSpec(new TestGroupTypeIdTranslationSpec())
                            .addTranslationSpec(new GroupMemberFilterTranslationSpec())
                            .addTranslationSpec(new GroupsForPersonAndGroupTypeFilterTranslationSpec())
                            .addTranslationSpec(new GroupsForPersonFilterTranslationSpec())
                            .addTranslationSpec(new GroupTypesForPersonFilterTranslationSpec())
                            .addTranslationSpec(new TestGroupPropertyIdTranslationSpec());

                    if (withReport) {
                        translationEngineBuilder.addTranslationSpec(new GroupPropertyReportPluginDataTranslationSpec());
                    }

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
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
