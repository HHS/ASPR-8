package plugins.regions.events.observation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Consumer;

import javax.naming.Context;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.regions.RegionPlugin;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = PersonRegionChangeObservationEvent.class)
public class AT_PersonRegionChangeObservationEvent {

	@Test
	@UnitTestConstructor(args = { PersonId.class, RegionId.class, RegionId.class })
	public void testConstructor() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertNotNull(event);
	}

	@Test
	@UnitTestMethod(name = "getCurrentRegionId", args = {})
	public void testGetCurrentRegionId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(currentRegionId, event.getCurrentRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousRegionId", args = {})
	public void testGetPreviousRegionId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(previousRegionId, event.getPreviousRegionId());
	}

	@Test
	@UnitTestMethod(name = "getPersonId", args = {})
	public void testGetPersonId() {
		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);
		assertEquals(personId, event.getPersonId());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {

		PersonId personId = new PersonId(456);
		RegionId previousRegionId = TestRegionId.REGION_1;
		RegionId currentRegionId = TestRegionId.REGION_2;
		PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, previousRegionId, currentRegionId);

		String actualValue = event.toString();
		String expectedValue = "PersonRegionChangeObservationEvent [personId=456, previousRegionId=REGION_1, currentRegionId=REGION_2]";
		assertEquals(expectedValue, actualValue);
	}

	/*
	 * Runs the engine by loading all plugins necessary to support regions
	 * and executes the given consumer as an AgentActionPlan.
	 */
	private void testConsumer(Consumer<AgentContext> consumer) {

		Builder builder = Simulation.builder();

		// add the test regions
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();
		for (TestRegionId testRegionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(testRegionId, () -> (c) -> {
			});
		}
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(PeopleInitialData.builder().build())::init);
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(162474236345345L).build())::init);
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();

		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));

		ActionPlugin actionPlugin = pluginBuilder.build();
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestMethod(name = "getEventLabelByArrivalRegion", args = { Context.class, RegionId.class })
	public void testGetEventLabelByArrivalRegion() {
		testConsumer((c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, testRegionId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByDepartureRegion", args = { Context.class, RegionId.class })
	public void testGetEventLabelByDepartureRegion() {
		testConsumer((c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, testRegionId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByPerson", args = { Context.class, PersonId.class })
	public void getEventLabelByPerson() {
		testConsumer((c) -> {
			for (TestRegionId testRegionId : TestRegionId.values()) {
				PersonContructionData personContructionData = PersonContructionData.builder().add(testRegionId).build();
				c.resolveEvent(new PersonCreationEvent(personContructionData));
				PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

				EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId);
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(PersonRegionChangeObservationEvent.class, eventLabel.getPrimaryKeyValue());
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelerForPerson().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForArrivalRegion", args = {})
	public void testGetEventLabelerForArrivalRegion() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForArrivalRegion();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, testRegionId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(new PersonId(0), TestRegionId.REGION_1, TestRegionId.REGION_2);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(c, TestRegionId.REGION_2);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForDepartureRegion", args = {})
	public void testGetEventLabelerForDepartureRegion() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForDepartureRegion();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestRegionId testRegionId : TestRegionId.values()) {
				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, testRegionId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(new PersonId(0), TestRegionId.REGION_1, TestRegionId.REGION_2);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(c, TestRegionId.REGION_1);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForPerson", args = {})
	public void testGetEventLabelerForPerson() {
		testConsumer((c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<PersonRegionChangeObservationEvent> eventLabeler = PersonRegionChangeObservationEvent.getEventLabelerForPerson();
			assertEquals(PersonRegionChangeObservationEvent.class, eventLabeler.getEventClass());
			c.resolveEvent(new PersonCreationEvent(PersonContructionData.builder().add(TestRegionId.REGION_1).build()));
			PersonId personId = c.getDataView(PersonDataView.class).get().getLastIssuedPersonId().get();

			for (TestRegionId regionId : TestRegionId.values()) {
				TestRegionId nextRegionId = regionId.next();

				assertEquals(PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId).getLabelerId(), eventLabeler.getId());

				// show that the event labeler produces the expected event
				// label

				// create an event
				PersonRegionChangeObservationEvent event = new PersonRegionChangeObservationEvent(personId, regionId, nextRegionId);

				// derive the expected event label for this event
				EventLabel<PersonRegionChangeObservationEvent> expectedEventLabel = PersonRegionChangeObservationEvent.getEventLabelByPerson(c, personId);

				// have the event labeler produce an event label and show it
				// is equal to the expected event label
				EventLabel<PersonRegionChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
				assertEquals(expectedEventLabel, actualEventLabel);

			}
		});
	}

}
