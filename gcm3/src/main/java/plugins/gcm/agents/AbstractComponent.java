package plugins.gcm.agents;

import java.util.List;

import nucleus.AgentContext;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.globals.events.observation.GlobalPropertyChangeObservationEvent;
import plugins.globals.support.GlobalPropertyId;
import plugins.groups.events.observation.GroupCreationObservationEvent;
import plugins.groups.events.observation.GroupImminentRemovalObservationEvent;
import plugins.groups.events.observation.GroupMembershipAdditionObservationEvent;
import plugins.groups.events.observation.GroupMembershipRemovalObservationEvent;
import plugins.groups.events.observation.GroupPropertyChangeObservationEvent;
import plugins.groups.support.GroupId;
import plugins.groups.support.GroupPropertyId;
import plugins.materials.events.observation.MaterialsProducerPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerResourceChangeObservationEvent;
import plugins.materials.events.observation.StageImminentRemovalObservationEvent;
import plugins.materials.events.observation.StageMaterialsProducerChangeObservationEvent;
import plugins.materials.events.observation.StageOfferChangeObservationEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.people.events.observation.BulkPersonCreationObservationEvent;
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
import plugins.people.support.BulkPersonContructionData;
import plugins.people.support.PersonContructionData;
import plugins.people.support.PersonId;
import plugins.personproperties.events.observation.PersonPropertyChangeObservationEvent;
import plugins.personproperties.support.PersonPropertyId;
import plugins.regions.events.observation.PersonRegionChangeObservationEvent;
import plugins.regions.events.observation.RegionPropertyChangeObservationEvent;
import plugins.regions.support.RegionId;
import plugins.regions.support.RegionPropertyId;
import plugins.resources.events.observation.PersonResourceChangeObservationEvent;
import plugins.resources.events.observation.RegionResourceChangeObservationEvent;
import plugins.resources.events.observation.ResourcePropertyChangeObservationEvent;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;

/**
 * An abstract implementation of the Component interface that throws a
 * RunTimeException for all methods except for init() and close() methods.
 * Init() remains unimplemented here and close() is an empty implementation.
 *
 * Modelers who write descendant classes should override each method that
 * corresponds to a data change that their component registers to observe.
 *
 * @author Shawn Hatch
 *
 */
public class AbstractComponent {

	private Environment environment;

	protected Environment getEnvironment() {
		return environment;
	}

	
	public void init(AgentContext agentContext) {
		environment = new Environment();
		environment.init(agentContext, this);
		init(environment);
	}

	protected void init(Environment environment) {

	}

	protected void executePlan(final Environment environment, final Plan plan) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a
	 * compartment's property value has changed.
	 *
	 * @param compartmentId
	 * @param compartmentPropertyId
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */

