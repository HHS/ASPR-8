package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Event;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.Context;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.events.observation.AttributeChangeObservationEvent;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.stochastics.datacontainers.StochasticsDataView;
import util.ContractException;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = DegeneratePopulationPartitionImpl.class)
public class AT_DegeneratePopulationPartitionImpl {

	@Test
	@UnitTestConstructor(args = { Context.class, Partition.class })
	public void testConstructor() {

		PartitionsActionSupport.testConsumer(100, 3760806761100897313L, (c) -> {
			// establish data view
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// select about half of the people
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// set attribute BOOLEAN_0 to true for those people
			for (PersonId personId : expectedPeople) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
			}

			// create the population partition
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the population partition contains the expected people
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests
			// if the context is null
			assertThrows(RuntimeException.class, () -> new DegeneratePopulationPartitionImpl(null, partition));

			// if the partition is null
			assertThrows(RuntimeException.class, () -> new DegeneratePopulationPartitionImpl(c, null));

			// if the partition is not degenerate
			ContractException contractException = assertThrows(ContractException.class,
					() -> new DegeneratePopulationPartitionImpl(c, Partition.builder().addLabeler(new AttributeLabeler(TestAttributeId.BOOLEAN_0, (v) -> v)).build()));
			assertEquals(PartitionError.NON_DEGENERATE_PARTITION, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "attemptPersonAddition", args = { PersonId.class })
	public void testAttemptPersonAddition() {

		PartitionsActionSupport.testConsumer(100, 2545018253500191849L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// precondition test:
			assertThrows(RuntimeException.class, () -> populationPartition.attemptPersonAddition(null));

			/*
			 * Add new people, setting the attribute to alternating values of
			 * true and false
			 */
			for (int i = 0; i < 20; i++) {
				c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
				PersonId personId = personDataView.getLastIssuedPersonId().get();
				boolean attributeValue = i % 2 == 0;
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, attributeValue));
				populationPartition.attemptPersonAddition(personId);

				/*
				 * Show that the person is in the population partition if and
				 * only if their attribute value was set to true
				 */
				assertEquals(attributeValue, populationPartition.contains(personId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "attemptPersonRemoval", args = { PersonId.class })
	public void testAttemptPersonRemoval() {
		PartitionsActionSupport.testConsumer(100, 1924419629240381672L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the expected people are in the population partition
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			/*
			 * Remove people and show that they are no longer in the partition
			 */
			for (PersonId personId : expectedPeople) {
				c.resolveEvent(new PersonRemovalRequestEvent(personId));
				populationPartition.attemptPersonRemoval(personId);
				// show that the person was removed
				assertFalse(populationPartition.contains(personId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "handleEvent", args = { Event.class })
	public void testHandleEvent() {

		PartitionsActionSupport.testConsumer(100, 5331854470768144150L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			for (PersonId personId : personDataView.getPeople()) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean()));

			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			for (PersonId personId : personDataView.getPeople()) {
				Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, !b0));
				populationPartition.handleEvent(new AttributeChangeObservationEvent(personId, TestAttributeId.BOOLEAN_0, b0, !b0));

				assertEquals(!b0, populationPartition.contains(personId));

			}

		});
	}

	@Test
	@UnitTestMethod(name = "validateLabelSetInfo", args = { LabelSet.class })
	public void testValidateLabelSetInfo() {
		PartitionsActionSupport.testConsumer(100, 7896267308674363012L, (c) -> {
			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, 2).build();
			assertFalse(populationPartition.validateLabelSetInfo(labelSet));

			assertTrue(populationPartition.validateLabelSetInfo(LabelSet.builder().build()));

		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleCount", args = {})
	public void testGetPeopleCount() {

		PartitionsActionSupport.testConsumer(100, 2295886123984917407L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the people count matches expectations
			assertEquals(expectedPeople.size(), populationPartition.getPeopleCount());

			/*
			 * Change the attributes for the expected people and show that the
			 * expected count is correct
			 */
			int expectedPeopleCount = expectedPeople.size();
			for (PersonId personId : expectedPeople) {
				expectedPeopleCount--;
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, false));
				populationPartition.handleEvent(new AttributeChangeObservationEvent(personId, TestAttributeId.BOOLEAN_0, true, false));
				assertEquals(expectedPeopleCount, populationPartition.getPeopleCount());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getPeopleCount", args = { LabelSet.class })
	public void testGetPeopleCount_LabelSet() {
		PartitionsActionSupport.testConsumer(1000, 1957059921486084637L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			// Randomize the attribute values for all people
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean()));
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean()));
			}

			// build a container to hold the expected relationship from label
			// sets to people
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (PersonId personId : personDataView.getPeople()) {
				Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Boolean b1 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
				if (b0 && !b1) {
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition.builder().setFilter(filter).build();

			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the people count matches expectations
			List<PersonId> actualPeople = populationPartition.getPeople(LabelSet.builder().build());
			assertEquals(expectedPeople.size(), actualPeople.size());

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleCountMap", args = { LabelSet.class })
	public void testGetPeopleCountMap() {

		PartitionsActionSupport.testConsumer(1000, 5254073186909000918L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			// randomize BOOLEAN_0 attribute values
			for (PersonId personId : personDataView.getPeople()) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean()));
			}
			// build the population partition with the BOOLEAN_0
			Partition partition = Partition.builder().setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true)).build();
			DegeneratePopulationPartitionImpl populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			Map<LabelSet, Integer> peopleCountMap = populationPartition.getPeopleCountMap(LabelSet.builder().build());

			assertEquals(1, peopleCountMap.size());
			LabelSet keyLabelSet = peopleCountMap.keySet().iterator().next();
			assertTrue(keyLabelSet.isEmpty());
			Integer count = peopleCountMap.get(keyLabelSet);
			assertEquals(populationPartition.getPeopleCount(), count);

		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class })
	public void testContains() {
		PartitionsActionSupport.testConsumer(100, 2907418341194860848L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the person data view contains the people we expect
			assertEquals(expectedPeople.size(), populationPartition.getPeople().size());
			for (PersonId personId : personDataView.getPeople()) {
				assertEquals(expectedPeople.contains(personId), populationPartition.contains(personId));
			}
		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class, LabelSet.class })
	public void testContains_LabelSet() {
		PartitionsActionSupport.testConsumer(100, 2888054511830289156L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			LabelSet labelSet = LabelSet.builder().build();
			// show that the person data view contains the people we expect
			assertEquals(expectedPeople.size(), populationPartition.getPeople().size());
			for (PersonId personId : personDataView.getPeople()) {
				assertEquals(expectedPeople.contains(personId), populationPartition.contains(personId, labelSet));
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getPeople", args = { LabelSet.class })
	public void testGetPeople_LabelSet() {
		PartitionsActionSupport.testConsumer(1000, 8577028018353363458L, (c) -> {

			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			// Randomize the attribute values for all people
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (PersonId personId : personDataView.getPeople()) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, randomGenerator.nextBoolean()));
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_1, randomGenerator.nextBoolean()));
			}

			// build a container to hold the expected relationship from label
			// sets to people
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (PersonId personId : personDataView.getPeople()) {
				Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Boolean b1 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
				if (b0 && !b1) {
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition.builder().setFilter(filter).build();

			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the people count matches expectations
			List<PersonId> actualPeople = populationPartition.getPeople(LabelSet.builder().build());
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

		});

	}

