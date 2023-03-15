package gov.hhs.aspr.gcm.translation.plugins.regions.translators;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.SimpleRegionPropertyIdInput;
import plugins.regions.support.SimpleRegionPropertyId;

public class SimpleRegionPropertyIdTranslator extends AObjectTranslatorSpec<SimpleRegionPropertyIdInput, SimpleRegionPropertyId> {

    @Override
    protected SimpleRegionPropertyId convertInputObject(SimpleRegionPropertyIdInput inputObject) {
       return new SimpleRegionPropertyId(this.translator.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionPropertyIdInput convertSimObject(SimpleRegionPropertyId simObject) {
        return SimpleRegionPropertyIdInput.newBuilder().setValue(this.translator.getAnyFromObject(simObject.getValue())).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return SimpleRegionPropertyIdInput.getDescriptor();
    }

    @Override
    public SimpleRegionPropertyIdInput getDefaultInstanceForInputObject() {
       return SimpleRegionPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<SimpleRegionPropertyId> getSimObjectClass() {
        return SimpleRegionPropertyId.class;
    }

    @Override
    public Class<SimpleRegionPropertyIdInput> getInputObjectClass() {
       return SimpleRegionPropertyIdInput.class;
    }
    
}
