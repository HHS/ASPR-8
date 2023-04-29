package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.TranslatorId;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexTranslatorId;

public class AT_TestObjectTranslator {
    @Test
    public void testGetTranslator() {
        Translator testObjectTranslator = TestObjectTranslator.getTestTranslator();

        assertEquals(TestObjectTranslatorId.TRANSLATOR_ID, testObjectTranslator.getTranslatorId());
        Set<TranslatorId> expectedDependencies = new LinkedHashSet<>();

        expectedDependencies.add(TestComplexTranslatorId.TRANSLATOR_ID);

        assertEquals(expectedDependencies, testObjectTranslator.getTranslatorDependencies());
    }
}
