package plugins.gcm;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.compartments.testsupport.TestCompartmentId;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.input.Scenario;
import plugins.gcm.input.ScenarioBuilder;
import plugins.gcm.input.ScenarioErrorType;
import plugins.gcm.input.ScenarioException;
import plugins.gcm.input.UnstructuredScenarioBuilder;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.personproperties.testsupport.TestPersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.regions.testsupport.TestRegionId;
import plugins.regions.testsupport.TestRegionPropertyId;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link UnstructuredScenarioBuilder}
 * 
 * UnstructuredScenarioBuilder methods are invoked in an order that is
 * inconsistent with the ordering requirements of the StructuredScenarioBuilder.
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = UnstructuredScenarioBuilder.class)
public class AT_UnstructuredScenarioBuilder {

	/*
	 * A placeholder implementation to satisfy scenario construction
	 */
	private static class PlaceholderComponent extends AbstractComponent {

	}

	/*
	 * A Component implementor that acts as a placeholder for components not
	 * expected to execute activities relevant to the tests.
	 */
	public static class EmptyComponent extends AbstractComponent {

	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#build() }
	 */
	@Test
	@UnitTestMethod(name = "build", args = {})
	public void testBuild() {
		// No test performed: The build method is tested by proxy via the other
		// test methods.
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addBatch(BatchId, MaterialId, double, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addBatch", args = { BatchId.class, MaterialId.class, double.class, MaterialsProducerId.class })
	public void testAddBatch() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		BatchId batchId = new BatchId(14);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		double amount = 10;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the batch id is null
		scenarioBuilder.addBatch(null, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());
		// precondition: if the material id is null
		scenarioBuilder.addBatch(batchId, null, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());
		// precondition: if the material id is unknown
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIAL_ID, scenarioException.getScenarioErrorType());
		// precondition: if the amount is negative
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, -1, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NEGATIVE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());
		// precondition: if the amount is not finite
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, Double.POSITIVE_INFINITY, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, Double.NEGATIVE_INFINITY, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, Double.NaN, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());
		// precondition: if the materials producer id is null
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, amount, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is unknown
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch id was previously added
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the batch has the expected amount
		assertEquals(amount, scenario.getBatchAmount(batchId).doubleValue(), 0);
		// postcondition: the batch has the expected material type
		assertEquals(materialId, scenario.getBatchMaterial(batchId));
		// postcondition: the batch is owned by the expected materials producer
		assertEquals(materialsProducerId, scenario.getBatchMaterialsProducer(batchId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addBatchToStage(StageId, BatchId)}
	 */
	@Test
	@UnitTestMethod(name = "addBatchToStage", args = { StageId.class, BatchId.class })
	public void testAddBatchToStage() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		StageId stageId = new StageId(37);
		BatchId batchId = new BatchId(15);
		MaterialId materialId = TestMaterialId.MATERIAL_2;
		double amount = 13.5;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		// precondition: if the stage id is null
		scenarioBuilder.addBatchToStage(null, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_STAGE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the stage id is unknown
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_STAGE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch id is null
		scenarioBuilder.addBatchToStage(stageId, null);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch id is unknown
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_ID, scenarioException.getScenarioErrorType());

		// precondition: if the stage and batch are not associated with the same
		// materials producer
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addMaterialsProducerId(TestMaterialsProducerId.MATERIALS_PRODUCER_2, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER, scenarioException.getScenarioErrorType());

		// precondition: if the batch is already associated any stage
		StageId alternateStageId = new StageId(319);
		scenarioBuilder.addBatchToStage(alternateStageId, batchId);
		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addStage(alternateStageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.BATCH_ALREADY_STAGED, scenarioException.getScenarioErrorType());

		scenarioBuilder.addBatchToStage(stageId, batchId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		Set<BatchId> stageBatches = scenario.getStageBatches(stageId);

		// postcondition: the stage is associated with only the batches added
		assertEquals(1, stageBatches.size());

		// postcondition: the stage is associated with the batch
		assertTrue(stageBatches.contains(batchId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addCompartmentId(CompartmentId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addCompartmentId", args = { CompartmentId.class, Class.class })
	public void testAddCompartmentId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition: if the compartment id is null
		scenarioBuilder.addCompartmentId(null, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is equal to another previously
		// added component id
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the component class is null
		scenarioBuilder.addCompartmentId(compartmentId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario should contain the compartment and
		// component class
		assertTrue(scenario.getCompartmentIds().contains(compartmentId));
		assertNotNull(scenario.getCompartmentInitialBehaviorSupplier(compartmentId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addGlobalComponentId(GlobalComponentId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addGlobalComponentId", args = { GlobalComponentId.class, Class.class })
	public void testAddGlobalComponentId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GlobalComponentId globalComponentId = TestGlobalComponentId.GLOBAL_COMPONENT_1;

		// precondition: if the global component id is null
		scenarioBuilder.addGlobalComponentId(null, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the global component id is equal to another
		// previously
		// added component id
		scenarioBuilder.addGlobalComponentId(globalComponentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addGlobalComponentId(globalComponentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition : if the global component class is null
		scenarioBuilder.addGlobalComponentId(globalComponentId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGlobalComponentId(globalComponentId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGlobalComponentIds().contains(globalComponentId));
		assertNotNull(scenario.getGlobalInitialBehaviorSupplier(globalComponentId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addGroup(GroupId, GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupId.class, GroupTypeId.class })
	public void testAddGroup() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(15);
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition : if the group id is null
		scenarioBuilder.addGroup(null, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition : if the group type id is null
		scenarioBuilder.addGroup(groupId, null);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the group type id is unknown
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the group was previously added
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();
		// post condition: the scenario contains the group
		scenario.getGroupIds().contains(groupId);
		// post condition: the group has the expected group type
		assertEquals(groupTypeId, scenario.getGroupTypeId(groupId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addGroupTypeId(GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroupTypeId", args = { GroupTypeId.class })
	public void testAddGroupTypeId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition: if the group type id is null
		scenarioBuilder.addGroupTypeId(null);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group type was previously added
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the group type id
		assertTrue(scenario.getGroupTypeIds().contains(groupTypeId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addMaterial(MaterialId)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterial", args = { MaterialId.class })
	public void testAddMaterial() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		MaterialId materialId = TestMaterialId.MATERIAL_1;

		// precondition: if the material id is null
		scenarioBuilder.addMaterial(null);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition: if the material was previously added
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addMaterial(materialId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addMaterial(materialId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition : the scenario contains the material id
		assertTrue(scenario.getMaterialIds().contains(materialId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addMaterialsProducerId(MaterialsProducerId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterialsProducerId", args = { MaterialsProducerId.class, Class.class })
	public void testAddMaterialsProducerId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the materials producer id is null
		scenarioBuilder.addMaterialsProducerId(null, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is equal to another
		// previously added component id
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer component class is null
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the materials producer id
		assertTrue(scenario.getMaterialsProducerIds().contains(materialsProducerId));
		assertNotNull(scenario.getMaterialsProducerInitialBehaviorSupplier(materialsProducerId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addPerson(PersonId, RegionId, CompartmentId)}
	 */
	@Test
	@UnitTestMethod(name = "addPerson", args = { PersonId.class, RegionId.class, CompartmentId.class })
	public void testAddPerson() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(56);
		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition: if the person id is null
		scenarioBuilder.addPerson(null, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region id is null
		scenarioBuilder.addPerson(personId, null, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region id is unknown
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is null
		scenarioBuilder.addPerson(personId, regionId, null);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is unknown
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person was previously added
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the person
		assertTrue(scenario.getPeopleIds().contains(personId));
		// postcondition: the person has the expected compartment
		assertEquals(compartmentId, scenario.getPersonCompartment(personId));
		// postcondition: the person has the expected region
		assertEquals(regionId, scenario.getPersonRegion(personId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addPersonToGroup(GroupId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(45);
		PersonId personId = new PersonId(37);
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_2;
		RegionId regionId = TestRegionId.REGION_5;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;

		// precondition: if the person id is null
		scenarioBuilder.addPersonToGroup(groupId, null);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person id is unknown
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group id is null
		scenarioBuilder.addPersonToGroup(null, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group id is unknown
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person was previously added to the group
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_GROUP_MEMBERSHIP, scenarioException.getScenarioErrorType());

		scenarioBuilder.addPersonToGroup(groupId, personId);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the person is in the group
		assertTrue(scenario.getGroupMembers(groupId).contains(personId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addRegionId(RegionId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addRegionId", args = { RegionId.class, Class.class })
	public void testAddRegionId() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionId regionId = TestRegionId.REGION_4;

		// precondition: if the region id is null
		scenarioBuilder.addRegionId(null, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the region was previously added
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition: if the region component class is null
		scenarioBuilder.addRegionId(regionId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the region and component class
		assertTrue(scenario.getRegionIds().contains(regionId));
		assertNotNull(scenario.getRegionInitialBehaviorSupplier(regionId));
	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#addResource(ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "addResource", args = { ResourceId.class })
	public void testAddResource() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE_2;

		// precondition: if the resource id is null
		scenarioBuilder.addResource(null);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource was previously added
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getResourceIds().contains(resourceId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addStage(StageId, boolean, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addStage", args = { StageId.class, boolean.class, MaterialsProducerId.class })
	public void testAddStage() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		StageId stageId = new StageId(67);

		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;

		// precondition: if the stage id is null
		scenarioBuilder.addStage(null, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_STAGE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is null
		scenarioBuilder.addStage(stageId, true, null);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is unknown
		scenarioBuilder.addStage(stageId, true, TestMaterialsProducerId.MATERIALS_PRODUCER_2);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the stage id was previously added
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// an initially offered stage
		scenarioBuilder.addStage(stageId, true, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.getStageIds().contains(stageId));
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.isStageOffered(stageId));
		// postcondition: the stage is owned by the materials producer in the
		// scenario
		assertEquals(materialsProducerId, scenario.getStageMaterialsProducer(stageId));
		// postcondition: the stage has no associated batches in the scenario
		assertTrue(scenario.getStageBatches(stageId).isEmpty());

		// a stage not yet offered
		scenarioBuilder.addStage(stageId, false, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenario = scenarioBuilder.build();
		// postcondition: the scenario contains the stage id
		assertTrue(scenario.getStageIds().contains(stageId));
		// postcondition: the scenario contains the stage id
		assertFalse(scenario.isStageOffered(stageId));
		// postcondition: the stage is owned by the materials producer in the
		// scenario
		assertEquals(materialsProducerId, scenario.getStageMaterialsProducer(stageId));
		// postcondition: the stage has no associated batches in the scenario
		assertTrue(scenario.getStageBatches(stageId).isEmpty());

	}

	private PropertyDefinition generateRandomPropertyDefinition(Random random) {
		Class<?> type;
		final int typeCase = random.nextInt(4);
		Object defaultValue;
		switch (typeCase) {
		case 0:
			type = Boolean.class;
			defaultValue = random.nextBoolean();
			break;
		case 1:
			type = Integer.class;
			defaultValue = random.nextInt();
			break;
		case 2:
			type = String.class;
			defaultValue = "String " + random.nextInt();
			break;
		default:
			type = Long.class;
			defaultValue = random.nextLong();
			break;
		}
		boolean propertyValuesAreMutability = random.nextBoolean();
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.values()[random.nextInt(TimeTrackingPolicy.values().length)];

		final PropertyDefinition result = PropertyDefinition.builder()//
															.setType(type)//
															.setDefaultValue(defaultValue)//
															.setPropertyValueMutability(propertyValuesAreMutability)//
															.setTimeTrackingPolicy(timeTrackingPolicy)//
															.build();//
		return result;
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineBatchProperty(MaterialId, BatchPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineBatchProperty", args = { MaterialId.class, BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random(47348457892L);
		PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(random);
		MaterialId materialId = TestMaterialId.MATERIAL_1;
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_1_BOOLEAN_IMMUTABLE_NO_TRACK;

		// precondition: if the material id is null
		scenarioBuilder.defineBatchProperty(null, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition: if the material id is unknown
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property id is null
		scenarioBuilder.defineBatchProperty(materialId, null, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, null);
		scenarioBuilder.addMaterial(materialId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the batch property was previously defined
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_BATCH_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		for (TestMaterialId material : TestMaterialId.values()) {
			for (BatchPropertyId property : TestBatchPropertyId.getTestBatchPropertyIds(material)) {
				propertyDefinition = generateRandomPropertyDefinition(random);
				scenarioBuilder.defineBatchProperty(material, property, propertyDefinition);
				scenarioBuilder.addMaterial(material);
				Scenario scenario = scenarioBuilder.build();
				// postcondition: the scenario should contain the property id
				assertTrue(scenario.getBatchPropertyIds(material).contains(property));
				// postcondition: the scenario should contain the property
				// definition
				assertEquals(propertyDefinition, scenario.getBatchPropertyDefinition(material, property));
			}
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineCompartmentProperty(CompartmentId, CompartmentPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineCompartmentProperty", args = { CompartmentId.class, CompartmentPropertyId.class, PropertyDefinition.class })
	public void testDefineCompartmentProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_3.getCompartmentPropertyId(0);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(5)//
																	.build();//

		// precondition: if the compartment id is null
		scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
		scenarioBuilder.defineCompartmentProperty(null, compartmentPropertyId, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is unknown
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property id is null
		scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, null, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the compartment property was previously defined
		scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		for (int i = 0; i < 100; i++) {
			CompartmentPropertyId property = new CompartmentPropertyId() {
			};
			PropertyDefinition propertyDef = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineCompartmentProperty(compartmentId, property, propertyDef);
			scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getCompartmentPropertyIds(compartmentId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDef, scenario.getCompartmentPropertyDefinition(compartmentId, property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineGlobalProperty(GlobalPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random();
		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(5)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineGlobalProperty(null, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.defineGlobalProperty(globalPropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the global property was previously defined
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		for (int i = 0; i < 1000; i++) {
			GlobalPropertyId property = new GlobalPropertyId() {
			};
			PropertyDefinition propertyDef = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineGlobalProperty(property, propertyDef);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getGlobalPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDef, scenario.getGlobalPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineGroupProperty(GroupTypeId, GroupPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGroupProperty", args = { GroupTypeId.class, GroupPropertyId.class, PropertyDefinition.class })
	public void testDefineGroupProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(235)//
																	.build();//

		// precondition: if the group type id is null
		scenarioBuilder.defineGroupProperty(null, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group type id is unknown
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property id is null
		scenarioBuilder.defineGroupProperty(groupTypeId, null, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, null);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the group property was previously defined
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_GROUP_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		Random random = new Random(34593450987L);
		for (int i = 0; i < 1000; i++) {
			GroupPropertyId property = new GroupPropertyId() {
			};
			groupTypeId = TestGroupTypeId.values()[random.nextInt(TestGroupTypeId.values().length)];
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineGroupProperty(groupTypeId, property, propertyDefinition);
			scenarioBuilder.addGroupTypeId(groupTypeId);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getGroupPropertyIds(groupTypeId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getGroupPropertyDefinition(groupTypeId, property));
		}

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineMaterialsProducerProperty(MaterialsProducerPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(6)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineMaterialsProducerProperty(null, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer property was previously
		// defined
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		Random random = new Random(474563456347L);
		for (int i = 0; i < 100; i++) {
			MaterialsProducerPropertyId matProducerPropertyId = new MaterialsProducerPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineMaterialsProducerProperty(matProducerPropertyId, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getMaterialsProducerPropertyIds().contains(matProducerPropertyId));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getMaterialsProducerPropertyDefinition(matProducerPropertyId));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#definePersonProperty(PersonPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("value")//
																	.build();//

		// precondition if the property id is null
		scenarioBuilder.definePersonProperty(null, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition if the property definition is null
		scenarioBuilder.definePersonProperty(personPropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition if the person property was previously defined
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_PERSON_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			PersonPropertyId property = new PersonPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.definePersonProperty(property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getPersonPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getPersonPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineRegionProperty(RegionPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineRegionProperty", args = { RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(6.7)//
																	.build();//

		// precondition: if the property id is null
		scenarioBuilder.defineRegionProperty(null, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.defineRegionProperty(regionPropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the region property was previously defined
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_REGION_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			RegionPropertyId property = new RegionPropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.defineRegionProperty(property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getRegionPropertyIds().contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getRegionPropertyDefinition(property));
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#defineResourceProperty(ResourceId, ResourcePropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineResourceProperty", args = { ResourceId.class, ResourcePropertyId.class, PropertyDefinition.class })
	public void testDefineResourceProperty() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_3_2_STRING_MUTABLE;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Long.class)//
																	.setDefaultValue(3454L)//
																	.build();//

		// precondition: if the resource id is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(null, resourcePropertyId, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(TestResourceId.getUnknownResourceId(), resourcePropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property id is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, null, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the property definition is null
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, null);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition: if the resource property was previously defined
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		Random random = new Random(784418899778L);
		for (int i = 0; i < 100; i++) {
			ResourcePropertyId property = new ResourcePropertyId() {
			};
			propertyDefinition = generateRandomPropertyDefinition(random);
			scenarioBuilder.addResource(resourceId);
			scenarioBuilder.defineResourceProperty(resourceId, property, propertyDefinition);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario should contain the property id
			assertTrue(scenario.getResourcePropertyIds(resourceId).contains(property));
			// postcondition: the scenario should contain the property
			// definition
			assertEquals(propertyDefinition, scenario.getResourcePropertyDefinition(resourceId, property));
		}
	}

	private static Object generateIncompatiblePropertyValue(final PropertyDefinition propertyDefinition, final Random random) {

		final Class<?> type = propertyDefinition.getType();

		if (type == Boolean.class) {
			return random.nextLong();
		} else if (type == Integer.class) {
			return random.nextBoolean();

		} else if (type == String.class) {
			return random.nextInt();

		} else if (type == Long.class) {
			return "String " + random.nextInt();
		} else {
			throw new RuntimeException("unknown type " + type);
		}
	}

	/*
	 * Generates a property value consistent with the property definition that
	 * is not equal to the definition's default value.
	 */
	private static Object generatePropertyValue(final PropertyDefinition propertyDefinition, final Random random) {

		final Class<?> type = propertyDefinition.getType();
		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new RuntimeException("requires a property definition with a non-null default value");
		}

		Object defaultValue = propertyDefinition.getDefaultValue().get();
		Object result = defaultValue;
		while (result.equals(defaultValue)) {
			if (type == Boolean.class) {
				result = random.nextBoolean();

			} else if (type == Integer.class) {
				result = random.nextInt();

			} else if (type == String.class) {
				result = "String " + random.nextInt();

			} else if (type == Long.class) {
				result = random.nextLong();

			} else {
				throw new RuntimeException("unknown type " + type);
			}
		}
		return result;
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setBatchPropertyValue(BatchId, BatchPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		Random random = new Random(5745690788442345906L);

		BatchId batchId = new BatchId(645778);
		BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_3_1_BOOLEAN_MUTABLE_NO_TRACK;
		PropertyDefinition propertyDefinition = generateRandomPropertyDefinition(random);
		Object propertyValue = generatePropertyValue(propertyDefinition, random);
		Object incompatiblePropertyValue = generateIncompatiblePropertyValue(propertyDefinition, random);
		MaterialId materialId = TestMaterialId.MATERIAL_3;
		double amount = 2341456;
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;

		// precondition: if the batch id is null
		scenarioBuilder.setBatchPropertyValue(null, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch id is unknown
		scenarioBuilder.setBatchPropertyValue(new BatchId(234), batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch property id is null
		scenarioBuilder.setBatchPropertyValue(batchId, null, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the batch property id is unknown
		scenarioBuilder.setBatchPropertyValue(batchId, TestBatchPropertyId.getUnknownBatchPropertyId(), propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, null);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, incompatiblePropertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the batch property value was previously set
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
		scenarioBuilder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
		scenarioBuilder.addMaterial(materialId);
		scenarioBuilder.addBatch(batchId, materialId, amount, materialsProducerId);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the batch has the expected property value
		assertEquals(propertyValue, scenario.getBatchPropertyValue(batchId, batchPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setCompartmentPropertyValue(CompartmentId, CompartmentPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testSetCompartmentPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_4;
		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_4.getCompartmentPropertyId(0);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(15)//
																	.build();//
		Object propertyValue = 77;

		// precondition: if the compartment property id is null
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, null, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment property id is unknown
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, TestCompartmentId.getUnknownCompartmentPropertyId(), propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is null
		scenarioBuilder.setCompartmentPropertyValue(null, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the compartment id is unknown
		scenarioBuilder.setCompartmentPropertyValue(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, null);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, "incompatible value");
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the compartment property value was previously set
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the compartment has the expected property value
		assertEquals(propertyValue, scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setGlobalPropertyValue(GlobalPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		Object propertyValue = "value";
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue("default")//
																	.build();//

		// precondition: if the global property id is null
		scenarioBuilder.setGlobalPropertyValue(null, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the global property id is unknown
		scenarioBuilder.setGlobalPropertyValue(TestGlobalPropertyId.getUnknownGlobalPropertyId(), propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, null);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, 67);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the global property value was previously set
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the global property has the expected value
		assertEquals(propertyValue, scenario.getGlobalPropertyValue(globalPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setGroupPropertyValue(GroupId, GroupPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		GroupId groupId = new GroupId(64);
		GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
		Object propertyValue = 78;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(45)//
																	.build();//
		GroupTypeId groupTypeId = TestGroupTypeId.GROUP_TYPE_1;

		// precondition: if the group id is null
		scenarioBuilder.setGroupPropertyValue(null, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group id is unknown
		scenarioBuilder.setGroupPropertyValue(new GroupId(345), groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group property is null
		scenarioBuilder.setGroupPropertyValue(groupId, null, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group property is unknown
		scenarioBuilder.setGroupPropertyValue(groupId, TestGroupPropertyId.getUnknownGroupPropertyId(), propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, null);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, "incompatible value");
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the group property value was previously set
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);
		scenarioBuilder.addGroup(groupId, groupTypeId);
		scenarioBuilder.addGroupTypeId(groupTypeId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the group has the expected property value
		assertEquals(propertyValue, scenario.getGroupPropertyValue(groupId, groupPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setMaterialsProducerPropertyValue(MaterialsProducerId, MaterialsProducerPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		MaterialsProducerPropertyId materialsProducerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = 45;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(634)//
																	.build();//

		// precondition: if the materials producer property id is null
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer property id is unknown
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, TestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is null
		scenarioBuilder.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is unknown
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, "incompatible value");
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer property value was previously
		// set
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the materials producer has the expected property value
		assertEquals(propertyValue, scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setMaterialsProducerResourceLevel(MaterialsProducerId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
	public void testSetMaterialsProducerResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		TestMaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_1;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		long amount = 234;

		// precondition: if the materials producer id is null
		scenarioBuilder.setMaterialsProducerResourceLevel(null, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer id is unknown
		scenarioBuilder.setMaterialsProducerResourceLevel(TestMaterialsProducerId.getUnknownMaterialsProducerId(), resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is null
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, null, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the amount is negative
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, -234L);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition: if the materials producer resource level was previously
		// set
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId).longValue());
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonCompartmentArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonCompartmentArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonCompartmentArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition: if the trackPersonCompartmentArrivalTimes is null
			scenarioBuilder.setPersonCompartmentArrivalTracking(null);
			ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
			assertEquals(ScenarioErrorType.NULL_COMPARTMENT_TRACKING_POLICY, scenarioException.getScenarioErrorType());

			// precondition: if the compartment arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
			assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

			scenarioBuilder.setPersonCompartmentArrivalTracking(timeTrackingPolicy);
			Scenario scenario = scenarioBuilder.build();
			// postcondition: the scenario has the expected compartment tracking
			// arrival tracking policy
			assertEquals(timeTrackingPolicy, scenario.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonPropertyValue(PersonId, PersonPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(68);
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = 38;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//
		RegionId regionId = TestRegionId.REGION_5;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_3;

		// precondition: if the person id is null
		scenarioBuilder.setPersonPropertyValue(null, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person id is unknown
		scenarioBuilder.setPersonPropertyValue(new PersonId(444), personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person property id is null
		scenarioBuilder.setPersonPropertyValue(personId, null, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person property id is unknown
		scenarioBuilder.setPersonPropertyValue(personId, TestPersonPropertyId.getUnknownPersonPropertyId(), propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, null);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, "incompatible value");
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the person property value was previously set
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the person have the expected property value
		assertEquals(propertyValue, scenario.getPersonPropertyValue(personId, personPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonRegionArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonRegionArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonRegionArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition: if the trackPersonRegionArrivalTimes is null
			scenarioBuilder.setPersonRegionArrivalTracking(null);
			ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
			assertEquals(ScenarioErrorType.NULL_REGION_TRACKING_POLICY, scenarioException.getScenarioErrorType());

			// precondition: if the region arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
			assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

			scenarioBuilder.setPersonRegionArrivalTracking(timeTrackingPolicy);
			Scenario scenario = scenarioBuilder.build();

			// postcondition: the scenario has the expected region arrival
			// tracking policy
			assertEquals(timeTrackingPolicy, scenario.getPersonRegionArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setPersonResourceLevel(PersonId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonResourceLevel", args = { PersonId.class, ResourceId.class, long.class })
	public void testSetPersonResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		PersonId personId = new PersonId(76);
		ResourceId resourceId = TestResourceId.RESOURCE_2;
		long amount = 3453L;
		RegionId regionId = TestRegionId.REGION_4;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_2;

		// precondition: if the person id is null
		scenarioBuilder.setPersonResourceLevel(null, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person id is unknown
		scenarioBuilder.setPersonResourceLevel(new PersonId(88), resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is null
		scenarioBuilder.setPersonResourceLevel(personId, null, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.setPersonResourceLevel(personId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the amount is negative
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, -321L);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition: if the person resource level was previously set
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the person has the expected resource level
		assertEquals(amount, scenario.getPersonResourceLevel(personId, resourceId).longValue());
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setRegionPropertyValue(RegionId, RegionPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		RegionId regionId = TestRegionId.REGION_3;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_7;
		Object propertyValue = 88;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(789)//
																	.build();//

		// precondition: if the region id is null
		scenarioBuilder.setRegionPropertyValue(null, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region id is unknown
		scenarioBuilder.setRegionPropertyValue(TestRegionId.getUnknownRegionId(), regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region property id is null
		scenarioBuilder.setRegionPropertyValue(regionId, null, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region property id is unknown
		scenarioBuilder.setRegionPropertyValue(regionId, TestRegionPropertyId.getUnknownRegionPropertyId(), propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, null);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, "incompatible value");
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the region property value was previously set
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the region has the expected property value
		assertEquals(propertyValue, scenario.getRegionPropertyValue(regionId, regionPropertyId));

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setRegionResourceLevel(RegionId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionResourceLevel", args = { RegionId.class, ResourceId.class, long.class })
	public void testSetRegionResourceLevel() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		RegionId regionId = TestRegionId.REGION_6;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long amount = 345;

		// precondition: if the region id is null
		scenarioBuilder.setRegionResourceLevel(null, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the region id is unknown
		scenarioBuilder.setRegionResourceLevel(TestRegionId.getUnknownRegionId(), resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is null
		scenarioBuilder.setRegionResourceLevel(regionId, null, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.setRegionResourceLevel(regionId, TestResourceId.getUnknownResourceId(), amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the amount is negative
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, -12312L);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition: if the region resource level was previously set
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the region has the expected resource amount
		assertEquals(amount, scenario.getRegionResourceLevel(regionId, resourceId).longValue());

	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setResourcePropertyValue(ResourceId, ResourcePropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		ResourcePropertyId resourcePropertyId = TestResourcePropertyId.ResourceProperty_5_1_DOUBLE_IMMUTABLE;
		Object propertyValue = 534;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//

		// precondition: if the resource id is null
		scenarioBuilder.setResourcePropertyValue(null, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.setResourcePropertyValue(TestResourceId.getUnknownResourceId(), resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource property id is null
		scenarioBuilder.setResourcePropertyValue(resourceId, null, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource property id is unknown
		scenarioBuilder.setResourcePropertyValue(resourceId, TestResourcePropertyId.getUnknownResourcePropertyId(), propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, null);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the value is not compatible with the property
		// definition
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, "incompatible value");
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition: if the resource property value was previously set
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();

		// postcondition: the resource has the expected resource property value
		assertEquals(propertyValue, scenario.getResourcePropertyValue(resourceId, resourcePropertyId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#setResourceTimeTracking(ResourceId, TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setResourceTimeTracking", args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testSetResourceTimeTracking() {
		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.DO_NOT_TRACK_TIME;

		// precondition: if the resource id is null
		scenarioBuilder.setResourceTimeTracking(null, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource id is unknown
		scenarioBuilder.setResourceTimeTracking(TestResourceId.getUnknownResourceId(), timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the trackValueAssignmentTimes is null
		scenarioBuilder.setResourceTimeTracking(resourceId, null);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RESOURCE_TRACKING_POLICY, scenarioException.getScenarioErrorType());

		// precondition: if the resource TimeTrackingPolicy was previously set
		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
		scenarioBuilder.addResource(resourceId);
		Scenario scenario = scenarioBuilder.build();
		// postcondition: the resource has the expected time tracking policy
		// value
		assertEquals(timeTrackingPolicy, scenario.getPersonResourceTimeTrackingPolicy(resourceId));
	}

	/**
	 * Tests
	 * {@link UnstructuredScenarioBuilder#addRandomNumberGeneratorId(RandomNumberGeneratorId)}
	 */
	@Test
	@UnitTestMethod(name = "addRandomNumberGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomNumberGeneratorId() {

		ScenarioBuilder scenarioBuilder = new UnstructuredScenarioBuilder();

		// precondition : if the generator id is null
		scenarioBuilder.addRandomNumberGeneratorId(null);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID, scenarioException.getScenarioErrorType());

		// precondition : if the generator id was previously added
		scenarioBuilder.addRandomNumberGeneratorId(TestRandomGeneratorId.BLITZEN);
		scenarioBuilder.addRandomNumberGeneratorId(TestRandomGeneratorId.BLITZEN);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.build());
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		Set<RandomNumberGeneratorId> expected = new LinkedHashSet<>();
		expected.add(TestRandomGeneratorId.COMET);
		expected.add(TestRandomGeneratorId.CUPID);
		expected.add(TestRandomGeneratorId.DONNER);
		expected.add(TestRandomGeneratorId.BLITZEN);

		for (RandomNumberGeneratorId randomNumberGeneratorId : expected) {
			scenarioBuilder.addRandomNumberGeneratorId(randomNumberGeneratorId);
		}

		Scenario scenario = scenarioBuilder.build();

		// postcondition: the scenario contains the expected ids
		Set<RandomNumberGeneratorId> actual = scenario.getRandomNumberGeneratorIds();
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link UnstructuredScenarioBuilder#UnstructuredScenarioBuilder()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		UnstructuredScenarioBuilder unstructuredScenarioBuilder = new UnstructuredScenarioBuilder();
		assertNotNull(unstructuredScenarioBuilder);
	}
}
