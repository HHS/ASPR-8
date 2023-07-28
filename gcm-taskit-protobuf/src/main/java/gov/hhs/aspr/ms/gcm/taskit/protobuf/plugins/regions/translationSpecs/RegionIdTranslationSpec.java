package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInput;
import gov.hhs.aspr.ms.taskit.protobuf.ProtobufTranslationSpec;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionIdInput} and
 * {@linkplain RegionId}
 */
public class RegionIdTranslationSpec extends ProtobufTranslationSpec<RegionIdInput, RegionId> {

    @Override
    protected RegionId convertInputObject(RegionIdInput inputObject) {
        return this.translationEngine.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RegionIdInput convertAppObject(RegionId appObject) {
        return RegionIdInput.newBuilder().setId(this.translationEngine.getAnyFromObject(appObject)).build();
    }

    @Override
    public Class<RegionId> getAppObjectClass() {
        return RegionId.class;
    }

    @Override
    public Class<RegionIdInput> getInputObjectClass() {
        return RegionIdInput.class;
    }

}
