package gov.hhs.aspr.translation.core;

public class TranslatorContext {

    private final TranslationController translatorController;

    public TranslatorContext(TranslationController translatorController) {
        this.translatorController = translatorController;
    }

    public <T extends TranslatorCore.Builder> T getTranslatorCoreBuilder(Class<T> classRef) {
        return this.translatorController.getTranslatorCoreBuilder(classRef);
    }

    public <T, U extends T> void addMarkerInterface(Class<U> classRef, Class<T> markerInterface) {
        this.translatorController.addMarkerInterface(classRef, markerInterface);
    }
}
