package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceReportPluginDataInput;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceId;

public class ResourceReportPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<ResourceReportPluginDataInput, ResourceReportPluginData> {

    @Override
    protected ResourceReportPluginData convertInputObject(ResourceReportPluginDataInput inputObject) {
        ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel());
        ReportPeriod reportPeriod = this.translator.convertInputObject(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod)
                .setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (ResourceIdInput resourceIdInput : inputObject.getIncludedPropertiesList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourceIdInput);
            builder.includeResource(resourceId);
        }

        for (ResourceIdInput resourceIdInput : inputObject.getExcludedPropertiesList()) {
            ResourceId resourceId = this.translator.convertInputObject(resourceIdInput);
            builder.excludeResource(resourceId);
        }

        return builder.build();
    }

    @Override
    protected ResourceReportPluginDataInput convertAppObject(ResourceReportPluginData simObject) {
        ResourceReportPluginDataInput.Builder builder = ResourceReportPluginDataInput.newBuilder();

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
    public Class<ResourceReportPluginData> getAppObjectClass() {
        return ResourceReportPluginData.class;
    }

    @Override
    public Class<ResourceReportPluginDataInput> getInputObjectClass() {
        return ResourceReportPluginDataInput.class;
    }

}
