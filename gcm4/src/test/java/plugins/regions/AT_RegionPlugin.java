package plugins.regions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.PluginId;
import plugins.people.PeoplePluginId;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.regions.reports.RegionPropertyReportPluginData;
import plugins.regions.reports.RegionTransferReportPluginData;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_RegionPlugin {

	

	@Test
	@UnitTestMethod(target = RegionsPlugin.Builder.class,name = "getRegionsPlugin", args = {})
	public void testGetRegionPlugin() {
		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
		Plugin regionPlugin = RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();

		assertEquals(1,regionPlugin.getPluginDatas().size());
		assertTrue(regionPlugin.getPluginDatas().contains(regionsPluginData));

		assertEquals(RegionsPluginId.PLUGIN_ID, regionPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		
		assertEquals(expectedDependencies, regionPlugin.getPluginDependencies());

	}
	
//	RegionsPlugin	public static plugins.regions.RegionsPlugin$Builder plugins.regions.RegionsPlugin.builder()
	@Test
	@UnitTestMethod(target = RegionsPlugin.class,name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(RegionsPlugin.builder());
	}


	
	@Test
    @UnitTestMethod(target = RegionsPlugin.Builder.class, name = "setRegionsPluginData", args = {
    		RegionsPluginData.class })
    public void testSetRegionsPluginData() {
		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
       

        Plugin regionsPlugin = RegionsPlugin.builder()
                .setRegionsPluginData(regionsPluginData)               
                .getRegionsPlugin();

        assertTrue(regionsPlugin.getPluginDatas().contains(regionsPluginData));
    }
	

	@Test
    @UnitTestMethod(target = RegionsPlugin.Builder.class, name = "setRegionPropertyReportPluginData", args = {
    		RegionPropertyReportPluginData.class })
    public void testSetRegionPropertyReportPluginData() {
		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
		RegionPropertyReportPluginData regionPropertyReportPluginData = RegionPropertyReportPluginData.builder().setReportLabel(new SimpleReportLabel("RegionPropertyReport")).build();

        Plugin regionsPlugin = RegionsPlugin.builder()//
                .setRegionsPluginData(regionsPluginData)//
                .setRegionPropertyReportPluginData(regionPropertyReportPluginData)//
                .getRegionsPlugin();

        assertTrue(regionsPlugin.getPluginDatas().contains(regionPropertyReportPluginData));
    }

	@Test
    @UnitTestMethod(target = RegionsPlugin.Builder.class, name = "setRegionTransferReportPluginData", args = {
    		RegionTransferReportPluginData.class })
    public void setRegionTransferReportPluginData() {
		RegionsPluginData regionsPluginData = RegionsPluginData.builder().build();
		RegionTransferReportPluginData regionTransferReportPluginData = RegionTransferReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("RegionPropertyReport"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build();

        Plugin regionsPlugin = RegionsPlugin.builder()//
                .setRegionsPluginData(regionsPluginData)//
                .setRegionTransferReportPluginData(regionTransferReportPluginData)//
                .getRegionsPlugin();

        assertTrue(regionsPlugin.getPluginDatas().contains(regionTransferReportPluginData));
    }



}
