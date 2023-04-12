package gov.hhs.aspr.gcm.translation.protobuf.core;

import com.google.protobuf.Descriptors.FieldDescriptor;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I, S> void addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
        this.translatorController.addTranslatorSpec(translatorSpec);
    }

    public void addFieldToIncludeDefaultValue(FieldDescriptor fieldDescriptor) {
        this.translatorController.addFieldToIncludeDefaultValue(fieldDescriptor);
    }

    public <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {
        this.translatorController.addMarkerInterface(classRef, markerInterface);
    }
}
