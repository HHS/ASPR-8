package gov.hhs.aspr.gcm.translation.plugins.globalproperties.translatorSpecs;

import gov.hhs.aspr.gcm.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.gcm.translation.plugins.globalproperties.input.GlobalPropertyIdInput;
import plugins.globalproperties.support.GlobalPropertyId;

public class GlobalPropertyIdTranslatorSpec extends AbstractTranslatorSpec<GlobalPropertyIdInput, GlobalPropertyId> {

    @Override
    protected GlobalPropertyId convertInputObject(GlobalPropertyIdInput inputObject) {
        return this.translator.getObjectFromAny(inputObject.getId(), getAppObjectClass());
    }

    @Override
    protected GlobalPropertyIdInput convertAppObject(GlobalPropertyId simObject) {
        return GlobalPropertyIdInput.newBuilder().setId(this.translator.getAnyFromObject(simObject))
                .build();
    }

    @Override
    public GlobalPropertyIdInput getDefaultInstanceForInputObject() {
        return GlobalPropertyIdInput.getDefaultInstance();
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
