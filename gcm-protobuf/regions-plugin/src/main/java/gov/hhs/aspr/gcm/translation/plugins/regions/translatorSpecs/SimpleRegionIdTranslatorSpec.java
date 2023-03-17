package gov.hhs.aspr.gcm.translation.plugins.regions.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.regions.input.SimpleRegionIdInput;
import plugins.regions.support.SimpleRegionId;

public class SimpleRegionIdTranslatorSpec extends AObjectTranslatorSpec<SimpleRegionIdInput, SimpleRegionId> {

    @Override
    protected SimpleRegionId convertInputObject(SimpleRegionIdInput inputObject) {
       return new SimpleRegionId(this.translator.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionIdInput convertSimObject(SimpleRegionId simObject) {
        return SimpleRegionIdInput.newBuilder().setValue(this.translator.getAnyFromObject(simObject.getValue())).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return SimpleRegionIdInput.getDescriptor();
    }

    @Override
    public SimpleRegionIdInput getDefaultInstanceForInputObject() {
       return SimpleRegionIdInput.getDefaultInstance();
    }

    @Override
    public Class<SimpleRegionId> getSimObjectClass() {
        return SimpleRegionId.class;
    }

    @Override
    public Class<SimpleRegionIdInput> getInputObjectClass() {
       return SimpleRegionIdInput.class;
    }
    
}
