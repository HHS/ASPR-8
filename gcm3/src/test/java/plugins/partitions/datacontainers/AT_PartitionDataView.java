package plugins.partitions.datacontainers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import nucleus.AgentId;
import nucleus.Context;
import nucleus.NucleusError;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.LabelSetWeightingFunction;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionError;
import plugins.partitions.support.PartitionSampler;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.support.StochasticsError;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.ContractException;
import util.MutableInteger;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionDataView.class)
public final class AT_PartitionDataView {

	@Test
	@UnitTestConstructor(args = { Context.class, PartitionDataManager.class })
	public void testConstructor() {
		PartitionsActionSupport.testConsumer(0, 4959182295195625802L, (c) -> {

			PartitionDataManager partitionDataManager = new PartitionDataManager();

			assertNotNull(new PartitionDataView(c, partitionDataManager));

			// precondition tests
			ContractException contractException = assertThrows(ContractException.class, () -> new PartitionDataView(null, partitionDataManager));
			assertEquals(NucleusError.NULL_CONTEXT, contractException.getErrorType());

			contractException = assertThrows(ContractException.class, () -> new PartitionDataView(c, null));
			assertEquals(PartitionError.NULL_PARTITION_DATA_MANAGER, contractException.getErrorType());
		});

	}

	@Test
	@UnitTestMethod(name = "getPeople", args = { Object.class })
	public void testGetPeople() {

		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 6033209037401060593L, (c) -> {

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();

			// create a container to hold the people we expect to find in the
			// partition
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, intValue));

