package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;

import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportLabel;

public class RegionPropertyReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<RegionPropertyReportPluginDataInput, RegionPropertyReportPluginData> {

    @Override
    protected RegionPropertyReportPluginData convertInputObject(RegionPropertyReportPluginDataInput inputObject) {
        RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translationEnine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translationEnine.convertObject(regionPropertyIdInput);
            builder.includeRegionProperty(regionPropertyId);
        }

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translationEnine.convertObject(regionPropertyIdInput);
            builder.excludeRegionProperty(regionPropertyId);
        }

        return builder.build();
    }

    @Override
    protected RegionPropertyReportPluginDataInput convertAppObject(RegionPropertyReportPluginData appObject) {
        RegionPropertyReportPluginDataInput.Builder builder = RegionPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEnine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (RegionPropertyId regionPropertyId : appObject.getIncludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translationEnine.convertObjectAsSafeClass(regionPropertyId,
                    RegionPropertyId.class);
            builder.addIncludedProperties(regionPropertyIdInput);
        }

        for (RegionPropertyId regionPropertyId : appObject.getExcludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translationEnine.convertObjectAsSafeClass(regionPropertyId,
                    RegionPropertyId.class);
            builder.addExcludedProperties(regionPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public Class<RegionPropertyReportPluginData> getAppObjectClass() {
        return RegionPropertyReportPluginData.class;
    }

    @Override
    public Class<RegionPropertyReportPluginDataInput> getInputObjectClass() {
        return RegionPropertyReportPluginDataInput.class;
    }

}
