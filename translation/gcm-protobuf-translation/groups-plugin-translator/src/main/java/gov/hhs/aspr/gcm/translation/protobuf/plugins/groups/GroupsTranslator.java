package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.input.GroupIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupPropertyReportPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupTypeIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.GroupsPluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.SimpleGroupTypeIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.TestGroupPropertyIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.groups.translatorSpecs.TestGroupTypeIdTranslatorSpec;
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
                    
                    coreBuilder.addTranslatorSpec(new GroupsPluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new GroupIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new GroupTypeIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new GroupPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestGroupTypeIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new TestGroupPropertyIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new SimpleGroupTypeIdTranslatorSpec());

                    if (withReport) {
                        coreBuilder.addTranslatorSpec(new GroupPropertyReportPluginDataTranslatorSpec());
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
