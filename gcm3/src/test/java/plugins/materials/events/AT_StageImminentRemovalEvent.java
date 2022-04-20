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

@UnitTest(target = StageImminentRemovalEvent.class)

public class AT_StageImminentRemovalEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalEvent stageImminentRemovalEvent = new StageImminentRemovalEvent(stageId);
		assertEquals(StageImminentRemovalEvent.class, stageImminentRemovalEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalEvent stageImminentRemovalEvent = new StageImminentRemovalEvent(stageId);
		assertEquals(stageId, stageImminentRemovalEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(4534);
		StageImminentRemovalEvent stageImminentRemovalEvent = new StageImminentRemovalEvent(stageId);
		assertEquals("StageImminentRemovalEvent [stageId=4534]", stageImminentRemovalEvent.toString());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = {})
	public void testGetEventLabelByAll() {
		MaterialsActionSupport.testConsumer(2554165052883907962L, (c) -> {
			EventLabel<StageImminentRemovalEvent> eventLabel = StageImminentRemovalEvent.getEventLabelByAll();
			assertEquals(StageImminentRemovalEvent.class, eventLabel.getEventClass());
			assertEquals(StageImminentRemovalEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageImminentRemovalEvent.getEventLabelerForAll().getEventLabelerId(), eventLabel.getLabelerId());
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {
		
		MaterialsActionSupport.testConsumer(5265237610749054744L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageImminentRemovalEvent> eventLabeler = StageImminentRemovalEvent.getEventLabelerForAll();
			assertEquals(StageImminentRemovalEvent.class, eventLabeler.getEventClass());

			
					assertEquals(StageImminentRemovalEvent.getEventLabelByAll().getLabelerId(), eventLabeler.getEventLabelerId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					StageImminentRemovalEvent event = new StageImminentRemovalEvent(new StageId(453));

					// derive the expected event label for this event
					EventLabel<StageImminentRemovalEvent> expectedEventLabel = StageImminentRemovalEvent.getEventLabelByAll();

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<StageImminentRemovalEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				
		});
	}

}
