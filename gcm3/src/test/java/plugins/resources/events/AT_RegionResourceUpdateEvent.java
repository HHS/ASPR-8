package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.util.ContractException;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.datamanagers.ResourceDataManager;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RegionResourceChangeObservationEvent.class)
public class AT_RegionResourceChangeObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { RegionId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceChangeObservationEvent regionResourceChangeObservationEvent = new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, regionResourceChangeObservationEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceChangeObservationEvent regionResourceChangeObservationEvent = new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(regionId, regionResourceChangeObservationEvent.getRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceChangeObservationEvent regionResourceChangeObservationEvent = new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, regionResourceChangeObservationEvent.getPreviousResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		RegionId regionId = TestRegionId.REGION_4;
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		RegionResourceChangeObservationEvent regionResourceChangeObservationEvent = new RegionResourceChangeObservationEvent(regionId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, regionResourceChangeObservationEvent.getCurrentResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByRegionAndResource", args = { Context.class, RegionId.class, ResourceId.class })
	public void testGetEventLabelByRegionAndResource() {
		ResourcesActionSupport.testConsumer(10, 7912737444879496875L, (c) -> {

			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ResourceDataManager resourceDataManager = c.getDataManager(ResourceDataManager.class).get();
			Set<RegionId> regionIds = regionDataManager.getRegionIds();
			Set<ResourceId> resourceIds = resourceDataManager.getResourceIds();

			Set<EventLabel<RegionResourceChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (RegionId regionId : regionIds) {
				for (ResourceId resourceId : resourceIds) {

					EventLabel<RegionResourceChangeObservationEvent> eventLabel = RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);

					// show that the event label has the correct event class
					assertEquals(RegionResourceChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(resourceId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<RegionResourceChangeObservationEvent> eventLabeler = RegionResourceChangeObservationEvent.getEventLabelerForRegionAndResource();
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<RegionResourceChangeObservationEvent> eventLabel2 = RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}

			// precondition tests

			// if the region id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, null, TestResourceId.RESOURCE_1));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class,
					() -> RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, TestRegionId.REGION_5, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, TestRegionId.REGION_5, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForRegionAndResource", args = {})
	public void testGetEventLabelerForRegionAndResource() {
		ResourcesActionSupport.testConsumer(30, 5829392632134617932L, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			ResourceDataManager resourceDataManager = c.getDataManager(ResourceDataManager.class).get();
			Set<RegionId> regionIds = regionDataManager.getRegionIds();
			Set<ResourceId> resourceIds = resourceDataManager.getResourceIds();

			// create an event labeler
			EventLabeler<RegionResourceChangeObservationEvent> eventLabeler = RegionResourceChangeObservationEvent.getEventLabelerForRegionAndResource();

			// show that the event labeler has the correct event class
			assertEquals(RegionResourceChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (RegionId regionId : regionIds) {

				for (ResourceId resourceId : resourceIds) {

					// derive the expected event label for this event
					EventLabel<RegionResourceChangeObservationEvent> expectedEventLabel = RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);

					// show that the event label and event labeler have
					// equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					RegionResourceChangeObservationEvent event = new RegionResourceChangeObservationEvent(regionId, resourceId, 10L, 30L);

					// show that the event labeler produces the correct
					// event
					// label
					EventLabel<RegionResourceChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			RegionId regionId = TestRegionId.REGION_4;
			long previousResourceLevel = 45L;
			long currentResourceLevel = 398L;
			RegionResourceChangeObservationEvent regionResourceChangeObservationEvent = new RegionResourceChangeObservationEvent(regionId, testResourceId, previousResourceLevel, currentResourceLevel);
			assertEquals(testResourceId, regionResourceChangeObservationEvent.getPrimaryKeyValue());
		}
	}

}
