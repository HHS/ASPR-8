package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestConstructor;
import annotations.UnitTestMethod;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;

@UnitTest(target = StageOfferChangeObservationEvent.class)
public class AT_StageOfferChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferChangeObservationEvent stageOfferChangeObservationEvent = new StageOfferChangeObservationEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(stageId, stageOfferChangeObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "isPreviousOfferState", args = {})
	public void testIsPreviousOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferChangeObservationEvent stageOfferChangeObservationEvent = new StageOfferChangeObservationEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(previousOfferState, stageOfferChangeObservationEvent.isPreviousOfferState());
	}

	@Test
	@UnitTestMethod(name = "isCurrentOfferState", args = {})
	public void testIsCurrentOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferChangeObservationEvent stageOfferChangeObservationEvent = new StageOfferChangeObservationEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(currentOfferState, stageOfferChangeObservationEvent.isCurrentOfferState());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferChangeObservationEvent stageOfferChangeObservationEvent = new StageOfferChangeObservationEvent(stageId, previousOfferState, currentOfferState);
		assertEquals("StageOfferChangeObservationEvent [stageId=543, previousOfferState=true, currentOfferState=false]", stageOfferChangeObservationEvent.toString());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByStage", args = { SimulationContext.class, StageId.class })
	public void testGetEventLabelByStage() {

		MaterialsActionSupport.testConsumer(1144625150509891316L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();
			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId);
				assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForStage", args = {})
	public void testGetEventLabelerForStage() {

		MaterialsActionSupport.testConsumer(305963642755178739L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageOfferChangeObservationEvent> eventLabeler = StageOfferChangeObservationEvent.getEventLabelerForStage();
			assertEquals(StageOfferChangeObservationEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				assertEquals(StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabeler.getId());

				/*
				 * show that the event labeler produces the expected event label
				 */

				// create an event
				StageOfferChangeObservationEvent event = new StageOfferChangeObservationEvent(stageId, true, false);

				// derive the expected event label for this event
				EventLabel<StageOfferChangeObservationEvent> expectedEventLabel = StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId);

				// have the event labeler produce an event label and
				// show it
				// is equal to the expected event label
				EventLabel<StageOfferChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = { SimulationContext.class })
	public void testGetEventLabelByAll() {
		MaterialsActionSupport.testConsumer(7385651282696514403L, (c) -> {
			EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByAll(c);
			assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getEventClass());
			assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageOfferChangeObservationEvent.getEventLabelByAll(c).getLabelerId(), eventLabel.getLabelerId());
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {

		MaterialsActionSupport.testConsumer(7765621733196161981L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageOfferChangeObservationEvent> eventLabeler = StageOfferChangeObservationEvent.getEventLabelerForAll();
			assertEquals(StageOfferChangeObservationEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertEquals(StageOfferChangeObservationEvent.getEventLabelByAll(c).getLabelerId(), eventLabeler.getId());

				/*
				 * show that the event labeler produces the expected event label
				 */

				// create an event
				StageOfferChangeObservationEvent event = new StageOfferChangeObservationEvent(stageId, true, false);

				// derive the expected event label for this event
				EventLabel<StageOfferChangeObservationEvent> expectedEventLabel = StageOfferChangeObservationEvent.getEventLabelByAll(c);

				// have the event labeler produce an event label and
				// show it
				// is equal to the expected event label
				EventLabel<StageOfferChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);
			}
		});
	}

}
