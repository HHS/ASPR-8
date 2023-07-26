package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.SimpleRegionPropertyIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import plugins.regions.support.SimpleRegionPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain SimpleRegionPropertyIdInput} and
 * {@linkplain SimpleRegionPropertyId}
 */
public class SimpleRegionPropertyIdTranslationSpec
        extends ProtobufTranslationSpec<SimpleRegionPropertyIdInput, SimpleRegionPropertyId> {

    @Override
    protected SimpleRegionPropertyId convertInputObject(SimpleRegionPropertyIdInput inputObject) {
        return new SimpleRegionPropertyId(this.translationEngine.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionPropertyIdInput convertAppObject(SimpleRegionPropertyId appObject) {
        return SimpleRegionPropertyIdInput.newBuilder()
                .setValue(this.translationEngine.getAnyFromObject(appObject.getValue()))
                .build();
    }

    @Override
    public Class<SimpleRegionPropertyId> getAppObjectClass() {
        return SimpleRegionPropertyId.class;
    }

    @Override
    public Class<SimpleRegionPropertyIdInput> getInputObjectClass() {
        return SimpleRegionPropertyIdInput.class;
    }

}
