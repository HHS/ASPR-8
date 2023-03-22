package gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.TestRegionIdInput;
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
