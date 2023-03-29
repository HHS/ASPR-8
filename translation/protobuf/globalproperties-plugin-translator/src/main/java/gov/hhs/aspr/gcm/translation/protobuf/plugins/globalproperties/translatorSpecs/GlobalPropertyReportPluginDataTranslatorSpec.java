package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportLabelInput;
import plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.reports.support.ReportLabel;

public class GlobalPropertyReportPluginDataTranslatorSpec
        extends AbstractTranslatorSpec<GlobalPropertyReportPluginDataInput, GlobalPropertyReportPluginData> {

    @Override
    protected GlobalPropertyReportPluginData convertInputObject(GlobalPropertyReportPluginDataInput inputObject) {
        GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

        ReportLabel reportLabel = this.translator.convertInputObject(inputObject.getReportLabel(), ReportLabel.class);
        builder.setReportLabel(reportLabel);

        builder.setDefaultInclusion(inputObject.getDefaultInclusionPolicy());

        for (GlobalPropertyIdInput GlobalPropertyIdInput : inputObject.getIncludedPropertiesList()) {
            GlobalPropertyId GlobalPropertyId = this.translator.convertInputObject(GlobalPropertyIdInput,
                    GlobalPropertyId.class);
            builder.includeGlobalProperty(GlobalPropertyId);
        }

        for (GlobalPropertyIdInput GlobalPropertyIdInput : inputObject.getExcludedPropertiesList()) {
            GlobalPropertyId GlobalPropertyId = this.translator.convertInputObject(GlobalPropertyIdInput,
                    GlobalPropertyId.class);
            builder.excludeGlobalProperty(GlobalPropertyId);
        }

        return builder.build();
    }

    @Override
    protected GlobalPropertyReportPluginDataInput convertAppObject(GlobalPropertyReportPluginData simObject) {
        GlobalPropertyReportPluginDataInput.Builder builder = GlobalPropertyReportPluginDataInput.newBuilder();

        ReportLabelInput reportLabelInput = this.translator.convertSimObject(simObject.getReportLabel(),
                ReportLabel.class);

        builder
                .setDefaultInclusionPolicy(simObject.getDefaultInclusionPolicy())
                .setReportLabel(reportLabelInput);

        for (GlobalPropertyId GlobalPropertyId : simObject.getIncludedProperties()) {
            GlobalPropertyIdInput GlobalPropertyIdInput = this.translator.convertSimObject(GlobalPropertyId,
                    GlobalPropertyId.class);
            builder.addIncludedProperties(GlobalPropertyIdInput);
        }

        for (GlobalPropertyId GlobalPropertyId : simObject.getExcludedProperties()) {
            GlobalPropertyIdInput GlobalPropertyIdInput = this.translator.convertSimObject(GlobalPropertyId,
                    GlobalPropertyId.class);
            builder.addExcludedProperties(GlobalPropertyIdInput);
        }

        return builder.build();
    }

    @Override
    public GlobalPropertyReportPluginDataInput getDefaultInstanceForInputObject() {
        return GlobalPropertyReportPluginDataInput.getDefaultInstance();
    }

    @Override
    public Class<GlobalPropertyReportPluginData> getAppObjectClass() {
        return GlobalPropertyReportPluginData.class;
    }

    @Override
    public Class<GlobalPropertyReportPluginDataInput> getInputObjectClass() {
        return GlobalPropertyReportPluginDataInput.class;
    }

}
