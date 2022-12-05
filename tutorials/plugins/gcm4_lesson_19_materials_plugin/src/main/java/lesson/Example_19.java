package lesson;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.IntStream;

import org.apache.commons.math3.random.RandomGenerator;

import lesson.plugins.model.ModelPlugin;
import lesson.plugins.model.actors.reports.DiseaseStateReport;
import lesson.plugins.model.support.DiseaseState;
import lesson.plugins.model.support.GlobalProperty;
import lesson.plugins.model.support.GroupType;
import lesson.plugins.model.support.Material;
import lesson.plugins.model.support.MaterialsProducer;
import lesson.plugins.model.support.ModelReportId;
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
import plugins.personproperties.actors.PersonPropertyReport;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.reports.ReportsPlugin;
import plugins.reports.ReportsPluginData;
import plugins.reports.support.NIOReportItemHandler;
import plugins.reports.support.ReportPeriod;
import plugins.resources.ResourcesPlugin;
import plugins.resources.ResourcesPluginData;
import plugins.resources.support.ResourceId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.util.properties.PropertyDefinition;
import util.random.RandomGeneratorProvider;

public final class Example_19 {

	private Example_19() {
	}

	private RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9032703880551658180L);

	private Plugin getReportsPlugin() {
		
				
		ReportsPluginData reportsPluginData = //
				ReportsPluginData	.builder()//
									.addReport(() -> new DiseaseStateReport(ModelReportId.DISEASE_STATE_REPORT, ReportPeriod.END_OF_SIMULATION)::init)//
									.addReport(() -> PersonPropertyReport.builder().setReportId(ModelReportId.PERSON_PROPERTY_REPORT).setReportPeriod(ReportPeriod.END_OF_SIMULATION).includePersonProperty(PersonProperty.CONTACT_COUNT).build()::init)//
									.build();

		return ReportsPlugin.getReportsPlugin(reportsPluginData);
	}
	
	private NIOReportItemHandler getNIOReportItemHandler() {
		return NIOReportItemHandler	.builder()//
									.addReport(ModelReportId.DISEASE_STATE_REPORT, Paths.get("c:\\temp\\gcm\\disease_state_report.xls"))//
									.addReport(ModelReportId.PERSON_PROPERTY_REPORT, Paths.get("c:\\temp\\gcm\\person_property_report.xls"))//
									.build();
	}


	private Plugin getMaterialsPlugin() {
		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();
		for (MaterialsProducer materialsProducer : MaterialsProducer.values()) {
			builder.addMaterialsProducerId(materialsProducer);
		}
		for(Material material : Material.values()) {
			builder.addMaterial(material);
		}
		MaterialsPluginData materialsPluginData = builder.build();
		return MaterialsPlugin.getMaterialsPlugin(materialsPluginData);
	}

	private Plugin getResourcesPlugin() {
		ResourcesPluginData.Builder builder = ResourcesPluginData.builder();
		for (ResourceId resourcId : Resource.values()) {
			builder.addResource(resourcId);
		}
		ResourcesPluginData resourcesPluginData = builder.build();
		return ResourcesPlugin.getResourcesPlugin(resourcesPluginData);
	}


	private Plugin getPeoplePlugin() {
		PeoplePluginData peoplePluginData = PeoplePluginData.builder().build();
		return PeoplePlugin.getPeoplePlugin(peoplePluginData);
	}

	private Plugin getRegionsPlugin() {
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();

		for (int i = 0; i < 1; i++) {
			regionsPluginDataBuilder.addRegion(new Region(i));
		}
		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		return RegionsPlugin.getRegionsPlugin(regionsPluginData);
	}

	private Plugin getStochasticsPlugin() {
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData	.builder()//

																			.setSeed(randomGenerator.nextLong())//
																			.build();

		return StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);
	}

	private Plugin getPersonPropertiesPlugin() {

		PersonPropertiesPluginData.Builder builder = PersonPropertiesPluginData.builder();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Boolean.class)//
																	.setDefaultValue(false)//
																	.build();

		builder.definePersonProperty(PersonProperty.VACCINATED, propertyDefinition);//

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
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

		PersonPropertiesPluginData personPropertiesPluginData = builder.build();

		return PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

	}

	private Plugin getGlobalPropertiesPlugin() {
		Builder builder = GlobalPropertiesPluginData.builder();//

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setPropertyValueMutability(false)//
																	.setDefaultValue(0.0)//
																	.build();

		builder.defineGlobalProperty(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.VACCINED_DOSES_PER_PERSON, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_HOME_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_SCHOOL_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.AVERAGE_WORK_SIZE, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.CHILD_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.SENIOR_POPULATION_PROPORTION, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.R0, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.COMMUNITY_CONTACT_RATE, propertyDefinition);
		

		propertyDefinition = PropertyDefinition	.builder()//
												.setType(Integer.class)//
												.setPropertyValueMutability(false)//
												.build();
		builder.defineGlobalProperty(GlobalProperty.INITIAL_INFECTIONS, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MIN_INFECTIOUS_PERIOD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.MAX_INFECTIOUS_PERIOD, propertyDefinition);
		builder.defineGlobalProperty(GlobalProperty.POPULATION_SIZE, propertyDefinition);

		builder.setGlobalPropertyValue(GlobalProperty.POPULATION_SIZE, 10_000);
		builder.setGlobalPropertyValue(GlobalProperty.VACCINED_DOSES_PER_PERSON, 0.01);
		builder.setGlobalPropertyValue(GlobalProperty.SUSCEPTIBLE_POPULATION_PROPORTION, 1.0);
		builder.setGlobalPropertyValue(GlobalProperty.INITIAL_INFECTIONS, 100);
		builder.setGlobalPropertyValue(GlobalProperty.MIN_INFECTIOUS_PERIOD, 7);
		builder.setGlobalPropertyValue(GlobalProperty.MAX_INFECTIOUS_PERIOD, 14);
		builder.setGlobalPropertyValue(GlobalProperty.R0, 2.0);
		builder.setGlobalPropertyValue(GlobalProperty.CHILD_POPULATION_PROPORTION, 0.235);
		builder.setGlobalPropertyValue(GlobalProperty.SENIOR_POPULATION_PROPORTION, 0.169);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_HOME_SIZE, 2.5);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_SCHOOL_SIZE, 250.0);
		builder.setGlobalPropertyValue(GlobalProperty.AVERAGE_WORK_SIZE, 30.0);
		builder.setGlobalPropertyValue(GlobalProperty.COMMUNITY_CONTACT_RATE, 0.01);
		

		GlobalPropertiesPluginData globalPropertiesPluginData = builder.build();

		return GlobalPropertiesPlugin.getGlobalPropertiesPlugin(globalPropertiesPluginData);

	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, double[] values) {
		Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				double value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private Dimension getGlobalPropertyDimension(GlobalPropertyId globalPropertyId, String header, int[] values) {
		Dimension.Builder dimensionBuilder = Dimension.builder();//
		IntStream.range(0, values.length).forEach((i) -> {
			dimensionBuilder.addLevel((context) -> {
				GlobalPropertiesPluginData.Builder builder = context.get(GlobalPropertiesPluginData.Builder.class);
				int value = values[i];
				builder.setGlobalPropertyValue(globalPropertyId, value);
				ArrayList<String> result = new ArrayList<>();
				result.add(Double.toString(value));
				return result;
			});//
		});
		dimensionBuilder.addMetaDatum(header);//
		return dimensionBuilder.build();
	}

	private Plugin getGroupsPlugin() {
		GroupsPluginData.Builder builder = GroupsPluginData.builder();
		for (GroupType groupType : GroupType.values()) {
			builder.addGroupTypeId(groupType);
		}
		GroupsPluginData groupsPluginData = builder.build();
		return GroupsPlugin.getGroupPlugin(groupsPluginData);
	}

	
	private Dimension getR0Dimension() {
		double[] values = new double[] { 2.0, 2.5, 3.0, 4.0, 5.0 };
		return getGlobalPropertyDimension(GlobalProperty.R0, "R0", values);
	}

	private Dimension getIntialInfectionsDimension() {
		int[] values = new int[] { 1, 10, 100, 1000 };
		return getGlobalPropertyDimension(GlobalProperty.INITIAL_INFECTIONS, "initial_infections", values);
	}
	
	private Dimension getCommunityContactRateDimension() {
		double[] values = new double[] { 0.0, 0.01, 0.05, 1.0};
		return getGlobalPropertyDimension(GlobalProperty.COMMUNITY_CONTACT_RATE, "community_contact_rate", values);
	}


	private void execute() {

		Experiment	.builder().addPlugin(getMaterialsPlugin())//
					.addPlugin(getGroupsPlugin())//
					.addPlugin(getResourcesPlugin())//
					.addPlugin(getGlobalPropertiesPlugin())//
					.addPlugin(getPersonPropertiesPlugin())//
					.addPlugin(getReportsPlugin())//
					.addPlugin(getRegionsPlugin())//
					.addPlugin(getPeoplePlugin())//
					.addPlugin(getStochasticsPlugin())//
					.addPlugin(ModelPlugin.getModelPlugin())//
					
//					.addDimension(getCommunityContactRateDimension())//
//					.addDimension(getIntialInfectionsDimension())//
//					.addDimension(getR0Dimension())//
					
					.addExperimentContextConsumer(getNIOReportItemHandler())//
					//.setThreadCount(8)//
					.reportProgressToConsole(true)//
					.build()//
					.execute();//

	}

	public static void main(String[] args) {
		new Example_19().execute();
	}

}
