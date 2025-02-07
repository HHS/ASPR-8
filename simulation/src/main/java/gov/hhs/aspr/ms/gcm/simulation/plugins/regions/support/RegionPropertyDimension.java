package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.datamanagers.RegionsPluginData;

/**
 * Dimension implementation for setting a region property to a list of values in
 * a region properties plugin data.
 */
public final class RegionPropertyDimension implements Dimension {

    private final RegionPropertyDimensionData regionPropertyDimensionData;

    public RegionPropertyDimension(RegionPropertyDimensionData regionPropertyDimensionData) {
        this.regionPropertyDimensionData = regionPropertyDimensionData;
    }

    public DimensionData getDimensionData() {
        return regionPropertyDimensionData;
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(regionPropertyDimensionData.getRegionPropertyId().toString());
        return result;
    }

    @Override
    public int levelCount() {
        return regionPropertyDimensionData.getLevelCount();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        RegionsPluginData.Builder builder = dimensionContext.getPluginDataBuilder(RegionsPluginData.Builder.class);

        RegionId regionId = regionPropertyDimensionData.getRegionId();
        RegionPropertyId regionPropertyId = regionPropertyDimensionData.getRegionPropertyId();
        Object value = regionPropertyDimensionData.getValue(level);

        builder.setRegionPropertyValue(regionId, regionPropertyId, value);

        List<String> result = new ArrayList<>();
        String levelName = regionPropertyDimensionData.getLevelName(level);
        result.add(levelName);
        result.add(value.toString());
        return result;
    }

    @Override
    public String toString() {
        return "RegionPropertyDimension [regionPropertyDimensionData=" + regionPropertyDimensionData + "]";
    }
}
