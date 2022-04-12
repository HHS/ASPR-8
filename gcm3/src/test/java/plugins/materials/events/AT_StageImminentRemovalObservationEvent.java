package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageImminentRemovalObservationEvent.class)

public class AT_StageImminentRemovalObservationEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent = new StageImminentRemovalObservationEvent(stageId);
		assertEquals(StageImminentRemovalObservationEvent.class, stageImminentRemovalObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent = new StageImminentRemovalObservationEvent(stageId);
		assertEquals(stageId, stageImminentRemovalObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent = new StageImminentRemovalObservationEvent(stageId);
		assertEquals("StageImminentRemovalObservationEvent [stageId=4534]", stageImminentRemovalObservationEvent.toString());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = {})
	public void testGetEventLabelByAll() {
		MaterialsActionSupport.testConsumer(2554165052883907962L, (c) -> {
			EventLabel<StageImminentRemovalObservationEvent> eventLabel = StageImminentRemovalObservationEvent.getEventLabelByAll();
			assertEquals(StageImminentRemovalObservationEvent.class, eventLabel.getEventClass());
			assertEquals(StageImminentRemovalObservationEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageImminentRemovalObservationEvent.getEventLabelerForAll().getId(), eventLabel.getLabelerId());
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {
		
		MaterialsActionSupport.testConsumer(5265237610749054744L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageImminentRemovalObservationEvent> eventLabeler = StageImminentRemovalObservationEvent.getEventLabelerForAll();
			assertEquals(StageImminentRemovalObservationEvent.class, eventLabeler.getEventClass());

			
					assertEquals(StageImminentRemovalObservationEvent.getEventLabelByAll().getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					StageImminentRemovalObservationEvent event = new StageImminentRemovalObservationEvent(new StageId(453));

					// derive the expected event label for this event
					EventLabel<StageImminentRemovalObservationEvent> expectedEventLabel = StageImminentRemovalObservationEvent.getEventLabelByAll();

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<StageImminentRemovalObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				
		});
	}

}
