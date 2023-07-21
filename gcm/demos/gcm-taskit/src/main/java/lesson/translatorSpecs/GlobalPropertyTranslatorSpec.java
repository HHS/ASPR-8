package lesson.translatorSpecs;

import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import lesson.input.GlobalPropertyInput;
import lesson.plugins.model.GlobalProperty;

public class GlobalPropertyTranslatorSpec extends ProtobufTranslationSpec<GlobalPropertyInput, GlobalProperty> {

    @Override
    protected GlobalProperty convertInputObject(GlobalPropertyInput inputObject) {
        return GlobalProperty.valueOf(inputObject.name());
    }

    @Override
    protected GlobalPropertyInput convertAppObject(GlobalProperty simObject) {
        return GlobalPropertyInput.valueOf(simObject.name());
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
