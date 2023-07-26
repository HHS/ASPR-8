package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import com.google.protobuf.Any;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyDimensionInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionPropertyIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyDimension;
import plugins.regions.support.RegionPropertyId;

public class RegionPropertyDimensionTranslationSpec
        extends ProtobufTranslationSpec<RegionPropertyDimensionInput, RegionPropertyDimension> {
    @Override
    protected RegionPropertyDimension convertInputObject(RegionPropertyDimensionInput inputObject) {
        RegionPropertyDimension.Builder builder = RegionPropertyDimension.builder();

        RegionPropertyId globalPropertyId = this.translationEngine.convertObject(inputObject.getRegionPropertyId());
        RegionId groupId = this.translationEngine.convertObject(inputObject.getRegionId());

        builder
                .setRegionPropertyId(globalPropertyId)
                .setRegionId(groupId);

        for (Any anyValue : inputObject.getValuesList()) {
            Object value = this.translationEngine.getObjectFromAny(anyValue);
            builder.addValue(value);
        }

        return builder.build();
    }

    @Override
    protected RegionPropertyDimensionInput convertAppObject(RegionPropertyDimension appObject) {
        RegionPropertyDimensionInput.Builder builder = RegionPropertyDimensionInput.newBuilder();

        RegionPropertyIdInput globalPropertyIdInput = this.translationEngine
                .convertObjectAsSafeClass(appObject.getRegionPropertyId(), RegionPropertyId.class);
        RegionIdInput groupIdInput = this.translationEngine.convertObjectAsSafeClass(appObject.getRegionId(), RegionId.class);

        builder
                .setRegionPropertyId(globalPropertyIdInput)
                .setRegionId(groupIdInput);

        for (Object objValue : appObject.getValues()) {
            builder.addValues(this.translationEngine.getAnyFromObject(objValue));
        }

        return builder.build();
    }

    @Override
    public Class<RegionPropertyDimension> getAppObjectClass() {
        return RegionPropertyDimension.class;
    }

    @Override
    public Class<RegionPropertyDimensionInput> getInputObjectClass() {
        return RegionPropertyDimensionInput.class;
    }
}
