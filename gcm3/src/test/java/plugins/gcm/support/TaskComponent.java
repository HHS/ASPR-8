package plugins.gcm.support;

import java.util.List;

import nucleus.AgentContext;
import plugins.compartments.events.observation.CompartmentPropertyChangeObservationEvent;
import plugins.compartments.events.observation.PersonCompartmentChangeObservationEvent;
import plugins.compartments.support.CompartmentId;
import plugins.compartments.support.CompartmentPropertyId;
import plugins.components.support.ComponentId;
import plugins.gcm.agents.AbstractComponent;
import plugins.gcm.agents.Environment;
import plugins.gcm.agents.Plan;
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
import plugins.people.events.observation.PersonCreationObservationEvent;
import plugins.people.events.observation.PersonImminentRemovalObservationEvent;
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
import util.MultiKey;
import util.MultiKey.Builder;

/**
 * Component implementor for all tests.
 *
 * The general @Test life cycle is to create a scenario, a replication and
 * possible some output item handlers and execute a simulation using them. Each
 * of the components added to the scenario should be TaskComponents. A
 * TaskPlanContainer is added to the scenario as a global property value and the
 * test fills this container with TaskPlans that are slated for specific
 * components to execute at specific times.
 * 
 * Each TaskComponent retrieves the list of TaskPlans from the TaskPlanContainer
 * during its init() method. Using the planning system, the component schedules
 * each TaskPlan for eventual execution. Each TaskPlan contains execution logic
 * that is contained in the unit test method.
 *
 * Task Plans often contain assertions that are executed during the simulation
 * run. However, it is impractical to test observations this way. The component
 * implements all of the observation methods of Component by recording the
 * observation and its time value in a MultiKey instance. These are retrievable
 * after the simulation run completes and are used in assertions on what was and
 * was not observed. Observations are collected in an ObservationContainer that
 * is optionally supplied by the test via a global property value. If the
 * execution logic of the Tasks in the test include registration for
 * observations, then the ObservationContainer must be present.
 */
public class TaskComponent extends AbstractComponent {

	private ComponentId id;
	private ObservationContainer observationContainer;

	@Override
	protected void executePlan(Environment environment, final Plan plan) {
		final TaskPlan taskPlan = (TaskPlan) plan;
		taskPlan.executeTask(environment);
	}

	private Environment environment;

	@Override
	public void init(AgentContext agentContext) {

		environment = new Environment();
		environment.init(agentContext, this);
		this.id = environment.getCurrentComponentId();
		TaskPlanContainer taskPlanContainer = environment.getGlobalPropertyValue(EnvironmentSupport.TASK_PLAN_CONTAINER_PROPERTY_ID);
		List<TaskPlan> taskPlans = taskPlanContainer.getTaskPlans(id);

		for (final TaskPlan taskPlan : taskPlans) {
			if (taskPlan.getKey() != null) {
				environment.addPlan(taskPlan, taskPlan.getScheduledTime(), taskPlan.getKey());
			} else {
				environment.addPlan(taskPlan, taskPlan.getScheduledTime());
			}
		}

		if (environment.getGlobalPropertyIds().contains(EnvironmentSupport.OBSERVATION_CONTAINER_PROPERTY_ID)) {
			observationContainer = environment.getGlobalPropertyValue(EnvironmentSupport.OBSERVATION_CONTAINER_PROPERTY_ID);
		}
	}

	@Override
	protected void handleCompartmentPropertyChangeObservationEvent(AgentContext context, CompartmentPropertyChangeObservationEvent compartmentPropertyChangeObservationEvent) {
		CompartmentId compartmentId = compartmentPropertyChangeObservationEvent.getCompartmentId();
		CompartmentPropertyId compartmentPropertyId = compartmentPropertyChangeObservationEvent.getCompartmentPropertyId();
		Object currentPropertyValue = compartmentPropertyChangeObservationEvent.getCurrentPropertyValue();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(compartmentPropertyChangeObservationEvent.getClass());
		builder.addKey(compartmentId);
		builder.addKey(compartmentPropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());

	}

