package gov.hhs.aspr.gcm.translation.protobuf.core;

public abstract class AbstractProtobufTranslatorSpec<I, S> extends AbstractTranslatorSpec<I,S> {
    protected ProtobufTranslatorCore translator;

    public void init(TranslatorCore translator) {
        this.translator = (ProtobufTranslatorCore) translator;
        this.initialized = true;
    }
}
