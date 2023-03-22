package lesson.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.core.AbstractTranslatorSpec;
import lesson.input.GlobalPropertyInput;
import lesson.plugins.model.GlobalProperty;

public class GlobalPropertyTranslatorSpec extends AbstractTranslatorSpec<GlobalPropertyInput, GlobalProperty> {

    @Override
    protected GlobalProperty convertInputObject(GlobalPropertyInput inputObject) {
        return GlobalProperty.valueOf(inputObject.name());
    }

    @Override
    protected GlobalPropertyInput convertAppObject(GlobalProperty simObject) {
        return GlobalPropertyInput.valueOf(simObject.name());
    }

    @Override
    public GlobalPropertyInput getDefaultInstanceForInputObject() {
        return GlobalPropertyInput.forNumber(0);
    }

    @Override
    public Class<GlobalPropertyInput> getInputObjectClass() {
        return GlobalPropertyInput.class;
    }

    @Override
    public Class<GlobalProperty> getAppObjectClass() {
        return GlobalProperty.class;
    }

}
