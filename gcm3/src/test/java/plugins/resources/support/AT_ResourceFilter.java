package plugins.resources.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static plugins.support.EnvironmentSupport.assertAllPlansExecuted;

import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import nucleus.AgentContext;
import nucleus.Context;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.gcm.experiment.Replication;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ReplicationImpl;
import plugins.gcm.input.Scenario;
import plugins.gcm.input.ScenarioBuilder;
import plugins.gcm.input.UnstructuredScenarioBuilder;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.groups.testsupport.XTestGroupTypeId;
import plugins.partitions.support.Equality;
import plugins.partitions.support.Filter;
import plugins.partitions.support.FilterSensitivity;
import plugins.people.support.PersonId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.testsupport.ResourcesActionSupport;
import plugins.support.EnvironmentSupport;
import plugins.support.TaskComponent;
import plugins.support.TaskPlanContainer;
import plugins.support.XTestMaterialId;
import plugins.support.XTestMaterialsProducerId;
import plugins.support.XTestResourceId;
import util.SeedProvider;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test unit for {@link ResourceFilter}.
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ResourceFilter.class)
public class AT_ResourceFilter {

	

	private static void addStandardTrackingAndScenarioId(ScenarioBuilder scenarioBuilder, RandomGenerator randomGenerator) {
		scenarioBuilder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		scenarioBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
	}

