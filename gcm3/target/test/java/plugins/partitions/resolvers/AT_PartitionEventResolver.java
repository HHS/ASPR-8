package plugins.partitions.resolvers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
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

import nucleus.AgentContext;
import nucleus.DataManagerContext;
import nucleus.SimpleReportId;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import nucleus.testsupport.actionplugin.ReportActionPlan;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.Partition;
import plugins.partitions.testsupport.PartitionsActionSupport;
import plugins.partitions.testsupport.attributes.datacontainers.AttributesDataView;
import plugins.partitions.testsupport.attributes.events.mutation.AttributeValueAssignmentEvent;
import plugins.partitions.testsupport.attributes.support.AttributeFilter;
import plugins.partitions.testsupport.attributes.support.AttributeLabeler;
import plugins.partitions.testsupport.attributes.support.TestAttributeId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.reports.ReportId;
import plugins.stochastics.StochasticsDataManager;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

@UnitTest(target = PartitionEventResolver.class)
public final class AT_PartitionEventResolver {

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
	 * Adds the given number of people to the simulation
	 */
	private static void addPeople(final AgentContext c, final int numberOfPeopleToAdd) {
		for (int i = 0; i < numberOfPeopleToAdd; i++) {
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
		}
	}

	/*
	 * Assigns randomized values for all attributes to all people. Values are
	 * assigned to be consistent with the static labeling functions.
	 */
	private static void assignRandomAttributes(final AgentContext c) {
		final PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
		final StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
		final RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

		for (final PersonId personId : personDataView.getPeople()) {
			
			boolean b = randomGenerator.nextBoolean();
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, b));

			b = randomGenerator.nextBoolean();
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_1, b));

			int i = randomGenerator.nextInt(100);
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_0, i));

			i = randomGenerator.nextInt(100);
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.INT_1, i));

			double d = randomGenerator.nextDouble() * 100;
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_0, d));

			d = randomGenerator.nextDouble() * 100;
			c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.DOUBLE_1, d));

		}
	}

	/*
	 * Creates a map from LabelSet to PersonId that covers all people who have
	 * an attribute value of true for BOOLEAN_0 and false for BOOLEAN_1, to be
	 * consistent with the filter used in the partition addition test. Label
	 * sets consist of labels for INT_0, INT_1, DOUBLE_0 and DOUBLE_1.
	 */
	private static Map<LabelSet, Set<PersonId>> getExpectedStructure(final AgentContext c) {
		final PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
		final AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
		final Map<LabelSet, Set<PersonId>> expectedPeople = new LinkedHashMap<>();
		for (final PersonId personId : personDataView.getPeople()) {

			final Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
			final Boolean b1 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
			if (b0 && !b1) {

				final Integer i0 = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
				final Object label_i0 = INT_0_LABELFUNCTION.apply(i0);

				final Integer i1 = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_1);
				final Object label_i1 = INT_1_LABELFUNCTION.apply(i1);

				final Double d0 = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
				final Object label_d0 = DOUBLE_0_LABELFUNCTION.apply(d0);

				final Double d1 = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
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
	 * Removes the given number of people from the simulation, chosen at random.
	 * Person count may exceed the current population size.
	 */
	private static void removePeople(final AgentContext c, final int numberOfPeopleToRemove) {
		final PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
		final StochasticsDataManager stochasticsDataManager = c.getDataView(StochasticsDataManager.class).get();
		final long seed = stochasticsDataManager.getRandomGenerator().nextLong();
		final Random random = new Random(seed);
		final List<PersonId> people = personDataView.getPeople();
		Collections.shuffle(people, random);
		final int correctedNumberOfPeopleToRemove = FastMath.min(numberOfPeopleToRemove, people.size());
		for (int i = 0; i < correctedNumberOfPeopleToRemove; i++) {
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));
		}
	}

	/*
	 * Compares the expected alignment of people to label sets to the population
	 * partition's content via assertions.
	 */
	private static void showPartitionIsCorrect(final AgentContext c, final Map<LabelSet, Set<PersonId>> expectedPartitionStructure, final Object key) {

		final PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

		// derive the number of people in the expected partition structure
		int expectedPersonCount = 0;
		for (final LabelSet labelSet : expectedPartitionStructure.keySet()) {
			final Set<PersonId> expectedPeople = expectedPartitionStructure.get(labelSet);
			expectedPersonCount += expectedPeople.size();
		}

		// Show that the number of people in the partition matches the expected
		// count of people.
		final int actualPersonCount = partitionDataView.getPersonCount(key);
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
			final List<PersonId> actualpeople = partitionDataView.getPeople(key, labelSet);
			assertEquals(expectedPeople.size(), actualpeople.size());
			assertEquals(expectedPeople, new LinkedHashSet<>(actualpeople));
		}
	}

	//	private void popReport(AgentContext c) {
