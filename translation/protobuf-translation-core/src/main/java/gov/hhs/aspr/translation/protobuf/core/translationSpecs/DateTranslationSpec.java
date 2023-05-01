package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import java.time.LocalDate;

import com.google.type.Date;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;

public class DateTranslationSpec extends ProtobufTranslationSpec<Date, LocalDate> {

    @Override
    protected LocalDate convertInputObject(Date inputObject) {
        return LocalDate.of(inputObject.getYear(), inputObject.getMonth(), inputObject.getDay());
    }

    @Override
    protected Date convertAppObject(LocalDate appObject) {
        return Date
                .newBuilder()
                .setYear(appObject.getYear())
                .setMonth(appObject.getMonth().getValue())
                .setDay(appObject.getDayOfMonth())
                .build();
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
