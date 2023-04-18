package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyInteractionReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

public class PersonPropertyInteractionReportPluginDataTranslatorSpec
        extends
        AbstractProtobufTranslatorSpec<PersonPropertyInteractionReportPluginDataInput, PersonPropertyInteractionReportPluginData> {

    @Override
    protected PersonPropertyInteractionReportPluginData convertInputObject(
            PersonPropertyInteractionReportPluginDataInput inputObject) {
        PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        builder.setReportLabel(reportLabel);

        ReportPeriod reportPeriod = this.translator.convertInputEnum(inputObject.getReportPeriod());
        builder.setReportPeriod(reportPeriod);

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getPersonPropertyIdsList()) {
            PersonPropertyId personPropertyId = this.translator.convertInputObject(personPropertyIdInput,
                    PersonPropertyId.class);
            builder.addPersonPropertyId(personPropertyId);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyInteractionReportPluginDataInput convertAppObject(
            PersonPropertyInteractionReportPluginData simObject) {
        PersonPropertyInteractionReportPluginDataInput.Builder builder = PersonPropertyInteractionReportPluginDataInput
                .newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translator.convertSimObject(simObject.getReportPeriod());

        builder
                .setReportLabel(reportLabelInput)
                .setReportPeriod(reportPeriodInput);

        for (PersonPropertyId personPropertyId : simObject.getPersonPropertyIds()) {
            PersonPropertyIdInput personPropertyIdInput = this.translator.convertSimObject(personPropertyId,
                    PersonPropertyId.class);
            builder.addPersonPropertyIds(personPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public PersonPropertyInteractionReportPluginDataInput getDefaultInstanceForInputObject() {
        return PersonPropertyInteractionReportPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<PersonPropertyInteractionReportPluginData> getAppObjectClass() {
        return PersonPropertyInteractionReportPluginData.class;
    }

    @Override
    public Class<PersonPropertyInteractionReportPluginDataInput> getInputObjectClass() {
        return PersonPropertyInteractionReportPluginDataInput.class;
    }

}
