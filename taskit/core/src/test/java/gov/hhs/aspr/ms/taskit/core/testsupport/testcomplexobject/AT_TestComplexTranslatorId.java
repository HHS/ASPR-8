package gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_TestComplexTranslatorId {

    @Test
    @UnitTestField(target = TestComplexObjectTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(TestComplexObjectTranslatorId.TRANSLATOR_ID);
    }
}
