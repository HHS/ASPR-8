package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.support.input.GroupIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupMemberFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupPopulationReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupPropertyDimensionTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupPropertyIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupPropertyReportPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupTypeIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupTypesForPersonFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupsForPersonAndGroupTypeFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupsForPersonFilterTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.GroupsPluginDataTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.TestGroupPropertyIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.groups.translationSpecs.TestGroupTypeIdTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.PeopleTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.properties.PropertiesTranslatorId;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.ReportsTranslatorId;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;
import gov.hhs.aspr.ms.taskit.core.Translator;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationEngine;

/**
 * Translator for the Groups Plugin.
 * <li>Using this Translator will add
 * all the necessary TanslationSpecs needed to read and write
 * GroupsPluginData
 */
public class GroupsTranslator {

    private GroupsTranslator() {
    }

    protected static List<TranslationSpec<?, ?>> getTranslationSpecs() {
        List<TranslationSpec<?, ?>> list = new ArrayList<>();

        list.add(new GroupsPluginDataTranslationSpec());
        list.add(new GroupIdTranslationSpec());
        list.add(new GroupTypeIdTranslationSpec());
        list.add(new GroupPropertyIdTranslationSpec());
        list.add(new TestGroupTypeIdTranslationSpec());
        list.add(new GroupMemberFilterTranslationSpec());
        list.add(new GroupsForPersonAndGroupTypeFilterTranslationSpec());
        list.add(new GroupsForPersonFilterTranslationSpec());
        list.add(new GroupTypesForPersonFilterTranslationSpec());
        list.add(new GroupPropertyDimensionTranslationSpec());
        list.add(new TestGroupPropertyIdTranslationSpec());
        list.add(new GroupPropertyReportPluginDataTranslationSpec());
        list.add(new GroupPopulationReportPluginDataTranslationSpec());

        return list;
    }

    private static Translator.Builder builder() {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .addDependency(ReportsTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    ProtobufTranslationEngine.Builder translationEngineBuilder = translatorContext
                            .getTranslationEngineBuilder(ProtobufTranslationEngine.Builder.class);

                    for (TranslationSpec<?, ?> translationSpec : getTranslationSpecs()) {
                        translationEngineBuilder.addTranslationSpec(translationSpec);
                    }

                    translationEngineBuilder
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });

        return builder;
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