	@Test
	@UnitTestMethod(name = "getPeople", args = {})
	public void testGetPeople() {
		PartitionsActionSupport.testConsumer(100, 3706541397073246652L, (c) -> {
			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : personDataView.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new DegeneratePopulationPartitionImpl(c, partition);

			// show that the person data view contains the people we expect
			assertEquals(expectedPeople.size(), populationPartition.getPeople().size());
			assertEquals(expectedPeople, new LinkedHashSet<>(populationPartition.getPeople()));

		});
	}

	private static enum ExcludedPersonType {
		NULL, MATCHING_MEMBER, NON_MATCHING_MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestMethod(name = "samplePartition", args = { PartitionSampler.class })
	public void testSamplePartition() {

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
		 * will either be null or empty since degenerate population partitions
		 * do no contain labelers.
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

		Set<Boolean> useLabelSetValues = new LinkedHashSet<>();
		useLabelSetValues.add(false);
		useLabelSetValues.add(true);

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(8925918754735468568L);

		for (ExcludedPersonType excludedPersonType : ExcludedPersonType.values()) {
			for (Boolean useWeightingFunction : weightingFunctionValues) {
				for (Boolean useFilter : useFilterValues) {
					long seed = randomGenerator.nextLong();
					for (Boolean useLabelSet : useLabelSetValues) {
						seed = randomGenerator.nextLong();
						executeSamplingTest(seed, useFilter, excludedPersonType, useWeightingFunction, useLabelSet);
					}
				}
			}
		}
	}

	private void executeSamplingTest(long seed, Boolean useFilter, ExcludedPersonType excludedPersonType, Boolean useWeightingFunction, boolean useLabelSet) {

		PartitionsActionSupport.testConsumer(1000, seed, (c) -> {

			// remember to test with general and COMET to show they get
			// different results?

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

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
			List<PersonId> peopleInTheWorld = personDataView.getPeople();

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : peopleInTheWorld) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, intValue));

				intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_1, intValue));

				double doubleValue = randomGenerator.nextDouble() * 100;
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_0, doubleValue));

				doubleValue = randomGenerator.nextDouble() * 100;
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_1, doubleValue));

				boolean booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, booleanValue));

				booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_1, booleanValue));

			}

			/*
			 * Create a partition that may filter about half of the population
			 * on BOOLEAN_0. We will partition on INT_0, INT_1, DOUBLE_0 and
			 * DOUBLE_1. Note that we do not use BOOLEAN_1 as part of the
			 * partition.
			 */
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

			PopulationPartitionImpl populationPartition = new PopulationPartitionImpl(c, partition);

			/*
			 * Create a label set for the query.
			 */
			LabelSet queryLabelSet = null;
			if (useLabelSet) {
				queryLabelSet = LabelSet.builder().build();
			}

			// determine the people in the partition
			Set<PersonId> expectedPeopleInPartition = new LinkedHashSet<>();

			for (PersonId personId : peopleInTheWorld) {

				if (useFilter) {
					Boolean personInPartition = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
					if (personInPartition) {
						expectedPeopleInPartition.add(personId);
					}
				} else {
					expectedPeopleInPartition.add(personId);
				}

			}

			// determine the people who will match the query label set
			Set<PersonId> expectedPeopleMatchingQueryLabelSet = new LinkedHashSet<>();
			expectedPeopleMatchingQueryLabelSet.addAll(expectedPeopleInPartition);

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
					Integer int_1_attributeValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_1);
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
				Optional<PersonId> optional = populationPartition.samplePartition(partitionSampler);
				if (optional.isPresent()) {
					PersonId selectedPerson = optional.get();
					assertTrue(expectedPeopleMatchingPartitionSampler.contains(selectedPerson));
				} else {
					assertTrue(expectedPeopleMatchingPartitionSampler.isEmpty());
				}
			}

		});

	}

}
