package gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.translationSpecs;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionFilterInput;
import gov.hhs.aspr.ms.gcm.taskit.protobuf.plugins.regions.support.input.RegionIdInput;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationSpec;
import plugins.regions.support.RegionFilter;
import plugins.regions.support.RegionId;

/**
 * TranslationSpec that defines how to convert between
 * {@linkplain RegionFilterInput} and
 * {@linkplain RegionFilter}
 */
public class RegionFilterTranslationSpec extends ProtobufTranslationSpec<RegionFilterInput, RegionFilter> {

    @Override
    protected RegionFilter convertInputObject(RegionFilterInput inputObject) {
        List<RegionId> regionIds = new ArrayList<>();

        for (RegionIdInput regionIdInput : inputObject.getRegionIdsList()) {
            regionIds.add(this.translationEngine.convertObject(regionIdInput));
        }
        return new RegionFilter(regionIds.toArray(new RegionId[0]));
    }

    @Override
    protected RegionFilterInput convertAppObject(RegionFilter appObject) {
        RegionFilterInput.Builder builder = RegionFilterInput.newBuilder();

        for (RegionId regionId : appObject.getRegionIds()) {
            RegionIdInput regionIdInput = this.translationEngine.convertObjectAsSafeClass(regionId, RegionId.class);
            builder.addRegionIds(regionIdInput);
        }

        return builder.build();
    }

    @Override
    public Class<RegionFilter> getAppObjectClass() {
        return RegionFilter.class;
    }

    @Override
    public Class<RegionFilterInput> getInputObjectClass() {
        return RegionFilterInput.class;
    }

}
