package gov.hhs.aspr.gcm.translation.plugins.regions.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionIdInput;
import plugins.regions.support.RegionId;

public class RegionIdTranslator extends AObjectTranslatorSpec<RegionIdInput, RegionId> {

    @Override
    protected RegionId convertInputObject(RegionIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected RegionIdInput convertSimObject(RegionId simObject) {
        return RegionIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return RegionIdInput.getDescriptor();
    }

    @Override
    public RegionIdInput getDefaultInstanceForInputObject() {
       return RegionIdInput.getDefaultInstance();
    }

    @Override
    public Class<RegionId> getSimObjectClass() {
        return RegionId.class;
    }

    @Override
    public Class<RegionIdInput> getInputObjectClass() {
       return RegionIdInput.class;
    }
    
}
