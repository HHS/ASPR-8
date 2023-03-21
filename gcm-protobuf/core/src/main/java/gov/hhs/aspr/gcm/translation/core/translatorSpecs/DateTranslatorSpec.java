package gov.hhs.aspr.gcm.translation.core.translatorSpecs;

import java.time.LocalDate;

import com.google.type.Date;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;

public class DateTranslatorSpec extends AObjectTranslatorSpec<Date, LocalDate> {

    @Override
    protected LocalDate convertInputObject(Date inputObject) {
        return LocalDate.of(inputObject.getYear(), inputObject.getMonth(), inputObject.getDay());
    }

    @Override
    protected Date convertAppObject(LocalDate simObject) {
        return Date
                .newBuilder()
                .setYear(simObject.getYear())
                .setMonth(simObject.getMonth().getValue())
                .setDay(simObject.getDayOfMonth())
                .build();
    }

    @Override
    public Date getDefaultInstanceForInputObject() {
        return Date.getDefaultInstance();
    }

    @Override
    public Class<LocalDate> getAppObjectClass() {
        return LocalDate.class;
    }

    @Override
    public Class<Date> getInputObjectClass() {
        return Date.class;
    }

}
