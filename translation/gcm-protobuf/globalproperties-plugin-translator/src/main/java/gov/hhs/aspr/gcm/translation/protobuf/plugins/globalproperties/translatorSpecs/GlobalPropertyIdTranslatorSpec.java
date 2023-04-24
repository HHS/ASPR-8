package gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.globalproperties.input.GlobalPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.globalproperties.support.GlobalPropertyId;

public class GlobalPropertyIdTranslatorSpec extends ProtobufTranslatorSpec<GlobalPropertyIdInput, GlobalPropertyId> {

    @Override
    protected GlobalPropertyId convertInputObject(GlobalPropertyIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected GlobalPropertyIdInput convertAppObject(GlobalPropertyId simObject) {
        return GlobalPropertyIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(simObject))
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
