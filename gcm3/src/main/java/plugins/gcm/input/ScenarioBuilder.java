package plugins.gcm.input;

import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.NotThreadSafe;
import nucleus.AgentContext;
import nucleus.NucleusError;
import nucleus.ReportContext;
import nucleus.ReportId;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.people.support.PersonId;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.stochastics.support.RandomNumberGeneratorId;

/**
 * An interface for Scenario Builders.
 *
 * @author Shawn Hatch
 *
 */
@NotThreadSafe
public interface ScenarioBuilder {

	/**
	 * Adds a batch to the scenario for the given materials producer
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID}if the material
	 *             id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID}if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the amount is not finite
	 *             <li>{@link ScenarioErrorType#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative and finite
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the batch id was previously added
	 * 
	 */
	public ScenarioBuilder addBatch(final BatchId batchId, MaterialId materialId, double amount, MaterialsProducerId materialsProducerId);

	/**
	 * Associates a batch with a stage
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_STAGE_ID} if the stage id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#BATCH_STAGED_TO_DIFFERENT_OWNER}
	 *             if the stage and batch are not associated with the same
	 *             materials producer
	 *             <li>{@link ScenarioErrorType#BATCH_ALREADY_STAGED} if the
	 *             batch is already associated any stage
	 * 
	 */
	public ScenarioBuilder addBatchToStage(final StageId stageId, final BatchId batchId);

	/**
	 * Adds a compartment id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component. The supplier must be thread safe and produce new instances of
	 * AgentContext consumers.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the compartment id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the compartment id is equal to another previously added
	 *             component id
	 *             <li>{@link ScenarioErrorType#NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER}
	 *             if the comparmentComponentClass is null
	 */
	public ScenarioBuilder addCompartmentId(final CompartmentId compartmentId, Supplier<Consumer<AgentContext>> supplier);

	/**
	 * Adds a report id to the scenario. The supplier must be thread safe and produce new instances of
	 * ReportContext consumers.
	 *
	 * @throws ScenarioException
	 *
	 *             
	 */
	public ScenarioBuilder addReportId(final ReportId reportId, Supplier<Consumer<ReportContext>> supplier);
	
	/**
	 * Adds the global component id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component. The supplier must be thread safe and produce new instances of
	 * AgentContext consumers.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the GlobalComponentId is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the global component id is equal to another previously added
	 *             component id *
	 *             <li>{@link ScenarioErrorType#NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER}
	 *             if the globalComponentClass is null
	 */
	public ScenarioBuilder addGlobalComponentId(final GlobalComponentId globalComponentId, Supplier<Consumer<AgentContext>> supplier);

	/**
	 * Adds a group to the scenario with the given group type id.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the group was previously added
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown
	 * 
	 */
	public ScenarioBuilder addGroup(final GroupId groupId, final GroupTypeId groupTypeId);

	/**
	 * Adds a group type identifier to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the group type was previously added
	 */
	public ScenarioBuilder addGroupTypeId(final GroupTypeId groupTypeId);

	/**
	 * Adds a material to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the material was previously added
	 *
	 */
	public ScenarioBuilder addMaterial(final MaterialId materialId);

	/**
	 * Adds a random number generator id to the scenario.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the random generator id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the random number generator id was previously added
	 *
	 */
	public ScenarioBuilder addRandomNumberGeneratorId(final RandomNumberGeneratorId randomNumberGeneratorId);

	/**
	 * Adds a materials producer component id to the scenario. This identifier
	 * informs the simulation that a component having this identifier is
	 * expected to exist. Component identifiers must be unique without regard to
	 * the type of component. The supplier must be thread safe and produce new
	 * instances of AgentContext consumers.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the materials producer id is equal to another previously
	 *             added component id
	 *             <li>{@link ScenarioErrorType#NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER}
	 *             if the materialProducerComponentClass is null
	 */
	public ScenarioBuilder addMaterialsProducerId(final MaterialsProducerId materialsProducerId, Supplier<Consumer<AgentContext>> supplier);

