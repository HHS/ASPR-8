
package plugins.gcm.agents;

/**
 * <p>
 * The General Compartment Model (GCM) environment interface.
 *
 * <p>
 * GCM is primarily composed of a simulation instance and a set of contributed
 * components that work together through the environment.
 *
 * <p>
 * The Environment:
 * <li>is common to multiple modeling efforts</li>
 * <li>does not represent the business rules for a specific model</li>
 * <li>groups data (properties) by component types: global, region, compartment,
 * resource, and materials production
 * <li>provides a means to inspect data, observe changes to data, alter data and
 * make plans for future action</li>
 *
 * <p>
 * Components:
 * <li>can act upon the data</li>
 * <li>provide business logic that represents a specific model</li>
 * <li>use the simulation to work with data and make plans</li>
 *
 * <p>
 * Global Components:
 * <li>are identified by a model-defined global component id</li>
 * <li>represent an activity or feature that generally spans all regions and
 * compartments</li>
 * <li>can act upon the simulation data</li>
 * <li>are usually concerned with global issues such as vaccination, addition of
 * people, etc</li>
 *
 * <p>
 * Region Components:
 * <li>represent a physical location or area</li>
 * <li>are identified by a model-defined region id</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>have resource inventory</li>
 * <li>contain compartments</li>
 * <li>manage resource production</li>
 *
 * <p>
 * Compartments Components:
 * <li>represent a general state of people such as a disease state</li>
 * <li>are identified by a model-defined compartment id</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>contain people</li>
 * <li>manage resource distribution and consumption by people</li>
 * <li>move people to other compartments</li>
 * <li>remove people from the simulation</li>
 *
 * <p>
 * Materials Producer Components:
 * <li>represent the production capability to create the resources that will be
 * consumed by people</li>
 * <li>are identified by a model-defined materials producer</li>
 * <li>can act upon the simulation data</li>
 * <li>have property values</li>
 * <li>manage materials creation and processing</li>
 * <li>produce resources from materials</li>
 *
 *
 * <p>
 * Resources:
 * <li>represent a resource type(but not quantity)such as medicines, hospital
 * beds, etc</li>
 * <li>are identified by a model-defined resource id</li>
 * <li>are global in scope</li>
 * <li>are not components and cannot act on their own</li>
 * <li>have property values</li>
 * <li>are associated with people and regions</li>
 *
 * <p>
 * Materials:
 * <li>represent a material type(but not quantity) such as eggs used for vaccine
 * production</li>
 * <li>are identified by a model-defined material id</li>
 * <li>are global in scope</li>
 * <li>are not components and cannot act on their own</li>
 * <li>have property values</li>
 * <li>are associated with materials producer components</li>
 *
 * <p>
 * People:
 * <li>represent a single person</li>
 * <li>are identified by an integer id</li>
 * <li>are not components and cannot act on their own</li>
 * <li>may be added and removed from the simulation</li>
 * <li>are always in a compartment</li>
 * <li>may move from one compartment to another</li>
 * <li>have resource inventory</li>
 *
 * <p>
 * Groups:
 * <li>represent groups of physically co-located people
 * <li>are identified by an integer id</li>
 * <li>are identified by an integer id</li>
 * <li>are not components and cannot act on their own</li>
 * <li>may be added and removed from the simulation</li>
 * <li>may have people added and removed from them without limitation
 *
 * <p>
 * Properties:
 * <li>represent model-defined data values that are associated with components,
 * people and resources</li>
 * <li>are composed of a definition and a value</li>
 * <li>property definitions establish the data type and default value of a
 * property</li>
 * <li>property values are associated with components, people and resources</li>
 * <li>may be of any data type</li>
 *
 * <p>
 * Resource Amounts:
 * <li>represent a non-negative integral quantity of a specific resource type *
 * <li>are associated with people and regions</li>
 *
 * <p>
 * Materials Amounts:
 * <li>represent a floating point quantity of a material that will support the
 * production of resources
 * <li>are associated with materials producer components</li>
 *
 * @NotThreadSafe
 * @author Shawn Hatch
 */
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.math3.random.RandomGenerator;

import net.jcip.annotations.NotThreadSafe;
import nucleus.AgentContext;
import nucleus.EventLabel;
import nucleus.NucleusError;
import nucleus.ReportId;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.mutation.CompartmentPropertyValueAssignmentEvent;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.datacontainers.ComponentDataView;
import plugins.components.support.ComponentId;
import plugins.globals.datacontainers.GlobalDataView;
import plugins.globals.events.mutation.GlobalComponentConstructionEvent;
import plugins.globals.events.mutation.GlobalPropertyValueAssignmentEvent;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;
import plugins.globals.support.GlobalComponentId;
import plugins.globals.support.GlobalPropertyId;
import plugins.groups.datacontainers.PersonGroupDataView;
import plugins.groups.events.mutation.GroupConstructionEvent;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupMembershipRemovalEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.groups.events.observation.GroupCreationObservationEvent;
import plugins.groups.events.observation.GroupImminentRemovalObservationEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.events.observation.GroupPropertyChangeObservationEvent;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupSampler;
import plugins.groups.support.GroupTypeId;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.BatchConstructionEvent;
import plugins.materials.events.mutation.BatchContentShiftEvent;
import plugins.materials.events.mutation.BatchCreationEvent;
import plugins.materials.events.mutation.BatchPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.BatchRemovalRequestEvent;
import plugins.materials.events.mutation.MaterialsProducerPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.MoveBatchToInventoryEvent;
import plugins.materials.events.mutation.MoveBatchToStageEvent;
import plugins.materials.events.mutation.OfferedStageTransferToMaterialsProducerEvent;
import plugins.materials.events.mutation.ProducedResourceTransferToRegionEvent;
import plugins.materials.events.mutation.StageCreationEvent;
import plugins.materials.events.mutation.StageOfferEvent;
import plugins.materials.events.mutation.StageRemovalRequestEvent;
import plugins.materials.events.mutation.StageToBatchConversionEvent;
import plugins.materials.events.mutation.StageToResourceConversionEvent;
import plugins.materials.events.observation.MaterialsProducerPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerResourceChangeObservationEvent;
import plugins.materials.events.observation.StageMaterialsProducerChangeObservationEvent;
import plugins.materials.events.observation.StageOfferChangeObservationEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.partitions.datacontainers.PartitionDataView;
import plugins.partitions.events.PartitionAdditionEvent;
import plugins.partitions.events.PartitionRemovalEvent;
import plugins.partitions.support.LabelSet;
import plugins.partitions.support.Partition;
import plugins.partitions.support.PartitionSampler;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.events.mutation.PopulationGrowthProjectionEvent;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.datacontainers.PersonPropertyDataView;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.TimeTrackingPolicy;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.mutation.PersonRegionAssignmentEvent;
import plugins.regions.events.mutation.RegionPropertyValueAssignmentEvent;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.events.observation.RegionPropertyChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.reports.datacontainers.ReportsDataView;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.InterRegionalResourceTransferEvent;
import plugins.resources.events.mutation.PersonResourceRemovalEvent;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.RegionResourceRemovalEvent;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.events.mutation.ResourceTransferFromPersonEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.events.observation.ResourcePropertyChangeObservationEvent;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.stochastics.StochasticsDataView;
import plugins.stochastics.support.RandomNumberGeneratorId;
import util.ContractException;
import util.objectrepository.ObjectRepository;

@NotThreadSafe

public final class Environment {

	private ComponentDataView componentManager;

	private PartitionDataView partitionDataView;

	private StochasticsDataView stochasticsDataView;

	private MaterialsDataView materialsDataView;

	private ResourceDataView resourceDataView;

	private RegionDataView regionDataView;

	private CompartmentDataView compartmentDataView;

	private GlobalDataView globalDataView;

	private CompartmentLocationDataView compartmentLocationDataView;

	private RegionLocationDataView regionLocationDataView;

	private PersonGroupDataView personGroupDataView;

	private PersonDataView personDataView;

	private PersonPropertyDataView personPropertyDataView;

	private ReportsDataView reportsDataView;

	private AgentContext context;

	private AbstractComponent component;

	/**
	 * Adds a global component dynamically to the simulation
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GLOBAL_COMPONENT_ID} if the
	 *             component id is null
	 *             <li>{@link NucleusError#GLOBAL_COMPONENT_ID_ALREADY_EXISTS}
	 *             if the component id is already present
	 *
	 */

	public void addGlobalComponent(final GlobalComponentId globalComponentId, final Consumer<AgentContext> consumer) {
		context.resolveEvent(new GlobalComponentConstructionEvent(globalComponentId, consumer));
	}

	/**
	 * Returns the group identifier for a newly created group
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_CONSTRUCTION_INFO} if the
	 *             group construction info is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if any
	 *             group property type id is unknown
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if any group
	 *             property value is incompatible with the group property
	 *             definition
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component
	 */

	public GroupId addGroup(final GroupConstructionInfo groupConstructionInfo) {
		context.resolveEvent(new GroupConstructionEvent(groupConstructionInfo));
		return personGroupDataView.getLastIssuedGroupId().get();
	}

	/**
	 * Returns the group identifier for a newly created group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} the group type id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component
	 */

	public GroupId addGroup(final GroupTypeId groupTypeId) {
		context.resolveEvent(new GroupCreationEvent(groupTypeId));
		return personGroupDataView.getLastIssuedGroupId().get();
	}

	/**
	 * Adds a population partition using the supplied population partition
	 * definition.
	 *
	 * Only the component that creates a partition may remove that partition,
	 * but other components may access the partition if they know its key. The
	 * resulting population partition is actively maintained by the GCM and is
	 * accessed by the key provided. This index is owned by the component that
	 * created it in that only that component may remove it from the
	 * Environment. However, all components have access to the partition via its
	 * key.
	 *
	 *
	 * @throws ContractException
	 *             *
	 *             <li>{@link NucleusError#NULL_PARTITION} if the
	 *             population partition definition is null
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#DUPLICATE_PARTITION} if the key
	 *             corresponds to an existing population partition
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if this
	 *             method is invoked while the simulation has no active
	 *             component
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             definition contains an unknown person property id
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the
	 *             definition contains an unknown resource id
	 *
	 *
	 *
	 *
	 *
	 *
	 *
	 */