//		PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
//		AttributesDataView attributesDataView = c.getDataView(AttributesDataView.class).get();
//		System.out.println();
//		for(PersonId personId : personDataView.getPeople()) {
//			Boolean b0 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_0);
//			Boolean b1 = attributesDataView.getAttributeValue(personId, TestAttributeId.BOOLEAN_1);
//			Integer i0 = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_0);
//			Integer i1 = attributesDataView.getAttributeValue(personId, TestAttributeId.INT_1);
//			Double d0 = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_0);
//			Double d1 = attributesDataView.getAttributeValue(personId, TestAttributeId.DOUBLE_1);
//			Object label_i0 = INT_0_LABELFUNCTION.apply(i0);
//			Object label_i1 = INT_1_LABELFUNCTION.apply(i1);
//			Object label_d0 = DOUBLE_0_LABELFUNCTION.apply(d0);
//			Object label_d1 = DOUBLE_1_LABELFUNCTION.apply(d1);
//			
//			
//			StringBuilder sb = new StringBuilder();
//			sb.append(personId);
//			sb.append("\t");
//			sb.append(b0);
//			sb.append("\t");
//			sb.append(b1);
//			sb.append("\t");
//			sb.append(i0);
//			sb.append("\t");
//			sb.append(i1);
//			sb.append("\t");
//			sb.append(d0);
//			sb.append("\t");
//			sb.append(d1);
//			sb.append("\t");
//			sb.append(label_i0);
//			sb.append("\t");
//			sb.append(label_i1);
//			sb.append("\t");
//			sb.append(label_d0);
//			sb.append("\t");
//			sb.append(label_d1);
//
//			System.out.println(sb);
//			
//		}
//		
//		System.out.println();
//	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPartitionAdditionEvent() {

		// Have the simulation initialized with 1000 people. Have an agent
		// execute a partition addition and multiple changes to the population
		// and their attributes to show that the partition resolver maintains
		// the partition.

		PartitionsActionSupport.
		testConsumer(1000, 5127268948453841557L, (c) -> {
			// get the partition data view
			final PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			
			// intitialize people's attributes
			assignRandomAttributes(c);
			
			// create a key to use for a new partition
			final Object key = new Object();

			// show that the population partition does not yet exist
			assertFalse(partitionDataView.partitionExists(key));

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
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// show that the partition was added
			assertTrue(partitionDataView.partitionExists(key));

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
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPartitionDataViewInitialization() {
		PartitionsActionSupport.testConsumer(0, 2954766214498605129L, (c) -> {
			final Optional<PartitionDataView> optional = c.getDataView(PartitionDataView.class);
			assertTrue(optional.isPresent());
		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPartitionRemovalEvent() {
		PartitionsActionSupport.testConsumer(0, 3219096369262553225L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

			// create a key for the population partition
			Object key = new Object();

			// show that the population partition does not exist
			assertFalse(partitionDataView.partitionExists(key));

			// add the partition
			c.resolveEvent(new PartitionAdditionEvent(Partition.builder().build(), key));

			// show that the population partition exists
			assertTrue(partitionDataView.partitionExists(key));

			// remove the population partition
			c.resolveEvent(new PartitionRemovalEvent(key));

			// show that the population partition does not exist
			assertFalse(partitionDataView.partitionExists(key));

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonCreationObservationEvent() {
		PartitionsActionSupport.testConsumer(100, 6964380012813498875L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();

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
			c.resolveEvent(new PartitionAdditionEvent(partition1, key1));

			filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);
			Partition partition2 = Partition.builder().setFilter(filter).build();
			c.resolveEvent(new PartitionAdditionEvent(partition2, key2));

			// add a new person, by default they will have BOOLEAN_0 = false
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().build()));

			// determine the person id of the person just added
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			PersonId personId = personDataView.getLastIssuedPersonId().get();

			// show that the person is not a member of partition 1
			assertFalse(partitionDataView.contains(personId, key1));

			// show that the person is a member of partition 2
			assertTrue(partitionDataView.contains(personId, key2));

		});
	}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testPersonImminentRemovalObservationEvent() {
		
		
		
		final ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		/*
		 * Create a key for a partition of interest that will contain a person
		 * we are about to delete
		 */
		Object key = new Object();

		/*
		 * Add an agent that will create a partition that will contain 10 people
		 * of interest who will be removed later.
		 */
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, (c) -> {

			// select 10 people
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			List<PersonId> people = personDataView.getPeople();
			List<PersonId> peopleOfInterest = new ArrayList<>();
			for (int i = 0; i < 10; i++) {
				peopleOfInterest.add(people.get(i));
			}

			/*
			 * Give these people an attribute BOOLEAN_0 a value of true so they
			 * will be included in the partition
			 */
			for (PersonId personId : peopleOfInterest) {
				c.resolveEvent(new AttributeValueAssignmentEvent(personId, TestAttributeId.BOOLEAN_0, true));
			}

			/*
			 * Create a partition that will include the people of interest
			 */
			Filter filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, true);
			Partition partition = Partition.builder().setFilter(filter).build();
			c.resolveEvent(new PartitionAdditionEvent(partition, key));

			// show that the partition does contain the people of interest
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			List<PersonId> actualPeople = partitionDataView.getPeople(key);
			assertEquals(peopleOfInterest.size(), actualPeople.size());
			assertEquals(new LinkedHashSet<>(peopleOfInterest), new LinkedHashSet<>(actualPeople));

		}));

		/*
		 * Create a report that subscribes to the
		 * PersonImminentRemovalObservationEvent. This will be used to show that
		 * a report or any other observer can still see a person and their
		 * membership in a partition even though the removal of the person is
		 * already underway.
		 * 
		 * The report will record the ids of the people who were in the removal
		 * process
		 */
		List<PersonId> peopleVerifiedByReport = new ArrayList<>();
		ReportId reportId = new SimpleReportId("report");
		pluginBuilder.addReport(reportId);
		pluginBuilder.addReportActionPlan(reportId, new ReportActionPlan(0, (c) -> {
			c.subscribe(PersonImminentRemovalObservationEvent.class, (c2, e) -> {

				PersonId personId = e.getPersonId();

				// show that the person is still in the partition
				PartitionDataView partitionDataView = c2.getDataView(PartitionDataView.class).get();
				assertTrue(partitionDataView.contains(personId, key));

				// add the person to the verified list for later use
				peopleVerifiedByReport.add(personId);

			});
		}));

		/*
		 * Have the agent remove the people who are in the partition from the
		 * simulation. The people will temporarily remain in the simulation and
		 * will only leave when the planning system moves to the next plan.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {

			// Remove from the simulation the people who are in the partition
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			List<PersonId> people = partitionDataView.getPeople(key);
			for (PersonId personId : people) {
				c.resolveEvent(new PersonRemovalRequestEvent(personId));
			}

			// show that the people still exist
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			for (PersonId personId : people) {
				assertTrue(personDataView.personExists(personId));
			}
			List<PersonId> peopleImmediatelyAfterRemoval = partitionDataView.getPeople(key);
			assertEquals(people, peopleImmediatelyAfterRemoval);

		}));

		/*
		 * Have the agent verify that the people are gone and that the partition
		 * no longer contains them. Note that this plan is for the same time as
		 * the plan above but is guaranteed to execute after that plan.
		 */
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(1, (c) -> {
			/*
			 * Show that the report, as an observer of the removals, was able to
			 * observe each removal and still perceived each person as being a
			 * member of the partition.
			 */
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			assertEquals(10, peopleVerifiedByReport.size());

			//show that each of these people is no longer in the simulation
			for (PersonId personId : peopleVerifiedByReport) {
				assertFalse(personDataView.personExists(personId));
			}
			
			//show that the partition is empty
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			assertEquals(0, partitionDataView.getPersonCount(key));

		}));

		// build and add the action plugin to the engine
		final ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		PartitionsActionSupport.testConsumers(100, 6406306513403641718L, actionPluginInitializer);
			}

	@Test
	@UnitTestMethod(name = "init", args = { DataManagerContext.class })
	public void testBulkPersonCreationObservationEvent() {
		PartitionsActionSupport.testConsumer(100, 2561425586247460069L, (c) -> {
			PartitionDataView partitionDataView = c.getDataView(PartitionDataView.class).get();
			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
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
			c.resolveEvent(new PartitionAdditionEvent(partition1, key1));

			filter = new AttributeFilter(TestAttributeId.BOOLEAN_0, Equality.EQUAL, false);
			Partition partition2 = Partition.builder().setFilter(filter).build();
			c.resolveEvent(new PartitionAdditionEvent(partition2, key2));

			// determine the person ids of the people before the bulk addition
			List<PersonId> priorPeople = personDataView.getPeople();

			// add three new people, by default they will have BOOLEAN_0 = false
			PersonContructionData.Builder personBuilder = PersonContructionData.builder();
			BulkPersonContructionData bulkPersonContructionData = BulkPersonContructionData.builder().add(personBuilder.build()).add(personBuilder.build()).add(personBuilder.build()).build();
			c.resolveEvent(new BulkPersonCreationEvent(bulkPersonContructionData));

			// determine the new people who were added
			List<PersonId> newPeople = personDataView.getPeople();
			newPeople.removeAll(priorPeople);

			// show that there are three new people
			assertEquals(3, newPeople.size());

			// show that the new people are not members of partition 1
			for (PersonId personId : newPeople) {
				assertFalse(partitionDataView.contains(personId, key1));
			}
			// show that the new people are members of partition 2
			for (PersonId personId : newPeople) {
				assertTrue(partitionDataView.contains(personId, key2));
			}
		});
	}

}
