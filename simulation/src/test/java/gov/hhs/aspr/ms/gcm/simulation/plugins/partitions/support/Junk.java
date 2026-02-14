package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManager;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestDataManagerPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.FunctionalAttributeLabeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;

public class Junk {
	private static class Holder<T> {
		private T t;

		public T get() {
			return t;
		}

		public void set(T t) {
			this.t = t;
		}

	}

	public void stub() {
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager());
		pluginDataBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(StochasticsPluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(AttributesPluginId.PLUGIN_ID);
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
		}));
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Factory factory = PartitionsTestPluginFactory.factory(100, 3760806761100897313L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "attemptPersonAddition", args = { PersonId.class })
	public void testAttemptPersonAddition() {
		Holder<PopulationPartition> holder = new Holder<>();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager());
		pluginDataBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(StochasticsPluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(AttributesPluginId.PLUGIN_ID);

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(0, (c) -> {

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0 = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(null, c, partition, false);
			holder.set(populationPartition);

			// precondition test:
			assertThrows(RuntimeException.class, () -> populationPartition.attemptPersonAddition(null));

		}));

		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			/*
			 * Add new people, setting the attribute to alternating values of true and false
			 */
			for (int i = 0; i < 20; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				boolean attributeValue = i % 2 == 0;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, attributeValue);
			}

		}));

		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(2, (c) -> {
			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			PopulationPartition populationPartition = holder.get();

			/*
			 * Add new people, setting the attribute to alternating values of true and false
			 */
			for (PersonId personId : peopleDataManager.getPeople()) {
				boolean attributeValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				populationPartition.attemptPersonAddition(personId);

				/*
				 * Show that the person is in the population partition if and only if their
				 * attribute value was set to true
				 */
				assertEquals(attributeValue, populationPartition.contains(personId));
			}

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Factory factory = PartitionsTestPluginFactory.factory(100, 3063819509780972206L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestConstructor(target = PopulationPartitionImpl.class, args = { PartitionsContext.class, Partition.class,
			boolean.class })
	public void testConstructor() {
		Set<PersonId> expectedPeople = new LinkedHashSet<>();

		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager());
		pluginDataBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(StochasticsPluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(AttributesPluginId.PLUGIN_ID);
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {

			// establish data view
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select about half of the people
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// set attribute BOOLEAN_0 to true for those people
			for (PersonId personId : expectedPeople) {
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
			}

		}));
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			// create the population partition
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(null, c, partition, false);

			// show that the population partition contains the expected people
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests
			// if the context is null
			assertThrows(RuntimeException.class, () -> new PopulationPartitionImpl(null, null, partition, false));

			// if the partition is null
			assertThrows(RuntimeException.class, () -> new PopulationPartitionImpl(null, c, null, false));
		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Factory factory = PartitionsTestPluginFactory.factory(100, 2997202170895856110L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "attemptPersonRemoval", args = { PersonId.class })
	public void testAttemptPersonRemoval() {
		Set<PersonId> expectedPeople = new LinkedHashSet<>();
		
		TestPluginData.Builder pluginDataBuilder = TestPluginData.builder();
		pluginDataBuilder.addTestDataManager("dm", () -> new TestDataManager());
		pluginDataBuilder.addPluginDependency(PeoplePluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(StochasticsPluginId.PLUGIN_ID);
		pluginDataBuilder.addPluginDependency(AttributesPluginId.PLUGIN_ID);
		pluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {


			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
	

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
					expectedPeople.add(personId);
				}
			}			

		}));
		pluginDataBuilder.addTestDataManagerPlan("dm", new TestDataManagerPlan(1, (c) -> {
			/*
			 * Create the population partition filtering on attribute BOOLEAN_0 = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(null, c,
					partition, false);

			// show that the expected people are in the population partition
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			/*
			 * Remove people and show that they are no longer in the partition
			 */
			for (PersonId personId : expectedPeople) {				
				populationPartition.attemptPersonRemoval(personId);
				// show that the person was removed
				assertFalse(populationPartition.contains(personId));
			}

		}));
		TestPluginData testPluginData = pluginDataBuilder.build();
		Factory factory = PartitionsTestPluginFactory.factory(100, 4856457716960397685L, testPluginData);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
	
	
	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "handleEvent", args = { Event.class })
	public void testHandleEvent() {
		//monkey
		Factory factory = PartitionsTestPluginFactory.factory(100, 8982209428616460818L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			for (PersonId personId : peopleDataManager.getPeople()) {
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean());
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean());
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */

			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> v)).setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(null,testPartitionsContext, partition,false);

			for (PersonId personId : peopleDataManager.getPeople()) {
				Boolean b0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Boolean b1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);

				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, !b0);
				populationPartition.handleEvent(new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, b0, !b0));

				assertEquals(!b0, populationPartition.contains(personId));

				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, !b1);
				populationPartition.handleEvent(new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_1, b1, !b1));

				if (!b0) {
					LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, !b1).build();
					assertTrue(populationPartition.contains(personId, labelSet));

					labelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, b1).build();
					assertFalse(populationPartition.contains(personId, labelSet));
				}
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

}
