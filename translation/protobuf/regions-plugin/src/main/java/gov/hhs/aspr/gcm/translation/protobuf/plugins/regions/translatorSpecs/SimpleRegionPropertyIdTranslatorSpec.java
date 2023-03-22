package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.regions.input.SimpleRegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.regions.support.SimpleRegionPropertyId;

public class SimpleRegionPropertyIdTranslatorSpec
        extends AbstractTranslatorSpec<SimpleRegionPropertyIdInput, SimpleRegionPropertyId> {

    @Override
    protected SimpleRegionPropertyId convertInputObject(SimpleRegionPropertyIdInput inputObject) {
        return new SimpleRegionPropertyId(this.translator.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionPropertyIdInput convertAppObject(SimpleRegionPropertyId simObject) {
        return SimpleRegionPropertyIdInput.newBuilder().setValue(this.translator.getAnyFromObject(simObject.getValue()))
                .build();
    }

    @Override
    public SimpleRegionPropertyIdInput getDefaultInstanceForInputObject() {
        return SimpleRegionPropertyIdInput.getDefaultInstance();
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
