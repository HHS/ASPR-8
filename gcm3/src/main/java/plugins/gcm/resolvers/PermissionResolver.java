package plugins.gcm.resolvers;

import nucleus.DataManagerContext;
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

	private void handleCompartmentPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final CompartmentPropertyValueAssignmentEvent compartmentPropertyValueAssignmentEvent) {
		final CompartmentId compartmentId = compartmentPropertyValueAssignmentEvent.getCompartmentId();
		validateCompartmentId(dataManagerContext, compartmentId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handleGlobalPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final GlobalPropertyValueAssignmentEvent globalPropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handleGroupConstructionEvent(final DataManagerContext dataManagerContext, final GroupConstructionEvent groupConstructionEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//

	}

	private void handleGroupCreationEvent(final DataManagerContext dataManagerContext, final GroupCreationEvent groupCreationEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//
	}

	private void handleGroupMembershipAdditionEvent(final DataManagerContext dataManagerContext, final GroupMembershipAdditionEvent groupMembershipAdditionEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(CompartmentId.class)//
							.addPermissionByType(RegionId.class)//
							.check();//

	}

	private void handleGroupMembershipRemovalEvent(final DataManagerContext dataManagerContext, final GroupMembershipRemovalEvent groupMembershipRemovalEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(CompartmentId.class)//
							.addPermissionByType(RegionId.class)//
							.check();//
	}

	private void handleGroupPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final GroupPropertyValueAssignmentEvent groupPropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//
	}

	private void handleGroupRemovalEvent(final DataManagerContext dataManagerContext, final GroupRemovalRequestEvent groupRemovalRequestEvent) {

		PermissionChecker	.newChecker(dataManagerContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(RegionId.class)//
							.addPermissionByType(CompartmentId.class)//
							.check();//

	}

	private void handleInterRegionalResourceTransferEvent(final DataManagerContext dataManagerContext, final InterRegionalResourceTransferEvent interRegionalResourceTransferEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//

	}

	private void handleMaterialsProducerPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext,
			final MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent) {
		final MaterialsProducerId materialsProducerId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerId();

		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(materialsProducerId)//
							.check();//

	}

	private void handleOfferedStageTransferToMaterialsProducerEvent(final DataManagerContext dataManagerContext,
			final OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionByType(MaterialsProducerId.class)//
							.check();//
	}

	private void handlePersonCompartmentAssignmentEvent(final DataManagerContext dataManagerContext, final PersonCompartmentAssignmentEvent personCompartmentAssignmentEvent) {

		final PersonId personId = personCompartmentAssignmentEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final CompartmentId oldCompartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(dataManagerContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(oldCompartmentId)//
							.check();//
	}

	private void handlePersonCreationEvent(final DataManagerContext dataManagerContext, final PersonCreationEvent personCreationEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handleBulkPersonCreationEvent(final DataManagerContext dataManagerContext, final BulkPersonCreationEvent bulkPersonCreationEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//
	}

	private void handlePersonPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final PersonPropertyValueAssignmentEvent personPropertyValueAssignmentEvent) {

		final PersonId personId = personPropertyValueAssignmentEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	private void handlePersonRegionAssignmentEvent(final DataManagerContext dataManagerContext, final PersonRegionAssignmentEvent personRegionAssignmentEvent) {
		final PersonId personId = personRegionAssignmentEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handlePersonRemovalRequestEvent(final DataManagerContext dataManagerContext, final PersonRemovalRequestEvent personRemovalRequestEvent) {
		final PersonId personId = personRemovalRequestEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handlePersonResourceRemovalEvent(final DataManagerContext dataManagerContext, final PersonResourceRemovalEvent personResourceRemovalEvent) {
		final PersonId personId = personResourceRemovalEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(dataManagerContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//
	}

	private void handleProducedResourceTransferToRegionEvent(final DataManagerContext dataManagerContext, final ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent) {

		final MaterialsProducerId materialsProducerId = producedResourceTransferToRegionEvent.getMaterialsProducerId();
		final RegionId regionId = producedResourceTransferToRegionEvent.getRegionId();

		validateRegionId(dataManagerContext, regionId);
		validateMaterialsProducerId(dataManagerContext, materialsProducerId);

		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(materialsProducerId)//
							.check();//

	}

	private void handleRegionPropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final RegionPropertyValueAssignmentEvent regionPropertyValueAssignmentEvent) {
		final RegionId regionId = regionPropertyValueAssignmentEvent.getRegionId();
		validateRegionId(dataManagerContext, regionId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handleRegionResourceAdditionEvent(final DataManagerContext dataManagerContext, final RegionResourceAdditionEvent regionResourceAdditionEvent) {
		final RegionId regionId = regionResourceAdditionEvent.getRegionId();
		validateRegionId(dataManagerContext, regionId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.permitResolverGeneratedEvents()//
							.check();//
	}

	private void handleRegionResourceRemovalEvent(final DataManagerContext dataManagerContext, final RegionResourceRemovalEvent regionResourceRemovalEvent) {
		final RegionId regionId = regionResourceRemovalEvent.getRegionId();
		validateRegionId(dataManagerContext, regionId);

		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.check();//
	}

	private void handleResourcePropertyValueAssignmentEvent(final DataManagerContext dataManagerContext, final ResourcePropertyValueAssignmentEvent resourcePropertyValueAssignmentEvent) {
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.check();//

	}

	private void handleResourceTransferFromPersonEvent(final DataManagerContext dataManagerContext, final ResourceTransferFromPersonEvent resourceTransferFromPersonEvent) {

		final PersonId personId = resourceTransferFromPersonEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);

		PermissionChecker	.newChecker(dataManagerContext, componentDataView).addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	private void handleResourceTransferToPersonEvent(final DataManagerContext dataManagerContext, final ResourceTransferToPersonEvent resourceTransferToPersonEvent) {
		final PersonId personId = resourceTransferToPersonEvent.getPersonId();
		validatePersonExists(dataManagerContext, personId);
		final RegionId regionId = regionLocationDataView.getPersonRegion(personId);
		final CompartmentId compartmentId = compartmentLocationDataView.getPersonCompartment(personId);
		PermissionChecker	.newChecker(dataManagerContext, componentDataView)//
							.addPermissionByType(GlobalComponentId.class)//
							.addPermissionById(regionId)//
							.addPermissionById(compartmentId)//
							.check();//

	}

	public void init(final DataManagerContext dataManagerContext) {

		compartmentDataView = dataManagerContext.getDataView(CompartmentDataView.class).get();
		compartmentLocationDataView = dataManagerContext.getDataView(CompartmentLocationDataView.class).get();
		personDataView = dataManagerContext.getDataView(PersonDataView.class).get();
		regionDataView = dataManagerContext.getDataView(RegionDataView.class).get();
		materialsDataView = dataManagerContext.getDataView(MaterialsDataView.class).get();
		regionLocationDataView = dataManagerContext.getDataView(RegionLocationDataView.class).get();
		componentDataView = dataManagerContext.getDataView(ComponentDataView.class).get();

		dataManagerContext.subscribeToEventValidationPhase(CompartmentPropertyValueAssignmentEvent.class, this::handleCompartmentPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonCompartmentAssignmentEvent.class, this::handlePersonCompartmentAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonRemovalRequestEvent.class, this::handlePersonRemovalRequestEvent);
		dataManagerContext.subscribeToEventValidationPhase(GlobalPropertyValueAssignmentEvent.class, this::handleGlobalPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupConstructionEvent.class, this::handleGroupConstructionEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupCreationEvent.class, this::handleGroupCreationEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupRemovalRequestEvent.class, this::handleGroupRemovalEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupMembershipAdditionEvent.class, this::handleGroupMembershipAdditionEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupMembershipRemovalEvent.class, this::handleGroupMembershipRemovalEvent);
		dataManagerContext.subscribeToEventValidationPhase(GroupPropertyValueAssignmentEvent.class, this::handleGroupPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(MaterialsProducerPropertyValueAssignmentEvent.class, this::handleMaterialsProducerPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(ProducedResourceTransferToRegionEvent.class, this::handleProducedResourceTransferToRegionEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonCreationEvent.class, this::handlePersonCreationEvent);
		dataManagerContext.subscribeToEventValidationPhase(BulkPersonCreationEvent.class, this::handleBulkPersonCreationEvent);
		dataManagerContext.subscribeToEventValidationPhase(OfferedStageTransferToMaterialsProducerEvent.class, this::handleOfferedStageTransferToMaterialsProducerEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonPropertyValueAssignmentEvent.class, this::handlePersonPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonRegionAssignmentEvent.class, this::handlePersonRegionAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(RegionPropertyValueAssignmentEvent.class, this::handleRegionPropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(InterRegionalResourceTransferEvent.class, this::handleInterRegionalResourceTransferEvent);
		dataManagerContext.subscribeToEventValidationPhase(PersonResourceRemovalEvent.class, this::handlePersonResourceRemovalEvent);
		dataManagerContext.subscribeToEventValidationPhase(RegionResourceAdditionEvent.class, this::handleRegionResourceAdditionEvent);
		dataManagerContext.subscribeToEventValidationPhase(RegionResourceRemovalEvent.class, this::handleRegionResourceRemovalEvent);
		dataManagerContext.subscribeToEventValidationPhase(ResourcePropertyValueAssignmentEvent.class, this::handleResourcePropertyValueAssignmentEvent);
		dataManagerContext.subscribeToEventValidationPhase(ResourceTransferFromPersonEvent.class, this::handleResourceTransferFromPersonEvent);
		dataManagerContext.subscribeToEventValidationPhase(ResourceTransferToPersonEvent.class, this::handleResourceTransferToPersonEvent);

	}

	/*
	 * Validates the compartment id
	 *
	 */
	private void validateCompartmentId(final DataManagerContext dataManagerContext, final CompartmentId compartmentId) {
		if (compartmentId == null) {
			dataManagerContext.throwContractException(CompartmentError.NULL_COMPARTMENT_ID);
		}

		if (!compartmentDataView.compartmentIdExists(compartmentId)) {
			dataManagerContext.throwContractException(CompartmentError.UNKNOWN_COMPARTMENT_ID, compartmentId);
		}
	}

	private void validateMaterialsProducerId(final DataManagerContext dataManagerContext, final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			dataManagerContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsDataView.materialsProducerIdExists(materialsProducerId)) {
			dataManagerContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validatePersonExists(final DataManagerContext dataManagerContext, final PersonId personId) {
		if (personId == null) {
			dataManagerContext.throwContractException(PersonError.NULL_PERSON_ID);
		}
		if (!personDataView.personExists(personId)) {
			dataManagerContext.throwContractException(PersonError.UNKNOWN_PERSON_ID);
		}
	}

	private void validateRegionId(final DataManagerContext dataManagerContext, final RegionId regionId) {

		if (regionId == null) {
			dataManagerContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionDataView.regionIdExists(regionId)) {
			dataManagerContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

}
