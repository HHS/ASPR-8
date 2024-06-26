package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.testsupport.PeopleTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;

/*
 * Static support class for testing PopulationContainer implementer classes
 */

public class PeopleContainerTester {

	public static void testGetPeople(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}

			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// add the people to the people container
			for (PersonId personId : expectedPeople) {
				peopleContainer.safeAdd(personId);
			}

			// show that the people we added are present in the people container
			List<PersonId> peopleList = peopleContainer.getPeople();
			assertEquals(expectedPeople.size(), peopleList.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(peopleList));

			// remove up to 10 people in a random order
			Random random = new Random(randomGenerator.nextLong());
			Collections.shuffle(expectedPeople, random);
			int n = FastMath.min(10, expectedPeople.size());

			for (int i = 0; i < n; i++) {
				PersonId personId = expectedPeople.remove(0);
				peopleContainer.remove(personId);
			}

			// show that the people container still has the correct people
			peopleList = peopleContainer.getPeople();
			assertEquals(expectedPeople.size(), peopleList.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(peopleList));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	public static void testSafeAdd(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// add the people to the people container
			for (PersonId personId : expectedPeople) {
				peopleContainer.safeAdd(personId);
				// show that the people container does contain the person
				assertTrue(peopleContainer.contains(personId));

				// add the person again to later show that the addition was safe
				// against duplication
				peopleContainer.safeAdd(personId);
			}

			// show that the people container still has the correct people with
			// no duplications
			List<PersonId> peopleList = peopleContainer.getPeople();
			assertEquals(expectedPeople.size(), peopleList.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(peopleList));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	public static void testUnsafeAdd(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			/*
			 * add the people to the people container -- we will not add duplicates since
			 * this is the unsafe add.
			 */
			for (PersonId personId : expectedPeople) {
				peopleContainer.unsafeAdd(personId);

				// show that the people container does contain the person
				assertTrue(peopleContainer.contains(personId));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	public static void testRemove(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// add the people to the people container
			for (PersonId personId : expectedPeople) {
				peopleContainer.safeAdd(personId);
			}

			// show that the people container has the correct people with
			// no duplications
			List<PersonId> peopleList = peopleContainer.getPeople();
			assertEquals(expectedPeople.size(), peopleList.size());
			assertEquals(new LinkedHashSet<>(expectedPeople), new LinkedHashSet<>(peopleList));

			// remove the people from the people container, showing that the
			// person is removed
			for (PersonId personId : expectedPeople) {
				peopleContainer.remove(personId);
				assertFalse(peopleContainer.contains(personId));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	public static void testSize(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// add the people to the people container, showing that the size
			// increments accordingly
			int expectedSize = 0;
			for (PersonId personId : expectedPeople) {
				peopleContainer.safeAdd(personId);
				expectedSize++;
				assertEquals(expectedSize, peopleContainer.size());
			}

			// remove the people from the people container, showing that the
			// size decrements accordingly
			for (PersonId personId : expectedPeople) {
				peopleContainer.remove(personId);
				expectedSize--;
				assertEquals(expectedSize, peopleContainer.size());
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	public static void testContains(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeople = new ArrayList<>();
			List<PersonId> peopleNotIncluded = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				} else {
					peopleNotIncluded.add(personId);
				}

			}

			// add the people to the people container
			for (PersonId personId : expectedPeople) {
				peopleContainer.safeAdd(personId);
				assertTrue(peopleContainer.contains(personId));
			}

			// show that the people that were not added are not contained
			for (PersonId personId : peopleNotIncluded) {
				peopleContainer.remove(personId);
				assertFalse(peopleContainer.contains(personId));
			}

			// remove people and show they are no long contained
			for (PersonId personId : expectedPeople) {
				peopleContainer.remove(personId);
				assertFalse(peopleContainer.contains(personId));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	public static void testGetRandomPersonId(Function<PeopleDataManager, PeopleContainer> provider, long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			
			// get the people container to test
			PeopleContainer peopleContainer = provider.apply(peopleDataManager);

			for (int i = 0; i < 100; i++) {
				peopleDataManager.addPerson(PersonConstructionData.builder().build());
			}
			
			
			// show that the simulation contains the correct number of people
			assertEquals(100, peopleDataManager.getPopulationCount());

			// add about half of the people to the people we expect to find in
			// the people container
			List<PersonId> expectedPeopleList = new ArrayList<>();
			List<PersonId> peopleNotIncluded = new ArrayList<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeopleList.add(personId);
				} else {
					peopleNotIncluded.add(personId);
				}

			}

			assertNull(peopleContainer.getRandomPersonId(randomGenerator));

			// add the people to the people container
			for (PersonId personId : expectedPeopleList) {
				peopleContainer.safeAdd(personId);
			}

			/*
			 * Make some random selections from the people container. Show that each
			 * selection is a person contained in the people container
			 */
			Set<PersonId> expectedPeopleSet = new LinkedHashSet<>(expectedPeopleList);
			for (int i = 0; i < 1000; i++) {
				PersonId randomPersonId = peopleContainer.getRandomPersonId(randomGenerator);
				assertTrue(expectedPeopleSet.contains(randomPersonId));
			}

			// remove up to 20 people in a random order
			Random random = new Random(randomGenerator.nextLong());
			Collections.shuffle(expectedPeopleList, random);
			int n = FastMath.min(20, expectedPeopleList.size());

			for (int i = 0; i < n; i++) {
				PersonId personId = expectedPeopleList.remove(0);
				peopleContainer.remove(personId);
				expectedPeopleSet.remove(personId);
			}

			// show that the people selected after the removal are still
			// consistent with the people in the people container
			for (int i = 0; i < 1000; i++) {
				PersonId randomPersonId = peopleContainer.getRandomPersonId(randomGenerator);
				assertTrue(expectedPeopleSet.contains(randomPersonId));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}