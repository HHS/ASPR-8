package gov.hhs.aspr.ms.taskit.core.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.TranslationEngine;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.app.TestAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input.TestInputObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs.TestObjectTranslationSpec;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

public class AT_TestTranslationSpec {

    @Test
    @UnitTestConstructor(target = TestTranslationSpec.class, args = {})
    public void testConstructor() {
        assertNotNull(new TestTranslationSpec<TestInputObject, TestAppObject>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                return new TestAppObject();
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                return new TestInputObject();
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        });
    }

    @Test
    @UnitTestMethod(target = TestTranslationSpec.class, name = "init", args = { TranslationEngine.class })
    public void testInit() {
        TestTranslationEngine testTranslationEngine = TestTranslationEngine.builder().build();

        TestTranslationSpec<TestInputObject, TestAppObject> testTranslationSpec = new TestObjectTranslationSpec();

        testTranslationSpec.init(testTranslationEngine);

        assertTrue(testTranslationSpec.isInitialized());
        assertEquals(testTranslationEngine, testTranslationSpec.translationEngine);
    }
}
