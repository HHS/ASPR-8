package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.reports.input.RegionPropertyReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;

import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportLabel;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionPropertyReportPluginDataInput} and
 * {@linkplain RegionPropertyReportPluginData}
 */
public class RegionPropertyReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<RegionPropertyReportPluginDataInput, RegionPropertyReportPluginData> {

    @Override
    protected RegionPropertyReportPluginData convertInputObject(RegionPropertyReportPluginDataInput inputObject) {
        RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translationEngine.convertObject(regionPropertyIdInput);
            builder.includeRegionProperty(regionPropertyId);
        }

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translationEngine.convertObject(regionPropertyIdInput);
            builder.excludeRegionProperty(regionPropertyId);
        }

        return builder.build();
    }

    @Override
    protected RegionPropertyReportPluginDataInput convertAppObject(RegionPropertyReportPluginData appObject) {
        RegionPropertyReportPluginDataInput.Builder builder = RegionPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(appObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (RegionPropertyId regionPropertyId : appObject.getIncludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                    regionPropertyId,
                    RegionPropertyId.class);
            builder.addIncludedProperties(regionPropertyIdInput);
        }

        for (RegionPropertyId regionPropertyId : appObject.getExcludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translationEngine.convertObjectAsSafeClass(
                    regionPropertyId,
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
