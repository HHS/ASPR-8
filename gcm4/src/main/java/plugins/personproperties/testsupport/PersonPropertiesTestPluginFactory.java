package plugins.personproperties.testsupport;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.ActorContext;
import nucleus.NucleusError;
import nucleus.Plugin;
import nucleus.PluginData;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.testsupport.testplugin.TestSimulation;
import plugins.people.PeoplePlugin;
import plugins.people.PeoplePluginData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.PersonPropertiesPlugin;
import plugins.personproperties.PersonPropertiesPluginData;
import plugins.personproperties.reports.PersonPropertyInteractionReportPluginData;
import plugins.personproperties.reports.PersonPropertyReportPluginData;
import plugins.personproperties.support.PersonPropertyError;
import plugins.regions.RegionsPlugin;
import plugins.regions.RegionsPluginData;
import plugins.regions.support.RegionError;
import plugins.regions.testsupport.TestRegionId;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.StochasticsPluginData;
import plugins.stochastics.support.StochasticsError;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

/**
 * A static test support class for the {@linkplain PersonPropertiesPlugin}.
 * Provides convenience methods for obtaining standarized PluginData for the
 * listed Plugin.
 * 
 * <p>
 * Also contains factory methods to obtain a list of plugins that is the minimal
 * set needed to adequately test this Plugin that can be utilized with
 * </p>
 * 
 * <li>{@link TestSimulation#executeSimulation}
 */
public class PersonPropertiesTestPluginFactory {

	private PersonPropertiesTestPluginFactory() {
	}

	private static class Data {
		private PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData;
		private PersonPropertyReportPluginData personPropertyReportPluginData;
		private PersonPropertiesPluginData personPropertiesPluginData;
		private RegionsPluginData regionsPluginData;
		private PeoplePluginData peoplePluginData;
		private StochasticsPluginData stochasticsPluginData;
		private TestPluginData testPluginData;

		private Data(int initialPopulation, long seed, TestPluginData testPluginData) {

			this.peoplePluginData = getStandardPeoplePluginData(initialPopulation);
			this.personPropertiesPluginData = PersonPropertiesTestPluginFactory.getStandardPersonPropertiesPluginData(this.peoplePluginData.getPersonIds(), seed);
			this.regionsPluginData = PersonPropertiesTestPluginFactory.getStandardRegionsPluginData(this.peoplePluginData.getPersonIds(), seed);
			this.stochasticsPluginData = getStandardStochasticsPluginData(seed);
			this.testPluginData = testPluginData;
		}
	}

	/**
	 * Factory class that facilitates the building of {@linkplain PluginData}
	 * with the various setter methods.
	 */
	public static class Factory {
		private Data data;

		private Factory(Data data) {
			this.data = data;
		}

		/**
		 * Returns a list of plugins containing a PersonProperties, Regions,
		 * People, Stochastics and Test Plugin built from the contributed
		 * PluginDatas.
		 * 
		 * <li>PersonPropertiesPlugin is defaulted to one formed from
		 * {@link PersonPropertiesTestPluginFactory#getStandardPersonPropertiesPluginData}
		 * <li>RegionsPlugin is defaulted to one formed from
		 * {@link PersonPropertiesTestPluginFactory#getStandardRegionsPluginData}
		 * <li>PeoplePlugin is defaulted to one formed from
		 * {@link PersonPropertiesTestPluginFactory#getStandardPeoplePluginData}
		 * <li>StochasticsPlugin is defaulted to one formed from
		 * {@link PersonPropertiesTestPluginFactory#getStandardStochasticsPluginData}
		 * <li>TestPlugin is formed from the TestPluginData passed into
		 * {@link PersonPropertiesTestPluginFactory#factory}
		 */
		public List<Plugin> getPlugins() {
			List<Plugin> pluginsToAdd = new ArrayList<>();

			PersonPropertiesPlugin.Builder personPropertiesPluginBuilder = PersonPropertiesPlugin.builder().setPersonPropertiesPluginData(data.personPropertiesPluginData);
			if (data.personPropertyInteractionReportPluginData != null) {
				personPropertiesPluginBuilder.setPersonPropertyInteractionReportPluginData(data.personPropertyInteractionReportPluginData);
			}
			if (data.personPropertyReportPluginData != null) {
				personPropertiesPluginBuilder.setPersonPropertyReportPluginData(data.personPropertyReportPluginData);
			}
			Plugin personPropertiesPlugin = personPropertiesPluginBuilder.getPersonPropertyPlugin();

			Plugin peoplePlugin = PeoplePlugin.getPeoplePlugin(this.data.peoplePluginData);

			Plugin regionsPlugin = RegionsPlugin.builder().setRegionsPluginData(this.data.regionsPluginData).getRegionsPlugin();

			Plugin stochasticPlugin = StochasticsPlugin.getStochasticsPlugin(this.data.stochasticsPluginData);

			Plugin testPlugin = TestPlugin.getTestPlugin(this.data.testPluginData);

			pluginsToAdd.add(personPropertiesPlugin);
			pluginsToAdd.add(peoplePlugin);
			pluginsToAdd.add(regionsPlugin);
			pluginsToAdd.add(stochasticPlugin);
			pluginsToAdd.add(testPlugin);

			return pluginsToAdd;
		}

