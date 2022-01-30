package plugins.regions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import nucleus.Simulation;
import nucleus.Simulation.Builder;
import nucleus.testsupport.actionplugin.ActionAgent;
import nucleus.testsupport.actionplugin.ActionPlugin;
import nucleus.testsupport.actionplugin.AgentActionPlan;
import plugins.compartments.support.CompartmentFilter;
import plugins.components.ComponentPlugin;
import plugins.partitions.PartitionsPlugin;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.PeoplePlugin;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.initialdata.PeopleInitialData;
import plugins.people.support.PersonId;
import plugins.properties.PropertiesPlugin;
import plugins.regions.RegionPlugin;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.initialdata.RegionInitialData;
import plugins.regions.testsupport.TestRegionId;
import plugins.reports.ReportPlugin;
import plugins.reports.initialdata.ReportsInitialData;
import plugins.stochastics.StochasticsPlugin;
import plugins.stochastics.initialdata.StochasticsInitialData;
import util.ContractException;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = RegionFilter.class)
public class AT_RegionFilter {

	private void testConsumer(int initialPopulation, long seed, Consumer<AgentContext> consumer) {
		ActionPlugin.Builder pluginBuilder = ActionPlugin.builder();
		pluginBuilder.addAgent("agent");
		pluginBuilder.addAgentActionPlan("agent", new AgentActionPlan(0, consumer));
		testConsumers(initialPopulation, seed, pluginBuilder.build());
	}

	private void testConsumers(int initialPopulation, long seed, ActionPlugin actionPlugin) {
		List<PersonId> people = new ArrayList<>();

		for (int i = 0; i < initialPopulation; i++) {
			people.add(new PersonId(i));
		}

		Builder builder = Simulation.builder();

		// add the region plugin
		RegionInitialData.Builder regionBuilder = RegionInitialData.builder();

		// assign people to regions
		TestRegionId testRegionId = TestRegionId.REGION_1;
		for (PersonId personId : people) {
			regionBuilder.setPersonRegion(personId, testRegionId.next());
		}

		for (TestRegionId regionId : TestRegionId.values()) {
			regionBuilder.setRegionComponentInitialBehaviorSupplier(regionId, () -> new ActionAgent(regionId)::init);
		}
		builder.addPlugin(RegionPlugin.PLUGIN_ID, new RegionPlugin(regionBuilder.build())::init);

		// add the people plugin

		PeopleInitialData.Builder peopleBuilder = PeopleInitialData.builder();
		for (PersonId personId : people) {
			peopleBuilder.addPersonId(personId);
		}

		builder.addPlugin(PeoplePlugin.PLUGIN_ID, new PeoplePlugin(peopleBuilder.build())::init);

		// add the properties plugin
		builder.addPlugin(PropertiesPlugin.PLUGIN_ID, new PropertiesPlugin()::init);

		// add the component plugin
		builder.addPlugin(ComponentPlugin.PLUGIN_ID, new ComponentPlugin()::init);

		// add the report plugin
		builder.addPlugin(ReportPlugin.PLUGIN_ID, new ReportPlugin(ReportsInitialData.builder().build())::init);

		// add the stochastics plugin
		builder.addPlugin(StochasticsPlugin.PLUGIN_ID, new StochasticsPlugin(StochasticsInitialData.builder().setSeed(seed).build())::init);

		// add the partitions plugin
		builder.addPlugin(PartitionsPlugin.PLUGIN_ID, new PartitionsPlugin()::init);

		// add the action plugin
		builder.addPlugin(ActionPlugin.PLUGIN_ID, actionPlugin::init);

		// build and execute the engine
		builder.build().execute();

		// show that all actions were executed
		assertTrue(actionPlugin.allActionsExecuted());

	}

	@Test
	@UnitTestConstructor(args = { Context.class, Set.class })
	public void testConstructor() {
		testConsumer(100, 7513298944605144297L, (c) -> {

			/* precondition: if the set is null */
			Set<RegionId> regionIds = null;

			assertThrows(RuntimeException.class, () -> new RegionFilter(regionIds));

			/* precondition: if the region is unknown */
			ContractException contractException = assertThrows(ContractException.class, () -> new RegionFilter(TestRegionId.getUnknownRegionId()).validate(c));
			assertEquals(RegionError.UNKNOWN_REGION_ID, contractException.getErrorType());

			assertThrows(RuntimeException.class, () -> new RegionFilter(null, TestRegionId.REGION_1).validate(c));

		});

	}

	/**
	 * Tests {@link CompartmentFilter#getFilterSensitivities()}
	 */
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {
		testConsumer(100, 4278456048187470819L, (c) -> {

			Filter filter = new RegionFilter(TestRegionId.REGION_1);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonRegionChangeObservationEvent.class, filterSensitivity.getEventClass());
		});
	}

	@Test
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {
		testConsumer(100, 8908124836418429909L, (c) -> {

			PersonDataView personDataView = c.getDataView(PersonDataView.class).get();
			RegionLocationDataView regionLocationDataView = c.getDataView(RegionLocationDataView.class).get();

			Filter filter = new RegionFilter(TestRegionId.REGION_1, TestRegionId.REGION_2);

			for (PersonId personId : personDataView.getPeople()) {
				boolean expected = regionLocationDataView.getPersonRegion(personId).equals(TestRegionId.REGION_1) || regionLocationDataView.getPersonRegion(personId).equals(TestRegionId.REGION_2);
				boolean actual = filter.evaluate(c, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(c, new PersonId(123412342)));

		});

	}
}
