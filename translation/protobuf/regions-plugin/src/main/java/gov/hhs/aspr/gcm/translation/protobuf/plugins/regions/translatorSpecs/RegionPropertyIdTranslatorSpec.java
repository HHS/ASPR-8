package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionPropertyIdInput;
import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import plugins.regions.support.RegionPropertyId;

public class RegionPropertyIdTranslatorSpec extends AbstractTranslatorSpec<RegionPropertyIdInput, RegionPropertyId> {

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
