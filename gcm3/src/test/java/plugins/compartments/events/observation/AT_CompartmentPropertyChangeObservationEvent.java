package plugins.compartments.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.CompartmentsActionSupport;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.compartments.testsupport.TestCompartmentPropertyId;
import plugins.properties.support.TimeTrackingPolicy;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = CompartmentPropertyChangeObservationEvent.class)
public class AT_CompartmentPropertyChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { CompartmentId.class, CompartmentPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_1;
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, previousValue, currentValue);
		assertNotNull(event);
	}

	@Test
	@UnitTestMethod(name = "getCompartmentId", args = {})
	public void tetGetCompartmentId() {
		CompartmentId expectedCompartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_1;
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(expectedCompartmentId, compartmentPropertyId, previousValue, currentValue);
		CompartmentId actualCompartmentId = event.getCompartmentId();
		assertEquals(expectedCompartmentId, actualCompartmentId);
	}

	@Test
	@UnitTestMethod(name = "getCompartmentPropertyId", args = {})
	public void testGetCompartmentPropertyId() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId expectedCompartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_2;
		Object previousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, expectedCompartmentPropertyId, previousValue, currentValue);
		CompartmentPropertyId actualCompartmentPropertyId = event.getCompartmentPropertyId();
		assertEquals(expectedCompartmentPropertyId, actualCompartmentPropertyId);
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_1;
		Object previousValue = 5;
		Object expectedCurrentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, previousValue, expectedCurrentValue);
		Object actualCurrentPropertyValue = event.getCurrentPropertyValue();
		assertEquals(expectedCurrentValue, actualCurrentPropertyValue);
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_1;
		Object expectedPreviousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, expectedPreviousValue, currentValue);
		Object actualPreviousPropertyValue = event.getPreviousPropertyValue();
		assertEquals(expectedPreviousValue, actualPreviousPropertyValue);
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
				Object previousValue = 5;
				Object currentValue = 6;
				CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(testCompartmentId, compartmentPropertyId, previousValue, currentValue);
				assertEquals(compartmentPropertyId, event.getPrimaryKeyValue());
			}
		}
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentPropertyId.COMPARTMENT_PROPERTY_2_1;
		Object expectedPreviousValue = 5;
		Object currentValue = 6;
		CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(compartmentId, compartmentPropertyId, expectedPreviousValue, currentValue);
		String actualValue = event.toString();
		String expectedValue = "CompartmentPropertyChangeObservationEvent [compartmentId=COMPARTMENT_2, compartmentPropertyId=COMPARTMENT_PROPERTY_2_1, previousPropertyValue=5, currentPropertyValue=6]";
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = { Context.class, CompartmentId.class, CompartmentPropertyId.class })
	public void testGetEventLabel() {

		CompartmentsActionSupport.testConsumer(0, 1925120766573695456L, TimeTrackingPolicy.TRACK_TIME, (c) -> {
			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
					EventLabel<CompartmentPropertyChangeObservationEvent> eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId);
					assertEquals(CompartmentPropertyChangeObservationEvent.class, eventLabel.getEventClass());
					assertEquals(compartmentPropertyId, eventLabel.getPrimaryKeyValue());
					assertEquals(CompartmentPropertyChangeObservationEvent.getEventLabeler().getId(), eventLabel.getLabelerId());
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {

		CompartmentsActionSupport.testConsumer(0, 628042077827535235L, TimeTrackingPolicy.DO_NOT_TRACK_TIME, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<CompartmentPropertyChangeObservationEvent> eventLabeler = CompartmentPropertyChangeObservationEvent.getEventLabeler();
			assertEquals(CompartmentPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (CompartmentPropertyId compartmentPropertyId : TestCompartmentPropertyId.getTestCompartmentPropertyIds(testCompartmentId)) {
					assertEquals(CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					Object previousPropertyValue = 5;
					Object currentPropertyValue = 6;
					CompartmentPropertyChangeObservationEvent event = new CompartmentPropertyChangeObservationEvent(testCompartmentId, compartmentPropertyId, previousPropertyValue,
							currentPropertyValue);

					// derive the expected event label for this event
					EventLabel<CompartmentPropertyChangeObservationEvent> expectedEventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(c, testCompartmentId, compartmentPropertyId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<CompartmentPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);
				}
			}

		});

	}

}
