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
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;

@UnitTest(target = StageMaterialsProducerChangeObservationEvent.class)
public class AT_StageMaterialsProducerChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, MaterialsProducerId.class, MaterialsProducerId.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(StageMaterialsProducerChangeObservationEvent.class, stageMaterialsProducerChangeObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(stageId, stageMaterialsProducerChangeObservationEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousMaterialsProducerId", args = {})
	public void testGetPreviousMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(previousMaterialsProducerId, stageMaterialsProducerChangeObservationEvent.getPreviousMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getCurrentMaterialsProducerId", args = {})
	public void testGetCurrentMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(currentMaterialsProducerId, stageMaterialsProducerChangeObservationEvent.getCurrentMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		String expectedValue = "StageMaterialsProducerChangeObservationEvent [stageId=344, previousMaterialsProducerId=MATERIALS_PRODUCER_1, currentMaterialsProducerId=MATERIALS_PRODUCER_2]";
		String actualValue = stageMaterialsProducerChangeObservationEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByDestination", args = { SimulationContext.class, MaterialsProducerId.class })
	public void testGetEventLabelByDestination() {

		MaterialsActionSupport.testConsumer(5070867343126122585L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByDestination(c, testMaterialsProducerId);
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByDestination(c, testMaterialsProducerId).getLabelerId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForDestination", args = {})
	public void testGetEventLabelerForDestination() {

		MaterialsActionSupport.testConsumer(1319347369419715424L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler = StageMaterialsProducerChangeObservationEvent.getEventLabelerForDestination();
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByDestination(c, currentMaterialsProducerId).getLabelerId(), eventLabeler.getId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerChangeObservationEvent event = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerChangeObservationEvent> expectedEventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByDestination(c,
								currentMaterialsProducerId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
						assertEquals(expectedEventLabel, actualEventLabel);
					}
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelBySource", args = { SimulationContext.class, MaterialsProducerId.class })
	public void testGetEventLabelBySource() {

		MaterialsActionSupport.testConsumer(1144625150509891316L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelBySource(c, testMaterialsProducerId);
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelBySource(c, testMaterialsProducerId).getLabelerId(), eventLabel.getLabelerId());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForSource", args = {})
	public void testGetEventLabelerForSource() {

		MaterialsActionSupport.testConsumer(4315809780717887025L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler = StageMaterialsProducerChangeObservationEvent.getEventLabelerForSource();
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelBySource(c, previousMaterialsProducerId).getLabelerId(), eventLabeler.getId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerChangeObservationEvent event = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerChangeObservationEvent> expectedEventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelBySource(c,
								previousMaterialsProducerId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
						assertEquals(expectedEventLabel, actualEventLabel);
					}
				}
			}

		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByStage", args = { SimulationContext.class, StageId.class })
	public void testGetEventLabelByStage() {

		MaterialsActionSupport.testConsumer(2260703021186632066L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();
			for (int i = 0; i < 10; i++) {
				StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
				EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByStage(c, stageId);
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForStage", args = {})
	public void testGetEventLabelerForStage() {

		MaterialsActionSupport.testConsumer(6880728031897161656L, (c) -> {
			MaterialsDataManager materialsDataManager = c.getDataManager(MaterialsDataManager.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler = StageMaterialsProducerChangeObservationEvent.getEventLabelerForStage();
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
						assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabeler.getId());

						/*
						 * show that the event labeler produces the expected
						 * event label
						 */

						// create an event
						StageMaterialsProducerChangeObservationEvent event = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerChangeObservationEvent> expectedEventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByStage(c, stageId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
						assertEquals(expectedEventLabel, actualEventLabel);
					}
				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = { SimulationContext.class })
	public void testGetEventLabelByAll() {

		MaterialsActionSupport.testConsumer(147633233074528056L, (c) -> {
			EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(c);
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getEventClass());
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(c).getLabelerId(), eventLabel.getLabelerId());
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {

		MaterialsActionSupport.testConsumer(4336563584188708581L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerChangeObservationEvent> eventLabeler = StageMaterialsProducerChangeObservationEvent.getEventLabelerForAll();
			assertEquals(StageMaterialsProducerChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(c).getLabelerId(), eventLabeler.getId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerChangeObservationEvent event = new StageMaterialsProducerChangeObservationEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerChangeObservationEvent> expectedEventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(c);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
						assertEquals(expectedEventLabel, actualEventLabel);
					}
				}
			}

		});
	}
}