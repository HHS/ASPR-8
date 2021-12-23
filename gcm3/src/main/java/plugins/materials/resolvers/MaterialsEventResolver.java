package plugins.materials.resolvers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import nucleus.AgentContext;
import nucleus.ResolverContext;
import plugins.components.datacontainers.ComponentDataView;
import plugins.components.events.ComponentConstructionEvent;
import plugins.components.support.ComponentId;
import plugins.materials.MaterialsPlugin;
import plugins.materials.datacontainers.MaterialsDataManager;
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
import plugins.materials.events.observation.BatchAmountChangeObservationEvent;
import plugins.materials.events.observation.BatchCreationObservationEvent;
import plugins.materials.events.observation.BatchImminentRemovalObservationEvent;
import plugins.materials.events.observation.BatchPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerPropertyChangeObservationEvent;
import plugins.materials.events.observation.MaterialsProducerResourceChangeObservationEvent;
import plugins.materials.events.observation.StageCreationObservationEvent;
import plugins.materials.events.observation.StageImminentRemovalObservationEvent;
import plugins.materials.events.observation.StageMaterialsProducerChangeObservationEvent;
import plugins.materials.events.observation.StageMembershipAdditionObservationEvent;
import plugins.materials.events.observation.StageMembershipRemovalObservationEvent;
import plugins.materials.events.observation.StageOfferChangeObservationEvent;
import plugins.materials.initialdata.MaterialsInitialData;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.regions.datacontainers.RegionDataView;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.events.mutation.RegionResourceAdditionEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.ContractException;

/**
 * 
 * Provides event resolution for the {@linkplain MaterialsPlugin}.
 * <P>
 * Creates, publishes and maintains the {@linkplain MaterialsDataView}.
 * Initializes the data view from the {@linkplain MaterialsInitialData}
 * instance provided to the plugin.
 * </P>
 * <P>
 * Creates all materials producer agents upon initialization.
 * </P>
 * 
 * <P>
 * Initializes all event labelers defined by
 * <ul>
 * <li>{@linkplain StageOfferChangeObservationEvent}</li>
 * <li>{@linkplain StageMaterialsProducerChangeObservationEvent}</li>
 * <li>{@linkplain MaterialsProducerPropertyChangeObservationEvent}</li> 
 * <li>{@linkplain MaterialsProducerResourceChangeObservationEvent}</li>
 * <li>{@linkplain BatchAmountChangeObservationEvent}</li>
 * <li>{@linkplain BatchCreationObservationEvent}</li> 
 * <li>{@linkplain BatchImminentRemovalObservationEvent}</li>
 * <li>{@linkplain BatchPropertyChangeObservationEvent}</li> 
 * <li>{@linkplain StageMembershipRemovalObservationEvent}</li>
 * <li>{@linkplain StageMembershipAdditionObservationEvent}</li>
 * <li>{@linkplain StageCreationObservationEvent}</li>
 * <li>{@linkplain StageImminentRemovalObservationEvent}</li>
 * 
 * </ul>
 * 
 * </P>
 * 
 * 
 * <P>
 * Resolves the following events:
 * <ul>
 * <li>{@linkplain BatchConstructionEvent} <blockquote> Creates a batch from the
 * {@linkplain BatchConstructionInfo} contained in the event. Sets batch
 * properties found in the batch construction info. Updates the
 * {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain BatchCreationObservationEvent} event<BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#MATERIALS_PRODUCER_REQUIRED} if the requesting
 * agent is not a materials producer</li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_CONSTRUCTION_INFO} if the batch
 * construction info in the event is null</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the material id in the
 * batch construction info is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the material id in the
 * batch construction info is unknown</li>
 * <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if the amount in
 * the batch construction info is not finite</li>
 * <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if the amount in the
 * batch construction info is negative</li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the batch
 * construction info contains a null batch property id</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if the batch
 * construction info contains an unknown batch property id</li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_VALUE} if the batch
 * construction info contains a null batch property value</li>
 * <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the batch construction
 * info contains a batch property value that is incompatible with the
 * corresponding property def</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain BatchContentShiftEvent} <blockquote> Transfers an amount of
 * material from one batch to another batch of the same material type and
 * material producer. Updates the {@linkplain MaterialsDataView}. Generates a
 * corresponding {@linkplain BatchAmountChangeObservationEvent} for each batch.
 * {@linkplain } <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the source batch id is
 * null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the source batch id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the destination batch id is
 * null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the destination batch id
 * is unknown</li>
 * <li>{@linkplain MaterialsError#REFLEXIVE_BATCH_SHIFT} if the source and
 * destination batches are the same</li>
 * <li>{@linkplain MaterialsError#MATERIAL_TYPE_MISMATCH} if the batches do not
 * have the same material type</li>
 * <li>{@linkplain MaterialsError#BATCH_SHIFT_WITH_MULTIPLE_OWNERS} if the
 * batches have different owning materials producers</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the source batch
 * is on a stage is offered</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the destination
 * batch is on a stage is offered</li>
 * <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if the shift
 * amount is not a finite number</li>
 * <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if the shift amount
 * is negative</li>
 * <li>{@linkplain MaterialsError#INSUFFICIENT_MATERIAL_AVAILABLE} if the shift
 * amount exceeds the available material on the source batch</li>
 * <li>{@linkplain MaterialsError#MATERIAL_ARITHMETIC_EXCEPTION} if the shift
 * amount would cause an overflow on the destination batch</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer for the two batches</li>
 * </ul>
 * 
 * </blockquote></li>
 * <li>{@linkplain BatchCreationEvent} <blockquote> Creates a batch and updates
 * the {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain BatchCreationObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#MATERIALS_PRODUCER_REQUIRED} if the requesting
 * agent is not a materials producer</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the material id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the material id is unknown</li>
 * <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if the amount is not finite</li>
 * <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if the amount is negative</li>
 * </ul>
 * </blockquote></li> **************************************
 * <li>{@linkplain BatchRemovalRequestEvent} <blockquote> Removes the given
 * batch and updates the {@linkplain MaterialsDataView}. Generates a
 * corresponding {@linkplain BatchImminentRemovalObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch id is unknown
 * </li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the batch is on
 * an offered stage</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain BatchPropertyValueAssignmentEvent} <blockquote> Assigns a
 * batch's property value and updates the {@linkplain MaterialsDataView}.
 * Generates a corresponding {@linkplain BatchPropertyChangeObservationEvent}
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch id is unknown
 * </li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the batch property
 * id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if the batch
 * property id is unknown</li>
 * <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if batch property is not
 * mutable</li>
 * <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_VALUE} if the batch
 * property value is null</li>
 * <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the batch property value
 * is not compatible with the corresponding property definition</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the batch in on
 * an offered stage</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer</li>
 * 
 * </ul>
 * 
 * 
 * </blockquote></li>
 * <li>{@linkplain MaterialsProducerPropertyValueAssignmentEvent} <blockquote>
 * Assigns a property value to a materials producer. Updates the
 * {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain MaterialsProducerPropertyChangeObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if the materials
 * producer id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
 * materials producer id is unknown</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID} if the
 * materials producer property id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID} if the
 * materials producer property id is unknown</li>
 * <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if the materials producer
 * property is immutable</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE} if the
 * property value is null</li> {@linkplain PropertyError#INCOMPATIBLE_VALUE} if
 * the property value is incompatible with the corresponding property
 * definition</li>
 * 
 * 
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain MoveBatchToInventoryEvent} <blockquote> Removes a batch from
 * an non-offered stage, placing it into the materials producer's inventory.
 * Updates the {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain StageMembershipRemovalObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer</li>
 * <li>{@linkplain MaterialsError#BATCH_NOT_STAGED} if the batch is not
 * staged</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE } if the stage
 * containing the batch is offered</li>
 * 
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain MoveBatchToStageEvent} <blockquote> Removes a batch from
 * inventory, placing it on a stage. Updates the {@linkplain MaterialsDataView}.
 * Generates a corresponding
 * {@linkplain StageMembershipAdditionObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#BATCH_ALREADY_STAGED} if the batch is already
 * staged</li>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the stage is
 * offered</li>
 * <li>{@linkplain MaterialsError#BATCH_STAGED_TO_DIFFERENT_OWNER} if batch and
 * stage do not have the same owning materials producer</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning material producer</li>
 * 
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain OfferedStageTransferToMaterialsProducerEvent} <blockquote>
 * Transfers an offered stage from one materials producer to another and marks
 * the stage as not offered. Updates the {@linkplain MaterialsDataView}.
 * Generates the corresponding
 * {@linkplain StageMaterialsProducerChangeObservationEvent}
 * {@linkplain StageOfferChangeObservationEvent}
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID } if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID } if the stage id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID } if the materials
 * producer id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID } if the
 * materials producer id is unknown</li>
 * <li>{@linkplain MaterialsError#UNOFFERED_STAGE_NOT_TRANSFERABLE } if the
 * stage is not offered</li>
 * <li>{@linkplain MaterialsError#REFLEXIVE_STAGE_TRANSFER } if the source and
 * destination materials producers are the same</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain ProducedResourceTransferToRegionEvent} <blockquote> Transfers
 * a resource amount from a materials producer to a region. Updates the
 * {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain RegionResourceAdditionEvent} and
 * {@linkplain MaterialsProducerResourceChangeObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain RegionError#NULL_REGION_ID} if the region id is null</li>
 * <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if the materials
 * producer id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID} if the
 * materials producer id is unknown</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the materials
 * amount is negative</li>
 * <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE} if the
 * materials amount exceeds the resource level of the materials producer</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the materials
 * amount would cause an overflow of the regions resource level</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain StageCreationEvent} <blockquote> Creates a stage. Update the
 * {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain StageCreationObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError.MATERIALS_PRODUCER_REQUIRED} if the requesting
 * agent is not a materials producer</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain StageRemovalRequestEvent} <blockquote> Removes a non-offered
 * stage and optionally removes any associated batches. Updates the
 * {@linkplain MaterialsDataView}. Generates the corresponding
 * {@linkplain BatchImminentRemovalObservationEvent},
 * {@linkplain StageMembershipRemovalObservationEvent} and
 * {@linkplain StageImminentRemovalObservationEvent} events. <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if stage is
 * offered</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer id</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain StageOfferEvent} <blockquote> Sets the offer state of a
 * stage. Updates the {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain StageOfferChangeObservationEvent} <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID } if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID } if the stage id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP } if the requesting agent
 * is not the owning materials producer</li>
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain StageToBatchConversionEvent} <blockquote> Converts a
 * non-offered stage, including its associated batches, into a new batch of the
 * given material. The new batch is placed into inventory. Updates the
 * {@linkplain MaterialsDataView}. Generates a corresponding
 * {@linkplain BatchImminentRemovalObservationEvent},
 * {@linkplain BatchCreationObservationEvent} and
 * {@linkplain StageImminentRemovalObservationEvent} events<BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * 
 * <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the material id is
 * null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the material id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if stage id is unknown</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the stage is
 * offered</li>
 * <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if the material
 * amount is not finite</li>
 * <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if the material
 * amount is negative</li>
 * 
 * </ul>
 * </blockquote></li>
 * <li>{@linkplain StageToResourceConversionEvent} <blockquote> Converts a
 * non-offered stage, including its associated batches, into an amount of
 * resource. The resulting resource is owned by the associated material
 * producer. Updates the {@linkplain MaterialsDataView}. Generates the
 * corresponding {@linkplain BatchImminentRemovalObservationEvent},
 * {@linkplain MaterialsProducerResourceChangeObservationEvent} and
 * {@linkplain StageImminentRemovalObservationEvent} events.
 * 
 * <BR>
 * <BR>
 * Throws {@link ContractException}
 *
 * <ul>
 * <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the resource id is
 * null</li>
 * <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the resource id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id is null</li>
 * <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage id is
 * unknown</li>
 * <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if the stage is
 * offered</li>
 * <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the requesting agent
 * is not the owning materials producer</li>
 * <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if the the resource
 * amount is negative</li>
 * <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION} if the resource
 * amount would cause an overflow of the materials producer's resource
 * level</li>
 * 
 * </ul>
 * </blockquote></li>
 * <ul>
 * </p>
 * 
 * 
 * 
 * @author Shawn Hatch
 *
 */
