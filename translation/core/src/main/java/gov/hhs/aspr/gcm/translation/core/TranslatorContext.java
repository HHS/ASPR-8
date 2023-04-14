package gov.hhs.aspr.gcm.translation.core;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I, S> void addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
        this.translatorController.addTranslatorSpec(translatorSpec);
    }

    public TranslatorCore.Builder getTranslatorCoreBuilder() {
        return this.translatorController.getTranslatorCoreBuilder();
    }

    public <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {
        this.translatorController.addMarkerInterface(classRef, markerInterface);
    }
}
