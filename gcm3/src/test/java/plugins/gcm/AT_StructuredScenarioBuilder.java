package plugins.gcm;

//import static gcmtest.plugins.support.ExceptionAssertion.assertScenarioException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
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
import plugins.gcm.input.StructuredScenarioBuilder;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.globals.testsupport.TestGlobalComponentId;
import plugins.globals.testsupport.TestGlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.XTestGroupTypeId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
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
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import plugins.support.XTestMaterialId;
import plugins.support.XTestMaterialsProducerId;
import plugins.support.XTestMaterialsProducerPropertyId;
import plugins.support.XTestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link StructuredScenarioBuilder}
 *
 * @author Shawn Hatch
 *
 */
@UnitTest(target = StructuredScenarioBuilder.class)
public class AT_StructuredScenarioBuilder {

	/*
	 * A placeholder implementation to satisfy scenario construction
	 */
	private static class PlaceholderComponent extends AbstractComponent {

	}

	/**
	 * Tests {@link StructuredScenarioBuilder#StructuredScenarioBuilder()}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		StructuredScenarioBuilder structuredScenarioBuilder = new StructuredScenarioBuilder();
		assertNotNull(structuredScenarioBuilder);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addBatch(BatchId, MaterialId, double, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addBatch", args = { BatchId.class, MaterialId.class, double.class, MaterialsProducerId.class })
	public void testAddBatch() {

		// identifiers and values
		Double amount = 4.0;
		BatchId batchId1 = new BatchId(1);
		BatchId batchId2 = new BatchId(2);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if the batch id is null
		scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(null, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());
		// precondition : ModelException thrown if the material does not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, null, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.getUnknownMaterialId(), amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the amount is negative
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, -1.0, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NEGATIVE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the amount is not finite
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, Double.NEGATIVE_INFINITY, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, Double.POSITIVE_INFINITY, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, Double.NaN, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NON_FINITE_MATERIAL_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the materials producer does
		// not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, amount, null));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.getUnknownMaterialsProducerId()));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the batch id was previously
		// added
		scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// postcondition : the batch has the expected amount
		scenarioBuilder.addBatch(batchId2, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getBatchAmount(batchId2));

		// postcondition : the batch has the expected material
		assertEquals(XTestMaterialId.MATERIAL_1, scenario.getBatchMaterial(batchId2));

		// postcondition : the batch is owned by the correct materials producer
		assertEquals(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, scenario.getBatchMaterialsProducer(batchId2));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addBatchToStage(StageId, BatchId)}
	 */