	public void addPartition(final Partition partition, final Object key) {
		context.resolveEvent(new PartitionAdditionEvent(partition, key));
	}

	/**
	 * Returns the PersonId for a new person who is given the region,
	 * compartment, properties and resources specfied in the
	 * {@linkplain ObjectRepository}
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component
	 *
	 */

	public PersonId addPerson(final PersonContructionData personContructionData) {
		PersonCreationEvent personCreationEvent = new PersonCreationEvent(personContructionData);
		context.resolveEvent(personCreationEvent);
		return personDataView.getLastIssuedPersonId().get();
	}

	public PersonId addBulkPeople(BulkPersonContructionData bulkPersonContructionData) {
		BulkPersonCreationEvent bulkPersonCreationEvent = new BulkPersonCreationEvent(bulkPersonContructionData);
		context.resolveEvent(bulkPersonCreationEvent);
		return personDataView.getLastIssuedPersonId().get();
	}

	/**
	 * Returns the PersonId for a new person who is given the region, and
	 * compartment
	 */

	public PersonId addPerson(final RegionId regionId, final CompartmentId compartmentId) {
		PersonContructionData personContructionData = PersonContructionData.builder().add(regionId).add(compartmentId).build();
		context.resolveEvent(new PersonCreationEvent(personContructionData));
		return personDataView.getLastIssuedPersonId().get();
	}

	/**
	 * Adds a person to the group associated with the given group type and group
	 * identifiers.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#DUPLICATE_GROUP_MEMBERSHIP} if the
	 *             person is already a member of the group
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component
	 */

	public void addPersonToGroup(final PersonId personId, final GroupId groupId) {
		context.resolveEvent(new GroupMembershipAdditionEvent(personId, groupId));
	}

	private static class PlanWrapper implements Consumer<AgentContext> {
		private final Plan plan;
		private final Environment environment;

		private PlanWrapper(Environment environment, Plan plan) {
			this.environment = environment;
			this.plan = plan;
		}

		@Override
		public void accept(AgentContext context) {
			environment.component.executePlan(environment, plan);
		}

	}

	/**
	 * Schedules a plan. The plan is identified by the ordered keys provided.
	 * When time progresses to the planTime, the plan is removed from the
	 * simulation and returned to the invoking component. Plans without any keys
	 * cannot be retrieved or removed.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan time
	 *             is in the past
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component or a materials producer
	 *             component
	 *
	 *
	 */
	public void addPlan(final Plan plan, final double planTime) {
		if (plan == null) {
			context.throwContractException(NucleusError.NULL_PLAN);
		}

		context.addPlan(new PlanWrapper(this, plan), planTime);
	}

	/**
	 * Schedules a plan. The plan is identified by the ordered keys provided.
	 * When time progresses to the planTime, the plan is removed from the
	 * simulation and returned to the invoking component. Plans without any keys
	 * cannot be retrieved or removed.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PLAN} if the plan is null
	 *             <li>{@link NucleusError#PAST_PLANNING_TIME} if the plan time
	 *             is in the past
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the key is null
	 *             <li>{@link NucleusError#DUPLICATE_PLAN_KEY} if the key
	 *             corresponds to an active plan
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component or a materials producer
	 *             component
	 *
	 *
	 */

	public void addPlan(final Plan plan, final double planTime, final Object key) {
		if (plan == null) {
			context.throwContractException(NucleusError.NULL_PLAN);
		}
		context.addPlan(new PlanWrapper(this, plan), planTime, key);
	}

	/**
	 * Adds the amount of resource to the given region.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow of the corresponding region's
	 *             inventory level
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component or region
	 *
	 */

