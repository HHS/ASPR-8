package gov.hhs.aspr.ms.gcm.plugins.groups;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.plugins.groups.datamanagers.GroupsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPopulationReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.groups.reports.GroupPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import util.annotations.UnitTestMethod;

public class AT_GroupsPlugin {

    @Test
    @UnitTestMethod(target = GroupsPlugin.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GroupsPlugin.builder());
    }

    @Test
    @UnitTestMethod(target = GroupsPlugin.Builder.class, name = "setGroupPropertyReportPluginData", args = {
            GroupPropertyReportPluginData.class })
    public void testSetGroupPropertyReportPluginData() {
        GroupsPluginData groupsPluginData = GroupsPluginData.builder().build();
        GroupPropertyReportPluginData groupPropertyReportPluginData = GroupPropertyReportPluginData.builder()
                .setReportLabel(new SimpleReportLabel("test"))
                .setReportPeriod(ReportPeriod.DAILY)
                .build();

        Plugin groupPlugin = GroupsPlugin.builder()
                .setGroupsPluginData(groupsPluginData)
                .setGroupPropertyReportPluginData(groupPropertyReportPluginData)
                .getGroupsPlugin();

        assertTrue(groupPlugin.getPluginDatas().contains(groupPropertyReportPluginData));
    }

    @Test
    @UnitTestMethod(target = GroupsPlugin.Builder.class, name = "setGroupPopulationReportPluginData", args = {
            GroupPopulationReportPluginData.class })
    public void testSetGroupPopulationReportPluginData() {
        GroupsPluginData groupsPluginData = GroupsPluginData.builder().build();
        GroupPopulationReportPluginData groupPopulationReportPluginData = GroupPopulationReportPluginData.builder()
                .setReportLabel(new SimpleReportLabel("test"))
                .setReportPeriod(ReportPeriod.DAILY)
                .build();

        Plugin groupPlugin = GroupsPlugin.builder()
                .setGroupsPluginData(groupsPluginData)
                .setGroupPopulationReportPluginData(groupPopulationReportPluginData)
                .getGroupsPlugin();

        assertTrue(groupPlugin.getPluginDatas().contains(groupPopulationReportPluginData));
    }

    @Test
    @UnitTestMethod(target = GroupsPlugin.Builder.class, name = "setGroupsPluginData", args = {
            GroupsPluginData.class })
    public void testSetGroupsPluginData() {

        GroupsPluginData groupsPluginData = GroupsPluginData.builder().build();
        Plugin groupPlugin = GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();

        assertTrue(groupPlugin.getPluginDatas().contains(groupsPluginData));
    }

    @Test
    @UnitTestMethod(target = GroupsPlugin.Builder.class, name = "getGroupsPlugin", args = {})
    public void testGetGroupsPlugin() {

        GroupsPluginData groupsPluginData = GroupsPluginData.builder().build();
        Plugin groupPlugin = GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();

        assertEquals(1, groupPlugin.getPluginDatas().size());
        assertTrue(groupPlugin.getPluginDatas().contains(groupsPluginData));

        assertEquals(GroupsPluginId.PLUGIN_ID, groupPlugin.getPluginId());

        Set<PluginId> expectedDependencies = new LinkedHashSet<>();
        expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
        expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);

        assertEquals(expectedDependencies, groupPlugin.getPluginDependencies());

    }

}