	@Test
	@UnitTestMethod(name = "addBatchToStage", args = { StageId.class, BatchId.class })
	public void testAddBatchToStage() {
		// identifiers and values
		BatchId batchId = new BatchId(5);
		StageId stageId1 = new StageId(7);
		StageId stageId2 = new StageId(8);
		StageId stageId3 = new StageId(9);
		Double amount = 4.0;

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if the stage does not exist
		scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		scenarioBuilder.addStage(stageId1, false, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);

		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(null, batchId));
		assertEquals(ScenarioErrorType.NULL_STAGE_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(new StageId(55), batchId));
		assertEquals(ScenarioErrorType.UNKNOWN_STAGE_ID, scenarioException.getScenarioErrorType());
		// precondition : ModelException thrown if the batch does not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(stageId1, null));
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(stageId1, new BatchId(99)));
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_ID, scenarioException.getScenarioErrorType());
		// precondition : ModelException thrown if the stage and batch are not
		// associated with the same
		// materials producer
		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_2, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addStage(stageId2, false, XTestMaterialsProducerId.MATERIALS_PRODUCER_2);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(stageId2, batchId));
		assertEquals(ScenarioErrorType.BATCH_STAGED_TO_DIFFERENT_OWNER, scenarioException.getScenarioErrorType());
		// precondition : ModelException thrown if the batch is already
		// associated with any stage
		scenarioBuilder.addBatchToStage(stageId1, batchId);
		scenarioBuilder.addStage(stageId3, false, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(stageId3, batchId));
		assertEquals(ScenarioErrorType.BATCH_ALREADY_STAGED, scenarioException.getScenarioErrorType());
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addBatchToStage(stageId1, batchId));
		assertEquals(ScenarioErrorType.BATCH_ALREADY_STAGED, scenarioException.getScenarioErrorType());
		// postcondition : the batch is on the stage
		Scenario scenario = scenarioBuilder.build();
		Set<BatchId> stageBatches = scenario.getStageBatches(stageId1);
		assertTrue(stageBatches.contains(batchId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addCompartmentId(CompartmentId, Class)}
	 */

	@Test
	@UnitTestMethod(name = "addCompartmentId", args = { CompartmentId.class, Class.class })
	public void testAddCompartmentId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : compartment id cannot be null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addCompartmentId(null, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());
		// precondition : comparmentComponentClass cannot be null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, null));
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());
		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, () -> new PlaceholderComponent()::init);

		// precondition : compartment id cannot duplicate previous compartment
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition : scenario contains only the compartment added
		Scenario scenario = scenarioBuilder.build();
		Set<CompartmentId> compartmentIds = scenario.getCompartmentIds();
		assertTrue(compartmentIds.size() == 1);
		assertTrue(compartmentIds.contains(TestCompartmentId.COMPARTMENT_1));
		assertNotNull(scenario.getCompartmentInitialBehaviorSupplier(TestCompartmentId.COMPARTMENT_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addGlobalComponentId(GlobalComponentId, Class)}
	 */

	@Test
	@UnitTestMethod(name = "addGlobalComponentId", args = { GlobalComponentId.class, Class.class })
	public void testAddGlobalComponentId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : global component id cannot be null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGlobalComponentId(null, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());
		// precondition : globalComponentClass cannot be null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, null));
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());
		scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, () -> new PlaceholderComponent()::init);

		// precondition : global component id cannot duplicate previous global
		// component id
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGlobalComponentId(TestGlobalComponentId.GLOBAL_COMPONENT_1, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition : scenario contains only the global component id added
		Scenario scenario = scenarioBuilder.build();
		Set<GlobalComponentId> globalComponentIds = scenario.getGlobalComponentIds();
		assertTrue(globalComponentIds.size() == 1);
		assertTrue(globalComponentIds.contains(TestGlobalComponentId.GLOBAL_COMPONENT_1));
		assertNotNull(scenario.getGlobalInitialBehaviorSupplier(TestGlobalComponentId.GLOBAL_COMPONENT_1));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addGroup(GroupId, GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroup", args = { GroupId.class, GroupTypeId.class })
	public void testAddGroup() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		GroupId groupId = new GroupId(45);

		// precondition : if the group id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroup(null, XTestGroupTypeId.GROUP_TYPE_1));
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());
		// precondition : the group type must have been previously added
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroup(groupId, null));
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());
		// precondition : the group type must have been previously added
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroup(groupId, XTestGroupTypeId.GROUP_TYPE_1));
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addGroupTypeId(XTestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addGroup(groupId, XTestGroupTypeId.GROUP_TYPE_1);

		// precondition : the group must not have been previously added
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroup(groupId, XTestGroupTypeId.GROUP_TYPE_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition : the group exists and is of the correct type
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupIds().size() == 1);
		assertTrue(scenario.getGroupIds().contains(groupId));
		assertEquals(XTestGroupTypeId.GROUP_TYPE_1, scenario.getGroupTypeId(groupId));

	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addGroupTypeId(GroupTypeId)}
	 */
	@Test
	@UnitTestMethod(name = "addGroupTypeId", args = { GroupTypeId.class })
	public void testAddGroupTypeId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : the group type is not null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroupTypeId(null));
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addGroupTypeId(XTestGroupTypeId.GROUP_TYPE_1);

		// precondition : the group must not have been previously added
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addGroupTypeId(XTestGroupTypeId.GROUP_TYPE_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition : the group type exists
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupTypeIds().size() == 1);
		assertTrue(scenario.getGroupTypeIds().contains(XTestGroupTypeId.GROUP_TYPE_1));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addMaterial(MaterialId)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterial", args = { MaterialId.class })
	public void testAddMaterial() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : the material id is not null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addMaterial(null));
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1);

		// precondition : the material id was not previouslyAdded
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition: the material id is the the single material in the
		// scenario
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getMaterialIds().size() == 1);
		assertTrue(scenario.getMaterialIds().contains(XTestMaterialId.MATERIAL_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addMaterialsProducerId(MaterialsProducerId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addMaterialsProducerId", args = { MaterialsProducerId.class, Class.class })
	public void testAddMaterialsProducerId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : materials producer id cannot be null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addMaterialsProducerId(null, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());
		// precondition : materialsProducerComponentClass cannot be null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, null));
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());
		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init);

		// precondition : materials producer id cannot duplicate previous
		// materials producer id
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());
		// postcondition : scenario contains only the materials producer id
		// added
		Scenario scenario = scenarioBuilder.build();
		Set<MaterialsProducerId> materialsProducerIds = scenario.getMaterialsProducerIds();
		assertTrue(materialsProducerIds.size() == 1);
		assertTrue(materialsProducerIds.contains(XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertNotNull(scenario.getMaterialsProducerInitialBehaviorSupplier(XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addPerson(PersonId, RegionId, CompartmentId)}
	 */
	@Test
	@UnitTestMethod(name = "addPerson", args = { PersonId.class, RegionId.class, CompartmentId.class })
	public void testAddPerson() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		PersonId personId = new PersonId(45);
		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(TestRegionId.REGION_1, () -> new PlaceholderComponent()::init);

		// precondition : if the person id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(null, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1));
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());
		// precondition : if the region id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(personId, null, TestCompartmentId.COMPARTMENT_1));
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());
		// precondition : if the region id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_2, TestCompartmentId.COMPARTMENT_1));
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());
		// precondition : if the compartment id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, null));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());
		// precondition : if the compartment id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_2));
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());
		// precondition : if the person was previously added
		scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// postcondition : the person is in the scenario and has the expected
		// region and compartment assignments
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getPeopleIds().size() == 1);
		assertTrue(scenario.getPeopleIds().contains(personId));
		assertEquals(TestCompartmentId.COMPARTMENT_1, scenario.getPersonCompartment(personId));
		assertEquals(TestRegionId.REGION_1, scenario.getPersonRegion(personId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addPersonToGroup(GroupId, PersonId)}
	 */
	@Test
	@UnitTestMethod(name = "addPersonToGroup", args = { GroupId.class, PersonId.class })
	public void testAddPersonToGroup() {
		PersonId personId = new PersonId(1);
		GroupId groupId = new GroupId(2);

		PersonId placeHolderPersonId = new PersonId(3);
		GroupId placeHolderGroupId = new GroupId(4);

		GroupId unknownGroupId = new GroupId(5);
		PersonId unknownPersonId = new PersonId(6);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		// establish some place holders so that the precondition tests each have
		// exactly one expected failure point
		scenarioBuilder.addRegionId(TestRegionId.REGION_1, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(TestCompartmentId.COMPARTMENT_1, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addGroupTypeId(XTestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addGroup(placeHolderGroupId, XTestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addPerson(placeHolderPersonId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);

		// precondition: if the group id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPersonToGroup(null, placeHolderPersonId));
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPersonToGroup(placeHolderGroupId, null));
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		// precondition: if the group id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPersonToGroup(unknownGroupId, placeHolderPersonId));
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_ID, scenarioException.getScenarioErrorType());

		// precondition: if the person id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPersonToGroup(placeHolderGroupId, unknownPersonId));
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGroup(groupId, XTestGroupTypeId.GROUP_TYPE_1);
		scenarioBuilder.addPerson(personId, TestRegionId.REGION_1, TestCompartmentId.COMPARTMENT_1);
		scenarioBuilder.addPersonToGroup(groupId, personId);

		// precondition: if the person was previously added to the group
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addPersonToGroup(groupId, personId));
		assertEquals(ScenarioErrorType.DUPLICATE_GROUP_MEMBERSHIP, scenarioException.getScenarioErrorType());

		// postcondition : the person is a member of the group
		Scenario scenario = scenarioBuilder.build();
		assertTrue(scenario.getGroupMembers(groupId).contains(personId));
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addRegionId(RegionId, Class)}
	 */
	@Test
	@UnitTestMethod(name = "addRegionId", args = { RegionId.class, Class.class })
	public void testAddRegionId() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : region id cannot be null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addRegionId(null, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.NULL_COMPONENT_IDENTIFIER, scenarioException.getScenarioErrorType());

		// precondition : regionComponentClass cannot be null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addRegionId(TestRegionId.REGION_1, null));
		assertEquals(ScenarioErrorType.NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER, scenarioException.getScenarioErrorType());

		scenarioBuilder.addRegionId(TestRegionId.REGION_1, () -> new PlaceholderComponent()::init);

		// precondition : region id cannot duplicate previous global
		// component id
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addRegionId(TestRegionId.REGION_1, () -> new PlaceholderComponent()::init));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// postcondition : scenario contains only the region id added
		Scenario scenario = scenarioBuilder.build();
		Set<RegionId> regionIds = scenario.getRegionIds();
		assertTrue(regionIds.size() == 1);
		assertTrue(regionIds.contains(TestRegionId.REGION_1));
		assertNotNull(scenario.getRegionInitialBehaviorSupplier(TestRegionId.REGION_1));

	}

	/**
	 * Tests {@link StructuredScenarioBuilder#addResource(ResourceId)}
	 */
	@Test
	@UnitTestMethod(name = "addResource", args = { ResourceId.class })
	public void testAddResource() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition: if the resource id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addResource(null));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition: if the resource was previously added
		scenarioBuilder.addResource(XTestResourceId.RESOURCE1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addResource(XTestResourceId.RESOURCE1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		Scenario scenario = scenarioBuilder.build();
		Set<ResourceId> resourceIds = scenario.getResourceIds();
		assertTrue(resourceIds.size() == 1);
		assertTrue(resourceIds.contains(XTestResourceId.RESOURCE1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addStage(StageId, boolean, MaterialsProducerId)}
	 */
	@Test
	@UnitTestMethod(name = "addStage", args = { StageId.class, boolean.class, MaterialsProducerId.class })
	public void testAddStage() {

		StageId stageId1 = new StageId(50);
		StageId stageId2 = new StageId(13);

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : ModelException thrown if if the stage id is null
		scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1);
		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init);
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addStage(null, true, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.NULL_STAGE_ID, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if if the materials producer
		// does not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addStage(stageId1, true, null));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addStage(stageId1, true, XTestMaterialsProducerId.getUnknownMaterialsProducerId()));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if if the stage id was
		// previously added
		scenarioBuilder.addStage(stageId2, false, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addStage(stageId2, true, XTestMaterialsProducerId.MATERIALS_PRODUCER_1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		// postcondition : the stage has the offered state
		scenarioBuilder.addStage(stageId1, true, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);
		Scenario scenario = scenarioBuilder.build();
		assertEquals(true, scenario.isStageOffered(stageId1));
		assertEquals(false, scenario.isStageOffered(stageId2));

		// postcondition : the stage is owned by the correct materials producer
		assertEquals(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, scenario.getStageMaterialsProducer(stageId1));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineBatchProperty(MaterialId, BatchPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineBatchProperty", args = { MaterialId.class, BatchPropertyId.class, PropertyDefinition.class })
	public void testDefineBatchProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		XTestMaterialId xTestMaterialId = XTestMaterialId.MATERIAL_1;
		scenarioBuilder.addMaterial(xTestMaterialId);
		BatchPropertyId batchPropertyId = xTestMaterialId.getBatchPropertyIds()[0];
		String defaultValue = "Default";
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(String.class)//
																	.setDefaultValue(defaultValue)//
																	.build();//

		// precondition : if the material id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineBatchProperty(null, batchPropertyId, propertyDefinition));
		assertEquals(ScenarioErrorType.NULL_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition : if the material id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineBatchProperty(XTestMaterialId.getUnknownMaterialId(), batchPropertyId, propertyDefinition));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIAL_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineBatchProperty(xTestMaterialId, null, propertyDefinition));
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineBatchProperty(xTestMaterialId, batchPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition : if the batch property was previously defined
		scenarioBuilder.defineBatchProperty(xTestMaterialId, batchPropertyId, propertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineBatchProperty(xTestMaterialId, batchPropertyId, propertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_BATCH_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getBatchPropertyDefinition(xTestMaterialId, batchPropertyId);
		assertEquals(propertyDefinition, outputPropertyDefinition);

	}

	/*
	 * A Component implementor that acts as a placeholder for components not
	 * expected to execute activities relevant to the tests.
	 */
	public static class EmptyComponent extends AbstractComponent {

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineCompartmentProperty(CompartmentId, CompartmentPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineCompartmentProperty", args = { CompartmentId.class, CompartmentPropertyId.class, PropertyDefinition.class })
	public void testDefineCompartmentProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		String defaultValue = "Default";
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue(defaultValue)//
																		.build();//
		scenarioBuilder.addCompartmentId(compartmentId, () -> new EmptyComponent()::init);

		// precondition : if the compartment id is unknown
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineCompartmentProperty(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition : if the compartment id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineCompartmentProperty(null, compartmentPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property id is null

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineCompartmentProperty(compartmentId, null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property definition is null

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition : if the compartment property was previously defined

		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineGlobalProperty(GlobalPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGlobalProperty", args = { GlobalPropertyId.class, PropertyDefinition.class })
	public void testDefineGlobalProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		String defaultValue = "Default";
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(String.class)//
																		.setDefaultValue(defaultValue)//
																		.build();//

		// precondition : if the property id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGlobalProperty(null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGlobalProperty(globalPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition : if the compartment property was previously defined
		scenarioBuilder.defineGlobalProperty(globalPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGlobalProperty(globalPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_GLOBAL_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getGlobalPropertyDefinition(globalPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineGroupProperty(GroupTypeId, GroupPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineGroupProperty", args = { GroupTypeId.class, GroupPropertyId.class, PropertyDefinition.class })
	public void testDefineGroupProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GroupPropertyId groupPropertyId = XTestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
		GroupTypeId groupTypeId = XTestGroupTypeId.GROUP_TYPE_1;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the group type id is not defined
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGroupProperty(null, groupPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_TYPE_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGroupTypeId(groupTypeId);
		// precondition : if the property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGroupProperty(groupTypeId, null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// precondition : if the group property was previously defined
		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_GROUP_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineMaterialsProducerProperty(MaterialsProducerPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineMaterialsProducerProperty", args = { MaterialsProducerPropertyId.class, PropertyDefinition.class })
	public void testDefineMaterialsProducerProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerPropertyId materialsProducerPropertyId = XTestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_1;

		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineMaterialsProducerProperty(null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());
		// precondition : if the materials producer property was previously
		// defined
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());
		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#definePersonProperty(PersonPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "definePersonProperty", args = { PersonPropertyId.class, PropertyDefinition.class })
	public void testDefinePersonProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.definePersonProperty(null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());
		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.definePersonProperty(personPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());
		// precondition : if the person property was previously defined
		scenarioBuilder.definePersonProperty(personPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.definePersonProperty(personPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_PERSON_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getPersonPropertyDefinition(personPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineRegionProperty(RegionPropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineRegionProperty", args = { RegionPropertyId.class, PropertyDefinition.class })
	public void testDefineRegionProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		// precondition : if the property id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineRegionProperty(null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());
		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineRegionProperty(regionPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());
		// precondition : if the region property was previously defined
		scenarioBuilder.defineRegionProperty(regionPropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineRegionProperty(regionPropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_REGION_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getRegionPropertyDefinition(regionPropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#defineResourceProperty(ResourceId, ResourcePropertyId, PropertyDefinition)}
	 */
	@Test
	@UnitTestMethod(name = "defineResourceProperty", args = { ResourceId.class, ResourcePropertyId.class, PropertyDefinition.class })
	public void testDefineResourceProperty() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		ResourceId resourceId = XTestResourceId.RESOURCE5;
		ResourcePropertyId resourcePropertyId = XTestResourceId.RESOURCE5.getResourcePropertyIds()[0];
		PropertyDefinition inputPropertyDefinition = PropertyDefinition	.builder()//
																		.setType(Integer.class)//
																		.setDefaultValue(15)//
																		.build();//

		scenarioBuilder.addResource(resourceId);

		// precondition : if the resource id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineResourceProperty(null, resourcePropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());
		// precondition : if the resource id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineResourceProperty(XTestResourceId.getUnknownResourceId(), resourcePropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineResourceProperty(resourceId, null, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());
		// precondition : if the property definition is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, null));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());
		// precondition : if the resource property was previously defined
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, inputPropertyDefinition);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, inputPropertyDefinition));
		assertEquals(ScenarioErrorType.DUPLICATE_RESOURCE_PROPERTY_DEFINITION, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		PropertyDefinition outputPropertyDefinition = scenario.getResourcePropertyDefinition(resourceId, resourcePropertyId);
		assertEquals(inputPropertyDefinition, outputPropertyDefinition);
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setBatchPropertyValue(BatchId, BatchPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setBatchPropertyValue", args = { BatchId.class, BatchPropertyId.class, Object.class })
	public void testSetBatchPropertyValue() {
		BatchId batchId1 = new BatchId(1);
		BatchId batchId2 = new BatchId(2);
		Double propertyValue1 = 4.7;
		Integer propertyValue2 = 5;
		Double amount = 5.6;

		BatchPropertyId batchPropertyId1 = XTestMaterialId.MATERIAL_1.getBatchPropertyIds()[0];
		BatchPropertyId batchPropertyId2 = XTestMaterialId.MATERIAL_1.getBatchPropertyIds()[1];

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		scenarioBuilder.addMaterial(XTestMaterialId.MATERIAL_1);
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Double.class)//
																	.setDefaultValue(17.5)//
																	.build();//
		scenarioBuilder.defineBatchProperty(XTestMaterialId.MATERIAL_1, batchPropertyId1, propertyDefinition);

		scenarioBuilder.addMaterialsProducerId(XTestMaterialsProducerId.MATERIALS_PRODUCER_1, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addBatch(batchId1, XTestMaterialId.MATERIAL_1, amount, XTestMaterialsProducerId.MATERIALS_PRODUCER_1);

		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(null, batchPropertyId1, propertyValue1));
		assertEquals(ScenarioErrorType.NULL_BATCH_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId2, batchPropertyId1, propertyValue1));
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_ID, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the batch property is not
		// defined
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId1, null, propertyValue1));
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId2, propertyValue1));
		assertEquals(ScenarioErrorType.UNKNOWN_BATCH_PROPERTY_ID, scenarioException.getScenarioErrorType());
		// precondition : ModelException thrown if the value is not compatible
		// with the property definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue2));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, null));
		assertEquals(ScenarioErrorType.NULL_BATCH_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : ModelException thrown if the batch property value was
		// previously set
		scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue1);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setBatchPropertyValue(batchId1, batchPropertyId1, propertyValue1));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition : the batch has the expected property value
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue1, scenario.getBatchPropertyValue(batchId1, batchPropertyId1));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setCompartmentPropertyValue(CompartmentId, CompartmentPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setCompartmentPropertyValue", args = { CompartmentId.class, CompartmentPropertyId.class, Object.class })
	public void testSetCompartmentPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);

		CompartmentPropertyId compartmentPropertyId = TestCompartmentId.COMPARTMENT_1.getCompartmentPropertyId(0);
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		scenarioBuilder.defineCompartmentProperty(compartmentId, compartmentPropertyId, propertyDefinition);

		// precondition : if the compartment property id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the compartment property id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, TestCompartmentId.getUnknownCompartmentPropertyId(), propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the compartment id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(null, compartmentPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition : if the compartment id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(TestCompartmentId.getUnknownCompartmentId(), compartmentPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_COMPARTMENT_ID, scenarioException.getScenarioErrorType());

		// precondition : if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_COMPARTMENT_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the compartment property value was previously set
		scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setCompartmentPropertyValue(compartmentId, compartmentPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getCompartmentPropertyValue(compartmentId, compartmentPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setGlobalPropertyValue(GlobalPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGlobalPropertyValue", args = { GlobalPropertyId.class, Object.class })
	public void testSetGlobalPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GlobalPropertyId globalPropertyId = TestGlobalPropertyId.Global_Property_1;
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//

		// precondition : if the global property is not a defined
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGlobalPropertyValue(null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_GLOBAL_PROPERTY_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.defineGlobalProperty(globalPropertyId, propertyDefinition);

		// precondition : if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_GLOBAL_PROPERTY_VALUE, scenarioException.getScenarioErrorType());
		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());
		// precondition : if the global property value was previously set
		scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGlobalPropertyValue(globalPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());
		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getGlobalPropertyValue(globalPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setGroupPropertyValue(GroupId, GroupPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setGroupPropertyValue", args = { GroupId.class, GroupPropertyId.class, Object.class })
	public void testSetGroupPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		GroupPropertyId groupPropertyId = XTestGroupTypeId.GROUP_TYPE_1.getGroupPropertyIds()[0];
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		GroupId groupId = new GroupId(7);
		GroupTypeId groupTypeId = XTestGroupTypeId.GROUP_TYPE_1;

		// precondition : if the group id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(null, groupPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_GROUP_ID, scenarioException.getScenarioErrorType());
		// precondition : if the group id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addGroupTypeId(groupTypeId);
		scenarioBuilder.addGroup(groupId, groupTypeId);

		// precondition : if the group property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the group property id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_GROUP_PROPERTY_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.defineGroupProperty(groupTypeId, groupPropertyId, propertyDefinition);

		// precondition : if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_GROUP_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the group property value was previously set
		scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setGroupPropertyValue(groupId, groupPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getGroupPropertyValue(groupId, groupPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setMaterialsProducerPropertyValue(MaterialsProducerId, MaterialsProducerPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerPropertyValue", args = { MaterialsProducerId.class, MaterialsProducerPropertyId.class, Object.class })
	public void testSetMaterialsProducerPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerPropertyId materialsProducerPropertyId = XTestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2;
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		MaterialsProducerId materialsProducerId = XTestMaterialsProducerId.MATERIALS_PRODUCER_1;
		scenarioBuilder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);

		// precondition : if the materials producer id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(null, materialsProducerPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition : if the materials producer id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);

		// precondition : if the materials property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the materials property id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, XTestMaterialsProducerPropertyId.getUnknownMaterialsProducerPropertyId(), propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the materials producer property value was
		// previously set
		scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setMaterialsProducerResourceLevel(MaterialsProducerId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setMaterialsProducerResourceLevel", args = { MaterialsProducerId.class, ResourceId.class, long.class })
	public void testSetMaterialsProducerResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		MaterialsProducerId materialsProducerId = XTestMaterialsProducerId.MATERIALS_PRODUCER_1;
		Long amount = 2578L;
		ResourceId resourceId = XTestResourceId.RESOURCE1;

		// precondition : if the materials producer id null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(null, resourceId, amount));
		assertEquals(ScenarioErrorType.NULL_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		// precondition : if the materials producer id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_MATERIALS_PRODUCER_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addMaterialsProducerId(materialsProducerId, () -> new PlaceholderComponent()::init);

		// precondition : if the resource does not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, null, amount));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, -1L));
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition : if the materials producer resource level was
		// previously set
		scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getMaterialsProducerResourceLevel(materialsProducerId, resourceId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonCompartmentArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonCompartmentArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonCompartmentArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		for (TimeTrackingPolicy trackPersonCompartmentArrivalTimes : TimeTrackingPolicy.values()) {

			// precondition : if the trackPersonCompartmentArrivalTimes is null
			ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonCompartmentArrivalTracking(null));
			assertEquals(ScenarioErrorType.NULL_COMPARTMENT_TRACKING_POLICY, scenarioException.getScenarioErrorType());

			// precondition : if the compartment arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonCompartmentArrivalTracking(trackPersonCompartmentArrivalTimes);
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonCompartmentArrivalTracking(trackPersonCompartmentArrivalTimes));
			assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(trackPersonCompartmentArrivalTimes, scenario.getPersonCompartmentArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonPropertyValue(PersonId, PersonPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonPropertyValue", args = { PersonId.class, PersonPropertyId.class, Object.class })
	public void testSetPersonPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		PersonId personId = new PersonId(25);

		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;
		PersonPropertyId personPropertyId = TestPersonPropertyId.PERSON_PROPERTY_1_BOOLEAN_MUTABLE_NO_TRACK;
		Object propertyValue = 17;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//

		// precondition : if the person does not exist
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(null, personPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);

		// precondition : if the person property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the person property id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_PROPERTY_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.definePersonProperty(personPropertyId, propertyDefinition);

		// precondition : if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_PERSON_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the person property value was previously set
		scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonPropertyValue(personId, personPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getPersonPropertyValue(personId, personPropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonRegionArrivalTracking(TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonRegionArrivalTracking", args = { TimeTrackingPolicy.class })
	public void testSetPersonRegionArrivalTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		for (TimeTrackingPolicy trackPersonCompartmentArrivalTimes : TimeTrackingPolicy.values()) {

			// precondition : if the trackPersonRegionArrivalTimes is null
			ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonRegionArrivalTracking(null));
			assertEquals(ScenarioErrorType.NULL_REGION_TRACKING_POLICY, scenarioException.getScenarioErrorType());

			// precondition : if the region arrival TimeTrackingPolicy was
			// previously set
			scenarioBuilder.setPersonRegionArrivalTracking(trackPersonCompartmentArrivalTimes);
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonRegionArrivalTracking(trackPersonCompartmentArrivalTimes));
			assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());
			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(trackPersonCompartmentArrivalTimes, scenario.getPersonRegionArrivalTrackingPolicy());
		}
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setPersonResourceLevel(PersonId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setPersonResourceLevel", args = { PersonId.class, ResourceId.class, long.class })
	public void testSetPersonResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		Long amount = 2578L;
		ResourceId resourceId = XTestResourceId.RESOURCE1;
		PersonId personId = new PersonId(534);
		RegionId regionId = TestRegionId.REGION_1;
		CompartmentId compartmentId = TestCompartmentId.COMPARTMENT_1;

		// precondition : if the person id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(null, resourceId, amount));
		assertEquals(ScenarioErrorType.NULL_PERSON_ID, scenarioException.getScenarioErrorType());
		// precondition : if the person id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_PERSON_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addCompartmentId(compartmentId, () -> new PlaceholderComponent()::init);
		scenarioBuilder.addPerson(personId, regionId, compartmentId);

		// precondition : if the resource id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(personId, null, amount));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the resource id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, -1L));
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition : if the person resource level was previously set
		scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setPersonResourceLevel(personId, resourceId, amount));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getPersonResourceLevel(personId, resourceId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setRegionPropertyValue(RegionId, RegionPropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionPropertyValue", args = { RegionId.class, RegionPropertyId.class, Object.class })
	public void testSetRegionPropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		RegionId regionId = TestRegionId.REGION_1;
		RegionPropertyId regionPropertyId = TestRegionPropertyId.REGION_PROPERTY_1;
		Object propertyValue = 17;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(12)//
																	.build();//

		// precondition : if the region id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(null, regionPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());

		// precondition : if the region id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());
		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);

		// precondition : if the region property id is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the region property id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_PROPERTY_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.defineRegionProperty(regionPropertyId, propertyDefinition);

		// precondition : if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, null));
		assertEquals(ScenarioErrorType.NULL_REGION_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the region property value was previously set
		scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionPropertyValue(regionId, regionPropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getRegionPropertyValue(regionId, regionPropertyId));

	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setRegionResourceLevel(RegionId, ResourceId, long)}
	 */
	@Test
	@UnitTestMethod(name = "setRegionResourceLevel", args = { RegionId.class, ResourceId.class, long.class })
	public void testSetRegionResourceLevel() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		Long amount = 2578L;
		ResourceId resourceId = XTestResourceId.RESOURCE1;
		RegionId regionId = TestRegionId.REGION_1;

		// precondition : if the region does not exist
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(null, resourceId, amount));
		assertEquals(ScenarioErrorType.NULL_REGION_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_REGION_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addRegionId(regionId, () -> new PlaceholderComponent()::init);

		// precondition : if the resource does not exist
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(regionId, null, amount));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		scenarioBuilder.addResource(resourceId);

		// precondition : if the amount is negative
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, -1L));
		assertEquals(ScenarioErrorType.NEGATIVE_RESOURCE_AMOUNT, scenarioException.getScenarioErrorType());

		// precondition : if the region resource level was previously set
		scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setRegionResourceLevel(regionId, resourceId, amount));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(amount, scenario.getRegionResourceLevel(regionId, resourceId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setResourcePropertyValue(ResourceId, ResourcePropertyId, Object)}
	 */
	@Test
	@UnitTestMethod(name = "setResourcePropertyValue", args = { ResourceId.class, ResourcePropertyId.class, Object.class })
	public void testSetResourcePropertyValue() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		ResourceId resourceId = XTestResourceId.RESOURCE1;
		ResourcePropertyId resourcePropertyId = XTestResourceId.RESOURCE1.getResourcePropertyIds()[0];
		Object propertyValue = 5;
		PropertyDefinition propertyDefinition = PropertyDefinition	.builder()//
																	.setType(Integer.class)//
																	.setDefaultValue(17)//
																	.build();//
		scenarioBuilder.addResource(resourceId);
		scenarioBuilder.defineResourceProperty(resourceId, resourcePropertyId, propertyDefinition);

		// precondition : if the resource id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(null, resourcePropertyId, propertyValue));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the resource id is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(XTestResourceId.getUnknownResourceId(), resourcePropertyId, propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

		// precondition : if the resource property is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(resourceId, null, propertyValue));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition : if the resource property is unknown
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(resourceId, XTestResourceId.getUnknownResourcePropertyId(), propertyValue));
		assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_PROPERTY_ID, scenarioException.getScenarioErrorType());

		// precondition: if the value is null
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, null));
		assertEquals(ScenarioErrorType.NULL_RESOURCE_PROPERTY_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the value is not compatible with the property
		// definition
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, "incompatible value"));
		assertEquals(ScenarioErrorType.INCOMPATIBLE_VALUE, scenarioException.getScenarioErrorType());

		// precondition : if the resource property value was previously set
		scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourcePropertyValue(resourceId, resourcePropertyId, propertyValue));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

		// postcondition :
		Scenario scenario = scenarioBuilder.build();
		assertEquals(propertyValue, scenario.getResourcePropertyValue(resourceId, resourcePropertyId));
	}

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#setResourceTimeTracking(ResourceId, TimeTrackingPolicy)}
	 */
	@Test
	@UnitTestMethod(name = "setResourceTimeTracking", args = { ResourceId.class, TimeTrackingPolicy.class })
	public void testSetResourceTimeTracking() {
		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();
		ResourceId resourceId = XTestResourceId.RESOURCE1;

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {

			// precondition : if the resource does not exist
			ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourceTimeTracking(null, timeTrackingPolicy));
			assertEquals(ScenarioErrorType.NULL_RESOURCE_ID, scenarioException.getScenarioErrorType());

			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy));
			assertEquals(ScenarioErrorType.UNKNOWN_RESOURCE_ID, scenarioException.getScenarioErrorType());

			scenarioBuilder.addResource(resourceId);

			// precondition : if the trackValueAssignmentTimes is null
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourceTimeTracking(resourceId, null));
			assertEquals(ScenarioErrorType.NULL_RESOURCE_TRACKING_POLICY, scenarioException.getScenarioErrorType());

			// precondition : if the resource TimeTrackingPolicy was previously
			// set
			scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy);
			scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.setResourceTimeTracking(resourceId, timeTrackingPolicy));
			assertEquals(ScenarioErrorType.PREVIOUSLY_ASSIGNED_VALUE, scenarioException.getScenarioErrorType());

			// postcondition :
			Scenario scenario = scenarioBuilder.build();
			assertEquals(timeTrackingPolicy, scenario.getPersonResourceTimeTrackingPolicy(resourceId));
		}
	}

	/**
	 * Tests {@link StructuredScenarioBuilder#build() }
	 */
	@Test
	@UnitTestMethod(name = "build", args = {})
	public void testBuild() {
		// No test performed: The build method is tested by proxy via the other
		// test methods.
	}

	

	/**
	 * Tests
	 * {@link StructuredScenarioBuilder#addRandomNumberGeneratorId(RandomNumberGeneratorId)}
	 */
	@Test
	@UnitTestMethod(name = "addRandomNumberGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testAddRandomNumberGeneratorId() {

		ScenarioBuilder scenarioBuilder = new StructuredScenarioBuilder();

		// precondition : if the generator id is null
		ScenarioException scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addRandomNumberGeneratorId(null));
		assertEquals(ScenarioErrorType.NULL_RANDOM_NUMBER_GENERATOR_ID, scenarioException.getScenarioErrorType());

		// precondition : if the generator id was previously added
		scenarioBuilder.addRandomNumberGeneratorId(TestRandomGeneratorId.BLITZEN);
		scenarioException = assertThrows(ScenarioException.class, () -> scenarioBuilder.addRandomNumberGeneratorId(TestRandomGeneratorId.BLITZEN));
		assertEquals(ScenarioErrorType.PREVIOUSLY_ADDED_IDENTIFIER, scenarioException.getScenarioErrorType());

		ScenarioBuilder scenarioBuilder2 = new StructuredScenarioBuilder();
		Set<RandomNumberGeneratorId> expected = new LinkedHashSet<>();
		expected.add(TestRandomGeneratorId.COMET);
		expected.add(TestRandomGeneratorId.CUPID);
		expected.add(TestRandomGeneratorId.DONNER);
		expected.add(TestRandomGeneratorId.BLITZEN);

		for (RandomNumberGeneratorId randomNumberGeneratorId : expected) {
			scenarioBuilder2.addRandomNumberGeneratorId(randomNumberGeneratorId);
		}

		Scenario scenario = scenarioBuilder2.build();

		// postcondition: the scenario contains the expected ids
		Set<RandomNumberGeneratorId> actual = scenario.getRandomNumberGeneratorIds();
		assertEquals(expected, actual);

	}
}
