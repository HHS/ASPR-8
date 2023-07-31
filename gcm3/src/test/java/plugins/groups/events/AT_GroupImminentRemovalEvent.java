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
import plugins.groups.datamanagers.GroupsDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = GroupImminentRemovalEvent.class)
public class AT_GroupImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(35);
		GroupImminentRemovalEvent groupImminentRemovalEvent = new GroupImminentRemovalEvent(groupId);
		assertEquals(groupId, groupImminentRemovalEvent.getGroupId());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		GroupId groupId = new GroupId(35);
		GroupImminentRemovalEvent groupImminentRemovalEvent = new GroupImminentRemovalEvent(groupId);
		assertEquals(GroupImminentRemovalEvent.class, groupImminentRemovalEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroup", args = { SimulationContext.class, GroupId.class })
	public void testGetEventLabelByGroup() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 4793492577271802585L, (c) -> {
			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			Set<EventLabel<GroupImminentRemovalEvent>> eventLabels = new LinkedHashSet<>();
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0;i<10;i++) {
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();
				
				EventLabel<GroupImminentRemovalEvent> eventLabel = GroupImminentRemovalEvent.getEventLabelByGroup(c, groupId);

				// show that the event label has the correct event class
				assertEquals(GroupImminentRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupImminentRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupImminentRemovalEvent> eventLabeler = GroupImminentRemovalEvent.getEventLabelerForGroup();
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupImminentRemovalEvent> eventLabel2 = GroupImminentRemovalEvent.getEventLabelByGroup(c, groupId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});		
		
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroup", args = {})
	public void testGetEventLabelerForGroup() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 2029538624275094851L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// create an event labeler
			EventLabeler<GroupImminentRemovalEvent> eventLabeler = GroupImminentRemovalEvent.getEventLabelerForGroup();

			// show that the event labeler has the correct event class
			assertEquals(GroupImminentRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0;i<10;i++) {
				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();

				// derive the expected event label for this event
				EventLabel<GroupImminentRemovalEvent> expectedEventLabel = GroupImminentRemovalEvent.getEventLabelByGroup(c, groupId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				GroupImminentRemovalEvent event = new GroupImminentRemovalEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupImminentRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});		


	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { SimulationContext.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		
		
		GroupsActionSupport.testConsumer(0, 3, 5, 5740881055810962155L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);
			
			Set<EventLabel<GroupImminentRemovalEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupImminentRemovalEvent> eventLabel = GroupImminentRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupImminentRemovalEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupImminentRemovalEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupImminentRemovalEvent> eventLabeler = GroupImminentRemovalEvent.getEventLabelerForGroupType(groupsDataManager);
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupImminentRemovalEvent> eventLabel2 = GroupImminentRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 1065941748199403338L, (c) -> {

			GroupsDataManager groupsDataManager = c.getDataManager(GroupsDataManager.class);

			// create an event labeler
			EventLabeler<GroupImminentRemovalEvent> eventLabeler = GroupImminentRemovalEvent.getEventLabelerForGroupType(groupsDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupImminentRemovalEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupImminentRemovalEvent> expectedEventLabel = GroupImminentRemovalEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				GroupId groupId = groupsDataManager.addGroup(testGroupTypeId);
				

				// create an event
				GroupImminentRemovalEvent event = new GroupImminentRemovalEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupImminentRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});		

	}

}