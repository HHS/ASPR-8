package gov.hhs.aspr.ms.taskit.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.taskit.core.testsupport.TestObjectUtil;
import gov.hhs.aspr.ms.taskit.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.TestComplexInputObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testcomplexobject.translationSpecs.TestComplexObjectTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppChildObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestAppObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestInputChildObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.TestInputObject;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.translationSpecs.TestObjectTranslationSpec;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_TranslationSpec {

    @Test
    @UnitTestConstructor(target = TranslationSpec.class, args = {})
    public void testConstructor() {
        TranslationSpec<TestInputObject, TestAppObject> translationSpec = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        assertNotNull(translationSpec);
    }

    @Test
    @UnitTestMethod(target = TranslationSpec.class, name = "init", args = { TranslationEngine.class })
    public void testInit() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .build();

        testObjectTranslationSpec.init(testTranslationEngine);

        assertTrue(testObjectTranslationSpec.isInitialized());

    }

    @Test
    @UnitTestMethod(target = TranslationSpec.class, name = "isInitialized", args = {})
    public void testIsInitialized() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .build();

        testObjectTranslationSpec.init(testTranslationEngine);

        assertTrue(testObjectTranslationSpec.isInitialized());

        assertFalse(new TestObjectTranslationSpec().isInitialized());
    }

    @Test
    @UnitTestMethod(target = TranslationSpec.class, name = "convert", args = { Object.class })
    public void testConvert() {
        TestObjectTranslationSpec testObjectTranslationSpec = new TestObjectTranslationSpec();
        TestComplexObjectTranslationSpec complexObjectTranslationSpec = new TestComplexObjectTranslationSpec();
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .addTranslationSpec(testObjectTranslationSpec)
                .addTranslationSpec(complexObjectTranslationSpec)
                .build();

        testObjectTranslationSpec.init(testTranslationEngine);
        complexObjectTranslationSpec.init(testTranslationEngine);

        TestAppObject expectedAppObject = TestObjectUtil.generateTestAppObject();
        TestInputObject expectedInputObject = TestObjectUtil.getInputFromApp(expectedAppObject);

        TestInputObject actualInputObject = testObjectTranslationSpec.convert(expectedAppObject);
        assertEquals(expectedInputObject, actualInputObject);

        TestAppObject actualAppObject = testObjectTranslationSpec.convert(expectedInputObject);
        assertEquals(expectedAppObject, actualAppObject);

        TestAppChildObject expectedAppChildObject = TestObjectUtil.getChildAppFromApp(expectedAppObject);
        TestInputChildObject expectedInputChildObject = TestObjectUtil.getChildInputFromInput(expectedInputObject);

        TestInputObject actualInputChildObject = testObjectTranslationSpec.convert(expectedAppChildObject);
        assertEquals(expectedInputChildObject, TestObjectUtil.getChildInputFromInput(actualInputChildObject));

        TestAppObject actualAppChildObject = testObjectTranslationSpec.convert(expectedInputChildObject);
        assertEquals(expectedAppChildObject, TestObjectUtil.getChildAppFromApp(actualAppChildObject));

        // precondition
        // TranslationSpec not intialized
        ContractException contractException = assertThrows(ContractException.class, () -> {
            TestObjectTranslationSpec testObjectTranslationSpec2 = new TestObjectTranslationSpec();
            testObjectTranslationSpec2.convert(new TestAppObject());
        });

        assertEquals(CoreTranslationError.UNITIALIZED_TRANSLATION_SPEC, contractException.getErrorType());

        // unknown object
        contractException = assertThrows(ContractException.class, () -> {
            TestObjectTranslationSpec testObjectTranslationSpec2 = new TestObjectTranslationSpec();
            testObjectTranslationSpec2.init(testTranslationEngine);
            testObjectTranslationSpec2.convert(new TestComplexInputObject());
        });

        assertEquals(CoreTranslationError.UNKNOWN_OBJECT, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = TranslationSpec.class, name = "hashCode", args = {})
    public void testHashCode() {
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .build();
        // base
        TranslationSpec<TestInputObject, TestAppObject> translationSpecA = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // same input class, different app class
        TranslationSpec<TestInputObject, TestAppChildObject> translationSpecB = new TranslationSpec<>() {

            @Override
            protected TestAppChildObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppChildObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppChildObject> getAppObjectClass() {
                return TestAppChildObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // same app class, different input class
        TranslationSpec<TestInputChildObject, TestAppObject> translationSpecC = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputChildObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputChildObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputChildObject> getInputObjectClass() {
                return TestInputChildObject.class;
            }

        };

        // different app and different input class
        TranslationSpec<TestInputChildObject, TestAppChildObject> translationSpecD = new TranslationSpec<>() {

            @Override
            protected TestAppChildObject convertInputObject(TestInputChildObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputChildObject convertAppObject(TestAppChildObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppChildObject> getAppObjectClass() {
                return TestAppChildObject.class;
            }

            @Override
            public Class<TestInputChildObject> getInputObjectClass() {
                return TestInputChildObject.class;
            }

        };

        // duplicate of the base
        TranslationSpec<TestInputObject, TestAppObject> translationSpecE = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // init the duplicate base
        translationSpecE.init(testTranslationEngine);

        // same exact object should be equal
        assertEquals(translationSpecA.hashCode(), translationSpecA.hashCode());

        // different types of objects should not be equal
        assertNotEquals(translationSpecA.hashCode(), new Object().hashCode());

        // different app class should not be equal
        assertNotEquals(translationSpecA.hashCode(), translationSpecB.hashCode());

        // different input class should not be equal
        assertNotEquals(translationSpecA.hashCode(), translationSpecC.hashCode());

        // different input and different app class should not be equal
        assertNotEquals(translationSpecA.hashCode(), translationSpecD.hashCode());

        // if one is initialized and the other is not, they should not be equal
        assertNotEquals(translationSpecA.hashCode(), translationSpecE.hashCode());

        // init base
        translationSpecA.init(testTranslationEngine);

        // if all above are equal, then the two specs are equal
        assertEquals(translationSpecA.hashCode(), translationSpecE.hashCode());
    }

    @Test
    @UnitTestMethod(target = TranslationSpec.class, name = "equals", args = { Object.class })
    public void testEquals() {
        TestTranslationEngine testTranslationEngine = TestTranslationEngine
                .builder()
                .build();
        // base
        TranslationSpec<TestInputObject, TestAppObject> translationSpecA = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // same input class, different app class
        TranslationSpec<TestInputObject, TestAppChildObject> translationSpecB = new TranslationSpec<>() {

            @Override
            protected TestAppChildObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppChildObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppChildObject> getAppObjectClass() {
                return TestAppChildObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // same app class, different input class
        TranslationSpec<TestInputChildObject, TestAppObject> translationSpecC = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputChildObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputChildObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputChildObject> getInputObjectClass() {
                return TestInputChildObject.class;
            }

        };

        // different app and different input class
        TranslationSpec<TestInputChildObject, TestAppChildObject> translationSpecD = new TranslationSpec<>() {

            @Override
            protected TestAppChildObject convertInputObject(TestInputChildObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputChildObject convertAppObject(TestAppChildObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppChildObject> getAppObjectClass() {
                return TestAppChildObject.class;
            }

            @Override
            public Class<TestInputChildObject> getInputObjectClass() {
                return TestInputChildObject.class;
            }

        };

        // duplicate of the base
        TranslationSpec<TestInputObject, TestAppObject> translationSpecE = new TranslationSpec<>() {

            @Override
            protected TestAppObject convertInputObject(TestInputObject inputObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertInputObject'");
            }

            @Override
            protected TestInputObject convertAppObject(TestAppObject appObject) {
                throw new UnsupportedOperationException("Unimplemented method 'convertAppObject'");
            }

            @Override
            public Class<TestAppObject> getAppObjectClass() {
                return TestAppObject.class;
            }

            @Override
            public Class<TestInputObject> getInputObjectClass() {
                return TestInputObject.class;
            }

        };

        // init the duplicate base
        translationSpecE.init(testTranslationEngine);

        // same exact object should be equal
        assertEquals(translationSpecA, translationSpecA);

        // null object should not be equal
        assertNotEquals(translationSpecA, null);

        // different types of objects should not be equal
        assertNotEquals(translationSpecA, new Object());

        // different app class should not be equal
        assertNotEquals(translationSpecA, translationSpecB);

        // different input class should not be equal
        assertNotEquals(translationSpecA, translationSpecC);

        // different input and different app class should not be equal
        assertNotEquals(translationSpecA, translationSpecD);

        // if one is initialized and the other is not, they should not be equal
        assertNotEquals(translationSpecA, translationSpecE);

        // init base
        translationSpecA.init(testTranslationEngine);

        // if all above are equal, then the two specs are equal
        assertEquals(translationSpecA, translationSpecE);
    }
}
