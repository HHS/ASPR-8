package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.reports.input.RegionTransferReportPluginDataInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportLabelInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportPeriodInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionTransferReportPluginDataInput} and
 * {@linkplain RegionTransferReportPluginData}
 */
public class RegionTransferReportPluginDataTranslationSpec
        extends ProtobufTranslationSpec<RegionTransferReportPluginDataInput, RegionTransferReportPluginData> {

    @Override
    protected RegionTransferReportPluginData convertInputObject(RegionTransferReportPluginDataInput inputObject) {
        RegionTransferReportPluginData.Builder builder = RegionTransferReportPluginData.builder();

        ReportLabel reportLabel = this.translationEngine.convertObject(inputObject.getReportLabel());
        ReportPeriod reportPeriod = this.translationEngine.convertObject(inputObject.getReportPeriod());

        builder.setReportLabel(reportLabel).setReportPeriod(reportPeriod);

        return builder.build();
    }

    @Override
    protected RegionTransferReportPluginDataInput convertAppObject(RegionTransferReportPluginData appObject) {
        RegionTransferReportPluginDataInput.Builder builder = RegionTransferReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translationEngine.convertObjectAsSafeClass(appObject.getReportLabel(),
                ReportLabel.class);

        ReportPeriodInput reportPeriodInput = this.translationEngine.convertObject(appObject.getReportPeriod());
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
