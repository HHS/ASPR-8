package gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.translationSpecs;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.input.SimpleRegionIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.SimpleRegionId;

public class SimpleRegionIdTranslatorSpec extends ProtobufTranslationSpec<SimpleRegionIdInput, SimpleRegionId> {

    @Override
    protected SimpleRegionId convertInputObject(SimpleRegionIdInput inputObject) {
        return new SimpleRegionId(this.translatorCore.getObjectFromAny(inputObject.getValue()));
    }

    @Override
    protected SimpleRegionIdInput convertAppObject(SimpleRegionId appObject) {
        return SimpleRegionIdInput.newBuilder().setValue(this.translatorCore.getAnyFromObject(appObject.getValue()))
                .build();
    }

    @Override
    public Class<SimpleRegionId> getAppObjectClass() {
        return SimpleRegionId.class;
    }

    @Override
    public Class<SimpleRegionIdInput> getInputObjectClass() {
        return SimpleRegionIdInput.class;
    }

}
