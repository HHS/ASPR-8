package gov.hhs.aspr.gcm.translation.protobuf.plugins.people.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PeoplePluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.input.PersonRangeInput;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonRange;

public class PeoplePluginDataTranslatorSpec
        extends AbstractTranslatorSpec<PeoplePluginDataInput, PeoplePluginData> {

    @Override
    protected PeoplePluginData convertInputObject(PeoplePluginDataInput inputObject) {
        PeoplePluginData.Builder builder = PeoplePluginData.builder();

        for (PersonRangeInput personRangeInput : inputObject.getPersonRangesList()) {
            PersonRange personRange = this.translator.convertInputObject(personRangeInput);
            builder.addPersonRange(personRange);
        }

        if (inputObject.hasPersonCount()) {
            builder.setPersonCount(inputObject.getPersonCount());
        }

        return builder.build();
    }

    @Override
    protected PeoplePluginDataInput convertAppObject(PeoplePluginData simObject) {
        PeoplePluginDataInput.Builder builder = PeoplePluginDataInput.newBuilder();

        for (PersonRange personRange : simObject.getPersonRanges()) {
            PersonRangeInput personRangeInput = this.translator.convertSimObject(personRange);
            builder.addPersonRanges(personRangeInput);
        }

        builder.setPersonCount(simObject.getPersonCount());

        return builder.build();
    }

    @Override
    public PeoplePluginDataInput getDefaultInstanceForInputObject() {
        return PeoplePluginDataInput.getDefaultInstance();
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