	public void addResourceToRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		context.resolveEvent(new RegionResourceAdditionEvent(resourceId, regionId, amount));
	}

	/**
	 * Returns true if and only if the batch exists
	 *
	 */

	public boolean batchExists(final BatchId batchId) {
		return materialsDataView.batchExists(batchId);
	}

	/**
	 * Converts a stage to a batch that will be held in the inventory of the
	 * invoking materials producer. The stage and its associated batches are
	 * destroyed. Returns the newly created batch's id. The stage must be owned
	 * by the invoking materials producer and must not be in the offered state.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if stage
	 *             is in the offered state
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if material id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if material is
	 *             unknown
	 *             <li>{@link NucleusError#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#NON_FINITE_MATERIAL_AMOUNT} if the
	 *             amount is not finite
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 */

	public BatchId convertStageToBatch(final StageId stageId, final MaterialId materialId, final double amount) {
		context.resolveEvent(new StageToBatchConversionEvent(stageId, materialId, amount));
		return materialsDataView.getLastIssuedBatchId().get();
	}

	/**
	 * Converts a stage to a resource that will be held in the inventory of the
	 * invoking materials producer. The stage and its associated batches are
	 * destroyed. Returns the new created batch's id. The stage must be owned by
	 * the invoking materials producer and must not be in the offered state.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if stage
	 *             is in the offered state
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if resource id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if resource is
	 *             unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative *
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow of the corresponding materials
	 *             producer's inventory level
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 */

	public void convertStageToResource(final StageId stageId, final ResourceId resourceId, final long amount) {
		context.resolveEvent(new StageToResourceConversionEvent(stageId, resourceId, amount));
	}

	/**
	 * Returns the batch id of a new created batch that is stored in the
	 * inventory of the invoking materials producer.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_CONSTRUCTION_INFO} if the
	 *             batch construction info is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             property id is unknown
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a materials producer component
	 */

	public BatchId createBatch(final BatchConstructionInfo batchConstructionInfo) {
		context.resolveEvent(new BatchConstructionEvent(batchConstructionInfo));
		return materialsDataView.getLastIssuedBatchId().get();
	}

	/**
	 * Returns the batch id of a new created batch that is stored in the
	 * inventory of the invoking materials producer.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#NON_FINITE_MATERIAL_AMOUNT} if the
	 *             amount is not finite
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a materials producer component
	 *
	 */

	public BatchId createBatch(final MaterialId materialId, final double amount) {
		context.resolveEvent(new BatchCreationEvent(materialId, amount));
		return materialsDataView.getLastIssuedBatchId().get();
	}

	/**
	 * Creates a new stage owned by the invoking materials producer component
	 * and returns its id .
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a materials producer component
	 *
	 */

	public StageId createStage() {
		context.resolveEvent(new StageCreationEvent());
		return materialsDataView.getLastIssuedStageId().get();
	}

	/**
	 * Destroys the indicated batch that is owned by the invoking materials
	 * producer. The batch may not be part of an offered stage.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown for the materials producer
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if the
	 *             batch is part of an offered stage
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 *
	 */

	public void destroyBatch(final BatchId batchId) {
		context.resolveEvent(new BatchRemovalRequestEvent(batchId));
	}

	/**
	 * Destroys a stage owned by the invoking materials producer component. If
	 * destroyBatches is set to true, then all batches associated with the stage
	 * are also destroyed, otherwise they are returned to inventory.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if stage
	 *             is in an offered state
	 *             <li>{@link ErrorType#} if invoker is not the owning materials
	 *             producer component
	 *
	 */

	public void destroyStage(final StageId stageId, final boolean destroyBatches) {
		context.resolveEvent(new StageRemovalRequestEvent(stageId, destroyBatches));
	}

	/**
	 * Returns the amount in the batch
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batchId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batchId id
	 *             is unknown
	 *
	 */

	public double getBatchAmount(final BatchId batchId) {
		return materialsDataView.getBatchAmount(batchId);
	}

	/**
	 * Returns the material id of the batch
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link ErrorType#} if the batchId id is null
	 *             <li>{@link ErrorType#} if the batchId id is unknown
	 *
	 */

	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		return materialsDataView.getBatchMaterial(batchId);
	}

	/**
	 * Returns the materials producer identifier of the batch
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batchId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batchId id
	 *             is unknown
	 *
	 */

	public <T> T getBatchProducer(final BatchId batchId) {
		return materialsDataView.getBatchProducer(batchId);
	}

	/**
	 * Returns the batch property definition associated with the given material
	 * and property identifiers
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the materialId
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the
	 *             materialId id unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if the
	 *             batchPropertyId id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if the
	 *             batchPropertyId id unknown
	 */

	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		return materialsDataView.getBatchPropertyDefinition(materialId, batchPropertyId);
	}

	/**
	 * Returns the batch property identifiers supplied to the simulation by the
	 * scenario for the given material identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 */

	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		return materialsDataView.getBatchPropertyIds(materialId);
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given batch and property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 */

	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		return materialsDataView.getBatchPropertyTime(batchId, batchPropertyId);
	}

	/**
	 * Returns the batch property value for the given property identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batch id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batch id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if the
	 *             property id is unknown
	 *
	 */

	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		return materialsDataView.getBatchPropertyValue(batchId, batchPropertyId);
	}

	/**
	 * Returns the batch's stage id.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batchId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batchId id
	 *             is unknown
	 *
	 */

	public Optional<StageId> getBatchStageId(final BatchId batchId) {
		return materialsDataView.getBatchStageId(batchId);
	}

	/**
	 * Returns the creation time of the batch
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the batchId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the batchId id
	 *             is unknown
	 *
	 */

	public double getBatchTime(final BatchId batchId) {
		return materialsDataView.getBatchTime(batchId);
	}

	/**
	 * Returns the set of compartment identifiers
	 */

	public <T extends CompartmentId> Set<T> getCompartmentIds() {
		return compartmentDataView.getCompartmentIds();
	}

	/**
	 * Returns the number of people in given compartment for the given region
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 */

	public int getCompartmentPopulationCount(final CompartmentId compartmentId) {
		return compartmentLocationDataView.getCompartmentPopulationCount(compartmentId);
	}

	/**
	 * Returns the simulation time when compartment's population count was last
	 * set.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 */

	public double getCompartmentPopulationCountTime(final CompartmentId compartmentId) {
		return compartmentLocationDataView.getCompartmentPopulationTime(compartmentId);
	}

	/**
	 * Returns the compartment property definition associated with the given
	 * property identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property id does not correspond to a known compartment
	 *             property identifier
	 */

	public PropertyDefinition getCompartmentPropertyDefinition(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return compartmentDataView.getCompartmentPropertyDefinition(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the compartment property identifiers supplied to the simulation
	 * by the scenario.
	 */

	public <T extends CompartmentPropertyId> Set<T> getCompartmentPropertyIds(final CompartmentId compartmentId) {
		return compartmentDataView.getCompartmentPropertyIds(compartmentId);
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given compartment and compartment property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property is unknown
	 *
	 */

	public double getCompartmentPropertyTime(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return compartmentDataView.getCompartmentPropertyTime(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the value associated with the given compartment and property
	 * identifier.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property is unknown
	 */

	public <T> T getCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		return compartmentDataView.getCompartmentPropertyValue(compartmentId, compartmentPropertyId);
	}

	/**
	 * Returns the agent context
	 */

	public AgentContext getContext() {		
		return context;
	}

	/**
	 * Returns the ComponentId of the currently active Component
	 */

	public <T extends ComponentId> T getCurrentComponentId() {
		return componentManager.getFocalComponentId();
	}

	/**
	 * Returns the set of globals component identifiers
	 */

	public <T extends GlobalComponentId> Set<T> getGlobalComponentIds() {
		return globalDataView.getGlobalComponentIds();
	}

	/**
	 * Returns the global property definition associated with the given property
	 * identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id does not correspond to a known global property
	 *             identifier
	 */

	public PropertyDefinition getGlobalPropertyDefinition(final GlobalPropertyId globalPropertyId) {
		return globalDataView.getGlobalPropertyDefinition(globalPropertyId);
	}

	/**
	 * Returns the global property identifiers supplied to the simulation by the
	 * scenario.
	 */

	public <T extends GlobalPropertyId> Set<T> getGlobalPropertyIds() {
		return globalDataView.getGlobalPropertyIds();
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given global property identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id is unknown
	 *
	 */

	public double getGlobalPropertyTime(final GlobalPropertyId globalPropertyId) {
		return globalDataView.getGlobalPropertyTime(globalPropertyId);
	}

	/**
	 * Returns the value associated with the given global property identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id is unknown
	 */

	public <T> T getGlobalPropertyValue(final GlobalPropertyId globalPropertyId) {
		return globalDataView.getGlobalPropertyValue(globalPropertyId);
	}

	/**
	 * Returns the number of groups associated with the given group type
	 * identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *
	 */

	public int getGroupCountForGroupType(final GroupTypeId groupTypeId) {
		return personGroupDataView.getGroupCountForGroupType(groupTypeId);
	}

	/**
	 * Returns the number of groups associated with the given group type
	 * identifier and person identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public int getGroupCountForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return personGroupDataView.getGroupCountForGroupTypeAndPerson(groupTypeId, personId);
	}

	/**
	 * Returns the number of groups associated with the given person id.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public int getGroupCountForPerson(final PersonId personId) {
		return personGroupDataView.getGroupCountForPerson(personId);
	}

	/**
	 * Returns the list of all group identifiers.
	 */

	public List<GroupId> getGroupIds() {
		return personGroupDataView.getGroupIds();
	}

	/**
	 * Returns the group property definition associated with the given group
	 * type, group and property identifiers
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the
	 *             groupPropertyId id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             groupPropertyId id unknown
	 */

	public PropertyDefinition getGroupPropertyDefinition(final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		return personGroupDataView.getGroupPropertyDefinition(groupTypeId, groupPropertyId);
	}

	/**
	 * Returns the group property identifiers supplied to the simulation by the
	 * scenario for the given group type identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type id is unknown
	 */

	public <T extends GroupPropertyId> Set<T> getGroupPropertyIds(final GroupTypeId groupTypeId) {
		return personGroupDataView.getGroupPropertyIds(groupTypeId);
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given group and property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 */

	public double getGroupPropertyTime(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		return personGroupDataView.getGroupPropertyTime(groupId, groupPropertyId);
	}

	/**
	 * Returns the group's property value for the given property identifier
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             property id is unknown
	 *
	 */

	public <T> T getGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId) {
		return personGroupDataView.getGroupPropertyValue(groupId, groupPropertyId);
	}

	/**
	 * Returns the list of group identifiers associated with the given group
	 * type identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *
	 */

	public List<GroupId> getGroupsForGroupType(final GroupTypeId groupTypeId) {
		return personGroupDataView.getGroupsForGroupType(groupTypeId);
	}

	/**
	 * Returns the list of group identifiers associated with the given group
	 * type identifier and person identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public List<GroupId> getGroupsForGroupTypeAndPerson(final GroupTypeId groupTypeId, final PersonId personId) {
		return personGroupDataView.getGroupsForGroupTypeAndPerson(groupTypeId, personId);
	}

	/**
	 * Returns the group type identifiers associated with the given person id
	 * and group id.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public List<GroupId> getGroupsForPerson(final PersonId personId) {
		return personGroupDataView.getGroupsForPerson(personId);
	}

	/**
	 * Returns the group type of the given group.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId id
	 *             is unknown
	 */

	public <T extends GroupTypeId> T getGroupType(final GroupId groupId) {
		return personGroupDataView.getGroupType(groupId);
	}

	/**
	 * Returns the number of group types associated the person's groups.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public int getGroupTypeCountForPerson(final PersonId personId) {
		return personGroupDataView.getGroupTypeCountForPersonId(personId);
	}

	/**
	 * Returns the set of group type identifiers as provided during simulation
	 * construction.
	 *
	 */

	public <T extends GroupTypeId> Set<T> getGroupTypeIds() {
		return personGroupDataView.getGroupTypeIds();
	}

	/**
	 * Returns the group type identifiers associated the person's groups.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public <T extends GroupTypeId> List<T> getGroupTypesForPerson(final PersonId personId) {
		return personGroupDataView.getGroupTypesForPerson(personId);
	}

	/**
	 * Returns the batches owned by a particular materials producer that are in
	 * inventory (not staged).
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materialsProducer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materialsProducer id is unknown
	 *
	 */

	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		return materialsDataView.getInventoryBatches(materialsProducerId);
	}

	/**
	 * Returns the batches having a particular material id and owned by a
	 * particular materials producer that are in inventory (not staged).
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materialsProducer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materialsProducer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 *
	 */

	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId) {
		return materialsDataView.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
	}

	/**
	 * Returns the set of material identifiers as provided during simulation
	 * construction.
	 *
	 */

	public <T extends MaterialId> Set<T> getMaterialIds() {
		return materialsDataView.getMaterialIds();
	}

	/**
	 * Returns the set of material producer identifiers
	 */

	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		return materialsDataView.getMaterialsProducerIds();
	}

	/**
	 * Returns the property definition associated with the given materials
	 * producer property identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id does not correspond to a known materials
	 *             producer property identifier
	 */

	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataView.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
	}

	/**
	 * Returns the materials producer property identifiers supplied to the
	 * simulation by the scenario.
	 */

	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		return materialsDataView.getMaterialsProducerPropertyIds();
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given materials producer and materials producer property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property is unknown
	 *
	 */

	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataView.getMaterialsProducerPropertyTime(materialsProducerId, materialsProducerPropertyId);
	}

	/**
	 * Returns the value associated with the given materials producer and
	 * materials producer property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials Producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials Producer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property is unknown
	 *
	 *
	 */

	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
	}

	/**
	 * Returns the materials producer's current resource level for the given
	 * resource identifier.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		return materialsDataView.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
	}

	/**
	 * Returns the simulation time when the materials producer's resource level
	 * was last set for the given resource identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *
	 */

	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		return materialsDataView.getMaterialsProducerResourceTime(materialsProducerId, resourceId);
	}

	/**
	 * Returns the offered stage's for the given materials producer.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 */

	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {
		return materialsDataView.getOfferedStages(materialsProducerId);
	}

	/**
	 * Returns a list of person identifiers associated with the population
	 * partition.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population
	 *             partition
	 *
	 */

	public List<PersonId> getPartitionPeople(final Object key) {
		return partitionDataView.getPeople(key);
	}

	/**
	 * Returns a list of person identifiers associated with the population
	 * partition and label set.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population
	 *             partition
	 *             <li>{@link NucleusError#NULL_LABEL_SET} if the label set is
	 *             null *
	 *             <li>{@link NucleusError#INCOMPATIBLE_LABEL_SET} if the label
	 *             set is incompatible with the population partition
	 *
	 *
	 */

	public List<PersonId> getPartitionPeople(final Object key, final LabelSet labelSet) {
		return partitionDataView.getPeople(key, labelSet);
	}

	/**
	 * Returns the size of a partition.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population
	 *             partition
	 */

	public int getPartitionSize(final Object key) {
		return partitionDataView.getPersonCount(key);
	}

	/**
	 * Returns the size of the partition cells consistent with the label set for
	 * the partition.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population
	 *             partition
	 *             <li>{@link NucleusError#NULL_LABEL_SET} if the label set is
	 *             null *
	 *             <li>{@link NucleusError#INCOMPATIBLE_LABEL_SET} if the label
	 *             set is incompatible with the population partition
	 */
	public int getPartitionSize(final Object key, final LabelSet labelSet) {
		return partitionDataView.getPersonCount(key, labelSet);
	}

	/**
	 * Returns the size of the partition cells consistent with the label set for
	 * the partition. The returned map will be keyed with the label sets
	 * corresponding to the cells. Each mapped value will be positive.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population
	 *             partition
	 *             <li>{@link NucleusError#NULL_LABEL_SET} if the label set is
	 *             null *
	 *             <li>{@link NucleusError#INCOMPATIBLE_LABEL_SET} if the label
	 *             set is incompatible with the population partition
	 */
	public Map<LabelSet, Integer> getPartitionSizeMap(final Object key, LabelSet labelSet) {
		return partitionDataView.getPeopleCountMap(key, labelSet);
	}

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the simulation.
	 *
	 */

	public List<PersonId> getPeople() {
		return personDataView.getPeople();
	}

	/**
	 * Returns the list of people identifiers associated with the given group
	 * type identifier and group identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown(group does not exist)
	 *
	 *
	 */

	public List<PersonId> getPeopleForGroup(final GroupId groupId) {
		return personGroupDataView.getPeopleForGroup(groupId);
	}

	/**
	 * Returns the list of people identifiers associated with the given group
	 * type identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *
	 */

	public List<PersonId> getPeopleForGroupType(final GroupTypeId groupTypeId) {
		return personGroupDataView.getPeopleForGroupType(groupTypeId);
	}

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the the given compartment.
	 *
	 * throws ModelException
	 *
	 * <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the compartment id is
	 * null
	 * <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the compartment is
	 * unknown
	 */

	public List<PersonId> getPeopleInCompartment(final CompartmentId compartmentId) {
		return compartmentLocationDataView.getPeopleInCompartment(compartmentId);
	}

	/**
	 * Returns the list of person identifier values for all people currently in
	 * the the given region.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 */

	public List<PersonId> getPeopleInRegion(final RegionId regionId) {
		return regionLocationDataView.getPeopleInRegion(regionId);
	}

	/**
	 * Returns the list of person identifier values for all people currently
	 * having zero units of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id is null
	 * <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource id is not
	 * known
	 */

	public List<PersonId> getPeopleWithoutResource(final ResourceId resourceId) {
		return resourceDataView.getPeopleWithoutResource(resourceId);
	}

	/**
	 * Returns the list of person identifier values for all people currently
	 * having the the given property value for the given person property
	 * identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the property id is
	 * null
	 * <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the property id is
	 * unknown
	 * <li>{@link NucleusError#NULL_PERSON_PROPERTY_VALUE} if the property value
	 * is null
	 * <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the property value is not
	 * compatible with the property definition
	 */

	public List<PersonId> getPeopleWithPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertyDataView.getPeopleWithPropertyValue(personPropertyId, personPropertyValue);
	}

	/**
	 * Returns the list of person identifier values for all people currently
	 * having at least one unit of the given resource identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id is null
	 * <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource id is not
	 * known
	 */

	public List<PersonId> getPeopleWithResource(final ResourceId resourceId) {
		return resourceDataView.getPeopleWithResource(resourceId);
	}

	/**
	 * Returns the compartment identifier for the given person.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 */

	public <T extends CompartmentId> T getPersonCompartment(final PersonId personId) {
		return compartmentLocationDataView.getPersonCompartment(personId);
	}

	/**
	 * Returns the simulation time when the person arrived in their current
	 * compartment. Movement between regions within a single compartment does
	 * not alter this value.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#COMPARTMENT_ARRIVAL_TIMES_NOT_TRACKED}
	 *             if compartment arrival times are not selected for tracking in
	 *             the scenario
	 *
	 */

	public double getPersonCompartmentArrivalTime(final PersonId personId) {
		return compartmentLocationDataView.getPersonCompartmentArrivalTime(personId);
	}

	/**
	 * Returns true if and only if the simulation is actively tracking
	 * compartment arrival times for people.
	 *
	 * @return
	 */

	public TimeTrackingPolicy getPersonCompartmentArrivalTrackingPolicy() {
		return compartmentLocationDataView.getPersonCompartmentArrivalTrackingPolicy();
	}

	/**
	 * Returns the number of people associated with the given group type
	 * identifier and group identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *
	 *
	 */

	public int getPersonCountForGroup(final GroupId groupId) {
		return personGroupDataView.getPersonCountForGroup(groupId);
	}

	/**
	 * Returns the number of people associated with the given group type
	 * identifier.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group Type
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             Type id is unknown
	 *
	 */

	public int getPersonCountForGroupType(final GroupTypeId groupTypeId) {
		return personGroupDataView.getPersonCountForGroupType(groupTypeId);
	}

	/**
	 * Returns the number of people currently having the the given property
	 * value for the given person property identifier.
	 *
	 * throws ModelException
	 *
	 * <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the property id is
	 * null
	 * <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the property id is
	 * unknown
	 * <li>{@link NucleusError#NULL_PERSON_PROPERTY_VALUE} if the property value
	 * is null
	 * <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the property value is not
	 * compatible with the property definition
	 */

	public int getPersonCountForPropertyValue(final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		return personPropertyDataView.getPersonCountForPropertyValue(personPropertyId, personPropertyValue);
	}

	/**
	 * Returns the person property definition associated with the given property
	 * identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property id does not correspond to a known person property
	 *             identifier
	 */

	public PropertyDefinition getPersonPropertyDefinition(final PersonPropertyId personPropertyId) {
		return personPropertyDataView.getPersonPropertyDefinition(personPropertyId);
	}

	/**
	 * Returns the person property identifiers supplied to the simulation by the
	 * scenario.
	 */

	public <T extends PersonPropertyId> Set<T> getPersonPropertyIds() {
		return personPropertyDataView.getPersonPropertyIds();
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given person and person property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property is unknown
	 *             <li>{@link NucleusError#PROPERTY_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if person property times are not selected for tracking in the
	 *             scenario
	 *
	 */

	public double getPersonPropertyTime(final PersonId personId, final PersonPropertyId personPropertyId) {
		return personPropertyDataView.getPersonPropertyTime(personId, personPropertyId);
	}

	/**
	 * Returns the value associated with the given person and property
	 * identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property id is not a person property
	 */

	public <T> T getPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId) {
		return personPropertyDataView.getPersonPropertyValue(personId, personPropertyId);
	}

	/**
	 * Returns the region identifier for the given person.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 */

	public <T extends RegionId> T getPersonRegion(final PersonId personId) {
		return regionLocationDataView.getPersonRegion(personId);
	}

	/**
	 * Returns the simulation time when the person arrived in their current
	 * region. Movement between compartments within a single region does not
	 * alter this value.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#REGION_ARRIVAL_TIMES_NOT_TRACKED} if
	 *             person region arrival times are not selected for tracking in
	 *             the scenario
	 *
	 */

	public double getPersonRegionArrivalTime(final PersonId personId) {
		return regionLocationDataView.getPersonRegionArrivalTime(personId);
	}

	/**
	 * Returns true if and only if the simulation is actively tracking region
	 * arrival times for people.
	 *
	 * @return
	 */

	public TimeTrackingPolicy getPersonRegionArrivalTrackingPolicy() {
		return regionLocationDataView.getPersonRegionArrivalTrackingPolicy();
	}

	/**
	 * Returns the person's current resource level for the given resource
	 * identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public long getPersonResourceLevel(final PersonId personId, final ResourceId resourceId) {
		return resourceDataView.getPersonResourceLevel(resourceId, personId);
	}

	/**
	 * Returns the simulation time when the person's resource level was last set
	 * for the given resource identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *             <li>{@link NucleusError#RESOURCE_ASSIGNMENT_TIME_NOT_TRACKED}
	 *             if person resource assignment times are not selected for
	 *             tracking in the scenario
	 *
	 */

	public double getPersonResourceTime(final PersonId personId, final ResourceId resourceId) {
		return resourceDataView.getPersonResourceTime(resourceId, personId);
	}

	/**
	 * Returns true if and only if the simulation is actively tracking resource
	 * value assignment times for the given resource id.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *
	 * @param resourceId
	 * @return
	 */

	public TimeTrackingPolicy getPersonResourceTimeTrackingPolicy(final ResourceId resourceId) {
		return resourceDataView.getPersonResourceTimeTrackingPolicy(resourceId);
	}

	/**
	 * Retrieves a plan that was added with the given key. The returned play
	 * remains scheduled.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the key is null
	 */

	@SuppressWarnings("unchecked")
	public <T> Optional<T> getPlan(final Object key) {
		Optional<Consumer<AgentContext>> optional = context.getPlan(key);
		Plan result = null;
		if (optional.isPresent()) {
			PlanWrapper planWrapper = (PlanWrapper) optional.get();
			result = planWrapper.plan;
		}
		return Optional.ofNullable((T) result);
	}

	/**
	 * Returns the set of plan keys for the current Component as a list. Items
	 * on the list are unique.
	 */

	public List<Object> getPlanKeys() {
		return context.getPlanKeys();
	}

	/**
	 * Returns the planned execution time for a scheduled plan that was added
	 * with the given key. Returns a negative value if the plan cannot be found.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the key is null
	 */

	public Optional<Double> getPlanTime(final Object key) {
		return context.getPlanTime(key);
	}

	/**
	 * Returns the number of people currently in the simulation.
	 *
	 */

	public int getPopulationCount() {
		return personDataView.getPopulationCount();
	}

	/**
	 * Returns the PersonId that corresponds to the int value.
	 * 
	 * @Throws {@link RuntimeException}
	 *         <li>if the given person id does not correspond to an existing
	 *         person
	 */
	public PersonId getBoxedPersonId(int personId) {
		return personDataView.getBoxedPersonId(personId);
	}

	/**
	 * Returns the simulation time when population count was last set.
	 *
	 */

	public double getPopulationTime() {
		return personDataView.getPopulationTime();
	}

	/**
	 * Returns the standard RandomGenerator instance from the simulation. This
	 * RandomGenerator is initialized with a seed value during simulation
	 * construction.
	 */

	public RandomGenerator getRandomGenerator() {
		return stochasticsDataView.getRandomGenerator();
	}

	/**
	 * Returns the RandomGenerator instance from the simulation associated with
	 * the given RandomNumberGeneratorId. This RandomGenerator is initialized
	 * with a seed value during simulation construction that is a hash of the
	 * replication seed and the randomNumberGeneratorId.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_RANDOM_NUMBER_GENERATOR_ID} if
	 *             the randomNumberGeneratorId is null
	 *             <li>{@link NucleusError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the randomNumberGeneratorId does not correspond to an
	 *             existing RandomNumberGeneratorId found in the scenario.
	 */

	public RandomGenerator getRandomGeneratorFromId(final RandomNumberGeneratorId randomNumberGeneratorId) {
		return stochasticsDataView.getRandomGeneratorFromId(randomNumberGeneratorId);
	}

	/**
	 * Returns the set of random number generator identifiers as provided during
	 * simulation construction.
	 *
	 */

	public <T extends RandomNumberGeneratorId> Set<T> getRandomNumberGeneratorIds() {
		return stochasticsDataView.getRandomNumberGeneratorIds();
	}

	/**
	 * Returns the set of region identifiers
	 */

	public <T extends RegionId> Set<T> getRegionIds() {
		return regionDataView.getRegionIds();
	}

	/**
	 * Returns the number of people currently in the simulation.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 */

	public int getRegionPopulationCount(final RegionId regionId) {
		return regionLocationDataView.getRegionPopulationCount(regionId);
	}

	/**
	 * Returns the simulation time when region's population count was last set.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 */

	public double getRegionPopulationCountTime(final RegionId regionId) {
		return regionLocationDataView.getRegionPopulationTime(regionId);
	}

	/**
	 * Returns the region property definition associated with the given property
	 * identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property id does not correspond to a known region property
	 *             identifier
	 */

	public PropertyDefinition getRegionPropertyDefinition(final RegionPropertyId regionPropertyId) {
		return regionDataView.getRegionPropertyDefinition(regionPropertyId);
	}

	/**
	 * Returns the region property identifiers supplied to the simulation by the
	 * scenario.
	 */

	public <T extends RegionPropertyId> Set<T> getRegionPropertyIds() {
		return regionDataView.getRegionPropertyIds();
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given region and region property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 */

	public double getRegionPropertyTime(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return regionDataView.getRegionPropertyTime(regionId, regionPropertyId);
	}

	/**
	 * Returns the value associated with the given region and region property
	 * identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 *
	 */

	public <T> T getRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId) {
		return regionDataView.getRegionPropertyValue(regionId, regionPropertyId);
	}

	/**
	 * Returns the region's current resource level for the given resource
	 * identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public long getRegionResourceLevel(final RegionId regionId, final ResourceId resourceId) {
		return resourceDataView.getRegionResourceLevel(regionId, resourceId);
	}

	/**
	 * Returns the simulation time when the region's resource level was last set
	 * for the given resource identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *
	 */

	public double getRegionResourceTime(final RegionId regionId, final ResourceId resourceId) {
		return resourceDataView.getRegionResourceTime(regionId, resourceId);
	}

	/**
	 * Returns the set of resource identifiers as provided during simulation
	 * construction.
	 *
	 */

	public <T extends ResourceId> Set<T> getResourceIds() {
		return resourceDataView.getResourceIds();
	}

	/**
	 * Returns the resource property definition associated with the given
	 * property identifier
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if resource id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if resource id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property id does not correspond to a known resource property
	 *             identifier
	 */

	public PropertyDefinition getResourcePropertyDefinition(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourceDataView.getResourcePropertyDefinition(resourceId, resourcePropertyId);
	}

	/**
	 * Returns the resource property identifiers supplied to the simulation by
	 * the scenario.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if resource id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if resource id
	 *             is unknown
	 */

	public <T extends ResourcePropertyId> Set<T> getResourcePropertyIds(final ResourceId resourceId) {
		return resourceDataView.getResourcePropertyIds(resourceId);
	}

	/**
	 * Returns the simulation time when the property value was last set for the
	 * given resource and resource property identifiers.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 */

	public double getResourcePropertyTime(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourceDataView.getResourcePropertyTime(resourceId, resourcePropertyId);
	}

	/**
	 * Returns the value associated with the given resource and property
	 * identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property is unknown
	 *
	 *
	 */

	public <T> T getResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		return resourceDataView.getResourcePropertyValue(resourceId, resourcePropertyId);
	}

	/**
	 * Returns the batches associated with a stage.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stageId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stageId id
	 *             is unknown
	 *
	 */

	public List<BatchId> getStageBatches(final StageId stageId) {
		return materialsDataView.getStageBatches(stageId);
	}

	/**
	 * Returns the batches having the given material type associated with a
	 * particular stage.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stageId id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stageId id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_MATERIAL_ID} if the material id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIAL_ID} if the material
	 *             id is unknown
	 *
	 */

	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
		return materialsDataView.getStageBatchesByMaterialId(stageId, materialId);
	}

	/**
	 * Returns the stage's materials producer
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage id is
	 *             unknown
	 *
	 */

	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		return materialsDataView.getStageProducer(stageId);
	}

	/**
	 * Returns the stage's for the given materials producer.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 */

	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		return materialsDataView.getStages(materialsProducerId);
	}

	/**
	 * Returns the suggested population size from the scenario.
	 *
	 * @throws RuntimeException
	 *             <li>if the count is negative
	 */

	public void suggestedPopulationGrowth(int count) {
		context.resolveEvent(new PopulationGrowthProjectionEvent(count));
	}

	/**
	 * Returns the current time. Time is measured in days and initializes to
	 * zero. Time progresses via planning.
	 *
	 */

	public double getTime() {
		return context.getTime();
	}

	/**
	 * Returns true if and only if the group associated with the given group id
	 * exists. Tolerates null.
	 */

	public boolean groupExists(final GroupId groupId) {
		return personGroupDataView.groupExists(groupId);
	}

	/**
	 * Gracefully stops the processing of plans and observations, allowing the
	 * simulation to finalize state change listeners.
	 */

	public void halt() {
		context.halt();
	}

	/*
	 * Initializes(loads into local data structures) the scenario and
	 * replication data as well as constructs the simulations components using
	 * the ComponentFactory
	 */

	public void init(final AgentContext agentContext, final AbstractComponent abstractComponent) {
		context = agentContext;
		component = abstractComponent;
		globalDataView = context.getDataView(GlobalDataView.class).get();
		materialsDataView = context.getDataView(MaterialsDataView.class).get();
		partitionDataView = context.getDataView(PartitionDataView.class).get();
		personGroupDataView = context.getDataView(PersonGroupDataView.class).get();
		personDataView = context.getDataView(PersonDataView.class).get();
		personPropertyDataView = context.getDataView(PersonPropertyDataView.class).get();
		compartmentLocationDataView = context.getDataView(CompartmentLocationDataView.class).get();
		regionLocationDataView = context.getDataView(RegionLocationDataView.class).get();
		compartmentDataView = context.getDataView(CompartmentDataView.class).get();
		regionDataView = context.getDataView(RegionDataView.class).get();
		resourceDataView = context.getDataView(ResourceDataView.class).get();
		stochasticsDataView = context.getDataView(StochasticsDataView.class).get();
		reportsDataView = context.getDataView(ReportsDataView.class).get();
		componentManager = context.getDataView(ComponentDataView.class).get();
	}

	/**
	 * Returns true if and only if the report associated with the given report
	 * class is active
	 *
	 * @throws ContractException
	 * 
	 *
	 */

	public boolean isActiveReport(final ReportId reportId) {
		return reportsDataView.isActiveReport(reportId);
	}

	/**
	 * Returns true if and only if the person is a member of the group
	 * identified by the group type and group identifiers.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 */

	public boolean isGroupMember(final PersonId personId, final GroupId groupId) {
		return personGroupDataView.isGroupMember(groupId, personId);
	}

	/**
	 * Returns the stage's offer state. Offered stages cannot be altered until
	 * they are no longer offered
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage id is
	 *             unknown
	 *
	 */

	public boolean isStageOffered(final StageId stageId) {
		return materialsDataView.isStageOffered(stageId);
	}

	/**
	 * Disassociates a batch from its current stage .The batch must owned by the
	 * invoking materials producer and the its stage may not be in the offered
	 * state.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if batch id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if batch id is
	 *             unknown
	 *             <li>{@link NucleusError#BATCH_NOT_STAGED} if batch id is not
	 *             associated with a stage
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if batch's
	 *             stage is in the offered state
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 */

	public void moveBatchToInventory(final BatchId batchId) {
		context.resolveEvent(new MoveBatchToInventoryEvent(batchId));
	}

	/**
	 * Associates a batch with a stage, both of which are owned by the invoking
	 * materials producer. The batch must be in the inventory of the invoking
	 * materials producer and not associated with a stage and the stage must not
	 * be in an offered state.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if batch id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if batch id is
	 *             unknown
	 *             <li>{@link NucleusError#BATCH_ALREADY_STAGED} if batch id is
	 *             already associated with a stage
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if stage
	 *             is in an offered state
	 *             <li>{@link NucleusError# BATCH_STAGED_TO_DIFFERENT_OWNER} if
	 *             the batch and stage are owned by different materials
	 *             producers
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component of the
	 *             batch
	 *
	 *
	 */

	public void moveBatchToStage(final BatchId batchId, final StageId stageId) {
		context.resolveEvent(new MoveBatchToStageEvent(batchId, stageId));

	}

	/**
	 * Starts or stops observation of property value changes on all people in a
	 * particular compartment for the calling component.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property id is unknown
	 */

	public void observeCompartmentalPersonPropertyChange(final boolean observe, final CompartmentId compartmentId, final PersonPropertyId personPropertyId) {

		EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByCompartmentAndProperty(context, compartmentId, personPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of resource level changes on all people in
	 * the given compartment for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeCompartmentalPersonResourceChange(final boolean observe, final CompartmentId compartmentId, final ResourceId resourceId) {

		EventLabel<PersonResourceChangeObservationEvent> eventLabel = PersonResourceChangeObservationEvent.getEventLabelByCompartmentAndResource(context, compartmentId, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of property value changes on the given
	 * compartment for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property is unknown
	 */

	public void observeCompartmentPropertyChange(final boolean observe, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		final EventLabel<CompartmentPropertyChangeObservationEvent> eventLabel = CompartmentPropertyChangeObservationEvent.getEventLabel(context, compartmentId, compartmentPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleCompartmentPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of people being added to the simulation for
	 * the calling component.
	 *
	 */
	public void observeGlobalPersonArrival(final boolean observe) {
		EventLabel<BulkPersonCreationObservationEvent> bulkEventLabel = BulkPersonCreationObservationEvent.getEventLabel();
		EventLabel<PersonCreationObservationEvent> individualEventLabel = PersonCreationObservationEvent.getEventLabel();
		if (observe) {
			context.subscribe(individualEventLabel, component::handlePersonCreationObservationEvent);
			context.subscribe(bulkEventLabel, component::handleBulkPersonCreationObservationEvent);
		} else {
			context.unsubscribe(individualEventLabel);
			context.unsubscribe(bulkEventLabel);
		}
	}

	/**
	 * Starts or stops observation of people being removed from the simulation
	 * for the calling component.
	 *
	 */

	public void observeGlobalPersonDeparture(final boolean observe) {
		EventLabel<PersonImminentRemovalObservationEvent> eventLabel = PersonImminentRemovalObservationEvent.getEventLabel();
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonImminentRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of property value changes on all people for
	 * the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observeGlobalPersonPropertyChange(final boolean observe, final PersonPropertyId personPropertyId) {
		EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByProperty(context, personPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of resource level changes on all people for
	 * the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeGlobalPersonResourceChange(final boolean observe, final ResourceId resourceId) {
		EventLabel<PersonResourceChangeObservationEvent> eventLabel = PersonResourceChangeObservationEvent.getEventLabelByResource(context, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the given global property for the calling
	 * component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id is unknown
	 *
	 */

	public void observeGlobalPropertyChange(final boolean observe, final GlobalPropertyId globalPropertyId) {
		EventLabel<GlobalPropertyChangeObservationEvent> eventLabel = GlobalPropertyChangeObservationEvent.getEventLabel(context, globalPropertyId);
		if (observe) {
			context.subscribe(eventLabel, this.component::handleGlobalPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of property value changes for all regions for
	 * the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observeGlobalRegionPropertyChange(final boolean observe, final RegionPropertyId regionPropertyId) {
		EventLabel<RegionPropertyChangeObservationEvent> eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByProperty(context, regionPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleRegionPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 *
	 */

	public void observeGroupArrival(final boolean observe) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByAll();
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the arrival of any person into the given
	 * group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId is
	 *             unknown
	 */

	public void observeGroupArrivalByGroup(final boolean observe, final GroupId groupId) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByGroup(context, groupId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the arrival of the given person into the
	 * given group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 */

	public void observeGroupArrivalByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByGroupAndPerson(context, groupId, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the arrival of the given person into any
	 * group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 */

	public void observeGroupArrivalByPerson(final boolean observe, final PersonId personId) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByPerson(context, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the arrival of any person into any group
	 * having the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 */

	public void observeGroupArrivalByType(final boolean observe, final GroupTypeId groupTypeId) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByGroupType(context, groupTypeId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the arrival of the given person into any
	 * group having the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 */

	public void observeGroupArrivalByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId) {
		EventLabel<GroupMembershipAdditionObservationEvent> eventLabel = GroupMembershipAdditionObservationEvent.getEventLabelByGroupTypeAndPerson(context, groupTypeId, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipAdditionObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of all group construction
	 *
	 */

	public void observeGroupConstruction(final boolean observe) {
		EventLabel<GroupCreationObservationEvent> eventLabel = GroupCreationObservationEvent.getEventLabelByAll();
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupCreationObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of group construction for groups having the
	 * given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 */

	public void observeGroupConstructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		EventLabel<GroupCreationObservationEvent> eventLabel = GroupCreationObservationEvent.getEventLabelByGroupType(context, groupTypeId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupCreationObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the departure of any person from any group
	 *
	 */

	public void observeGroupDeparture(final boolean observe) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByAll();
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the departure of any person from the given
	 * group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId is
	 *             unknown
	 */

	public void observeGroupDepartureByGroup(final boolean observe, final GroupId groupId) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroup(context, groupId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the departure of the given person from the
	 * given group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 *
	 * @throws ContractException
	 *
	 */

	public void observeGroupDepartureByGroupAndPerson(final boolean observe, final GroupId groupId, final PersonId personId) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupAndPerson(context, groupId, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 */

	public void observeGroupDepartureByPerson(final boolean observe, final PersonId personId) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByPerson(context, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the departure of any person from any group
	 * having the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 */

	public void observeGroupDepartureByType(final boolean observe, final GroupTypeId groupTypeId) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupType(context, groupTypeId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the departure of the given person from any
	 * group having the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 */

	public void observeGroupDepartureByTypeAndPerson(final boolean observe, final GroupTypeId groupTypeId, final PersonId personId) {
		EventLabel<GroupMembershipRemovalObservationEvent> eventLabel = GroupMembershipRemovalObservationEvent.getEventLabelByGroupTypeAndPerson(context, groupTypeId, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupMembershipRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of all group destruction
	 *
	 */

	public void observeGroupDestruction(final boolean observe) {
		EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByAll();
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupImminentRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of group destruction for the given group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the groupId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the groupId is
	 *             unknown
	 */

	public void observeGroupDestructionByGroup(final boolean observe, final GroupId groupId) {
		EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroup(context, groupId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupImminentRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of group destruction for groups having the
	 * given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the
	 *             groupTypeId is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the
	 *             groupTypeId is unknown
	 */

	public void observeGroupDestructionByType(final boolean observe, final GroupTypeId groupTypeId) {
		EventLabel<GroupImminentRemovalObservationEvent> eventLabel = GroupImminentRemovalObservationEvent.getEventLabelByGroupType(context, groupTypeId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupImminentRemovalObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of all group property changes for all groups
	 *
	 *
	 */

	public void observeGroupPropertyChange(final boolean observe) {
		EventLabel<GroupPropertyChangeObservationEvent> eventLabel = GroupPropertyChangeObservationEvent.getEventLabelByAll();
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of all group property changes for the given
	 * group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the group
	 *             property id null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown
	 *
	 */

	public void observeGroupPropertyChangeByGroup(final boolean observe, final GroupId groupId) {
		EventLabel<GroupPropertyChangeObservationEvent> eventLabel = GroupPropertyChangeObservationEvent.getEventLabelByGroup(context, groupId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of all group property changes for the given
	 * group property and group
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the group
	 *             property id null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown
	 *
	 */

	public void observeGroupPropertyChangeByGroupAndProperty(final boolean observe, final GroupId groupId, final GroupPropertyId groupPropertyId) {
		EventLabel<GroupPropertyChangeObservationEvent> eventLabel = GroupPropertyChangeObservationEvent.getEventLabelByGroupAndProperty(context, groupId, groupPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of all group property changes for all groups
	 * of the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type is unknown
	 *
	 */

	public void observeGroupPropertyChangeByType(final boolean observe, final GroupTypeId groupTypeId) {
		EventLabel<GroupPropertyChangeObservationEvent> eventLabel = GroupPropertyChangeObservationEvent.getEventLabelByGroupType(context, groupTypeId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the given group property changes for all
	 * groups of the given group type
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_TYPE_ID} if the group type
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_TYPE_ID} if the group
	 *             type is unknown *
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if the group
	 *             property id null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_PROPERTY_ID} if the
	 *             group property id is unknown
	 *
	 */

	public void observeGroupPropertyChangeByTypeAndProperty(final boolean observe, final GroupTypeId groupTypeId, final GroupPropertyId groupPropertyId) {
		EventLabel<GroupPropertyChangeObservationEvent> eventLabel = GroupPropertyChangeObservationEvent.getEventLabelByGroupTypeAndProperty(context, groupTypeId, groupPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleGroupPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of property value changes on the given
	 * materials producer for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property is unknown
	 */

	public void observeMaterialsProducerPropertyChange(final boolean observe, final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		EventLabel<MaterialsProducerPropertyChangeObservationEvent> eventLabel = MaterialsProducerPropertyChangeObservationEvent.getEventLabelByMaterialsProducerAndProperty(context,
				materialsProducerId, materialsProducerPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleMaterialsProducerPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of resource level changes on all materials
	 * producers for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeMaterialsProducerResourceChange(final boolean observe, final ResourceId resourceId) {
		EventLabel<MaterialsProducerResourceChangeObservationEvent> eventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(context, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handleMaterialsProducerResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of resource level changes on the given
	 * materials producer for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeMaterialsProducerResourceChangeByMaterialsProducerId(final boolean observe, final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		EventLabel<MaterialsProducerResourceChangeObservationEvent> eventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByMaterialsProducerAndResource(context,
				materialsProducerId, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handleMaterialsProducerResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of people arriving at the given compartment
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 */
	public void observeCompartmentPersonArrival(final boolean observe, final CompartmentId compartmentId) {
		EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByArrivalCompartment(context, compartmentId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonCompartmentChangeObservationEventForArrival);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of people leaving from the given compartment
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 */
	public void observeCompartmentPersonDeparture(final boolean observe, final CompartmentId compartmentId) {
		EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByDepartureCompartment(context, compartmentId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonCompartmentChangeObservationEventForDeparture);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of a person moving from one compartment to
	 * another
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment is unknown
	 */
	public void observePersonCompartmentChange(final boolean observe, final PersonId personId) {
		EventLabel<PersonCompartmentChangeObservationEvent> eventLabel = PersonCompartmentChangeObservationEvent.getEventLabelByPerson(context, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonCompartmentChangeObservationEventForPerson);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of property value changes on the given person
	 * for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observePersonPropertyChange(final boolean observe, final PersonId personId, final PersonPropertyId personPropertyId) {
		EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByPersonAndProperty(context, personId, personPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of region changes on the given person for the
	 * calling component.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 */

	public void observePersonRegionChange(final boolean observe, final PersonId personId) {
		EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByPerson(context, personId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonRegionChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of people arriving in or departing from the
	 * given region for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 */

	public void observeRegionPersonArrival(final boolean observe, final RegionId regionId) {
		EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByArrivalRegion(context, regionId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonRegionChangeObservationEventByArrival);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of people arriving in or departing from the
	 * given region for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 */

	public void observeRegionPersonDeparture(final boolean observe, final RegionId regionId) {
		EventLabel<PersonRegionChangeObservationEvent> eventLabel = PersonRegionChangeObservationEvent.getEventLabelByDepartureRegion(context, regionId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonRegionChangeObservationEventByDeparture);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of resource level changes on the given person
	 * for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observePersonResourceChange(final boolean observe, final PersonId personId, final ResourceId resourceId) {

		EventLabel<PersonResourceChangeObservationEvent> eventLabel = PersonResourceChangeObservationEvent.getEventLabelByPersonAndResource(context, personId, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of property value changes on all people in a
	 * particular region for the calling component.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observeRegionPersonPropertyChange(final boolean observe, final RegionId regionId, final PersonPropertyId personPropertyId) {
		EventLabel<PersonPropertyChangeObservationEvent> eventLabel = PersonPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(context, regionId, personPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of resource level changes on all people in
	 * the given region for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeRegionPersonResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId) {
		EventLabel<PersonResourceChangeObservationEvent> eventLabel = PersonResourceChangeObservationEvent.getEventLabelByRegionAndResource(context, regionId, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handlePersonResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of property value changes on the given region
	 * for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observeRegionPropertyChange(final boolean observe, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		EventLabel<RegionPropertyChangeObservationEvent> eventLabel = RegionPropertyChangeObservationEvent.getEventLabelByRegionAndProperty(context, regionId, regionPropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleRegionPropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of resource level changes on the given region
	 * for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 */

	public void observeRegionResourceChange(final boolean observe, final RegionId regionId, final ResourceId resourceId) {
		EventLabel<RegionResourceChangeObservationEvent> eventLabel = RegionResourceChangeObservationEvent.getEventLabelByRegionAndResource(context, regionId, resourceId);
		if (observe) {
			context.subscribe(eventLabel, component::handleRegionResourceChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of property value changes on the given
	 * resource for the calling component.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property is unknown
	 */

	public void observeResourcePropertyChange(final boolean observe, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		EventLabel<ResourcePropertyChangeObservationEvent> eventLabel = ResourcePropertyChangeObservationEvent.getEventLabel(context, resourceId, resourcePropertyId);
		if (observe) {
			context.subscribe(eventLabel, component::handleResourcePropertyChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of changes to the offer state of all stages.
	 *
	 */
	public void observeStageOfferChange(final boolean observe) {
		EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByAll(context);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageOfferChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of changes to the offer state of the stage.
	 *
	 * @throws ContractException
	 *
	 *
	 */

	public void observeStageOfferChangeByStageId(final boolean observe, final StageId stageId) {
		EventLabel<StageOfferChangeObservationEvent> eventLabel = StageOfferChangeObservationEvent.getEventLabelByStage(context, stageId);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageOfferChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}
	}

	/**
	 * Starts or stops observation of the transfer of all stages.
	 *
	 * @throws ContractException
	 *
	 *
	 */

	public void observeStageTransfer(final boolean observe) {
		EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByAll(context);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageMaterialsProducerChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the transfer of stages to the given
	 * destination materials producer
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             destination materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             destination materials producer id is unknown
	 */

	public void observeStageTransferByDestinationMaterialsProducerId(final boolean observe, final MaterialsProducerId destinationMaterialsProducerId) {
		EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByDestination(context, destinationMaterialsProducerId);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageMaterialsProducerChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the transfer of stages from the given
	 * source materials producer
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             source materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             source materials producer id is unknown
	 */

	public void observeStageTransferBySourceMaterialsProducerId(final boolean observe, final MaterialsProducerId sourceMaterialsProducerId) {
		EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelBySource(context, sourceMaterialsProducerId);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageMaterialsProducerChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Starts or stops observation of the transfer of a stage
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if the stage id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if the stage id is
	 *             unknown
	 */

	public void observeStageTransferByStageId(final boolean observe, final StageId stageId) {
		EventLabel<StageMaterialsProducerChangeObservationEvent> eventLabel = StageMaterialsProducerChangeObservationEvent.getEventLabelByStage(context, stageId);
		if (observe) {
			context.subscribe(eventLabel, component::handleStageMaterialsProducerChangeObservationEvent);
		} else {
			context.unsubscribe(eventLabel);
		}

	}

	/**
	 * Returns true if and only if the a population partition exists with the
	 * given key. Tolerates null key.
	 */

	public boolean partitionExists(final Object key) {
		return partitionDataView.partitionExists(key);
	}

	/**
	 * Returns true if and only if the given person identifier is associated
	 * with a person in the simulation. Tolerates null person id.
	 *
	 */

	public boolean personExists(final PersonId personId) {
		return personDataView.personExists(personId);
	}

	/**
	 *
	 * Returns true if and only if the person is associated with the index
	 * specified by the population index keys.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population index
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_LABEL_SET} if the label set is
	 *             null *
	 *             <li>{@link NucleusError#INCOMPATIBLE_LABEL_SET} if the label
	 *             set is incompatible with the population partition
	 *
	 */

	public boolean personIsInPopulationPartition(final PersonId personId, final Object key, final LabelSet labelSet) {
		return partitionDataView.contains(personId, labelSet, key);
	}

	/**
	 *
	 * Releases an output item to the OutputItemManger
	 *
	 */

	public void releaseOutput(final Object output) {
		context.releaseOutput(output);
	}

	/**
	 * Removes the group associated with the given group type and group
	 * identifiers. People associated with the group are removed from the group.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is not a global component, a region
	 *             component or a compartment component
	 */

	public void removeGroup(final GroupId groupId) {
		context.resolveEvent(new GroupRemovalRequestEvent(groupId));
	}

	/**
	 * Removes a partitiong.
	 *
	 *
	 * Partitions may only be removed by the components that create them.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population index
	 *             <li>{@link NucleusError#PARTITION_DELETION_BY_NON_OWNER} if
	 *             the invoker is not the component that created the index
	 *
	 */

	public void removePartition(final Object key) {
		context.resolveEvent(new PartitionRemovalEvent(key));
	}

	/**
	 * Removes the person associated with the given person identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the personId is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the personId is
	 *             unknown
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not the compartment containing the person
	 *
	 */

	public void removePerson(final PersonId personId) {
		context.resolveEvent(new PersonRemovalRequestEvent(personId));
	}

	/**
	 * Removes a person to the group associated with the given group type and
	 * group identifiers.
	 *
	 * @throws ContractException
	 *             <li>{@link ErrorType#} if the group id is null
	 *             <li>{@link ErrorType#} if the group id is unknown(group does
	 *             not exist)
	 *             <li>{@link ErrorType#} if the person id is null
	 *             <li>{@link ErrorType#} if the person id is unknown
	 *             <li>{@link ErrorType#} if the person is not a member of the
	 *             group
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoking component is no either a global component, a region
	 *             or a compartment
	 */

	public void removePersonFromGroup(final PersonId personId, final GroupId groupId) {
		context.resolveEvent(new GroupMembershipRemovalEvent(personId, groupId));
	}

	/**
	 * Removes a plan. The plan is identified by the key provided. Returns an
	 * empty optional if the plan cannot be found.
	 *
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PLAN_KEY} if the key is null
	 *
	 */

	public <T> Optional<T> removePlan(final Object key) {
		return context.removePlan(key);
	}

	/**
	 * Removes the amount of resource to the given person.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the amount the person possesses
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a global component or the person's region or
	 *             the person's compartment
	 *
	 */

	public void removeResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		context.resolveEvent(new PersonResourceRemovalEvent(resourceId, personId, amount));
	}

	/**
	 * Removes the amount of resource to the given region.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the amount the region possesses
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a global component or the given region
	 *
	 */

	public void removeResourceFromRegion(final ResourceId resourceId, final RegionId regionId, final long amount) {
		context.resolveEvent(new RegionResourceRemovalEvent(resourceId, regionId, amount));
	}

	/**
	 * Returns a randomly contacted person from the group specified by the
	 * groupId and {@link GroupSampler}. Optional result will reflect when no
	 * selection was possible.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown(group does not exist)
	 *             <li>{@link NucleusError#MALFORMED_WEIGHTING_FUNCTION} if the
	 *             groupWeightingFunction is malformed. (some evaluate to
	 *             negative numbers, etc. -- note that if all weights are zero
	 *             then the optional will return an isPresent() of false)
	 *             <li>{@link NucleusError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the group sampler's randomNumberGeneratorId does not
	 *             correspond to an existing RandomNumberGeneratorId found in
	 *             the scenario.
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the
	 *             excludedPersonId is unknown
	 *             <li>{@link NucleusError#NULL_GROUP_SAMPLER} if the group
	 *             sampler is null
	 */

	public Optional<PersonId> sampleGroup(final GroupId groupId, final GroupSampler groupSampler) {
		return personGroupDataView.sampleGroup(groupId, groupSampler);
	}

	/**
	 * Returns a randomly selected person identifier from a population
	 * partition. Returns null if the population partition is empty.
	 *
	 * @throws ContractException
	 *             <li>{@link NucleusError#NULL_PARTITION_KEY} if the key is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_POPULATION_PARTITION_KEY} if
	 *             the key does not correspond to an existing population index
	 *             <li>{@link NucleusError#NULL_PARTITION_SAMPLER} if the
	 *             partition sampler is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_LABEL_SET} if the
	 *             partition sampler's label set is incompatible with the
	 *             population partition
	 *             <li>{@link NucleusError#UNKNOWN_RANDOM_NUMBER_GENERATOR_ID}
	 *             if the partition sampler's randomNumberGeneratorId does not
	 *             correspond to an existing random Number Generator Id in the
	 *             scenario
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the partition
	 *             sampler's excluded person is unknown
	 *
	 */

	public Optional<PersonId> samplePartition(final Object key, final PartitionSampler partitionSampler) {
		return partitionDataView.samplePartition(key, partitionSampler);
	}

	/**
	 * Sets a property value on the indicated batch.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if batch id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if batch id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_ID} if property
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_BATCH_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if the
	 *             batch is part of an offered stage
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 */

	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue) {
		context.resolveEvent(new BatchPropertyValueAssignmentEvent(batchId, batchPropertyId, batchPropertyValue));
	}

	/**
	 * Sets property value for the given compartment and property. Compartment
	 * properties may only be set by the owning compartment.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_PROPERTY_ID} if
	 *             the property id is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_PROPERTY_VALUE} if
	 *             the value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component of the given compartment
	 *
	 */

	public void setCompartmentPropertyValue(final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId, final Object compartmentPropertyValue) {
		context.resolveEvent(new CompartmentPropertyValueAssignmentEvent(compartmentId, compartmentPropertyId, compartmentPropertyValue));
	}

	/**
	 * Sets property value for the given global property identifier.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_GLOBAL_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_GLOBAL_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component
	 *
	 */

	public void setGlobalPropertyValue(final GlobalPropertyId globalPropertyId, final Object globalPropertyValue) {
		context.resolveEvent(new GlobalPropertyValueAssignmentEvent(globalPropertyId, globalPropertyValue));
	}

	/**
	 * Sets a property value on the indicated group.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_GROUP_ID} if the group id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_GROUP_ID} if the group id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if property
	 *             id is null
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_ID} if property
	 *             id is unknown
	 *             <li>{@link NucleusError#NULL_GROUP_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component, region component or
	 *             compartment component
	 *
	 *
	 */

	public void setGroupPropertyValue(final GroupId groupId, final GroupPropertyId groupPropertyId, final Object groupPropertyValue) {
		context.resolveEvent(new GroupPropertyValueAssignmentEvent(groupId, groupPropertyId, groupPropertyValue));
	}

	/**
	 * Sets property value for the given materials producer and property.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the property id is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE}
	 *             if the value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the given materials
	 *             producer
	 *
	 */

	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		context.resolveEvent(new MaterialsProducerPropertyValueAssignmentEvent(materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue));
	}

	/**
	 * Sets the person's compartment. Compartment assignment may only be set by
	 * the owning compartment, except for person creation.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_COMPARTMENT_ID} if the
	 *             compartment id is null
	 *             <li>{@link NucleusError#UNKNOWN_COMPARTMENT_ID} if the
	 *             compartment id is unknown
	 *             <li>{@link NucleusError#SAME_COMPARTMENT} if the compartment
	 *             id is currently assigned to the person
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the person's current
	 *             compartment
	 *
	 */

	public void setPersonCompartment(final PersonId personId, final CompartmentId compartmentId) {
		context.resolveEvent(new PersonCompartmentAssignmentEvent(personId, compartmentId));
	}

	/**
	 * Sets property value for the given person and property.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_VALUE} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_PERSON_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the the person's current
	 *             region or the the person's current compartment
	 *
	 */

	public void setPersonPropertyValue(final PersonId personId, final PersonPropertyId personPropertyId, final Object personPropertyValue) {
		context.resolveEvent(new PersonPropertyValueAssignmentEvent(personId, personPropertyId, personPropertyValue));
	}

	/**
	 * Sets the person's region. Region assignment may only be set by the owning
	 * compartment, except for person creation.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#SAME_REGION} if the region is the
	 *             current region for the person
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the person's current
	 *             region
	 *
	 */

	public void setPersonRegion(final PersonId personId, final RegionId regionId) {
		context.resolveEvent(new PersonRegionAssignmentEvent(personId, regionId));
	}

	/**
	 * Sets property value for the given region and property.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the region id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the region id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_REGION_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component or the given region
	 *
	 */

	public void setRegionPropertyValue(final RegionId regionId, final RegionPropertyId regionPropertyId, final Object regionPropertyValue) {
		context.resolveEvent(new RegionPropertyValueAssignmentEvent(regionId, regionPropertyId, regionPropertyValue));
	}

	/**
	 * Sets property value for the given resource and property.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_ID} if the
	 *             property id is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_PROPERTY_ID} if the
	 *             property id is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_PROPERTY_VALUE} if the
	 *             value is null
	 *             <li>{@link NucleusError#INCOMPATIBLE_VALUE} if the value is
	 *             incompatible with the defined type for the property
	 *             <li>{@link NucleusError#IMMUTABLE_VALUE} if the property has
	 *             been defined as immutable
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if the
	 *             invoker is not a global component
	 *
	 */

	public void setResourcePropertyValue(final ResourceId resourceId, final ResourcePropertyId resourcePropertyId, final Object resourcePropertyValue) {
		context.resolveEvent(new ResourcePropertyValueAssignmentEvent(resourceId, resourcePropertyId, resourcePropertyValue));
	}

	/**
	 * Sets the offer state for a stage owned by the invoking materials
	 * producer. An offered stage is available for transfer to another materials
	 * producer, but has batches that cannot be mutated until the stage's
	 * offered state is either set to false or is transferred to another
	 * materials producer.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component
	 *
	 */

	public void setStageOffer(final StageId stageId, final boolean offer) {
		context.resolveEvent(new StageOfferEvent(stageId, offer));
	}

	/**
	 * Transfers the given amount from one batch to another. The batches must be
	 * distinct and owned by the invoking materials producer component and
	 * neither may be part of an offered stage.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the source batch id
	 *             is null
	 *             <li>{@link NucleusError#NULL_BATCH_ID} if the destination
	 *             batch id is null
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the source batch
	 *             id is unknown
	 *             <li>{@link NucleusError#UNKNOWN_BATCH_ID} if the destination
	 *             batch id is unknown
	 *             <li>{@link NucleusError#REFLEXIVE_BATCH_SHIFT} if the source
	 *             and destination batch ids are equal
	 *             <li>{@link NucleusError#MATERIAL_TYPE_MISMATCH} if the
	 *             material ids of the batches are not equal
	 *             <li>{@link NucleusError#BATCH_SHIFT_WITH_MULTIPLE_OWNERS}if
	 *             the batches are owned by different material producers
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if the
	 *             source batch is part of an offered stage
	 *             <li>{@link NucleusError#OFFERED_STAGE_UNALTERABLE} if the
	 *             destination batch is part of an offered stage
	 *             <li>{@link NucleusError#NEGATIVE_MATERIAL_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError.NON_FINITE_MATERIAL_AMOUNT} if the
	 *             amount is not finite
	 *             <li>{@link NucleusError.MATERIAL_ARITHMETIC_EXCEPTION} if the
	 *             amount causes the receiving batch overflow to infinity
	 * 
	 *             <li>{@link NucleusError.MATERIAL_NON_FINITE_ARITHMETIC_EXCEPTION}
	 *             if the amount results in a non finite amount in the receiving
	 *             batch
	 *             <li>{@link NucleusError#INSUFFICIENT_MATERIAL_AVAILABLE} if
	 *             the amount exceeds the capacity of the source batch
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component for
	 *             both batches
	 */

	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		context.resolveEvent(new BatchContentShiftEvent(sourceBatchId, destinationBatchId, amount));
	}

	/**
	 * Returns true if and only if there is a stage with the given id. Tolerates
	 * null stage id.
	 */

	public boolean stageExists(final StageId stageId) {
		return materialsDataView.stageExists(stageId);
	}

	/**
	 * Transfers an offered stage to the provided materials producer. Once
	 * transferred, the stage will not be in the offered state.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_STAGE_ID} if stage id is null
	 *             <li>{@link NucleusError#UNKNOWN_STAGE_ID} if stage id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if the
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 *             materials producer is unknown
	 *             <li>{@link NucleusError#UNOFFERED_STAGE_NOT_TRANSFERABLE} if
	 *             stage is not in the offered state
	 *             <li>{@link NucleusError#REFLEXIVE_STAGE_TRANSFER} if the
	 *             material producer is the invoking materials producer
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a materials producer component or a global
	 *             component
	 *
	 */

	public void transferOfferedStageToMaterialsProducer(final StageId stageId, final MaterialsProducerId materialsProducerId) {
		context.resolveEvent(new OfferedStageTransferToMaterialsProducerEvent(stageId, materialsProducerId));
	}

	/**
	 * Transfers an amount of resource from a materials producer to a region.
	 *
	 * @throws ContractException
	 *
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if region id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if region id is
	 *             unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if resource id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if resource id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_MATERIALS_PRODUCER_ID} if
	 *             materials producer id is null
	 *             <li>{@link NucleusError#UNKNOWN_MATERIALS_PRODUCER_ID} if
	 *             materials producer id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow of the corresponding region's
	 *             inventory level
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the the resource level of the
	 *             materials producer
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not the owning materials producer component, the
	 *             receiving region or a global component
	 *
	 */

	public void transferProducedResourceToRegion(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final RegionId regionId, final long amount) {
		context.resolveEvent(new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount));
	}

	/**
	 * Transfers the amount of resource from one region to another
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the source region
	 *             id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the source
	 *             region id is unknown
	 *             <li>{@link NucleusError#NULL_REGION_ID} if the destination
	 *             region id is null
	 *             <li>{@link NucleusError#UNKNOWN_REGION_ID} if the destination
	 *             region id is unknown
	 *             <li>{@link NucleusError#REFLEXIVE_RESOURCE_TRANSFER} if the
	 *             source region id and the destination region are the same
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow of the corresponding
	 *             destination region's inventory level
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the amount the source region
	 *             possesses
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a global component
	 *
	 */

	public void transferResourceBetweenRegions(final ResourceId resourceId, final RegionId sourceRegionId, final RegionId destinationRegionId, final long amount) {
		context.resolveEvent(new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId, amount));
	}

	/**
	 * Transfers the amount of resource from the given person back to their
	 * region.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount results in an overflow of the corresponding region's
	 *             inventory level
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the amount the person possesses
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not a global component or the person's region or
	 *             the person's compartment
	 *
	 */

	public void transferResourceFromPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		context.resolveEvent(new ResourceTransferFromPersonEvent(resourceId, personId, amount));
	}

	/**
	 * Transfers the amount of resource to the given person back from their
	 * region.
	 *
	 * @throws ContractException
	 *
	 *             <li>{@link NucleusError#NULL_PERSON_ID} if the person id is
	 *             null
	 *             <li>{@link NucleusError#UNKNOWN_PERSON_ID} if the person id
	 *             is unknown
	 *             <li>{@link NucleusError#NULL_RESOURCE_ID} if the resource id
	 *             is null
	 *             <li>{@link NucleusError#UNKNOWN_RESOURCE_ID} if the resource
	 *             id is unknown
	 *             <li>{@link NucleusError#NEGATIVE_RESOURCE_AMOUNT} if the
	 *             amount is negative
	 *             <li>{@link NucleusError#RESOURCE_ARITHMETIC_EXCEPTION} if the
	 *             amount is negative if the amount results in an overflow of
	 *             the corresponding person's inventory level
	 *             <li>{@link NucleusError#INSUFFICIENT_RESOURCES_AVAILABLE} if
	 *             the amount is in excess of the amount the region possesses
	 *             <li>{@link NucleusError#COMPONENT_LACKS_PERMISSION} if
	 *             invoker is not global component , the person's region or the
	 *             person's compartment
	 *
	 */

	public void transferResourceToPerson(final ResourceId resourceId, final PersonId personId, final long amount) {
		context.resolveEvent(new ResourceTransferToPersonEvent(resourceId, personId, amount));
	}
}