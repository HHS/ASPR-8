package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject;

import gov.hhs.aspr.translation.core.TranslatorId;

public class TestProtobufObjectTranslatorId implements TranslatorId {
    public final static TranslatorId TRANSLATOR_ID = new TestProtobufObjectTranslatorId();

    private TestProtobufObjectTranslatorId() {}
}
