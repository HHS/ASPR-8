package plugins.groups.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.groups.GroupCreationObservationEvent;
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupCreationObservationEvent.class)
public class AT_GroupCreationObservationEvent {


	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(46);
		GroupCreationObservationEvent groupCreationObservationEvent = new GroupCreationObservationEvent(groupId);
		assertEquals(groupId, groupCreationObservationEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		GroupId groupId = new GroupId(46);
		GroupCreationObservationEvent groupCreationObservationEvent = new GroupCreationObservationEvent(groupId);
		assertEquals(GroupCreationObservationEvent.class, groupCreationObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { SimulationContext.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 296314827017119408L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			
			Set<EventLabel<GroupCreationObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupCreationObservationEvent> eventLabel = GroupCreationObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupCreationObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupCreationObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupCreationObservationEvent> eventLabeler = GroupCreationObservationEvent.getEventLabelerForGroupType(groupDataManager);
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupCreationObservationEvent> eventLabel2 = GroupCreationObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupCreationObservationEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupCreationObservationEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4044175875975004087L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupCreationObservationEvent> eventLabeler = GroupCreationObservationEvent.getEventLabelerForGroupType(groupDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupCreationObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupCreationObservationEvent> expectedEventLabel = GroupCreationObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);

				// create an event
				GroupCreationObservationEvent event = new GroupCreationObservationEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = {})
	public void testGetEventLabelByAll() {

		GroupsActionSupport.testConsumer(0, 3, 5, 2339912574580459526L, (c) -> {

			Set<EventLabel<GroupCreationObservationEvent>> eventLabels = new LinkedHashSet<>();

			EventLabel<GroupCreationObservationEvent> eventLabel = GroupCreationObservationEvent.getEventLabelByAll();

			// show that the event label has the correct event class
			assertEquals(GroupCreationObservationEvent.class, eventLabel.getEventClass());

			// show that the event label has the correct primary key
			assertEquals(GroupCreationObservationEvent.class, eventLabel.getPrimaryKeyValue());

			// show that the event label has the same id as its
			// associated labeler
			EventLabeler<GroupCreationObservationEvent> eventLabeler = GroupCreationObservationEvent.getEventLabelerForAll();
			assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

			// show that two event labels with the same inputs are equal
			EventLabel<GroupCreationObservationEvent> eventLabel2 = GroupCreationObservationEvent.getEventLabelByAll();
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

		GroupsActionSupport.testConsumer(0, 3, 5, 8218909509504210133L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupCreationObservationEvent> eventLabeler = GroupCreationObservationEvent.getEventLabelerForAll();

			// show that the event labeler has the correct event class
			assertEquals(GroupCreationObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupCreationObservationEvent> expectedEventLabel = GroupCreationObservationEvent.getEventLabelByAll();

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);

				// create an event
				GroupCreationObservationEvent event = new GroupCreationObservationEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupCreationObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});
	}
}
