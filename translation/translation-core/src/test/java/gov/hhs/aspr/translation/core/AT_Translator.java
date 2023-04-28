package gov.hhs.aspr.translation.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexTranslatorId;
import gov.hhs.aspr.translation.core.testsupport.testobject.TestObjectTranslatorId;
import util.errors.ContractException;

public class AT_Translator {

    @Test
    public void testGetInitializer() {
        Consumer<TranslatorContext> expectedInitializer = (translatorConext) -> {
        };
        Translator testTranslator = Translator.builder()
                .setInitializer(expectedInitializer)
                .setTranslatorId(new TranslatorId() {
                })
                .build();

        assertEquals(expectedInitializer, testTranslator.getInitializer());
    }

    @Test
    public void testGetTranslatorId() {
        TranslatorId expectedTranslatorId = new TranslatorId() {
        };
        Translator testTranslator = Translator.builder()
                .setInitializer((translatorConext) -> {
                })
                .setTranslatorId(expectedTranslatorId)
                .build();

        assertEquals(expectedTranslatorId, testTranslator.getTranslatorId());
    }

    @Test
    public void testGetDependencies() {
        TranslatorId expectedTranslatorId = new TranslatorId() {
        };
        Translator testTranslator = Translator.builder()
                .setInitializer((translatorConext) -> {
                })
                .setTranslatorId(expectedTranslatorId)
                .addDependency(TestObjectTranslatorId.TRANSLATOR_ID)
                .addDependency(TestComplexTranslatorId.TRANSLATOR_ID)
                .build();

        Set<TranslatorId> expectedDependencies = new LinkedHashSet<>();
        expectedDependencies.add(TestObjectTranslatorId.TRANSLATOR_ID);
        expectedDependencies.add(TestComplexTranslatorId.TRANSLATOR_ID);

        Set<TranslatorId> actualDependencies = testTranslator.getTranslatorDependencies();

        assertEquals(expectedDependencies, actualDependencies);
    }

    @Test
    public void testHashCode() {
        TranslatorId translatorIdA = new TranslatorId() {
        };
        TranslatorId translatorIdB = new TranslatorId() {
        };
        Consumer<TranslatorContext> consumerA = (translatorConext) -> {
        };
        Consumer<TranslatorContext> consumerB = (translatorConext) -> {
        };
        Translator translatorA = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .build();

        Translator translatorB = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdB)
                .build();

        Translator translatorC = Translator.builder()
                .setInitializer(consumerB)
                .setTranslatorId(translatorIdA)
                .build();

        Translator translatorD = Translator.builder()
                .setInitializer(consumerB)
                .setTranslatorId(translatorIdB)
                .build();

        Translator translatorE = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .addDependency(translatorIdB)
                .build();

        Translator translatorF = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .addDependency(translatorIdB)
                .build();

        // if same instance, equal
        assertEquals(translatorA.hashCode(), translatorA.hashCode());

        // if different class, not equal
        assertNotEquals(translatorA.hashCode(), new Object().hashCode());

        // if cloned (same data instance), then equal
        assertEquals(translatorA.hashCode(), translatorA.cloneBuilder().build().hashCode());

        // if different translator id, not equal
        assertNotEquals(translatorA.hashCode(), translatorB.hashCode());

        // if different initializer, not equal
        assertNotEquals(translatorA.hashCode(), translatorC.hashCode());

        // if different intializer and id, not equal
        assertNotEquals(translatorA.hashCode(), translatorD.hashCode());

        // if same initializer and id, but different dependencies, not equal
        assertNotEquals(translatorA.hashCode(), translatorE.hashCode());

