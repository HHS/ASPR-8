package gov.hhs.aspr.gcm.translation.plugins.regions.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.Translator;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.RegionPropertyIdInput;
import plugins.regions.support.RegionPropertyId;

public class RegionPropertyIdTranslator extends Translator<RegionPropertyIdInput, RegionPropertyId> {

    @Override
    protected RegionPropertyId convertInputObject(RegionPropertyIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected RegionPropertyIdInput convertSimObject(RegionPropertyId simObject) {
        return RegionPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return RegionPropertyIdInput.getDescriptor();
    }

    @Override
    public RegionPropertyIdInput getDefaultInstanceForInputObject() {
       return RegionPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<RegionPropertyId> getSimObjectClass() {
        return RegionPropertyId.class;
    }

    @Override
    public Class<RegionPropertyIdInput> getInputObjectClass() {
       return RegionPropertyIdInput.class;
    }
    
}
