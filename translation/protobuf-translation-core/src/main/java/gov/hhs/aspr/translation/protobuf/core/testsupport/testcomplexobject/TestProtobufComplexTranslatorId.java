package gov.hhs.aspr.translation.protobuf.core.testsupport.testcomplexobject;

import gov.hhs.aspr.translation.core.TranslatorId;

public class TestProtobufComplexTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new TestProtobufComplexTranslatorId();

    private TestProtobufComplexTranslatorId() {
    }
}
