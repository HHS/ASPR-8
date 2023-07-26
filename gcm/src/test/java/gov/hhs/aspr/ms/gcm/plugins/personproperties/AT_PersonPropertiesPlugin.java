package gov.hhs.aspr.ms.gcm.plugins.personproperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;

public class AT_PersonPropertiesPlugin {

	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.Builder.class, name = "getPersonPropertyPlugin", args = {})
	public void testGetPersonPropertyPlugin() {
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();
		Plugin personPropertiesPlugin = PersonPropertiesPlugin.builder()
				.setPersonPropertiesPluginData(personPropertiesPluginData).getPersonPropertyPlugin();

		assertEquals(1, personPropertiesPlugin.getPluginDatas().size());
		assertTrue(personPropertiesPlugin.getPluginDatas().contains(personPropertiesPluginData));

		assertEquals(PersonPropertiesPluginId.PLUGIN_ID, personPropertiesPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(RegionsPluginId.PLUGIN_ID);

		assertEquals(expectedDependencies, personPropertiesPlugin.getPluginDependencies());

	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PersonPropertiesPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.Builder.class, name = "setPersonPropertyInteractionReportPluginData", args = {
			PersonPropertyInteractionReportPluginData.class })
	public void testSetGroupPropertyReportPluginData() {
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();
		PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData = PersonPropertyInteractionReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel("test")).setReportPeriod(ReportPeriod.DAILY).build();

		Plugin personPropertiesPlugin = PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)
				.setPersonPropertyInteractionReportPluginData(personPropertyInteractionReportPluginData)
				.getPersonPropertyPlugin();

		assertTrue(personPropertiesPlugin.getPluginDatas().contains(personPropertyInteractionReportPluginData));
	}
	
	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.Builder.class, name = "setPersonPropertyReportPluginData", args = {
			PersonPropertyReportPluginData.class })
	public void testSetPersonPropertyReportPluginData() {
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();
		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel("test")).setReportPeriod(ReportPeriod.DAILY).build();

		Plugin personPropertiesPlugin = PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)
				.getPersonPropertyPlugin();

		assertTrue(personPropertiesPlugin.getPluginDatas().contains(personPropertyReportPluginData));
	}

	@Test
	@UnitTestMethod(target = PersonPropertiesPlugin.Builder.class, name = "setPersonPropertiesPluginData", args = {
			PersonPropertiesPluginData.class })
	public void testSetPersonPropertiesPluginData() {
		PersonPropertiesPluginData personPropertiesPluginData = PersonPropertiesPluginData.builder().build();

		Plugin personPropertiesPlugin = PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)		
				.getPersonPropertyPlugin();

		assertTrue(personPropertiesPlugin.getPluginDatas().contains(personPropertiesPluginData));
	}

}
