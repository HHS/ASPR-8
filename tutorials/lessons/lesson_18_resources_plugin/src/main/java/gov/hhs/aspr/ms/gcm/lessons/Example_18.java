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
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.Resource;
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
import gov.hhs.aspr.ms.gcm.plugins.regions.RegionsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.NIOReportItemHandler;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.resources.ResourcesPlugin;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.reports.PersonResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPlugin;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.datamanagers.StochasticsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.support.WellState;
import gov.hhs.aspr.ms.gcm.plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_18 {

	private final Path outputDirectory;

	private Example_18(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	/* start code_ref=resources_getResourcesPlugin|code_cap=The resource plugin is initialized with defining the two resource ids at time zero with time tracking turned on. The person resource report is set to report at the end of the simulation.*/
	private Plugin getResourcesPlugin() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (ResourceId resourceId : Resource.values()) {
			builder.addResource(resourceId, 0.0, true);
		}
		ResourcesPluginData resourcesPluginData = builder.build();

		PersonResourceReportPluginData personResourceReportPluginData = PersonResourceReportPluginData//
				.builder()//
				.setReportLabel(ModelReportLabel.PERSON_RESOURCE_REPORT)//
				.setReportPeriod(ReportPeriod.END_OF_SIMULATION)//
				.build();

		return ResourcesPlugin.builder()//
				.setResourcesPluginData(resourcesPluginData)//
				.setPersonResourceReportPluginData(personResourceReportPluginData)//
				.getResourcesPlugin();//
	}
	/* end */

	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler.builder()//
				.addReport(ModelReportLabel.PERSON_RESOURCE_REPORT,
						outputDirectory.resolve("person_resource_report.xls"))//
				.addReport(ModelReportLabel.TREATMENT_REPORT, outputDirectory.resolve("treatment_report.xls"))//
				.addReport(ModelReportLabel.DEATH_REPORT, outputDirectory.resolve("death_report.xls"))//
				.addReport(ModelReportLabel.QUESTIONNAIRE_REPORT, outputDirectory.resolve("questionnaire_report.xls"))//
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

	private Plugin getStochasticsPlugin() {
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder()//

				.setMainRNGState(wellState)//
				.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	/* start code_ref=resources_getPersonPropertiesPlugin|code_cap=The person properties plugin is initialized with several properties.*/
	private Plugin getPersonPropertiesPlugin() {

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();

		builder.definePersonProperty(PersonProperty.IMMUNE, propertyDefinition, 0, false);//
		builder.definePersonProperty(PersonProperty.INFECTED, propertyDefinition, 0, false);//
		builder.definePersonProperty(PersonProperty.HOSPITALIZED, propertyDefinition, 0, false);//
		builder.definePersonProperty(PersonProperty.TREATED_WITH_ANTIVIRAL, propertyDefinition, 0, false);//
		builder.definePersonProperty(PersonProperty.DEAD_IN_HOME, propertyDefinition, 0, false);//
		builder.definePersonProperty(PersonProperty.DEAD_IN_HOSPITAL, propertyDefinition, 0, false);//

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Boolean.class)//
				.setDefaultValue(false)//
				.build();
		builder.definePersonProperty(PersonProperty.RECEIVED_QUESTIONNAIRE, propertyDefinition, 0, true);//

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		return PersonPropertiesPlugin.builder().setPersonPropertiesPluginData(personPropertiesPluginData)
				.getPersonPropertyPlugin();
	}
	/* end */

	/* start code_ref=resources_getGlobalPropertiesPlugin|code_cap=The global properties plugin is initialized with several properties.*/
	private Plugin getGlobalPropertiesPlugin() {
		GlobalPropertiesPluginData.Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition.builder()//
				.setType(Double.class)//
				.setDefaultValue(0.0)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_COVERAGE_TIME, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_SUCCESS_RATE, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_BEDS_PER_PERSON, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_STAY_DURATION_MIN, propertyDefinition, 0);
		builder.defineGlobalProperty(GlobalProperty.HOSPITAL_STAY_DURATION_MAX, propertyDefinition, 0);

		propertyDefinition = PropertyDefinition.builder()//
				.setType(Integer.class)//
				.setDefaultValue(10000)//
				.setPropertyValueMutability(false)//
				.build();

		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition, 0);

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData)
				.getGlobalPropertiesPlugin();
	}
	/* end */

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

	private Dimension getHospitalStayDurationDimension() {
		double[] minValues = { 2.0, 5.0 };
		double[] maxValues = { 5.0, 10.0 };

		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, minValues.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context
						.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double minValue = minValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MIN, minValue, 0);
				double maxValue = maxValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_STAY_DURATION_MAX, maxValue, 0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(minValue));
				result.add(Double.toString(maxValue));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum("hospital_stay_duration_min");//
		dimensionBuilder.addMetaDatum("hospital_stay_duration_max");//
		return dimensionBuilder.build();
	}

	private Dimension getAntiviralDosesPerPersonDimension() {
		double[] values = new double[] { .10, 0.20, 0.5 };
		return getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_DOSES_PER_PERSON, "antiviral_doses_per_person",
				values);
	}

	private Dimension getHospitalBedsPerPersonDimension() {
		double[] values = new double[] { 0.001, 0.003, 0.005 };
		return getGlobalPropertyDimension(GlobalProperty.HOSPITAL_BEDS_PER_PERSON, "hospital_beds_per_person", values);
	}

	private Dimension getHospitalSuccessDimension() {
		double[] minValues = { 0.30, 5.0 };
		double[] maxValues = { 0.50, 0.75 };

		FunctionalDimension.Builder dimensionBuilder = FunctionalDimension.builder();//
		IntStream.range(0, minValues.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context
						.getPluginDataBuilder(GlobalPropertiesPluginData.Builder.class);
				double minValue = minValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITHOUT_ANTIVIRAL, minValue, 0);
				double maxValue = maxValues[i];
				builder.setGlobalPropertyValue(GlobalProperty.HOSPITAL_SUCCESS_WITH_ANTIVIRAL, maxValue, 0);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(minValue));
				result.add(Double.toString(maxValue));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum("hospital_success_without_antiviral");//
		dimensionBuilder.addMetaDatum("hospital_success_with_antiviral");//
		return dimensionBuilder.build();
	}

	private Dimension getAntiviralSuccessRateDimension() {
		double[] values = new double[] { .50, 0.8 };
		return getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_SUCCESS_RATE, "antiviral_success_rate", values);
	}

	private Dimension getAntiviralCoverageTimeDimension() {
		double[] values = new double[] { 10.0, 15.0 };
		return getGlobalPropertyDimension(GlobalProperty.ANTIVIRAL_COVERAGE_TIME, "antiviral_coverage_time", values);
	}

	private Dimension getMaximumSymptomOnsetTimeDimension() {
		double[] values = new double[] { 60, 120 };
		return getGlobalPropertyDimension(GlobalProperty.MAXIMUM_SYMPTOM_ONSET_TIME, "maximum_symptom_onset_time",
				values);
	}

	private Dimension getSusceptiblePopulationProportionDimension() {
		double[] values = new double[] { 0.25, 0.5, 0.75 };
		return getGlobalPropertyDimension(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION,
				"susceptible_population_proportion", values);
	}

	/* start code_ref=resources_execute|code_cap=The various plugins are gathered from their initial data, dimensions are added and the experiment is executed over 864 scenarios using 8 threads.*/
	private void execute() {

		ExperimentParameterData experimentParameterData = ExperimentParameterData.builder()//
				.setThreadCount(8)//
				.build();

		Experiment.builder()

				.addPlugin(getResourcesPlugin())//
				.addPlugin(getGlobalPropertiesPlugin())//
				.addPlugin(getPersonPropertiesPlugin())//
				.addPlugin(getRegionsPlugin())//
				.addPlugin(getPeoplePlugin())//
				.addPlugin(getStochasticsPlugin())//
				.addPlugin(ModelPlugin.getModelPlugin())//

				.addDimension(getMaximumSymptomOnsetTimeDimension())//
				.addDimension(getSusceptiblePopulationProportionDimension())//
				.addDimension(getAntiviralCoverageTimeDimension())//
				.addDimension(getAntiviralSuccessRateDimension())//
				.addDimension(getHospitalSuccessDimension())//
				.addDimension(getHospitalBedsPerPersonDimension())//
				.addDimension(getAntiviralDosesPerPersonDimension())//
				.addDimension(getHospitalStayDurationDimension())//

				.addExperimentContextConsumer(getNIOReportItemHandler())//
				.setExperimentParameterData(experimentParameterData)//
				.build()//
				.execute();//
	}
	/* end */

	/* start code_ref=resources_main|code_cap=Executing example 18 with an output directory.*/
	public static void main(String[] args) throws IOException {
		if (args.length == 0) {
			throw new RuntimeException("One output directory argument is required");
		}
		Path outputDirectory = Paths.get(args[0]);
		if (!Files.exists(outputDirectory)) {
			Files.createDirectory(outputDirectory);
		} else {
			if (!Files.isDirectory(outputDirectory)) {
				throw new IOException("Provided path is not a directory");
			}
		}

		new Example_18(outputDirectory).execute();
	}
	/* end */

}
