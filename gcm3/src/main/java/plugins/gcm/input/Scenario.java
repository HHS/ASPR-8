package plugins.gcm.input;

import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import net.jcip.annotations.Immutable;
import nucleus.AgentContext;
import nucleus.NucleusError;
import nucleus.ReportContext;
import nucleus.ReportId;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.support.ComponentId;
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
import util.ContractException;

/**
 * Describes the non-replication parts of a simulation's initial state. During
 * the simulation execution, components access this data via the Environment
 * instance provided to them by the simulation.
 *
 * Scenarios are thread safe, immutable containers of such data. Components are
 * included via a class reference and {@link ComponentId}. Identifiers for
 * components( regions, compartments, materials producers, and global
 * components) must be unique.
 * 
 * Resource and property identifiers may be of any type and must be unique
 * within type.
 *
 * @author Shawn Hatch
 *
 */
@Immutable
public interface Scenario {

	/**
	 * Returns the batch property definition associated with the given material
	 * and property identifiers
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if the
	 *             property id is unknown
	 */
	public PropertyDefinition getBatchPropertyDefinition(MaterialId materialId, BatchPropertyId batchPropertyId);

	/**
	 * Returns the group property definition associated with the given group
	 * type id and property identifiers
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             property id is unknown
	 */
	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId);

	/**
	 * Returns the explicitly assigned property value associated with the group
	 * and property identifiers. Returns null if an explicit value was not
	 * assigned. Group property ids are constrained to those associated with the
	 * group type of the group.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if group id is
	 *             unknown *
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if
	 *             property id is unknown
	 *
	 * 
	 */
	public <T> T getGroupPropertyValue(GroupId groupId, GroupPropertyId groupPropertyId);

	/**
	 * Returns the batch property identifiers supplied to the simulation by the
	 * scenario for the given material identifier.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 */
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(MaterialId materialId);

	/**
	 * Returns the group type property identifiers supplied to the simulation by
	 * the scenario for the given group type identifier.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 */
	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId);

	/**
	 * Returns the report identifiers for this scenario. Each such
	 * identifier is associated with a report in the simulation.
	 */

	public <T extends ReportId> Set<T> getReportIds();

	/**
	 * Returns a supplier of the report's initial behavior
	 */
	public Supplier<Consumer<ReportContext>> getReportInitialBehaviorSupplier(ReportId reportId);

	
	/**
	 * Returns the compartment identifiers for this scenario. Each such
	 * identifier is associated with a compartment component in the simulation.
	 */

	public <T extends CompartmentId> Set<T> getCompartmentIds();

	/**
	 * Returns a supplier of the Compartment's initial behavior
	 */
	public Supplier<Consumer<AgentContext>> getCompartmentInitialBehaviorSupplier(CompartmentId compartmentId);

	/**
	 * Returns the compartment property definition associated with the property
	 * identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is unknown
	 */
	public PropertyDefinition getCompartmentPropertyDefinition(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the compartment property identifiers for the given compartmentF.
	 * Each compartment property identifier is associated with a property
	 * definition.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 */
	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(CompartmentId compartmentId);

	/**
	 * Returns the explicitly assigned property value associated with the
	 * compartment and property identifiers. Returns null if an explicit value
	 * was not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             property id is unknown
	 *
	 */
	public <T> T getCompartmentPropertyValue(CompartmentId compartmentId, CompartmentPropertyId compartmentPropertyId);

	/**
	 * Returns the global component identifiers for this scenario. Each such
	 * identifier is associated with a global component in the simulation.
	 */
	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds();

	/**
	 * Returns a supplier of the GlobalComponent's initial behavior
	 */
	public Supplier<Consumer<AgentContext>> getGlobalInitialBehaviorSupplier(GlobalComponentId globalComponentId);

	/**
	 * Returns the global property definition associated with the property
	 * identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id is unknown
	 */
	public PropertyDefinition getGlobalPropertyDefinition(GlobalPropertyId globalPropertyId);

	/**
	 * Returns the global property identifiers for this scenario. Each global
	 * property identifier is associated with a property definition.
	 */
	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds();

	/**
	 * Returns the explicitly assigned property value associated with the global
	 * property identifier. Returns null if an explicit value was not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property identifier is unknown
	 */
	public <T> T getGlobalPropertyValue(GlobalPropertyId globalPropertyId);

	/**
	 * Returns the material identifiers for this scenario.
	 */
	public <T extends MaterialId> Set<T> getMaterialIds();

	/**
	 * Returns the region identifiers for this scenario. Each such identifier is
	 * associated with a region component in the simulation.
	 */
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds();

	/**
	 * Returns a supplier of the Materials Producer's initial behavior
	 */
	public Supplier<Consumer<AgentContext>> getMaterialsProducerInitialBehaviorSupplier(MaterialsProducerId materialsProducerId);

	/**
	 * Returns the materials producer property definition associated with the
	 * property identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property identifier is unknown
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the region property identifiers for this scenario. Each region
	 * property identifier is associated with a property definition.
	 */
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds();

	/**
	 * Returns the explicitly assigned property value associated with the
	 * materials producer and property identifiers. Returns null if an explicit
	 * value was not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is unknown
	 */
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId);

	/**
	 * Returns the explicitly assigned resource level associated with the
	 * materials producer and resource identifiers. Returns null if an explicit
	 * value was not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 */
	public Long getMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId);

	/**
	 * Returns the people initially contained in the scenario. People can, and
	 * usually are, created dynamically within the simulation. Unlike the other
	 * identifiers, people are associated with Integers for efficiency since
	 * simulations usually have millions of people and only relatively small
	 * collections of other entities.
	 */
	public Set<PersonId> getPeopleIds();

	/**
	 * Returns the groups initially contained in the scenario.
	 */
	public Set<GroupId> getGroupIds();

	/**
	 * Returns the person ids associated with the group
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group
	 *             identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group
	 *             identifier is unknown
	 * 
	 */
	public Set<PersonId> getGroupMembers(GroupId groupId);

	/**
	 * Returns the group type id for the given group
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group
	 *             identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group
	 *             identifier is unknown
	 * 
	 */
	public <T extends GroupTypeId> T getGroupTypeId(GroupId groupId);

	/**
	 * Returns the compartment identifier associated with the person identifier.
	 * Returns null if the person identifier was not explicitly added.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person
	 *             identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person
	 *             identifier is unknown
	 */
	public <T extends CompartmentId> T getPersonCompartment(PersonId personId);

	/**
	 * Returns the person property definition associated with the property
	 * identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property identifier is unknown
	 */
	public PropertyDefinition getPersonPropertyDefinition(PersonPropertyId personPropertyId);

	/**
	 * Returns the person property identifiers for this scenario. Each person
	 * property identifier is associated with a property definition.
	 */
	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds();

	/**
	 * Returns the explicitly assigned property value associated with the person
	 * and property identifiers. Returns null if an explicit value was not
	 * assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if person id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if person id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if property
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if
	 *             property id is unknown
	 * 
	 */
	public <T> T getPersonPropertyValue(PersonId personId, PersonPropertyId personPropertyId);

	/**
	 * Returns the region identifier associated with the person identifier.
	 * Returns null if the person identifier was not explicitly added.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person
	 *             identifier is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person
	 *             identifier is null
	 */
	public <T extends RegionId> T getPersonRegion(PersonId personId);

	/**
	 * Returns the resource level associated with the person and resource
	 * identifiers. Returns null if an explicit value was not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 * 
	 */
	public Long getPersonResourceLevel(PersonId personId, ResourceId resourceId);

	/**
	 * Returns the region identifiers for this scenario. Each such identifier is
	 * associated with a region component in the simulation.
	 */
	public Set<RegionId> getRegionIds();

	/**
	 * Returns a supplier of the Region's initial behavior
	 */
	public Supplier<Consumer<AgentContext>> getRegionInitialBehaviorSupplier(RegionId regionId);

	/**
	 * Returns the region property definition associated with the property
	 * identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property id is unknown
	 * 
	 */
	public PropertyDefinition getRegionPropertyDefinition(RegionPropertyId regionPropertyId);

	/**
	 * Returns the region property identifiers for this scenario. Each region
	 * property identifier is associated with a property definition.
	 */
	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds();

	/**
	 * Returns the explicitly assigned property value associated with the region
	 * and property identifiers. Returns null if an explicit value was not
	 * assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REGION_ID} if region id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if region id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if property
	 *             id
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if
	 *             property id is unknown
	 *
	 */
	public <T> T getRegionPropertyValue(RegionId regionId, RegionPropertyId regionPropertyId);

	/**
	 * Returns the explicitly assigned resource level associated with the region
	 * and resource identifiers. Returns null if an explicit value was not
	 * assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 */
	public Long getRegionResourceLevel(RegionId regionId, ResourceId resourceId);

	/**
	 * Returns the resource identifiers for this scenario. All resources are
	 * defined during scenario construction and are not dynamically created
	 * during the simulation run.
	 */
	public <T extends ResourceId> Set<T> getResourceIds();

	/**
	 * Returns the resource property definition associated with the property
	 * identifier
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property id is unknown
	 */
	public PropertyDefinition getResourcePropertyDefinition(ResourceId resourceId, ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the resource property identifiers for this scenario. Each
	 * resource property identifier is associated with a property definition.
	 */
	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(ResourceId resourceId);

	/**
	 * Returns the explicitly assigned property value associated with the
	 * resource and property identifiers. Returns null if an explicit value was
	 * not assigned.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property id is unknown
	 *
	 */
	public <T> T getResourcePropertyValue(ResourceId resourceId, ResourcePropertyId resourcePropertyId);

	/**
	 * Returns the resource time tracking policy for the given resource id.
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 */
	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(ResourceId resourceId);

	/**
	 * Returns the person compartment time arrival tracking policy, which is
	 * defaulted to false.
	 * 
	 *
	 */
	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy();

	/**
	 * Returns the person region time arrival tracking policy, which is
	 * defaulted to false.
	 * 
	 *
	 */
	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy();

	/**
	 * Returns the group type identifiers for this scenario.
	 */
	public <T extends GroupTypeId> Set<T> getGroupTypeIds();

	/**
	 * Returns the batches initially contained in the scenario.
	 */
	public Set<BatchId> getBatchIds();

	/**
	 * Returns the stages initially contained in the scenario.
	 */
	public Set<StageId> getStageIds();

	/**
	 * Returns the offer state of the given stage
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage id is
	 *             unknown
	 * 
	 */
	public Boolean isStageOffered(StageId stageId);

	/**
	 * Returns the materials producer id of the given batch
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 * 
	 */

	public <T extends MaterialsProducerId> T getBatchMaterialsProducer(BatchId batchId);

	/**
	 * Returns the materials producer id of the given stage
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage id is
	 *             unknown
	 * 
	 */

	public <T> T getStageMaterialsProducer(StageId stageId);

	
	/**
	 * Returns the material of the given batch
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 * 
	 */
	public <T> T getBatchMaterial(BatchId batchId);

	/**
	 * Returns the amount of the given batch
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 * 
	 */
	public Double getBatchAmount(BatchId batchId);

	/**
	 * Returns property value associated with the batch and property
	 * identifiers.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *
	 */
	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId);

	/**
	 * Returns the set of batches associated with the given stage
	 * 
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage is is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage is is
	 *             unknown
	 * 
	 */
	public Set<BatchId> getStageBatches(StageId stageId);

	/**
	 * Returns the random generator identifiers for this scenario.
	 */
	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds();

}
