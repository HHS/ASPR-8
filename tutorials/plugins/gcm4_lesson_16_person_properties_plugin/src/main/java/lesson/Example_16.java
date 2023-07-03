package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.GlobalProperty;
import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.ModelReportLabel;
import lesson.plugins.model.PersonProperty;
import lesson.plugins.model.Region;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.ExperimentParameterData;
import nucleus.FunctionalDimension;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.datamanagers.RegionsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_16 {

	private final Path outputDirectory;

	private Example_16(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportLabel.PERSON_PROPERTY_REPORT, outputDirectory.resolve("person_property_report.xls"))//
									.addReport(ModelReportLabel.VACCINATION, outputDirectory.resolve("vaccination_report.xls"))//
									.build();
	}

	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 5; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	private Plugin getPersonPropertiesPlugin() {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(0)//
																	.build();
		builder.definePersonProperty(PersonProperty.EDUCATION_ATTEMPTS, propertyDefinition,0,false);
		builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS, propertyDefinition, 0, false);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.build();
		builder.definePersonProperty(PersonProperty.REFUSES_VACCINE, propertyDefinition,0,false);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.build();
		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition,0,false);

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData	.builder()//
																										.setReportLabel(ModelReportLabel.PERSON_PROPERTY_REPORT)//
																										.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
																										.setDefaultInclusion(true)//
																										.build();//

		return PersonPropertiesPlugin	.builder()//
										.setPersonPropertiesPluginData(personPropertiesPluginData)//
										.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
										.getPersonPropertyPlugin();
	}

	private Plugin getStochasticsPlugin() {
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																			.setMainRNGState(wellState)//
																			.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value,0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private Dimension getVaccineRefusalProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.25, 0.5, 0.75, 1.0 };
		return getGlobalPropertyDimension(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, "intial_refusal_probability", values);
	}

	private Dimension getImmunityStartTimeDimension() {
		double[] values = new double[] { 120.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_START_TIME, "immunity_start_time", values);
	}

	private Dimension getImmunityProbabilityDimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2 };
		return getGlobalPropertyDimension(GlobalProperty.IMMUNITY_PROBABILITY, "immunity_probabilty", values);
	}

	private Dimension getVaccineAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 45.0, 60.0 };

		return getGlobalPropertyDimension(GlobalProperty.VACCINE_ATTEMPT_INTERVAL, "vaccine_atttempt_interval", values);
	}

	private Dimension getEducationAttemptIntervalDimension() {
		double[] values = new double[] { 30.0, 60.0, 180.0 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, "education_attempt_interval", values);
	}

	private Dimension getEducationSuccessRatedimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_SUCCESS_RATE, "education_success_rate", values);
	}

	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(0.0)//
																	.setPropertyValueMutability(false)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_START_TIME, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_ATTEMPT_INTERVAL, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_SUCCESS_RATE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_PROBABILITY, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Double.class)//
												.setDefaultValue(365.0)//
												.setPropertyValueMutability(false)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.SIMULATION_DURATION, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setDefaultValue(1000)//
												.setPropertyValueMutability(false)//
												.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition,0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();

	}

	private void execute() {

		/*
		 * Create the global properties plugin
		 */
		Plugin globalPropertiesPlugin = getGlobalPropertiesPlugin();

		/*
		 * Create the reports
		 */
		
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
		 * create the stochastics plugin
		 */
		Plugin stochasticsPlugin = getStochasticsPlugin();

		Plugin modelPlugin = ModelPlugin.getModelPlugin();

		/*
		 * Assemble and execute the experiment
		 */
		
		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(8)//				
				.build();

		Experiment	.builder()//

					.addPlugin(personPropertiesPlugin)//
					.addPlugin(globalPropertiesPlugin)//
					.addPlugin(modelPlugin)//
					.addPlugin(regionsPlugin)//
					.addPlugin(peoplePlugin)//
					.addPlugin(stochasticsPlugin)//
					

					.addDimension(getImmunityStartTimeDimension())//
					.addDimension(getImmunityProbabilityDimension())//
					.addDimension(getVaccineAttemptIntervalDimension())//
					.addDimension(getEducationAttemptIntervalDimension())//
					.addDimension(getEducationSuccessRatedimension())//
					.addDimension(getVaccineRefusalProbabilityDimension())//
					.addExperimentContextConsumer(nioReportItemHandler)//
					.setExperimentParameterData(experimentParameterData)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("One output directory argument is required");
		}
		Path outputPath = Paths.get(args[0]);
		if (!Files.exists(outputPath)) {
			Files.createDirectory(outputPath);
		} else {
			if (!Files.isDirectory(outputPath)) {
				throw new IOException("Provided path is not a directory");
			}
		}
		new Example_16(outputPath).execute();
	}

}
