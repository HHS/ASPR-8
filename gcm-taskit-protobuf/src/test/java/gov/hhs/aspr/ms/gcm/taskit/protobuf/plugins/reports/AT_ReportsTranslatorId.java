package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.reports;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_ReportsTranslatorId {

    @Test
    @UnitTestField(target = ReportsTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(ReportsTranslatorId.TRANSLATOR_ID);
    }
}
