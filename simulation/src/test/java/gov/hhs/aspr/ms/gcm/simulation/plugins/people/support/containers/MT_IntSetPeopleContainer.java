package gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.containers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.math3.random.RandomGenerator;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.testsupport.PeopleTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.integersets.MemorySizer;
import gov.hhs.aspr.ms.util.time.TimeElapser;

public final class MT_IntSetPeopleContainer {

	private static enum Alogorithm {

		/*
		 * Very fast and uses 1.4 bits per person in the population -- supports
		 * numerically ordered retrieval, but not addition ordered retrieval.
		 * Appropriate for partitions that have a significant portion of the population
		 * passing the filter and relatively few cells.
		 */
		TREE_BIT_SET_PEOPLE_CONTAINER,

		/*
		 * Fairly fast and uses 85 bits per person, but does not either addition order
		 * nor numerical order retrieval. Appropriate for use cases that do not require
		 * run continuity. Use for small cells -- i.e. when then number of cells gets
		 * large. We may be able to reduce this to 40-50 bits per person
		 */
		INT_SET_PEOPLE_CONTAINER,
		
		INT_SET_PEOPLE_CONTAINER2,

		/*
		 * Fairly slow and high mem use 450 bits per person. Simple, but not good for
		 * most cases.
		 */
		TREE_SET,

		/*
		 * Extremely slow, nsquare add and remove times, 32 or 64 bits per person. Both
		 * of these require continual sorting and could sort only when sampled. Still
		 * that mostly won't help because each addition to the membership will generally
		 * trigger a sample.
		 */
		SORTED_LIST,

		SORTED_ARRAY,

		/*
		 * Slow, uses 718 bits per person, supports addition order retrieval.
		 */
		LINKED_HASH_SET,

		/*
		 * The next solution would use a tree version of a linked list with the bottom
		 * of the tree using a long value used for bit math, etc. Should generally use
		 * about 240 bits per person and supports ordered retrieval. We are trading the
		 * int array for tree management for an object based tree and thus the cost per
		 * node is much higher. This should be an appropriate solution when the number
		 * of cells in the partition is about 170+
		 * 
		 * As density goes up, the collision probability per bottom element in the tree
		 * goes up and the cost of the entire data structure barely grows at all. As the
		 * density approaches 100% of the population, this container approaches 4 bits
		 * per person. So the threshold between this and the TreeBitSetPeopleContainer
		 * may be somewhere around 10% and 30% of the pop.
		 */
		FAST_LINKED_LIST,
		
		SORTED_BUCKETS,

		;
	}

	private void execute() {
		testMem(2345345634L);
	}

	public void testMem( long seed) {
		PeopleTestPluginFactory.Factory factory = PeopleTestPluginFactory.factory(seed, (c) -> {

			// get some data views that will be needed below
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			double membershipProbability = 1;

			List<PersonId> members = new ArrayList<>();
			for (int i = 0; i < 10_000_000; i++) {
				PersonConstructionData personConstructionData = PersonConstructionData.builder().build();
				PersonId personId = peopleDataManager.addPerson(personConstructionData);
				if (randomGenerator.nextDouble() < membershipProbability) {
					members.add(personId);
				}
			}

			Collections.shuffle(members, new Random(randomGenerator.nextLong()));

			TimeElapser timeElapser = new TimeElapser();
			
			Alogorithm alogorithm = Alogorithm.INT_SET_PEOPLE_CONTAINER2;
			double averageInstanceByteCount = MemorySizer.getAverageInstanceByteCount(() -> {
				switch (alogorithm) {

				case INT_SET_PEOPLE_CONTAINER:
					return getIntSetPeopleContainerSolution(members);
				case INT_SET_PEOPLE_CONTAINER2:
					return getIntSetPeopleContainer2Solution(members,peopleDataManager);
				case LINKED_HASH_SET:
					getLinkedHashSetSolution(members);
				case SORTED_ARRAY:
					return getSortedArraySolution(members);
				case SORTED_LIST:
					return getSortedListSolution(members);
				case TREE_BIT_SET_PEOPLE_CONTAINER:
					return getTreeBitSetPeopleContainerSolution(members, peopleDataManager);
				case TREE_SET:
					return getTreeSetSolution(members);
				case FAST_LINKED_LIST:
					// fall through
				case SORTED_BUCKETS:
					//fall through
				default:
					throw new RuntimeException("unhandled case " + alogorithm);
				}

			}, 1);

			System.out.println("bits per person = " + averageInstanceByteCount * 8 / members.size());
			System.out.println("elapsed time = " + timeElapser.getElapsedMilliSeconds());

		});
		TestSimulation.builder()//
				.addPlugins(factory.getPlugins())//
				.build()//
				.execute();
	}

	private static class SortedList {
		private List<PersonId> personIds = new ArrayList<>();

		public void add(PersonId personId) {
			personIds.add(personId);
			Collections.sort(personIds);
		}
	}

	private SortedList getSortedListSolution(List<PersonId> members) {
		SortedList result = new SortedList();
		for (PersonId personId : members) {
			result.add(personId);
		}
		return result;
	}

	private static class SortedArray {
		private int[] personIds;
		private int size;
		private int capacity;

		public void add(PersonId personId) {
			if (size == capacity) {
				if (capacity == 0) {
					capacity = 1;
					personIds = new int[1];
				} else {
					capacity *= 2;
					personIds = Arrays.copyOf(personIds, capacity);
				}
			}
			personIds[size++] = personId.getValue();
			Arrays.sort(personIds, 0, size);
		}
	}

	private SortedArray getSortedArraySolution(List<PersonId> members) {
		SortedArray result = new SortedArray();
		for (PersonId personId : members) {
			result.add(personId);
		}
		return result;
	}

	public static void main(String[] args) {
		new MT_IntSetPeopleContainer().execute();
	}

	private TreeSet<PersonId> getTreeSetSolution(List<PersonId> members) {
		TreeSet<PersonId> treeSet = new TreeSet<>();
		for (PersonId personId : members) {
			treeSet.add(personId);
		}
		return treeSet;
	}

	private PeopleContainer getIntSetPeopleContainerSolution(List<PersonId> members) {
		PeopleContainer peopleContainer = new IntSetPeopleContainer();

		for (PersonId personId : members) {
			peopleContainer.unsafeAdd(personId);
		}
		return peopleContainer;
	}
	
	private PeopleContainer getIntSetPeopleContainer2Solution(List<PersonId> members,PeopleDataManager peopleDataManager) {
		PeopleContainer peopleContainer = new IntSetPeopleContainer2(peopleDataManager);

		for (PersonId personId : members) {
			peopleContainer.unsafeAdd(personId);
		}
		return peopleContainer;
	}

	private PeopleContainer getTreeBitSetPeopleContainerSolution(List<PersonId> members,
			PeopleDataManager peopleDataManager) {
		PeopleContainer peopleContainer = new TreeBitSetPeopleContainer(peopleDataManager);
		for (PersonId personId : members) {
			peopleContainer.unsafeAdd(personId);
		}
		return peopleContainer;
	}

	private Set<PersonId> getLinkedHashSetSolution(List<PersonId> members) {
		return new LinkedHashSet<>(members);
	}

}