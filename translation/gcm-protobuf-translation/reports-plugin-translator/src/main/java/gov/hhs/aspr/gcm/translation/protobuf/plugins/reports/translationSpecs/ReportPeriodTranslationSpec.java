package gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.input.ReportPeriodInput;
import plugins.reports.support.ReportPeriod;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain ReportPeriodInput} and
 * {@linkplain ReportPeriod}
 */
public class ReportPeriodTranslationSpec extends ProtobufTranslationSpec<ReportPeriodInput, ReportPeriod> {

    @Override
    protected ReportPeriod convertInputObject(ReportPeriodInput inputObject) {
        return ReportPeriod.valueOf(inputObject.name());
    }

    @Override
    protected ReportPeriodInput convertAppObject(ReportPeriod appObject) {
        return ReportPeriodInput.valueOf(appObject.name());
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
