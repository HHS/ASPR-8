package gov.hhs.aspr.translation.core;

/**
 * Base interface for TranslationSpecifications (TranslationSpecs)
 * 
 * Package level access
 */
interface BaseTranslationSpec {
    <T extends TranslationEngine> void init(T translationEngine);

    <T> T convert(Object object);

    boolean isInitialized();
}
