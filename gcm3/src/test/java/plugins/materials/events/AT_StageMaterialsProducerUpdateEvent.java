package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = StageMaterialsProducerUpdateEvent.class)
public class AT_StageMaterialsProducerUpdateEvent {

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
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(StageMaterialsProducerUpdateEvent.class, stageMaterialsProducerUpdateEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getStageId", args = {})
	public void testGetStageId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(stageId, stageMaterialsProducerUpdateEvent.getStageId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousMaterialsProducerId", args = {})
	public void testGetPreviousMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(previousMaterialsProducerId, stageMaterialsProducerUpdateEvent.getPreviousMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getCurrentMaterialsProducerId", args = {})
	public void testGetCurrentMaterialsProducerId() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		assertEquals(currentMaterialsProducerId, stageMaterialsProducerUpdateEvent.getCurrentMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		StageId stageId = new StageId(344);
		MaterialsProducerId previousMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerId currentMaterialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		StageMaterialsProducerUpdateEvent stageMaterialsProducerUpdateEvent = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId,
				currentMaterialsProducerId);
		String expectedValue = "StageMaterialsProducerUpdateEvent [stageId=344, previousMaterialsProducerId=MATERIALS_PRODUCER_1, currentMaterialsProducerId=MATERIALS_PRODUCER_2]";
		String actualValue = stageMaterialsProducerUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByDestination", args = { SimulationContext.class, MaterialsProducerId.class })
	public void testGetEventLabelByDestination() {

		MaterialsActionSupport.testConsumer(5070867343126122585L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				EventLabel<StageMaterialsProducerUpdateEvent> eventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByDestination(c, testMaterialsProducerId);
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByDestination(c, testMaterialsProducerId).getLabelerId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForDestination", args = {})
	public void testGetEventLabelerForDestination() {

		MaterialsActionSupport.testConsumer(1319347369419715424L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler = StageMaterialsProducerUpdateEvent.getEventLabelerForDestination();
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByDestination(c, currentMaterialsProducerId).getLabelerId(), eventLabeler.getEventLabelerId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerUpdateEvent event = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerUpdateEvent> expectedEventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByDestination(c,
								currentMaterialsProducerId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
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
				EventLabel<StageMaterialsProducerUpdateEvent> eventLabel = StageMaterialsProducerUpdateEvent.getEventLabelBySource(c, testMaterialsProducerId);
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelBySource(c, testMaterialsProducerId).getLabelerId(), eventLabel.getLabelerId());
			}
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForSource", args = {})
	public void testGetEventLabelerForSource() {

		MaterialsActionSupport.testConsumer(4315809780717887025L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler = StageMaterialsProducerUpdateEvent.getEventLabelerForSource();
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelBySource(c, previousMaterialsProducerId).getLabelerId(), eventLabeler.getEventLabelerId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerUpdateEvent event = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerUpdateEvent> expectedEventLabel = StageMaterialsProducerUpdateEvent.getEventLabelBySource(c,
								previousMaterialsProducerId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
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
				EventLabel<StageMaterialsProducerUpdateEvent> eventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByStage(c, stageId);
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getEventClass());
				assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabel.getLabelerId());
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
			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler = StageMaterialsProducerUpdateEvent.getEventLabelerForStage();
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = materialsDataManager.addStage(TestMaterialsProducerId.MATERIALS_PRODUCER_1);
						assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabeler.getEventLabelerId());

						/*
						 * show that the event labeler produces the expected
						 * event label
						 */

						// create an event
						StageMaterialsProducerUpdateEvent event = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerUpdateEvent> expectedEventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByStage(c, stageId);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
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
			EventLabel<StageMaterialsProducerUpdateEvent> eventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByAll(c);
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getEventClass());
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByAll(c).getLabelerId(), eventLabel.getLabelerId());
		});

	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {

		MaterialsActionSupport.testConsumer(4336563584188708581L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageMaterialsProducerUpdateEvent> eventLabeler = StageMaterialsProducerUpdateEvent.getEventLabelerForAll();
			assertEquals(StageMaterialsProducerUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId previousMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestMaterialsProducerId currentMaterialsProducerId : TestMaterialsProducerId.values()) {
					for (int i = 0; i < 10; i++) {
						StageId stageId = new StageId(i);
						assertEquals(StageMaterialsProducerUpdateEvent.getEventLabelByAll(c).getLabelerId(), eventLabeler.getEventLabelerId());

						// show that the event labeler produces the expected
						// event
						// label

						// create an event
						StageMaterialsProducerUpdateEvent event = new StageMaterialsProducerUpdateEvent(stageId, previousMaterialsProducerId, currentMaterialsProducerId);

						// derive the expected event label for this event
						EventLabel<StageMaterialsProducerUpdateEvent> expectedEventLabel = StageMaterialsProducerUpdateEvent.getEventLabelByAll(c);

						// have the event labeler produce an event label and
						// show it
						// is equal to the expected event label
						EventLabel<StageMaterialsProducerUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
						assertEquals(expectedEventLabel, actualEventLabel);
					}
				}
			}

		});
	}
}