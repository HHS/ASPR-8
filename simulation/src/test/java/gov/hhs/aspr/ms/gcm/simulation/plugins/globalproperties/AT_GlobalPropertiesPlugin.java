package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.reports.GlobalPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_GlobalPropertiesPlugin {

    @Test
    @UnitTestMethod(target = GlobalPropertiesPlugin.class, name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GlobalPropertiesPlugin.builder());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesPlugin.Builder.class, name = "setGlobalPropertiesPluginData", args = {
            GlobalPropertiesPluginData.class })
    public void testSetGlobalPropertiesPluginData() {

        GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesPluginData.builder().build();
        Plugin globalsPlugin = GlobalPropertiesPlugin.builder()
                .setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();

        assertTrue(globalsPlugin.getPluginDatas().contains(globalPropertiesPluginData));
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesPlugin.Builder.class, name = "setGlobalPropertyReportPluginData", args = {
            GlobalPropertyReportPluginData.class })
    public void testSetGlobalPropertyReportPluginData() {
        GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesPluginData.builder().build();
        GlobalPropertyReportPluginData globalPropertyReportPluginData = GlobalPropertyReportPluginData.builder()
                .setReportLabel(new SimpleReportLabel("test")).build();
        Plugin globalsPlugin = GlobalPropertiesPlugin.builder()
                .setGlobalPropertiesPluginData(globalPropertiesPluginData)
                .setGlobalPropertyReportPluginData(globalPropertyReportPluginData).getGlobalPropertiesPlugin();

        assertTrue(globalsPlugin.getPluginDatas().contains(globalPropertyReportPluginData));
    }

    @Test
    @UnitTestMethod(target = GlobalPropertiesPlugin.Builder.class, name = "getGlobalPropertiesPlugin", args = {})
    public void testGetGlobalPropertiesPlugin() {
        /*
         * Show that the plugin contains the plugin data and has the property id and
         * dependencies
         */

        GlobalPropertiesPluginData globalPropertiesPluginData = GlobalPropertiesPluginData.builder().build();
        Plugin globalsPlugin = GlobalPropertiesPlugin.builder()
                .setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();

        assertTrue(globalsPlugin.getPluginDatas().contains(globalPropertiesPluginData));
        assertEquals(GlobalPropertiesPluginId.PLUGIN_ID, globalsPlugin.getPluginId());

        Set<PluginId> expectedDependencies = new LinkedHashSet<>();
        assertEquals(expectedDependencies, globalsPlugin.getPluginDependencies());

    }

}
