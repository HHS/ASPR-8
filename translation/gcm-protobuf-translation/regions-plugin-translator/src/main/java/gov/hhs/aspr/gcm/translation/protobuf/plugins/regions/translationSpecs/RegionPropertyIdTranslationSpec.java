package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.RegionPropertyId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionPropertyIdInput} and
 * {@linkplain RegionPropertyId}
 */
public class RegionPropertyIdTranslationSpec extends ProtobufTranslationSpec<RegionPropertyIdInput, RegionPropertyId> {

    @Override
    protected RegionPropertyId convertInputObject(RegionPropertyIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RegionPropertyIdInput convertAppObject(RegionPropertyId appObject) {
        return RegionPropertyIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<RegionPropertyId> getAppObjectClass() {
        return RegionPropertyId.class;
    }

    @Override
    public Class<RegionPropertyIdInput> getInputObjectClass() {
        return RegionPropertyIdInput.class;
    }

}
