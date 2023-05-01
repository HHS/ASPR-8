package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestField;

public class AT_TestObjectTranslatorId {

    @Test
    @UnitTestField(target = TestObjectTranslatorId.class, name = "TRANSLATOR_ID")
    public void testTranslatorId() {
        assertNotNull(TestObjectTranslatorId.TRANSLATOR_ID);
    }
}
