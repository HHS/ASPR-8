package gov.hhs.aspr.ms.gcm.simulation.plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ActorContext;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.Event;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.testsupport.testplugin.TestSimulation;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.datamanagers.GroupsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.events.GroupMembershipRemovalEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.GroupsTestPluginFactory.Factory;
import gov.hhs.aspr.ms.gcm.simulation.plugins.groups.testsupport.TestGroupTypeId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.LabelerSensitivity;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.support.PartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.partitions.testsupport.TestPartitionsContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.datamanagers.PeopleDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.stochastics.datamanagers.StochasticsDataManager;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class AT_GroupLabeler {
	private static class LocalGroupLabeler extends GroupLabeler {
		private final Function<GroupTypeCountMap, Object> labelingFunction;

		public LocalGroupLabeler(Function<GroupTypeCountMap, Object> labelingFunction) {
			this.labelingFunction = labelingFunction;
		}

		@Override
		protected Object getLabelFromGroupTypeCountMap(GroupTypeCountMap groupTypeCountMap) {
			return labelingFunction.apply(groupTypeCountMap);
		}

	}

	@Test
	@UnitTestMethod(target = GroupLabeler.class, name = "getLabelerSensitivities", args = {})
	public void testGetLabelerSensitivities() {

		Set<LabelerSensitivity<?>> labelerSensitivities = new LocalGroupLabeler((g) -> null).getLabelerSensitivities();

		// show that we get back some labeler sensitivities
		assertNotNull(labelerSensitivities);

		// we expect exactly two
		assertEquals(2, labelerSensitivities.size());

		boolean groupMembershipAdditionEventSensitivityFound = false;
		boolean groupMembershipRemovalEventSensitivityFound = false;
		for (LabelerSensitivity<?> labelerSensitivity : labelerSensitivities) {
			if (labelerSensitivity.getEventClass() == GroupMembershipAdditionEvent.class) {
				groupMembershipAdditionEventSensitivityFound = true;
				PersonId personId = new PersonId(45253);

				Optional<PersonId> optional = labelerSensitivity
						.getPersonId(new GroupMembershipAdditionEvent(personId, new GroupId(56)));
				assertTrue(optional.isPresent());
				PersonId actualPersonId = optional.get();
				assertEquals(personId, actualPersonId);

			} else if (labelerSensitivity.getEventClass() == GroupMembershipRemovalEvent.class) {
				groupMembershipRemovalEventSensitivityFound = true;
				PersonId personId = new PersonId(45253);

				Optional<PersonId> optional = labelerSensitivity
						.getPersonId(new GroupMembershipRemovalEvent(personId, new GroupId(56)));
				assertTrue(optional.isPresent());
				PersonId actualPersonId = optional.get();
				assertEquals(personId, actualPersonId);

			} else {
				fail("unknown labeler sensitivity");
			}
		}

		// show that we found both labeler sensitivities
		assertTrue(groupMembershipAdditionEventSensitivityFound);
		assertTrue(groupMembershipRemovalEventSensitivityFound);

	}

	@Test
	@UnitTestMethod(target = GroupLabeler.class, name = "getCurrentLabel", args = { PartitionsContext.class,
			PersonId.class })
	public void testGetCurrentLabel() {

		Consumer<ActorContext> consumer = (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			Function<GroupTypeCountMap, Object> func = (g) -> {
				int result = 0;
				for (GroupTypeId groupTypeId : g.getGroupTypeIds()) {
					TestGroupTypeId testGroupTypeId = (TestGroupTypeId) groupTypeId;
					result += (testGroupTypeId.ordinal() + 1) * g.getGroupCount(groupTypeId);
				}
				return result;
			};

			GroupLabeler groupLabeler = new LocalGroupLabeler(func);

			for (PersonId personId : peopleDataManager.getPeople()) {
				GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
				for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
					builder.setCount(groupTypeId,
							groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId));
				}
				GroupTypeCountMap groupTypeCountMap = builder.build();
				Object expectedLabel = func.apply(groupTypeCountMap);
				Object actualLabel = groupLabeler.getCurrentLabel(testPartitionsContext, personId);
				assertEquals(expectedLabel, actualLabel);
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> groupLabeler.getCurrentLabel(testPartitionsContext, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupLabeler.getCurrentLabel(testPartitionsContext, new PersonId(100000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		};

		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 5880749882920317232L, consumer);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();

	}

	@Test
	@UnitTestMethod(target = GroupLabeler.class, name = "getId", args = {})
	public void testGetId() {
		Function<GroupTypeCountMap, Object> f = (g) -> null;
		assertEquals(GroupTypeId.class, new LocalGroupLabeler(f).getId());
	}

	@Test
	@UnitTestMethod(target = GroupLabeler.class, name = "getPastLabel", args = { PartitionsContext.class, Event.class })
	public void testGetPastLabel() {

		Consumer<ActorContext> consumer = (c) -> {

			TestPartitionsContext testPartitionsContext = new TestPartitionsContext(c);

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			int delta;
			Function<GroupTypeCountMap, Object> func = (g) -> {
				int result = 0;
				for (GroupTypeId groupTypeId : g.getGroupTypeIds()) {

					TestGroupTypeId testGroupTypeId = (TestGroupTypeId) groupTypeId;
					result += (testGroupTypeId.ordinal() + 1) * g.getGroupCount(groupTypeId);
				}
				return result;
			};

			GroupLabeler groupLabeler = new LocalGroupLabeler(func);

			// Addition Events
			GroupMembershipAdditionEvent groupMembershipAdditionEvent;
			for (PersonId personId : peopleDataManager.getPeople()) {
				List<GroupId> groupIdsForPersonId = groupsDataManager.getGroupsForPerson(personId);
				int numGroupsForPerson = groupIdsForPersonId.size();
				if (numGroupsForPerson <= 0)
					continue;
				GroupId groupId = groupIdsForPersonId.get(randomGenerator.nextInt(numGroupsForPerson));
				GroupTypeId expectedGroupTypeId = groupsDataManager.getGroupType(groupId);

				groupMembershipAdditionEvent = new GroupMembershipAdditionEvent(personId, groupId);
				delta = -1;
				GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
				for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
					int count = groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
					if (groupTypeId.equals(expectedGroupTypeId)) {
						count += delta;
					}
					builder.setCount(groupTypeId, count);
				}
				GroupTypeCountMap groupTypeCountMap = builder.build();
				Object expectedLabel = func.apply(groupTypeCountMap);
				Object actualLabel = groupLabeler.getPastLabel(testPartitionsContext, groupMembershipAdditionEvent);
				assertEquals(expectedLabel, actualLabel);
			}

			// Removal Events
			GroupMembershipRemovalEvent groupMembershipRemovalEvent;
			for (PersonId personId : peopleDataManager.getPeople()) {
				List<GroupId> groupIdsForPersonId = groupsDataManager.getGroupsForPerson(personId);
				int numGroupsForPerson = groupIdsForPersonId.size();
				if (numGroupsForPerson <= 0)
					continue;
				GroupId groupId = groupIdsForPersonId.get(randomGenerator.nextInt(numGroupsForPerson));
				GroupTypeId expectedGroupTypeId = groupsDataManager.getGroupType(groupId);
				groupsDataManager.removePersonFromGroup(personId, groupId);

				groupMembershipRemovalEvent = new GroupMembershipRemovalEvent(personId, groupId);
				delta = 1;
				GroupTypeCountMap.Builder builder = GroupTypeCountMap.builder();
				for (GroupTypeId groupTypeId : groupsDataManager.getGroupTypeIds()) {
					int count = groupsDataManager.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
					if (groupTypeId.equals(expectedGroupTypeId)) {
						count += delta;
					}
					builder.setCount(groupTypeId, count);
				}
				GroupTypeCountMap groupTypeCountMap = builder.build();
				Object expectedLabel = func.apply(groupTypeCountMap);
				Object actualLabel = groupLabeler.getPastLabel(testPartitionsContext, groupMembershipRemovalEvent);
				assertEquals(expectedLabel, actualLabel);
			}

			GroupId groupId = groupsDataManager.getGroupIds().get(0);

			// precondition: person id is null
			ContractException contractException = assertThrows(ContractException.class, () -> groupLabeler
					.getPastLabel(testPartitionsContext, new GroupMembershipAdditionEvent(null, groupId)));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// precondition: person id is unknown
			contractException = assertThrows(ContractException.class,
					() -> groupLabeler.getPastLabel(testPartitionsContext,
							new GroupMembershipAdditionEvent(new PersonId(100000), groupId)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		};

		Factory factory = GroupsTestPluginFactory.factory(30, 3, 5, 8478102896119863988L, consumer);
		TestSimulation.builder().addPlugins(factory.getPlugins()).build().execute();
	}

	@Test
	@UnitTestMethod(target = GroupLabeler.class, name = "toString", args = {})
	public void testToString() {		
		LocalGroupLabeler localGroupLabeler = new LocalGroupLabeler((g) -> null);
		String actualValue = localGroupLabeler.toString();		
		String expectedValue = "GroupLabeler []";
		assertEquals(expectedValue, actualValue);
	}

}
