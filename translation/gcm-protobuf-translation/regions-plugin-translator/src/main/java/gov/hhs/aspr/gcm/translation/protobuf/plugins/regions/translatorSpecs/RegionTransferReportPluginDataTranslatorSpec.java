package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionTransferReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

public class RegionTransferReportPluginDataTranslatorSpec
        extends ProtobufTranslatorSpec<RegionTransferReportPluginDataInput, RegionTransferReportPluginData> {

    @Override
    protected RegionTransferReportPluginData convertInputObject(RegionTransferReportPluginDataInput inputObject) {
        RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

        ReportLabel reportLabel = this.translatorCore.convertObject(inputObject.getReportLabel());
        ReportPeriod reportPeriod = this.translatorCore.convertObject(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod);

        return builder.build();
    }

    @Override
    protected RegionTransferReportPluginDataInput convertAppObject(RegionTransferReportPluginData appObject) {
        RegionTransferReportPluginDataInput.Builder builder = RegionTransferReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translatorCore.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        ReportPeriodInput reportPeriodInput = this.translatorCore.convertObject(appObject.getReportPeriod());
        builder.setReportLabel(reportLabelInput).setReportPeriod(reportPeriodInput);

        return builder.build();
    }

    @Override
    public Class<RegionTransferReportPluginData> getAppObjectClass() {
        return RegionTransferReportPluginData.class;
    }

    @Override
    public Class<RegionTransferReportPluginDataInput> getInputObjectClass() {
        return RegionTransferReportPluginDataInput.class;
    }

}
