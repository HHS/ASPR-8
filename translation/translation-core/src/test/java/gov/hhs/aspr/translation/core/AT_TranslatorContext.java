package gov.hhs.aspr.translation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.TestTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestAppObject;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;

public class AT_TranslatorContext {

    @Test
    @UnitTestConstructor(target = TranslatorContext.class, args = { TranslationController.class })
    public void testConstructor() {
        TestTranslationEngine.Builder expectedBuilder = TestTranslationEngine.builder();
        TranslationController translationController = TranslationController
                .builder()
                .setTranslationEngineBuilder(expectedBuilder)
                .build();

        TranslatorContext translatorContext = new TranslatorContext(translationController);

        assertNotNull(translatorContext);
    }

    @Test
    @UnitTestMethod(target = TranslatorContext.class, name = "getTranslationEngineBuilder", args = { Class.class })
    public void testGetTranslationEngineBuilder() {
        TestTranslationEngine.Builder expectedBuilder = TestTranslationEngine.builder();
        TranslationController translationController = TranslationController
                .builder()
                .setTranslationEngineBuilder(expectedBuilder)
                .buildWithoutInitAndChecks();

        TestTranslationEngine.Builder actualBuilder = translationController
                .getTranslationEngineBuilder(TestTranslationEngine.Builder.class);
        assertTrue(expectedBuilder == actualBuilder);

        // preconditions

        // invalid class ref
        ContractException contractException = assertThrows(ContractException.class, () -> {
            translationController.getTranslationEngineBuilder(TranslationEngine.Builder.class);
        });

        assertEquals(CoreTranslationError.INVALID_TRANSLATION_ENGINE_BUILDER, contractException.getErrorType());

        assertThrows(RuntimeException.class, () -> {
            TranslationController translationController2 = TranslationController
                    .builder()
                    .setTranslationEngineBuilder(expectedBuilder)
                    .build();

            translationController2.getTranslationEngineBuilder(TranslationEngine.Builder.class);
        });
    }

    @Test
    @UnitTestMethod(target = TranslatorContext.class, name = "addParentChildClassRelationship", args = { Class.class })
    public void testAddParentChildClassRelationship() {
        TestTranslationEngine.Builder expectedBuilder = TestTranslationEngine.builder();
        TranslationController translationController = TranslationController
                .builder()
                .setTranslationEngineBuilder(expectedBuilder)
                .build();

        TranslatorContext translatorContext = new TranslatorContext(translationController);

        translatorContext.addParentChildClassRelationship(TestAppObject.class, Object.class);

        // preconditions
        ContractException contractException = assertThrows(ContractException.class, () -> {
            translatorContext
                    .addParentChildClassRelationship(null, Object.class);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            translatorContext
                    .addParentChildClassRelationship(TestAppObject.class, null);
        });

        assertEquals(CoreTranslationError.NULL_CLASS_REF, contractException.getErrorType());

        contractException = assertThrows(ContractException.class, () -> {
            translatorContext
                    .addParentChildClassRelationship(TestAppObject.class, Object.class);
            translatorContext.addParentChildClassRelationship(TestAppObject.class, Object.class);
        });

        assertEquals(CoreTranslationError.DUPLICATE_CLASSREF, contractException.getErrorType());
    }
}
