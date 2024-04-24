package gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestActorPlan;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestPluginData;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.datamanagers.PartitionsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.filters.Filter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.FunctionalAttributeLabeler;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.PartitionsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.AttributesDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.events.AttributeUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.AttributeFilter;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.attributes.support.TestAttributeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonConstructionData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestConstructor;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_PopulationPartitionImpl {

	@Test
	@UnitTestConstructor(target = PopulationPartitionImpl.class, args = { PartitionsContext.class, Partition.class,boolean.class })
	public void testConstructor() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 2997202170895856110L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data view
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// select about half of the people
			Set<PersonId> expectedPeople = new LinkedHashSet<>();
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					expectedPeople.add(personId);
				}
			}

			// set attribute BOOLEAN_0 to true for those people
			for (PersonId personId : expectedPeople) {
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
			}

			// create the population partition
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the population partition contains the expected people
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			// precondition tests
			// if the context is null
			assertThrows(RuntimeException.class, () -> new PopulationPartitionImpl(null, partition,false));

			// if the partition is null
			assertThrows(RuntimeException.class, () -> new PopulationPartitionImpl(testPartitionsContext, null,false));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "attemptPersonAddition", args = { PersonId.class })
	public void testAttemptPersonAddition() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 3063819509780972206L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// precondition test:
			assertThrows(RuntimeException.class, () -> populationPartition.attemptPersonAddition(null));

			/*
			 * Add new people, setting the attribute to alternating values of
			 * true and false
			 */
			for (int i = 0; i < 20; i++) {
				PersonId personId = peopleDataManager.addPerson(PersonConstructionData.builder().build());
				boolean attributeValue = i % 2 == 0;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, attributeValue);
				populationPartition.attemptPersonAddition(personId);

				/*
				 * Show that the person is in the population partition if and
				 * only if their attribute value was set to true
				 */
				assertEquals(attributeValue, populationPartition.contains(personId));
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "attemptPersonRemoval", args = { PersonId.class })
	public void testAttemptPersonRemoval() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 4856457716960397685L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the expected people are in the population partition
			List<PersonId> actualPeople = populationPartition.getPeople();
			assertEquals(expectedPeople.size(), actualPeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));

			/*
			 * Remove people and show that they are no longer in the partition
			 */
			for (PersonId personId : expectedPeople) {
				peopleDataManager.removePerson(personId);
				populationPartition.attemptPersonRemoval(personId);
				// show that the person was removed
				assertFalse(populationPartition.contains(personId));
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "handleEvent", args = { Event.class })
	public void testHandleEvent() {
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
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

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

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "validateLabelSetInfo", args = { LabelSet.class })
	public void testValidateLabelSetInfo() {

		Factory factory = PartitionsTestPluginFactory.factory(100, 4662203440339012044L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition	.builder().setFilter(filter).addLabeler(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> 1))
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, (i) -> "value")).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.BOOLEAN_1, 2).build();
			assertTrue(populationPartition.validateLabelSetInfo(labelSet));

			labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, 2).build();
			assertTrue(populationPartition.validateLabelSetInfo(labelSet));

			labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_1, 2).build();
			assertFalse(populationPartition.validateLabelSetInfo(labelSet));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPeopleCount", args = {})
	public void testGetPeopleCount() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 9050139615348413060L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the people count matches expectations
			assertEquals(expectedPeople.size(), populationPartition.getPeopleCount());

			/*
			 * Change the attributes for the expected people and show that the
			 * expected count is correct
			 */
			int expectedPeopleCount = expectedPeople.size();
			for (PersonId personId : expectedPeople) {
				expectedPeopleCount--;
				attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, false);
				populationPartition.handleEvent(new AttributeUpdateEvent(personId, TestAttributeId.BOOLEAN_0, true, false));
				assertEquals(expectedPeopleCount, populationPartition.getPeopleCount());
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
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
	 * Assigns randomized values for all attributes to all people. Values are
	 * assigned to be consistent with the static labeling functions.
	 */
	private static void assignRandomAttributes(final ActorContext c) {
		final PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
		final StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
		AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
		final RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		for (final PersonId personId : peopleDataManager.getPeople()) {
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

	/*
	 * Creates a map from LabelSet to PersonId that covers all people who have
	 * an attribute value of true for BOOLEAN_0 and false for BOOLEAN_1, to be
	 * consistent with the filter used in the partition addition test. Label
	 * sets consist of labels for INT_0, INT_1, DOUBLE_0 and DOUBLE_1.
	 */
	private static Map<LabelSet, Set<PersonId>> getExpectedStructure(final ActorContext c) {
		final PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
		final AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
		final Map<LabelSet, Set<PersonId>> expectedPeople = new LinkedHashMap<>();
		for (final PersonId personId : peopleDataManager.getPeople()) {

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

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPeopleCount", args = { LabelSet.class })
	public void testGetPeopleCount_LabelSet() {
		Factory factory = PartitionsTestPluginFactory.factory(1000, 8522399796145249846L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// Randomize the attribute values for all people
			assignRandomAttributes(c);

			// build a container to hold the expected relationship from label
			// sets to people
			Map<LabelSet, Set<PersonId>> expectedStructure = getExpectedStructure(c);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition	.builder()//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, INT_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, INT_1_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, DOUBLE_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, DOUBLE_1_LABELFUNCTION))//
											.setFilter(filter)//
											.build();//

			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the people count matches expectations
			int expectedCount = 0;
			for (LabelSet labelSet : expectedStructure.keySet()) {
				expectedCount += expectedStructure.get(labelSet).size();
			}
			assertEquals(expectedCount, populationPartition.getPeopleCount());

			for (LabelSet labelSet : expectedStructure.keySet()) {
				Set<PersonId> expectedPeople = expectedStructure.get(labelSet);
				List<PersonId> actualPeople = populationPartition.getPeople(labelSet);
				assertEquals(expectedPeople.size(), actualPeople.size());
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPeopleCountMap", args = { LabelSet.class })
	public void testGetPeopleCountMap() {
		Factory factory = PartitionsTestPluginFactory.factory(1000, 4793886153660135719L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// Randomize the attribute values for all people
			assignRandomAttributes(c);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true and BOOLEAN_1 = false
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition	.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, INT_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, INT_1_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, DOUBLE_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, DOUBLE_1_LABELFUNCTION))//
											.setFilter(filter)//
											.build();//

			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			List<Integer> int_0_labelValues = new ArrayList<>();
			int_0_labelValues.add(0);
			int_0_labelValues.add(1);
			int_0_labelValues.add(2);
			int_0_labelValues.add(3);

			List<String> double_0_labelValues = new ArrayList<>();
			double_0_labelValues.add("A");
			double_0_labelValues.add("B");
			double_0_labelValues.add("C");
			double_0_labelValues.add("D");

			// build a container to hold the expected relationship from label
			// sets to people
			Map<LabelSet, Map<LabelSet, Integer>> expectedStructure = new LinkedHashMap<>();

			// build the keys of the expected structure

			for (Integer int_0_labelValue : int_0_labelValues) {
				for (String double_0_labelValue : double_0_labelValues) {
					LabelSet.Builder labelSetBuilder = LabelSet.builder();
					LabelSet labelSet = labelSetBuilder.setLabel(TestAttributeId.INT_0, int_0_labelValue).setLabel(TestAttributeId.DOUBLE_0, double_0_labelValue).build();
					expectedStructure.put(labelSet, new LinkedHashMap<>());
				}
			}

			// build the values of the expected structure
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			for (PersonId personId : peopleDataManager.getPeople()) {
				Boolean b0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				Boolean b1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
				if (b0 && !b1) {
					Integer i0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					Object int_0_label_value = INT_0_LABELFUNCTION.apply(i0);
					Double d0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
					Object double_0_label_value = DOUBLE_0_LABELFUNCTION.apply(d0);
					Integer i1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
					Object int_1_label_value = INT_1_LABELFUNCTION.apply(i1);
					Double d1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
					Object double_1_label_value = DOUBLE_1_LABELFUNCTION.apply(d1);

					LabelSet labelSet = LabelSet.builder().setLabel(TestAttributeId.INT_0, int_0_label_value).setLabel(TestAttributeId.DOUBLE_0, double_0_label_value).build();
					Map<LabelSet, Integer> map = expectedStructure.get(labelSet);
					labelSet = LabelSet	.builder().setLabel(TestAttributeId.INT_0, int_0_label_value).setLabel(TestAttributeId.DOUBLE_0, double_0_label_value)
										.setLabel(TestAttributeId.INT_1, int_1_label_value).setLabel(TestAttributeId.DOUBLE_1, double_1_label_value).build();
					Integer count = map.get(labelSet);
					if (count == null) {
						count = 0;
					}
					count = count + 1;
					map.put(labelSet, count);
				}
			}

			// show that every expected people count map corresponds to an
			// identical people count map from the population partition
			for (LabelSet labelSet : expectedStructure.keySet()) {
				Map<LabelSet, Integer> expectedPeopleCountMap = expectedStructure.get(labelSet);
				Map<LabelSet, Integer> actualPeopleCountMap = populationPartition.getPeopleCountMap(labelSet);
				assertEquals(expectedPeopleCountMap, actualPeopleCountMap);
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "contains", args = { PersonId.class })
	public void testContains() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 2652052463264971998L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
					expectedPeople.add(personId);

				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the person data view contains the people we expect
			assertEquals(expectedPeople.size(), populationPartition.getPeople().size());
			for (PersonId personId : peopleDataManager.getPeople()) {

				assertEquals(expectedPeople.contains(personId), populationPartition.contains(personId));
			}
		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "contains", args = { PersonId.class, LabelSet.class })
	public void testContains_LabelSet() {
		Factory factory = PartitionsTestPluginFactory.factory(1000, 827063967966581841L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// Randomize the attribute values for all people
			assignRandomAttributes(c);

			// build a container to hold the expected relationship from label
			// sets to people
			Map<LabelSet, Set<PersonId>> expectedStructure = getExpectedStructure(c);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition	.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, INT_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, INT_1_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, DOUBLE_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, DOUBLE_1_LABELFUNCTION))//
											.setFilter(filter)//
											.build();//

			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the people count matches expectations
			int expectedCount = 0;
			for (LabelSet labelSet : expectedStructure.keySet()) {
				expectedCount += expectedStructure.get(labelSet).size();
			}
			assertEquals(expectedCount, populationPartition.getPeopleCount());

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> allPeople = peopleDataManager.getPeople();

			// show that each label set contains the people expected
			for (LabelSet labelSet : expectedStructure.keySet()) {
				Set<PersonId> expectedPeople = expectedStructure.get(labelSet);
				for (PersonId personId : allPeople) {
					assertEquals(expectedPeople.contains(personId), populationPartition.contains(personId, labelSet));
				}
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPeople", args = { LabelSet.class })
	public void testGetPeople_LabelSet() {
		Factory factory = PartitionsTestPluginFactory.factory(1000, 1040083420377037302L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// Randomize the attribute values for all people
			assignRandomAttributes(c);

			// build a container to hold the expected relationship from label
			// sets to people
			Map<LabelSet, Set<PersonId>> expectedStructure = getExpectedStructure(c);

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter_0 = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Filter filter_1 = new AttributeFilter(TestAttributeId.BOOLEAN_1, Equality.EQUAL, false);
			Filter filter = filter_0.and(filter_1);
			Partition partition = Partition	.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, INT_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, INT_1_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, DOUBLE_0_LABELFUNCTION))//
											.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, DOUBLE_1_LABELFUNCTION))//
											.setFilter(filter)//
											.build();//

			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the people count matches expectations
			int expectedCount = 0;
			for (LabelSet labelSet : expectedStructure.keySet()) {
				expectedCount += expectedStructure.get(labelSet).size();
			}
			assertEquals(expectedCount, populationPartition.getPeopleCount());

			for (LabelSet labelSet : expectedStructure.keySet()) {
				Set<PersonId> expectedPeople = expectedStructure.get(labelSet);
				List<PersonId> actualPeople = populationPartition.getPeople(labelSet);
				assertEquals(expectedPeople.size(), actualPeople.size());
				assertEquals(expectedPeople, new LinkedHashSet<>(actualPeople));
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPeople", args = {})
	public void testGetPeople() {
		Factory factory = PartitionsTestPluginFactory.factory(100, 4597503339659285165L, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

			/*
			 * Create a container for the people we expect to be contained in
			 * the population partition.
			 */
			Set<PersonId> expectedPeople = new LinkedHashSet<>();

			// select about half of the people to have attribute BOOLEAN_0 value
			// of true
			for (PersonId personId : peopleDataManager.getPeople()) {
				if (randomGenerator.nextBoolean()) {
					attributesDataManager.setAttributeValue(personId, TestAttributeId.BOOLEAN_0, true);
					expectedPeople.add(personId);
				}
			}

			/*
			 * Create the population partition filtering on attribute BOOLEAN_0
			 * = true
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().addLabeler(new FunctionalAttributeLabeler(TestAttributeId.BOOLEAN_1, (v) -> v)).setFilter(filter).build();
			PopulationPartition populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

			// show that the person data view contains the people we expect
			assertEquals(expectedPeople.size(), populationPartition.getPeople().size());
			assertEquals(expectedPeople, new LinkedHashSet<>(populationPartition.getPeople()));

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	private static enum ExcludedPersonType {
		NULL, MATCHING_MEMBER, NON_MATCHING_MEMBER, NON_MEMBER;
	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "samplePartition", args = { PartitionSampler.class })
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

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6166310781583500795L);

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

		Factory factory = PartitionsTestPluginFactory.factory(1000, seed, (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			// remember to test with general and COMET to show they get
			// different results?

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);

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
			List<PersonId> peopleInTheWorld = peopleDataManager.getPeople();

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
			Partition.Builder partitionBuilder = Partition.builder();
			if (useFilter) {
				partitionBuilder//
								.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true));//
			}
			partitionBuilder//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction));

			Partition partition = partitionBuilder.build();

			PopulationPartitionImpl populationPartition = new PopulationPartitionImpl(testPartitionsContext, partition,false);

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
				Optional<PersonId> optional = populationPartition.samplePartition(partitionSampler);
				if (optional.isPresent()) {
					PersonId selectedPerson = optional.get();
					assertTrue(expectedPeopleMatchingPartitionSampler.contains(selectedPerson));
				} else {
					assertTrue(expectedPeopleMatchingPartitionSampler.isEmpty());
				}
			}

		});
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = PopulationPartitionImpl.class, name = "getPersonValue", args = { LabelSetFunction.class, PersonId.class })
	public void testGetPersonValue() {
		RandomGenerator rng = RandomGeneratorProvider.getRandomGenerator(1889608169419896318L);
		long seed = rng.nextLong();

		String key = "key";

		/*
		 * Define functions that will convert attribute values into labels for
		 * attributes INT_0, INT_1, DOUBLE_0, and DOUBLE_1. We will use these in
		 * the partition's labeling
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

		TestPluginData.Builder testPluginDataBuilder = TestPluginData.builder();

		/*
		 * Have the actor set the attribute values for each person to random
		 * values
		 */
		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(0, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			// determine the people in the world
			List<PersonId> peopleInTheWorld = peopleDataManager.getPeople();

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
		}));

		/*
		 * Have the actor add a partition under the key that has four labelers
		 * corresponding to the functions above and a simple filter based on
		 * TestAttributeId.BOOLEAN_0.
		 */
		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(1, (c) -> {
			PartitionsDataManager partitionsDataManager = c.getDataManager(PartitionsDataManager.class);

			/*
			 * Create a partition that may filter about half of the population
			 * on BOOLEAN_0. We will partition on INT_0, INT_1, DOUBLE_0 and
			 * DOUBLE_1. Note that we do not use BOOLEAN_1 as part of the
			 * partition.
			 */
			Partition.Builder partitionBuilder = Partition.builder();

			partitionBuilder.setFilter(new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true));//

			partitionBuilder//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_0, int_0_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.INT_1, int_1_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_0, double_0_labelFunction))//
							.addLabeler(new FunctionalAttributeLabeler(TestAttributeId.DOUBLE_1, double_1_labelFunction));

			Partition partition = partitionBuilder.build();
			partitionsDataManager.addPartition(partition, key);
		}));

		// Have the actor get person values using a labeling function and show
		// that the result of the function matches expectations.
		testPluginDataBuilder.addTestActorPlan("actor", new TestActorPlan(2, (c) -> {

			// establish data views
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			AttributesDataManager attributesDataManager = c.getDataManager(AttributesDataManager.class);
			PartitionsDataManager partitionsDataManager = c.getDataManager(PartitionsDataManager.class);

			for (PersonId personId : peopleDataManager.getPeople()) {

				
				
				LabelSetFunction<Integer> f = (pc, labelset) -> {
					Integer i = (Integer) labelset.getLabel(TestAttributeId.INT_0).get();
					Boolean b1 = (Boolean) labelset.getLabel(TestAttributeId.INT_1).get();
					String s = (String) labelset.getLabel(TestAttributeId.DOUBLE_0).get();
					Boolean b2 = (Boolean) labelset.getLabel(TestAttributeId.DOUBLE_1).get();
					int result = i;
					if (b1) {
						result += 20;
					}
					switch (s) {
					case "A":
						result *= 2;
						break;
					case "B":
						result *= 3;
						break;
					default:
						result *= 4;
						break;
					}
					if (b2) {
						result += 17;
					}										
					return result;
				};
				

				Optional<Integer> optional = partitionsDataManager.getPersonValue(key, f, personId);

				// the person should be in the partition if and only if the
				// optional is present
				Boolean expectedInclusion = attributesDataManager.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
				assertEquals(expectedInclusion, optional.isPresent());

				if (optional.isPresent()) {
					// determine the expected value of the function
					
					//first, get the attribute values of the person
					int i0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_0);
					int i1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.INT_1);
					double d0 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
					double d1 = attributesDataManager.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
					
					//now determine what the labelers will do with those values
					int i = (Integer)int_0_labelFunction.apply(i0);
					boolean b1 = (Boolean)int_1_labelFunction.apply(i1);
					String s = (String)double_0_labelFunction.apply(d0);
					boolean b2 = (Boolean)double_1_labelFunction.apply(d1);
					
					//finally calculate what the label function will do with the label values
					int expectedValue = i;
					if (b1) {
						expectedValue += 20;
					}
					switch (s) {
					case "A":
						expectedValue *= 2;
						break;
					case "B":
						expectedValue *= 3;
						break;
					default:
						expectedValue *= 4;
						break;
					}
					if (b2) {
						expectedValue += 17;
					}

					// show the values are equal
					assertEquals(expectedValue, optional.get().intValue());
				}

			}
		}));

		TestPluginData testPluginData = testPluginDataBuilder.build();

		Factory factory = PartitionsTestPluginFactory.factory(1000, seed, testPluginData);

		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}
}
