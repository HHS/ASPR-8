package gov.hhs.aspr.gcm.translation.protobuf.core;

public interface ITranslatorSpec {
    void init(TranslatorCore translator);

    <T> T convert(Object object);
}
