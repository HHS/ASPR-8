package gov.hhs.aspr.translation.protobuf.core;

import gov.hhs.aspr.translation.core.AbstractTranslatorSpec;
import gov.hhs.aspr.translation.core.TranslatorCore;

public abstract class AbstractProtobufTranslatorSpec<I, S> extends AbstractTranslatorSpec<I,S> {
    protected ProtobufTranslatorCore translator;

    public void init(TranslatorCore translator) {
        super.init(translator);
        this.translator = (ProtobufTranslatorCore) translator;
    }
}
