package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import com.google.protobuf.Descriptors.Descriptor;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchPropertyIdInput;
import plugins.materials.support.BatchPropertyId;


public class BatchPropertyIdTranslatorSpec extends AObjectTranslatorSpec<BatchPropertyIdInput, BatchPropertyId> {

    @Override
    protected BatchPropertyId convertInputObject(BatchPropertyIdInput inputObject) {
       return this.translator.getObjectFromAny(inputObject.getId(), getSimObjectClass());
    }

    @Override
    protected BatchPropertyIdInput convertSimObject(BatchPropertyId simObject) {
        return BatchPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public Descriptor getDescriptorForInputObject() {
        return BatchPropertyIdInput.getDescriptor();
    }

    @Override
    public BatchPropertyIdInput getDefaultInstanceForInputObject() {
       return BatchPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<BatchPropertyId> getSimObjectClass() {
        return BatchPropertyId.class;
    }

    @Override
    public Class<BatchPropertyIdInput> getInputObjectClass() {
       return BatchPropertyIdInput.class;
    }
    
}
