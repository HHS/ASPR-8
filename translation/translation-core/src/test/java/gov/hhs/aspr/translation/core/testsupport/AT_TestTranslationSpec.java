package gov.hhs.aspr.translation.core.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslationSpec;

public class AT_TestTranslationSpec {
    
    @Test
    public void testInit() {
        TestTranslationEngine testTranslationEngine = TestTranslationEngine.builder().build();

        TestTranslationSpec<TestInputObject, TestAppObject> testTranslationSpec = new TestObjectTranslationSpec();

        testTranslationSpec.init(testTranslationEngine);

        assertTrue(testTranslationSpec.isInitialized());
        assertEquals(testTranslationEngine, testTranslationSpec.translationEngine);
    }
}
