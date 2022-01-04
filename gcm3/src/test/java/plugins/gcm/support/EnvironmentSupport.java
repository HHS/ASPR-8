package plugins.gcm.support;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;

import nucleus.Context;
import nucleus.Engine;
import nucleus.Engine.EngineBuilder;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.components.support.ComponentId;
import plugins.gcm.GCMMonolithicSupport;
import plugins.gcm.agents.Environment;
import plugins.gcm.experiment.Replication;
import plugins.gcm.experiment.ReplicationId;
import plugins.gcm.experiment.ReplicationImpl;
import plugins.gcm.experiment.ScenarioId;
import plugins.gcm.experiment.output.OutputItemHandler;
import plugins.gcm.input.Scenario;
import plugins.gcm.input.ScenarioBuilder;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupWeightingFunction;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.people.support.PersonId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import util.SeedProvider;

/**
 * A collection of static methods that support the automated Environment tests.
 * 
 * The number and code size of tests associated with {@link Environment}
 * requires that the tests be split up amongst several unit tests.
 * 
 * 
 * All observation method tests assert that:
 *
 * Post Condition 1: Those components that have registered to observe receive
 * observations(except as noted in assertion 4)
 *
 * Post Condition 2: Those components that have not registered to observe do not
 * receive observations
 *
 * Post Condition 3: Those components that have registered to stop
 * observing(even if they did not register beforehand) no longer receive
 * observations
 *
 * Post Condition 4: Those components that have registered to observe do not
 * receive observations if they are the component that caused the observation
 *
 * Seed cases for creating TestPlanExecutors range from 1000 to 1999
 * 
 */
public class EnvironmentSupport {
	public final static GlobalPropertyId TASK_PLAN_CONTAINER_PROPERTY_ID = new GlobalPropertyId() {
	};
	
	public final static GlobalPropertyId OBSERVATION_CONTAINER_PROPERTY_ID = new GlobalPropertyId() {
	};
	/*
	 * This is the central seed provider for the automated environment tests. It
	 * is used to provide seeds for the Seed Providers associated with each of
	 * the numbered environment tests.
	 */
	private static SeedProvider META_LEVEL_SEED_PROVIDER = new SeedProvider(4800196005692919747L);

	// This version of the META_LEVEL_SEED_PROVIDER's declaration is used by
	// getMetaSeed when manually testing the automated test suite.

	// private static SeedProvider META_LEVEL_SEED_PROVIDER;

	/**
	 * Returns a long seed value to associate with each of the numbered
	 * environment tests.
	 */
	public static long getMetaSeed(int unitCase) {
		// This section of code is used to manually test the automated test
		// suite's sensitivity to the statically defined seed value.
		if (META_LEVEL_SEED_PROVIDER == null) {
			long seed = new Random().nextLong();
			System.out.println("META_LEVEL_SEED_PROVIDER = " + seed);
			META_LEVEL_SEED_PROVIDER = new SeedProvider(seed);
		}
		return META_LEVEL_SEED_PROVIDER.getSeedValue(unitCase);
	}

	/**
	 * An error tolerance value used for comparing calculated double values for
	 * materials and times.
	 */
	public static final double COMPARISON_EPSILON = 0.000001;

	/**
	 * An enumeration for specifying the availability of property assignments
	 * for each of the randomly generated properties. TRUE forces all property
	 * definitions to allow property values to be assigned by components. FALSE
	 * forces the opposite. RANDOM forces random assignment of each property
	 * definition's policy on post-construction property assignment
	 * availability.
	 * 
	 * @author Shawn Hatch
	 *
	 */
	public static enum PropertyAssignmentPolicy {
		TRUE, FALSE, RANDOM
	};

	/**
	 * Adds a TaskPlanContainer to the scenario as a global property value under
	 * Global property id,TestGlobalPropertyId.TASK_PLAN_CONTAINER_PROPERTY_ID.
	 * The property is not assignable by components and is used by the
	 * TaskComponents to retrieve the container where each will find TaskPlans
	 * that they are to execute for a test. Returns the TaskPlanContainer so
	 * that the test can fill it from outside the scenario/simulation and then
	 * also retrieve the task plans to ensure that each was executed by a
	 * component. This helps to ensure that the component-driven tests actually
	 * execute and that the overall test is not a false positive.
	 */
	public static TaskPlanContainer addTaskPlanContainer(ScenarioBuilder scenarioBuilder) {
		TaskPlanContainer taskPlanContainer = new TaskPlanContainer();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder().setType(TaskPlanContainer.class)//
																	.setDefaultValue(taskPlanContainer)//
																	.setPropertyValueMutability(false)//
																	.build();//
		scenarioBuilder.defineGlobalProperty(EnvironmentSupport.TASK_PLAN_CONTAINER_PROPERTY_ID, propertyDefinition);
		return taskPlanContainer;
	}

