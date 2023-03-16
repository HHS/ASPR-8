package lesson.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import lesson.input.RegionInput;
import lesson.plugins.model.Region;

public class RegionTranslatorSpec extends AObjectTranslatorSpec<RegionInput, Region> {

    @Override
    protected Region convertInputObject(RegionInput inputObject) {
      return new Region(inputObject.getId());
    }

    @Override
    protected RegionInput convertSimObject(Region simObject) {
        return RegionInput.newBuilder().setId(simObject.getValue()).build();
    }

    @Override
    public RegionInput getDefaultInstanceForInputObject() {
        return RegionInput.getDefaultInstance();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
       return RegionInput.getDescriptor();
    }

    @Override
    public Class<RegionInput> getInputObjectClass() {
        return RegionInput.class;
    }

    @Override
    public Class<Region> getSimObjectClass() {
        return Region.class;
    }

    

}
