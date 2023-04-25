package gov.hhs.aspr.translation.core;

public interface BaseTranslationSpec {
    <T extends TranslationEngine> void init(T translatorCore);

    <T> T convert(Object object);

    boolean isInitialized();
}