	/**
	 * Adds an ObservationContainer to the scenario as a global property value
	 * under Global property
	 * id,TestGlobalPropertyId.OBSERVATION_CONTAINER_PROPERTY_ID. The property
	 * is not assignable by components and is used by the TaskComponents to
	 * retrieve the container where each will deposit the observations they
	 * receive from GCM. Returns the ObservationContainer so that the test can
	 * compare the actual observations to the expected observations from the
	 * test.
	 */
	public static ObservationContainer addObservationContainer(ScenarioBuilder scenarioBuilder) {
		ObservationContainer observationContainer = new ObservationContainer();

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(ObservationContainer.class)//
																	.setDefaultValue(observationContainer)//
																	.setPropertyValueMutability(false)//
																	.build();//

		scenarioBuilder.defineGlobalProperty(EnvironmentSupport.OBSERVATION_CONTAINER_PROPERTY_ID, propertyDefinition);
		return observationContainer;
	}

	/**
	 * Adds the standard set of compartments, regions, global components,
	 * material producers, materials and group types as defined in the various
	 * support enumerations for those items.
	 */
	public static void addStandardComponentsAndTypes(ScenarioBuilder scenarioBuilder) {
		for (final TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			scenarioBuilder.addCompartmentId(testCompartmentId, () -> new TaskComponent()::init);
		}
		for (final TestGlobalComponentId testGlobalComponentId : TestGlobalComponentId.values()) {
			scenarioBuilder.addGlobalComponentId(testGlobalComponentId, () -> new TaskComponent()::init);
		}
		for (final TestRegionId testRegionId : TestRegionId.values()) {
			scenarioBuilder.addRegionId(testRegionId, () -> new TaskComponent()::init);
		}
		for (final TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
			scenarioBuilder.addMaterialsProducerId(testMaterialsProducerId, () -> new TaskComponent()::init);
		}
		for (final TestResourceId testResourceId : TestResourceId.values()) {
			scenarioBuilder.addResource(testResourceId);
			scenarioBuilder.setResourceTimeTracking(testResourceId, testResourceId.getTimeTrackingPolicy());
		}
		for (final TestMaterialId testMaterialId : TestMaterialId.values()) {
			scenarioBuilder.addMaterial(testMaterialId);
		}
		for (final TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			scenarioBuilder.addGroupTypeId(testGroupTypeId);
		}
	}

