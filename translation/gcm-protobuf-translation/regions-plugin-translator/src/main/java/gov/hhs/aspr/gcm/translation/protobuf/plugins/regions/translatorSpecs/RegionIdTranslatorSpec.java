package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.RegionId;

public class RegionIdTranslatorSpec extends ProtobufTranslationSpec<RegionIdInput, RegionId> {

    @Override
    protected RegionId convertInputObject(RegionIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RegionIdInput convertAppObject(RegionId appObject) {
        return RegionIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(appObject)).build();
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
