package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.reports.PersonResourceReportPluginData;
import plugins.resources.support.ResourceId;

public class PersonResourceReportPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<PersonResourceReportPluginDataInput, PersonResourceReportPluginData> {

    @Override
    protected PersonResourceReportPluginData convertInputObject(PersonResourceReportPluginDataInput inputObject) {
        PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        ReportPeriod reportPeriod = this.translator.convertInputEnum(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod)
                .setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (ResourceIdInput resourceIdInput : inputObject.getIncludedPropertiesList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourceIdInput,
                    ResourceId.class);
            builder.includeResource(resourceId);
        }

        for (ResourceIdInput resourceIdInput : inputObject.getExcludedPropertiesList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourceIdInput,
                    ResourceId.class);
            builder.excludeResource(resourceId);
        }

        return builder.build();
    }

    @Override
    protected PersonResourceReportPluginDataInput convertAppObject(PersonResourceReportPluginData simObject) {
        PersonResourceReportPluginDataInput.Builder builder = PersonResourceReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translator.convertSimObject(simObject.getReportPeriod());

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportPeriod(reportPeriodInput)
                .setReportLabel(reportLabelInput);

        for (ResourceId resourceId : simObject.getIncludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translator.convertSimObject(resourceId,
            ResourceId.class);
            builder.addIncludedProperties(resourceIdInput);
        }

        for (ResourceId resourceId : simObject.getExcludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translator.convertSimObject(resourceId,
            ResourceId.class);
            builder.addExcludedProperties(resourceIdInput);
        }

        return builder.build();
    }

    @Override
    public PersonResourceReportPluginDataInput getDefaultInstanceForInputObject() {
        return PersonResourceReportPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<PersonResourceReportPluginData> getAppObjectClass() {
        return PersonResourceReportPluginData.class;
    }

    @Override
    public Class<PersonResourceReportPluginDataInput> getInputObjectClass() {
        return PersonResourceReportPluginDataInput.class;
    }

}
