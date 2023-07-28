package gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.nucleus.input.DimensionInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.nucleus.Dimension;

public class DimensionTranslationSpec extends ProtobufTranslationSpec<DimensionInput, Dimension> {

    @Override
    protected Dimension convertInputObject(DimensionInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getDimension());
    }

    @Override
    protected DimensionInput convertAppObject(Dimension appObject) {
        return DimensionInput.newBuilder()
                .setDimension(this.translationEngine.getAnyFromObject(appObject))
                .build();
    }

    @Override
    public Class<Dimension> getAppObjectClass() {
        return Dimension.class;
    }

    @Override
    public Class<DimensionInput> getInputObjectClass() {
        return DimensionInput.class;
    }

}
