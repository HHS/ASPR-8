package gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.translationSpecs;

import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.taskit.core.testsupport.testobject.app.TestAppEnum;
import gov.hhs.aspr.ms.taskit.protobuf.testsupport.testobject.input.TestInputEnum;

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
