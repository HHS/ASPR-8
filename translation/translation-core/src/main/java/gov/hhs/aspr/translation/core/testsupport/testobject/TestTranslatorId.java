package gov.hhs.aspr.translation.core.testsupport.testobject;

import gov.hhs.aspr.translation.core.TranslatorId;

public class TestTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new TestTranslatorId();

    private TestTranslatorId() {
    }
}
