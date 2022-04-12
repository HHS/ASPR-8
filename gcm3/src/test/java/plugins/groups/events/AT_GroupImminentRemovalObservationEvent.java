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
import plugins.groups.GroupDataManager;
import plugins.groups.support.GroupError;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.GroupsActionSupport;
import plugins.groups.testsupport.TestGroupTypeId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = GroupImminentRemovalObservationEvent.class)
public class AT_GroupImminentRemovalObservationEvent {

	@Test
	@UnitTestConstructor(args = { GroupId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getGroupId", args = {})
	public void testGetGroupId() {
		GroupId groupId = new GroupId(35);
		GroupImminentRemovalObservationEvent groupImminentRemovalObservationEvent = new GroupImminentRemovalObservationEvent(groupId);
		assertEquals(groupId, groupImminentRemovalObservationEvent.getGroupId());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		GroupId groupId = new GroupId(35);
		GroupImminentRemovalObservationEvent groupImminentRemovalObservationEvent = new GroupImminentRemovalObservationEvent(groupId);
		assertEquals(GroupImminentRemovalObservationEvent.class, groupImminentRemovalObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroup", args = { SimulationContext.class, GroupId.class })
	public void testGetEventLabelByGroup() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 4793492577271802585L, (c) -> {
			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			Set<EventLabel<GroupImminentRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();
			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0;i<10;i++) {
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();
				
				EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroup(c, groupId);

				// show that the event label has the correct event class
				assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForGroup();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupImminentRemovalObservationEvent> eventLabel2 = GroupImminentRemovalObservationEvent.getEventLabelByGroup(c, groupId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});		
		
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroup", args = {})
	public void testGetEventLabelerForGroup() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 2029538624275094851L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForGroup();

			// show that the event labeler has the correct event class
			assertEquals(GroupImminentRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			TestGroupTypeId testGroupTypeId = TestGroupTypeId.GROUP_TYPE_1;
			for (int i = 0;i<10;i++) {
				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				
				testGroupTypeId = testGroupTypeId.next();

				// derive the expected event label for this event
				EventLabel<GroupImminentRemovalObservationEvent> expectedEventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroup(c, groupId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				GroupImminentRemovalObservationEvent event = new GroupImminentRemovalObservationEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupImminentRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});		


	}

	@Test
	@UnitTestMethod(name = "getEventLabelByGroupType", args = { SimulationContext.class, GroupTypeId.class })
	public void testGetEventLabelByGroupType() {

		
		
		GroupsActionSupport.testConsumer(0, 3, 5, 5740881055810962155L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();
			
			Set<EventLabel<GroupImminentRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label has the correct event class
				assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForGroupType(groupDataManager);
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<GroupImminentRemovalObservationEvent> eventLabel2 = GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the group type id is null
			ContractException contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, null));
			assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

			// if the group type id is unknown
			contractException = assertThrows(ContractException.class, () -> GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, TestGroupTypeId.getUnknownGroupTypeId()));
			assertEquals(GroupError.UNKNOWN_GROUP_TYPE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForGroupType", args = {})
	public void testGetEventLabelerForGroupType() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 1065941748199403338L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForGroupType(groupDataManager);

			// show that the event labeler has the correct event class
			assertEquals(GroupImminentRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupImminentRemovalObservationEvent> expectedEventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroupType(c, testGroupTypeId);

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				

				// create an event
				GroupImminentRemovalObservationEvent event = new GroupImminentRemovalObservationEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupImminentRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});		

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = {})
	public void testGetEventLabelByAll() {
		
		GroupsActionSupport.testConsumer(0, 3, 5, 8130798061872293595L, (c) -> {

			Set<EventLabel<GroupImminentRemovalObservationEvent>> eventLabels = new LinkedHashSet<>();

			EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByAll();

			// show that the event label has the correct event class
			assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getEventClass());

			// show that the event label has the correct primary key
			assertEquals(GroupImminentRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());

			// show that the event label has the same id as its
			// associated labeler
			EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForAll();
			assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

			// show that two event labels with the same inputs are equal
			EventLabel<GroupImminentRemovalObservationEvent> eventLabel2 = GroupImminentRemovalObservationEvent.getEventLabelByAll();
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
		
		GroupsActionSupport.testConsumer(0, 3, 5, 8334910225017031504L, (c) -> {

			GroupDataManager groupDataManager = c.getDataManager(GroupDataManager.class).get();

			// create an event labeler
			EventLabeler<GroupImminentRemovalObservationEvent> eventLabeler = GroupImminentRemovalObservationEvent.getEventLabelerForAll();

			// show that the event labeler has the correct event class
			assertEquals(GroupImminentRemovalObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {

				// derive the expected event label for this event
				EventLabel<GroupImminentRemovalObservationEvent> expectedEventLabel = GroupImminentRemovalObservationEvent.getEventLabelByAll();

				// show that the event label and event labeler have equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				GroupId groupId = groupDataManager.addGroup(testGroupTypeId);
				

				// create an event
				GroupImminentRemovalObservationEvent event = new GroupImminentRemovalObservationEvent(groupId);

				// show that the event labeler produces the correct event
				// label
				EventLabel<GroupImminentRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});

	}

}
