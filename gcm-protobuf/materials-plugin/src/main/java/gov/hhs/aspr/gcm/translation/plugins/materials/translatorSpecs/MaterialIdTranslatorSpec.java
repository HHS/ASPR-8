package gov.hhs.aspr.gcm.translation.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AObjectTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.materials.input.MaterialIdInput;
import plugins.materials.support.MaterialId;

public class MaterialIdTranslatorSpec extends AObjectTranslatorSpec<MaterialIdInput, MaterialId> {

    @Override
    protected MaterialId convertInputObject(MaterialIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected MaterialIdInput convertAppObject(MaterialId simObject) {
        return MaterialIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject)).build();
    }

    @Override
    public MaterialIdInput getDefaultInstanceForInputObject() {
        return MaterialIdInput.getDefaultInstance();
    }

    @Override
    public Class<MaterialId> getAppObjectClass() {
        return MaterialId.class;
    }

    @Override
    public Class<MaterialIdInput> getInputObjectClass() {
        return MaterialIdInput.class;
    }

}