	/**
	 * Adds the given number of people to each region/compartment pair in the
	 * scenario. Regions and compartments are defined in {@link TestRegionId}
	 * and {@link TestCompartmentId}
	 */
	public static void addStandardPeople(ScenarioBuilder scenarioBuilder, int peoplePerRegionAndCompartmentPair) {
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

	/**
	 * Adds the standard property definitions for each of the various properties
	 * defined in the supporting enumerations for all property types.
	 */
	public static void addStandardPropertyDefinitions(ScenarioBuilder scenarioBuilder, PropertyAssignmentPolicy propertyAssignmentPolicy, RandomGenerator randomGenerator) {
		addStandardPropertyDefinitions(scenarioBuilder, new LinkedHashMap<>(), propertyAssignmentPolicy, randomGenerator);
	}

	/**
	 * Adds the standard property definitions for each of the various properties
	 * defined in the supporting enumerations for all property types. Allows
	 * forced overrides for individual property definitions. This assumes that
	 * all property id values across all property type are unique. This is
	 * guaranteed by the various support enumerations.
	 */
	public static void addStandardPropertyDefinitions(ScenarioBuilder scenarioBuilder, Map<Object, PropertyDefinition> forcedPropertyDefinitions, PropertyAssignmentPolicy propertyAssignmentPolicy, RandomGenerator randomGenerator) {
		Map<Object, PropertyDefinition> propertyDefinitionMap;
		for (TestResourceId testResourceId : TestResourceId.values()) {
			Set<TestResourcePropertyId> testResourcePropertyIds = TestResourcePropertyId.getTestResourcePropertyIds(testResourceId);
			TestResourcePropertyId[] values = new TestResourcePropertyId[testResourcePropertyIds.size()];
			int index = 0;
			for(TestResourcePropertyId testResourcePropertyId : testResourcePropertyIds) {
				values[index++]=testResourcePropertyId;
			}
			propertyDefinitionMap = getPropertyDefinitionMap(values, propertyAssignmentPolicy, randomGenerator);
			for (final ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
				PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(resourcePropertyId);
				if (propertyDefinition == null) {
					propertyDefinition = propertyDefinitionMap.get(resourcePropertyId);
				}
				scenarioBuilder.defineResourceProperty(testResourceId, resourcePropertyId, propertyDefinition);
			}
		}

		for (TestCompartmentId testCompartmentId : TestCompartmentId.values()) {
			CompartmentPropertyId[] compartmentPropertyIds = testCompartmentId.getCompartmentPropertyIds();
			propertyDefinitionMap = getPropertyDefinitionMap(compartmentPropertyIds, propertyAssignmentPolicy, randomGenerator);
			for (final CompartmentPropertyId compartmentPropertyId : compartmentPropertyIds) {
				PropertyDefinition propertyDefinition = propertyDefinitionMap.get(compartmentPropertyId);
				scenarioBuilder.defineCompartmentProperty(testCompartmentId, compartmentPropertyId, propertyDefinition);
			}
		}

		propertyDefinitionMap = getPropertyDefinitionMap(TestGlobalPropertyId.values(), propertyAssignmentPolicy, randomGenerator);
		for (final TestGlobalPropertyId testGlobalPropertyId : TestGlobalPropertyId.values()) {
			PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(testGlobalPropertyId);
			if (propertyDefinition == null) {
				propertyDefinition = propertyDefinitionMap.get(testGlobalPropertyId);
			}
			scenarioBuilder.defineGlobalProperty(testGlobalPropertyId, propertyDefinition);
		}

		propertyDefinitionMap = getPropertyDefinitionMap(TestPersonPropertyId.values(), propertyAssignmentPolicy, randomGenerator);
		for (final TestPersonPropertyId testPersonPropertyId : TestPersonPropertyId.values()) {
			PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(testPersonPropertyId);
			if (propertyDefinition == null) {
				propertyDefinition = propertyDefinitionMap.get(testPersonPropertyId);
			}
			scenarioBuilder.definePersonProperty(testPersonPropertyId, propertyDefinition);
		}

		propertyDefinitionMap = getPropertyDefinitionMap(TestRegionPropertyId.values(), propertyAssignmentPolicy, randomGenerator);
		for (final TestRegionPropertyId testRegionPropertyId : TestRegionPropertyId.values()) {
			PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(testRegionPropertyId);
			if (propertyDefinition == null) {
				propertyDefinition = propertyDefinitionMap.get(testRegionPropertyId);
			}
			scenarioBuilder.defineRegionProperty(testRegionPropertyId, propertyDefinition);
		}

		propertyDefinitionMap = getPropertyDefinitionMap(TestMaterialsProducerPropertyId.values(), propertyAssignmentPolicy, randomGenerator);
		for (final TestMaterialsProducerPropertyId testMaterialsProducerPropertyId : TestMaterialsProducerPropertyId.values()) {
			PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(testMaterialsProducerPropertyId);
			if (propertyDefinition == null) {
				propertyDefinition = propertyDefinitionMap.get(testMaterialsProducerPropertyId);
			}
			scenarioBuilder.defineMaterialsProducerProperty(testMaterialsProducerPropertyId, propertyDefinition);
		}

		for (TestMaterialId testMaterialId : TestMaterialId.values()) {
			Set<TestBatchPropertyId> testBatchPropertyIds = TestBatchPropertyId.getTestBatchPropertyIds(testMaterialId);
			BatchPropertyId[] batchPropertyIds = new BatchPropertyId[testBatchPropertyIds.size()];
			int index = 0;
			for(TestBatchPropertyId testBatchPropertyId : testBatchPropertyIds) {
				batchPropertyIds[index++] = testBatchPropertyId;
			}
			propertyDefinitionMap = getPropertyDefinitionMap(batchPropertyIds, propertyAssignmentPolicy, randomGenerator);

			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(batchPropertyId);
				if (propertyDefinition == null) {
					propertyDefinition = propertyDefinitionMap.get(batchPropertyId);
				}
				scenarioBuilder.defineBatchProperty(testMaterialId, batchPropertyId, propertyDefinition);
			}
		}

		for (TestGroupTypeId testGroupTypeId : TestGroupTypeId.values()) {
			Set<TestGroupPropertyId> testGroupPropertyIds = TestGroupPropertyId.getTestGroupPropertyIds(testGroupTypeId);
			
			GroupPropertyId[] groupPropertyIds = new GroupPropertyId[testGroupPropertyIds.size()];
			int index = 0;
			for(TestGroupPropertyId testGroupPropertyId : testGroupPropertyIds) {
				groupPropertyIds[index++] =testGroupPropertyId;
					
			}
			propertyDefinitionMap = getPropertyDefinitionMap(groupPropertyIds, propertyAssignmentPolicy, randomGenerator);
			for (final GroupPropertyId groupPropertyId : groupPropertyIds) {
				PropertyDefinition propertyDefinition = forcedPropertyDefinitions.get(groupPropertyId);
				if (propertyDefinition == null) {
					propertyDefinition = propertyDefinitionMap.get(groupPropertyId);
				}
				scenarioBuilder.defineGroupProperty(testGroupTypeId, groupPropertyId, propertyDefinition);
			}
		}
	}

