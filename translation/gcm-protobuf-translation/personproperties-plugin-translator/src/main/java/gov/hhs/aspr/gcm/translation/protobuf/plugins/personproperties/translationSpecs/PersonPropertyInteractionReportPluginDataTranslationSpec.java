package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyInteractionReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

public class PersonPropertyInteractionReportPluginDataTranslationSpec
        extends
        ProtobufTranslationSpec<PersonPropertyInteractionReportPluginDataInput, PersonPropertyInteractionReportPluginData> {

    @Override
    protected PersonPropertyInteractionReportPluginData convertInputObject(
            PersonPropertyInteractionReportPluginDataInput inputObject) {
        PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder();

        ReportLabel reportLabel = this.translationEnine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        ReportPeriod reportPeriod = this.translationEnine.convertObject(inputObject.getReportPeriod());
        builder.setReportPeriod(reportPeriod);

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getPersonPropertyIdsList()) {
            PersonPropertyId personPropertyId = this.translationEnine.convertObject(personPropertyIdInput);
            builder.addPersonPropertyId(personPropertyId);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyInteractionReportPluginDataInput convertAppObject(
            PersonPropertyInteractionReportPluginData appObject) {
        PersonPropertyInteractionReportPluginDataInput.Builder builder = PersonPropertyInteractionReportPluginDataInput
                .newBuilder();

        ReportLabelInput reportLabelInput = this.translationEnine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translationEnine.convertObject(appObject.getReportPeriod());

        builder
                .setReportLabel(reportLabelInput)
                .setReportPeriod(reportPeriodInput);

        for (PersonPropertyId personPropertyId : appObject.getPersonPropertyIds()) {
            PersonPropertyIdInput personPropertyIdInput = this.translationEnine.convertObjectAsSafeClass(personPropertyId,
                    PersonPropertyId.class);
            builder.addPersonPropertyIds(personPropertyIdInput);
        }

        return builder.build();
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