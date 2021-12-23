package plugins.gcm.resolvers;

import nucleus.ResolverContext;
import plugins.compartments.datacontainers.CompartmentDataView;
import plugins.compartments.datacontainers.CompartmentLocationDataView;
import plugins.compartments.events.mutation.CompartmentPropertyValueAssignmentEvent;
import plugins.compartments.events.mutation.PersonCompartmentAssignmentEvent;
import plugins.compartments.support.CompartmentError;
import plugins.compartments.support.CompartmentId;
import plugins.components.datacontainers.ComponentDataView;
import plugins.gcm.support.PermissionChecker;
import plugins.globals.events.mutation.GlobalPropertyValueAssignmentEvent;
import plugins.globals.support.GlobalComponentId;
import plugins.groups.events.mutation.GroupConstructionEvent;
import plugins.groups.events.mutation.GroupCreationEvent;
import plugins.groups.events.mutation.GroupMembershipAdditionEvent;
import plugins.groups.events.mutation.GroupMembershipRemovalEvent;
import plugins.groups.events.mutation.GroupPropertyValueAssignmentEvent;
import plugins.groups.events.mutation.GroupRemovalRequestEvent;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.mutation.MaterialsProducerPropertyValueAssignmentEvent;
import plugins.materials.events.mutation.OfferedStageTransferToMaterialsProducerEvent;
import plugins.materials.events.mutation.ProducedResourceTransferToRegionEvent;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.people.datacontainers.PersonDataView;
import plugins.people.events.mutation.BulkPersonCreationEvent;
import plugins.people.events.mutation.PersonCreationEvent;
import plugins.people.events.mutation.PersonRemovalRequestEvent;
import plugins.people.support.PersonError;
import plugins.people.support.PersonId;
import plugins.personproperties.events.mutation.PersonPropertyValueAssignmentEvent;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.datacontainers.RegionLocationDataView;
import plugins.regions.events.mutation.PersonRegionAssignmentEvent;
import plugins.regions.events.mutation.RegionPropertyValueAssignmentEvent;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.events.mutation.InterRegionalResourceTransferEvent;
import plugins.resources.events.mutation.PersonResourceRemovalEvent;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.events.mutation.RegionResourceRemovalEvent;
import plugins.resources.events.mutation.ResourcePropertyValueAssignmentEvent;
import plugins.resources.events.mutation.ResourceTransferFromPersonEvent;
import plugins.resources.events.mutation.ResourceTransferToPersonEvent;

public class PermissionResolver {

	private CompartmentDataView compartmentDataView;

	private RegionDataView regionDataView;

	private RegionLocationDataView regionLocationDataView;

	private CompartmentLocationDataView compartmentLocationDataView;

	private PersonDataView personDataView;

	private ComponentDataView componentDataView;

	private MaterialsDataView materialsDataView;

