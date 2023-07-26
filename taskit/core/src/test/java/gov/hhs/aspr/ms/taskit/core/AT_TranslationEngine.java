package gov.hhs.aspr.ms.taskit.core;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.ms.taskit.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.TestComplexAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.input.TestComplexInputObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.translationSpecs.TestComplexObjectTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppChildObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestObjectWrapper;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input.TestInputChildObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.input.TestInputObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs.TestObjectTranslationSpec;
import util.annotations.UnitTestForCoverage;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_TranslationEngine {

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "init", args = {})
        public void testInit() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                testTranslationEngine.init();
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "isInitialized", args = {})
        public void testIsInitialized() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                assertFalse(testTranslationEngine.isInitialized);

                testTranslationEngine.init();

                assertTrue(testTranslationEngine.isInitialized());
        }

        @Test
        @UnitTestForCoverage
        public void testTranslationSpecsAreInitialized() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                testTranslationEngine.init();

                assertDoesNotThrow(() -> testTranslationEngine.translationSpecsAreInitialized());

                // preconditions
                // one or more Translation Specs are not properly initialized
                assertThrows(RuntimeException.class, () -> {
                        TestTranslationEngine testTranslationEngine2 = TestTranslationEngine
                                        .builder()
                                        .addTranslationSpec(new TestObjectTranslationSpec())
                                        .addTranslationSpec(testComplexObjectTranslationSpec)
                                        .build();

                        testTranslationEngine2.translationSpecsAreInitialized();
                });
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "getTranslationSpecs", args = {})
        public void testGetTranslationSpecs() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                Set<BaseTranslationSpec> actualTranslationSpecs = testTranslationEngine.getTranslationSpecs();

                assertTrue(actualTranslationSpecs.contains(testObjectTranslationSpec));
                assertTrue(actualTranslationSpecs.contains(testComplexObjectTranslationSpec));
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "convertObject", args = { Object.class })
        public void testConvertObject() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
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

                // the passed in object is null
                ContractException contractException = assertThrows(ContractException.class, () -> {
                        testTranslationEngine.convertObject(null);
                });

                assertEquals(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION, contractException.getErrorType());
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "convertObjectAsSafeClass", args = { Object.class,
                        Class.class })
        public void testConvertObjectAsSafeClass() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                testTranslationEngine.init();
                TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();
                TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(expectedAppObject);

                TestAppChildObject expectedAppChildObject = TestObjectUtil.getChildAppFromApp(expectedAppObject);
                TestInputChildObject expectedInputChildObject = TestObjectUtil
                                .getChildInputFromInput(expectedInputObject);

                TestInputObject actualInputChildObject = testTranslationEngine.convertObjectAsSafeClass(
                                expectedAppChildObject,
                                TestAppObject.class);
                assertEquals(expectedInputChildObject, TestObjectUtil.getChildInputFromInput(actualInputChildObject));

                TestAppObject actualAppChildObject = testTranslationEngine.convertObjectAsSafeClass(
                                expectedInputChildObject,
                                TestInputObject.class);
                assertEquals(expectedAppChildObject, TestObjectUtil.getChildAppFromApp(actualAppChildObject));

                // preconditions
                // the contract exception for CoreTranslationError.UNKNOWN_TRANSLATION_SPEC is
                // covered by the test - testGetTranslationSpecForClass

                // the passed in object is null
                ContractException contractException = assertThrows(ContractException.class, () -> {
                        testTranslationEngine.convertObjectAsSafeClass(null, Object.class);
                });

                assertEquals(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION, contractException.getErrorType());

                // the passed in parentClassRef is null
                contractException = assertThrows(ContractException.class, () -> {
                        testTranslationEngine.convertObjectAsSafeClass(expectedAppChildObject, null);
                });

                assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "convertObjectAsUnsafeClass", args = { Object.class,
                        Class.class })
        public void testConvertObjectAsUnsafeClass() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
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

                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .addTranslationSpec(wrapperTranslationSpec)
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

                // the passed in object is null
                ContractException contractException = assertThrows(ContractException.class, () -> {
                        testTranslationEngine.convertObjectAsUnsafeClass(null, Object.class);
                });

                assertEquals(CoreTranslationError.NULL_OBJECT_FOR_TRANSLATION, contractException.getErrorType());

                // the passed in parentClassRef is null
                contractException = assertThrows(ContractException.class, () -> {
                        testTranslationEngine.convertObjectAsUnsafeClass(expectedAppObject, null);
                });

                assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());
        }

        @Test
        @UnitTestForCoverage
        public void testGetTranslationSpecForClass() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                testTranslationEngine.init();

                assertEquals(testObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(TestAppObject.class));
                assertEquals(testObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(TestInputObject.class));

                assertNotEquals(testComplexObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(TestAppObject.class));
                assertNotEquals(testComplexObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(TestInputObject.class));

                assertEquals(testComplexObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(TestComplexAppObject.class));
                assertEquals(testComplexObjectTranslationSpec,
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
        @UnitTestMethod(target = TranslationEngine.class, name = "builder", args = {})
        public void testBuilder() {
                assertNotNull(TranslationEngine.builder());
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.Builder.class, name = "build", args = {})
        public void testBuild() {
                assertThrows(RuntimeException.class, () -> {
                        TranslationEngine.builder().build();
                });
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.Builder.class, name = "addTranslationSpec", args = {
                        TranslationSpec.class })
        public void testAddTranslationSpec() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                testTranslationEngine.init();

                // show that the translation specs are retrievable by their own app and input
                // classes
                assertEquals(testObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(
                                                testObjectTranslationSpec.getAppObjectClass()));
                assertEquals(testObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(
                                                testObjectTranslationSpec.getInputObjectClass()));

                assertEquals(testComplexObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(
                                                testComplexObjectTranslationSpec.getAppObjectClass()));
                assertEquals(testComplexObjectTranslationSpec,
                                testTranslationEngine.getTranslationSpecForClass(
                                                testComplexObjectTranslationSpec.getInputObjectClass()));

                // preconditions
                // translationSpec is null
                ContractException contractException = assertThrows(ContractException.class, () -> {
                        TestTranslationEngine
                                        .builder()
                                        .addTranslationSpec(null);
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
                                        .addTranslationSpec(wrapperTranslationSpec);
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
                                        .addTranslationSpec(wrapperTranslationSpec);
                });

                assertEquals(CoreTranslationError.NULL_TRANSLATION_SPEC_INPUT_CLASS, contractException.getErrorType());

                // if the translation spec has already been added (same, but different
                // instances)
                contractException = assertThrows(ContractException.class, () -> {
                        TestObjectTranslationSpec testObjectTranslationSpec1 = new TestObjectTranslationSpec();
                        TestObjectTranslationSpec testObjectTranslationSpec2 = new TestObjectTranslationSpec();

                        TestTranslationEngine
                                        .builder()
                                        .addTranslationSpec(testObjectTranslationSpec1)
                                        .addTranslationSpec(testObjectTranslationSpec2);
                });

                assertEquals(CoreTranslationError.DUPLICATE_TRANSLATION_SPEC, contractException.getErrorType());

                // if the translation spec has already been added (exact same instance)
                contractException = assertThrows(ContractException.class, () -> {
                        TestObjectTranslationSpec testObjectTranslationSpec1 = new TestObjectTranslationSpec();

                        TestTranslationEngine
                                        .builder()
                                        .addTranslationSpec(testObjectTranslationSpec1)
                                        .addTranslationSpec(testObjectTranslationSpec1);
                });

                assertEquals(CoreTranslationError.DUPLICATE_TRANSLATION_SPEC, contractException.getErrorType());
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "hashCode", args = {})
        public void testHashCode() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine1 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine2 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine3 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine4 = TestTranslationEngine
                                .builder()
                                .build();

                TestTranslationEngine testTranslationEngine5 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                // exact same, same hash code
                assertEquals(testTranslationEngine1.hashCode(), testTranslationEngine1.hashCode());

                // different translation specs
                assertNotEquals(testTranslationEngine1.hashCode(), testTranslationEngine2.hashCode());
                assertNotEquals(testTranslationEngine1.hashCode(), testTranslationEngine3.hashCode());
                assertNotEquals(testTranslationEngine1.hashCode(), testTranslationEngine4.hashCode());
                assertNotEquals(testTranslationEngine2.hashCode(), testTranslationEngine3.hashCode());
                assertNotEquals(testTranslationEngine2.hashCode(), testTranslationEngine4.hashCode());
                assertNotEquals(testTranslationEngine3.hashCode(), testTranslationEngine4.hashCode());

                // same translation specs, but initialized vs not
                testTranslationEngine5.init();
                assertNotEquals(testTranslationEngine1.hashCode(), testTranslationEngine5.hashCode());

                // same translation specs and both initialized
                testTranslationEngine1.init();
                assertEquals(testTranslationEngine1.hashCode(), testTranslationEngine5.hashCode());
        }

        @Test
        @UnitTestMethod(target = TranslationEngine.class, name = "equals", args = { Object.class })
        public void testEquals() {
                TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
                TestComplexObjectTranslationSpec testComplexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
                TestTranslationEngine testTranslationEngine1 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine2 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine3 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .build();

                TestTranslationEngine testTranslationEngine4 = TestTranslationEngine
                                .builder()
                                .build();

                TestTranslationEngine testTranslationEngine5 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec)
                                .addTranslationSpec(testComplexObjectTranslationSpec)
                                .build();

                TestObjectTranslationSpec testObjectTranslationSpec2 = new TestObjectTranslationSpec();
                TestObjectTranslationSpec testObjectTranslationSpec3 = new TestObjectTranslationSpec();

                TestTranslationEngine testTranslationEngine6 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec2)
                                .build();

                TestTranslationEngine testTranslationEngine7 = TestTranslationEngine
                                .builder()
                                .addTranslationSpec(testObjectTranslationSpec3)
                                .build();

                // exact same
                assertEquals(testTranslationEngine1, testTranslationEngine1);

                assertNotEquals(testTranslationEngine1, null);

                assertNotEquals(testTranslationEngine1, new Object());

                // different translation specs
                assertNotEquals(testTranslationEngine1, testTranslationEngine2);
                assertNotEquals(testTranslationEngine1, testTranslationEngine3);
                assertNotEquals(testTranslationEngine1, testTranslationEngine4);
                assertNotEquals(testTranslationEngine2, testTranslationEngine3);
                assertNotEquals(testTranslationEngine2, testTranslationEngine4);
                assertNotEquals(testTranslationEngine3, testTranslationEngine4);

                testObjectTranslationSpec2.init(testTranslationEngine1);
                testObjectTranslationSpec3.init(testTranslationEngine5);
                assertNotEquals(testTranslationEngine6, testTranslationEngine7);

                // same translation specs, but initialized vs not
                testTranslationEngine5.init();
                assertNotEquals(testTranslationEngine1, testTranslationEngine5);

                // same translation specs and both initialized
                testTranslationEngine1.init();
                assertEquals(testTranslationEngine1, testTranslationEngine5);

                TranslationEngine.Data data = new TranslationEngine.Data();
                assertEquals(data, data);
                assertNotEquals(data, null);
                assertNotEquals(data, new Object());
        }
}
