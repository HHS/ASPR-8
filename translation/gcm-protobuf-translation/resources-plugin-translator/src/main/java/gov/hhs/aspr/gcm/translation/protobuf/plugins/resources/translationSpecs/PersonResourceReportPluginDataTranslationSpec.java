package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.PersonResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.reports.PersonResourceReportPluginData;
import plugins.resources.support.ResourceId;

public class PersonResourceReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<PersonResourceReportPluginDataInput, PersonResourceReportPluginData> {

    @Override
    protected PersonResourceReportPluginData convertInputObject(PersonResourceReportPluginDataInput inputObject) {
        PersonResourceReportPluginData.Builder builder = PersonResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        ReportPeriod reportPeriod = this.translationEngine.convertObject(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod)
                .setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (ResourceIdInput resourceIdInput : inputObject.getIncludedPropertiesList()) {
            ResourceId resourceId = this.translationEngine.convertObject(resourceIdInput);
            builder.includeResource(resourceId);
        }

        for (ResourceIdInput resourceIdInput : inputObject.getExcludedPropertiesList()) {
            ResourceId resourceId = this.translationEngine.convertObject(resourceIdInput);
            builder.excludeResource(resourceId);
        }

        return builder.build();
    }

    @Override
    protected PersonResourceReportPluginDataInput convertAppObject(PersonResourceReportPluginData appObject) {
        PersonResourceReportPluginDataInput.Builder builder = PersonResourceReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translationEngine.convertObject(appObject.getReportPeriod());

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportPeriod(reportPeriodInput)
                .setReportLabel(reportLabelInput);

        for (ResourceId resourceId : appObject.getIncludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                    ResourceId.class);
            builder.addIncludedProperties(resourceIdInput);
        }

        for (ResourceId resourceId : appObject.getExcludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translationEngine.convertObjectAsSafeClass(resourceId,
                    ResourceId.class);
            builder.addExcludedProperties(resourceIdInput);
        }

        return builder.build();
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