public final class MaterialsEventResolver {
	private final MaterialsInitialData materialsInitialData;

	/**
	 * Creates this resolver from the the given {@link MaterialsInitialData}
	 * 
	 * @throws ContractException
	 * <li>{@linkplain MaterialsError#NULL_MATERIALS_INITIAL_DATA} if the material initial data is null </li>
	 */
	public MaterialsEventResolver(MaterialsInitialData materialsInitialData) {
		if(materialsInitialData == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_INITIAL_DATA);
		}
		this.materialsInitialData = materialsInitialData;
	}

	private MaterialsDataManager materialsDataManager;

	private ResourceDataView resourceDataView;

	private ComponentDataView componentDataView;

	private final Map<MaterialId, Set<BatchPropertyId>> materialIds = new LinkedHashMap<>();

	private Set<MaterialsProducerId> materialsProducerIds;
	private Set<MaterialsProducerPropertyId> materialsProducerPropertyIds;
	private Set<RegionId> regionIds;
	private Set<ResourceId> resourceIds;
	private final Map<MaterialId, Set<BatchPropertyId>> materialToBatchPropertyIdsMap = new LinkedHashMap<>();

	private void handleBatchConstructionEventExecution(final ResolverContext resolverContext, BatchConstructionEvent batchConstructionEvent) {
		BatchConstructionInfo batchConstructionInfo = batchConstructionEvent.getBatchConstructionInfo();
		final MaterialId materialId = batchConstructionInfo.getMaterialId();
		final MaterialsProducerId materialsProducerId = componentDataView.getFocalComponentId();
		final Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
		final double amount = batchConstructionInfo.getAmount();
		BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);
		resolverContext.queueEventForResolution(new BatchCreationObservationEvent(batchId));
		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
		}

	}

	private void handleBatchConstructionEventValidation(final ResolverContext resolverContext, BatchConstructionEvent batchConstructionEvent) {
		BatchConstructionInfo batchConstructionInfo = batchConstructionEvent.getBatchConstructionInfo();
		validateCurrentAgentIsAMaterialsProducer(resolverContext);
		validateBatchConstructionInfoNotNull(resolverContext, batchConstructionInfo);
		final MaterialId materialId = batchConstructionInfo.getMaterialId();
		validateMaterialId(resolverContext, materialId);
		validateNonnegativeFiniteMaterialAmount(resolverContext, batchConstructionInfo.getAmount());

		final Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			validateBatchPropertyId(resolverContext, materialId, batchPropertyId);
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			final PropertyDefinition propertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
			validateBatchPropertyValueNotNull(resolverContext, batchPropertyValue);
			validateValueCompatibility(resolverContext, batchPropertyId, propertyDefinition, batchPropertyValue);
		}
	}

	private void handleBatchCreationEventValidation(final ResolverContext resolverContext, final BatchCreationEvent batchCreationEvent) {
		double amount = batchCreationEvent.getAmount();
		MaterialId materialId = batchCreationEvent.getMaterialId();
		validateCurrentAgentIsAMaterialsProducer(resolverContext);
		validateMaterialId(resolverContext, materialId);
		validateNonnegativeFiniteMaterialAmount(resolverContext, amount);
	}

	private void handleBatchCreationEventExecution(final ResolverContext resolverContext, final BatchCreationEvent batchCreationEvent) {
		double amount = batchCreationEvent.getAmount();
		MaterialId materialId = batchCreationEvent.getMaterialId();
		final MaterialsProducerId materialsProducerId = componentDataView.getFocalComponentId();
		BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);
		resolverContext.queueEventForResolution(new BatchCreationObservationEvent(batchId));
	}

	private void handleStageCreationEventValidation(final ResolverContext resolverContext, final StageCreationEvent stageCreationEvent) {
		validateCurrentAgentIsAMaterialsProducer(resolverContext);
	}

	private void handleStageCreationEventExecution(final ResolverContext resolverContext, final StageCreationEvent stageCreationEvent) {
		final MaterialsProducerId materialsProducerId = componentDataView.getFocalComponentId();
		StageId stageId;
		stageId = materialsDataManager.createStage(materialsProducerId);
		resolverContext.queueEventForResolution(new StageCreationObservationEvent(stageId));
	}

	public void init(ResolverContext resolverContext) {

		resolverContext.subscribeToEventExecutionPhase(BatchConstructionEvent.class, this::handleBatchConstructionEventExecution);
		resolverContext.subscribeToEventValidationPhase(BatchConstructionEvent.class, this::handleBatchConstructionEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BatchContentShiftEvent.class, this::handleBatchContentShiftEventExecution);
		resolverContext.subscribeToEventValidationPhase(BatchContentShiftEvent.class, this::handleBatchContentShiftEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BatchCreationEvent.class, this::handleBatchCreationEventExecution);
		resolverContext.subscribeToEventValidationPhase(BatchCreationEvent.class, this::handleBatchCreationEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BatchRemovalRequestEvent.class, this::handleBatchRemovalRequestEventExecution);
		resolverContext.subscribeToEventValidationPhase(BatchRemovalRequestEvent.class, this::handleBatchRemovalRequestEventValidation);

		resolverContext.subscribeToEventExecutionPhase(BatchPropertyValueAssignmentEvent.class, this::handleBatchPropertyValueAssignmentEventExecution);
		resolverContext.subscribeToEventValidationPhase(BatchPropertyValueAssignmentEvent.class, this::handleBatchPropertyValueAssignmentEventValidation);

		resolverContext.subscribeToEventExecutionPhase(MaterialsProducerPropertyValueAssignmentEvent.class, this::handleMaterialsProducerPropertyValueAssignmentEventExecution);
		resolverContext.subscribeToEventValidationPhase(MaterialsProducerPropertyValueAssignmentEvent.class, this::handleMaterialsProducerPropertyValueAssignmentEventValidation);

		resolverContext.subscribeToEventExecutionPhase(MoveBatchToInventoryEvent.class, this::handleMoveBatchToInventoryEventExecution);
		resolverContext.subscribeToEventValidationPhase(MoveBatchToInventoryEvent.class, this::handleMoveBatchToInventoryEventValidation);

		resolverContext.subscribeToEventExecutionPhase(MoveBatchToStageEvent.class, this::handleMoveBatchToStageEventExecution);
		resolverContext.subscribeToEventValidationPhase(MoveBatchToStageEvent.class, this::handleMoveBatchToStageEventValidation);

		resolverContext.subscribeToEventExecutionPhase(OfferedStageTransferToMaterialsProducerEvent.class, this::handleOfferedStageTransferToMaterialsProducerEventExecution);
		resolverContext.subscribeToEventValidationPhase(OfferedStageTransferToMaterialsProducerEvent.class, this::handleOfferedStageTransferToMaterialsProducerEventValidation);

		resolverContext.subscribeToEventExecutionPhase(ProducedResourceTransferToRegionEvent.class, this::handleProducedResourceTransferToRegionEventExecution);
		resolverContext.subscribeToEventValidationPhase(ProducedResourceTransferToRegionEvent.class, this::handleProducedResourceTransferToRegionEventValidation);

		resolverContext.subscribeToEventValidationPhase(StageCreationEvent.class, this::handleStageCreationEventValidation);
		resolverContext.subscribeToEventExecutionPhase(StageCreationEvent.class, this::handleStageCreationEventExecution);

		resolverContext.subscribeToEventValidationPhase(StageRemovalRequestEvent.class, this::handleStageRemovalRequestEventValidation);
		resolverContext.subscribeToEventExecutionPhase(StageRemovalRequestEvent.class, this::handleStageRemovalRequestEventExecution);

		resolverContext.subscribeToEventValidationPhase(StageOfferEvent.class, this::handleStageOfferEventValidation);
		resolverContext.subscribeToEventExecutionPhase(StageOfferEvent.class, this::handleStageOfferEventExecution);

		resolverContext.subscribeToEventExecutionPhase(StageToBatchConversionEvent.class, this::handleStageToBatchConversionEventExecution);
		resolverContext.subscribeToEventValidationPhase(StageToBatchConversionEvent.class, this::handleStageToBatchConversionEventValidation);

		resolverContext.subscribeToEventValidationPhase(StageToResourceConversionEvent.class, this::handleStageToResourceConversionEventValidation);
		resolverContext.subscribeToEventExecutionPhase(StageToResourceConversionEvent.class, this::handleStageToResourceConversionEventExecution);

		/*
		 * Establish all the convenience references
		 */
		resolverContext.addEventLabeler(StageOfferChangeObservationEvent.getEventLabelerForStage());
		resolverContext.addEventLabeler(StageOfferChangeObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(StageMaterialsProducerChangeObservationEvent.getEventLabelerForAll());
		resolverContext.addEventLabeler(StageMaterialsProducerChangeObservationEvent.getEventLabelerForDestination());
		resolverContext.addEventLabeler(StageMaterialsProducerChangeObservationEvent.getEventLabelerForSource());
		resolverContext.addEventLabeler(StageMaterialsProducerChangeObservationEvent.getEventLabelerForStage());

		resolverContext.addEventLabeler(MaterialsProducerPropertyChangeObservationEvent.getEventLabelerForMaterialsProducerAndProperty());

		resolverContext.addEventLabeler(MaterialsProducerResourceChangeObservationEvent.getEventLabelerForMaterialsProducerAndResource());
		resolverContext.addEventLabeler(MaterialsProducerResourceChangeObservationEvent.getEventLabelerForResource());
		
		resolverContext.addEventLabeler(BatchCreationObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(BatchAmountChangeObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(BatchImminentRemovalObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(BatchPropertyChangeObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(StageMembershipRemovalObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(StageMembershipAdditionObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(StageCreationObservationEvent.getEventLabelerForAll());
		
		resolverContext.addEventLabeler(StageImminentRemovalObservationEvent.getEventLabelerForAll());
		
		

		resourceDataView = resolverContext.getDataView(ResourceDataView.class).get();

		materialsDataManager = new MaterialsDataManager(resolverContext.getSafeContext());
		for (final MaterialId materialId : materialsInitialData.getMaterialIds()) {
			materialsDataManager.addMaterialId(materialId);
			for (final BatchPropertyId batchPropertyId : materialsInitialData.getBatchPropertyIds(materialId)) {
				final PropertyDefinition propertyDefinition = materialsInitialData.getBatchPropertyDefinition(materialId, batchPropertyId);
				materialsDataManager.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			}
		}

		for (ResourceId resourceId : resourceDataView.getResourceIds()) {
			materialsDataManager.addResource(resourceId);
		}

		for (final MaterialsProducerId materialsProducerId : materialsInitialData.getMaterialsProducerIds()) {
			materialsDataManager.addMaterialsProducerId(materialsProducerId);
		}

		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsInitialData.getMaterialsProducerPropertyIds()) {
			PropertyDefinition propertyDefinition = materialsInitialData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			materialsDataManager.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		}

		componentDataView = resolverContext.getDataView(ComponentDataView.class).get();

		for (final MaterialId materialId : materialsInitialData.getMaterialIds()) {
			materialToBatchPropertyIdsMap.put(materialId, materialsInitialData.getBatchPropertyIds(materialId));
		}
		resourceIds = resourceDataView.getResourceIds();
		RegionDataView regionDataView = resolverContext.getDataView(RegionDataView.class).get();
		regionIds = regionDataView.getRegionIds();
		materialsProducerIds = materialsInitialData.getMaterialsProducerIds();

		for (final MaterialId materialId : materialsInitialData.getMaterialIds()) {
			materialIds.put(materialId, materialsInitialData.getBatchPropertyIds(materialId));
		}
		materialsProducerPropertyIds = materialsInitialData.getMaterialsProducerPropertyIds();

		/*
		 * Load the remaining data from the scenario that generally corresponds
		 * to mutations available to components so that reporting will properly
		 * reflect these data. The adding of resources directly to people and
		 * material producers is covered here but do not correspond to mutations
		 * allowed to components.
		 */

		loadMaterialsProducerPropertyValues(resolverContext, materialsInitialData);
		final Map<StageId, StageId> scenarioToSimStageMap = loadStages(resolverContext, materialsInitialData);
		final Map<BatchId, BatchId> scenarioToSimBatchMap = loadBatches(resolverContext, materialsInitialData, scenarioToSimStageMap);
		loadBatchProperties(resolverContext, materialsInitialData, scenarioToSimBatchMap);
		loadStageOfferings(resolverContext, materialsInitialData, scenarioToSimStageMap);
		loadMaterialsProducerResources(resolverContext, materialsInitialData);

		for (MaterialsProducerId materialsProducerId : materialsProducerIds) {
			Consumer<AgentContext> consumer = materialsInitialData.getMaterialsProducerInitialBehavior(materialsProducerId);
			resolverContext.queueEventForResolution(new ComponentConstructionEvent(materialsProducerId, consumer));
		}

		resolverContext.publishDataView(new MaterialsDataView(resolverContext.getSafeContext(), materialsDataManager));

	}

	private Map<BatchId, BatchId> loadBatches(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData, final Map<StageId, StageId> scenarioToSimStageMap) {
		final Map<BatchId, BatchId> result = new LinkedHashMap<>();
		final List<BatchId> scenarioBatchIds = new ArrayList<>(materialsInitialData.getBatchIds());
		Collections.sort(scenarioBatchIds);
		for (final BatchId scenarioBatchId : scenarioBatchIds) {
			final MaterialsProducerId materialsProducerId = materialsInitialData.getBatchMaterialsProducer(scenarioBatchId);
			final MaterialId materialId = materialsInitialData.getBatchMaterial(scenarioBatchId);
			final double amount = materialsInitialData.getBatchAmount(scenarioBatchId);
			final BatchId simulationBatchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);
			result.put(scenarioBatchId, simulationBatchId);
		}
		for (final StageId scenarioStageId : materialsInitialData.getStageIds()) {
			final Set<BatchId> scenarioBatches = materialsInitialData.getStageBatches(scenarioStageId);
			final StageId simulationStageId = scenarioToSimStageMap.get(scenarioStageId);
			for (final BatchId scenarioBatchId : scenarioBatches) {
				final BatchId simulationBatchId = result.get(scenarioBatchId);
				validateBatchAndStageOwnersMatch(resolverContext, simulationBatchId, simulationStageId);
				materialsDataManager.moveBatchToStage(simulationBatchId, simulationStageId);
			}
		}

		return result;
	}

	private void loadBatchProperties(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData, final Map<BatchId, BatchId> scenarioToSimBatchMap) {
		final Set<BatchId> scenarioBatchIds = materialsInitialData.getBatchIds();
		for (final BatchId scenarioBatchId : scenarioBatchIds) {
			final MaterialId materialId = materialsInitialData.getBatchMaterial(scenarioBatchId);
			final Set<BatchPropertyId> batchPropertyIds = materialsInitialData.getBatchPropertyIds(materialId);
			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				final Object batchPropertyValue = materialsInitialData.getBatchPropertyValue(scenarioBatchId, batchPropertyId);
				final BatchId simulationBatchId = scenarioToSimBatchMap.get(scenarioBatchId);
				final PropertyDefinition propertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
				validateBatchPropertyValueNotNull(resolverContext, batchPropertyValue);
				validateValueCompatibility(resolverContext, batchPropertyId, propertyDefinition, batchPropertyValue);
				validateBatchIsNotOnOfferedStage(resolverContext, simulationBatchId);
				materialsDataManager.setBatchPropertyValue(simulationBatchId, batchPropertyId, batchPropertyValue);
			}
		}
	}

	private void loadMaterialsProducerPropertyValues(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData) {
		for (final MaterialsProducerId materialsProducerId : materialsInitialData.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsInitialData.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsInitialData.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
			}
		}
	}

	private void loadMaterialsProducerResources(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData) {
		for (ResourceId resourceId : materialsInitialData.getResourceIds()) {
			if (!resourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " in resource levels of materials producers");
			}
		}
		for (final MaterialsProducerId materialsProducerId : materialsInitialData.getMaterialsProducerIds()) {
			for (final ResourceId resourceId : resourceIds) {
				final Long amount = materialsInitialData.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				if (amount != null) {
					validateNonnegativeResourceAmount(resolverContext, amount);
					materialsDataManager.incrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
				}
			}
		}
	}

	private void loadStageOfferings(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData, final Map<StageId, StageId> scenarioToSimStageMap) {

		/*
		 * The stage offer state cannot be set until all of the batches have
		 * been associated with their stages.
		 */
		for (final StageId scenarioStageId : materialsInitialData.getStageIds()) {
			final StageId simulationStageId = scenarioToSimStageMap.get(scenarioStageId);
			final boolean stageIsOffered = materialsInitialData.isStageOffered(scenarioStageId);
			materialsDataManager.setStageOffer(simulationStageId, stageIsOffered);
		}

	}

	private Map<StageId, StageId> loadStages(final ResolverContext resolverContext, final MaterialsInitialData materialsInitialData) {
		final Map<StageId, StageId> result = new LinkedHashMap<>();

		final List<StageId> scenarioStageIds = new ArrayList<>(materialsInitialData.getStageIds());
		Collections.sort(scenarioStageIds);
		for (final StageId scenarioStageId : scenarioStageIds) {
			final MaterialsProducerId materialsProducerId = materialsInitialData.getStageMaterialsProducer(scenarioStageId);
			final StageId simulationStageId = materialsDataManager.createStage(materialsProducerId);
			result.put(scenarioStageId, simulationStageId);
		}
		return result;
	}

	private void handleMoveBatchToInventoryEventValidation(final ResolverContext resolverContext, final MoveBatchToInventoryEvent moveBatchToInventoryEvent) {
		BatchId batchId = moveBatchToInventoryEvent.getBatchId();
		validateBatchId(resolverContext, batchId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getBatchProducer(batchId);
		validateCurrentAgent(resolverContext, materialsProducerId);
		validateBatchIsStaged(resolverContext, batchId);
		final StageId stageId = materialsDataManager.getBatchStageId(batchId).get();
		validateStageIsNotOffered(resolverContext, stageId);
	}

	private void handleMoveBatchToInventoryEventExecution(final ResolverContext resolverContext, final MoveBatchToInventoryEvent moveBatchToInventoryEvent) {
		BatchId batchId = moveBatchToInventoryEvent.getBatchId();
		final StageId stageId = materialsDataManager.getBatchStageId(batchId).get();
		materialsDataManager.moveBatchToInventory(batchId);
		resolverContext.queueEventForResolution(new StageMembershipRemovalObservationEvent(batchId, stageId));
	}

	private void handleMoveBatchToStageEventValidation(final ResolverContext resolverContext, final MoveBatchToStageEvent moveBatchToStageEvent) {
		BatchId batchId = moveBatchToStageEvent.getBatchId();
		StageId stageId = moveBatchToStageEvent.getStageId();

		validateBatchId(resolverContext, batchId);
		validateBatchIsNotStaged(resolverContext, batchId);
		validateStageId(resolverContext, stageId);
		validateStageIsNotOffered(resolverContext, stageId);
		validateBatchAndStageOwnersMatch(resolverContext, batchId, stageId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getBatchProducer(batchId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleMoveBatchToStageEventExecution(final ResolverContext resolverContext, final MoveBatchToStageEvent moveBatchToStageEvent) {
		BatchId batchId = moveBatchToStageEvent.getBatchId();
		StageId stageId = moveBatchToStageEvent.getStageId();

		materialsDataManager.moveBatchToStage(batchId, stageId);
		resolverContext.queueEventForResolution(new StageMembershipAdditionObservationEvent(batchId, stageId));
	}

	private void handleBatchPropertyValueAssignmentEventValidation(final ResolverContext resolverContext, final BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent) {
		BatchId batchId = batchPropertyValueAssignmentEvent.getBatchId();
		BatchPropertyId batchPropertyId = batchPropertyValueAssignmentEvent.getBatchPropertyId();
		Object batchPropertyValue = batchPropertyValueAssignmentEvent.getBatchPropertyValue();

		validateBatchId(resolverContext, batchId);
		final MaterialId materialId = materialsDataManager.getBatchMaterial(batchId);		
		validateBatchPropertyId(resolverContext, materialId, batchPropertyId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getBatchProducer(batchId);
		final PropertyDefinition propertyDefinition = materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
		validatePropertyMutability(resolverContext, propertyDefinition);
		validateBatchPropertyValueNotNull(resolverContext, batchPropertyValue);
		validateValueCompatibility(resolverContext, batchPropertyId, propertyDefinition, batchPropertyValue);
		validateBatchIsNotOnOfferedStage(resolverContext, batchId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleBatchPropertyValueAssignmentEventExecution(final ResolverContext resolverContext, final BatchPropertyValueAssignmentEvent batchPropertyValueAssignmentEvent) {
		BatchId batchId = batchPropertyValueAssignmentEvent.getBatchId();
		BatchPropertyId batchPropertyId = batchPropertyValueAssignmentEvent.getBatchPropertyId();
		Object batchPropertyValue = batchPropertyValueAssignmentEvent.getBatchPropertyValue();

		Object previousPropertyValue = materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
		materialsDataManager.setBatchPropertyValue(batchId, batchPropertyId, batchPropertyValue);
		resolverContext.queueEventForResolution(new BatchPropertyChangeObservationEvent(batchId, batchPropertyId, previousPropertyValue, batchPropertyValue));
	}

	private void handleMaterialsProducerPropertyValueAssignmentEventValidation(final ResolverContext resolverContext,
			final MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyId();
		Object materialsProducerPropertyValue = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyValue();

		validateMaterialsProducerId(resolverContext, materialsProducerId);
		validateMaterialsProducerPropertyId(resolverContext, materialsProducerPropertyId);
		final PropertyDefinition propertyDefinition = materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
		validatePropertyMutability(resolverContext, propertyDefinition);
		validateMaterialProducerPropertyValueNotNull(resolverContext, materialsProducerPropertyValue);
		validateValueCompatibility(resolverContext, materialsProducerPropertyId, propertyDefinition, materialsProducerPropertyValue);
	}

	private void handleMaterialsProducerPropertyValueAssignmentEventExecution(final ResolverContext resolverContext,
			final MaterialsProducerPropertyValueAssignmentEvent materialsProducerPropertyValueAssignmentEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyId();
		Object materialsProducerPropertyValue = materialsProducerPropertyValueAssignmentEvent.getMaterialsProducerPropertyValue();

		Object oldPropertyValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
		materialsDataManager.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
		resolverContext.queueEventForResolution(
				new MaterialsProducerPropertyChangeObservationEvent(materialsProducerId, materialsProducerPropertyId, oldPropertyValue, materialsProducerPropertyValue));
	}

	private void handleStageOfferEventValidation(final ResolverContext resolverContext, final StageOfferEvent stageOfferEvent) {
		StageId stageId = stageOfferEvent.getStageId();

		validateStageId(resolverContext, stageId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleStageOfferEventExecution(final ResolverContext resolverContext, final StageOfferEvent stageOfferEvent) {
		StageId stageId = stageOfferEvent.getStageId();
		boolean offer = stageOfferEvent.isOffer();

		materialsDataManager.setStageOffer(stageId, offer);
		resolverContext.queueEventForResolution(new StageOfferChangeObservationEvent(stageId, !offer, offer));
	}

	private void validateCurrentAgent(final ResolverContext resolverContext, MaterialsProducerId materialsProducerId) {
		ComponentId focalComponentId = componentDataView.getFocalComponentId();
		if (!materialsProducerId.equals(focalComponentId)) {
			resolverContext.throwContractException(MaterialsError.MATERIALS_OWNERSHIP);
		}
	}

	private void validateCurrentAgentIsAMaterialsProducer(final ResolverContext resolverContext) {
		ComponentId focalComponentId = componentDataView.getFocalComponentId();
		if (focalComponentId == null || !(focalComponentId instanceof MaterialsProducerId)) {
			resolverContext.throwContractException(MaterialsError.MATERIALS_PRODUCER_REQUIRED);
		}
	}

	private void validateBatchHasSufficientUllage(ResolverContext resolverContext, BatchId batchId, double amount) {		
		if (!Double.isFinite(materialsDataManager.getBatchAmount(batchId) + amount)) {
			resolverContext.throwContractException(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION);
		}
	}

	private void handleBatchContentShiftEventValidation(final ResolverContext resolverContext, final BatchContentShiftEvent batchContentShiftEvent) {
		double amount = batchContentShiftEvent.getAmount();
		BatchId destinationBatchId = batchContentShiftEvent.getDestinationBatchId();
		BatchId sourceBatchId = batchContentShiftEvent.getSourceBatchId();

		validateBatchId(resolverContext, sourceBatchId);
		validateBatchId(resolverContext, destinationBatchId);
		validateDifferentBatchesForShift(resolverContext, sourceBatchId, destinationBatchId);
		validateMaterialsMatchForShift(resolverContext, sourceBatchId, destinationBatchId);
		validateProducersMatchForShift(resolverContext, sourceBatchId, destinationBatchId);
		validateBatchIsNotOnOfferedStage(resolverContext, sourceBatchId);
		validateBatchIsNotOnOfferedStage(resolverContext, destinationBatchId);
		validateNonnegativeFiniteMaterialAmount(resolverContext, amount);
		validateBatchHasSufficientMaterial(resolverContext, sourceBatchId, amount);
		validateBatchHasSufficientUllage(resolverContext, destinationBatchId, amount);

		final MaterialsProducerId materialsProducerId = materialsDataManager.getBatchProducer(sourceBatchId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleBatchContentShiftEventExecution(final ResolverContext resolverContext, final BatchContentShiftEvent batchContentShiftEvent) {
		double amount = batchContentShiftEvent.getAmount();
		BatchId destinationBatchId = batchContentShiftEvent.getDestinationBatchId();
		BatchId sourceBatchId = batchContentShiftEvent.getSourceBatchId();

		double previousSourceAmount = materialsDataManager.getBatchAmount(sourceBatchId);
		double previousDestinationAmount = materialsDataManager.getBatchAmount(destinationBatchId);
		materialsDataManager.shiftBatchContent(sourceBatchId, destinationBatchId, amount);
		double currentSourceAmount = materialsDataManager.getBatchAmount(sourceBatchId);
		double currentDestinationAmount = materialsDataManager.getBatchAmount(destinationBatchId);
		resolverContext.queueEventForResolution(new BatchAmountChangeObservationEvent(sourceBatchId, previousSourceAmount, currentSourceAmount));
		resolverContext.queueEventForResolution(new BatchAmountChangeObservationEvent(destinationBatchId, previousDestinationAmount, currentDestinationAmount));
	}

	private void handleOfferedStageTransferToMaterialsProducerEventValidation(final ResolverContext resolverContext,
			final OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent) {
		MaterialsProducerId materialsProducerId = offeredStageTransferToMaterialsProducerEvent.getMaterialsProducerId();
		StageId stageId = offeredStageTransferToMaterialsProducerEvent.getStageId();

		validateStageId(resolverContext, stageId);
		validateMaterialsProducerId(resolverContext, materialsProducerId);
		validateStageIsOffered(resolverContext, stageId);
		validateStageNotOwnedByReceivingMaterialsProducer(resolverContext, stageId, materialsProducerId);
	}

	private void handleOfferedStageTransferToMaterialsProducerEventExecution(final ResolverContext resolverContext,
			final OfferedStageTransferToMaterialsProducerEvent offeredStageTransferToMaterialsProducerEvent) {
		MaterialsProducerId materialsProducerId = offeredStageTransferToMaterialsProducerEvent.getMaterialsProducerId();
		StageId stageId = offeredStageTransferToMaterialsProducerEvent.getStageId();

		MaterialsProducerId stageProducer = materialsDataManager.getStageProducer(stageId);
		materialsDataManager.transferOfferedStageToMaterialsProducer(materialsProducerId, stageId);
		resolverContext.queueEventForResolution(new StageMaterialsProducerChangeObservationEvent(stageId, stageProducer, materialsProducerId));
		resolverContext.queueEventForResolution(new StageOfferChangeObservationEvent(stageId, true, false));

	}

	private void handleProducedResourceTransferToRegionEventValidation(final ResolverContext resolverContext, final ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent) {
		long amount = producedResourceTransferToRegionEvent.getAmount();
		MaterialsProducerId materialsProducerId = producedResourceTransferToRegionEvent.getMaterialsProducerId();
		RegionId regionId = producedResourceTransferToRegionEvent.getRegionId();
		ResourceId resourceId = producedResourceTransferToRegionEvent.getResourceId();

		validateResourceId(resolverContext, resourceId);
		validateRegionId(resolverContext, regionId);
		validateMaterialsProducerId(resolverContext, materialsProducerId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		validateMaterialsProducerHasSufficientResource(resolverContext, materialsProducerId, resourceId, amount);
		final long currentResourceLevel = resourceDataView.getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(resolverContext, currentResourceLevel, amount);

	}

	private void handleProducedResourceTransferToRegionEventExecution(final ResolverContext resolverContext, final ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent) {
		long amount = producedResourceTransferToRegionEvent.getAmount();
		MaterialsProducerId materialsProducerId = producedResourceTransferToRegionEvent.getMaterialsProducerId();
		RegionId regionId = producedResourceTransferToRegionEvent.getRegionId();
		ResourceId resourceId = producedResourceTransferToRegionEvent.getResourceId();

		long previousMaterialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		materialsDataManager.decrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		resolverContext.queueEventForResolution(new RegionResourceAdditionEvent(resourceId, regionId, amount));
		long currentMaterialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		resolverContext.queueEventForResolution(
				new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId, previousMaterialsProducerResourceLevel, currentMaterialsProducerResourceLevel));
	}

	/*
	 * Preconditions : the batch and stage must exist
	 */
	private void validateBatchAndStageOwnersMatch(final ResolverContext resolverContext, final BatchId batchId, final StageId stageId) {
		final MaterialsProducerId batchProducerId = materialsDataManager.getBatchProducer(batchId);
		final MaterialsProducerId stageProducerId = materialsDataManager.getStageProducer(stageId);
		if (!batchProducerId.equals(stageProducerId)) {
			resolverContext.throwContractException(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER);
		}
	}

	private void validateBatchConstructionInfoNotNull(final ResolverContext resolverContext, final BatchConstructionInfo batchConstructionInfo) {
		if (batchConstructionInfo == null) {
			resolverContext.throwContractException(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO);
		}
	}

	/*
	 * Precondition : batch must exist
	 */
	private void validateBatchHasSufficientMaterial(final ResolverContext resolverContext, final BatchId batchId, final double amount) {
		if (materialsDataManager.getBatchAmount(batchId) < amount) {
			resolverContext.throwContractException(MaterialsError.INSUFFICIENT_MATERIAL_AVAILABLE);
		}
	}

	private void validateBatchId(final ResolverContext resolverContext, final BatchId batchId) {

		if (batchId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_BATCH_ID);
		}

		if (!materialsDataManager.batchExists(batchId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId);
		}
	}

	/*
	 * Preconditions : batch must exist
	 */
	private void validateBatchIsNotOnOfferedStage(final ResolverContext resolverContext, final BatchId batchId) {
		final Optional<StageId> optionalStageId = materialsDataManager.getBatchStageId(batchId);
		if (optionalStageId.isPresent()) {
			if (materialsDataManager.isStageOffered(optionalStageId.get())) {
				resolverContext.throwContractException(MaterialsError.OFFERED_STAGE_UNALTERABLE);
			}
		}
	}

	private void validateBatchIsNotStaged(final ResolverContext resolverContext, final BatchId batchId) {
		if (materialsDataManager.getBatchStageId(batchId).isPresent()) {
			resolverContext.throwContractException(MaterialsError.BATCH_ALREADY_STAGED);
		}
	}

	private void validateBatchIsStaged(final ResolverContext resolverContext, final BatchId batchId) {
		if (!materialsDataManager.getBatchStageId(batchId).isPresent()) {
			resolverContext.throwContractException(MaterialsError.BATCH_NOT_STAGED);
		}
	}

	private void validateBatchPropertyId(final ResolverContext resolverContext, final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}
		final Set<BatchPropertyId> batchPropertyIds = materialIds.get(materialId);
		if (!batchPropertyIds.contains(batchPropertyId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID);
		}

	}

	private void validateBatchPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(MaterialsError.NULL_BATCH_PROPERTY_VALUE);
		}
	}

	private void validateDifferentBatchesForShift(final ResolverContext resolverContext, final BatchId sourceBatchId, final BatchId destinationBatchId) {
		if (sourceBatchId.equals(destinationBatchId)) {
			resolverContext.throwContractException(MaterialsError.REFLEXIVE_BATCH_SHIFT);
		}

	}

	/*
	 * Validates a material id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_MATERIAL_ID} if the material id is
	 * null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_MATERIAL_ID} if the material id
	 * does not correspond to a known material
	 */
	private void validateMaterialId(final ResolverContext resolverContext, final MaterialId materialId) {
		if (materialId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (!materialIds.containsKey(materialId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private void validateMaterialProducerPropertyValueNotNull(final ResolverContext resolverContext, final Object propertyValue) {
		if (propertyValue == null) {
			resolverContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE);
		}
	}

	/*
	 * Preconditions: The batches must exist
	 */
	private void validateMaterialsMatchForShift(final ResolverContext resolverContext, final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialId sourceMaterialId = materialsDataManager.getBatchMaterial(sourceBatchId);
		final MaterialId destinationMaterialId = materialsDataManager.getBatchMaterial(destinationBatchId);

		if (!sourceMaterialId.equals(destinationMaterialId)) {
			// componentDataView.throwModelException(MaterialsError.MATERIAL_TYPE_MISMATCH);
			throw new ContractException(MaterialsError.MATERIAL_TYPE_MISMATCH);
		}

	}

	/*
	 * Preconditions : the resource and materials producer must exist
	 */
	private void validateMaterialsProducerHasSufficientResource(final ResolverContext resolverContext, final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		final long materialsProducerResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		if (materialsProducerResourceLevel < amount) {
			resolverContext.throwContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	/*
	 * Validates a materials producer id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_MATERIALS_PRODUCER_ID} if the
	 * materials Producer id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_MATERIALS_PRODUCER_ID} if the
	 * materials Producer id does not correspond to a known material producer
	 */
	private void validateMaterialsProducerId(final ResolverContext resolverContext, final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsProducerIds.contains(materialsProducerId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateMaterialsProducerPropertyId(final ResolverContext resolverContext, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}
		if (!materialsProducerPropertyIds.contains(materialsProducerPropertyId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
		}
	}

	private void validateNonnegativeResourceAmount(final ResolverContext resolverContext, final long amount) {
		if (amount < 0) {
			resolverContext.throwContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	/*
	 * Preconditions : the batches must exist
	 */
	private void validateProducersMatchForShift(final ResolverContext resolverContext, final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialsProducerId sourceMaterialsProducerId = materialsDataManager.getBatchProducer(sourceBatchId);
		final MaterialsProducerId destinationMaterialsProducerId = materialsDataManager.getBatchProducer(destinationBatchId);

		if (!sourceMaterialsProducerId.equals(destinationMaterialsProducerId)) {
			resolverContext.throwContractException(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS);
		}

	}

	private void validatePropertyMutability(final ResolverContext resolverContext, final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			resolverContext.throwContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	/*
	 * Validates the region id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_REGION_ID} if the region id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_REGION_ID} if the region id does
	 * not correspond to a known region
	 */
	private void validateRegionId(final ResolverContext resolverContext, final RegionId regionId) {

		if (regionId == null) {
			resolverContext.throwContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionIds.contains(regionId)) {
			resolverContext.throwContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateResourceAdditionValue(final ResolverContext resolverContext, final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (final ArithmeticException e) {
			resolverContext.throwContractException(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	/*
	 * Validates the resource id.
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_RESOURCE_ID} if the resource id is
	 * null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_RESOURCE_ID} if the resource id
	 * does not correspond to a known resource
	 */
	private void validateResourceId(final ResolverContext resolverContext, final ResourceId resourceId) {
		if (resourceId == null) {
			resolverContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (!resourceIds.contains(resourceId)) {
			resolverContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/*
	 * Validates a stage id
	 *
	 * @throws ModelException
	 *
	 * <li>{@link SimulationErrorType#NULL_STAGE_ID} if the stage id is null
	 *
	 * <li>{@link SimulationErrorType#UNKNOWN_STAGE_ID} if the stage id does not
	 * correspond to a known stage
	 */
	private void validateStageId(final ResolverContext resolverContext, final StageId stageId) {
		if (stageId == null) {
			resolverContext.throwContractException(MaterialsError.NULL_STAGE_ID);
		}

		if (!materialsDataManager.stageExists(stageId)) {
			resolverContext.throwContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	private void validateNonnegativeFiniteMaterialAmount(final ResolverContext resolverContext, final double amount) {
		if (!Double.isFinite(amount)) {
			resolverContext.throwContractException(MaterialsError.NON_FINITE_MATERIAL_AMOUNT);
		}
		if (amount < 0) {
			resolverContext.throwContractException(MaterialsError.NEGATIVE_MATERIAL_AMOUNT);
		}
	}

	private void validateStageIsNotOffered(final ResolverContext resolverContext, final StageId stageId) {
		if (materialsDataManager.isStageOffered(stageId)) {
			resolverContext.throwContractException(MaterialsError.OFFERED_STAGE_UNALTERABLE);
		}
	}

	private void validateStageIsOffered(final ResolverContext resolverContext, final StageId stageId) {
		if (!materialsDataManager.isStageOffered(stageId)) {
			resolverContext.throwContractException(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE);
		}
	}

	/*
	 * Preconditions : the stage and producer must exist
	 */
	private void validateStageNotOwnedByReceivingMaterialsProducer(final ResolverContext resolverContext, final StageId stageId, final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerId stageProducer = materialsDataManager.getStageProducer(stageId);
		if (materialsProducerId.equals(stageProducer)) {
			resolverContext.throwContractException(MaterialsError.REFLEXIVE_STAGE_TRANSFER);
		}
	}

	private void validateValueCompatibility(final ResolverContext resolverContext, final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			resolverContext.throwContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void handleBatchRemovalRequestEventExecution(final ResolverContext resolverContext, final BatchRemovalRequestEvent batchRemovalRequestEvent) {
		BatchId batchId = batchRemovalRequestEvent.getBatchId();
		resolverContext.queueEventForResolution(new BatchImminentRemovalObservationEvent(batchId));
		resolverContext.addPlan((context) -> materialsDataManager.destroyBatch(batchId), resolverContext.getTime());
	}

	private void handleBatchRemovalRequestEventValidation(final ResolverContext resolverContext, final BatchRemovalRequestEvent batchRemovalRequestEvent) {
		BatchId batchId = batchRemovalRequestEvent.getBatchId();
		validateBatchId(resolverContext, batchId);
		validateBatchIsNotOnOfferedStage(resolverContext, batchId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getBatchProducer(batchId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleStageRemovalRequestEventValidation(final ResolverContext resolverContext, final StageRemovalRequestEvent stageRemovalRequestEvent) {
		StageId stageId = stageRemovalRequestEvent.getStageId();

		validateStageId(resolverContext, stageId);
		validateStageIsNotOffered(resolverContext, stageId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		validateCurrentAgent(resolverContext, materialsProducerId);
	}

	private void handleStageRemovalRequestEventExecution(final ResolverContext resolverContext, final StageRemovalRequestEvent stageRemovalRequestEvent) {
		StageId stageId = stageRemovalRequestEvent.getStageId();
		boolean destroyBatches = stageRemovalRequestEvent.isDestroyBatches();

		List<BatchId> stageBatches = materialsDataManager.getStageBatches(stageId);
		if (destroyBatches) {
			for (BatchId batchId : stageBatches) {
				resolverContext.queueEventForResolution(new BatchImminentRemovalObservationEvent(batchId));
				resolverContext.addPlan((context) -> materialsDataManager.destroyBatch(batchId), resolverContext.getTime());
			}
		} else {
			for (BatchId batchId : stageBatches) {
				materialsDataManager.moveBatchToInventory(batchId);
				resolverContext.queueEventForResolution(new StageMembershipRemovalObservationEvent(batchId, stageId));
			}
		}
		resolverContext.queueEventForResolution(new StageImminentRemovalObservationEvent(stageId));
		resolverContext.addPlan((context) -> materialsDataManager.destroyStage(stageId), resolverContext.getTime());
	}

	private void handleStageToBatchConversionEventValidation(final ResolverContext resolverContext, final StageToBatchConversionEvent stageToBatchConversionEvent) {
		double amount = stageToBatchConversionEvent.getAmount();
		MaterialId materialId = stageToBatchConversionEvent.getMaterialId();
		StageId stageId = stageToBatchConversionEvent.getStageId();

		validateMaterialId(resolverContext, materialId);
		validateStageId(resolverContext, stageId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		validateCurrentAgent(resolverContext, materialsProducerId);
		validateStageIsNotOffered(resolverContext, stageId);
		validateNonnegativeFiniteMaterialAmount(resolverContext, amount);
	}

	private BatchId handleStageToBatchConversionEventExecution(final ResolverContext resolverContext, final StageToBatchConversionEvent stageToBatchConversionEvent) {
		double amount = stageToBatchConversionEvent.getAmount();
		MaterialId materialId = stageToBatchConversionEvent.getMaterialId();
		StageId stageId = stageToBatchConversionEvent.getStageId();

		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		for (BatchId batchId : materialsDataManager.getStageBatches(stageId)) {
			resolverContext.queueEventForResolution(new BatchImminentRemovalObservationEvent(batchId));
			resolverContext.addPlan((context) -> materialsDataManager.destroyBatch(batchId), resolverContext.getTime());
		}
		BatchId batchId = materialsDataManager.createBatch(materialsProducerId, materialId, amount);
		resolverContext.queueEventForResolution(new BatchCreationObservationEvent(batchId));
		resolverContext.queueEventForResolution(new StageImminentRemovalObservationEvent(stageId));
		resolverContext.addPlan((context) -> materialsDataManager.destroyStage(stageId), resolverContext.getTime());
		return batchId;
	}

	private void handleStageToResourceConversionEventValidation(final ResolverContext resolverContext, final StageToResourceConversionEvent stageToResourceConversionEvent) {
		long amount = stageToResourceConversionEvent.getAmount();
		ResourceId resourceId = stageToResourceConversionEvent.getResourceId();
		StageId stageId = stageToResourceConversionEvent.getStageId();
		validateResourceId(resolverContext, resourceId);
		validateStageId(resolverContext, stageId);
		validateStageIsNotOffered(resolverContext, stageId);
		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		validateCurrentAgent(resolverContext, materialsProducerId);
		validateNonnegativeResourceAmount(resolverContext, amount);
		long currentResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		validateResourceAdditionValue(resolverContext, currentResourceLevel, amount);
	}

	private void handleStageToResourceConversionEventExecution(final ResolverContext resolverContext, final StageToResourceConversionEvent stageToResourceConversionEvent) {
		long amount = stageToResourceConversionEvent.getAmount();
		ResourceId resourceId = stageToResourceConversionEvent.getResourceId();
		StageId stageId = stageToResourceConversionEvent.getStageId();
		final MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
		long previousResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		materialsDataManager.incrementMaterialsProducerResourceLevel(materialsProducerId, resourceId, amount);
		long currentResourceLevel = materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
		for (BatchId batchId : materialsDataManager.getStageBatches(stageId)) {
			resolverContext.queueEventForResolution(new BatchImminentRemovalObservationEvent(batchId));
			resolverContext.addPlan((context) -> materialsDataManager.destroyBatch(batchId), resolverContext.getTime());
		}
		resolverContext.queueEventForResolution(new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId, previousResourceLevel, currentResourceLevel));
		resolverContext.queueEventForResolution(new StageImminentRemovalObservationEvent(stageId));
		resolverContext.addPlan((context) -> materialsDataManager.destroyStage(stageId), resolverContext.getTime());
	}

}