	private static void addStandardComponentsAndTypes(ScenarioBuilder scenarioBuilder) {
		for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new TaskComponent()::init);
		}
		for (final TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			scenarioBuilder.addGlobalComponentId(testGlobalComponentId, () -> new TaskComponent()::init);
		}
		for (final TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new TaskComponent()::init);
		}
		for (final XTestMaterialsProducerId xTestMaterialsProducerId : XTestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(xTestMaterialsProducerId, () -> new TaskComponent()::init);
		}
		for (final XTestResourceId xTestResourceId : XTestResourceId.values()) {
			scenarioBuilder.addResource(xTestResourceId);
			scenarioBuilder.setResourceTimeTracking(xTestResourceId, xTestResourceId.trackValueAssignmentTimes());
		}
		for (final XTestMaterialId xTestMaterialId : XTestMaterialId.values()) {
			scenarioBuilder.addMaterial(xTestMaterialId);
		}
		for (final XTestGroupTypeId xTestGroupTypeId : XTestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(xTestGroupTypeId);
		}
	}

	private static void addStandardPeople(ScenarioBuilder scenarioBuilder, int peoplePerRegionAndCompartmentPair) {
		if (peoplePerRegionAndCompartmentPair < 1) {
			throw new RuntimeException("requires at least one person per (region,compartment) pair");
		}
		int personIndex = 0;
		for (final TestRegionId testRegionId : TestRegionId.values()) {
			for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
				for (int i = 0; i < peoplePerRegionAndCompartmentPair; i++) {
					PersonId personId = new PersonId(personIndex++);
					scenarioBuilder.addPerson(personId, testRegionId, testCompartmentId);
				}
			}
		}
	}

	private static TaskPlanContainer addTaskPlanContainer(ScenarioBuilder scenarioBuilder) {
		TaskPlanContainer taskPlanContainer = new TaskPlanContainer();
		scenarioBuilder.defineGlobalProperty(EnvironmentSupport.TASK_PLAN_CONTAINER_PROPERTY_ID, //
				PropertyDefinition	.builder()//
									.setType(TaskPlanContainer.class)//
									.setDefaultValue(taskPlanContainer)//
									.setPropertyValueMutability(false)//
									.build());//

		return taskPlanContainer;
	}

	private static Replication getReplication(RandomGenerator randomGenerator) {
		return new ReplicationImpl(new ReplicationId(randomGenerator.nextInt(1000) + 1), randomGenerator.nextLong());
	}

	private static SeedProvider SEED_PROVIDER;

	@BeforeAll
	public static void beforeClass() {
		SEED_PROVIDER = new SeedProvider(-3718170938428356262L);
	}

	/**
	 * Internal test(not part of public tests) to show that there are no large
	 * gaps in the seed cases generated by the SeedProvider.
	 */
	@AfterAll
	public static void afterClass() {
		// System.out.println(AT_ResourceFilter.class.getSimpleName() + " "
		// +SEED_PROVIDER.generateUnusedSeedReport());
	}

	/**
	 * Tests
	 * {@link ResourceFilter#ResourceFilter(Context, ResourceId, Equality, long)}
	 */
	@Test
	@UnitTestConstructor(args = { Context.class, ResourceId.class, Equality.class, long.class })
	public void testConstructor() {
		ResourcesActionSupport.testConsumer(100, 123123L, (c)->{});
		final long seed = SEED_PROVIDER.getSeedValue(0);
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {
			AgentContext context = environment.getContext();
			assertThrows(RuntimeException.class, () -> new ResourceFilter(XTestResourceId.getUnknownResourceId(), Equality.EQUAL, 12L).validate(context));
			assertThrows(RuntimeException.class, () -> new ResourceFilter(null, Equality.EQUAL, 12L).validate(context));
			assertThrows(RuntimeException.class, () -> new ResourceFilter(XTestResourceId.RESOURCE1, null, 12L).validate(context));

		});

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			final Filter filter = new ResourceFilter(XTestResourceId.RESOURCE1, Equality.EQUAL, 12L);
			assertNotNull(filter);

		});

		EnvironmentSupport.executeSimulation(scenario, replication);

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link ResourceFilter#getFilterSensitivities()}
	 */
	@Test
	@UnitTestMethod(name = "getFilterSensitivities", args = {})
	public void testGetFilterSensitivities() {

		final long seed = SEED_PROVIDER.getSeedValue(2);
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			Filter filter = new ResourceFilter(XTestResourceId.RESOURCE1, Equality.EQUAL, 12L);

			Set<FilterSensitivity<?>> filterSensitivities = filter.getFilterSensitivities();
			assertNotNull(filterSensitivities);
			assertEquals(filterSensitivities.size(), 1);

			FilterSensitivity<?> filterSensitivity = filterSensitivities.iterator().next();
			assertEquals(PersonResourceChangeObservationEvent.class, filterSensitivity.getEventClass());

		});

		EnvironmentSupport.executeSimulation(scenario, replication);

		assertAllPlansExecuted(taskPlanContainer);
	}

	/**
	 * Tests {@link ResourceFilter#evaluate(Context, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "evaluate", args = { Context.class, PersonId.class })
	public void testEvaluate() {
		final long seed = SEED_PROVIDER.getSeedValue(1);
		RandomGenerator randomGenerator = SeedProvider.getRandomGenerator(seed);

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		addStandardTrackingAndScenarioId(scenarioBuilder, randomGenerator);
		addStandardComponentsAndTypes(scenarioBuilder);
		addStandardPeople(scenarioBuilder, 30);
		TaskPlanContainer taskPlanContainer = addTaskPlanContainer(scenarioBuilder);

		Scenario scenario = scenarioBuilder.build();

		Replication replication = getReplication(randomGenerator);

		int testTime = 0;

		taskPlanContainer.addTaskPlan(TestGlobalComponentId.GLOBAL_COMPONENT_1, testTime++, (environment) -> {

			AgentContext context = environment.getContext();

			Filter filter = new ResourceFilter(XTestResourceId.RESOURCE1, Equality.GREATER_THAN, 12L);

			for (PersonId personId : environment.getPeople()) {
				long amount = environment.getRandomGenerator().nextInt(10) + 7;
				RegionId regionId = environment.getPersonRegion(personId);
				environment.addResourceToRegion(XTestResourceId.RESOURCE1, regionId, amount);
				environment.transferResourceToPerson(XTestResourceId.RESOURCE1, personId, amount);
			}

			for (PersonId personId : environment.getPeople()) {
				long personResourceLevel = environment.getPersonResourceLevel(personId, XTestResourceId.RESOURCE1);
				boolean expected = personResourceLevel > 12L;
				boolean actual = filter.evaluate(context, personId);
				assertEquals(expected, actual);
			}

			/* precondition: if the context is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(null, new PersonId(0)));

			/* precondition: if the person id is null */
			assertThrows(RuntimeException.class, () -> filter.evaluate(context, null));

			/* precondition: if the person id is unknown */
			assertThrows(RuntimeException.class, () -> filter.evaluate(context, new PersonId(123412342)));

		});

		EnvironmentSupport.executeSimulation(scenario, replication);

		assertAllPlansExecuted(taskPlanContainer);

	}
}
