package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;

/**
 * Dimension implementation for setting a global property to a list of values in
 * a global properties plugin data.
 */
public final class GlobalPropertyDimension implements Dimension {

    private final GlobalPropertyDimensionData globalPropertyDimensionData;

    public GlobalPropertyDimension(GlobalPropertyDimensionData globalPropertyDimensionData) {
        this.globalPropertyDimensionData = globalPropertyDimensionData;
    }

    public DimensionData getDimensionData() {
        return globalPropertyDimensionData;
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(globalPropertyDimensionData.getGlobalPropertyId().toString());
        return result;
    }

    @Override
    public int levelCount() {
        return globalPropertyDimensionData.getLevelCount();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        String levelName = globalPropertyDimensionData.getLevelName(level);

        GlobalPropertiesPluginData.Builder builder = dimensionContext
                .getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);

        Object value = globalPropertyDimensionData.getValue(level);
        GlobalPropertyId globalPropertyId = globalPropertyDimensionData.getGlobalPropertyId();
        double assignmentTime = globalPropertyDimensionData.getAssignmentTime();

        builder.setGlobalPropertyValue(globalPropertyId, value, assignmentTime);

        List<String> result = new ArrayList<>();
        result.add(levelName);
        result.add(value.toString());
        return result;
    }

    @Override
    public String toString() {
        return "GlobalPropertyDimension [globalPropertyDimensionData=" + globalPropertyDimensionData + "]";
    }

}