	/**
	 * Adds a person to the simulation in the given region and compartment.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the person was previously added
	 *
	 */
	public ScenarioBuilder addPerson(final PersonId personId, final RegionId regionId, final CompartmentId compartmentId);

	/**
	 * Adds a person to a group in the scenario
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GROUP_MEMBERSHIP} if
	 *             the person was previously added to the group
	 *
	 */
	public ScenarioBuilder addPersonToGroup(final GroupId groupId, final PersonId personId);

	/**
	 * Adds a region component id to the scenario. This identifier informs the
	 * simulation that a component having this identifier is expected to exist.
	 * Component identifiers must be unique without regard to the type of
	 * component. The supplier must be thread safe and produce new instances of
	 * AgentContext consumers.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_COMPONENT_IDENTIFIER} if
	 *             the region id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the region was previously added *
	 *             <li>{@link ScenarioErrorType#NULL_AGENT_INITIAL_BEHAVIOR_SUPPLIER}
	 *             if the regionComponentClass is null
	 * 
	 */
	public ScenarioBuilder addRegionId(final RegionId regionId, Supplier<Consumer<AgentContext>> supplier);

	/**
	 * Adds a resource to the scenario
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource was previously added
	 */
	public ScenarioBuilder addResource(final ResourceId resourceId);

	/**
	 * Adds a stage to the scenario for the given materials producer with the
	 * given offer state
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_STAGE_ID} if the stage id
	 *             is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ADDED_IDENTIFIER} if
	 *             the stage id was previously added
	 */
	public ScenarioBuilder addStage(final StageId stageId, boolean offered, MaterialsProducerId materialsProducerId);

	/**
	 * Returns the scenario instance and resets this builder's state.
	 * 
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *             if any property values cannot be determined from direct
	 *             property value contributions or default property values
	 *             associated with property definitions.
	 */
	public Scenario build();

