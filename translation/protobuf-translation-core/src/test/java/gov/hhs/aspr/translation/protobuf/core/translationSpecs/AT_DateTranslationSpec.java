package gov.hhs.aspr.translation.protobuf.core.translationSpecs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.google.type.Date;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;

public class AT_DateTranslationSpec {

    @Test
    public void testConvertInputObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        DateTranslationSpec dateTranslationSpec = new DateTranslationSpec();
        dateTranslationSpec.init(protobufTranslationEngine);

        LocalDate expectedValue = LocalDate.now();
        Date inputValue = Date.newBuilder().setDay(expectedValue.getDayOfMonth())
                .setMonth(expectedValue.getMonthValue()).setYear(expectedValue.getYear()).build();

        LocalDate actualValue = dateTranslationSpec.convertInputObject(inputValue);

        assertEquals(expectedValue, actualValue);
    }

    @Test
    public void testConvertAppObject() {
        ProtobufTranslationEngine protobufTranslationEngine = ProtobufTranslationEngine
                .builder()
                .build();

        DateTranslationSpec dateTranslationSpec = new DateTranslationSpec();
        dateTranslationSpec.init(protobufTranslationEngine);

        LocalDate appValue = LocalDate.now();
        Date inputValue = Date.newBuilder().setDay(appValue.getDayOfMonth()).setMonth(appValue.getMonthValue())
                .setYear(appValue.getYear()).build();

        Date actualValue = dateTranslationSpec.convertAppObject(appValue);

        assertEquals(inputValue, actualValue);
    }

    @Test
    public void getAppObjectClass() {
        DateTranslationSpec dateTranslationSpec = new DateTranslationSpec();

        assertEquals(LocalDate.class, dateTranslationSpec.getAppObjectClass());
    }

    @Test
    public void getInputObjectClass() {
        DateTranslationSpec dateTranslationSpec = new DateTranslationSpec();

        assertEquals(Date.class, dateTranslationSpec.getInputObjectClass());
    }
}
