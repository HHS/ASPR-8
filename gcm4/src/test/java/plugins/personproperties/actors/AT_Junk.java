package plugins.personproperties.actors;

import java.util.Map;

import org.junit.jupiter.api.Test;

import nucleus.Plugin;
import nucleus.SimplePluginId;
import nucleus.Simulation;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulationOutputConsumer;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.support.SimpleRegionId;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportId;
import plugins.util.properties.PropertyDefinition;

public class AT_Junk {

	private static enum PersonProperty implements PersonPropertyId {
		PROP
	}

	@Test
	public void test() {

		// reports
		PersonPropertyActorReport oldPersonPropertyReport = PersonPropertyActorReport//
																		.builder()//
																		.setReportId(new SimpleReportId("report"))//
																		.setReportPeriod(ReportPeriod.DAILY)//
																		.setDefaultInclusion(true)//
																		.build();//
		
		PersonPropertyReport newPersonPropertyReport = PersonPropertyReport//
				.builder()//
				.setReportId(new SimpleReportId("report"))//
				.setReportPeriod(ReportPeriod.DAILY)//
				.setDefaultInclusion(true)//
				.build();//

		ReportsPluginData reportsPluginData = ReportsPluginData//
																.builder()//
																//.addReport(() -> oldPersonPropertyReport::init)//
																.build();//

		Plugin reportsPlugin = ReportsPlugin.getReportsPlugin(reportsPluginData);

		// regions
		RegionId regionA = new SimpleRegionId("A");
		RegionId regionB = new SimpleRegionId("B");

		RegionsPluginData regionsPluginData = RegionsPluginData//
																.builder()//
																.addRegion(regionA).addRegion(regionB).build();
		Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);

		// people
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		// person properties

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();
		PersonPropertiesPluginData personPropertiesPluginData = //
				PersonPropertiesPluginData//
											.builder()//
											.definePersonProperty(PersonProperty.PROP, propertyDefinition)//
											.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

		
		//actor behaviors
		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor",new TestActorPlan(0,(c)->{
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();			
			PersonId personId = peopleDataManager.addPerson(personBuilder.add(regionA).build());
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			regionsDataManager.setPersonRegion(personId, regionB);			
			
		}));
		
		
		Plugin testPlugin = TestPlugin.getTestPlugin(pluginBuilder.build());

		// output collection
		TestSimulationOutputConsumer testSimulationOutputConsumer = new TestSimulationOutputConsumer();
		
		
		Plugin junkPlugin = Plugin.builder()//
				.setPluginId(new SimplePluginId("junk plugin"))//
				.setInitializer((c)->{
					c.addReport(newPersonPropertyReport::init);
				})
				.build();

		// simulation
		Simulation	.builder()//
					.addPlugin(reportsPlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(personPropertyPlugin)//
					.addPlugin(testPlugin)//
					.addPlugin(junkPlugin)//
					.setOutputConsumer(testSimulationOutputConsumer)//
					.build()//
					.execute();

		// output review
		Map<ReportItem, Integer> reportItemsMap = testSimulationOutputConsumer.getOutputItems(ReportItem.class);
		for (ReportItem reportItem : reportItemsMap.keySet()) {
			Integer count = reportItemsMap.get(reportItem);
			System.out.println(reportItem + " : " + count);
		}
	}
}
