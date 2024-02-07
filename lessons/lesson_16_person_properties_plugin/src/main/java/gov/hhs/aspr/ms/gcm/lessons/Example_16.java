package gov.hhs.aspr.ms.gcm.lessons;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.GlobalProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelPlugin;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.ModelReportLabel;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.PersonProperty;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Region;
import gov.hhs.aspr.ms.gcm.nucleus.Dimension;
import gov.hhs.aspr.ms.gcm.nucleus.Experiment;
import gov.hhs.aspr.ms.gcm.nucleus.ExperimentParameterData;
import gov.hhs.aspr.ms.gcm.nucleus.FunctionalDimension;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.GlobalPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.datamanagers.GlobalPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePlugin;
import gov.hhs.aspr.ms.gcm.plugins.people.datamanagers.PeoplePluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.PersonPropertiesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.datamanagers.PersonPropertiesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.personproperties.reports.PersonPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.properties.support.PropertyDefinition;
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import util.random.RandomGeneratorProvider;

public final class Example_16 {

	private final Path outputDirectory;

	private Example_16(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(524055747550937602L);

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.PERSON_PROPERTY_REPORT,
						outputDirectory.resolve("person_property_report.xls"))//
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

	/* start code_ref= person_properties_get_person_properties_plugin|code_cap= The person properties plugin is built with the four person properties needed to model each person.  The person property report is set to report only at the end of the simulation.*/
	private Plugin getPersonPropertiesPlugin() {
		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();
		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(0)//
				.build();
		builder.definePersonProperty(PersonProperty.EDUCATION_ATTEMPTS, propertyDefinition, 0, false);
		builder.definePersonProperty(PersonProperty.VACCINE_ATTEMPTS, propertyDefinition, 0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.build();
		builder.definePersonProperty(PersonProperty.REFUSES_VACCINE, propertyDefinition, 0, false);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();
		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition, 0, false);

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		PersonPropertyReportPluginData personPropertyReportPluginData = PersonPropertyReportPluginData.builder()//
				.setReportLabel(ModelReportLabel.PERSON_PROPERTY_REPORT)//
				.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
				.setDefaultInclusion(true)//
				.build();//

		return PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
				.getPersonPropertyPlugin();
	}
	/* end */

	private Plugin getStochasticsPlugin() {
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//
				.setMainRNGState(wellState)//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context
						.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value, 0);
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
		return getGlobalPropertyDimension(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, "intial_refusal_probability",
				values);
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
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, "education_attempt_interval",
				values);
	}

	private Dimension getEducationSuccessRatedimension() {
		double[] values = new double[] { 0.0, 0.1, 0.2 };
		return getGlobalPropertyDimension(GlobalProperty.EDUCATION_SUCCESS_RATE, "education_success_rate", values);
	}

	/* start code_ref= person_properties_get_global_properties_plugin|code_cap=The global properties plugin is initialized with several properties. */
	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(0.0)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_START_TIME, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_ATTEMPT_INTERVAL, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_ATTEMPT_INTERVAL, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.EDUCATION_SUCCESS_RATE, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.VACCINE_REFUSAL_PROBABILITY, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.IMMUNITY_PROBABILITY, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(365.0)//
				.setPropertyValueMutability(false)//
				.build();
		builder.defineGlobalProperty(GlobalProperty.SIMULATION_DURATION, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(1000)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition, 0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.getGlobalPropertiesPlugin();

	}

	/* end */
	/* start code_ref= person_properties_example_16_execute|code_cap=The various plugins are gathered from their initial data. */
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
		 * Create the region plugin 5 regions, each having a lat and lon and assign the
		 * people to random regions.
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
		/* end */

		/*
		 * Assemble and execute the experiment
		 */

		/* start code_ref= person_properties_execute_experiment|code_cap=The experiment executes 810 scenarios on 8 threads.*/

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(8)//
				.build();

		Experiment.builder()//

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
		/* end */
	}

	/* start code_ref= person_properties_example_16_main|code_cap=Executing example 16 with an output directory. */
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
	/* end */
}
