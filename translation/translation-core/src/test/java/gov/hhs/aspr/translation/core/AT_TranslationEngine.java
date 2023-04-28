package gov.hhs.aspr.translation.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.support.TestObjectUtil;
import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexInputObject;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslationSpec;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputChildObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestInputObject;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslationSpec;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectWrapper;
import util.errors.ContractException;

public class AT_TranslationEngine {
    @Test
    public void testInit() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();
    }

    @Test
    public void testIsInitialized() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        assertFalse(testTranslationEngine.isInitialized);

        testTranslationEngine.init();

        assertTrue(testTranslationEngine.isInitialized());
    }

    @Test
    public void testTranslationSpecsAreInitialized() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        assertDoesNotThrow(() -> testTranslationEngine.translationSpecsAreInitialized());

        // preconditions
        // one or more Translation Specs are not properly initialized
        assertThrows(RuntimeException.class, () -> {
            TestTranslationEngine testTranslationEngine2 = (TestTranslationEngine) TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(new TestObjectTranslationSpec())
                    .addTranslatorSpec(complexObjectTranslationSpec)
                    .build();

            testTranslationEngine2.translationSpecsAreInitialized();
        });
    }

    @Test
    public void testConvertObject() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(expectedAppObject);

        TestInputObject actualInputObject = testTranslationEngine.convertObject(expectedAppObject);
        assertEquals(expectedInputObject, actualInputObject);

        TestAppObject actualAppObject = testTranslationEngine.convertObject(expectedInputObject);
        assertEquals(expectedAppObject, actualAppObject);

        // preconditions
        // the contract exception for CoreTranslationError.UNKNOWN_TRANSLATION_SPEC is
        // covered by the test - testGetTranslationSpecForClass
    }

    @Test
    public void testConvertObjectAsSafeClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();
        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(expectedAppObject);

        TestAppChildObject expectedAppChildObject = TestObjectUtil.getChildAppFromApp(expectedAppObject);
        TestInputChildObject expectedInputChildObject = TestObjectUtil.getChildInputFromInput(expectedInputObject);

        TestInputObject actualInputChildObject = testTranslationEngine.convertObjectAsSafeClass(expectedAppChildObject,
                TestAppObject.class);
        assertEquals(expectedInputChildObject, TestObjectUtil.getChildInputFromInput(actualInputChildObject));

        TestAppObject actualAppChildObject = testTranslationEngine.convertObjectAsSafeClass(expectedInputChildObject,
                TestInputObject.class);
        assertEquals(expectedAppChildObject, TestObjectUtil.getChildAppFromApp(actualAppChildObject));

        // preconditions
        // the contract exception for CoreTranslationError.UNKNOWN_TRANSLATION_SPEC is
        // covered by the test - testGetTranslationSpecForClass
    }

    @Test
    public void testConvertObjectAsUnsafeClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        // custom Translation Spec to simulate a Spec that might use a class to "wrap"
        // another class
        TranslationSpec<TestObjectWrapper, Object> wrapperTranslationSpec = new TranslationSpec<TestObjectWrapper, Object>() {

            @Override
            protected Object convertInputObject(TestObjectWrapper inputObject) {
                return inputObject.getWrappedObject();
            }

            @Override
            protected TestObjectWrapper convertAppObject(Object appObject) {
                TestObjectWrapper objectWrapper = new TestObjectWrapper();

                objectWrapper.setWrappedObject(appObject);

                return objectWrapper;
            }

            @Override
            public Class<Object> getAppObjectClass() {
                return Object.class;
            }

            @Override
            public Class<TestObjectWrapper> getInputObjectClass() {
                return TestObjectWrapper.class;
            }
        };

        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .addTranslatorSpec(wrapperTranslationSpec)
                .build();

        testTranslationEngine.init();

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();

        TestObjectWrapper expectedWrapper = new TestObjectWrapper();
        expectedWrapper.setWrappedObject(expectedAppObject);

        TestObjectWrapper actualWrapper = testTranslationEngine.convertObjectAsUnsafeClass(expectedAppObject,
                TestObjectWrapper.class);

        assertEquals(expectedWrapper, actualWrapper);

        Object actualAppObject = testTranslationEngine.convertObject(actualWrapper);

        assertEquals(expectedAppObject, actualAppObject);

        // preconditions
        // the contract exception for CoreTranslationError.UNKNOWN_TRANSLATION_SPEC is
        // covered by the test - testGetTranslationSpecForClass
    }

    @Test
    public void testGetTranslationSpecForClass() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        assertEquals(testObjectTranslationSpec, testTranslationEngine.getTranslationSpecForClass(TestAppObject.class));
        assertEquals(testObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestInputObject.class));

        assertNotEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestAppObject.class));
        assertNotEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestInputObject.class));

        assertEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestComplexAppObject.class));
        assertEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestComplexInputObject.class));

        assertNotEquals(testObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestComplexAppObject.class));
        assertNotEquals(testObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(TestComplexInputObject.class));

        // preconditions
        // no Translation Spec exists for the given class
        ContractException contractException = assertThrows(ContractException.class, () -> {
            testTranslationEngine.getTranslationSpecForClass(Object.class);
        });

        assertEquals(CoreTranslationError.UNKNOWN_TRANSLATION_SPEC, contractException.getErrorType());
    }

    @Test
    public void testBuild() {
        assertThrows(RuntimeException.class, () -> {
            TranslationEngine.builder().build();
        });
    }

    @Test
    public void testBuilder() {
        assertNotNull(TranslationEngine.builder());
    }

    @Test
    public void testAddTranslatorSpec() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = (TestTranslationEngine) TestTranslationEngine
                .builder()
                .addTranslatorSpec(testObjectTranslationSpec)
                .addTranslatorSpec(complexObjectTranslationSpec)
                .build();

        testTranslationEngine.init();

        // show that the translation specs are retrievable by their own app and input
        // classes
        assertEquals(testObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(testObjectTranslationSpec.getAppObjectClass()));
        assertEquals(testObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(testObjectTranslationSpec.getInputObjectClass()));

        assertEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(complexObjectTranslationSpec.getAppObjectClass()));
        assertEquals(complexObjectTranslationSpec,
                testTranslationEngine.getTranslationSpecForClass(complexObjectTranslationSpec.getInputObjectClass()));

        // preconditions
        // translationSpec is null
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(null);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_SPEC, contractException.getErrorType());

        // the translation spec getAppClass method returns null
        contractException = assertThrows(ContractException.class, () -> {
            TranslationSpec<TestObjectWrapper, Object> wrapperTranslationSpec = new TranslationSpec<TestObjectWrapper, Object>() {

                @Override
                protected Object convertInputObject(TestObjectWrapper inputObject) {
                    return inputObject.getWrappedObject();
                }

                @Override
                protected TestObjectWrapper convertAppObject(Object appObject) {
                    TestObjectWrapper objectWrapper = new TestObjectWrapper();

                    objectWrapper.setWrappedObject(appObject);

                    return objectWrapper;
                }

                @Override
                public Class<Object> getAppObjectClass() {
                    return null;
                }

                @Override
                public Class<TestObjectWrapper> getInputObjectClass() {
                    return TestObjectWrapper.class;
                }
            };
            TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(wrapperTranslationSpec);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_SPEC_APP_CLASS, contractException.getErrorType());

        // the translation spec getInputClass method returns null
        contractException = assertThrows(ContractException.class, () -> {
            TranslationSpec<TestObjectWrapper, Object> wrapperTranslationSpec = new TranslationSpec<TestObjectWrapper, Object>() {

                @Override
                protected Object convertInputObject(TestObjectWrapper inputObject) {
                    return inputObject.getWrappedObject();
                }

                @Override
                protected TestObjectWrapper convertAppObject(Object appObject) {
                    TestObjectWrapper objectWrapper = new TestObjectWrapper();

                    objectWrapper.setWrappedObject(appObject);

                    return objectWrapper;
                }

                @Override
                public Class<Object> getAppObjectClass() {
                    return Object.class;
                }

                @Override
                public Class<TestObjectWrapper> getInputObjectClass() {
                    return null;
                }
            };
            TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(wrapperTranslationSpec);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATION_SPEC_INPUT_CLASS, contractException.getErrorType());

        // if the translation spec has already been added (same, but different
        // instances)
        contractException = assertThrows(ContractException.class, () -> {
            TestObjectTranslationSpec testObjectTranslationSpec1 = new TestObjectTranslationSpec();
            TestObjectTranslationSpec testObjectTranslationSpec2 = new TestObjectTranslationSpec();

            TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(testObjectTranslationSpec1)
                    .addTranslatorSpec(testObjectTranslationSpec2);
        });

        assertEquals(CoreTranslationError.DUPLICATE_TRANSLATION_SPEC, contractException.getErrorType());

        // if the translation spec has already been added (exact same instance)
        contractException = assertThrows(ContractException.class, () -> {
            TestObjectTranslationSpec testObjectTranslationSpec1 = new TestObjectTranslationSpec();

            TestTranslationEngine
                    .builder()
                    .addTranslatorSpec(testObjectTranslationSpec1)
                    .addTranslatorSpec(testObjectTranslationSpec1);
        });

        assertEquals(CoreTranslationError.DUPLICATE_TRANSLATION_SPEC, contractException.getErrorType());

    }
}
