package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;

import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.support.ReportLabel;

public class RegionPropertyReportPluginDataTranslatorSpec
        extends AbstractProtobufTranslatorSpec<RegionPropertyReportPluginDataInput, RegionPropertyReportPluginData> {

    @Override
    protected RegionPropertyReportPluginData convertInputObject(RegionPropertyReportPluginDataInput inputObject) {
        RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translator.convertInputObject(regionPropertyIdInput,
                    RegionPropertyId.class);
            builder.includeRegionProperty(regionPropertyId);
        }

        for (RegionPropertyIdInput regionPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            RegionPropertyId regionPropertyId = this.translator.convertInputObject(regionPropertyIdInput,
                    RegionPropertyId.class);
            builder.excludeRegionProperty(regionPropertyId);
        }

        return builder.build();
    }

    @Override
    protected RegionPropertyReportPluginDataInput convertAppObject(RegionPropertyReportPluginData simObject) {
        RegionPropertyReportPluginDataInput.Builder builder = RegionPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (RegionPropertyId regionPropertyId : simObject.getIncludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translator.convertSimObject(regionPropertyId,
                    RegionPropertyId.class);
            builder.addIncludedProperties(regionPropertyIdInput);
        }

        for (RegionPropertyId regionPropertyId : simObject.getExcludedProperties()) {
            RegionPropertyIdInput regionPropertyIdInput = this.translator.convertSimObject(regionPropertyId,
                    RegionPropertyId.class);
            builder.addExcludedProperties(regionPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public RegionPropertyReportPluginDataInput getDefaultInstanceForInputObject() {
        return RegionPropertyReportPluginDataInput.getDefaultInstance();
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
