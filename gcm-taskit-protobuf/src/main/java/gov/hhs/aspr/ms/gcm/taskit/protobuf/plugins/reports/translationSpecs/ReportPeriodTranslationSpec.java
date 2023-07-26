package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports.support.input.ReportPeriodInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
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
