package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class AT_TestObjectTranslatorId {

    @Test
    public void testTranslatorId() {
        assertNotNull(TestObjectTranslatorId.TRANSLATOR_ID);
    }
}
