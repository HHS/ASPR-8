package gov.hhs.aspr.gcm.translation.core;

import com.google.protobuf.Message;
import com.google.protobuf.ProtocolMessageEnum;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I extends Message, S> void addTranslator(Translator<I, S> translator) {
        this.translatorController.addTranslator(translator);
    }

    public <I extends ProtocolMessageEnum, S> void addTranslator(EnumTranslator<I, S> translator) {
        this.translatorController.addTranslator(translator);
    }

    public void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.translatorController.addFieldToIncludeDefaultValue(fieldDescriptor);
    }
}
