package plugins.regions.datamanagers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonConstructionData;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.RegionsActionSupport;
import plugins.regions.testsupport.TestRegionId;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonRegionChangeObservationEvent.class)
public class AT_PersonRegionChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, RegionId.class, RegionId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertNotNull(event);
	}

	@Test
	@UnitTestMethod(name = "getCurrentRegionId", args = {})
	public void testGetCurrentRegionId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(currentRegionId, event.getCurrentRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousRegionId", args = {})
	public void testGetPreviousRegionId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(previousRegionId, event.getPreviousRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(personId, event.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);

		String actualValue = event.toString();
		String expectedValue = "PersonRegionChangeObservationEvent [personId=456, previousRegionId=REGION_1, currentRegionId=REGION_2]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByArrivalRegion", args = { Context.class, RegionId.class })
	public void testGetEventLabelByArrivalRegion() {
		RegionsActionSupport.testConsumer(0, 1834330681874393158L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, testRegionId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByDepartureRegion", args = { Context.class, RegionId.class })
	public void testGetEventLabelByDepartureRegion() {

		RegionsActionSupport.testConsumer(0, 1200361918333577992L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, testRegionId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPerson", args = { Context.class, PersonId.class })
	public void getEventLabelByPerson() {

		RegionsActionSupport.testConsumer(0, 3991680549375011891L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
				PersonConstructionData personConstructionData = PersonConstructionData.builder().add(testRegionId).build();
				PersonId personId = personDataManager.addPerson(personConstructionData);

				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForPerson().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForArrivalRegion", args = {})
	public void testGetEventLabelerForArrivalRegion() {
		RegionsActionSupport.testConsumer(0, 6696076014058054790L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, testRegionId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(new PersonId(0), TestRegionId.REGION_1, TestRegionId.REGION_2);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, TestRegionId.REGION_2);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForDepartureRegion", args = {})
	public void testGetEventLabelerForDepartureRegion() {

		RegionsActionSupport.testConsumer(0, 5507742922324601760L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, testRegionId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(new PersonId(0), TestRegionId.REGION_1, TestRegionId.REGION_2);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, TestRegionId.REGION_1);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPerson", args = {})
	public void testGetEventLabelerForPerson() {

		RegionsActionSupport.testConsumer(0, 6570752422605720457L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForPerson();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			PersonId personId = personDataManager.addPerson(PersonConstructionData.builder().add(TestRegionId.REGION_1).build());

			for (TestRegionId regionId : TestRegionId.values()) {
				TestRegionId nextRegionId = regionId.next();

				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, regionId, nextRegionId);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		PersonRegionChangeObservationEvent personRegionChangeObservationEvent = new PersonRegionChangeObservationEvent(new PersonId(12), TestRegionId.REGION_2, TestRegionId.REGION_4);
		assertEquals(PersonRegionChangeObservationEvent.class, personRegionChangeObservationEvent.getPrimaryKeyValue());
	}

}
