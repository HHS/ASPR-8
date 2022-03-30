package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;

@UnitTest(target = GroupMembershipRemovalObservationEvent.class)
public class AT_GroupMembershipRemovalObservationEvent {

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
		GroupMembershipRemovalObservationEvent GroupMembershipRemovalObservationEvent = new GroupMembershipRemovalObservationEvent(personId, groupId);
		assertEquals(groupId, GroupMembershipRemovalObservationEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(12);
		GroupId groupId = new GroupId(23);
		GroupMembershipRemovalObservationEvent GroupMembershipRemovalObservationEvent = new GroupMembershipRemovalObservationEvent(personId, groupId);
		assertEquals(personId, GroupMembershipRemovalObservationEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupAndPerson", args = { Context.class, GroupId.class, PersonId.class })
	public void testGetEventLabelByGroupAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 298549072627101248L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();

			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			PersonId personId = new PersonId(0);

			for (GroupId groupId : groupIds) {

				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, groupId, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupAndPerson();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, groupId, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, null, personId));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, new GroupId(100000), personId));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, new GroupId(0), null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the group id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, new GroupId(0), new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupAndPerson", args = {})
	public void testGetEventLabelerForGroupAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 4452567174321509486L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupAndPerson();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(c, groupId, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroup", args = { Context.class, GroupId.class })
	public void testGetEventLabelByGroup() {

		GroupsActionSupport.testConsumer(0, 3, 5, 8484038291544974628L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				

				testGroupTypeId = testGroupTypeId.next();

				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroup(c, groupId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroup();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByGroup(c, groupId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroup(c, null));
			assertEquals(GroupError.NULL_GROUP_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroup(c, new GroupId(10000)));
			assertEquals(GroupError.UNKNOWN_GROUP_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroup", args = {})
	public void testGetEventLabelerForGroup() {

		GroupsActionSupport.testConsumer(10, 3, 5, 3313438051476160164L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroup();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0; i < 10; i++) {
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroup(c, groupId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				PersonId personId = new PersonId(0);
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPerson", args = { Context.class, PersonId.class })
	public void testGetEventLabelByPerson() {

		GroupsActionSupport.testConsumer(10, 3, 5, 5181120908681821960L, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();
			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (PersonId personId : people) {

				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByPerson(c, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForPerson();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByPerson(c, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByPerson(c, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByPerson(c, new PersonId(10000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPerson", args = {})
	public void testGetEventLabelerForPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 7591006487215638552L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForPerson();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByPerson(c, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupTypeAndPerson", args = { Context.class, GroupTypeId.class, PersonId.class })
	public void testGetEventLabelByGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(10, 3, 5, 2396297410749360025L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			PersonId personId = new PersonId(0);
			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, testGroupTypeId, personId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupTypeAndPerson(groupDataManager);
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, testGroupTypeId, personId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, null, new PersonId(0)));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class,
					() -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.getUnknownGroupTypeId(), new PersonId(0)));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());
			
			// if the person id is null
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.GROUP_TYPE_1, null));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class,
					() -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, TestGroupTypeId.GROUP_TYPE_1, new PersonId(1000000)));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());


		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupTypeAndPerson", args = {})
	public void testGetEventLabelerForGroupTypeAndPerson() {

		GroupsActionSupport.testConsumer(30, 3, 5, 944196534930517005L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			
			List<GroupId> groupIds = groupDataManager.getGroupIds();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupTypeAndPerson(groupDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			PersonId personId = new PersonId(0);
			for (GroupId groupId : groupIds) {

				GroupTypeId groupTypeId = groupDataManager.getGroupType(groupId);

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(c, groupTypeId, personId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { Context.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4360946626249599442L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupType(groupDataManager);
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {

		GroupsActionSupport.testConsumer(10, 3, 5, 825213654032168954L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForGroupType(groupDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);

				PersonId personId = new PersonId(0);
				// create an event
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = {})
	public void testGetEventLabelByAll() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4764889447042956281L, (c) -> {

			Set<EventLabel<GroupMembershipRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByAll();

			// show that the event label has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getEventClass());

			// show that the event label has the correct primary key
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

			// show that the event label has the same id as its
			// associated labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForAll();
			assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

			// show that two event labels with the same inputs are equal
			EventLabel<GroupMembershipRemovalObservationEvent> eventLabel2 = GroupMembershipRemovalObservationEvent.getEventLabelByAll();
			assertEquals(eventLabel, eventLabel2);

			// show that equal event labels have equal hash codes
			assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

			// show that two event labels with different inputs are not
			// equal
			assertTrue(eventLabels.add(eventLabel));

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {

		GroupsActionSupport.testConsumer(100, 3, 5, 7811446012558625127L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupMembershipRemovalObservationEvent> eventLabeler = GroupMembershipRemovalObservationEvent.getEventLabelerForAll();

			// show that the event labeler has the correct event class
			assertEquals(GroupMembershipRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupMembershipRemovalObservationEvent> expectedEventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByAll();

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);

				// create an event
				PersonId personId = new PersonId(0);
				GroupMembershipRemovalObservationEvent event = new GroupMembershipRemovalObservationEvent(personId, groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupMembershipRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}
}
