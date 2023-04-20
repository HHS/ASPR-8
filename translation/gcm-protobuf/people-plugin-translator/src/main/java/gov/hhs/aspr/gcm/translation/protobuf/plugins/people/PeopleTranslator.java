package gov.hhs.aspr.gcm.translation.protobuf.plugins.people;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorCore;
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
                    ProtobufTranslatorCore.Builder coreBuilder = translatorContext
                            .getTranslatorCoreBuilder(ProtobufTranslatorCore.Builder.class);

                    coreBuilder.addTranslatorSpec(new PeoplePluginDataTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PersonIdTranslatorSpec());
                    coreBuilder.addTranslatorSpec(new PersonRangeTranslatorSpec());

                    coreBuilder.addFieldToIncludeDefaultValue(PersonIdInput.getDescriptor().findFieldByName("id"));
                });

    }

    public static Translator getTranslator() {
        return builder().build();
    }
}
