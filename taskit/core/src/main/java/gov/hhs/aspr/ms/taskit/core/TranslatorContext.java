package gov.hhs.aspr.ms.taskit.core;

import util.errors.ContractException;

/**
 * Context that is used by {@link Translator}s to get the instance of
 * {@link TranslationEngine.Builder} from the {@link TranslationController}
 */
public class TranslatorContext {

    private final TranslationController translationController;

    public TranslatorContext(TranslationController translationController) {
        this.translationController = translationController;
    }

    /**
     * Returns an instance of the TranslationEngine Builder
     * 
     * @param <T> the type of the TranslationEngine
     * 
     * @throws ContractException
     *                           <li>{@linkplain CoreTranslationError#INVALID_TRANSLATION_ENGINE_BUILDER}
     *                           if the given classRef does not match the class or
     *                           the translatorCoreBuilder is null
     */
    public <T extends TranslationEngine.Builder> T getTranslationEngineBuilder(Class<T> classRef) {
        return this.translationController.getTranslationEngineBuilder(classRef);
    }

    /**
     * Adds the child class parent class relationship to the TranslatorController
     * 
     * @param <M> the type of the child
     * @param <U> the type of the parent
     * 
     */
    public <M extends U, U> void addParentChildClassRelationship(Class<M> classRef, Class<U> parentClassRef) {
        this.translationController.addParentChildClassRelationship(classRef, parentClassRef);
    }
}
