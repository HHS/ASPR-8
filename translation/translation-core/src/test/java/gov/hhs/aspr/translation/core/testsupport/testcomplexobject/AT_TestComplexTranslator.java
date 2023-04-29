package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.TranslatorId;

public class AT_TestComplexTranslator {

    @Test
    public void testGetTranslator() {
        Translator testComplexTranslator = TestComplexTranslator.getTranslator();

        assertEquals(TestComplexTranslatorId.TRANSLATOR_ID, testComplexTranslator.getTranslatorId());
        Set<TranslatorId> expectedDependencies = new LinkedHashSet<>();

        assertEquals(expectedDependencies, testComplexTranslator.getTranslatorDependencies());
    }
}
