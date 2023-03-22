package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.regions.input.TestRegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.regions.testsupport.TestRegionPropertyId;

public class TestRegionPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<TestRegionPropertyIdInput, TestRegionPropertyId> {

    @Override
    protected TestRegionPropertyId convertInputObject(TestRegionPropertyIdInput inputObject) {
        return TestRegionPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionPropertyIdInput convertAppObject(TestRegionPropertyId simObject) {
        return TestRegionPropertyIdInput.valueOf(simObject.name());
    }

    @Override
    public TestRegionPropertyIdInput getDefaultInstanceForInputObject() {
        return TestRegionPropertyIdInput.forNumber(0);
    }

    @Override
    public Class<TestRegionPropertyId> getAppObjectClass() {
        return TestRegionPropertyId.class;
    }

    @Override
    public Class<TestRegionPropertyIdInput> getInputObjectClass() {
        return TestRegionPropertyIdInput.class;
    }

}
