package gov.hhs.aspr.gcm.translation.core;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I extends Message, S> void addTranslatorSpec(AObjectTranslatorSpec<I, S> translatorSpec) {
        this.translatorController.addTranslatorSpec(translatorSpec);
    }

    public <I extends ProtocolMessageEnum, S> void addTranslatorSpec(AEnumTranslatorSpec<I, S> translatorSpec) {
        this.translatorController.addTranslatorSpec(translatorSpec);
    }

    public void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.translatorController.addFieldToIncludeDefaultValue(fieldDescriptor);
    }
}
