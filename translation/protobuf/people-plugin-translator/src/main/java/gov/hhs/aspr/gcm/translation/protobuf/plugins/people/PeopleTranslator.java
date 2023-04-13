package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.protobuf.core.ProtobufTranslatorCore;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PeoplePluginDataTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PersonIdTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs.PersonRangeTranslatorSpec;

public class PeopleTranslator {

    private PeopleTranslator() {
    }

    public static Translator.Builder builder() {
        return Translator.builder()
                .setTranslatorId(PeopleTranslatorId.TRANSLATOR_ID)
                .setInitializer((translatorContext) -> {
                    translatorContext.addTranslatorSpec(new PeoplePluginDataTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonIdTranslatorSpec());
                    translatorContext.addTranslatorSpec(new PersonRangeTranslatorSpec());

                    ((ProtobufTranslatorCore.Builder) translatorContext.getTranslatorCoreBuilder())
                            .addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
