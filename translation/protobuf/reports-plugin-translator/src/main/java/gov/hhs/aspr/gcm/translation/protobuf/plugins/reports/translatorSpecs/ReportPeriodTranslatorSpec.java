package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.reports.support.ReportPeriod;

public class ReportPeriodTranslatorSpec extends AbstractTranslatorSpec<ReportPeriodInput, ReportPeriod> {

    @Override
    protected ReportPeriod convertInputObject(ReportPeriodInput inputObject) {
        return ReportPeriod.valueOf(inputObject.name());
    }

    @Override
    protected ReportPeriodInput convertAppObject(ReportPeriod simObject) {
        return ReportPeriodInput.valueOf(simObject.name());
    }

    @Override
    public ReportPeriodInput getDefaultInstanceForInputObject() {
        return ReportPeriodInput.forNumber(0);
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
