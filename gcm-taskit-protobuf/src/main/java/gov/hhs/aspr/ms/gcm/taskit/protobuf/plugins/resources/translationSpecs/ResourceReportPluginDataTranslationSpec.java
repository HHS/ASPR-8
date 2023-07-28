package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportPeriodInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.reports.input.ResourceReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.resources.support.input.ResourceIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.ResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ResourceReportPluginDataInput} and
 * {@linkplain ResourceReportPluginData}
 */
public class ResourceReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<ResourceReportPluginDataInput, ResourceReportPluginData> {

    @Override
    protected ResourceReportPluginData convertInputObject(ResourceReportPluginDataInput inputObject) {
        ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

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
    protected ResourceReportPluginDataInput convertAppObject(ResourceReportPluginData appObject) {
        ResourceReportPluginDataInput.Builder builder = ResourceReportPluginDataInput.newBuilder();

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
    public Class<ResourceReportPluginData> getAppObjectClass() {
        return ResourceReportPluginData.class;
    }

    @Override
    public Class<ResourceReportPluginDataInput> getInputObjectClass() {
        return ResourceReportPluginDataInput.class;
    }

}
