package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.materials.support.MaterialId;

public class MaterialIdTranslatorSpec extends ProtobufTranslationSpec<MaterialIdInput, MaterialId> {

    @Override
    protected MaterialId convertInputObject(MaterialIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected MaterialIdInput convertAppObject(MaterialId appObject) {
        return MaterialIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
