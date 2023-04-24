package gov.hhs.aspr.translation.core;

public interface ITranslatorSpec {
    <T extends TranslatorCore> void init(T translatorCore);

    <T> T convert(Object object);

    boolean isInitialized();
}
