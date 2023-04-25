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
                    ProtobufTranslationEngine.Builder coreBuilder = translatorContext.getTranslatorCoreBuilder(ProtobufTranslationEngine.Builder.class);
                    
                    coreBuilder.addTranslatorSpec(new GroupsPluginDataTranslationSpec());
                    coreBuilder.addTranslatorSpec(new GroupIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new GroupTypeIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new GroupPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestGroupTypeIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new TestGroupPropertyIdTranslationSpec());
                    coreBuilder.addTranslatorSpec(new SimpleGroupTypeIdTranslationSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new GroupPropertyReportPluginDataTranslationSpec());
                    }

                    coreBuilder.addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
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
