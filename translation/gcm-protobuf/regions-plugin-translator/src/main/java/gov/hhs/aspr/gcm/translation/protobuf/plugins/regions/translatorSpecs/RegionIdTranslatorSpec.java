package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translatorSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.RegionIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslatorSpec;
import plugins.regions.support.RegionId;

public class RegionIdTranslatorSpec extends ProtobufTranslatorSpec<RegionIdInput, RegionId> {

    @Override
    protected RegionId convertInputObject(RegionIdInput inputObject) {
        return this.translatorCore.getObjectFromAny(inputObject.getId());
    }

    @Override
    protected RegionIdInput convertAppObject(RegionId simObject) {
        return RegionIdInput.newBuilder().setId(this.translatorCore.getAnyFromObject(simObject)).build();
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
