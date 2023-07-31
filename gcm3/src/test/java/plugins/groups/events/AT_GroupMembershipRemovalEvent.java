package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupMembershipRemovalEvent.class)
public class AT_GroupMembershipRemovalEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		PersonId personId = new PersonId(12);
		GroupId groupId = new GroupId(23);
		GroupMembershipRemovalEvent GroupMembershipRemovalEvent = new GroupMembershipRemovalEvent(personId, groupId);
		assertEquals(groupId, GroupMembershipRemovalEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(12);
		GroupId groupId = new GroupId(23);
		GroupMembershipRemovalEvent GroupMembershipRemovalEvent = new GroupMembershipRemovalEvent(personId, groupId);
		assertEquals(personId, GroupMembershipRemovalEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupAndPerson", args = { Context.class, GroupId.class, PersonId.class })
	public void testGetEventLabelByGroupAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 298549072627101248L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			Set<EventLabel<GroupMembershipRemovalEvent>> eventLabels = new LinkedHashSet<>();

			PersonId personId = new PersonId(0);

			for (GroupId groupId : groupIds) {

				EventLabel<GroupMembershipRemovalEvent> eventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, groupId, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupAndPerson();
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalEvent> eventLabel2 = GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, groupId, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, null, personId));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, new GroupId(100000), personId));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, new GroupId(0), null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, new GroupId(0), new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupAndPerson", args = {})
	public void testGetEventLabelerForGroupAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 4452567174321509486L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupAndPerson();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalEvent> expectedEventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupAndPerson(c, groupId, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				GroupMembershipRemovalEvent event = new GroupMembershipRemovalEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroup", args = { Context.class, GroupId.class })
	public void testGetEventLabelByGroup() {

		GroupsActionSupport.testConsumer(0, 3, 5, 8484038291544974628L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			Set<EventLabel<GroupMembershipRemovalEvent>> eventLabels = new LinkedHashSet<>();
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				

				testGroupTypeId = testGroupTypeId.next();

				EventLabel<GroupMembershipRemovalEvent> eventLabel = GroupMembershipRemovalEvent.getEventLabelByGroup(c, groupId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroup();
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalEvent> eventLabel2 = GroupMembershipRemovalEvent.getEventLabelByGroup(c, groupId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroup(c, null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroup(c, new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroup", args = {})
	public void testGetEventLabelerForGroup() {

		GroupsActionSupport.testConsumer(10, 3, 5, 3313438051476160164L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// create an event labeler
			EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroup();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalEvent> expectedEventLabel = GroupMembershipRemovalEvent.getEventLabelByGroup(c, groupId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				PersonId personId = new PersonId(0);
				GroupMembershipRemovalEvent event = new GroupMembershipRemovalEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPerson", args = { Context.class, PersonId.class })
	public void testGetEventLabelByPerson() {

		GroupsActionSupport.testConsumer(10, 3, 5, 5181120908681821960L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			Set<EventLabel<GroupMembershipRemovalEvent>> eventLabels = new LinkedHashSet<>();

			for (PersonId personId : people) {

				EventLabel<GroupMembershipRemovalEvent> eventLabel = GroupMembershipRemovalEvent.getEventLabelByPerson(c, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForPerson();
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalEvent> eventLabel2 = GroupMembershipRemovalEvent.getEventLabelByPerson(c, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByPerson(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByPerson(c, new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPerson", args = {})
	public void testGetEventLabelerForPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 7591006487215638552L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForPerson();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalEvent> expectedEventLabel = GroupMembershipRemovalEvent.getEventLabelByPerson(c, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				GroupMembershipRemovalEvent event = new GroupMembershipRemovalEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupTypeAndPerson", args = { Context.class, GroupTypeId.class, PersonId.class })
	public void testGetEventLabelByGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(10, 3, 5, 2396297410749360025L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			Set<EventLabel<GroupMembershipRemovalEvent>> eventLabels = new LinkedHashSet<>();

			PersonId personId = new PersonId(0);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupMembershipRemovalEvent> eventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, testGroupTypeId, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupTypeAndPerson(groupsDataManager);
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalEvent> eventLabel2 = GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, testGroupTypeId, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
			
			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class,
					() -> GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.GROUP_TYPE_1, new PersonId(1000000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());


		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupTypeAndPerson", args = {})
	public void testGetEventLabelerForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 944196534930517005L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			
			List<GroupId> groupIds = groupsDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupTypeAndPerson(groupsDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				GroupTypeId groupTypeId = groupsDataManager.getGroupType(groupId);

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalEvent> expectedEventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupTypeAndPerson(c, groupTypeId, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				GroupMembershipRemovalEvent event = new GroupMembershipRemovalEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { Context.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4360946626249599442L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			Set<EventLabel<GroupMembershipRemovalEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupMembershipRemovalEvent> eventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupType(groupsDataManager);
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalEvent> eventLabel2 = GroupMembershipRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {

		GroupsActionSupport.testConsumer(10, 3, 5, 825213654032168954L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// create an event labeler
			EventLabeler<GroupMembershipRemovalEvent> eventLabeler = GroupMembershipRemovalEvent.getEventLabelerForGroupType(groupsDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalEvent> expectedEventLabel = GroupMembershipRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);

				PersonId personId = new PersonId(0);
				// create an event
				GroupMembershipRemovalEvent event = new GroupMembershipRemovalEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}
}