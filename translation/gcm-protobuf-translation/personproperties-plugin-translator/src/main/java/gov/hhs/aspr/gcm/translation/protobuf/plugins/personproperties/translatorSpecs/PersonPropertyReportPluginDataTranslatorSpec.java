package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

public class PersonPropertyReportPluginDataTranslatorSpec
        extends ProtobufTranslationSpec<PersonPropertyReportPluginDataInput, PersonPropertyReportPluginData> {

    @Override
    protected PersonPropertyReportPluginData convertInputObject(PersonPropertyReportPluginDataInput inputObject) {
        PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        ReportPeriod reportPeriod = this.translatorCore.convertObject(inputObject.getReportPeriod());
        builder.setReportPeriod(reportPeriod);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            PersonPropertyId personPropertyId = this.translatorCore.convertObject(personPropertyIdInput);
            builder.includePersonProperty(personPropertyId);
        }

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            PersonPropertyId personPropertyId = this.translatorCore.convertObject(personPropertyIdInput);
            builder.excludePersonProperty(personPropertyId);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyReportPluginDataInput convertAppObject(PersonPropertyReportPluginData appObject) {
        PersonPropertyReportPluginDataInput.Builder builder = PersonPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translatorCore.convertObject(appObject.getReportPeriod());

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput)
                .setReportPeriod(reportPeriodInput);

        for (PersonPropertyId personPropertyId : appObject.getIncludedProperties()) {
            PersonPropertyIdInput personPropertyIdInput = this.translatorCore.convertObjectAsSafeClass(personPropertyId,
                    PersonPropertyId.class);
            builder.addIncludedProperties(personPropertyIdInput);
        }

        for (PersonPropertyId personPropertyId : appObject.getExcludedProperties()) {
            PersonPropertyIdInput personPropertyIdInput = this.translatorCore.convertObjectAsSafeClass(personPropertyId,
                    PersonPropertyId.class);
            builder.addExcludedProperties(personPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public Class<PersonPropertyReportPluginData> getAppObjectClass() {
        return PersonPropertyReportPluginData.class;
    }

    @Override
    public Class<PersonPropertyReportPluginDataInput> getInputObjectClass() {
        return PersonPropertyReportPluginDataInput.class;
    }

}
