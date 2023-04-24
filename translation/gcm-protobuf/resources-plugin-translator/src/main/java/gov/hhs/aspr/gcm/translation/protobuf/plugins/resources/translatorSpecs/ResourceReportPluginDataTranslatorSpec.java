package gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.input.ResourceReportPluginDataInput;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.resources.reports.ResourceReportPluginData;
import plugins.resources.support.ResourceId;

public class ResourceReportPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<ResourceReportPluginDataInput, ResourceReportPluginData> {

    @Override
    protected ResourceReportPluginData convertInputObject(ResourceReportPluginDataInput inputObject) {
        ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());
        ReportPeriod reportPeriod = this.translatorCore.convertObject(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod)
                .setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (ResourceIdInput resourceIdInput : inputObject.getIncludedPropertiesList()) {
            ResourceId resourceId = this.translatorCore.convertObject(resourceIdInput);
            builder.includeResource(resourceId);
        }

        for (ResourceIdInput resourceIdInput : inputObject.getExcludedPropertiesList()) {
            ResourceId resourceId = this.translatorCore.convertObject(resourceIdInput);
            builder.excludeResource(resourceId);
        }

        return builder.build();
    }

    @Override
    protected ResourceReportPluginDataInput convertAppObject(ResourceReportPluginData appObject) {
        ResourceReportPluginDataInput.Builder builder = ResourceReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);
        ReportPeriodInput reportPeriodInput = this.translatorCore.convertObject(appObject.getReportPeriod());

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportPeriod(reportPeriodInput)
                .setReportLabel(reportLabelInput);

        for (ResourceId resourceId : appObject.getIncludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translatorCore.convertObjectAsSafeClass(resourceId,
                    ResourceId.class);
            builder.addIncludedProperties(resourceIdInput);
        }

        for (ResourceId resourceId : appObject.getExcludedResourceIds()) {
            ResourceIdInput resourceIdInput = this.translatorCore.convertObjectAsSafeClass(resourceId,
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
