package gov.hhs.aspr.gcm.gcmprotobuf.core;

import com.google.protobuf.Message;
import com.google.protobuf.Descriptors.FieldDescriptor;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I extends Message, S> void addTranslator(AbstractTranslator<I, S> translator) {
        this.translatorController.addTranslator(translator);
    }

    public void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.translatorController.addFieldToIncludeDefaultValue(fieldDescriptor);
    }
}
