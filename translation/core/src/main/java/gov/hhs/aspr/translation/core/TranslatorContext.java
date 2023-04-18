package gov.hhs.aspr.translation.core;

public class TranslatorContext {

    private final TranslatorController translatorController;

    public TranslatorContext(TranslatorController translatorController) {
        this.translatorController = translatorController;
    }

    public <I, S> void addTranslatorSpec(AbstractTranslatorSpec<I, S> translatorSpec) {
        this.translatorController.addTranslatorSpec(translatorSpec);
    }

    public <T extends TranslatorCore.Builder> T getTranslatorCoreBuilder(Class<T> classRef) {
        return this.translatorController.getTranslatorCoreBuilder(classRef);
    }

    public <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {
        this.translatorController.addMarkerInterface(classRef, markerInterface);
    }
}