				boolean booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, booleanValue));
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
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// get the people in the partition
			List<PersonId> peopleInPartition = partitionDataView.getPeople(key);
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
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			// define a function that will convert an integer into another
			// integer that will be used as a labeling function for the
			// partition
			Function<Object, Object> attributeValueLabelingFunction = (value) -> {
				int v = (Integer) value;
				return v / 10;
			};

			// alter people's INT_0 and BOOLEAN_0 attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, intValue));

				boolean booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, booleanValue));
			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, attributeValueLabelingFunction))//
											.build();
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// create a container to hold the people we expect to find in the
			// partition associated with labels
			Map<Object, Set<PersonId>> expectedLabelToPeopleMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataView.getPeople()) {
				// will the person pass the filter?
				Boolean booleanValue = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {
					// determine the label that should be associated with the
					// person
					Integer intValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
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
				List<PersonId> peopleInPartition = partitionDataView.getPeople(key, labelSet);
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
	@UnitTestMethod(name = "getPersonCount", args = { Object.class })
	public void getPersonCount() {

		// initialized with 100 people
		PartitionsActionSupport.testConsumer(100, 1559429415782871174L, (c) -> {

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();

			// create a couter to hold the number people we expect to find in
			// the
			// partition
			int expectedPeopleCount = 0;

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, intValue));

				boolean booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, booleanValue));
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
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// get the people in the partition
			int actualCount = partitionDataView.getPersonCount(key);

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
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			// define a function that will convert an integer into another
			// integer that will be used as a labeling function for the
			// partition
			Function<Object, Object> attributeValueLabelingFunction = (value) -> {
				int v = (Integer) value;
				return v % 10;
			};

			// alter people's INT_0 and BOOLEAN_0 attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				int intValue = (int) (randomGenerator.nextDouble() * 100);
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, intValue));

				boolean booleanValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, booleanValue));
			}

			// create a partition that will contain about half of the population
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, attributeValueLabelingFunction))//
											.build();
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// create a container to hold the people we expect to find in the
			// partition associated with labels
			Map<Object, MutableInteger> expectedPeopleCountMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataView.getPeople()) {
				// will the person pass the filter?
				Boolean booleanValue = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {
					// determine the label that should be associated with the
					// person
					Integer intValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
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
				int actualPersonCount = partitionDataView.getPersonCount(key, labelSet);

				// show that expected and actual people counts are equal
				assertEquals(expectedPeopleCount.getValue(), actualPersonCount);
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getPeopleCountMap", args = { Object.class, LabelSet.class })
	public void testGetPeopleCountMap() {
		// initialized with 1000 people
		PartitionsActionSupport.testConsumer(1000, 3993911184725585603L, (c) -> {

			// establish data views
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
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

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
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

			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			/*
			 * Create a container to hold the number of people we expect to find
			 * in the partition for every label set that is associated with at
			 * least one person.
			 */
			Map<LabelSet, MutableInteger> expectedPartitionContentMap = new LinkedHashMap<>();

			// fill the expectedLabelToPeopleMap
			for (PersonId personId : personDataView.getPeople()) {
				// will the person pass the filter?
				Boolean booleanValue = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				if (booleanValue) {

					// construct the label set for this person we expect to
					// exist in the partition
					LabelSet.Builder labelSetBuilder = LabelSet.builder();

					Integer intValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = int_0_labelFunction.apply(intValue);
					labelSetBuilder.setLabel(TestAttributeId.INT_0, labelValue);

					intValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_1);
					labelValue = int_1_labelFunction.apply(intValue);
					labelSetBuilder.setLabel(TestAttributeId.INT_1, labelValue);

					Double doubleValue = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
					labelValue = double_0_labelFunction.apply(doubleValue);
					labelSetBuilder.setLabel(TestAttributeId.DOUBLE_0, labelValue);

					doubleValue = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
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
					Map<LabelSet, Integer> actualCountMap = partitionDataView.getPeopleCountMap(key, queryLabelSet);
					assertEquals(expectedCountMap, actualCountMap);
				}
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

		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(7729976665156925181L);

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
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
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

			c.resolveEvent(new PartitionAdditionEvent(partition, key));

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
			if (queryLabelSet != null) {
				for (PersonId personId : expectedPeopleInPartition) {
					// will the person pass the filter?

					Integer intValue = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
					Object labelValue = int_0_labelFunction.apply(intValue);
					if (labelValue.equals(int_0_label_value)) {
						Double doubleValue = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
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
				Optional<PersonId> optional = partitionDataView.samplePartition(key, partitionSampler);
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
		PartitionsActionSupport.testConsumer(100, 8368182028203057994L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			Object key = new Object();
			Partition partition = Partition.builder().setFilter(Filter.allPeople()).build();
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			PartitionSampler partitionSampler = PartitionSampler.builder().build();

			LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, 15).build();
			PartitionSampler partitionSamplerWithBadDimension = PartitionSampler.builder().setLabelSet(labelSet).build();

			PartitionSampler partitionSamplerWithUnknownExcludedPerson = PartitionSampler.builder().setExcludedPerson(new PersonId(10000)).build();

			PartitionSampler partitionSamplerWithUnknownRandomGeneratorId = PartitionSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.getUnknownRandomNumberGeneratorId()).build();

			Object unknownKey = new Object();

			// first we show that the values we will be using are valid
			assertNotNull(partitionDataView.samplePartition(key, partitionSampler));

			// if the key is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(null, partitionSampler));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

			// if the key is unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(unknownKey, partitionSampler));
			assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

			// if the partition sampler is null
			contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(key, null));
			assertEquals(PartitionError.NULL_PARTITION_SAMPLER, contractException.getErrorType());

			// if the partition sampler has a label set containing dimensions
			// not present in the population partition
			contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(key, partitionSamplerWithBadDimension));
			assertEquals(PartitionError.INCOMPATIBLE_LABEL_SET, contractException.getErrorType());

			// if the partition sampler has an excluded person that does not
			// exist
			contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(key, partitionSamplerWithUnknownExcludedPerson));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the partition sampler has a random number generator id that is
			// unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataView.samplePartition(key, partitionSamplerWithUnknownRandomGeneratorId));
			assertEquals(StochasticsError.UNKNOWN_RANDOM_NUMBER_GENERATOR_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class, Object.class })
	public void testContains() {
		// 607630153604184177L
		PartitionsActionSupport.testConsumer(100, 607630153604184177L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			StochasticsDataView stochasticsDataView = c.getDataView(StochasticsDataView.class).get();
			AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();

			// create a partition where half the population is in the partition
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, (v) -> 3)).build();//

			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// change the BOOLEAN_0 randomly for every person
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
				boolean newValue = randomGenerator.nextBoolean();
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, newValue));
			}

			// show that there is at least one person in the partition and one
			// person outside the partition
			int personCountInPartition = partitionDataView.getPersonCount(key);
			assertTrue(personCountInPartition < personDataView.getPopulationCount());
			assertTrue(personCountInPartition > 0);

			// show that a person is in the partition if and only if their
			// BOOLEAN_0 attribute is true
			for (PersonId personId : personDataView.getPeople()) {
				boolean expectPersonInPartition = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				boolean actualPersonInPartition = partitionDataView.contains(personId, key);
				assertEquals(expectPersonInPartition, actualPersonInPartition);
			}

		});
	}

	@Test
	@UnitTestMethod(name = "contains", args = { PersonId.class, LabelSet.class, Object.class })
	public void testContains_LabelSet() {

		PartitionsActionSupport.testConsumer(100, 7338572401998066291L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
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

			// create a partition where half the population is in the partition
			// with labeling
			Object key = new Object();
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.addLabeler(new AttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction)).addLabeler(new AttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))
											.addLabeler(new AttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction)).build();//

			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// alter people's attributes randomly
			RandomGenerator randomGenerator = stochasticsDataView.getRandomGenerator();
			for (PersonId personId : personDataView.getPeople()) {
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
			for (PersonId personId : personDataView.getPeople()) {
				boolean contained = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);

				Integer int0Value = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
				Integer int0Label = (Integer) int_0_labelFunction.apply(int0Value);

				Double double0Value = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
				String double0Label = (String) double_0_labelFunction.apply(double0Value);

				boolean expectPersonInPartitionUnderLabel = contained && int0Label.equals(0) && double0Label.equals("A");

				boolean actualPersonInPartitionUnderLabel = partitionDataView.contains(personId, queryLabelSet, key);
				assertEquals(expectPersonInPartitionUnderLabel, actualPersonInPartitionUnderLabel);
			}

			// precondition tests
			PersonId personId = new PersonId(0);
			assertTrue(personDataView.personExists(personId));

			PersonId unknownPersonId = new PersonId(10000000);
			assertFalse(personDataView.personExists(unknownPersonId));

			Object unknownKey = new Object();

			LabelSet badLabelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, 0).build();

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(null, queryLabelSet, key));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(unknownPersonId, queryLabelSet, key));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the key is null
			contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(personId, queryLabelSet, null));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

			// if the key is unknown
			contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(personId, queryLabelSet, unknownKey));
			assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

			// if the label set is null
			contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(personId, null, key));
			assertEquals(PartitionError.NULL_LABEL_SET, contractException.getErrorType());

			// if the label contains a dimension not present in the partition
			contractException = assertThrows(ContractException.class, () -> partitionDataView.contains(personId, badLabelSet, key));
			assertEquals(PartitionError.INCOMPATIBLE_LABEL_SET, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "partitionExists", args = { Object.class })
	public void testPartitionExists() {

		PartitionsActionSupport.testConsumer(0, 3994373028296366190L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			// create a key
			Object key = new Object();

			// show there is no partition for the key
			assertFalse(partitionDataView.partitionExists(key));

			// add a partition for the key
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.build();//

			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// show that there is a partition for the key
			assertTrue(partitionDataView.partitionExists(key));

			// remove the partition
			c.resolveEvent(new PartitionRemovalEvent(key));

			// show there is no partition for the key
			assertFalse(partitionDataView.partitionExists(key));

		});
	}

	@Test
	@UnitTestMethod(name = "getOwningAgentId", args = { Object.class })
	public void testGetOwningAgentId() {
		Object key1 = new Object();
		Object key2 = new Object();
		Map<Object, AgentId> ownerMap = new LinkedHashMap<>();

		/*
		 * Add an agent that executes the consumer.
		 * 
		 * Add a second agent to show that the initial population exists and the
		 * attribute ids exist.
		 * 
		 */
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		/*
		 * Add an agent that will add the first partition
		 */
		pluginBuilder.addAgent("agent_1");
		pluginBuilder.addAgentActionPlan("agent_1", new AgentActionPlan(0, (c) -> {
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.build();//

			c.resolveEvent(new PartitionAdditionEvent(partition, key1));
			ownerMap.put(key1, c.getCurrentAgentId());
		}));

		/*
		 * Add a second agent that will add the second partition
		 */
		pluginBuilder.addAgent("agent_2");
		pluginBuilder.addAgentActionPlan("agent_2", new AgentActionPlan(0, (c) -> {
			Partition partition = Partition	.builder()//
											.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true))//
											.build();//

			c.resolveEvent(new PartitionAdditionEvent(partition, key2));
			ownerMap.put(key2, c.getCurrentAgentId());
		}));

		/*
		 * Add a third agent that show that the owning agent ids are correct
		 */
		pluginBuilder.addAgent("agent_3");
		pluginBuilder.addAgentActionPlan("agent_3", new AgentActionPlan(1, (c) -> {

			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			AgentId expectedAgentId = ownerMap.get(key1);
			AgentId actualAgentId = partitionDataView.getOwningAgentId(key1);
			assertEquals(expectedAgentId, actualAgentId);

			expectedAgentId = ownerMap.get(key2);
			actualAgentId = partitionDataView.getOwningAgentId(key2);
			assertEquals(expectedAgentId, actualAgentId);

		}));
		// precondition tests
		pluginBuilder.addAgent("agent_3");
		pluginBuilder.addAgentActionPlan("agent_3", new AgentActionPlan(2, (c) -> {

			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			// if the key is null
			ContractException contractException = assertThrows(ContractException.class, () -> partitionDataView.getOwningAgentId(null));
			assertEquals(PartitionError.NULL_PARTITION_KEY, contractException.getErrorType());

			// if the key is
			contractException = assertThrows(ContractException.class, () -> partitionDataView.getOwningAgentId(new Object()));
			assertEquals(PartitionError.UNKNOWN_POPULATION_PARTITION_KEY, contractException.getErrorType());

		}));

		// build and add the action plugin to the engine
		ActionPlugin actionPlugin = pluginBuilder.build();
		PartitionsActionSupport.testConsumers(0, 1836218798187614083L, actionPlugin);

	}

}