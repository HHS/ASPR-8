package plugins.materials.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.SimulationContext;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPluginInitializer;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = StageOfferChangeObservationEvent.class)
public class AT_StageOfferChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { StageId.class, boolean.class, boolean.class })
	public void testConstructor() {
		//nothing to test
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
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();
			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
				EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId);
				assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(StageOfferChangeObservationEvent.getEventLabelByStage(c, stageId).getLabelerId(), eventLabel.getLabelerId());
			}
		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(1144625150509891316L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForStage", args = {})
	public void testGetEventLabelerForStage() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageOfferChangeObservationEvent> eventLabeler = StageOfferChangeObservationEvent.getEventLabelerForStage();
			assertEquals(StageOfferChangeObservationEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
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

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(305963642755178739L, actionPluginInitializer);

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByAll", args = { SimulationContext.class })
	public void testGetEventLabelByAll() {

		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {
			EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByAll(c);
			assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getEventClass());
			assertEquals(StageOfferChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
			assertEquals(StageOfferChangeObservationEvent.getEventLabelByAll(c).getLabelerId(), eventLabel.getLabelerId());
		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(7385651282696514403L, actionPluginInitializer);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForAll", args = {})
	public void testGetEventLabelerForAll() {
		
		ActionPluginInitializer.Builder pluginBuilder = ActionPluginInitializer.builder();

		pluginBuilder.addAgentActionPlan(TestMaterialsProducerId.MATERIALS_PRODUCER_1, new AgentActionPlan(0, (c) -> {
			MaterialsDataView materialsDataView = c.getDataView(MaterialsDataView.class).get();

			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<StageOfferChangeObservationEvent> eventLabeler = StageOfferChangeObservationEvent.getEventLabelerForAll();
			assertEquals(StageOfferChangeObservationEvent.class, eventLabeler.getEventClass());

			for (int i = 0; i < 10; i++) {
				c.resolveEvent(new StageCreationEvent());
				StageId stageId = materialsDataView.getLastIssuedStageId().get();
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

		}));

		ActionPluginInitializer actionPluginInitializer = pluginBuilder.build();
		MaterialsActionSupport.testConsumers(7765621733196161981L, actionPluginInitializer);
	}

}
