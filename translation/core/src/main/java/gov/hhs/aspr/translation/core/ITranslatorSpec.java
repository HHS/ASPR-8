package gov.hhs.aspr.translation.core;

interface ITranslatorSpec {
    <T extends TranslatorCore> void init(T translator);

    <T> T convert(Object object);

    boolean isInitialized();
}
