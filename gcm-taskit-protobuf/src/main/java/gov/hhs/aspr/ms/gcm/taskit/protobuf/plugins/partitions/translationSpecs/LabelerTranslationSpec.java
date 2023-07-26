package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.partitions.input.LabelerInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.partitions.support.Labeler;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain LabelerInput} and
 * {@linkplain Labeler}
 */
public class LabelerTranslationSpec extends ProtobufTranslationSpec<LabelerInput, Labeler> {

    @Override
    protected Labeler convertInputObject(LabelerInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getLabeler());
    }

    @Override
    protected LabelerInput convertAppObject(Labeler appObject) {
        return LabelerInput.newBuilder().setLabeler(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<Labeler> getAppObjectClass() {
        return Labeler.class;
    }

    @Override
    public Class<LabelerInput> getInputObjectClass() {
        return LabelerInput.class;
    }

}
