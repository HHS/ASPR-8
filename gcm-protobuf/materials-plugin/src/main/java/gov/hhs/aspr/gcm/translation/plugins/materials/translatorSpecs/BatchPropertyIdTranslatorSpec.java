package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.BatchPropertyIdInput;
import plugins.materials.support.BatchPropertyId;

public class BatchPropertyIdTranslatorSpec extends AObjectTranslatorSpec<BatchPropertyIdInput, BatchPropertyId> {

    @Override
    protected BatchPropertyId convertInputObject(BatchPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected BatchPropertyIdInput convertAppObject(BatchPropertyId simObject) {
        return BatchPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public BatchPropertyIdInput getDefaultInstanceForInputObject() {
        return BatchPropertyIdInput.getDefaultInstance();
    }

    @Override
    public Class<BatchPropertyId> getAppObjectClass() {
        return BatchPropertyId.class;
    }

    @Override
    public Class<BatchPropertyIdInput> getInputObjectClass() {
        return BatchPropertyIdInput.class;
    }

}
