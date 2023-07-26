package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.TestRegionPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.testsupport.TestRegionPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain TestRegionPropertyIdInput} and
 * {@linkplain TestRegionPropertyId}
 */
public class TestRegionPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<TestRegionPropertyIdInput, TestRegionPropertyId> {

    @Override
    protected TestRegionPropertyId convertInputObject(TestRegionPropertyIdInput inputObject) {
        return TestRegionPropertyId.valueOf(inputObject.name());
    }

    @Override
    protected TestRegionPropertyIdInput convertAppObject(TestRegionPropertyId appObject) {
        return TestRegionPropertyIdInput.valueOf(appObject.name());
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