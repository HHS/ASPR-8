package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.regions.testsupport.TestRegionId;

public class TestRegionIdTranslatorSpec extends AbstractTranslatorSpec<TestRegionIdInput, TestRegionId> {

    @Override
    protected TestRegionId convertInputObject(TestRegionIdInput inputObject) {
        return TestRegionId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionIdInput convertAppObject(TestRegionId simObject) {
        return TestRegionIdInput.valueOf(simObject.name());
    }

    @Override
    public TestRegionIdInput getDefaultInstanceForInputObject() {
        return TestRegionIdInput.forNumber(0);
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
