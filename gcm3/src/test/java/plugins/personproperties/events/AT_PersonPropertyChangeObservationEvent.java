package plugins.personproperties.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.people.PersonDataManager;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.PersonPropertiesActionSupport;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.regions.datamanagers.RegionDataManager;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = PersonPropertyChangeObservationEvent.class)
public class AT_PersonPropertyChangeObservationEvent implements Event {

	@Test
	@UnitTestConstructor(args = { PersonId.class, PersonPropertyId.class, Object.class, Object.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getCurrentPropertyValue", args = {})
	public void testGetCurrentPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		for (int i = 0; i < 10; i++) {
			Object currentValue = i;
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(currentValue, personPropertyChangeObservationEvent.getCurrentPropertyValue());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonPropertyId", args = {})
	public void testGetPersonPropertyId() {
		PersonId personId = new PersonId(10);
		Object previousValue = 0;
		Object currentValue = 1;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, previousValue, currentValue);
			assertEquals(testPersonPropertyId, personPropertyChangeObservationEvent.getPersonPropertyId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			PersonId personId = new PersonId(i);
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(personId, personPropertyChangeObservationEvent.getPersonId());
		}
	}

	@Test
	@UnitTestMethod(name = "getPreviousPropertyValue", args = {})
	public void testGetPreviousPropertyValue() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;

		Object currentValue = 1;
		for (int i = 0; i < 10; i++) {
			Object previousValue = i;
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
			assertEquals(previousValue, personPropertyChangeObservationEvent.getPreviousPropertyValue());
		}

	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		PersonId personId = new PersonId(10);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object previousValue = 0;
		Object currentValue = 1;
		PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, personPropertyId, previousValue, currentValue);
		String actualValue = personPropertyChangeObservationEvent.toString();
		String expectedValue = "PersonPropertyChangeObservationEvent [personId=10, personPropertyId=PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK, previousPropertyValue=0, currentPropertyValue=1]";
		assertEquals(actualValue, expectedValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPersonAndProperty", args = { SimulationContext.class, PersonId.class, PersonPropertyId.class })
	public void testGetEventLabelByPersonAndProperty() {

		PersonPropertiesActionSupport.testConsumer(5, 4447674464104241765L, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);

					// show that the event label has the correct event class
					assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty();
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPersonAndProperty", args = {})
	public void testGetEventLabelerForPersonAndProperty() {

		PersonPropertiesActionSupport.testConsumer(5, 1295505199200349679L, (c) -> {
			PersonDataManager personDataManager = c.getDataManager(PersonDataManager.class).get();
			List<PersonId> people = personDataManager.getPeople();

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForPersonAndProperty();

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			for (PersonId personId : people) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(c, personId, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByProperty", args = { SimulationContext.class, PersonPropertyId.class })
	public void testGetEventLabelByProperty() {

		PersonPropertiesActionSupport.testConsumer(0, 3639063830450063191L, (c) -> {

			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

				EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);

				// show that the event label has the correct event class
				assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

				// show that the event label has the correct primary key
				assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

				// show that the event label has the same id as its
				// associated labeler
				EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForProperty();
				assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

				// show that two event labels with the same inputs are equal
				EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);
				assertEquals(eventLabel, eventLabel2);

				// show that equal event labels have equal hash codes
				assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

				// show that two event labels with different inputs are not
				// equal
				assertTrue(eventLabels.add(eventLabel));
			}

		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForProperty", args = {})
	public void testGetEventLabelerForProperty() {

 
		PersonPropertiesActionSupport.testConsumer(0, 1006134798657400111L, (c) -> {
			

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForProperty();

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			
				
				
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(c, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(new PersonId(0), testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			
		});	

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByRegionAndProperty", args = { SimulationContext.class, RegionId.class, PersonPropertyId.class })
	public void testGetEventLabelByRegionAndProperty() {
		
		PersonPropertiesActionSupport.testConsumer(0, 7020781813930698612L, (c) -> {

			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();
			
			Set<EventLabel<PersonPropertyChangeObservationEvent>> eventLabels = new LinkedHashSet<>();

			for (TestRegionId testRegionId : TestRegionId.values()) {
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);

					// show that the event label has the correct event class
					assertEquals(PersonPropertyChangeObservationEvent.class, eventLabel.getEventClass());

					// show that the event label has the correct primary key
					assertEquals(testPersonPropertyId, eventLabel.getPrimaryKeyValue());

					// show that the event label has the same id as its
					// associated labeler
					EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionDataManager);
					assertEquals(eventLabeler.getId(), eventLabel.getLabelerId());

					// show that two event labels with the same inputs are equal
					EventLabel<PersonPropertyChangeObservationEvent> eventLabel2 = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);
					assertEquals(eventLabel, eventLabel2);

					// show that equal event labels have equal hash codes
					assertEquals(eventLabel.hashCode(), eventLabel2.hashCode());

					// show that two event labels with different inputs are not
					// equal
					assertTrue(eventLabels.add(eventLabel));
				}
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForRegionAndProperty", args = {})
	public void testGetEventLabelerForRegionAndProperty() {
	 
		PersonPropertiesActionSupport.testConsumer(50, 7370040718450691849L, (c) -> {
			RegionDataManager regionDataManager = c.getDataManager(RegionDataManager.class).get();

			// create an event labeler
			EventLabeler<PersonPropertyChangeObservationEvent> eventLabeler = PersonPropertyChangeObservationEvent.getEventLabelerForRegionAndProperty(regionDataManager);

			// show that the event labeler has the correct event class
			assertEquals(PersonPropertyChangeObservationEvent.class, eventLabeler.getEventClass());

			// show that the event labeler produces the expected event label
			for (TestRegionId testRegionId : TestRegionId.values()) {
				
				//we will need to select a person from the region to run this test correctly
				List<PersonId> peopleInRegion = regionDataManager.getPeopleInRegion(testRegionId);
				if(peopleInRegion.isEmpty()) {
					continue;
				}
				PersonId personId = peopleInRegion.get(0);
				for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {

					// derive the expected event label for this event
					EventLabel<PersonPropertyChangeObservationEvent> expectedEventLabel = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(c, testRegionId, testPersonPropertyId);

					// show that the event label and event labeler have equal id
					// values
					assertEquals(expectedEventLabel.getLabelerId(), eventLabeler.getId());

					// create an event
					PersonPropertyChangeObservationEvent event = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, 1, 2);

					// show that the event labeler produces the correct an event
					// label
					EventLabel<PersonPropertyChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);

					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});	

	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {

		PersonId personId = new PersonId(10);
		Object previousValue = 0;
		Object currentValue = 1;
		for (TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent = new PersonPropertyChangeObservationEvent(personId, testPersonPropertyId, previousValue, currentValue);
			assertEquals(testPersonPropertyId, personPropertyChangeObservationEvent.getPrimaryKeyValue());
		}
	}

}
