package gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionPropertyIdInput;
import plugins.regions.support.RegionPropertyId;

public class RegionPropertyIdTranslatorSpec extends AObjectTranslatorSpec<RegionPropertyIdInput, RegionPropertyId> {

    @Override
    protected RegionPropertyId convertInputObject(RegionPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected RegionPropertyIdInput convertAppObject(RegionPropertyId simObject) {
        return RegionPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public RegionPropertyIdInput getDefaultInstanceForInputObject() {
        return RegionPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<RegionPropertyId> getAppObjectClass() {
        return RegionPropertyId.class;
    }

    @Override
    public Class<RegionPropertyIdInput> getInputObjectClass() {
        return RegionPropertyIdInput.class;
    }

}