	private void handleCompartmentPropertyValueAssignmentEvent(final ResolverContext resolverContext, final CompartmentPropertyValueAssignmentEvent compartmentPropertyValueAssignmentEvent) {
		final CompartmentId compartmentId = compartmentPropertyValueAssignmentEvent.getCompartmentId();
		validateCompartmentId(resolverContext, compartmentId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handleGlobalPropertyValueAssignmentEvent(final ResolverContext resolverContext, final GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handleGroupConstructionEvent(final ResolverContext resolverContext, final GroupConstructionEvent groupConstructionEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//

	}

	private void handleGroupCreationEvent(final ResolverContext resolverContext, final GroupCreationEvent groupCreationEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//
	}

	private void handleGroupMembershipAdditionEvent(final ResolverContext resolverContext, final GroupMembershipAdditionEvent groupMembershipAdditionEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(CompartmentId.class)//
							.addPermissionByType(RegionId.class)//
							.check();//

	}

	private void handleGroupMembershipRemovalEvent(final ResolverContext resolverContext, final GroupMembershipRemovalEvent groupMembershipRemovalEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(CompartmentId.class)//
							.addPermissionByType(RegionId.class)//
							.check();//
	}

	private void handleGroupPropertyValueAssignmentEvent(final ResolverContext resolverContext, final GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//
	}

	private void handleGroupRemovalEvent(final ResolverContext resolverContext, final GroupRemovalRequestEvent groupRemovalRequestEvent) {

		PermissionChecker	.newChecker(resolverContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//

	}

	private void handleInterRegionalResourceTransferEvent(final ResolverContext resolverContext, final InterRegionalResourceTransferEvent interRegionalResourceTransferEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//

	}

	private void handleMaterialsProducerPropertyValueAssignmentEvent(final ResolverContext resolverContext,
			final MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent) {
		final MaterialsProducerId materialsProducerId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerId();

		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(materialsProducerId)//
							.check();//

	}

	private void handleOfferedStageTransferToMaterialsProducerEvent(final ResolverContext resolverContext,
			final OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(MaterialsProducerId.class)//
							.check();//
	}

	private void handlePersonCompartmentAssignmentEvent(final ResolverContext resolverContext, final PersonCompartmentAssignmentEvent personCompartmentAssignmentEvent) {

		final PersonId personId = personCompartmentAssignmentEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final CompartmentId oldCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(resolverContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(oldCompartmentId)//
							.check();//
	}

	private void handlePersonCreationEvent(final ResolverContext resolverContext, final PersonCreationEvent personCreationEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handleBulkPersonCreationEvent(final ResolverContext resolverContext, final BulkPersonCreationEvent bulkPersonCreationEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handlePersonPropertyValueAssignmentEvent(final ResolverContext resolverContext, final PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent) {

		final PersonId personId = personPropertyValueAssignmentEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	private void handlePersonRegionAssignmentEvent(final ResolverContext resolverContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handlePersonRemovalRequestEvent(final ResolverContext resolverContext, final PersonRemovalRequestEvent personRemovalRequestEvent) {
		final PersonId personId = personRemovalRequestEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handlePersonResourceRemovalEvent(final ResolverContext resolverContext, final PersonResourceRemovalEvent personResourceRemovalEvent) {
		final PersonId personId = personResourceRemovalEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(resolverContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handleProducedResourceTransferToRegionEvent(final ResolverContext resolverContext, final ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent) {

		final MaterialsProducerId materialsProducerId = producedResourceTransferToRegionEvent.getMaterialsProducerId();
		final RegionId regionId = producedResourceTransferToRegionEvent.getRegionId();

		validateRegionId(resolverContext, regionId);
		validateMaterialsProducerId(resolverContext, materialsProducerId);

		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(materialsProducerId)//
							.check();//

	}

	private void handleRegionPropertyValueAssignmentEvent(final ResolverContext resolverContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		validateRegionId(resolverContext, regionId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handleRegionResourceAdditionEvent(final ResolverContext resolverContext, final RegionResourceAdditionEvent regionResourceAdditionEvent) {
		final RegionId regionId = regionResourceAdditionEvent.getRegionId();
		validateRegionId(resolverContext, regionId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.permitResolverGeneratedEvents()//
							.check();//
	}

	private void handleRegionResourceRemovalEvent(final ResolverContext resolverContext, final RegionResourceRemovalEvent regionResourceRemovalEvent) {
		final RegionId regionId = regionResourceRemovalEvent.getRegionId();
		validateRegionId(resolverContext, regionId);

		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handleResourcePropertyValueAssignmentEvent(final ResolverContext resolverContext, final ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//

	}

	private void handleResourceTransferFromPersonEvent(final ResolverContext resolverContext, final ResourceTransferFromPersonEvent resourceTransferFromPersonEvent) {

		final PersonId personId = resourceTransferFromPersonEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(resolverContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	private void handleResourceTransferToPersonEvent(final ResolverContext resolverContext, final ResourceTransferToPersonEvent resourceTransferToPersonEvent) {
		final PersonId personId = resourceTransferToPersonEvent.getPersonId();
		validatePersonExists(resolverContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(resolverContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	public void init(final ResolverContext resolverContext) {

		compartmentDataView = resolverContext.getDataView(CompartmentDataView.class).get();
		compartmentLocationDataView = resolverContext.getDataView(CompartmentLocationDataView.class).get();
		personDataView = resolverContext.getDataView(PersonDataView.class).get();
		regionDataView = resolverContext.getDataView(RegionDataView.class).get();
		materialsDataView = resolverContext.getDataView(MaterialsDataView.class).get();
		regionLocationDataView = resolverContext.getDataView(RegionLocationDataView.class).get();
		componentDataView = resolverContext.getDataView(ComponentDataView.class).get();

		resolverContext.subscribeToEventValidationPhase(CompartmentPropertyValueAssignmentEvent.class, this::handleCompartmentPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(PersonCompartmentAssignmentEvent.class, this::handlePersonCompartmentAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(PersonRemovalRequestEvent.class, this::handlePersonRemovalRequestEvent);
		resolverContext.subscribeToEventValidationPhase(GlobalPropertyValueAssignmentEvent.class, this::handleGlobalPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(GroupConstructionEvent.class, this::handleGroupConstructionEvent);
		resolverContext.subscribeToEventValidationPhase(GroupCreationEvent.class, this::handleGroupCreationEvent);
		resolverContext.subscribeToEventValidationPhase(GroupRemovalRequestEvent.class, this::handleGroupRemovalEvent);
		resolverContext.subscribeToEventValidationPhase(GroupMembershipAdditionEvent.class, this::handleGroupMembershipAdditionEvent);
		resolverContext.subscribeToEventValidationPhase(GroupMembershipRemovalEvent.class, this::handleGroupMembershipRemovalEvent);
		resolverContext.subscribeToEventValidationPhase(GroupPropertyValueAssignmentEvent.class, this::handleGroupPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(MaterialsProducerPropertyValueAssignmentEvent.class, this::handleMaterialsProducerPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(ProducedResourceTransferToRegionEvent.class, this::handleProducedResourceTransferToRegionEvent);
		resolverContext.subscribeToEventValidationPhase(PersonCreationEvent.class, this::handlePersonCreationEvent);
		resolverContext.subscribeToEventValidationPhase(BulkPersonCreationEvent.class, this::handleBulkPersonCreationEvent);
		resolverContext.subscribeToEventValidationPhase(OfferedStageTransferToMaterialsProducerEvent.class, this::handleOfferedStageTransferToMaterialsProducerEvent);
		resolverContext.subscribeToEventValidationPhase(PersonPropertyValueAssignmentEvent.class, this::handlePersonPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(InterRegionalResourceTransferEvent.class, this::handleInterRegionalResourceTransferEvent);
		resolverContext.subscribeToEventValidationPhase(PersonResourceRemovalEvent.class, this::handlePersonResourceRemovalEvent);
		resolverContext.subscribeToEventValidationPhase(RegionResourceAdditionEvent.class, this::handleRegionResourceAdditionEvent);
		resolverContext.subscribeToEventValidationPhase(RegionResourceRemovalEvent.class, this::handleRegionResourceRemovalEvent);
		resolverContext.subscribeToEventValidationPhase(ResourcePropertyValueAssignmentEvent.class, this::handleResourcePropertyValueAssignmentEvent);
		resolverContext.subscribeToEventValidationPhase(ResourceTransferFromPersonEvent.class, this::handleResourceTransferFromPersonEvent);
		resolverContext.subscribeToEventValidationPhase(ResourceTransferToPersonEvent.class, this::handleResourceTransferToPersonEvent);

	}

	/*
	 * Validates the compartment id
	 *
	 */
	private void validateCompartmentId(final ResolverContext resolverContext, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			resolverContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!compartmentDataView.compartmentIdExists(compartmentId)) {
			resolverContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validateMaterialsProducerId(final ResolverContext resolverContext, final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsDataView.materialsProducerIdExists(materialsProducerId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validatePersonExists(final ResolverContext resolverContext, final PersonId personId) {
		if (personId == null) {
			resolverContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			resolverContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validateRegionId(final ResolverContext resolverContext, final RegionId regionId) {

		if (regionId == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataView.regionIdExists(regionId)) {
			resolverContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

}
