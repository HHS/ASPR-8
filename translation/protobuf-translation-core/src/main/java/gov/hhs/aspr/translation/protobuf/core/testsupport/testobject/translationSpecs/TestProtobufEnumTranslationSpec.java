package gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.translationSpecs;

import gov.hhs.aspr.translation.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import gov.hhs.aspr.translation.protobuf.core.testsupport.testobject.input.TestInputEnum;

public class TestProtobufEnumTranslationSpec extends ProtobufTranslationSpec<TestInputEnum, TestAppEnum> {
    @Override
    protected TestAppEnum convertInputObject(TestInputEnum inputObject) {
        return TestAppEnum.valueOf(inputObject.name());
    }

    @Override
    protected TestInputEnum convertAppObject(TestAppEnum appObject) {
        return TestInputEnum.valueOf(appObject.name());
    }

    @Override
    public Class<TestAppEnum> getAppObjectClass() {
        return TestAppEnum.class;
    }

    @Override
    public Class<TestInputEnum> getInputObjectClass() {
        return TestInputEnum.class;
    }
}
