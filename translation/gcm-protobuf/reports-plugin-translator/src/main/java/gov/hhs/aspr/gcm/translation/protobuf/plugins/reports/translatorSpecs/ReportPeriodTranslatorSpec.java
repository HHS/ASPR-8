package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.reports.support.ReportPeriod;

public class ReportPeriodTranslatorSpec extends ProtobufTranslatorSpec<ReportPeriodInput, ReportPeriod> {

    @Override
    protected ReportPeriod convertInputObject(ReportPeriodInput inputObject) {
        return ReportPeriod.valueOf(inputObject.name());
    }

    @Override
    protected ReportPeriodInput convertAppObject(ReportPeriod simObject) {
        return ReportPeriodInput.valueOf(simObject.name());
    }

    @Override
    public Class<ReportPeriod> getAppObjectClass() {
        return ReportPeriod.class;
    }

    @Override
    public Class<ReportPeriodInput> getInputObjectClass() {
        return ReportPeriodInput.class;
    }

}
