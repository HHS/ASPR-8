package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.support.input.PersonPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.personproperties.reports.input.PersonPropertyInteractionReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportPeriodInput;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain PersonPropertyInteractionReportPluginDataInput} and
 * {@linkplain PersonPropertyInteractionReportPluginData}
 */
public class PersonPropertyInteractionReportPluginDataTranslationSpec
        extends
        ProtobufTranslationSpec<PersonPropertyInteractionReportPluginDataInput, PersonPropertyInteractionReportPluginData> {

    @Override
    protected PersonPropertyInteractionReportPluginData convertInputObject(
            PersonPropertyInteractionReportPluginDataInput inputObject) {
        PersonPropertyInteractionReportPluginData.Builder builder = PersonPropertyInteractionReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        ReportPeriod reportPeriod = this.translationEngine.convertObject(inputObject.getReportPeriod());
        builder.setReportPeriod(reportPeriod);

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getPersonPropertyIdsList()) {
            PersonPropertyId personPropertyId = this.translationEngine.convertObject(personPropertyIdInput);
            builder.addPersonPropertyId(personPropertyId);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyInteractionReportPluginDataInput convertAppObject(
            PersonPropertyInteractionReportPluginData appObject) {
        PersonPropertyInteractionReportPluginDataInput.Builder builder = PersonPropertyInteractionReportPluginDataInput
                .newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translationEngine.convertObject(appObject.getReportPeriod());

        builder
                .setReportLabel(reportLabelInput)
                .setReportPeriod(reportPeriodInput);

        for (PersonPropertyId personPropertyId : appObject.getPersonPropertyIds()) {
            PersonPropertyIdInput personPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                    personPropertyId,
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
