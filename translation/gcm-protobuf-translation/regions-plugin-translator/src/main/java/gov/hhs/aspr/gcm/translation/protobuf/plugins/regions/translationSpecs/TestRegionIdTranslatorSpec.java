package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.testsupport.TestRegionId;

public class TestRegionIdTranslatorSpec extends ProtobufTranslationSpec<TestRegionIdInput, TestRegionId> {

    @Override
    protected TestRegionId convertInputObject(TestRegionIdInput inputObject) {
        return TestRegionId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionIdInput convertAppObject(TestRegionId appObject) {
        return TestRegionIdInput.valueOf(appObject.name());
    }

    @Override
    public Class<TestRegionId> getAppObjectClass() {
        return TestRegionId.class;
    }

    @Override
    public Class<TestRegionIdInput> getInputObjectClass() {
        return TestRegionIdInput.class;
    }

}