		/**
		 * Sets the {@link PersonPropertiesPluginData} in this Factory. This
		 * explicit instance of pluginData will be used to create a
		 * PersonPropertiesPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_PLUGN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setPersonPropertiesPluginData(PersonPropertiesPluginData personPropertiesPluginData) {
			if (personPropertiesPluginData == null) {
				throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_PLUGN_DATA);
			}
			this.data.personPropertiesPluginData = personPropertiesPluginData;
			return this;
		}

		/**
		 * Sets the {@link PersonPropertyInteractionReportPluginData} in this
		 * Factory. This explicit instance of pluginData will be used to create
		 * a PersonPropertiesPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setPersonPropertyInteractionReportPluginData(PersonPropertyInteractionReportPluginData personPropertyInteractionReportPluginData) {
			if (personPropertyInteractionReportPluginData == null) {
				throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_INTERACTION_REPORT_PLUGIN_DATA);
			}
			this.data.personPropertyInteractionReportPluginData = personPropertyInteractionReportPluginData;
			return this;
		}

		/**
		 * Sets the {@link PersonPropertyInteractionReportPluginData} in this
		 * Factory. This explicit instance of pluginData will be used to create
		 * a PersonPropertiesPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonPropertyError#NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setPersonPropertyReportPluginData(PersonPropertyReportPluginData personPropertyReportPluginData) {
			if (personPropertyReportPluginData == null) {
				throw new ContractException(PersonPropertyError.NULL_PERSON_PROPERTY_REPORT_PLUGIN_DATA);
			}
			this.data.personPropertyReportPluginData = personPropertyReportPluginData;
			return this;
		}
		

		/**
		 * Sets the {@link PeoplePluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a PeoplePlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain PersonError#NULL_PEOPLE_PLUGIN_DATA} if the
		 *             passed in pluginData is null
		 */
		public Factory setPeoplePluginData(PeoplePluginData peoplePluginData) {
			if (peoplePluginData == null) {
				throw new ContractException(PersonError.NULL_PEOPLE_PLUGIN_DATA);
			}
			this.data.peoplePluginData = peoplePluginData;
			return this;
		}

		/**
		 * Sets the {@link RegionsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a RegionsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain RegionError#NULL_REGION_PLUGIN_DATA} if the
		 *             passed in pluginData is null
		 */
		public Factory setRegionsPluginData(RegionsPluginData regionsPluginData) {
			if (regionsPluginData == null) {
				throw new ContractException(RegionError.NULL_REGION_PLUGIN_DATA);
			}
			this.data.regionsPluginData = regionsPluginData;
			return this;
		}

