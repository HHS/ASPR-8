package plugins.compartments.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.properties.support.TimeTrackingPolicy;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonCompartmentChangeObservationEvent.class)
public class AT_PersonCompartmentChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, CompartmentId.class, CompartmentId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(456);
		CompartmentId previousCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId currentCompartmentId = TestCompartmentId.COMPARTMENT_2;
		PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, previousCompartmentId, currentCompartmentId);
		assertNotNull(event);
	}

	@Test
	@UnitTestMethod(name = "getCurrentCompartmentId", args = {})
	public void testGetCurrentCompartmentId() {
		PersonId personId = new PersonId(456);
		CompartmentId previousCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId currentCompartmentId = TestCompartmentId.COMPARTMENT_2;
		PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, previousCompartmentId, currentCompartmentId);
		assertEquals(currentCompartmentId, event.getCurrentCompartmentId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousCompartmentId", args = {})
	public void testGetPreviousCompartmentId() {
		PersonId personId = new PersonId(456);
		CompartmentId previousCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId currentCompartmentId = TestCompartmentId.COMPARTMENT_2;
		PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, previousCompartmentId, currentCompartmentId);
		assertEquals(previousCompartmentId, event.getPreviousCompartmentId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(456);
		CompartmentId previousCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId currentCompartmentId = TestCompartmentId.COMPARTMENT_2;
		PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, previousCompartmentId, currentCompartmentId);
		assertEquals(personId, event.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		PersonId personId = new PersonId(456);
		CompartmentId previousCompartmentId = TestCompartmentId.COMPARTMENT_1;
		CompartmentId currentCompartmentId = TestCompartmentId.COMPARTMENT_2;
		PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, previousCompartmentId, currentCompartmentId);

		String actualValue = event.toString();
		String expectedValue = "PersonCompartmentChangeObservationEvent [personId=456, previousCompartmentId=COMPARTMENT_1, currentCompartmentId=COMPARTMENT_2]";
		assertEquals(expectedValue, actualValue);
	}

	
	@Test
	@UnitTestMethod(name = "getEventLabelByArrivalComparment", args = { Context.class, CompartmentId.class })
	public void testGetEventLabelByArrivalComparment() {

		CompartmentsActionSupport.testConsumer(0, 6930404735357267067L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(c, testCompartmentId);
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelerForArrivalCompartment().getId(), eventLabel.getLabelerId());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByDepartureComparment", args = { Context.class, CompartmentId.class })
	public void testGetEventLabelByDepartureComparment() {

		CompartmentsActionSupport.testConsumer(0, 767778471256766524L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByDepartureCompartment(c, testCompartmentId);
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelerForDepartureCompartment().getId(), eventLabel.getLabelerId());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPerson", args = { Context.class, PersonId.class })
	public void getEventLabelByPerson() {

		CompartmentsActionSupport.testConsumer(0, 4525400371240851987L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(testCompartmentId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

				EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByPerson(c, personId);
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelerForPerson().getId(), eventLabel.getLabelerId());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForArrivalCompartment", args = {})
	public void testGetEventLabelerForArrivalCompartment() {

		CompartmentsActionSupport.testConsumer(0, 6754595963957097220L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabeler = PersonCompartmentChangeObservationEvent.getEventLabelerForArrivalCompartment();
			assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(c, testCompartmentId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(new PersonId(0), TestCompartmentId.COMPARTMENT_1, TestCompartmentId.COMPARTMENT_2);

				// derive the expected event label for this event
				EventLabel<PersonCompartmentChangeObservationEvent> expectedEventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(c, TestCompartmentId.COMPARTMENT_2);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonCompartmentChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForDepartureCompartment", args = {})
	public void testGetEventLabelerForDepartureCompartment() {

		CompartmentsActionSupport.testConsumer(0, 6754595963957097220L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabeler = PersonCompartmentChangeObservationEvent.getEventLabelerForDepartureCompartment();
			assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				// show that the labeler and its corresponding event label have
				// the same id
				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelByDepartureCompartment(c, testCompartmentId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(new PersonId(0), TestCompartmentId.COMPARTMENT_1, TestCompartmentId.COMPARTMENT_2);

				// derive the expected event label for this event
				EventLabel<PersonCompartmentChangeObservationEvent> expectedEventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByDepartureCompartment(c,
						TestCompartmentId.COMPARTMENT_1);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonCompartmentChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPerson", args = {})
	public void testGetEventLabelerForPerson() {

		CompartmentsActionSupport.testConsumer(0, 7731934861668712012L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonCompartmentChangeObservationEvent> eventLabeler = PersonCompartmentChangeObservationEvent.getEventLabelerForPerson();
			assertEquals(PersonCompartmentChangeObservationEvent.class, eventLabeler.getEventClass());
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().add(TestCompartmentId.COMPARTMENT_1).build()));
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			for (TestCompartmentId compartmentId : TestCompartmentId.values()) {
				TestCompartmentId nextCompartmentId = compartmentId.next();

				assertEquals(PersonCompartmentChangeObservationEvent.getEventLabelByPerson(c, personId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonCompartmentChangeObservationEvent event = new PersonCompartmentChangeObservationEvent(personId, compartmentId, nextCompartmentId);

				// derive the expected event label for this event
				EventLabel<PersonCompartmentChangeObservationEvent> expectedEventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByPerson(c, personId);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonCompartmentChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});

	}

}