	/**
	 * Defines a batch property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIAL_ID} if the
	 *             material id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_BATCH_PROPERTY_DEFINITION}
	 *             if the batch property was previously defined
	 */
	public ScenarioBuilder defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a compartment property.
	 *
	 * @throws ScenarioException
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_COMPARTMENT_PROPERTY_DEFINITION}
	 *             if the compartment property was previously defined
	 */
	public ScenarioBuilder defineCompartmentProperty(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a global property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GLOBAL_PROPERTY_DEFINITION}
	 *             if the global property was previously defined
	 */
	public ScenarioBuilder defineGlobalProperty(final GlobalPropertyId globalPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a group property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_TYPE_ID} if the group
	 *             type id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_TYPE_ID} if the
	 *             group type id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_GROUP_PROPERTY_DEFINITION}
	 *             if the group property was previously defined
	 */
	public ScenarioBuilder defineGroupProperty(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a materials producer property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the materials producer property was previously defined
	 */
	public ScenarioBuilder defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a person property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_PERSON_PROPERTY_DEFINITION}
	 *             if the person property was previously defined
	 */
	public ScenarioBuilder definePersonProperty(final PersonPropertyId personPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a regional property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region property was previously defined
	 */
	public ScenarioBuilder defineRegionProperty(final RegionPropertyId regionPropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Defines a resource property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the property id is null
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_DEFINITION}
	 *             if the property definition is null
	 *             <li>{@link ScenarioErrorType#DUPLICATE_RESOURCE_PROPERTY_DEFINITION}
	 *             if the resource property was previously defined
	 */
	public ScenarioBuilder defineResourceProperty(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final PropertyDefinition propertyDefinition);

	/**
	 * Sets a batch property value for the given batch.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_ID} if the batch id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_BATCH_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the batch property value was previously set
	 */
	public ScenarioBuilder setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue);

	/**
	 * Sets a compartment property value for the given property.
	 *
	 * @throws ScenarioException
	 * 
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             the compartment property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_PROPERTY_ID}
	 *             if the compartment property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_PROPERTY_VALUE}
	 *             if the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment property value was previously set
	 */
	public ScenarioBuilder setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue);

	/**
	 * Sets a global property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_ID} if the
	 *             global property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GLOBAL_PROPERTY_ID} if
	 *             the global property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GLOBAL_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the global property value was previously set
	 *
	 */

	public ScenarioBuilder setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue);

	/**
	 * Sets the group property value for the given group
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_ID} if the group id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_ID} if the group
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_ID} if the
	 *             group property is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_GROUP_PROPERTY_ID} if
	 *             the group property is unknown
	 *             <li>{@link ScenarioErrorType#NULL_GROUP_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the group property value was previously set
	 */
	public ScenarioBuilder setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue);

	/**
	 * Sets a materials producer property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE}
	 *             if the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the materials producer property value was previously set
	 */
	public ScenarioBuilder setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId, final Object materialsProducerPropertyValue);

	/**
	 * Sets a materials producer's initial resource level.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the materials producer resource level was previously set
	 */
	public ScenarioBuilder setMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount);

	/**
	 * Sets the person compartment time arrival tracking policy, which is
	 * defaulted to DO_NOT_TRACK_TIME.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_COMPARTMENT_TRACKING_POLICY}
	 *             if the trackPersonCompartmentArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the compartment arrival TimeTrackingPolicy was previously set
	 *
	 */
	public ScenarioBuilder setPersonCompartmentArrivalTracking(final TimeTrackingPolicy trackPersonCompartmentArrivalTimes);

	/**
	 * Sets a person property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_ID} if the
	 *             person property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_PROPERTY_ID} if
	 *             the person property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the person property value was previously set
	 *
	 */
	public ScenarioBuilder setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue);

	/**
	 * Sets the person region time arrival tracking policy, which is defaulted
	 * to DO_NOT_TRACK_TIME.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_TRACKING_POLICY} if
	 *             the trackPersonRegionArrivalTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region arrival TimeTrackingPolicy was previously set
	 *
	 */
	public ScenarioBuilder setPersonRegionArrivalTracking(final TimeTrackingPolicy trackPersonRegionArrivalTimes);

	/**
	 * Sets a person's initial resource level.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_PERSON_ID} if the person id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_PERSON_ID} if the person
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the person resource level was previously set
	 */
	public ScenarioBuilder setPersonResourceLevel(final PersonId personId, final ResourceId resourceId, final long amount);

	/**
	 * Sets a region property value for the given property.
	 *
	 * @throws ScenarioException
	 *
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_ID} if the
	 *             region property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_PROPERTY_ID} if
	 *             the region property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_REGION_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region property value was previously set
	 */
	public ScenarioBuilder setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue);

	/**
	 * Sets a region's initial resource level.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_REGION_ID} if the region id
	 *             is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_REGION_ID} if the region
	 *             id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the region resource level was previously set
	 */
	public ScenarioBuilder setRegionResourceLevel(final RegionId regionId, final ResourceId resourceId, final long amount);

	/**
	 * Sets a resource property value for the given property.
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_PROPERTY_ID} if
	 *             the resource property id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link ScenarioErrorType#INCOMPATIBLE_VALUE} if the value
	 *             is not compatible with the property definition
	 * 
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource property value was previously set
	 */
	public ScenarioBuilder setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue);

	/**
	 * Sets the resource time tracking policy for resource assignments to people
	 *
	 * @throws ScenarioException
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_ID} if the
	 *             resource id is null
	 *             <li>{@link ScenarioErrorType#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown
	 *             <li>{@link ScenarioErrorType#NULL_RESOURCE_TRACKING_POLICY}
	 *             if the trackValueAssignmentTimes is null
	 *             <li>{@link ScenarioErrorType#PREVIOUSLY_ASSIGNED_VALUE} if
	 *             the resource TimeTrackingPolicy was previously set
	 */
	public ScenarioBuilder setResourceTimeTracking(final ResourceId resourceId, final TimeTrackingPolicy trackValueAssignmentTimes);

}