	/*
	 * Returns a map of randomly derived property definitions keyed to objects
	 * given. Each definition will comply with the PropertyAssignmentPolicy and
	 * there will be at least one non-Boolean property included in each
	 * non-empty return map. It is assumed that the incoming object array is
	 * non-null and non-empty. The guarantee for there being at least one
	 * non-Boolean property supports tests that require at least one such
	 * property because they need to assign at least three distinct such
	 * property values.
	 */
	private static Map<Object, PropertyDefinition> getPropertyDefinitionMap(final Object[] values, PropertyAssignmentPolicy propertyAssignmentPolicy, RandomGenerator randomGenerator) {
		final Map<Object, PropertyDefinition> result = new LinkedHashMap<>();
		boolean forceNonBooleanPropertyType = true;
		for (final Object value : values) {
			PropertyDefinition propertyDefinition = buildPropertyDefinition(randomGenerator, propertyAssignmentPolicy, forceNonBooleanPropertyType);
			forceNonBooleanPropertyType = false;
			result.put(value, propertyDefinition);
		}
		return result;
	}

	/*
	 * Returns a randomly generated property definition with the given
	 * PropertyAssignmentPolicy and subject to the forced non-boolean property
	 * type constraint.
	 */
	private static PropertyDefinition buildPropertyDefinition(RandomGenerator randomGenerator, PropertyAssignmentPolicy propertyAssignmentPolicy, boolean forceNonBooleanPropertyType) {
		Class<?> type;

		int typeCase;
		if (forceNonBooleanPropertyType) {
			typeCase = randomGenerator.nextInt(3) + 1;
		} else {
			typeCase = randomGenerator.nextInt(4);
		}
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = randomGenerator.nextBoolean();
			break;
		case 1:
			type = Integer.class;
			defaultValue = randomGenerator.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + randomGenerator.nextInt();
			break;
		default:
			type = Long.class;
			defaultValue = randomGenerator.nextLong();
			break;
		}
		boolean propertyValuesMayOverrideDefaultValue;
		switch (propertyAssignmentPolicy) {
		case FALSE:
			propertyValuesMayOverrideDefaultValue = false;
			break;
		case TRUE:
			propertyValuesMayOverrideDefaultValue = true;
			break;
		default:
			propertyValuesMayOverrideDefaultValue = randomGenerator.nextBoolean();
			break;
		}

		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(type)//
																	.setDefaultValue(defaultValue)//
																	.setPropertyValueMutability(propertyValuesMayOverrideDefaultValue)//
																	.setTimeTrackingPolicy(TimeTrackingPolicy.TRACK_TIME)//
																	.build();//

