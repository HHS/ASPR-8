package gov.hhs.aspr.translation.core.testsupport.testobject;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.translation.core.Translator;
import gov.hhs.aspr.translation.core.TranslatorId;
import gov.hhs.aspr.translation.core.testsupport.testcomplexobject.TestComplexObjectTranslatorId;
import util.annotations.UnitTestMethod;

public class AT_TestObjectTranslator {

    @Test
    @UnitTestMethod(target = TestObjectTranslator.class, name = "getTranslator", args = {})
    public void testGetTranslator() {
        Translator testObjectTranslator = TestObjectTranslator.getTranslator();

        assertEquals(TestObjectTranslatorId.TRANSLATOR_ID, testObjectTranslator.getTranslatorId());
        Set<TranslatorId> expectedDependencies = new LinkedHashSet<>();

        expectedDependencies.add(TestComplexObjectTranslatorId.TRANSLATOR_ID);

        assertEquals(expectedDependencies, testObjectTranslator.getTranslatorDependencies());
    }
}
