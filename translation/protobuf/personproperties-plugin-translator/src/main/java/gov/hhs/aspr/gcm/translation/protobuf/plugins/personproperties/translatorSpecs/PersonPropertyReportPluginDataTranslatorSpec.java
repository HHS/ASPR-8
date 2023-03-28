package gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.personproperties.input.PersonPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

public class PersonPropertyReportPluginDataTranslatorSpec
        extends AbstractTranslatorSpec<PersonPropertyReportPluginDataInput, PersonPropertyReportPluginData> {

    @Override
    protected PersonPropertyReportPluginData convertInputObject(PersonPropertyReportPluginDataInput inputObject) {
        PersonPropertyReportPluginData.Builder builder = PersonPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        builder.setReportLabel(reportLabel);

        ReportPeriod reportPeriod = this.translator.convertInputEnum(inputObject.getReportPeriod());
        builder.setReportPeriod(reportPeriod);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            PersonPropertyId personPropertyId = this.translator.convertInputObject(personPropertyIdInput,
                    PersonPropertyId.class);
            builder.includePersonProperty(personPropertyId);
        }

        for (PersonPropertyIdInput personPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            PersonPropertyId personPropertyId = this.translator.convertInputObject(personPropertyIdInput,
                    PersonPropertyId.class);
            builder.excludePersonProperty(personPropertyId);
        }

        return builder.build();
    }

    @Override
    protected PersonPropertyReportPluginDataInput convertAppObject(PersonPropertyReportPluginData simObject) {
        PersonPropertyReportPluginDataInput.Builder builder = PersonPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translator.convertSimObject(simObject.getReportPeriod());

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput)
                .setReportPeriod(reportPeriodInput);

        for (PersonPropertyId personPropertyId : simObject.getIncludedProperties()) {
            PersonPropertyIdInput personPropertyIdInput = this.translator.convertSimObject(personPropertyId,
                    PersonPropertyId.class);
            builder.addIncludedProperties(personPropertyIdInput);
        }

        for (PersonPropertyId personPropertyId : simObject.getExcludedProperties()) {
            PersonPropertyIdInput personPropertyIdInput = this.translator.convertSimObject(personPropertyId,
                    PersonPropertyId.class);
            builder.addExcludedProperties(personPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public PersonPropertyReportPluginDataInput getDefaultInstanceForInputObject() {
        return PersonPropertyReportPluginDataInput.getDefaultInstance();
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