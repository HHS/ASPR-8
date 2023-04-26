package gov.hhs.aspr.translation.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.TranslatorId;

public class TestComplexTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new TestComplexTranslatorId();

    private TestComplexTranslatorId() {
    }
}