        // if same initilizer, id and dependecies, equal
        assertEquals(translatorE.hashCode(), translatorF.hashCode());
    }

    @Test
    public void testEquals() {
        TranslatorId translatorIdA = new TranslatorId() {
        };
        TranslatorId translatorIdB = new TranslatorId() {
        };
        Consumer<TranslatorContext> consumerA = (translatorConext) -> {
        };
        Consumer<TranslatorContext> consumerB = (translatorConext) -> {
        };
        Translator translatorA = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .build();

        Translator translatorB = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdB)
                .build();

        Translator translatorC = Translator.builder()
                .setInitializer(consumerB)
                .setTranslatorId(translatorIdA)
                .build();

        Translator translatorD = Translator.builder()
                .setInitializer(consumerB)
                .setTranslatorId(translatorIdB)
                .build();

        Translator translatorE = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .addDependency(translatorIdB)
                .build();

        Translator translatorF = Translator.builder()
                .setInitializer(consumerA)
                .setTranslatorId(translatorIdA)
                .addDependency(translatorIdB)
                .build();

        // if same instance, equal
        assertEquals(translatorA, translatorA);

        // if null, not equal
        assertNotEquals(translatorA, null);

        // if different class, not equal
        assertNotEquals(translatorA, new Object());

        // if cloned (same data instance), then equal
        assertEquals(translatorA, translatorA.cloneBuilder().build());

        // if different translator id, not equal
        assertNotEquals(translatorA, translatorB);

        // if different initializer, not equal
        assertNotEquals(translatorA, translatorC);

        // if different intializer and id, not equal
        assertNotEquals(translatorA, translatorD);

        // if same initializer and id, but different dependencies, not equal
        assertNotEquals(translatorA, translatorE);

        // if same initilizer, id and dependecies, equal
        assertEquals(translatorE, translatorF);
    }

    @Test
    public void testBuilder() {
        assertNotNull(Translator.builder());
    }

    @Test
    public void testBuild() {
        TranslatorId translatorIdA = new TranslatorId() {
        };
        Translator translatorA = Translator.builder()
                .setInitializer((translatorConext) -> {
                })
                .setTranslatorId(translatorIdA)
                .build();

        assertNotNull(translatorA);

        // preconditions
        // null initializer
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .setTranslatorId(translatorIdA)
                    .build();
        });

        assertEquals(CoreTranslationError.NULL_INIT_CONSUMER, contractException.getErrorType());

        // null translatorId
        contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .setInitializer((translatorConext) -> {
                    })
                    .build();
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATOR_ID, contractException.getErrorType());
    }

    @Test
    public void testSetTranslatorId() {
        TranslatorId translatorIdA = new TranslatorId() {
        };
        Translator translatorA = Translator.builder()
                .setInitializer((translatorConext) -> {
                })
                .setTranslatorId(translatorIdA)
                .build();

        assertEquals(translatorIdA, translatorA.getTranslatorId());

        // preconditions
        // null translatorId
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .setTranslatorId(null);
        });

        assertEquals(CoreTranslationError.NULL_TRANSLATOR_ID, contractException.getErrorType());
    }

    @Test
    public void testSetInitializer() {
        Consumer<TranslatorContext> expectedInitializer = (translatorConext) -> {
        };
        Translator testTranslator = Translator.builder()
                .setInitializer(expectedInitializer)
                .setTranslatorId(new TranslatorId() {
                })
                .build();

        assertEquals(expectedInitializer, testTranslator.getInitializer());

        // preconditions
        // null initializer
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .setInitializer(null);
        });

        assertEquals(CoreTranslationError.NULL_INIT_CONSUMER, contractException.getErrorType());
    }

    @Test
    public void testAddDependency() {
        TranslatorId expectedTranslatorId = new TranslatorId() {
        };
        Translator testTranslator = Translator.builder()
                .setInitializer((translatorConext) -> {
                })
                .setTranslatorId(expectedTranslatorId)
                .addDependency(TestObjectTranslatorId.TRANSLATOR_ID)
                .addDependency(TestComplexTranslatorId.TRANSLATOR_ID)
                .build();

        Set<TranslatorId> expectedDependencies = new LinkedHashSet<>();
        expectedDependencies.add(TestObjectTranslatorId.TRANSLATOR_ID);
        expectedDependencies.add(TestComplexTranslatorId.TRANSLATOR_ID);

        Set<TranslatorId> actualDependencies = testTranslator.getTranslatorDependencies();

        assertEquals(expectedDependencies, actualDependencies);

        // preconditions
        // null dependency
        ContractException contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .addDependency(null);
        });

        assertEquals(CoreTranslationError.NULL_DEPENDENCY, contractException.getErrorType());

        // duplicate dependency
        contractException = assertThrows(ContractException.class, () -> {
            Translator.builder()
                    .addDependency(TestObjectTranslatorId.TRANSLATOR_ID)
                    .addDependency(TestObjectTranslatorId.TRANSLATOR_ID);
        });

        assertEquals(CoreTranslationError.DUPLICATE_DEPENDENCY, contractException.getErrorType());
    }
}
