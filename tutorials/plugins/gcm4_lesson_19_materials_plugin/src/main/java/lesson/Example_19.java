package lesson;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialsProducer;
import lesson.plugins.model.support.ModelReportLabel;
import lesson.plugins.model.support.PersonProperty;
import lesson.plugins.model.support.Region;
import lesson.plugins.model.support.Resource;
import nucleus.Dimension;
import nucleus.Experiment;
import nucleus.Plugin;
import plugins.globalproperties.GlobalPropertiesPlugin;
import plugins.globalproperties.GlobalPropertiesPluginData;
import plugins.globalproperties.GlobalPropertiesPluginData.Builder;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.GroupsPlugin;
import plugins.groups.GroupsPluginData;
import plugins.materials.MaterialsPlugin;
import plugins.materials.MaterialsPluginData;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.WellState;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_19 {

	private final Path outputDirectory;

	public static void main(final String[] args) throws IOException {
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

		new Example_19(outputDirectory).execute();
	}

	private final RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	private Example_19(Path outputDirectory) {
		this.outputDirectory = outputDirectory;
	}

	private void execute() {

		Experiment	.builder()//
					.addPlugin(getMaterialsPlugin())//
					.addPlugin(getResourcesPlugin())//
					.addPlugin(getGlobalPropertiesPlugin())//
					.addPlugin(getPersonPropertiesPlugin())//					
					.addPlugin(getStochasticsPlugin())//
					.addPlugin(getRegionsPlugin())//
					.addPlugin(getPeoplePlugin())//
					.addPlugin(getGroupsPlugin())//
					.addPlugin(ModelPlugin.getModelPlugin())//

					.addDimension(getInfectionThresholdDimension())//
					.addDimension(getCommunityContactRateDimension())//
					.addDimension(getIntialInfectionsDimension())//
					.addDimension(getR0Dimension())//

					.addExperimentContextConsumer(getNIOReportItemHandler())//
					.setThreadCount(8)//
					.build()//
					.execute();//

	}

	private Dimension getCommunityContactRateDimension() {
		final Double[] values = new Double[] { 0.0, 0.01, 0.05 };
		return getGlobalPropertyDimension(GlobalProperty.COMMUNITY_CONTACT_RATE, "community_contact_rate", values);
	}

	private Plugin getGlobalPropertiesPlugin() {
		final Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.setDefaultValue(0.0)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.COMMUNITY_CONTACT_RATE, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.INFECTION_THRESHOLD, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setPropertyValueMutability(false)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTIONS, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.MIN_INFECTIOUS_PERIOD, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.MAX_INFECTIOUS_PERIOD, propertyDefinition,0);
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition,0);

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Boolean.class)//
												.setDefaultValue(false)//
												.setPropertyValueMutability(true)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.MANUFACTURE_VACCINE, propertyDefinition,0);

		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000,0);
		builder.setGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, 1.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS, 1,0);
		builder.setGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD, 7,0);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD, 14,0);
		builder.setGlobalPropertyValue(GlobalProperty.R0, 2.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235,0);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.INFECTION_THRESHOLD, 0.0,0);
		builder.setGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE, 0.0,0);

		final GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.builder().setGlobalPropertiesPluginData(globalPropertiesPluginData).getGlobalPropertiesPlugin();

	}

	private Dimension getGlobalPropertyDimension(final GlobalPropertyId globalPropertyId, final String header, final Object[] values) {
		final Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				final GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				final Object value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value,0);
				final ArrayList<String> result = new ArrayList<>();
				result.add(value.toString());
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private Plugin getGroupsPlugin() {
		final GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (final GroupType groupType : GroupType.values()) {
			builder.addGroupTypeId(groupType);
		}
		final GroupsPluginData groupsPluginData = builder.build();
		return GroupsPlugin.builder().setGroupsPluginData(groupsPluginData).getGroupsPlugin();
	}

	private Dimension getIntialInfectionsDimension() {
		final Integer[] values = new Integer[] { 1, 10, 100 };
		return getGlobalPropertyDimension(GlobalProperty.INITIAL_INFECTIONS, "initial_infections", values);
	}

	private Plugin getMaterialsPlugin() {
		final MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (final MaterialsProducer materialsProducer : MaterialsProducer.values()) {
			builder.addMaterialsProducerId(materialsProducer);
		}
		for (final Material material : Material.values()) {
			builder.addMaterial(material);
		}
		final MaterialsPluginData materialsPluginData = builder.build();
		return MaterialsPlugin.builder().setMaterialsPluginData(materialsPluginData).getMaterialsPlugin();
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

		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition,0,false);//
		builder.definePersonProperty(PersonProperty.VACCINE_SCHEDULED, propertyDefinition,0,false);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.build();//
		builder.definePersonProperty(PersonProperty.AGE, propertyDefinition,0,false);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(DiseaseState.class)//
												.setDefaultValue(DiseaseState.SUSCEPTIBLE)//
												.build();

		builder.definePersonProperty(PersonProperty.DISEASE_STATE, propertyDefinition,0,false);//

		final PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		PersonPropertyReportPluginData personPropertyReportPluginData = //
				PersonPropertyReportPluginData	.builder()//
												.setReportLabel(ModelReportLabel.PERSON_PROPERTY_REPORT)//
												.setReportPeriod(ReportPeriod.DAILY)//
												.includePersonProperty(PersonProperty.VACCINATED)//
												.includePersonProperty(PersonProperty.VACCINE_SCHEDULED)//
												.build();

		return PersonPropertiesPlugin.builder()//
				.setPersonPropertiesPluginData(personPropertiesPluginData)//
				.setPersonPropertyReportPluginData(personPropertyReportPluginData)//
				.getPersonPropertyPlugin();

	}

	private Dimension getR0Dimension() {
		final Double[] values = new Double[] { 2.0, 2.5, 3.0 };
		return getGlobalPropertyDimension(GlobalProperty.R0, "R0", values);
	}

	private Dimension getInfectionThresholdDimension() {
		final Double[] values = new Double[] { 0.01, 0.02, 0.05 };
		return getGlobalPropertyDimension(GlobalProperty.INFECTION_THRESHOLD, "infection_threshold", values);
	}

	private Plugin getRegionsPlugin() {
		final RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 1; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		final RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.builder().setRegionsPluginData(regionsPluginData).getRegionsPlugin();
	}

	
	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportLabel.DISEASE_STATE_REPORT, outputDirectory.resolve("disease_state_report.xls"))//
									.addReport(ModelReportLabel.PERSON_PROPERTY_REPORT, outputDirectory.resolve("person_property_report.xls"))//
									.addReport(ModelReportLabel.VACCINE_REPORT, outputDirectory.resolve("vaccine_report.xls"))//
									.addReport(ModelReportLabel.VACCINE_PRODUCTION_REPORT, outputDirectory.resolve("vaccine_production_report.xls"))//
									.build();
	}

	private Plugin getResourcesPlugin() {
		final ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (final ResourceId resourcId : Resource.values()) {
			builder.addResource(resourcId);
		}
		final ResourcesPluginData resourcesPluginData = builder.build();
		return ResourcesPlugin.builder().setResourcesPluginData(resourcesPluginData).getResourcesPlugin();
	}

	private Plugin getStochasticsPlugin() {
		WellState wellState = WellState.builder().setSeed(randomGenerator.nextLong()).build();
		final StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//
																					.setMainRNGState(wellState)//
																					.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

}
