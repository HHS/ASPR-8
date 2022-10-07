package temp.filtereventtests;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.Experiment;
import nucleus.Plugin;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import temp.filtereventtests.plugins.model.ModelPlugin;
import temp.filtereventtests.plugins.model.ModelPluginData;
import util.random.RandomGeneratorProvider;
import util.time.TimeElapser;

public final class Driver {

	private Driver() {
	}

	public static void main(String[] args) {

		long seed = 57858904586956L;
		int populationCount = 1_000_000;
		int regionCount = 500;
		boolean useEventFilters = true;
		int eventCount = 1_000_000;

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		List<PersonId> people = new ArrayList<>();
		for (int i = 0; i < populationCount; i++) {
			people.add(new PersonId(i));
		}

		List<RegionIdentifier> regionIdentifiers = new ArrayList<>();
		for (int i = 0; i < regionCount; i++) {
			regionIdentifiers.add(new RegionIdentifier(i));
		}

		// build the regions plugin
		RegionsPluginData.Builder regionsPluginDataBuilder = RegionsPluginData.builder();
		for (RegionIdentifier regionIdentifier : regionIdentifiers) {
			regionsPluginDataBuilder.addRegion(regionIdentifier);
		}
		for (PersonId personId : people) {
			RegionIdentifier regionIdentifier = regionIdentifiers.get(randomGenerator.nextInt(regionIdentifiers.size()));
			regionsPluginDataBuilder.setPersonRegion(personId, regionIdentifier);
		}

		RegionsPluginData regionsPluginData = regionsPluginDataBuilder.build();
		Plugin regionsPlugin = RegionsPlugin.getRegionsPlugin(regionsPluginData);

		// build the people plugin
		PeoplePluginData.Builder peoplePluginDataBuilder = PeoplePluginData.builder();
		for (PersonId personId : people) {
			peoplePluginDataBuilder.addPersonId(personId);
		}
		PeoplePluginData peoplePluginData = peoplePluginDataBuilder.build();
		Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(peoplePluginData);

		// build the person properties plugin
		PersonPropertiesPluginData.Builder personPropertiesPluginDataBuilder = PersonPropertiesPluginData.builder();
		for (PersonPropertyIdentifier personPropertyIdentifier : PersonPropertyIdentifier.values()) {
			personPropertiesPluginDataBuilder.definePersonProperty(personPropertyIdentifier, personPropertyIdentifier.getPropertyDefinition());
		}
		PersonPropertiesPluginData personPropertiesPluginData = personPropertiesPluginDataBuilder.build();
		Plugin personPropertyPlugin = PersonPropertiesPlugin.getPersonPropertyPlugin(personPropertiesPluginData);

		// build the stochastics plugin
		StochasticsPluginData stochasticsPluginData = StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
		Plugin stochasticsPlugin = StochasticsPlugin.getStochasticsPlugin(stochasticsPluginData);

		// add the model plugin
		ModelPluginData modelPluginData = ModelPluginData.builder().setEventCount(eventCount).setUseEventFilters(useEventFilters).build();
		Plugin modelPlugin = ModelPlugin.getModelPlugin(modelPluginData);

		// build the experiment
		Experiment experiment = Experiment	.builder()//
											.addPlugin(personPropertyPlugin)//
											.addPlugin(peoplePlugin)//
											.addPlugin(regionsPlugin)//
											.addPlugin(stochasticsPlugin)//
											.addPlugin(modelPlugin)//
											.addExperimentContextConsumer(c -> {
												c.subscribeToOutput(Object.class, (c2, scenarioId, value) -> {
													System.out.println(value);
												});
											})//
											.build();//

		// run the experiment
		TimeElapser timeElapser = new TimeElapser();
		experiment.execute();
		System.out.println("total experiment time = "+timeElapser.getElapsedMilliSeconds());

	}
}
