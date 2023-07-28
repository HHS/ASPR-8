package gov.hhs.aspr.ms.taskit.protobuf;

import gov.hhs.aspr.ms.taskit.core.TranslationEngine;
import gov.hhs.aspr.ms.taskit.core.TranslationSpec;

/**
 * Abstract implementation of {@link TranslationSpec} that sets the
 * {@link TranslationEngine} on the Spec to a {@link ProtobufTranslationEngine}
 */
public abstract class ProtobufTranslationSpec<I, A> extends TranslationSpec<I, A> {
    protected ProtobufTranslationEngine translationEngine;

    protected ProtobufTranslationSpec() {
    }

    /**
     * init implementation, sets the translationEngine on this translationSpec.
     * calls super.init() to ensure this spec was properly initialized.
     */
    public void init(TranslationEngine translationEngine) {
        super.init(translationEngine);
        this.translationEngine = (ProtobufTranslationEngine) translationEngine;
    }
}