	protected void observeCompartmentPropertyChange(final Environment environment, final CompartmentId compartmentId, final CompartmentPropertyId compartmentPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component that a person has arrived
	 * into the simulation.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGlobalPersonArrival(final Environment environment, final PersonId personId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has departed the simulation. Person id values, unlike all other id
	 * values, are assigned by the simulation and are re-issued on an as needed
	 * basis. Therefore the departure of a person with id 1234 may be followed
	 * by the arrival of another person with the same id.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGlobalPersonDeparture(final Environment environment, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component that a global level
	 * property value has changed.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGlobalPropertyChange(final Environment environment, final GlobalPropertyId globalPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a materials
	 * producer's property value has changed.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeMaterialsProducerPropertyChange(final Environment environment, final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * level has changed for the given materialsProducer
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeMaterialsProducerResourceChange(final Environment environment, final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has arrived at the compartment.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeCompartmentPersonArrival(final Environment environment, final CompartmentId compartmentId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has departed from the compartment.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */

	protected void observeCompartmentPersonDeparture(final Environment environment, final CompartmentId compartmentId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved from one compartment to another
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observePersonCompartmentChange(final Environment environment, final CompartmentId previousCompartmentId, final CompartmentId currentCompartmentId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person's
	 * property value has changed.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observePersonPropertyChange(final Environment environment, final PersonId personId, final PersonPropertyId personPropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved to a new region.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observePersonRegionChange(final Environment environment, final RegionId previousRegionId, final RegionId currentRegionId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved to a new region.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeRegionPersonArrival(final Environment environment, final RegionId regionId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has moved to a new region.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeRegionPersonDeparture(final Environment environment, final RegionId regionId, final PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has had a resource level change.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observePersonResourceChange(final Environment environment, final PersonId personId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a region's
	 * property value has changed.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeRegionPropertyChange(final Environment environment, final RegionId regionId, final RegionPropertyId regionPropertyId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * level has changed for the given region
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeRegionResourceChange(final Environment environment, final RegionId regionId, final ResourceId resourceId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a resource
	 * property has changed for the given resource
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeResourcePropertyChange(final Environment environment, final ResourceId resourceId, final ResourcePropertyId resourcePropertyId) {
		throwNoConcreteImplementation();

	}

	protected void observeStageDeparture(final Environment environment, final StageId stageId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a stage's
	 * offer state has changed.
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeStageOfferChange(final Environment environment, final StageId stageId) {
		throwNoConcreteImplementation();

	}

	/**
	 * An alert from the simulation to the component indicating that a stage has
	 * been transferred from on materials producer to another.
	 *
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeStageTransfer(final Environment environment, final StageId stageId, MaterialsProducerId sourceMaterialsProducerId, MaterialsProducerId destinationMaterialsProducerId) {
		throwNoConcreteImplementation();

	}

	private void throwNoConcreteImplementation() {
		throw new RuntimeException("No concrete implementation " + getClass().getName());
	}

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * been created.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGroupConstruction(Environment environment, GroupId groupId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * been destroyed.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGroupDestruction(Environment environment, GroupId groupId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a group has
	 * had a property value change.
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGroupPropertyChange(Environment environment, GroupId groupId, GroupPropertyId groupPropertyId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been added to a group
	 *
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGroupPersonArrival(Environment environment, GroupId groupId, PersonId personId) {
		throwNoConcreteImplementation();
	}

	/**
	 * An alert from the simulation to the component indicating that a person
	 * has been removed from a group
	 * 
	 * @throws RuntimeException
	 *             if invoked
	 */
	protected void observeGroupPersonDeparture(Environment environment, GroupId groupId, PersonId personId) {
		throwNoConcreteImplementation();
	}

	protected void handleCompartmentPropertyChangeObservationEvent(AgentContext context, CompartmentPropertyChangeObservationEvent compartmentPropertyChangeObservationEvent) {
		CompartmentId compartmentId = compartmentPropertyChangeObservationEvent.getCompartmentId();
		CompartmentPropertyId compartmentPropertyId = compartmentPropertyChangeObservationEvent.getCompartmentPropertyId();
		observeCompartmentPropertyChange(environment, compartmentId, compartmentPropertyId);
	}

	protected void handleGlobalPropertyChangeObservationEvent(AgentContext context, GlobalPropertyChangeObservationEvent globalPropertyChangeObservationEvent) {
		GlobalPropertyId globalPropertyId = globalPropertyChangeObservationEvent.getGlobalPropertyId();
		observeGlobalPropertyChange(environment, globalPropertyId);
	}

	protected void handleResourcePropertyChangeObservationEvent(AgentContext context, ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent) {
		ResourceId resourceId = resourcePropertyChangeObservationEvent.getResourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyChangeObservationEvent.getResourcePropertyId();
		observeResourcePropertyChange(environment, resourceId, resourcePropertyId);
	}

	protected void handleRegionPropertyChangeObservationEvent(AgentContext context, RegionPropertyChangeObservationEvent regionPropertyChangeObservationEvent) {
		RegionId regionId = regionPropertyChangeObservationEvent.getRegionId();
		RegionPropertyId regionPropertyId = regionPropertyChangeObservationEvent.getRegionPropertyId();
		observeRegionPropertyChange(environment, regionId, regionPropertyId);
	}

	protected void handlePersonPropertyChangeObservationEvent(AgentContext context, PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonId personId = personPropertyChangeObservationEvent.getPersonId();
		PersonPropertyId personPropertyId = personPropertyChangeObservationEvent.getPersonPropertyId();
		observePersonPropertyChange(environment, personId, personPropertyId);
	}

	protected void handlePersonResourceChangeObservationEvent(AgentContext context, PersonResourceChangeObservationEvent personResourceChangeObservationEvent) {
		PersonId personId = personResourceChangeObservationEvent.getPersonId();
		ResourceId resourceId = personResourceChangeObservationEvent.getResourceId();
		observePersonResourceChange(environment, personId, resourceId);
	}

	protected void handlePersonCreationObservationEvent(AgentContext context, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		observeGlobalPersonArrival(environment, personId);
	}
	
	protected void handleBulkPersonCreationObservationEvent(AgentContext context, BulkPersonCreationObservationEvent bulkPersonCreationObservationEvent) {
		PersonId personId = bulkPersonCreationObservationEvent.getPersonId();
		int pId = personId.getValue();
		BulkPersonContructionData bulkPersonContructionData = bulkPersonCreationObservationEvent.getBulkPersonContructionData();
		List<PersonContructionData> personContructionDatas = bulkPersonContructionData.getPersonContructionDatas();
		for (int i = 0;i< personContructionDatas.size();i++) {			
			PersonId boxedPersonId = environment.getBoxedPersonId(pId);
			pId++;
			observeGlobalPersonArrival(environment, boxedPersonId);
		}
	}

	protected void handlePersonImminentRemovalObservationEvent(AgentContext context, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		PersonId personId = personImminentRemovalObservationEvent.getPersonId();
		observeGlobalPersonDeparture(environment, personId);
	}

	protected void handleGroupMembershipAdditionObservationEvent(AgentContext context, GroupMembershipAdditionObservationEvent groupMembershipAdditionObservationEvent) {
		GroupId groupId = groupMembershipAdditionObservationEvent.getGroupId();
		PersonId personId = groupMembershipAdditionObservationEvent.getPersonId();
		observeGroupPersonArrival(environment, groupId, personId);
	}

	protected void handleGroupMembershipRemovalObservationEvent(AgentContext context, GroupMembershipRemovalObservationEvent groupMembershipRemovalObservationEvent) {
		GroupId groupId = groupMembershipRemovalObservationEvent.getGroupId();
		PersonId personId = groupMembershipRemovalObservationEvent.getPersonId();
		observeGroupPersonDeparture(environment, groupId, personId);
	}

	protected void handleGroupCreationObservationEvent(AgentContext context, GroupCreationObservationEvent groupCreationObservationEvent) {
		GroupId groupId = groupCreationObservationEvent.getGroupId();
		observeGroupConstruction(environment, groupId);
	}

	protected void handleGroupImminentRemovalObservationEvent(AgentContext context, GroupImminentRemovalObservationEvent groupImminentRemovalObservationEvent) {
		GroupId groupId = groupImminentRemovalObservationEvent.getGroupId();
		observeGroupDestruction(environment, groupId);
	}

	protected void handleGroupPropertyChangeObservationEvent(AgentContext context, GroupPropertyChangeObservationEvent groupPropertyChangeObservationEvent) {
		GroupId groupId = groupPropertyChangeObservationEvent.getGroupId();
		GroupPropertyId groupPropertyId = groupPropertyChangeObservationEvent.getGroupPropertyId();
		observeGroupPropertyChange(environment, groupId, groupPropertyId);
	}

	protected void handleStageOfferChangeObservationEvent(AgentContext context, StageOfferChangeObservationEvent stageOfferChangeObservationEvent) {
		StageId stageId = stageOfferChangeObservationEvent.getStageId();
		observeStageOfferChange(environment, stageId);
	}

	protected void handleStageMaterialsProducerChangeObservationEvent(AgentContext context, StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent) {
		StageId stageId = stageMaterialsProducerChangeObservationEvent.getStageId();
		MaterialsProducerId previousMaterialsProducerId = stageMaterialsProducerChangeObservationEvent.getPreviousMaterialsProducerId();
		MaterialsProducerId currentMaterialsProducerId = stageMaterialsProducerChangeObservationEvent.getCurrentMaterialsProducerId();
		observeStageTransfer(environment, stageId, previousMaterialsProducerId, currentMaterialsProducerId);
	}

	protected void handleRegionResourceChangeObservationEvent(AgentContext context, RegionResourceChangeObservationEvent regionResourceChangeObservationEvent) {
		RegionId regionId = regionResourceChangeObservationEvent.getRegionId();
		ResourceId resourceId = regionResourceChangeObservationEvent.getResourceId();
		observeRegionResourceChange(environment, regionId, resourceId);
	}

	protected void handleMaterialsProducerPropertyChangeObservationEvent(AgentContext context, MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerPropertyId();
		observeMaterialsProducerPropertyChange(environment, materialsProducerId, materialsProducerPropertyId);
	}

	protected void handleStageImminentRemovalObservationEvent(AgentContext context, StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent) {
		StageId stageId = stageImminentRemovalObservationEvent.getStageId();
		observeStageDeparture(environment, stageId);
	}

	protected void handleMaterialsProducerResourceChangeObservationEvent(AgentContext context, MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerResourceChangeObservationEvent.getMaterialsProducerId();
		ResourceId resourceId = materialsProducerResourceChangeObservationEvent.getResourceId();
		observeMaterialsProducerResourceChange(environment, materialsProducerId, resourceId);
	}

	protected void handlePersonCompartmentChangeObservationEventForArrival(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		CompartmentId currentCompartmentId = personCompartmentChangeObservationEvent.getCurrentCompartmentId();
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		observeCompartmentPersonArrival(environment, currentCompartmentId, personId);

	}

	protected void handlePersonCompartmentChangeObservationEventForDeparture(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		CompartmentId previousCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		observeCompartmentPersonDeparture(environment, previousCompartmentId, personId);
	}

	protected void handlePersonCompartmentChangeObservationEventForPerson(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		CompartmentId currentCompartmentId = personCompartmentChangeObservationEvent.getCurrentCompartmentId();
		CompartmentId previousCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		observePersonCompartmentChange(environment, previousCompartmentId, currentCompartmentId, personId);

	}

	protected void handlePersonRegionChangeObservationEvent(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		observePersonRegionChange(environment, previousRegionId, currentRegionId, personId);
	}

	protected void handlePersonRegionChangeObservationEventByArrival(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		observeRegionPersonArrival(environment, currentRegionId, personId);
	}

	protected void handlePersonRegionChangeObservationEventByDeparture(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		observeRegionPersonDeparture(environment, previousRegionId, personId);
	}

}