		return propertyDefinition;
	}

	/**
	 * Adds a random scenario id (1--1000) to the scenario. Sets the compartment
	 * and region person arrival tracking to TimeTrackingPolicy.TRACK_TIME and
	 * sets the compartment and region map options to MapOption.ARRAY.
	 */
	public static void addStandardTrackingAndScenarioId(ScenarioBuilder scenarioBuilder, RandomGenerator randomGenerator) {

		scenarioBuilder.setPersonCompartmentArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
		scenarioBuilder.setPersonRegionArrivalTracking(TimeTrackingPolicy.TRACK_TIME);
	}

	/*
	 * Returns a randomized property value inconsistent with the given property
	 * definition.
	 */
	public static Object generateIncompatiblePropertyValue(final PropertyDefinition propertyDefinition, final RandomGenerator randomGenerator) {

		final Class<?> type = propertyDefinition.getType();

		if (type == Boolean.class) {
			return randomGenerator.nextLong();
		} else if (type == Integer.class) {
			return randomGenerator.nextBoolean();

		} else if (type == String.class) {
			return randomGenerator.nextInt();

		} else if (type == Long.class) {
			return "String " + randomGenerator.nextInt();
		} else {
			throw new RuntimeException("unknown type " + type);
		}
	}

	/*
	 * Returns a randomized property value consistent with the given property
	 * definition.
	 */
	public static Object generatePropertyValue(final PropertyDefinition propertyDefinition, final RandomGenerator randomGenerator) {

		final Class<?> type = propertyDefinition.getType();

		if (type == Boolean.class) {
			return randomGenerator.nextBoolean();
		} else if (type == Integer.class) {
			return randomGenerator.nextInt();
		} else if (type == String.class) {
			return "String " + randomGenerator.nextInt();
		} else if (type == Long.class) {
			return randomGenerator.nextLong();
		} else {
			throw new RuntimeException("unknown type " + type);
		}
	}

	/**
	 * A {@link GroupWeightingFunction} implementor that returns a constant
	 * weight of 1.
	 */
	public static double getConstantMonoWeight(final Context context, final PersonId personId, final GroupId groupId) {
		return 1;
	}

	/**
	 * A {@link GroupWeightingFunction} implementor that returns a constant
	 * weight of 0.
	 */
	public static double getZeroMonoWeight(final Context context, final PersonId personId, final GroupId groupId) {
		return 0;
	}

	/**
	 * A {@link GroupWeightingFunction} implementor that returns a constant
	 * weight of -1.
	 */
	public static double getNegativeMonoWeight(final Context context, final PersonId personId, final GroupId groupId) {
		return -1;
	}

	/**
	 * A {@link GroupWeightingFunction} implementor that returns a weight of 1
	 * for person id 3 and zero otherwise. of .
	 */
	public static double getPerson3MonoWeight(final Context context, final PersonId personId, final GroupId groupId) {
		if (personId.getValue() == 3) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns a randomly selected replication whose id will be 1 to 1000.
	 */
	public static Replication getReplication(RandomGenerator randomGenerator) {
		return new ReplicationImpl(new ReplicationId(randomGenerator.nextInt(1000) + 1), randomGenerator.nextLong());
	}

	/**
	 * Asserts that all TaskPlans in the given task plan container have been
	 * marked as executed.
	 */
	public static void assertAllPlansExecuted(TaskPlanContainer taskPlanContainer) {
		for (ComponentId componentId : taskPlanContainer.getComponentIds()) {
			for (TaskPlan taskPlan : taskPlanContainer.getTaskPlans(componentId)) {
				assertTrue(taskPlan.planExecuted(), componentId + ":" + String.valueOf(taskPlan.getKey()) + " was not executed");
			}
		}
	}

	

	private static class OutputManager {
		
		private final OutputItemHandler outputItemHandler;
		private final ScenarioId scenarioId;
		private final ReplicationId replicationId;

		public OutputManager(ScenarioId scenarioId, ReplicationId replicationId, OutputItemHandler outputItemHandler) {
			this.scenarioId = scenarioId;
			this.replicationId = replicationId;
			this.outputItemHandler = outputItemHandler;
		}

		public void accept(Object object) {			
			outputItemHandler.handle(scenarioId, replicationId, object);
		}
	}
	
	public static void executeSimulation(Scenario scenario, Replication replication) {
		EngineBuilder engineBuilder =  GCMMonolithicSupport.getEngineBuilder(scenario, replication.getSeed());
		Engine engine = engineBuilder.build();
		engine.execute();
	}
	
	public static void executeSimulationX(Scenario scenario, ScenarioId scenarioId, Replication replication, OutputItemHandler outputItemHandler) {
		EngineBuilder engineBuilder =  GCMMonolithicSupport.getEngineBuilder(scenario, replication.getSeed());
		
		
		OutputManager outputManager = new OutputManager(scenarioId, replication.getId(), outputItemHandler);
		engineBuilder.setOutputConsumer(outputManager::accept);
		
		Engine engine = engineBuilder.build();
		engine.execute();
	}

}
