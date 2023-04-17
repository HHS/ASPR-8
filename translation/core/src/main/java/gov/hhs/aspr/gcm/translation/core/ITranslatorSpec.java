package gov.hhs.aspr.gcm.translation.core;

public interface ITranslatorSpec {
    <T extends TranslatorCore> void init(T translator);

    <T> T convert(Object object);

    boolean isInitialized();
}
