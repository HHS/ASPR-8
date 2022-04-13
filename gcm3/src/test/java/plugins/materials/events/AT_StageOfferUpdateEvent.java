package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageOfferUpdateEvent.class)
public class AT_StageOfferUpdateEvent {

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
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(stageId, stageOfferUpdateEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "isPreviousOfferState", args = {})
	public void testIsPreviousOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(previousOfferState, stageOfferUpdateEvent.isPreviousOfferState());
	}

	@Test
	@UnitTestMethod(name = "isCurrentOfferState", args = {})
	public void testIsCurrentOfferState() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals(currentOfferState, stageOfferUpdateEvent.isCurrentOfferState());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(543);
		boolean previousOfferState = true;
		boolean currentOfferState = false;
		StageOfferUpdateEvent stageOfferUpdateEvent = new StageOfferUpdateEvent(stageId, previousOfferState, currentOfferState);
		assertEquals("StageOfferUpdateEvent [stageId=543, previousOfferState=true, currentOfferState=false]", stageOfferUpdateEvent.toString());
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByStage", args = { SimulationContext.class, StageId.class })
	public void testGetEventLabelByStage() {

		MaterialsActionSupport.testConsumer(1144625150509891316L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();
			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_2);
				EventLabel<StageOfferUpdateEvent> eventLabel = StageOfferUpdateEvent.getEventLabelByStage(c, stageId);
				assertEquals(StageOfferUpdateEvent.class, eventLabel.getEventClass());
				assertEquals(StageOfferUpdateEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageOfferUpdateEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabel.getLabelerId());
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
			EventLabeler<StageOfferUpdateEvent> eventLabeler = StageOfferUpdateEvent.getEventLabelerForStage();
			assertEquals(StageOfferUpdateEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_3);
				assertEquals(StageOfferUpdateEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabeler.getId());

				/*
				 * show that the event labeler produces the expected event label
				 */

				// create an event
				StageOfferUpdateEvent event = new StageOfferUpdateEvent(stageId, true, false);

				// derive the expected event label for this event
				EventLabel<StageOfferUpdateEvent> expectedEventLabel = StageOfferUpdateEvent.getEventLabelByStage(c, stageId);

				// have the event labeler produce an event label and
				// show it
				// is equal to the expected event label
				EventLabel<StageOfferUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = { SimulationContext.class })
	public void testGetEventLabelByAll() {
		MaterialsActionSupport.testConsumer(7385651282696514403L, (c) -> {
			EventLabel<StageOfferUpdateEvent> eventLabel = StageOfferUpdateEvent.getEventLabelByAll(c);
			assertEquals(StageOfferUpdateEvent.class, eventLabel.getEventClass());
			assertEquals(StageOfferUpdateEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageOfferUpdateEvent.getEventLabelByAll(c).getLabelerId(), eventLabel.getLabelerId());
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {

		MaterialsActionSupport.testConsumer(7765621733196161981L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageOfferUpdateEvent> eventLabeler = StageOfferUpdateEvent.getEventLabelerForAll();
			assertEquals(StageOfferUpdateEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				assertEquals(StageOfferUpdateEvent.getEventLabelByAll(c).getLabelerId(), eventLabeler.getId());

				/*
				 * show that the event labeler produces the expected event label
				 */

				// create an event
				StageOfferUpdateEvent event = new StageOfferUpdateEvent(stageId, true, false);

				// derive the expected event label for this event
				EventLabel<StageOfferUpdateEvent> expectedEventLabel = StageOfferUpdateEvent.getEventLabelByAll(c);

				// have the event labeler produce an event label and
				// show it
				// is equal to the expected event label
				EventLabel<StageOfferUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);
			}
		});
	}

}
