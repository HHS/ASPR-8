package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.AbstractProtobufTranslatorSpec;
import plugins.regions.testsupport.TestRegionPropertyId;

public class TestRegionPropertyIdTranslatorSpec
        extends AbstractProtobufTranslatorSpec<TestRegionPropertyIdInput, TestRegionPropertyId> {

    @Override
    protected TestRegionPropertyId convertInputObject(TestRegionPropertyIdInput inputObject) {
        return TestRegionPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionPropertyIdInput convertAppObject(TestRegionPropertyId simObject) {
        return TestRegionPropertyIdInput.valueOf(simObject.name());
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
