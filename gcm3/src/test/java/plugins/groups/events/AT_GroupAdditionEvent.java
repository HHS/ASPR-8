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
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupAdditionEvent.class)
public class AT_GroupAdditionEvent {


	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(46);
		GroupAdditionEvent groupAdditionEvent = new GroupAdditionEvent(groupId);
		assertEquals(groupId, groupAdditionEvent.getGroupId());
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		GroupId groupId = new GroupId(46);
		GroupAdditionEvent groupAdditionEvent = new GroupAdditionEvent(groupId);
		assertEquals(GroupAdditionEvent.class, groupAdditionEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { SimulationContext.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 296314827017119408L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			
			Set<EventLabel<GroupAdditionEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupAdditionEvent> eventLabel = GroupAdditionEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupAdditionEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupAdditionEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupAdditionEvent> eventLabeler = GroupAdditionEvent.getEventLabelerForGroupType(groupsDataManager);
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupAdditionEvent> eventLabel2 = GroupAdditionEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupAdditionEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupAdditionEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {

		GroupsActionSupport.testConsumer(0, 3, 5, 4044175875975004087L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// create an event labeler
			EventLabeler<GroupAdditionEvent> eventLabeler = GroupAdditionEvent.getEventLabelerForGroupType(groupsDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupAdditionEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupAdditionEvent> expectedEventLabel = GroupAdditionEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);

				// create an event
				GroupAdditionEvent event = new GroupAdditionEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupAdditionEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});
	}
}
