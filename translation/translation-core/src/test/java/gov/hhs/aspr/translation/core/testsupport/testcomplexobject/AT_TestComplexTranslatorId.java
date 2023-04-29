package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AT_TestComplexTranslatorId {
    @Test
    public void testTranslatorId() {
        assertNotNull(TestComplexTranslatorId.TRANSLATOR_ID);
    }
}
