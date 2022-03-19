package plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import nucleus.ActorContext;
import nucleus.DataManagerContext;
import nucleus.Plugin;
import nucleus.testsupport.testplugin.TestActorPlan;
import nucleus.testsupport.testplugin.TestPlugin;
import nucleus.testsupport.testplugin.TestPluginData;
import nucleus.util.ContractException;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.LabelSetWeightingFunction;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionSampler;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.AttributesDataManager;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.PersonDataManager;
import plugins.people.events.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonConstructionData;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataManager;
import util.MutableInteger;
import util.RandomGeneratorProvider;

@UnitTest(target = PartitionDataManager.class)
public final class AT_PartitionDataManager {

	/*
	 * Assigns randomized values for all attributes to all people. Values are
	 * assigned to be consistent with the static labeling functions.
	 */
	private static void assignRandomAttributes(final ActorContext c) {
		final PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
		AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
		final StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
		final RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		for (final PersonId personId : personDataManager.getPeople()) {

			boolean b = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, b);

			b = randomGenerator.nextBoolean();
			attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, b);

			int i = randomGenerator.nextInt(100);
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, i);

			i = randomGenerator.nextInt(100);
			attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, i);

			double d = randomGenerator.nextDouble() * 100;
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, d);

			d = randomGenerator.nextDouble() * 100;
			attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, d);

		}
	}

	private static Function<Object, Object> INT_0_LABELFUNCTION = (value) -> {
		final int v = (Integer) value;
		return v % 3;
	};

	private static Function<Object, Object> INT_1_LABELFUNCTION = (value) -> {
		final int v = (Integer) value;
		if (v < 40) {
			return true;
		}
		return false;
	};

	private static Function<Object, Object> DOUBLE_0_LABELFUNCTION = (value) -> {
		final double v = (Double) value;
		if (v < 33) {
			return "A";
		}
		if (v < 67) {
			return "B";
		}
		return "C";
	};

	private static Function<Object, Object> DOUBLE_1_LABELFUNCTION = (value) -> {
		final double v = (Double) value;
		return v < 90;
	};

	/*
	 * Creates a map from LabelSet to PersonId that covers all people who have
	 * an attribute value of true for BOOLEAN_0 and false for BOOLEAN_1, to be
	 * consistent with the filter used in the partition addition test. Label
	 * sets consist of labels for INT_0, INT_1, DOUBLE_0 and DOUBLE_1.
	 */
	private static Map<LabelSet, Set<PersonId>> getExpectedStructure(final ActorContext c) {
		final PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
		final AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
		final Map<LabelSet, Set<PersonId>> expectedPeople = new LinkedHashMap<>();
		for (final PersonId personId : personDataManager.getPeople()) {

			final Boolean b0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
			final Boolean b1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
			if (b0 && !b1) {

				final Integer i0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
				final Object label_i0 = INT_0_LABELFUNCTION.apply(i0);

				final Integer i1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
				final Object label_i1 = INT_1_LABELFUNCTION.apply(i1);

				final Double d0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
				final Object label_d0 = DOUBLE_0_LABELFUNCTION.apply(d0);

				final Double d1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
				final Object label_d1 = DOUBLE_1_LABELFUNCTION.apply(d1);

				final LabelSet labelSet = LabelSet	.builder()//
													.setLabel(TestAttributeId.INT_0, label_i0)//
													.setLabel(TestAttributeId.INT_1, label_i1)//
													.setLabel(TestAttributeId.DOUBLE_0, label_d0)//
													.setLabel(TestAttributeId.DOUBLE_1, label_d1)//
													.build();//

				Set<PersonId> people = expectedPeople.get(labelSet);
				if (people == null) {
					people = new LinkedHashSet<>();
					expectedPeople.put(labelSet, people);
				}
				people.add(personId);
			}
		}
		return expectedPeople;

	}

	/*
	 * Compares the expected alignment of people to label sets to the population
	 * partition's content via assertions.
	 */
	private static void showPartitionIsCorrect(final ActorContext c, final Map<LabelSet, Set<PersonId>> expectedPartitionStructure, final Object key) {

		final PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

		// derive the number of people in the expected partition structure
		int expectedPersonCount = 0;
		for (final LabelSet labelSet : expectedPartitionStructure.keySet()) {
			final Set<PersonId> expectedPeople = expectedPartitionStructure.get(labelSet);
			expectedPersonCount += expectedPeople.size();
		}

		// Show that the number of people in the partition matches the expected
		// count of people.
		final int actualPersonCount = partitionDataManager.getPersonCount(key);
		assertEquals(expectedPersonCount, actualPersonCount);

		/*
		 * Show that each label set in the expected structure is associated with
		 * the same people in the population partition.
		 * 
		 * Since we know that the expected partition structure and the
		 * population partition have the same number of people and that no
		 * person can be associated with two label sets, we know there are no
		 * uncounted people in the population partition and thus the two data
		 * structures match.
		 */
		for (final LabelSet labelSet : expectedPartitionStructure.keySet()) {
			final Set<PersonId> expectedPeople = expectedPartitionStructure.get(labelSet);
			final List<PersonId> actualpeople = partitionDataManager.getPeople(key, labelSet);
			assertEquals(expectedPeople.size(), actualpeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualpeople));
		}
	}

	/*
	 * Removes the given number of people from the simulation, chosen at random.
	 * Person count may exceed the current population size.
	 */
	private static void removePeople(final ActorContext c, final int numberOfPeopleToRemove) {
		final PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
		final StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
		final long seed = stochasticsDataManager.getRandomGenerator().nextLong();
		final Random random = new Random(seed);
		final List<PersonId> people = personDataManager.getPeople();
		Collections.shuffle(people, random);
		final int correctedNumberOfPeopleToRemove = FastMath.min(numberOfPeopleToRemove, people.size());
		for (int i = 0; i < correctedNumberOfPeopleToRemove; i++) {
			personDataManager.removePerson(people.get(i));
		}
	}

	/*
	 * Adds the given number of people to the simulation
	 */
	private static void addPeople(final ActorContext c, final int numberOfPeopleToAdd) {
		PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
		for (int i = 0; i < numberOfPeopleToAdd; i++) {
			personDataManager.addPerson(PersonConstructionData.builder().build());
		}
	}

	@Test
	@UnitTestMethod(name = "addPartition", args = { Partition.class, Object.class })
	public void testAddPartition() {

		// Have the simulation initialized with 1000 people. Have an agent
		// execute a partition addition and multiple changes to the population
		// and their attributes to show that the partition resolver maintains
		// the partition.

		PartitionsActionSupport.testConsumer(1000, 5127268948453841557L, (c) -> {
			// get the partition data view
			final PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			// initialize people's attributes
			assignRandomAttributes(c);

			// create a key to use for a new partition
			final Object key = new Object();

			// show that the population partition does not yet exist
			assertFalse(partitionDataManager.partitionExists(key));

			/*
			 * Add the partition. We will filter to select people who have
			 * BOOLEAN_0 as true and BOOLEAN_1 as false. The remaining
			 * attributes will be used to define the cells of the partition via
			 * the four static labeling functions defined in this class.
			 */
			final Filter filter0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			final Filter filter1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			final Filter filter = filter0.and(filter1);
			final Partition partition = Partition	.builder()//
													.setFilter(filter)//
													.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, INT_0_LABELFUNCTION))//
													.addLabeler(new AttributeLabeler(TestAttributeId.INT_1, INT_1_LABELFUNCTION))//
													.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_0, DOUBLE_0_LABELFUNCTION))//
													.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_1, DOUBLE_1_LABELFUNCTION))//
													.build();//

			partitionDataManager.addPartition(partition, key);

			// show that the partition was added
			assertTrue(partitionDataManager.partitionExists(key));

			/*
			 * Get the expected structure by examining each person and grouping
			 * them by the label sets that we expect to find in the partition
			 */
			Map<LabelSet, Set<PersonId>> expectedPartitionStructure = getExpectedStructure(c);

			/*
			 * Show that the expected structure matches the actual structure of
			 * the partition
			 */
			showPartitionIsCorrect(c, expectedPartitionStructure, key);

			/*
			 * Perform various changes to the people and their attributes.
			 */
			removePeople(c, 100);
			addPeople(c, 120);
			assignRandomAttributes(c);

			/*
			 * Get the expected structure again now that there have been changes
			 * to people's attributes
			 */
			expectedPartitionStructure = getExpectedStructure(c);

			/*
			 * Show that the expected structure matches the actual structure of
			 * the partition and thus the partition resolver must be maintaining
			 * the partition as stated in the contract.
			 */
			showPartitionIsCorrect(c, expectedPartitionStructure, key);

		});

		// if the key is already allocated to another population partition
		PartitionsActionSupport.testConsumer(0, 1137046131619466337L, (c) -> {

			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			Object key = new Object();

			partitionDataManager.addPartition(Partition.builder().build(), key);
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(Partition.builder().build(), key));
			assertEquals(PartitionError.DUPLICATE_PARTITION, contractException.getErrorType());

		});

		// precondition: if the partition is null
		PartitionsActionSupport.testConsumer(0, 1137046131619466337L, (c) -> {

			PartitionDataManager partitionDataManager = new PartitionDataManager();
			Object key = new Object();

			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(null, key));
			assertEquals(PartitionError.NULL_PARTITION, contractException.getErrorType());

		});

		// precondition: if the key is null
		PartitionsActionSupport.testConsumer(0, 1137046131619466337L, (c) -> {
			PartitionDataManager partitionDataManager = new PartitionDataManager();
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.addPartition(Partition.builder().build(), null));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "removePartition", args = { Object.class })
	public void testRemovePartition() {

		PartitionsActionSupport.testConsumer(0, 5767679585616452606L, (c) -> {

			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			Object key = new Object();

			// show that the partition does not yet exist
			assertFalse(partitionDataManager.partitionExists(key));

			Partition partition = Partition.builder().build();
			partitionDataManager.addPartition(partition, key);

			// show that the partition was added
			assertTrue(partitionDataManager.partitionExists(key));

			// show that partition is removed
			partitionDataManager.removePartition(key);
			assertFalse(partitionDataManager.partitionExists(key));

		});
	}

	@Test
	@UnitTestMethod(name = "partitionExists", args = { Object.class })
	public void testPartitionExists() {

		PartitionsActionSupport.testConsumer(0, 1968926333881399732L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			// create containers to hold known and unknown keys
			Set<Object> knownKeys = new LinkedHashSet<>();
			for (int i = 0; i < 10; i++) {
				Object key = "key" + i;
				knownKeys.add(key);
			}

			Set<Object> unknownKeys = new LinkedHashSet<>();
			for (int i = 10; i < 20; i++) {
				Object key = "key" + i;
				unknownKeys.add(key);
			}

			// add a partition for each key
			for (Object key : knownKeys) {
				Partition partition = Partition.builder().build();
				partitionDataManager.addPartition(partition, key);
			}

			// show that the known keys will have a partition
			for (Object key : knownKeys) {
				assertTrue(partitionDataManager.partitionExists(key));
			}

			// show that the unknown keys will not have a partition
			for (Object key : unknownKeys) {
				assertFalse(partitionDataManager.partitionExists(key));
			}

			// show that the null key has no partition
			assertFalse(partitionDataManager.partitionExists(null));

		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class, Object.class })
	public void testContains() {

		PartitionsActionSupport.testConsumer(100, 607630153604184177L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			// create a partition where half the population is in the partition
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, (v) -> 3)).build();//

			partitionDataManager.addPartition(partition, key);

			// change the BOOLEAN_0 randomly for every person
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				boolean newValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, newValue);
			}

			// show that there is at least one person in the partition and one
			// person outside the partition
			int personCountInPartition = partitionDataManager.getPersonCount(key);
			assertTrue(personCountInPartition < personDataManager.getPopulationCount());
			assertTrue(personCountInPartition > 0);

			// show that a person is in the partition if and only if their
			// BOOLEAN_0 attribute is true
			for (PersonId personId : personDataManager.getPeople()) {
				boolean expectPersonInPartition = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				boolean actualPersonInPartition = partitionDataManager.contains(personId, key);
				assertEquals(expectPersonInPartition, actualPersonInPartition);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class, LabelSet.class, Object.class })
	public void testContains_LabelSet() {

		PartitionsActionSupport.testConsumer(100, 7338572401998066291L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			/*
			 * Define functions that will convert attribute values into labels
			 * for attributes INT_0, INT_1, DOUBLE_0, and DOUBLE_1. We will use
			 * these in the partition's labeling
			 */
			Function<Object, Object> int_0_labelFunction = (value) -> {
				int v = (Integer) value;
				return v % 3;
			};

			Function<Object, Object> int_1_labelFunction = (value) -> {
				int v = (Integer) value;
				if (v < 40) {
					return true;
				}
				return false;
			};

			Function<Object, Object> double_0_labelFunction = (value) -> {
				double v = (Double) value;
				if (v < 33) {
					return "A";
				}
				if (v < 67) {
					return "B";
				}
				return "C";
			};

			Function<Object, Object> double_1_labelFunction = (value) -> {
				double v = (Double) value;
				return v < 90;
			};

			// create a partition where half the population is in the partition
			// with labeling
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction)).addLabeler(new AttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction)).build();//

			partitionDataManager.addPartition(partition, key);

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, intValue);

				double doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, doubleValue);

				doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, doubleValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

				booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, booleanValue);

			}

			// Create a label set to use in the contains query
			LabelSet queryLabelSet = LabelSet	.builder()//
												.setLabel(TestAttributeId.INT_0, 0)//
												.setLabel(TestAttributeId.DOUBLE_0, "A")//
												.build();//

			/*
			 * Show that a person is in the partition under the query label if
			 * and only if their BOOLEAN_0 attribute is true and their INT_0,
			 * and DOUBLE_0 labels are 0 and A
			 */
			for (PersonId personId : personDataManager.getPeople()) {
				boolean contained = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				Integer int0Value = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
				Integer int0Label = (Integer) int_0_labelFunction.apply(int0Value);

				Double double0Value = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
				String double0Label = (String) double_0_labelFunction.apply(double0Value);

				boolean expectPersonInPartitionUnderLabel = contained && int0Label.equals(0) && double0Label.equals("A");

				boolean actualPersonInPartitionUnderLabel = partitionDataManager.contains(personId, queryLabelSet, key);
				assertEquals(expectPersonInPartitionUnderLabel, actualPersonInPartitionUnderLabel);
			}

			// precondition tests
			PersonId personId = new PersonId(0);
			assertTrue(personDataManager.personExists(personId));

			PersonId unknownPersonId = new PersonId(10000000);
			assertFalse(personDataManager.personExists(unknownPersonId));

			Object unknownKey = new Object();

			LabelSet badLabelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, 0).build();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(null, queryLabelSet, key));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(unknownPersonId, queryLabelSet, key));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the key is null
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(personId, queryLabelSet, null));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

			// if the key is unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(personId, queryLabelSet, unknownKey));
			assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

			// if the label set is null
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(personId, null, key));
			assertEquals(PartitionError.NULL_LABEL_SET, contractException.getErrorType());

			// if the label contains a dimension not present in the partition
			contractException = assertThrows(ContractException.class, () -> partitionDataManager.contains(personId, badLabelSet, key));
			assertEquals(PartitionError.INCOMPATIBLE_LABEL_SET, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getPeople", args = { Object.class })
	public void testGetPeople() {

		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 6033209037401060593L, (c) -> {

			// establish data views
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();

			// create a container to hold the people we expect to find in the
			// partition
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

				if (booleanValue) {
					expectedPeople.add(personId);
				}
			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, (value) -> {
												int v = (Integer) value;
												return v / 10;
											}))//
											.build();
			partitionDataManager.addPartition(partition, key);

			// get the people in the partition
			List<PersonId> peopleInPartition = partitionDataManager.getPeople(key);
			Set<PersonId> actualPeople = new LinkedHashSet<>(peopleInPartition);
			// show that the list of people contained no duplicates
			assertEquals(peopleInPartition.size(), actualPeople.size());

			// show that there were the expected people
			assertEquals(expectedPeople, actualPeople);

		});
	}

	@Test
	@UnitTestMethod(name = "getPeople", args = { Object.class, LabelSet.class })
	public void testGetPeople_LabelSet() {
		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 7761046492495930843L, (c) -> {

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			// define a function that will convert an integer into another
			// integer that will be used as a labeling function for the
			// partition
			Function<Object, Object> attributeValueLabelingFunction = (value) -> {
				int v = (Integer) value;
				return v / 10;
			};

			// alter people's INT_0 and BOOLEAN_0 attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, attributeValueLabelingFunction))//
											.build();

			partitionDataManager.addPartition(partition, key);

			// create a container to hold the people we expect to find in the
			// partition associated with labels
			Map<Object, Set<PersonId>> expectedLabelToPeopleMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataManager.getPeople()) {
				// will the person pass the filter?

				Boolean booleanValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {
					// determine the label that should be associated with the
					// person
					Integer intValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = attributeValueLabelingFunction.apply(intValue);

					// place the person in the expectedLabelToPeopleMap
					Set<PersonId> expectedPeople = expectedLabelToPeopleMap.get(labelValue);
					if (expectedPeople == null) {
						expectedPeople = new LinkedHashSet<>();
						expectedLabelToPeopleMap.put(labelValue, expectedPeople);
					}
					expectedPeople.add(personId);
				}
			}

			// for each label in the expectedLabelToPeopleMap, get the people in
			// the partition who match that label value
			for (Object labelValue : expectedLabelToPeopleMap.keySet()) {

				// get the people we expect to find
				Set<PersonId> expectedPeople = expectedLabelToPeopleMap.get(labelValue);

				// get the people that the partition associates with the label
				LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, labelValue).build();
				List<PersonId> peopleInPartition = partitionDataManager.getPeople(key, labelSet);
				Set<PersonId> actualPeople = new LinkedHashSet<>(peopleInPartition);

				/*
				 * Show that the list of people returned from the population
				 * partition contains no duplicates
				 */
				assertEquals(peopleInPartition.size(), actualPeople.size());

				// show that expected and actual people are equal
				assertEquals(expectedPeople, actualPeople);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleCountMap", args = { Object.class, LabelSet.class })
	public void testGetPeopleCountMap() {
		// initialized with 1000 people
		PartitionsActionSupport.testConsumer(1000, 3993911184725585603L, (c) -> {

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			/*
			 * Define functions that will convert attribute values into labels
			 * for attributes INT_0, INT_1, DOUBLE_0, and DOUBLE_1. We will use
			 * these in the partition's labeling
			 */
			Function<Object, Object> int_0_labelFunction = (value) -> {
				int v = (Integer) value;
				return v % 3;
			};

			Function<Object, Object> int_1_labelFunction = (value) -> {
				int v = (Integer) value;
				if (v < 40) {
					return true;
				}
				return false;
			};

			Function<Object, Object> double_0_labelFunction = (value) -> {
				double v = (Double) value;
				if (v < 33) {
					return "A";
				}
				if (v < 67) {
					return "B";
				}
				return "C";
			};

			Function<Object, Object> double_1_labelFunction = (value) -> {
				double v = (Double) value;
				return v < 90;
			};

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, intValue);

				double doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, doubleValue);

				doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, doubleValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

				booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, booleanValue);

			}

			/*
			 * Create a partition that will contain about half of the population
			 * by filtering on BOOLEAN_0. We will partition on INT_0, INT_1,
			 * DOUBLE_0 and DOUBLE_1. Note that we do not use BOOLEAN_1 as part
			 * of the partition.
			 */
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))//
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))//
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction))//
											.build();

			partitionDataManager.addPartition(partition, key);

			/*
			 * Create a container to hold the number of people we expect to find
			 * in the partition for every label set that is associated with at
			 * least one person.
			 */
			Map<LabelSet, MutableInteger> expectedPartitionContentMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataManager.getPeople()) {
				// will the person pass the filter?
				Boolean booleanValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {

					// construct the label set for this person we expect to
					// exist in the partition
					LabelSet.Builder labelSetBuilder = LabelSet.builder();

					Integer intValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = int_0_labelFunction.apply(intValue);
					labelSetBuilder.setLabel(TestAttributeId.INT_0, labelValue);

					intValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
					labelValue = int_1_labelFunction.apply(intValue);
					labelSetBuilder.setLabel(TestAttributeId.INT_1, labelValue);

					Double doubleValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
					labelValue = double_0_labelFunction.apply(doubleValue);
					labelSetBuilder.setLabel(TestAttributeId.DOUBLE_0, labelValue);

					doubleValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
					labelValue = double_1_labelFunction.apply(doubleValue);
					labelSetBuilder.setLabel(TestAttributeId.DOUBLE_1, labelValue);

					LabelSet labelSet = labelSetBuilder.build();

					// place the person in the expectedLabelToPeopleMap
					MutableInteger mutableInteger = expectedPartitionContentMap.get(labelSet);
					if (mutableInteger == null) {
						mutableInteger = new MutableInteger();
						expectedPartitionContentMap.put(labelSet, mutableInteger);
					}
					mutableInteger.increment();
				}
			}

			/*
			 * We will form our query using two of the four partition dimensions
			 * so that the maps returned by the queries will contain multiple
			 * members.
			 * 
			 * We want to test create queries using INT_0 and DOUBLE_0 across
			 * all their known label values, but also want include some label
			 * values we known will not be present in the partition.
			 */

			Set<Integer> int_0_label_values = new LinkedHashSet<>();
			int_0_label_values.add(0);
			int_0_label_values.add(1);
			int_0_label_values.add(2);
			int_0_label_values.add(3);// will not match any person

			Set<String> double_0_label_values = new LinkedHashSet<>();
			double_0_label_values.add("A");
			double_0_label_values.add("B");
			double_0_label_values.add("C");
			double_0_label_values.add("D");// will not match any person

			for (Integer int_0_label_value : int_0_label_values) {
				for (String double_0_label_value : double_0_label_values) {

					/*
					 * Create a label set for the query that does not contain
					 * all the attribute labels and has legitimate values for
					 * each dimension.
					 */
					LabelSet.Builder labelSetBuilder = LabelSet.builder();
					labelSetBuilder.setLabel(TestAttributeId.INT_0, int_0_label_value);
					labelSetBuilder.setLabel(TestAttributeId.DOUBLE_0, double_0_label_value);
					LabelSet queryLabelSet = labelSetBuilder.build();

					/*
					 * We are only interested in those parts of the
					 * expectedPartitionContentMap that match the query's label
					 * set.
					 */
					Map<LabelSet, Integer> expectedCountMap = new LinkedHashMap<>();
					for (LabelSet labelSet : expectedPartitionContentMap.keySet()) {
						boolean isMatchingLabelSet = true;
						for (Object dimension : queryLabelSet.getDimensions()) {
							Optional<Object> queryLabel = queryLabelSet.getLabel(dimension);
							Optional<Object> label = labelSet.getLabel(dimension);
							if (!queryLabel.equals(label)) {
								isMatchingLabelSet = false;
								break;
							}
						}
						if (isMatchingLabelSet) {
							MutableInteger mutableInteger = expectedPartitionContentMap.get(labelSet);
							expectedCountMap.put(labelSet, mutableInteger.getValue());
						}
					}

					/*
					 * Show that the count map we receive from the partition
					 * matches our expectation
					 */
					Map<LabelSet, Integer> actualCountMap = partitionDataManager.getPeopleCountMap(key, queryLabelSet);
					assertEquals(expectedCountMap, actualCountMap);
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCount", args = { Object.class })
	public void getPersonCount() {

		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 1559429415782871174L, (c) -> {

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();

			// create a couter to hold the number people we expect to find in
			// the
			// partition
			int expectedPeopleCount = 0;

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);
				if (booleanValue) {
					expectedPeopleCount++;
				}
			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, (value) -> {
												int v = (Integer) value;
												return v / 10;
											}))//
											.build();
			partitionDataManager.addPartition(partition, key);

			// get the people in the partition
			int actualCount = partitionDataManager.getPersonCount(key);

			// show that there were the expected people
			assertEquals(expectedPeopleCount, actualCount);

		});

	}

	@Test
	@UnitTestMethod(name = "getPersonCount", args = { Object.class, LabelSet.class })
	public void testGetPersonCount_LabelSet() {
		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 3217787540697556531L, (c) -> {

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			// define a function that will convert an integer into another
			// integer that will be used as a labeling function for the
			// partition
			Function<Object, Object> attributeValueLabelingFunction = (value) -> {
				int v = (Integer) value;
				return v % 10;
			};

			// alter people's INT_0 and BOOLEAN_0 attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : personDataManager.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, attributeValueLabelingFunction))//
											.build();

			partitionDataManager.addPartition(partition, key);

			// create a container to hold the people we expect to find in the
			// partition associated with labels
			Map<Object, MutableInteger> expectedPeopleCountMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataManager.getPeople()) {
				// will the person pass the filter?
				Boolean booleanValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {
					// determine the label that should be associated with the
					// person
					Integer intValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = attributeValueLabelingFunction.apply(intValue);

					// place the person in the expectedLabelToPeopleMap
					MutableInteger mutableInteger = expectedPeopleCountMap.get(labelValue);
					if (mutableInteger == null) {
						mutableInteger = new MutableInteger();
						expectedPeopleCountMap.put(labelValue, mutableInteger);
					}
					mutableInteger.increment();
				}
			}

			// for each label in the expectedLabelToPeopleMap, get the people in
			// the partition who match that label value
			for (Object labelValue : expectedPeopleCountMap.keySet()) {

				// get the people we expect to find
				MutableInteger expectedPeopleCount = expectedPeopleCountMap.get(labelValue);

				// get the people that the partition associates with the label
				LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, labelValue).build();
				int actualPersonCount = partitionDataManager.getPersonCount(key, labelSet);

				// show that expected and actual people counts are equal
				assertEquals(expectedPeopleCount.getValue(), actualPersonCount);
			}

		});

	}

	private static enum ExcludedPersonType {
		NULL, MATCHING_MEMBER, NON_MATCHING_MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestMethod(name = "samplePartition", args = { Object.class, PartitionSampler.class })
	public void testSamplePartition_General() {

		/*
		 * Tests the sample mechanism under a variety of partition samplers.
		 * 
		 * The partition is formed from 4 labeling functions over the attributes
		 * 
		 * INT_0-> 0, 1, 2
		 * 
		 * INT_1 -> TRUE, FALSE
		 * 
		 * DOUBLE_0 -> A, B, C
		 * 
		 * DOUBLE_1 -> TRUE, FALSE
		 * 
		 * Filtering for the partition is either on or off. The filter passes
		 * when the attribute BOOLEAN_0 is true.
		 * 
		 * The partition sampler will optionally set its excluded person to
		 * null, a person not in the partition, a person in the partition who is
		 * not expected to match the sampler's label set and a person who does
		 * match the sampler's label set.
		 * 
		 * The partition sampler will optionally use a label set. The label set
		 * will be composed of combinations of labels over INT_0 and DOUBLE_0,
		 * using label values that are associated with people and some that are
		 * not.
		 * 
		 * The partition sampler will optionally use a weighting function. The
		 * weighting function will return 1 for any person having a label of
		 * TRUE for INT_1 and 0 otherwise.
		 * 
		 * This test does not demonstrate precondition checks, proper use of
		 * random number generator ids, or the proper distribution of results
		 * aligned to the weighting function other that the simple binary
		 * alignment for the weighting function described above.
		 * 
		 * Each combination is run with a randomly generated seed value.
		 * 
		 */

		Set<Integer> int_0_label_values = new LinkedHashSet<>();
		int_0_label_values.add(0);
		int_0_label_values.add(1);
		int_0_label_values.add(2);
		int_0_label_values.add(3);// will not match any person

		Set<String> double_0_label_values = new LinkedHashSet<>();
		double_0_label_values.add("A");
		double_0_label_values.add("B");
		double_0_label_values.add("C");
		double_0_label_values.add("D");// will not match any person

		Set<Boolean> weightingFunctionValues = new LinkedHashSet<>();
		weightingFunctionValues.add(false);
		weightingFunctionValues.add(true);

		Set<Boolean> useFilterValues = new LinkedHashSet<>();
		useFilterValues.add(false);
		useFilterValues.add(true);

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7729976665156925181L);

		for (ExcludedPersonType excludedPersonType : ExcludedPersonType.values()) {
			for (Boolean useWeightingFunction : weightingFunctionValues) {
				for (Boolean useFilter : useFilterValues) {
					long seed = randomGenerator.nextLong();
					executeSamplingTest(seed, useFilter, excludedPersonType, useWeightingFunction, null, null);
					for (Integer int_0_label_value : int_0_label_values) {
						for (String double_0_label_value : double_0_label_values) {
							seed = randomGenerator.nextLong();
							executeSamplingTest(seed, useFilter, excludedPersonType, useWeightingFunction, int_0_label_value, double_0_label_value);
						}
					}
				}
			}
		}

	}

	private void executeSamplingTest(long seed, Boolean useFilter, ExcludedPersonType excludedPersonType, Boolean useWeightingFunction, Integer int_0_label_value, String double_0_label_value) {

		PartitionsActionSupport.testConsumer(1000, seed, (c) -> {

			// remember to test with general and COMET to show they get
			// different results?

			// establish data views
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();

			/*
			 * Define functions that will convert attribute values into labels
			 * for attributes INT_0, INT_1, DOUBLE_0, and DOUBLE_1. We will use
			 * these in the partition's labeling
			 */
			Function<Object, Object> int_0_labelFunction = (value) -> {
				int v = (Integer) value;
				return v % 3;
			};

			Function<Object, Object> int_1_labelFunction = (value) -> {
				int v = (Integer) value;
				if (v < 40) {
					return true;
				}
				return false;
			};

			Function<Object, Object> double_0_labelFunction = (value) -> {
				double v = (Double) value;
				if (v < 33) {
					return "A";
				}
				if (v < 67) {
					return "B";
				}
				return "C";
			};

			Function<Object, Object> double_1_labelFunction = (value) -> {
				double v = (Double) value;
				return v < 90;
			};

			// determine the people in the world
			List<PersonId> peopleInTheWorld = personDataManager.getPeople();

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			for (PersonId personId : peopleInTheWorld) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_0, intValue);

				intValue = (int) (randomGenerator.nextDouble() * 100);
				attributesDataManager.setAttributeValue(personId, TestAttributeId.INT_1, intValue);

				double doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_0, doubleValue);

				doubleValue = randomGenerator.nextDouble() * 100;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.DOUBLE_1, doubleValue);

				boolean booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, booleanValue);

				booleanValue = randomGenerator.nextBoolean();
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_1, booleanValue);

			}

			/*
			 * Create a partition that may filter about half of the population
			 * on BOOLEAN_0. We will partition on INT_0, INT_1, DOUBLE_0 and
			 * DOUBLE_1. Note that we do not use BOOLEAN_1 as part of the
			 * partition.
			 */
			Object key = new Object();
			Partition.Builder partitionBuilder = Partition.builder();
			if (useFilter) {
				partitionBuilder//
								.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true));//
			}
			partitionBuilder//
							.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction))//
							.addLabeler(new AttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))//
							.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))//
							.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction));

			Partition partition = partitionBuilder.build();

			partitionDataManager.addPartition(partition, key);

			/*
			 * Create a label set for the query that does not contain all the
			 * attribute labels and has legitimate values for each dimension.
			 */
			LabelSet queryLabelSet = null;
			if (int_0_label_value != null && double_0_label_value != null) {
				LabelSet.Builder labelSetBuilder = LabelSet.builder();
				labelSetBuilder.setLabel(TestAttributeId.INT_0, int_0_label_value);
				labelSetBuilder.setLabel(TestAttributeId.DOUBLE_0, double_0_label_value);
				queryLabelSet = labelSetBuilder.build();
			}

			// determine the people in the partition
			Set<PersonId> expectedPeopleInPartition = new LinkedHashSet<>();

			for (PersonId personId : peopleInTheWorld) {

				if (useFilter) {
					Boolean personInPartition = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
					if (personInPartition) {
						expectedPeopleInPartition.add(personId);
					}
				} else {
					expectedPeopleInPartition.add(personId);
				}

			}

			// determine the people who will match the query label set
			Set<PersonId> expectedPeopleMatchingQueryLabelSet = new LinkedHashSet<>();
			if (queryLabelSet != null) {
				for (PersonId personId : expectedPeopleInPartition) {
					// will the person pass the filter?

					Integer intValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = int_0_labelFunction.apply(intValue);
					if (labelValue.equals(int_0_label_value)) {
						Double doubleValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
						labelValue = double_0_labelFunction.apply(doubleValue);
						if (labelValue.equals(double_0_label_value)) {
							expectedPeopleMatchingQueryLabelSet.add(personId);
						}

					}

				}
			} else {
				expectedPeopleMatchingQueryLabelSet.addAll(expectedPeopleInPartition);
			}

			PartitionSampler.Builder partitionSamplerBuilder = PartitionSampler.builder();
			partitionSamplerBuilder.setLabelSet(queryLabelSet);//

			// Select the excluded person
			PersonId excludedPersonId = null;
			switch (excludedPersonType) {
			case MATCHING_MEMBER:
				for (PersonId personId : expectedPeopleMatchingQueryLabelSet) {
					excludedPersonId = personId;
					break;
				}

				break;
			case NON_MATCHING_MEMBER:
				for (PersonId personId : expectedPeopleInPartition) {
					if (!expectedPeopleMatchingQueryLabelSet.contains(personId)) {
						excludedPersonId = personId;
						break;
					}
				}
				break;

			case NON_MEMBER:
				if (useFilter) {
					for (PersonId personId : peopleInTheWorld) {
						if (!expectedPeopleInPartition.contains(personId)) {
							excludedPersonId = personId;
							break;
						}
					}
				}
				break;

			case NULL:
				// do nothing
				break;
			default:
				throw new RuntimeException("unhandled case: " + excludedPersonType);
			}
			partitionSamplerBuilder.setExcludedPerson(excludedPersonId);

			Set<PersonId> expectedPeopleMatchingPartitionSampler = new LinkedHashSet<>(expectedPeopleMatchingQueryLabelSet);
			expectedPeopleMatchingPartitionSampler.remove(excludedPersonId);

			if (useWeightingFunction) {
				Iterator<PersonId> iterator = expectedPeopleMatchingPartitionSampler.iterator();
				while (iterator.hasNext()) {
					PersonId personId = iterator.next();
					Integer int_1_attributeValue = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
					Boolean passed = (Boolean) int_1_labelFunction.apply(int_1_attributeValue);
					if (!passed) {
						iterator.remove();
					}
				}

				LabelSetWeightingFunction labelSetWeightingFunction = (c2, labelSet) -> {
					Boolean value = (Boolean) labelSet.getLabel(TestAttributeId.INT_1).get();
					if (value) {
						return 1;
					} else {
						return 0;
					}
				};
				partitionSamplerBuilder.setLabelSetWeightingFunction(labelSetWeightingFunction);
			}

			PartitionSampler partitionSampler = partitionSamplerBuilder.build();

			int samplingCount = FastMath.min(expectedPeopleMatchingQueryLabelSet.size() + 1, 10);

			for (int i = 0; i < samplingCount; i++) {
				Optional<PersonId> optional = partitionDataManager.samplePartition(key, partitionSampler);
				if (optional.isPresent()) {
					PersonId selectedPerson = optional.get();
					assertTrue(expectedPeopleMatchingPartitionSampler.contains(selectedPerson));
				} else {
					assertTrue(expectedPeopleMatchingPartitionSampler.isEmpty());
				}
			}

		});

	}

	@Test
	@UnitTestMethod(name = "samplePartition", args = { Object.class, PartitionSampler.class })
	public void testSamplePartition_PreconditionChecks() {

		// precondition: if the key is null
		PartitionsActionSupport.testConsumer(10, 8368182028203057994L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			partitionDataManager.addPartition(partition, key);

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataManager.samplePartition(key, partitionSampler));

			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.samplePartition(null, partitionSampler));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

		});

		// precondition: if the key is unknown
		PartitionsActionSupport.testConsumer(10, 2301450217287059237L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			partitionDataManager.addPartition(partition, key);

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			Object unknownKey = new Object();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataManager.samplePartition(key, partitionSampler));

			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.samplePartition(unknownKey, partitionSampler));
			assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

		});

		PartitionsActionSupport.testConsumer(10, 8837909864261179707L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			partitionDataManager.addPartition(partition, key);

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataManager.samplePartition(key, partitionSampler));

			// if the partition sampler is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.samplePartition(key, null));
			assertEquals(PartitionError.NULL_PARTITION_SAMPLER, contractException.getErrorType());

		});

		/*
		 * precondition: if the partition sampler has a label set containing
		 * dimensions not present in the population partition
		 */
		PartitionsActionSupport.testConsumer(10, 1697817005173536231L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			partitionDataManager.addPartition(partition, key);

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, 15).build();
			PartitionSampler partitionSamplerWithBadDimension = PartitionSampler.builder().setLabelSet(labelSet).build();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataManager.samplePartition(key, partitionSampler));

			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.samplePartition(key, partitionSamplerWithBadDimension));
			assertEquals(PartitionError.INCOMPATIBLE_LABEL_SET, contractException.getErrorType());

		});

		/*
		 * precondition: if the partition sampler has an excluded person that
		 * does not exist
		 */
		PartitionsActionSupport.testConsumer(10, 624346712512051803L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			partitionDataManager.addPartition(partition, key);

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			PartitionSampler partitionSamplerWithUnknownExcludedPerson = PartitionSampler.builder().setExcludedPerson(new PersonId(10000)).build();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataManager.samplePartition(key, partitionSampler));

			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataManager.samplePartition(key, partitionSamplerWithUnknownExcludedPerson));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPartitionDataManagerInitialization() {
		PartitionsActionSupport.testConsumer(0, 2954766214498605129L, (c) -> {
			final Optional<PartitionDataManager> optional = c.getDataManager(PartitionDataManager.class);
			assertTrue(optional.isPresent());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonCreationObservationEvent() {
		PartitionsActionSupport.testConsumer(100, 6964380012813498875L, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();

			/*
			 * Create keys for the two population partitions. One that accepts
			 * people with attribute BOOLEAN_0 = true and the other with
			 * BOOLEAN_0 = false.
			 */
			Object key1 = new Object();
			Object key2 = new Object();

			// add the partitions
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition1 = Partition.builder().setFilter(filter).build();
			partitionDataManager.addPartition(partition1, key1);

			filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);
			Partition partition2 = Partition.builder().setFilter(filter).build();
			partitionDataManager.addPartition(partition2, key2);

			// add a new person, by default they will have BOOLEAN_0 = false
			// determine the person id of the person just added
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().build());

			// show that the person is not a member of partition 1
			assertFalse(partitionDataManager.contains(personId, key1));

			// show that the person is a member of partition 2
			assertTrue(partitionDataManager.contains(personId, key2));

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonImminentRemovalObservationEvent() {

		TestPluginData.Builder pluginBuilder = TestPluginData.builder();

		/*
		 * Create a key for a partition of interest that will contain a person
		 * we are about to delete
		 */
		Object key = new Object();

		/*
		 * Add an agent that will create a partition that will contain 10 people
		 * of interest who will be removed later.
		 */

		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(0, (c) -> {

			// select 10 people
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			List<PersonId> peopleOfInterest = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				peopleOfInterest.add(people.get(i));
			}

			/*
			 * Give these people an attribute BOOLEAN_0 a value of true so they
			 * will be included in the partition
			 */
			for (PersonId personId : peopleOfInterest) {
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
			}

			/*
			 * Create a partition that will include the people of interest
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			partitionDataManager.addPartition(partition, key);

			// show that the partition does contain the people of interest
			List<PersonId> actualPeople = partitionDataManager.getPeople(key);
			assertEquals(peopleOfInterest.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(peopleOfInterest), new LinkedHashSet<>(actualPeople));

		}));

		/*
		 * Create an observer that subscribes to the
		 * PersonImminentRemovalObservationEvent. This will be used to show that
		 * a report or any other observer can still see a person and their
		 * membership in a partition even though the removal of the person is
		 * already underway.
		 * 
		 * The report will record the ids of the people who were in the removal
		 * process
		 */
		List<PersonId> peopleVerifiedByReport = new ArrayList<>();

		pluginBuilder.addTestActorPlan("observer", new TestActorPlan(0, (c) -> {
			c.subscribe(PersonImminentRemovalObservationEvent.class, (c2, e) -> {

				PersonId personId = e.getPersonId();

				// show that the person is still in the partition
				PartitionDataManager partitionDataManager = c2.getDataManager(PartitionDataManager.class).get();
				assertTrue(partitionDataManager.contains(personId, key));

				// add the person to the verified list for later use
				peopleVerifiedByReport.add(personId);

			});
		}));

		/*
		 * Have the agent remove the people who are in the partition from the
		 * simulation. The people will temporarily remain in the simulation and
		 * will only leave when the planning system moves to the next plan.
		 */
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {

			// Remove from the simulation the people who are in the partition
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			List<PersonId> people = partitionDataManager.getPeople(key);
			for (PersonId personId : people) {
				personDataManager.removePerson(personId);
			}

			// show that the people still exist
			
			for (PersonId personId : people) {
				assertTrue(personDataManager.personExists(personId));
			}
			List<PersonId> peopleImmediatelyAfterRemoval = partitionDataManager.getPeople(key);
			assertEquals(people, peopleImmediatelyAfterRemoval);

		}));

		/*
		 * Have the agent verify that the people are gone and that the partition
		 * no longer contains them. Note that this plan is for the same time as
		 * the plan above but is guaranteed to execute after that plan.
		 */
		pluginBuilder.addTestActorPlan("agent", new TestActorPlan(1, (c) -> {
			/*
			 * Show that the report, as an observer of the removals, was able to
			 * observe each removal and still perceived each person as being a
			 * member of the partition.
			 */
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			assertEquals(10, peopleVerifiedByReport.size());

			// show that each of these people is no longer in the simulation
			for (PersonId personId : peopleVerifiedByReport) {
				assertFalse(personDataManager.personExists(personId));
			}

			// show that the partition is empty
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			assertEquals(0, partitionDataManager.getPersonCount(key));

		}));

		// build and add the action plugin to the engine
		TestPluginData testPluginData = pluginBuilder.build();
		Plugin testPlugin = TestPlugin.getPlugin(testPluginData);
		PartitionsActionSupport.testConsumers(100, 6406306513403641718L, testPlugin);
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testBulkPersonCreationObservationEvent() {
		PartitionsActionSupport.testConsumer(100, 2561425586247460069L, (c) -> {
			PartitionDataManager partitionDataManager = c.getDataManager(PartitionDataManager.class).get();
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			/*
			 * Create keys for the two population partitions. One that accepts
			 * people with attribute BOOLEAN_0 = true and the other with
			 * BOOLEAN_0 = false.
			 */
			Object key1 = new Object();
			Object key2 = new Object();

			// add the partitions
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition1 = Partition.builder().setFilter(filter).build();
			partitionDataManager.addPartition(partition1, key1);

			filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);
			Partition partition2 = Partition.builder().setFilter(filter).build();
			partitionDataManager.addPartition(partition2, key2);

			// determine the person ids of the people before the bulk addition
			List<PersonId> priorPeople = personDataManager.getPeople();

			// add three new people, by default they will have BOOLEAN_0 = false
			PersonConstructionData.Builder personBuilder = PersonConstructionData.builder();
			BulkPersonConstructionData bulkPersonConstructionData = BulkPersonConstructionData.builder().add(personBuilder.build()).add(personBuilder.build()).add(personBuilder.build()).build();
			personDataManager.addBulkPeople(bulkPersonConstructionData);

			// determine the new people who were added
			List<PersonId> newPeople = personDataManager.getPeople();
			newPeople.removeAll(priorPeople);

			// show that there are three new people
			assertEquals(3, newPeople.size());

			// show that the new people are not members of partition 1
			for (PersonId personId : newPeople) {
				assertFalse(partitionDataManager.contains(personId, key1));
			}
			// show that the new people are members of partition 2
			for (PersonId personId : newPeople) {
				assertTrue(partitionDataManager.contains(personId, key2));
			}
		});
	}

}