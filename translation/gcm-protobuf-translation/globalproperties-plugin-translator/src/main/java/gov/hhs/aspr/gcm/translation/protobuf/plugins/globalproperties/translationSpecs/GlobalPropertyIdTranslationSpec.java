package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.globalproperties.support.GlobalPropertyId;

public class GlobalPropertyIdTranslationSpec extends ProtobufTranslationSpec<GlobalPropertyIdInput, GlobalPropertyId> {

    @Override
    protected GlobalPropertyId convertInputObject(GlobalPropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GlobalPropertyIdInput convertAppObject(GlobalPropertyId appObject) {
        return GlobalPropertyIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject))
                .build();
    }

    @Override
    public Class<GlobalPropertyId> getAppObjectClass() {
        return GlobalPropertyId.class;
    }

    @Override
    public Class<GlobalPropertyIdInput> getInputObjectClass() {
        return GlobalPropertyIdInput.class;
    }

}
