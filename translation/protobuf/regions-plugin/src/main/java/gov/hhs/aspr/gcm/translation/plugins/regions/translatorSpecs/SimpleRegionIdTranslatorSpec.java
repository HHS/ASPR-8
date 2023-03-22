package gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.SimpleRegionIdInput;
import plugins.regions.support.SimpleRegionId;

public class SimpleRegionIdTranslatorSpec extends AbstractTranslatorSpec<SimpleRegionIdInput, SimpleRegionId> {

    @Override
    protected SimpleRegionId convertInputObject(SimpleRegionIdInput inputObject) {
        return new SimpleRegionId(this.translator.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionIdInput convertAppObject(SimpleRegionId simObject) {
        return SimpleRegionIdInput.newBuilder().setValue(this.translator.getAnyFromObject(simObject.getValue()))
                .build();
    }

    @Override
    public SimpleRegionIdInput getDefaultInstanceForInputObject() {
        return SimpleRegionIdInput.getDefaultInstance();
    }

    @Override
    public Class<SimpleRegionId> getAppObjectClass() {
        return SimpleRegionId.class;
    }

    @Override
    public Class<SimpleRegionIdInput> getInputObjectClass() {
        return SimpleRegionIdInput.class;
    }

}