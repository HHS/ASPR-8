package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.StochasticsDataManager;

@UnitTest(target = ResourcePropertyChangeObservationEvent.class)

public class AT_ResourcePropertyChangeObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, ResourcePropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent = new ResourcePropertyChangeObservationEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(resourceId, resourcePropertyChangeObservationEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getResourcePropertyId", args = {})
	public void testGetResourcePropertyId() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent = new ResourcePropertyChangeObservationEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(resourcePropertyId, resourcePropertyChangeObservationEvent.getResourcePropertyId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent = new ResourcePropertyChangeObservationEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(previousValue, resourcePropertyChangeObservationEvent.getPreviousPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		Object previousValue = "previous";
		Object currentValue = "current";
		ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent = new ResourcePropertyChangeObservationEvent(resourceId, resourcePropertyId, previousValue, currentValue);
		assertEquals(currentValue, resourcePropertyChangeObservationEvent.getCurrentPropertyValue());
	}

	@Test
	@UnitTestMethod(name = "getEventLabel", args = { Context.class, ResourceId.class, ResourcePropertyId.class })
	public void testGetEventLabel() {
		ResourcesActionSupport.testConsumer(10, 7912737444879496875L, (c) -> {

			Set<EventLabel<ResourcePropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				ResourceId resourceId = testResourcePropertyId.getTestResourceId();

				EventLabel<ResourcePropertyChangeObservationEvent> eventLabel = ResourcePropertyChangeObservationEvent.getEventLabel(c, resourceId, testResourcePropertyId);

				// show that the event label has the correct event class
				assertEquals(ResourcePropertyChangeObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(testResourcePropertyId, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<ResourcePropertyChangeObservationEvent> eventLabeler = ResourcePropertyChangeObservationEvent.getEventLabeler();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<ResourcePropertyChangeObservationEvent> eventLabel2 = ResourcePropertyChangeObservationEvent.getEventLabel(c, resourceId, testResourcePropertyId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> ResourcePropertyChangeObservationEvent.getEventLabel(c, null, TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> ResourcePropertyChangeObservationEvent.getEventLabel(c, TestResourceId.getUnknownResourceId(), TestResourcePropertyId.ResourceProperty_1_3_DOUBLE_MUTABLE));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

			// if the resource property id is null
			contractException = assertThrows(ContractException.class,
					() -> ResourcePropertyChangeObservationEvent.getEventLabel(c, TestResourceId.RESOURCE_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_PROPERTY_ID, contractException.getErrorType());

			// if the resource property id is unknown
			contractException = assertThrows(ContractException.class,
					() -> ResourcePropertyChangeObservationEvent.getEventLabel(c, TestResourceId.RESOURCE_1, TestResourcePropertyId.getUnknownResourcePropertyId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_PROPERTY_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabeler", args = {})
	public void testGetEventLabeler() {
		ResourcesActionSupport.testConsumer(30, 5829392632134617932L, (c) -> {
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class).get();
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create an event labeler
			EventLabeler<ResourcePropertyChangeObservationEvent> eventLabeler = ResourcePropertyChangeObservationEvent.getEventLabeler();

			// show that the event labeler has the correct event class
			assertEquals(ResourcePropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				ResourceId resourceId = testResourcePropertyId.getTestResourceId();

				// derive the expected event label for this event
				EventLabel<ResourcePropertyChangeObservationEvent> expectedEventLabel = ResourcePropertyChangeObservationEvent.getEventLabel(c, resourceId, testResourcePropertyId);

				/*
				 * show that the event label and event labeler have equal id
				 * values
				 */
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

				// create an event
				Object previousValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
				Object currentValue = testResourcePropertyId.getRandomPropertyValue(randomGenerator);
				ResourcePropertyChangeObservationEvent event = new ResourcePropertyChangeObservationEvent(resourceId, testResourcePropertyId, previousValue, currentValue);

				/*
				 * show that the event labeler produces the correct event label
				 */
				EventLabel<ResourcePropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			ResourceId resourceId = testResourcePropertyId.getTestResourceId();
			Object previousValue = "previous";
			Object currentValue = "current";
			ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent = new ResourcePropertyChangeObservationEvent(resourceId, testResourcePropertyId, previousValue, currentValue);
			assertEquals(testResourcePropertyId, resourcePropertyChangeObservationEvent.getPrimaryKeyValue());
		}
	}

}
