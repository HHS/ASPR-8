package gov.hhs.aspr.translation.core;

public interface BaseTranslationSpec {
    <T extends TranslatorCore> void init(T translatorCore);

    <T> T convert(Object object);

    boolean isInitialized();
}
