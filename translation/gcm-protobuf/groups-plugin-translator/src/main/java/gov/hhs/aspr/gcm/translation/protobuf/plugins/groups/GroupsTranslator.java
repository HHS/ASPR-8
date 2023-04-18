package gov.hhs.aspr.gcm.translation.protobuf.plugins.groups;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.translation.core.Translator;
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

public class GroupsTranslator {
    private GroupsTranslator() {
    }

    public static Translator.Builder builder(boolean withReport) {
        Translator.Builder builder = Translator.builder()
                .setTranslatorId(GroupsTranslatorId.TRANSLATOR_ID)
                .addDependency(PropertiesTranslatorId.TRANSLATOR_ID)
                .addDependency(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new GroupsPluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupTypeIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new GroupPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestGroupTypeIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new TestGroupPropertyIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new SimpleGroupTypeIdTranslatorSpec());

                    if (withReport) {
                        translatorContext.addTranslatorSpec(new GroupPropertyReportPluginDataTranslatorSpec());
                    }

                    translatorContext.getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class)
                            .addFieldToIncludeDefaultValue(GroupIdInput.getDescriptor().findFieldByName("id"));
                });

        if (withReport) {
            builder.addDependency(ReportsTranslatorId.TRANSLATOR_ID);
        }

        return builder;
    }

    public static Translator.Builder builder() {
        return builder(false);
    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
