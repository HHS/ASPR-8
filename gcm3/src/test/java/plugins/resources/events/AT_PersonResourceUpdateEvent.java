package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.people.datamanagers.PeopleDataManager;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.resources.testsupport.TestResourceId;
import plugins.stochastics.StochasticsDataManager;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;


@UnitTest(target = PersonResourceUpdateEvent.class)
public class AT_PersonResourceUpdateEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, personResourceUpdateEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, personResourceUpdateEvent.getCurrentResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(personId, personResourceUpdateEvent.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		PersonId personId = new PersonId(7645);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long previousResourceLevel = 45L;
		long currentResourceLevel = 398L;
		PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, resourceId, previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, personResourceUpdateEvent.getPreviousResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByRegionAndResource", args = { Context.class, RegionId.class, ResourceId.class })
	public void testGetEventLabelByRegionAndResource() {
		ResourcesActionSupport.testConsumer(10, 7912737444879496875L, (c) -> {

			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Set<RegionId> regionIds = regionsDataManager.getRegionIds();
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			Set<EventLabel<PersonResourceUpdateEvent>> eventLabels = new LinkedHashSet<>();

			for (RegionId regionId : regionIds) {
				for (ResourceId resourceId : resourceIds) {

					EventLabel<PersonResourceUpdateEvent> eventLabel = PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);

					// show that the event label has the correct event class
					assertEquals(PersonResourceUpdateEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(resourceId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForRegionAndResource(regionsDataManager);
					assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonResourceUpdateEvent> eventLabel2 = PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);
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
					() -> PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, null, TestResourceId.RESOURCE_1));
			assertEquals(RegionError.NULL_REGION_ID, contractException.getErrorType());

			// if the region id is unknown
			contractException = assertThrows(ContractException.class,
					() -> PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, TestRegionId.getUnknownRegionId(), TestResourceId.RESOURCE_1));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, TestRegionId.REGION_1, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, TestRegionId.REGION_1, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForRegionAndResource", args = {})
	public void testGetEventLabelerForRegionAndResource() {
		ResourcesActionSupport.testConsumer(30, 5829392632134617932L, (c) -> {
			RegionsDataManager regionsDataManager = c.getDataManager(RegionsDataManager.class);
			
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Set<RegionId> regionIds = regionsDataManager.getRegionIds();
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();
			StochasticsDataManager stochasticsDataManager = c.getDataManager(StochasticsDataManager.class);
			RandomGenerator randomGenerator = stochasticsDataManager.getRandomGenerator();

			// create an event labeler
			EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForRegionAndResource(regionsDataManager);

			// show that the event labeler has the correct event class
			assertEquals(PersonResourceUpdateEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (RegionId regionId : regionIds) {
				List<PersonId> peopleInRegion = regionsDataManager.getPeopleInRegion(regionId);
				if (peopleInRegion.size() > 0) {
					for (ResourceId resourceId : resourceIds) {
						PersonId personId = peopleInRegion.get(randomGenerator.nextInt(peopleInRegion.size()));

						// derive the expected event label for this event
						EventLabel<PersonResourceUpdateEvent> expectedEventLabel = PersonResourceUpdateEvent.getEventLabelByRegionAndResource(c, regionId, resourceId);

						// show that the event label and event labeler have
						// equal id
						// values
						assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

						// create an event
						PersonResourceUpdateEvent event = new PersonResourceUpdateEvent(personId, resourceId, 10L, 30L);

						// show that the event labeler produces the correct
						// event
						// label
						EventLabel<PersonResourceUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

						assertEquals(expectedEventLabel, actualEventLabel);

					}
				}
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPersonAndResource", args = { Context.class, PersonId.class, ResourceId.class })
	public void testGetEventLabelByPersonAndResource() {
		ResourcesActionSupport.testConsumer(10, 7912737444879496875L, (c) -> {

			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			Set<EventLabel<PersonResourceUpdateEvent>> eventLabels = new LinkedHashSet<>();

			for (PersonId personId : people) {
				for (ResourceId resourceId : resourceIds) {

					EventLabel<PersonResourceUpdateEvent> eventLabel = PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, personId, resourceId);

					// show that the event label has the correct event class
					assertEquals(PersonResourceUpdateEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(resourceId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForPersonAndResource();
					assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonResourceUpdateEvent> eventLabel2 = PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, personId, resourceId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}

			// precondition tests

			// if the person id is null
			ContractException contractException = assertThrows(ContractException.class,
					() -> PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, null, TestResourceId.RESOURCE_1));
			assertEquals(PersonError.NULL_PERSON_ID, contractException.getErrorType());

			// if the person id is unknown
			contractException = assertThrows(ContractException.class, () -> PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, new PersonId(11110), TestResourceId.RESOURCE_1));
			assertEquals(PersonError.UNKNOWN_PERSON_ID, contractException.getErrorType());

			// if the resource id is null
			contractException = assertThrows(ContractException.class, () -> PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, new PersonId(0), null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class,
					() -> PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, new PersonId(0), TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPersonAndResource", args = {})
	public void testGetEventLabelerForPersonAndResource() {
		ResourcesActionSupport.testConsumer(30, 5829392632134617932L, (c) -> {
			PeopleDataManager peopleDataManager = c.getDataManager(PeopleDataManager.class);
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			List<PersonId> people = peopleDataManager.getPeople();
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			// create an event labeler
			EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForPersonAndResource();

			// show that the event labeler has the correct event class
			assertEquals(PersonResourceUpdateEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (PersonId personId : people) {

				for (ResourceId resourceId : resourceIds) {

					// derive the expected event label for this event
					EventLabel<PersonResourceUpdateEvent> expectedEventLabel = PersonResourceUpdateEvent.getEventLabelByPersonAndResource(c, personId, resourceId);

					// show that the event label and event labeler have
					// equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

					// create an event
					PersonResourceUpdateEvent event = new PersonResourceUpdateEvent(personId, resourceId, 10L, 30L);

					// show that the event labeler produces the correct
					// event
					// label
					EventLabel<PersonResourceUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}

			}

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByResource", args = { Context.class, ResourceId.class })
	public void testGetEventLabelByResource() {
		ResourcesActionSupport.testConsumer(10, 7912737444879496875L, (c) -> {

			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			Set<EventLabel<PersonResourceUpdateEvent>> eventLabels = new LinkedHashSet<>();

			for (ResourceId resourceId : resourceIds) {

				EventLabel<PersonResourceUpdateEvent> eventLabel = PersonResourceUpdateEvent.getEventLabelByResource(c, resourceId);

				// show that the event label has the correct event class
				assertEquals(PersonResourceUpdateEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(resourceId, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForResource();
				assertEquals(eventLabeler.getEventLabelerId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<PersonResourceUpdateEvent> eventLabel2 = PersonResourceUpdateEvent.getEventLabelByResource(c, resourceId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

			// precondition tests

			// if the resource id is null
			ContractException contractException = assertThrows(ContractException.class, () -> PersonResourceUpdateEvent.getEventLabelByResource(c, null));
			assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());

			// if the resource id is unknown
			contractException = assertThrows(ContractException.class, () -> PersonResourceUpdateEvent.getEventLabelByResource(c, TestResourceId.getUnknownResourceId()));
			assertEquals(ResourceError.UNKNOWN_RESOURCE_ID, contractException.getErrorType());

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForResource", args = {})
	public void testGetEventLabelerForResource() {
		ResourcesActionSupport.testConsumer(30, 5829392632134617932L, (c) -> {
			ResourcesDataManager resourcesDataManager = c.getDataManager(ResourcesDataManager.class);
			Set<ResourceId> resourceIds = resourcesDataManager.getResourceIds();

			// create an event labeler
			EventLabeler<PersonResourceUpdateEvent> eventLabeler = PersonResourceUpdateEvent.getEventLabelerForResource();

			// show that the event labeler has the correct event class
			assertEquals(PersonResourceUpdateEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label

			for (ResourceId resourceId : resourceIds) {

				// derive the expected event label for this event
				EventLabel<PersonResourceUpdateEvent> expectedEventLabel = PersonResourceUpdateEvent.getEventLabelByResource(c, resourceId);

				// show that the event label and event labeler have
				// equal id
				// values
				assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getEventLabelerId());

				// create an event
				PersonResourceUpdateEvent event = new PersonResourceUpdateEvent(new PersonId(0), resourceId, 10L, 30L);

				// show that the event labeler produces the correct
				// event
				// label
				EventLabel<PersonResourceUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

				assertEquals(expectedEventLabel, actualEventLabel);

			}

		});
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {

		for (TestResourceId testResourceId : TestResourceId.values()) {
			PersonId personId = new PersonId(7645);
			long previousResourceLevel = 45L;
			long currentResourceLevel = 398L;
			PersonResourceUpdateEvent personResourceUpdateEvent = new PersonResourceUpdateEvent(personId, testResourceId, previousResourceLevel, currentResourceLevel);
			assertEquals(testResourceId, personResourceUpdateEvent.getPrimaryKeyValue());
		}
	}

}
