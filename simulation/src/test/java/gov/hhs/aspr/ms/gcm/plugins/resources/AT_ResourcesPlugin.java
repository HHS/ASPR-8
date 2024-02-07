package gov.hhs.aspr.ms.gcm.plugins.resources;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.PersonResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.ResourcePropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.ResourceReportPluginData;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class AT_ResourcesPlugin {

	@Test
	@UnitTestMethod(target = ResourcesPlugin.Builder.class, name = "getResourcesPlugin", args = { })
	public void testGetResourcesPlugin() {

		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		Plugin resourcesPlugin = ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData).getResourcesPlugin();

		assertEquals(1, resourcesPlugin.getPluginDatas().size());
		assertTrue(resourcesPlugin.getPluginDatas().contains(resourcesPluginData));

		assertEquals(ResourcesPluginId.PLUGIN_ID, resourcesPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(RegionsPluginId.PLUGIN_ID);

		assertEquals(expectedDependencies, resourcesPlugin.getPluginDependencies());

	}

	@Test
	@UnitTestMethod(target = ResourcesPlugin.class, name = "builder", args= {})
	public void testBuilder() {
		assertNotNull(ResourcesPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = ResourcesPlugin.Builder.class, name = "setPersonResourceReportPluginData", args = {PersonResourceReportPluginData.class})
	public void testSetPersonResourceReportPluginData() {
		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		
		PersonResourceReportPluginData personResourceReportPluginData = PersonResourceReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("PersonResourceReport"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build();//
		
		Plugin resourcePlugin = ResourcesPlugin.builder()//
				.setPersonResourceReportPluginData(personResourceReportPluginData)//
				.setResourcesPluginData(resourcesPluginData)//
				.getResourcesPlugin();//

		assertTrue(resourcePlugin.getPluginDatas().contains(personResourceReportPluginData));
	}

	@Test
	@UnitTestMethod(target = ResourcesPlugin.Builder.class, name = "setResourcePropertyReportPluginData", args = {ResourcePropertyReportPluginData.class})
	public void testSetResourcePropertyReportPluginData() {
		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		
		ResourcePropertyReportPluginData resourcePropertyReportPluginData = ResourcePropertyReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("ResourcePropertyReport"))//				
				.build();//
		
		Plugin resourcePlugin = ResourcesPlugin.builder()//
				.setResourcePropertyReportPluginData(resourcePropertyReportPluginData)//
				.setResourcesPluginData(resourcesPluginData)//
				.getResourcesPlugin();//

		assertTrue(resourcePlugin.getPluginDatas().contains(resourcePropertyReportPluginData));
	}

	@Test
	@UnitTestMethod(target = ResourcesPlugin.Builder.class, name = "setResourceReportPluginData", args = {ResourceReportPluginData.class})
	public void testSetResourceReportPluginData() {
		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		
		ResourceReportPluginData resourceReportPluginData = ResourceReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("ResourceReport"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build();//
		
		Plugin resourcePlugin = ResourcesPlugin.builder()//
				.setResourceReportPluginData(resourceReportPluginData)//
				.setResourcesPluginData(resourcesPluginData)//
				.getResourcesPlugin();//

		assertTrue(resourcePlugin.getPluginDatas().contains(resourceReportPluginData));
	}
 
	@Test
	@UnitTestMethod(target = ResourcesPlugin.Builder.class, name = "setResourcesPluginData", args = {ResourcesPluginData.class})
	public void testSetResourcesPluginData() {
		ResourcesPluginData resourcesPluginData = ResourcesPluginData.builder().build();
		
		Plugin resourcePlugin = ResourcesPlugin.builder()//				
				.setResourcesPluginData(resourcesPluginData)//
				.getResourcesPlugin();//

		assertTrue(resourcePlugin.getPluginDatas().contains(resourcesPluginData));
	}

}
