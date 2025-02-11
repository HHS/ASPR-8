package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import java.util.ArrayList;
import java.util.List;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.DimensionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsPluginData;

/**
 * Dimension implementation for setting a group property to a list of values in
 * a groups plugin data.
 */
public final class GroupPropertyDimension implements Dimension {

    private final GroupPropertyDimensionData groupPropertyDimensionData;

    public GroupPropertyDimension(GroupPropertyDimensionData groupPropertyDimensionData) {
        this.groupPropertyDimensionData = groupPropertyDimensionData;
    }

    public DimensionData getDimensionData() {
        return groupPropertyDimensionData;
    }

    @Override
    public List<String> getExperimentMetaData() {
        List<String> result = new ArrayList<>();
        result.add(groupPropertyDimensionData.getGroupPropertyId().toString());
        return result;
    }

    @Override
    public int levelCount() {
        return groupPropertyDimensionData.getLevelCount();
    }

    @Override
    public List<String> executeLevel(DimensionContext dimensionContext, int level) {
        GroupId groupId = groupPropertyDimensionData.getGroupId();
        GroupPropertyId groupPropertyId = groupPropertyDimensionData.getGroupPropertyId();
        Object value = groupPropertyDimensionData.getValue(level);

        GroupsPluginData.Builder builder = dimensionContext.getPluginDataBuilder(GroupsPluginData.Builder.class);
        builder.setGroupPropertyValue(groupId, groupPropertyId, value);

        List<String> result = new ArrayList<>();
        String levelName = groupPropertyDimensionData.getLevelName(level);
        result.add(levelName);
        result.add(value.toString());
        return result;
    }

    @Override
    public String toString() {
        return "GroupPropertyDimension [groupPropertyDimensionData=" + groupPropertyDimensionData + "]";
    }
}
