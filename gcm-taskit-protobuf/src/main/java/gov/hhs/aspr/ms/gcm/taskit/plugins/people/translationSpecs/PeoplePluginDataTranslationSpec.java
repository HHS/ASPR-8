package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.people.input.PersonRangeInput;
import plugins.people.datamanagers.PeoplePluginData;
import plugins.people.support.PersonRange;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PeoplePluginDataInput} and
 * {@linkplain PeoplePluginData}
 */
public class PeoplePluginDataTranslationSpec
        extends ProtobufTranslationSpec<PeoplePluginDataInput, PeoplePluginData> {

    @Override
    protected PeoplePluginData convertInputObject(PeoplePluginDataInput inputObject) {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();

        for (PersonRangeInput personRangeInput : inputObject.getPersonRangesList()) {
            PersonRange personRange = this.translationEngine.convertObject(personRangeInput);
            builder.addPersonRange(personRange);
        }

        if (inputObject.hasPersonCount()) {
            builder.setPersonCount(inputObject.getPersonCount());
        }

        return builder.build();
    }

    @Override
    protected PeoplePluginDataInput convertAppObject(PeoplePluginData appObject) {
        PeoplePluginDataInput.Builder builder = PeoplePluginDataInput.newBuilder();

        for (PersonRange personRange : appObject.getPersonRanges()) {
            PersonRangeInput personRangeInput = this.translationEngine.convertObject(personRange);
            builder.addPersonRanges(personRangeInput);
        }

        builder.setPersonCount(appObject.getPersonCount());

        return builder.build();
    }

    @Override
    public Class<PeoplePluginData> getAppObjectClass() {
        return PeoplePluginData.class;
    }

    @Override
    public Class<PeoplePluginDataInput> getInputObjectClass() {
        return PeoplePluginDataInput.class;
    }

}
