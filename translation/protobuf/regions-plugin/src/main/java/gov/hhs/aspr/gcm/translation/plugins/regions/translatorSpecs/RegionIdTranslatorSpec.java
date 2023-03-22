package gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionIdInput;
import plugins.regions.support.RegionId;

public class RegionIdTranslatorSpec extends AbstractTranslatorSpec<RegionIdInput, RegionId> {

    @Override
    protected RegionId convertInputObject(RegionIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected RegionIdInput convertAppObject(RegionId simObject) {
        return RegionIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public RegionIdInput getDefaultInstanceForInputObject() {
        return RegionIdInput.getDefaultInstance();
    }

    @Override
    public Class<RegionId> getAppObjectClass() {
        return RegionId.class;
    }

    @Override
    public Class<RegionIdInput> getInputObjectClass() {
        return RegionIdInput.class;
    }

}
