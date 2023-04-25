package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.SimpleRegionPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.SimpleRegionPropertyId;

public class SimpleRegionPropertyIdTranslatorSpec
        extends ProtobufTranslationSpec<SimpleRegionPropertyIdInput, SimpleRegionPropertyId> {

    @Override
    protected SimpleRegionPropertyId convertInputObject(SimpleRegionPropertyIdInput inputObject) {
        return new SimpleRegionPropertyId(this.translatorCore.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionPropertyIdInput convertAppObject(SimpleRegionPropertyId appObject) {
        return SimpleRegionPropertyIdInput.newBuilder().setValue(this.translatorCore.getAnyFromObject(appObject.getValue()))
                .build();
    }

    @Override
    public Class<SimpleRegionPropertyId> getAppObjectClass() {
        return SimpleRegionPropertyId.class;
    }

    @Override
    public Class<SimpleRegionPropertyIdInput> getInputObjectClass() {
        return SimpleRegionPropertyIdInput.class;
    }

}