	@Override
	protected void handleGlobalPropertyChangeObservationEvent(AgentContext context, GlobalPropertyChangeObservationEvent globalPropertyChangeObservationEvent) {
		GlobalPropertyId globalPropertyId = globalPropertyChangeObservationEvent.getGlobalPropertyId();
		Object currentPropertyValue = globalPropertyChangeObservationEvent.getCurrentPropertyValue();
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(globalPropertyChangeObservationEvent.getClass());
		builder.addKey(globalPropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleResourcePropertyChangeObservationEvent(AgentContext context, ResourcePropertyChangeObservationEvent resourcePropertyChangeObservationEvent) {

		ResourceId resourceId = resourcePropertyChangeObservationEvent.getResourceId();
		ResourcePropertyId resourcePropertyId = resourcePropertyChangeObservationEvent.getResourcePropertyId();
		Object currentPropertyValue = resourcePropertyChangeObservationEvent.getCurrentPropertyValue();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(resourcePropertyChangeObservationEvent.getClass());
		builder.addKey(resourceId);
		builder.addKey(resourcePropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());

	}

	@Override
	protected void handleRegionPropertyChangeObservationEvent(AgentContext context, RegionPropertyChangeObservationEvent regionPropertyChangeObservationEvent) {

		RegionId regionId = regionPropertyChangeObservationEvent.getRegionId();
		RegionPropertyId regionPropertyId = regionPropertyChangeObservationEvent.getRegionPropertyId();
		Object currentPropertyValue = regionPropertyChangeObservationEvent.getCurrentPropertyValue();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(regionPropertyChangeObservationEvent.getClass());
		builder.addKey(regionId);
		builder.addKey(regionPropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonPropertyChangeObservationEvent(AgentContext context, PersonPropertyChangeObservationEvent personPropertyChangeObservationEvent) {
		PersonId personId = personPropertyChangeObservationEvent.getPersonId();
		PersonPropertyId personPropertyId = personPropertyChangeObservationEvent.getPersonPropertyId();
		Object currentPropertyValue = personPropertyChangeObservationEvent.getCurrentPropertyValue();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personPropertyChangeObservationEvent.getClass());
		builder.addKey(personId);
		builder.addKey(personPropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonResourceChangeObservationEvent(AgentContext context, PersonResourceChangeObservationEvent personResourceChangeObservationEvent) {
		PersonId personId = personResourceChangeObservationEvent.getPersonId();
		ResourceId resourceId = personResourceChangeObservationEvent.getResourceId();
		long resourceLevel = personResourceChangeObservationEvent.getCurrentResourceLevel();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personResourceChangeObservationEvent.getClass());
		builder.addKey(personId);
		builder.addKey(resourceId);
		builder.addKey(resourceLevel);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonCreationObservationEvent(AgentContext context, PersonCreationObservationEvent personCreationObservationEvent) {
		PersonId personId = personCreationObservationEvent.getPersonId();
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personCreationObservationEvent.getClass());
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonImminentRemovalObservationEvent(AgentContext context, PersonImminentRemovalObservationEvent personImminentRemovalObservationEvent) {
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personImminentRemovalObservationEvent.getClass());
		builder.addKey(personImminentRemovalObservationEvent.getPersonId());
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleGroupMembershipAdditionObservationEvent(AgentContext context, GroupMembershipAdditionObservationEvent groupMembershipAdditionObservationEvent) {
		PersonId personId = groupMembershipAdditionObservationEvent.getPersonId();
		GroupId groupId = groupMembershipAdditionObservationEvent.getGroupId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(groupMembershipAdditionObservationEvent.getClass());
		builder.addKey(groupId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleGroupMembershipRemovalObservationEvent(AgentContext context, GroupMembershipRemovalObservationEvent groupMembershipRemovalObservationEvent) {
		PersonId personId = groupMembershipRemovalObservationEvent.getPersonId();
		GroupId groupId = groupMembershipRemovalObservationEvent.getGroupId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(groupMembershipRemovalObservationEvent.getClass());
		builder.addKey(groupId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleGroupCreationObservationEvent(AgentContext context, GroupCreationObservationEvent groupCreationObservationEvent) {
		GroupId groupId = groupCreationObservationEvent.getGroupId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(groupCreationObservationEvent.getClass());
		builder.addKey(groupId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleGroupImminentRemovalObservationEvent(AgentContext context, GroupImminentRemovalObservationEvent groupImminentRemovalObservationEvent) {

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(groupImminentRemovalObservationEvent.getClass());
		builder.addKey(groupImminentRemovalObservationEvent.getGroupId());
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleGroupPropertyChangeObservationEvent(AgentContext context, GroupPropertyChangeObservationEvent groupPropertyChangeObservationEvent) {
		GroupId groupId = groupPropertyChangeObservationEvent.getGroupId();
		GroupPropertyId groupPropertyId = groupPropertyChangeObservationEvent.getGroupPropertyId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(groupPropertyChangeObservationEvent.getClass());
		builder.addKey(groupId);
		builder.addKey(groupPropertyId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleStageOfferChangeObservationEvent(AgentContext context, StageOfferChangeObservationEvent stageOfferChangeObservationEvent) {
		StageId stageId = stageOfferChangeObservationEvent.getStageId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(stageOfferChangeObservationEvent.getClass());
		builder.addKey(stageId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleStageMaterialsProducerChangeObservationEvent(AgentContext context, StageMaterialsProducerChangeObservationEvent stageMaterialsProducerChangeObservationEvent) {
		StageId stageId = stageMaterialsProducerChangeObservationEvent.getStageId();
		MaterialsProducerId currentMaterialsProducerId = stageMaterialsProducerChangeObservationEvent.getCurrentMaterialsProducerId();
		MaterialsProducerId previousMaterialsProducerId = stageMaterialsProducerChangeObservationEvent.getPreviousMaterialsProducerId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(stageMaterialsProducerChangeObservationEvent.getClass());
		builder.addKey(stageId);
		builder.addKey(previousMaterialsProducerId);
		builder.addKey(currentMaterialsProducerId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleRegionResourceChangeObservationEvent(AgentContext context, RegionResourceChangeObservationEvent regionResourceChangeObservationEvent) {

		RegionId regionId = regionResourceChangeObservationEvent.getRegionId();
		ResourceId resourceId = regionResourceChangeObservationEvent.getResourceId();
		long currentResourceLevel = regionResourceChangeObservationEvent.getCurrentResourceLevel();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(regionResourceChangeObservationEvent.getClass());
		builder.addKey(regionId);
		builder.addKey(resourceId);
		builder.addKey(currentResourceLevel);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleMaterialsProducerPropertyChangeObservationEvent(AgentContext context, MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent) {

		MaterialsProducerId materialsProducerId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerPropertyId();
		Object currentPropertyValue = materialsProducerPropertyChangeObservationEvent.getCurrentPropertyValue();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(materialsProducerPropertyChangeObservationEvent.getClass());
		builder.addKey(materialsProducerId);
		builder.addKey(materialsProducerPropertyId);
		builder.addKey(currentPropertyValue);
		observationContainer.addObservation(builder.build());

	}

	@Override
	protected void handleStageImminentRemovalObservationEvent(AgentContext context, StageImminentRemovalObservationEvent stageImminentRemovalObservationEvent) {

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(stageImminentRemovalObservationEvent.getClass());
		builder.addKey(stageImminentRemovalObservationEvent.getStageId());
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handleMaterialsProducerResourceChangeObservationEvent(AgentContext context, MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerResourceChangeObservationEvent.getMaterialsProducerId();
		ResourceId resourceId = materialsProducerResourceChangeObservationEvent.getResourceId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(materialsProducerResourceChangeObservationEvent.getClass());
		builder.addKey(materialsProducerId);
		builder.addKey(resourceId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonCompartmentChangeObservationEventForArrival(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId currentCompartmentId = personCompartmentChangeObservationEvent.getCurrentCompartmentId();
		CompartmentId previousCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personCompartmentChangeObservationEvent.getClass());
		builder.addKey(previousCompartmentId);
		builder.addKey(currentCompartmentId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonCompartmentChangeObservationEventForDeparture(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId currentCompartmentId = personCompartmentChangeObservationEvent.getCurrentCompartmentId();
		CompartmentId previousCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personCompartmentChangeObservationEvent.getClass());
		builder.addKey(previousCompartmentId);
		builder.addKey(currentCompartmentId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonCompartmentChangeObservationEventForPerson(AgentContext context, PersonCompartmentChangeObservationEvent personCompartmentChangeObservationEvent) {
		PersonId personId = personCompartmentChangeObservationEvent.getPersonId();
		CompartmentId currentCompartmentId = personCompartmentChangeObservationEvent.getCurrentCompartmentId();
		CompartmentId previousCompartmentId = personCompartmentChangeObservationEvent.getPreviousCompartmentId();
		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personCompartmentChangeObservationEvent.getClass());
		builder.addKey(previousCompartmentId);
		builder.addKey(currentCompartmentId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonRegionChangeObservationEvent(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personRegionChangeObservationEvent.getClass());
		builder.addKey(previousRegionId);
		builder.addKey(currentRegionId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonRegionChangeObservationEventByArrival(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personRegionChangeObservationEvent.getClass());
		builder.addKey(previousRegionId);
		builder.addKey(currentRegionId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

	@Override
	protected void handlePersonRegionChangeObservationEventByDeparture(AgentContext context, PersonRegionChangeObservationEvent personRegionChangeObservationEvent) {
		PersonId personId = personRegionChangeObservationEvent.getPersonId();
		RegionId previousRegionId = personRegionChangeObservationEvent.getPreviousRegionId();
		RegionId currentRegionId = personRegionChangeObservationEvent.getCurrentRegionId();

		Builder builder = MultiKey.builder();
		builder.addKey(environment.getTime());
		builder.addKey(id);
		builder.addKey(personRegionChangeObservationEvent.getClass());
		builder.addKey(previousRegionId);
		builder.addKey(currentRegionId);
		builder.addKey(personId);
		observationContainer.addObservation(builder.build());
	}

}
