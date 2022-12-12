package lesson;

import java.nio.file.Paths;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.ModelReportId;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.Region;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.actors.PersonPropertyReport;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;
import util.time.TimeElapser;

public final class RecordEventsTest {

	public static void main(final String[] args) {
		new RecordEventsTest().execute();
	}

	private final RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	private RecordEventsTest() {
	}

	private void execute() {

		TimeElapser timeElapser = new TimeElapser();
		Experiment	.builder()//		
					.addPlugin(getGlobalPropertiesPlugin())//
					.addPlugin(getPersonPropertiesPlugin())//
					.addPlugin(getReportsPlugin())//
					.addPlugin(getRegionsPlugin())//
					.addPlugin(getPeoplePlugin())//
					.addPlugin(getStochasticsPlugin())//
					.addPlugin(ModelPlugin.getModelPlugin())//

					.addExperimentContextConsumer(getNIOReportItemHandler())//
					//.setThreadCount(8)//
					.reportProgressToConsole(true)//
					.build()//
					.execute();//

		System.out.println("total time = "+timeElapser.getElapsedMilliSeconds());
	}


	private Plugin getGlobalPropertiesPlugin() {
		final Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setPropertyValueMutability(false)//
																	.setDefaultValue(0)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MAX_UPDATE_COUNT, propertyDefinition);


		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_UPDATE_COUNT, 10_000_000);

		final GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);

	}

	




	private Plugin getPeoplePlugin() {
		final PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getPersonPropertiesPlugin() {

		final PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();

		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition);//
		builder.definePersonProperty(PersonProperty.VACCINE_SCHEDULED, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(0)//
												.build();//
		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(0)//
												.build();//
		builder.definePersonProperty(PersonProperty.CONTACT_COUNT, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(DiseaseState.class)//
												.setDefaultValue(DiseaseState.SUSCEPTIBLE)//
												.build();

		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition);//

		final PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

	}


	private Plugin getRegionsPlugin() {
		final RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 1; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		final RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private Plugin getReportsPlugin() {

		final ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									
									.addReport(() -> PersonPropertyReport	.builder()//
																			.setReportId(ModelReportId.PERSON_PROPERTY_REPORT)//
																			.setReportPeriod(ReportPeriod.DAILY)//
																			.setDefaultInclusion(true)//																			
																			.build()::init)//
									
									

									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									
									.addReport(ModelReportId.PERSON_PROPERTY_REPORT, Paths.get("c:\\temp\\gcm\\person_property_report.xls"))//
									
									.build();
	}


	private Plugin getStochasticsPlugin() {

		final StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//

																					.setSeed(randomGenerator.nextLong())//
																					.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

}
