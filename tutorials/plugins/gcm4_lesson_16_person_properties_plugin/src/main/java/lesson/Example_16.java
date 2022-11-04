package lesson;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportId;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import lesson.plugins.vaccine.VaccinePlugin;
import lesson.plugins.vaccine.VaccineReport;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
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

public final class Example_16 {

	private Example_16() {
	}

	private List<PersonId> initialPeople = new ArrayList<>();
	private List<Region> initialRegions = new ArrayList<>();
	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	private Plugin getReportsPlugin() {
		ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									.addReport(() -> {
										return PersonPropertyReport	.builder()//
																	.setReportId(ModelReportId.PERSON_PROPERTY_REPORT)//
																	.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
																	.setDefaultInclusion(true)//
																	.build()::init;//
									})//
									.addReport(() -> {
										return new VaccineReport(ModelReportId.VACCINATION, //
												ReportPeriod.END_OF_SIMULATION//
										)::init;
									})//
									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportId.PERSON_PROPERTY_REPORT, //
											Paths.get("C:\\temp\\gcm\\person_property_report.xls"))//
									.addReport(ModelReportId.VACCINATION, //
											Paths.get("C:\\temp\\gcm\\vaccine_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (PersonId personId : initialPeople) {
			peoplePluginDataBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		// create the region plugin with an initial five regions, each region
		// having 200 people
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();
		for (Region region : initialRegions) {
			regionsPluginDataBuilder.addRegion(region);
		}

		for (PersonId personId : initialPeople) {
			Region region = initialRegions.get(randomGenerator.nextInt(initialRegions.size()));
			regionsPluginDataBuilder.setPersonRegion(personId, region);
		}

		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private void initializePeopleAndRegions() {
		for (int i = 0; i < 1000; i++) {
			initialPeople.add(new PersonId(i));
		}
		for (int i = 0; i < 5; i++) {
			initialRegions.add(new Region(i));
		}
	}

	private Plugin getPersonPropertiesPlugin() {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(0)//
																	.build();
		builder.definePersonProperty(PersonProperty.EDUCATION_ATTEMPTS, propertyDefinition);
		builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS, propertyDefinition);
		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(true)//
												.build();
		builder.definePersonProperty(PersonProperty.REFUSES_VACCINE, propertyDefinition);

		for (PersonId personId : initialPeople) {
			if (randomGenerator.nextDouble() < 0.25) {
				builder.setPersonPropertyValue(personId, PersonProperty.REFUSES_VACCINE, false);
			}
		}

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();
		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setSeed(randomGenerator.nextLong()).build();
		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getStochasticsDimension(int replicationCount, long seed) {
		Dimension.Builder builder = Dimension.builder();//

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<Long> seedValues = new ArrayList<>();
		for (int i = 0; i < replicationCount; i++) {
			seedValues.add(randomGenerator.nextLong());
		}

		IntStream.range(0, seedValues.size()).forEach((i) -> {
			builder.addLevel((context) -> {
				StochasticsPluginData.Builder stochasticsPluginDataBuilder = context.get(StochasticsPluginData.Builder.class);
				long seedValue = seedValues.get(i);
				stochasticsPluginDataBuilder.setSeed(seedValue);

				ArrayList<String> result = new ArrayList<>();
				result.add(Integer.toString(i));
				result.add(Long.toString(seedValue) + "L");

				return result;
			});//
		});

		builder.addMetaDatum("seed index");//
		builder.addMetaDatum("seed value");//

		return builder.build();
	}

	private void execute() {
		/*
		 * Create person ids and region ids that are shared across the plugins
		 */
		initializePeopleAndRegions();

		/*
		 * Create the reports
		 */
		Plugin reportsPlugin = getReportsPlugin();
		NIOReportItemHandler nioReportItemHandler = getNIOReportItemHandler();

		/*
		 * Create the people plugin filled with 1000 people
		 */
		Plugin peoplePlugin = getPeoplePlugin();

		/*
		 * Create the region plugin 5 regions, each having a lat and lon and
		 * assign the people to random regions.
		 * 
		 */
		Plugin regionsPlugin = getRegionsPlugin();

		// Create the person properties plugin
		Plugin personPropertiesPlugin = getPersonPropertiesPlugin();

		/*
		 * create the stochastics plugin and build a dimension with 5 seed
		 * values
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();
		Dimension stochasticsDimension = getStochasticsDimension(5, randomGenerator.nextLong());

		/*
		 * Create the vaccine and model plugins
		 */
		Plugin vaccinePlugin = VaccinePlugin.getVaccinePlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		/*
		 * Assemble and execute the experiment
		 */
		Experiment	.builder()//
					.addPlugin(personPropertiesPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(stochasticsPlugin)//
					.addPlugin(vaccinePlugin)//
					.addPlugin(reportsPlugin)//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.addDimension(stochasticsDimension)//
					.reportProgressToConsole(false)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) {
		new Example_16().execute();
	}

}
