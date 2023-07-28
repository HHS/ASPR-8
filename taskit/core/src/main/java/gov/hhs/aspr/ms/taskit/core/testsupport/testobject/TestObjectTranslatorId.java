package gov.hhs.aspr.ms.taskit.core.testsupport.testobject;

import gov.hhs.aspr.ms.taskit.core.TranslatorId;

public class TestObjectTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new TestObjectTranslatorId();

    private TestObjectTranslatorId() {
    }
}
