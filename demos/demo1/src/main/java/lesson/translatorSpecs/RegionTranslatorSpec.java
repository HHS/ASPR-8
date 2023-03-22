package lesson.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import lesson.input.RegionInput;
import lesson.plugins.model.Region;

public class RegionTranslatorSpec extends AbstractTranslatorSpec<RegionInput, Region> {

    @Override
    protected Region convertInputObject(RegionInput inputObject) {
        return new Region(inputObject.getId());
    }

    @Override
    protected RegionInput convertAppObject(Region simObject) {
        return RegionInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public RegionInput getDefaultInstanceForInputObject() {
        return RegionInput.getDefaultInstance();
    }

    @Override
    public Class<RegionInput> getInputObjectClass() {
        return RegionInput.class;
    }

    @Override
    public Class<Region> getAppObjectClass() {
        return Region.class;
    }

}
