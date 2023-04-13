package gov.hhs.aspr.gcm.translation.core;

public interface ITranslatorSpec {
    void init(TranslatorCore translator);

    <T> T convert(Object object);
}