		/**
		 * Sets the {@link StochasticsPluginData} in this Factory. This explicit
		 * instance of pluginData will be used to create a StochasticsPlugin
		 * 
		 * @throws ContractExecption
		 *             {@linkplain StochasticsError#NULL_STOCHASTICS_PLUGIN_DATA}
		 *             if the passed in pluginData is null
		 */
		public Factory setStochasticsPluginData(StochasticsPluginData stochasticsPluginData) {
			if (stochasticsPluginData == null) {
				throw new ContractException(StochasticsError.NULL_STOCHASTICS_PLUGIN_DATA);
			}
			this.data.stochasticsPluginData = stochasticsPluginData;
			return this;
		}

	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link PersonPropertiesPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link PersonPropertiesPluginData}
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * 
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPersonPropertiesPluginData}
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPersonPropertiesPluginData}
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 * 
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_PLUGIN_DATA} if testPluginData
	 *             is null
	 */
	public static Factory factory(int initialPopulation, long seed, TestPluginData testPluginData) {
		if (testPluginData == null) {
			throw new ContractException(NucleusError.NULL_PLUGIN_DATA);
		}
		return new Factory(new Data(initialPopulation, seed, testPluginData));
	}

	/**
	 * Creates a Factory that facilitates the creation of a minimal set of
	 * plugins needed to adequately test the {@link PersonPropertiesPlugin} by
	 * generating:
	 * <ul>
	 * <li>{@link PersonPropertiesPluginData}
	 * <li>{@link RegionsPluginData}
	 * <li>{@link PeoplePluginData}
	 * <li>{@link StochasticsPluginData}
	 * </ul>
	 * 
	 * <li>either directly (by default) via
	 * <ul>
	 * <li>{@link #getStandardPersonPropertiesPluginData}
	 * <li>{@link #getStandardPeoplePluginData},
	 * <li>{@link #getStandardRegionsPluginData},
	 * <li>{@link #getStandardStochasticsPluginData}
	 * </ul>
	 * <li>or explicitly set via
	 * <ul>
	 * <li>{@link Factory#setPersonPropertiesPluginData}
	 * <li>{@link Factory#setPeoplePluginData},
	 * <li>{@link Factory#setRegionsPluginData},
	 * <li>{@link Factory#setStochasticsPluginData}
	 * </ul>
	 * 
	 * <li>via the {@link Factory#getPlugins()} method.
	 *
	 * @throws ContractExecption
	 *             {@linkplain NucleusError#NULL_ACTOR_CONTEXT_CONSUMER} if
	 *             consumer is null
	 */
	public static Factory factory(int initialPopulation, long seed, Consumer<ActorContext> consumer) {
		if (consumer == null) {
			throw new ContractException(NucleusError.NULL_ACTOR_CONTEXT_CONSUMER);
		}

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();
		pluginBuilder.addTestActorPlan("actor", new TestActorPlan(0, consumer));
		TestPluginData testPluginData = pluginBuilder.build();
		return factory(initialPopulation, seed, testPluginData);
	}

	/**
	 * Returns a standardized PersonPropertiesPluginData that is minimally
	 * adequate for testing the PersonPropertiesPlugin.
	 * <li>The resulting PersonPropertiesPluginData will include:
	 * <ul>
	 * <li>Every PersonPropertyId included in {@link TestPersonPropertyId}
	 * <ul>
	 * <li>along with the propertyDefinition for each
	 * </ul>
	 * <li>Every person in the list of passed in people.
	 * <ul>
	 * <li>Each person will have a property value if the propertyDefinition does
	 * not have a defaultValue, OR via a RandomGenerator seeded by the passed in
	 * seed via nextBoolean.
	 * <li>The property value is gotten from
	 * {@link TestPersonPropertyId#getRandomPropertyValue}
	 * </ul>
	 * </ul>
	 */
	public static PersonPropertiesPluginData getStandardPersonPropertiesPluginData(List<PersonId> people, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		PersonPropertiesPluginData.Builder personPropertyBuilder = PersonPropertiesPluginData.builder();
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			personPropertyBuilder.definePersonProperty(testPersonPropertyId, testPersonPropertyId.getPropertyDefinition());
		}
		for (PersonId personId : people) {
			personPropertyBuilder.addPerson(personId);
			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
				boolean doesNotHaveDefaultValue = testPersonPropertyId.getPropertyDefinition().getDefaultValue().isEmpty();
				if (doesNotHaveDefaultValue || randomGenerator.nextBoolean()) {
					Object randomPropertyValue = testPersonPropertyId.getRandomPropertyValue(randomGenerator);
					personPropertyBuilder.setPersonPropertyValue(personId, testPersonPropertyId, randomPropertyValue);
				}
			}
		}
		return personPropertyBuilder.build();
	}

	/**
	 * Returns a standardized PeoplePluginData that is minimally adequate for
	 * testing the PersonPropertiesPlugin
	 * <li>The resulting PeoplePluginData will include:
	 * <ul>
	 * <li>a number of people equal to the passed in intialPopulation
	 * </ul>
	 */
	public static PeoplePluginData getStandardPeoplePluginData(int initialPopulation) {
		PeoplePluginData.Builder peopleBuilder = PeoplePluginData.builder();

		for (int i = 0; i < initialPopulation; i++) {
			peopleBuilder.addPersonId(new PersonId(i));
		}
		return peopleBuilder.build();
	}

	/**
	 * Returns a standardized RegionsPluginData that is minimally adequate for
	 * testing the PersonPropertiesPlugin
	 * <li>The resulting RegionsPluginData will include:
	 * <ul>
	 * <li>Every RegionId included in {@link TestRegionId}
	 * <li>Every person in the list of passed in people.
	 * <ul>
	 * <li>Each person will be assigned to a random region based on a
	 * RandomGenerator seeded by the passed in seed and selected via the
	 * {@link TestRegionId#getRandomRegionId}
	 * </ul>
	 * </ul>
	 */
	public static RegionsPluginData getStandardRegionsPluginData(List<PersonId> people, long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		RegionsPluginData.Builder regionBuilder = RegionsPluginData.builder();
		// add the regions
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.addRegion(testRegionId);
		}
		for (PersonId personId : people) {
			TestRegionId randomRegionId = TestRegionId.getRandomRegionId(randomGenerator);
			regionBuilder.setPersonRegion(personId, randomRegionId);
		}
		return regionBuilder.build();

	}

	/**
	 * Returns a standardized StochasticsPluginData that is minimally adequate
	 * for testing the PersonPropertiesPlugin
	 * <li>The resulting StochasticsPluginData will include:
	 * <ul>
	 * <li>a seed based on the nextLong of a RandomGenerator seeded from the
	 * passed in seed
	 * </ul>
	 */
	public static StochasticsPluginData getStandardStochasticsPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		return StochasticsPluginData.builder().setSeed(randomGenerator.nextLong()).build();
	}

}
