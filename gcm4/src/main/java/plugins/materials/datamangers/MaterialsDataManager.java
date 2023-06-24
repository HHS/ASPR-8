package plugins.materials.datamangers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.Pair;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.Event;
import nucleus.EventFilter;
import nucleus.IdentifiableFunctionMap;
import nucleus.NucleusError;
import plugins.materials.MaterialsPluginData;
import plugins.materials.events.BatchAdditionEvent;
import plugins.materials.events.BatchAmountUpdateEvent;
import plugins.materials.events.BatchImminentRemovalEvent;
import plugins.materials.events.BatchPropertyDefinitionEvent;
import plugins.materials.events.BatchPropertyUpdateEvent;
import plugins.materials.events.MaterialIdAdditionEvent;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerPropertyDefinitionEvent;
import plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import plugins.materials.events.MaterialsProducerResourceUpdateEvent;
import plugins.materials.events.StageAdditionEvent;
import plugins.materials.events.StageImminentRemovalEvent;
import plugins.materials.events.StageMaterialsProducerUpdateEvent;
import plugins.materials.events.StageMembershipAdditionEvent;
import plugins.materials.events.StageMembershipRemovalEvent;
import plugins.materials.events.StageOfferUpdateEvent;
import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyDefinitionInitialization;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerConstructionData;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyDefinitionInitialization;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageConversionInfo;
import plugins.materials.support.StageId;
import plugins.regions.datamanagers.RegionsDataManager;
import plugins.regions.support.RegionError;
import plugins.regions.support.RegionId;
import plugins.resources.datamanagers.ResourcesDataManager;
import plugins.resources.events.ResourceIdAdditionEvent;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;
import util.wrappers.MutableLong;

/**
 * General manager for all material activities.
 *
 *
 */
public final class MaterialsDataManager extends DataManager {

	/*
	 * Represents the batch
	 */
	private static class BatchRecord {

		/*
		 * The non-negative amount of this batch
		 */
		private double amount;
		private final BatchId batchId;

		/*
		 * The non-null material for this batch
		 */
		private MaterialId materialId;
		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;
		/*
		 * The stage on which this batch is staged -- may be null
		 */
		private StageRecord stageRecord;

		private BatchRecord(BatchId batchId) {
			this.batchId = batchId;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
//			builder.append("BatchRecord [amount=");
			builder.append(amount);
//			builder.append(", batchId=");
//			builder.append(batchId);
//			builder.append(", materialId=");
//			builder.append(materialId);
//			builder.append(", materialsProducerRecord=");
//			builder.append(materialsProducerRecord.materialProducerId);
//
//			builder.append(", stageRecord=");
//			if (stageRecord != null) {
//				builder.append(stageRecord.stageId);
//			} else {
//				builder.append("null");
//			}
//
//			builder.append("]");
			return builder.toString();
		}

	}

	/*
	 * Represents the stage
	 */
	private static class StageRecord {
		/*
		 * The set of batches that are staged on this stage
		 */
		private final Set<BatchRecord> batchRecords = new LinkedHashSet<>();

		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;
		/*
		 * Flag marking that the stage has been offered up to other components. While
		 * true, this stage and its batches are immutable.
		 */
		private boolean offered;
		private final StageId stageId;

		private StageRecord(StageId stageId) {
			this.stageId = stageId;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
//			builder.append("StageRecord [offered=");
			builder.append(offered);
//			builder.append(", stageId=");
			builder.append(stageId);
//			builder.append(", materialsProducerRecord=");
//			builder.append(materialsProducerRecord.materialProducerId);
//			builder.append(", batchRecords=");
//			List<BatchId> batchIds = new ArrayList<>();
//			for (BatchRecord batchRecord : batchRecords) {
//				batchIds.add(batchRecord.batchId);
//			}
//			builder.append(batchIds);
//			builder.append("]");
			return builder.toString();
		}

	}

	private static record MaterialsProducerPropertyDefinitionMutationEvent(
			MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization)
			implements Event {
	}

	private static record BatchPropertyDefinitionMutationEvent(
			BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization) implements Event {
	}

	private static record MaterialsProducerAdditionMutationEvent(
			MaterialsProducerConstructionData materialsProducerConstructionData) implements Event {
	}

	private record BatchAdditionMutationEvent(BatchId batchId, BatchConstructionInfo batchConstructionInfo)
			implements Event {
	}

	private static record BatchMaterialTransferMutionEvent(BatchId sourceBatchId, BatchId destinationBatchId,
			double amount) implements Event {
	}

	private static record BatchRemovalMutationEvent(BatchId batchId) implements Event {
	}

	private static record BatchPropertyUpdateMutationEvent(BatchId batchId, BatchPropertyId batchPropertyId,
			Object batchPropertyValue) implements Event {
	}

	private static record MaterialsProducerPropertyUpdateMutationEvent(MaterialsProducerId materialsProducerId,
			MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue)
			implements Event {
	}

	private static record MoveBatchToInventoryMutationEvent(BatchId batchId) implements Event {
	}

	private static record MoveBatchToStageMutationEvent(BatchId batchId, StageId stageId) implements Event {
	}

	private static record TransferOfferedStageMutationEvent(StageId stageId, MaterialsProducerId materialsProducerId)
			implements Event {
	}

	private static record TransferResourceToRegionMutationEvent(MaterialsProducerId materialsProducerId,
			ResourceId resourceId, RegionId regionId, long amount) implements Event {
	}

	private static record StageAdditionMutationEvent(StageId stageId, MaterialsProducerId materialsProducerId)
			implements Event {
	}

	private static record StageRemovalMutationEvent(StageId stageId, boolean destroyBatches) implements Event {
	}

	private static record StageOfferUpdateMutationEvent(StageId stageId, boolean offer) implements Event {
	}

	private static record ConvertStageToBatchMutationEvent(StageConversionInfo stageConversionInfo, BatchId batchId)
			implements Event {
	}

	private static record ConvertStageToResourceMutationEvent(StageId stageId, ResourceId resourceId, long amount)
			implements Event {
	}

	private static record MaterialIdAdditionMutationEvent(MaterialId materialId) implements Event {
	}

	private static enum MaterialsProducerPropertyUpdateEventFunctionId {
		PRODUCER, PROPERTY
	}

	/*
	 * Represents the materials producer
	 */
	private static class MaterialsProducerRecord {

		/*
		 * Those batches owned by this materials producer that are not staged
		 */
		private final Set<BatchRecord> inventory = new LinkedHashSet<>();

		private MaterialsProducerId materialProducerId;

		private final Map<ResourceId, MutableLong> materialProducerResources = new LinkedHashMap<>();

		/*
		 * Those batches owned by this materials producer that are staged
		 */
		private final Set<StageRecord> stageRecords = new LinkedHashSet<>();

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("MaterialsProducerRecord [materialProducerId=");
			builder.append(materialProducerId);
			builder.append(", materialProducerResources=");
			builder.append(materialProducerResources);
//			builder.append(", stageRecords=");
//
//			List<StageId> stageIds = new ArrayList<>();
//			for (StageRecord stageRecord : stageRecords) {
//				stageIds.add(stageRecord.stageId);
//			}
//			builder.append(stageIds);
//			builder.append(", inventory=");
//
//			List<BatchId> batchIds = new ArrayList<>();
//			for (BatchRecord batchRecord : inventory) {
//				batchIds.add(batchRecord.batchId);
//			}
//			builder.append(batchIds);
//			builder.append("]");
			return builder.toString();
		}

	}

	private static enum MaterialsProducerResourceUpdateEventFunctionId {
		PRODUCER, RESOURCE
	}

	private static enum StageMaterialsProducerUpdateEventFunctionId {
		DESTINATION, SOURCE, STAGE
	}

	private static enum StageOfferUpdateEventFunctionId {
		STAGE
	}

	private final Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> batchPropertyDefinitions = new LinkedHashMap<>();

	private final Map<BatchId, Map<BatchPropertyId, Object>> batchPropertyMap = new LinkedHashMap<>();

	private final Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	private DataManagerContext dataManagerContext;

	private final Set<MaterialId> materialIds = new LinkedHashSet<>();

	private final MaterialsPluginData materialsPluginData;

	private final Map<MaterialsProducerId, MaterialsProducerRecord> materialsProducerMap = new LinkedHashMap<>();

	private final Map<MaterialsProducerPropertyId, PropertyDefinition> materialsProducerPropertyDefinitions = new LinkedHashMap<>();

	private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, Object>> materialsProducerPropertyMap = new LinkedHashMap<>();

	private IdentifiableFunctionMap<MaterialsProducerPropertyUpdateEvent> materialsProducerPropertyUpdateMap = //
			IdentifiableFunctionMap.builder(MaterialsProducerPropertyUpdateEvent.class)//
					.put(MaterialsProducerPropertyUpdateEventFunctionId.PRODUCER, e -> e.materialsProducerId())//
					.put(MaterialsProducerPropertyUpdateEventFunctionId.PROPERTY, e -> e.materialsProducerPropertyId())//
					.build();//

	private IdentifiableFunctionMap<MaterialsProducerResourceUpdateEvent> materialsProducerResourceUpdateMap = //
			IdentifiableFunctionMap.builder(MaterialsProducerResourceUpdateEvent.class)//
					.put(MaterialsProducerResourceUpdateEventFunctionId.RESOURCE, e -> e.resourceId())//
					.put(MaterialsProducerResourceUpdateEventFunctionId.PRODUCER, e -> e.materialsProducerId())//
					.build();//

	/*
	 * The identifier for the next created batch
	 */
	private int nextBatchRecordId;

	/*
	 * The identifier for the next created stage
	 */
	private int nextStageRecordId;

	private final Map<MaterialId, Map<BatchPropertyId, Integer>> nonDefaultBearingBatchPropertyIds = new LinkedHashMap<>();

	private final Map<MaterialsProducerPropertyId, Integer> nonDefaultBearingProducerPropertyIds = new LinkedHashMap<>();

	private Map<MaterialId, boolean[]> nonDefaultChecksForBatches = new LinkedHashMap<>();

	private boolean[] nonDefaultChecksForProducers = new boolean[0];

	private RegionsDataManager regionsDataManager;

	private final Set<ResourceId> resourceIds = new LinkedHashSet<>();

	private ResourcesDataManager resourcesDataManager;

	private IdentifiableFunctionMap<StageMaterialsProducerUpdateEvent> stageMaterialsProducerUpdateMap = //
			IdentifiableFunctionMap.builder(StageMaterialsProducerUpdateEvent.class)//
					.put(StageMaterialsProducerUpdateEventFunctionId.SOURCE, e -> e.previousMaterialsProducerId())//
					.put(StageMaterialsProducerUpdateEventFunctionId.DESTINATION, e -> e.currentMaterialsProducerId())//
					.put(StageMaterialsProducerUpdateEventFunctionId.STAGE, e -> e.stageId())//
					.build();//

	private IdentifiableFunctionMap<StageOfferUpdateEvent> stageOfferUpdateMap = //
			IdentifiableFunctionMap.builder(StageOfferUpdateEvent.class)//
					.put(StageOfferUpdateEventFunctionId.STAGE, e -> e.stageId())//
					.build();//

	/*
	 * <stage id, stage record>
	 */
	private final Map<StageId, StageRecord> stageRecords = new LinkedHashMap<>();

	/**
	 * Constructs the data manager.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA}
	 *                           if the material plugin data is null</li>
	 */
	public MaterialsDataManager(MaterialsPluginData materialsPluginData) {
		if (materialsPluginData == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PLUGIN_DATA);
		}
		this.materialsPluginData = materialsPluginData;
	}

	/**
	 * Creates a batch from the {@linkplain BatchConstructionInfo} contained in the
	 * event. Sets batch properties found in the batch construction info. Generates
	 * a corresponding {@linkplain BatchAdditionEvent} event
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_CONSTRUCTION_INFO}
	 *                           if the batch construction info in the event is
	 *                           null</li>
	 *
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id in the batch construction info
	 *                           is unknown</li>
	 *
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the batch construction info contains an unknown
	 *                           batch property id</li>
	 *
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the batch construction info contains a batch
	 *                           property value that is incompatible with the
	 *                           corresponding property def</li>
	 *
	 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *                           if the batch construction does not contain a batch
	 *                           property value assignment for a batch property that
	 *                           does not have a default value</li>
	 *
	 *
	 *
	 *
	 */
	public BatchId addBatch(BatchConstructionInfo batchConstructionInfo) {

		BatchId batchId = new BatchId(nextBatchRecordId++);
		dataManagerContext.releaseMutationEvent(new BatchAdditionMutationEvent(batchId, batchConstructionInfo));
		return batchId;
	}

	/**
	 * Adds a new material type
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           <li>{@linkplain MaterialsError#DUPLICATE_MATERIAL}
	 *                           if the material id is already present</li>
	 *
	 */
	public void addMaterialId(MaterialId materialId) {
		dataManagerContext.releaseMutationEvent(new MaterialIdAdditionMutationEvent(materialId));
	}

	/**
	 * Add a material producer
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#DUPLICATE_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is already
	 *                           present</li>
	 *
	 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *                           if the materialsProducerConstructionData does not
	 *                           contain a property value for any corresponding
	 *                           materials producer property definition that lacks a
	 *                           default value</li>
	 *
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the materialsProducerConstructionData contains a
	 *                           property value assignment for an unknown materials
	 *                           producer property id.</li>
	 *
	 */
	public void addMaterialsProducer(MaterialsProducerConstructionData materialsProducerConstructionData) {
		dataManagerContext
				.releaseMutationEvent(new MaterialsProducerAdditionMutationEvent(materialsProducerConstructionData));
	}

	private void addNonDefaultBatchProperty(MaterialId materialId, BatchPropertyId batchPropertyId) {

		Map<BatchPropertyId, Integer> map = nonDefaultBearingBatchPropertyIds.get(materialId);
		map.put(batchPropertyId, map.size());

		nonDefaultChecksForBatches.put(materialId, new boolean[map.size()]);
	}

	private void addNonDefaultProducerProperty(MaterialsProducerPropertyId materialsProducerPropertyId) {
		nonDefaultBearingProducerPropertyIds.put(materialsProducerPropertyId,
				nonDefaultBearingProducerPropertyIds.size());
		nonDefaultChecksForProducers = new boolean[nonDefaultBearingProducerPropertyIds.size()];
	}

	/**
	 * Creates a stage. Generates a corresponding {@linkplain StageAdditionEvent}
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError.NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *
	 *                           <li>{@linkplain MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *
	 */
	public StageId addStage(final MaterialsProducerId materialsProducerId) {
		StageId stageId = new StageId(nextStageRecordId++);
		dataManagerContext.releaseMutationEvent(new StageAdditionMutationEvent(stageId, materialsProducerId));
		return stageId;
	}

	/**
	 * Returns true if and only if the batch exists. Null tolerant.
	 */
	public boolean batchExists(final BatchId batchId) {
		return batchRecords.containsKey(batchId);
	}

	/**
	 * Returns true if and only if the batch property exists. Null tolerant.
	 *
	 */
	public boolean batchPropertyIdExists(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		if (map == null) {
			return false;
		}
		return map.containsKey(batchPropertyId);
	}

	private void clearNonDefaultBatchChecks(MaterialId materialId) {
		boolean[] checkArray = nonDefaultChecksForBatches.get(materialId);
		for (int i = 0; i < checkArray.length; i++) {
			checkArray[i] = false;
		}
	}

	private void clearNonDefaultProducerChecks() {
		for (int i = 0; i < nonDefaultChecksForProducers.length; i++) {
			nonDefaultChecksForProducers[i] = false;
		}
	}

	/**
	 *
	 * Converts a non-offered stage, including its associated batches, into a new
	 * batch of the given material. The new batch is placed into inventory.
	 * Generates a corresponding {@linkplain BatchImminentRemovalEvent},
	 * {@linkplain BatchAdditionEvent} and {@linkplain StageImminentRemovalEvent}
	 * events
	 *
	 * @throws ContractException
	 *
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id is unknown</li>
	 *
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           stage id is unknown</li>
	 *
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the stage is offered</li>
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_CONVERSION_INFO}
	 *                           if the stage conversion info in the event is
	 *                           null</li>
	 *
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the batch construction info contains an unknown
	 *                           batch property id</li>
	 *
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the batch construction info contains a batch
	 *                           property value that is incompatible with the
	 *                           corresponding property def</li>
	 *
	 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *                           if the batch construction does not contain a batch
	 *                           property value assignment for a batch property that
	 *                           does not have a default value</li>
	 *
	 *
	 *
	 */
	public BatchId convertStageToBatch(StageConversionInfo stageConversionInfo) {
		BatchId batchId = new BatchId(nextBatchRecordId++);
		dataManagerContext.releaseMutationEvent(new ConvertStageToBatchMutationEvent(stageConversionInfo, batchId));
		return batchId;
	}

	/**
	 *
	 * Converts a non-offered stage, including its associated batches, into an
	 * amount of resource. The resulting resource is owned by the associated
	 * material producer. Generates the corresponding
	 * {@linkplain BatchImminentRemovalEvent},
	 * {@linkplain MaterialsProducerResourceUpdateEvent} and
	 * {@linkplain StageImminentRemovalEvent} events.
	 *
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the stage is offered</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the the resource amount is negative</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the resource amount would cause an overflow of
	 *                           the materials producer's resource level</li>
	 *
	 *
	 */
	public void convertStageToResource(StageId stageId, ResourceId resourceId, long amount) {
		dataManagerContext.releaseMutationEvent(new ConvertStageToResourceMutationEvent(stageId, resourceId, amount));
	}

	/**
	 * Defines a new batch property
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id is unknown</li>
	 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *                           if the batch property id is already present</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           a batch property value assignment has an unknown
	 *                           batch id</li>
	 *                           <li>{@linkplain MaterialsError#MATERIAL_TYPE_MISMATCH}
	 *                           if a batch property value assignment has a batch id
	 *                           associated with a different material id type</li>
	 *
	 *
	 */
	public void defineBatchProperty(BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization) {
		dataManagerContext
				.releaseMutationEvent(new BatchPropertyDefinitionMutationEvent(batchPropertyDefinitionInitialization));
	}

	/**
	 *
	 * Defines a new person property
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION_INITIALIZATION}
	 *                           if the materials producer property definition
	 *                           initialization is null</li>
	 *
	 *                           <li>{@linkplain PropertyError#DUPLICATE_PROPERTY_DEFINITION}
	 *                           if the person property already exists</li>
	 *
	 *                           <li>{@linkplain PropertyError#INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT}
	 *                           if the property definition has no default value and
	 *                           there is no included value assignment for some
	 *                           extant person</li>
	 */
	public void defineMaterialsProducerProperty(
			MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization) {
		dataManagerContext.releaseMutationEvent(new MaterialsProducerPropertyDefinitionMutationEvent(
				materialsProducerPropertyDefinitionInitialization));
	}

	private void destroyBatch(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		batchPropertyMap.remove(batchId);
		/*
		 * Remove the batch from its stage and the master batch tracking map
		 */
		if (batchRecord.stageRecord != null) {
			batchRecord.stageRecord.batchRecords.remove(batchRecord);
		}
		batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
		batchRecords.remove(batchId);
	}

	/**
	 * Returns the amount of material in the batch.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 */
	public double getBatchAmount(final BatchId batchId) {
		validateBatchId(batchId);
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.amount;
	}

	/**
	 * Returns the type of material in the batch.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		validateBatchId(batchId);
		return (T) batchRecords.get(batchId).materialId;
	}

	/**
	 * Returns the materials producer identifier of the batch
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> T getBatchProducer(final BatchId batchId) {
		validateBatchId(batchId);
		return (T) batchRecords.get(batchId).materialsProducerRecord.materialProducerId;
	}

	/**
	 * Returns the property definition for the given {@link MaterialId} and
	 * {@link BatchPropertyId}
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the batch property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the batch property id is unknown</li>
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId,
			final BatchPropertyId batchPropertyId) {
		validateMaterialId(materialId);
		validateBatchPropertyId(materialId, batchPropertyId);
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		return map.get(batchPropertyId);
	}

	/**
	 * Returns the {@link BatchPropertyId} values for the given {@link MaterialId}
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the materials id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID
	 *                           S_PRODUCER_ID} if the materials id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		validateMaterialId(materialId);
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		final Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (final BatchPropertyId batchPropertyId : map.keySet()) {
			result.add((T) batchPropertyId);
		}
		return result;
	}

	/**
	 * Returns the value of the batch property. It is the caller's responsibility to
	 * validate the inputs.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the batch property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the batch property id is unknown</li>
	 */

	@SuppressWarnings("unchecked")
	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId) {
		validateBatchId(batchId);
		final MaterialId batchMaterial = batchRecords.get(batchId).materialId;
		validateBatchPropertyId(batchMaterial, batchPropertyId);
		final Map<BatchPropertyId, Object> map = batchPropertyMap.get(batchId);
		final Object propertyValue = map.get(batchPropertyId);
		if (propertyValue != null) {
			return (T) propertyValue;
		}
		PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(batchMaterial).get(batchPropertyId);
		return (T) propertyDefinition.getDefaultValue().get();
	}

	/**
	 * Returns the stage id the batch. Returns null if the batch is not in a stage.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 */
	public Optional<StageId> getBatchStageId(final BatchId batchId) {
		validateBatchId(batchId);
		final BatchRecord batchRecord = batchRecords.get(batchId);
		StageId stageId;
		if (batchRecord.stageRecord == null) {
			stageId = null;
		} else {
			stageId = batchRecord.stageRecord.stageId;
		}
		return Optional.ofNullable(stageId);
	}

	/**
	 * Returns an event filter used to subscribe to {@link BatchAdditionEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<BatchAdditionEvent> getEventFilterForBatchAdditionEvent() {
		return EventFilter.builder(BatchAdditionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link BatchAmountUpdateEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<BatchAmountUpdateEvent> getEventFilterForBatchAmountUpdateEvent() {
		return EventFilter.builder(BatchAmountUpdateEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link BatchImminentRemovalEvent} events. Matches on all such events.
	 */
	public EventFilter<BatchImminentRemovalEvent> getEventFilterForBatchImminentRemovalEvent() {
		return EventFilter.builder(BatchImminentRemovalEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link BatchPropertyDefinitionEvent} events. Matches on all such events.
	 */
	public EventFilter<BatchPropertyDefinitionEvent> getEventFilterForBatchPropertyDefinitionEvent() {
		return EventFilter.builder(BatchPropertyDefinitionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link BatchPropertyUpdateEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<BatchPropertyUpdateEvent> getEventFilterForBatchPropertyUpdateEvent() {
		return EventFilter.builder(BatchPropertyUpdateEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link MaterialIdAdditionEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<MaterialIdAdditionEvent> getEventFilterForMaterialIdAdditionEvent() {
		return EventFilter.builder(MaterialIdAdditionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerAdditionEvent} events. Matches on all such events.
	 */
	public EventFilter<MaterialsProducerAdditionEvent> getEventFilterForMaterialsProducerAdditionEvent() {
		return EventFilter.builder(MaterialsProducerAdditionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerPropertyDefinitionEvent} events. Matches on all such
	 * events.
	 */
	public EventFilter<MaterialsProducerPropertyDefinitionEvent> getEventFilterForMaterialsProducerPropertyDefinitionEvent() {
		return EventFilter.builder(MaterialsProducerPropertyDefinitionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerPropertyUpdateEvent} events. Matches all such events.
	 *
	 *
	 *
	 *
	 */
	public EventFilter<MaterialsProducerPropertyUpdateEvent> getEventFilterForMaterialsProducerPropertyUpdateEvent() {
		return EventFilter.builder(MaterialsProducerPropertyUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerPropertyUpdateEvent} events. Matches on the materials
	 * producer id and materials producer property id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError.NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is not known</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the materials producer property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the materials producer property id is not
	 *                           known</li>
	 *
	 */
	public EventFilter<MaterialsProducerPropertyUpdateEvent> getEventFilterForMaterialsProducerPropertyUpdateEvent(
			MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return EventFilter.builder(MaterialsProducerPropertyUpdateEvent.class)//
				.addFunctionValuePair(
						materialsProducerPropertyUpdateMap.get(MaterialsProducerPropertyUpdateEventFunctionId.PROPERTY),
						materialsProducerPropertyId)//
				.addFunctionValuePair(
						materialsProducerPropertyUpdateMap.get(MaterialsProducerPropertyUpdateEventFunctionId.PRODUCER),
						materialsProducerId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerResourceUpdateEvent} events. Matches on all such
	 * events.
	 */
	public EventFilter<MaterialsProducerResourceUpdateEvent> getEventFilterForMaterialsProducerResourceUpdateEvent() {
		return EventFilter.builder(MaterialsProducerResourceUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerResourceUpdateEvent} events. Matches on the materials
	 * producer id and resource id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError.NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is not known</li>
	 *                           <li>{@linkplain ResourceError.NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError.UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 */
	public EventFilter<MaterialsProducerResourceUpdateEvent> getEventFilterForMaterialsProducerResourceUpdateEvent(
			MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		validateMaterialsProducerId(materialsProducerId);
		validateResourceId(resourceId);
		return EventFilter.builder(MaterialsProducerResourceUpdateEvent.class)//
				.addFunctionValuePair(
						materialsProducerResourceUpdateMap.get(MaterialsProducerResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.addFunctionValuePair(
						materialsProducerResourceUpdateMap.get(MaterialsProducerResourceUpdateEventFunctionId.PRODUCER),
						materialsProducerId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link MaterialsProducerResourceUpdateEvent} events. Matches on the resource
	 * id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain ResourceError.NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError.UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is not known</li>
	 */
	public EventFilter<MaterialsProducerResourceUpdateEvent> getEventFilterForMaterialsProducerResourceUpdateEvent(
			ResourceId resourceId) {
		validateResourceId(resourceId);
		return EventFilter.builder(MaterialsProducerResourceUpdateEvent.class)//
				.addFunctionValuePair(
						materialsProducerResourceUpdateMap.get(MaterialsProducerResourceUpdateEventFunctionId.RESOURCE),
						resourceId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link StageAdditionEvent}
	 * events. Matches on all such events.
	 */
	public EventFilter<StageAdditionEvent> getEventFilterForStageAdditionEvent() {
		return EventFilter.builder(StageAdditionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageImminentRemovalEvent} events. Matches on all such events.
	 */
	public EventFilter<StageImminentRemovalEvent> getEventFilterForStageImminentRemovalEvent() {
		return EventFilter.builder(StageImminentRemovalEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMaterialsProducerUpdateEvent} events. Matches on all such events.
	 *
	 *
	 */
	public EventFilter<StageMaterialsProducerUpdateEvent> getEventFilterForStageMaterialsProducerUpdateEvent() {
		return EventFilter.builder(StageMaterialsProducerUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMaterialsProducerUpdateEvent} events. Matches on the stage id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is not known</li>
	 */
	public EventFilter<StageMaterialsProducerUpdateEvent> getEventFilterForStageMaterialsProducerUpdateEvent(
			StageId stageId) {
		validateStageId(stageId);
		return EventFilter.builder(StageMaterialsProducerUpdateEvent.class)//
				.addFunctionValuePair(
						stageMaterialsProducerUpdateMap.get(StageMaterialsProducerUpdateEventFunctionId.STAGE), stageId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMaterialsProducerUpdateEvent} events. Matches on the destination
	 * materials producer id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is not known</li>
	 */
	public EventFilter<StageMaterialsProducerUpdateEvent> getEventFilterForStageMaterialsProducerUpdateEvent_ByDestination(
			MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		return EventFilter.builder(StageMaterialsProducerUpdateEvent.class)//
				.addFunctionValuePair(
						stageMaterialsProducerUpdateMap.get(StageMaterialsProducerUpdateEventFunctionId.DESTINATION),
						materialsProducerId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMaterialsProducerUpdateEvent} events. Matches on the source
	 * materials producer id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is not known</li>
	 */
	public EventFilter<StageMaterialsProducerUpdateEvent> getEventFilterForStageMaterialsProducerUpdateEvent_BySource(
			MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		return EventFilter.builder(StageMaterialsProducerUpdateEvent.class)//
				.addFunctionValuePair(
						stageMaterialsProducerUpdateMap.get(StageMaterialsProducerUpdateEventFunctionId.SOURCE),
						materialsProducerId)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMembershipAdditionEvent} events. Matches on all such events.
	 */
	public EventFilter<StageMembershipAdditionEvent> getEventFilterForStageMembershipAdditionEvent() {
		return EventFilter.builder(StageMembershipAdditionEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to
	 * {@link StageMembershipRemovalEvent} events. Matches on all such events.
	 */
	public EventFilter<StageMembershipRemovalEvent> getEventFilterForStageMembershipRemovalEvent() {
		return EventFilter.builder(StageMembershipRemovalEvent.class).build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link StageOfferUpdateEvent}
	 * events. Matches on all such events.
	 *
	 */
	public EventFilter<StageOfferUpdateEvent> getEventFilterForStageOfferUpdateEvent() {
		return EventFilter.builder(StageOfferUpdateEvent.class)//
				.build();
	}

	/**
	 * Returns an event filter used to subscribe to {@link StageOfferUpdateEvent}
	 * events. Matches on the stage id.
	 *
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is not known</li>
	 */
	public EventFilter<StageOfferUpdateEvent> getEventFilterForStageOfferUpdateEvent(StageId stageId) {
		validateStageId(stageId);
		return EventFilter.builder(StageOfferUpdateEvent.class)//
				.addFunctionValuePair(stageOfferUpdateMap.get(StageOfferUpdateEventFunctionId.STAGE), stageId)//
				.build();
	}

	/**
	 * Returns as a list the set of batch ids matching the materials producer where
	 * the batches are not staged.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 */
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<BatchId> result = new ArrayList<>(materialsProducerRecord.inventory.size());
		for (final BatchRecord batchRecord : materialsProducerRecord.inventory) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	/**
	 * Returns as a list the set of batch ids matching the materials producer and
	 * material id where the batches are not staged.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id is unknown</li>
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId,
			final MaterialId materialId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialId(materialId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : materialsProducerRecord.inventory) {
			if (batchRecord.materialId.equals(materialId)) {
				result.add(batchRecord.batchId);
			}
		}
		return result;
	}

	/**
	 * Returns the material id values for the simulation
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialId> Set<T> getMaterialIds() {
		final Set<T> result = new LinkedHashSet<>(materialIds.size());
		for (final MaterialId materialId : materialIds) {
			result.add((T) materialId);
		}
		return result;
	}

	/**
	 * Returns the set of material producer ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		final Set<T> result = new LinkedHashSet<>(materialsProducerMap.size());
		for (final MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
			result.add((T) materialsProducerId);
		}
		return result;
	}

	/**
	 * Returns the property definition for the given
	 * {@link MaterialsProducerPropertyId}
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the materials producer property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the materials producer property id is
	 *                           unknown</li>
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(
			final MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
	}

	/**
	 * Returns the set materials producer property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		final Set<T> result = new LinkedHashSet<>(materialsProducerPropertyDefinitions.keySet().size());
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions
				.keySet()) {
			result.add((T) materialsProducerPropertyId);
		}
		return result;
	}

	/**
	 * Returns the value of the materials producer property.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the materials producer property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the materials producer property id is
	 *                           unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId,
			MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);

		Object propertyValue = materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId);
		if (propertyValue != null) {
			return (T) propertyValue;
		}
		PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
		return (T) propertyDefinition.getDefaultValue().get();
	}

	/**
	 * Returns the materials producer resource level.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 */
	public long getMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		validateMaterialsProducerId(materialsProducerId);
		validateResourceId(resourceId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);
		return mutableLong.getValue();
	}

	/**
	 * Returns as a list the set of stage ids owned by the material producer where
	 * the stage is being offered.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 */
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		final List<StageId> result = new ArrayList<>();
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		for (final StageRecord stageRecord : materialsProducerRecord.stageRecords) {
			if (stageRecord.offered) {
				result.add(stageRecord.stageId);
			}
		}
		return result;
	}

	/**
	 * Returns as a list the set of batch ids matching the stage .
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 */
	public List<BatchId> getStageBatches(final StageId stageId) {
		validateStageId(stageId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		final List<BatchId> result = new ArrayList<>(stageRecord.batchRecords.size());
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	/**
	 * Returns as a list the set of batch ids matching the stage and material type.
	 *
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *                           if the material id is unknown</li>
	 */
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
		validateStageId(stageId);
		validateMaterialId(materialId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		final List<BatchId> result = new ArrayList<>();
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			if (batchRecord.materialId.equals(materialId)) {
				result.add(batchRecord.batchId);
			}
		}
		return result;
	}

	/**
	 * Returns the materials producer id for the stage.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		validateStageId(stageId);
		return (T) stageRecords.get(stageId).materialsProducerRecord.materialProducerId;
	}

	/**
	 * Returns as a list the set of stage ids owned by the material producer.
	 *
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 */
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<StageId> result = new ArrayList<>(materialsProducerRecord.stageRecords.size());
		for (final StageRecord stageRecord : materialsProducerRecord.stageRecords) {
			result.add(stageRecord.stageId);
		}
		return result;
	}

	private void handleBatchAdditionMutationEvent(DataManagerContext dataManagerContext,
			BatchAdditionMutationEvent batchAdditionMutationEvent) {
		BatchConstructionInfo batchConstructionInfo = batchAdditionMutationEvent.batchConstructionInfo();
		BatchId batchId = batchAdditionMutationEvent.batchId();
		validateBatchConstructionInfoNotNull(batchConstructionInfo);
		final MaterialId materialId = batchConstructionInfo.getMaterialId();
		MaterialsProducerId materialsProducerId = batchConstructionInfo.getMaterialsProducerId();
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialId(materialId);
		final double amount = batchConstructionInfo.getAmount();

		final Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
		boolean checkPropertyCoverage = !nonDefaultBearingBatchPropertyIds.get(materialId).isEmpty();

		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			validateBatchPropertyId(materialId, batchPropertyId);
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			final PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
			validateBatchPropertyValueNotNull(batchPropertyValue);
			validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
		}

		if (checkPropertyCoverage) {
			clearNonDefaultBatchChecks(materialId);
			for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
				markBatchPropertyAssigned(materialId, batchPropertyId);
			}
			verifyNonDefaultBatchChecks(materialId);
		}

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final BatchRecord batchRecord = new BatchRecord(batchId);
		batchRecord.amount = amount;
		batchRecord.materialId = materialId;
		batchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(batchRecord);
		batchRecords.put(batchRecord.batchId, batchRecord);

		final Map<BatchPropertyId, Object> map = new LinkedHashMap<>();
		batchPropertyMap.put(batchRecord.batchId, map);

		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			map.put(batchPropertyId, batchPropertyValue);
		}

		if (dataManagerContext.subscribersExist(BatchAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new BatchAdditionEvent(batchRecord.batchId));
		}

	}

	private void handleBatchMaterialTransferMutionEvent(DataManagerContext dataManagerContext,
			BatchMaterialTransferMutionEvent batchMaterialTransferMutionEvent) {
		BatchId sourceBatchId = batchMaterialTransferMutionEvent.sourceBatchId();
		BatchId destinationBatchId = batchMaterialTransferMutionEvent.destinationBatchId();
		double amount = batchMaterialTransferMutionEvent.amount();
		validateBatchId(sourceBatchId);
		validateBatchId(destinationBatchId);
		validateDifferentBatchesForShift(sourceBatchId, destinationBatchId);
		validateMaterialsMatchForShift(sourceBatchId, destinationBatchId);
		validateProducersMatchForShift(sourceBatchId, destinationBatchId);
		validateBatchIsNotOnOfferedStage(sourceBatchId);
		validateBatchIsNotOnOfferedStage(destinationBatchId);
		validateNonnegativeFiniteMaterialAmount(amount);
		validateBatchHasSufficientMaterial(sourceBatchId, amount);
		validateBatchHasSufficientUllage(destinationBatchId, amount);

		BatchRecord sourceBatchRecord = batchRecords.get(sourceBatchId);
		BatchRecord destinationBatchRecord = batchRecords.get(destinationBatchId);

		if (dataManagerContext.subscribersExist(BatchAmountUpdateEvent.class)) {
			double previousSourceAmount = sourceBatchRecord.amount;
			double previousDestinationAmount = destinationBatchRecord.amount;
			sourceBatchRecord.amount -= amount;
			destinationBatchRecord.amount += amount;
			dataManagerContext.releaseObservationEvent(
					new BatchAmountUpdateEvent(sourceBatchId, previousSourceAmount, sourceBatchRecord.amount));
			dataManagerContext.releaseObservationEvent(new BatchAmountUpdateEvent(destinationBatchId,
					previousDestinationAmount, destinationBatchRecord.amount));
		} else {
			sourceBatchRecord.amount -= amount;
			destinationBatchRecord.amount += amount;
		}

	}

	private void handleBatchPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext,
			BatchPropertyDefinitionMutationEvent batchPropertyDefinitionMutationEvent) {
		BatchPropertyDefinitionInitialization batchPropertyDefinitionInitialization = batchPropertyDefinitionMutationEvent
				.batchPropertyDefinitionInitialization();
		MaterialId materialId = batchPropertyDefinitionInitialization.getMaterialId();
		PropertyDefinition propertyDefinition = batchPropertyDefinitionInitialization.getPropertyDefinition();
		BatchPropertyId batchPropertyId = batchPropertyDefinitionInitialization.getPropertyId();

		validateMaterialId(materialId);
		validateNewBatchPropertyId(materialId, batchPropertyId);
		validateBatchPropertyDefinition(propertyDefinition);

		batchPropertyDefinitions.get(materialId).put(batchPropertyId, propertyDefinition);

		// validate the <batchid, property> value assignments
		for (Pair<BatchId, Object> pair : batchPropertyDefinitionInitialization.getPropertyValues()) {
			/*
			 * We know that the batch id and value are non-null and that the value is
			 * compatible with the property definition
			 */
			BatchId batchId = pair.getFirst();
			validateBatchId(batchId);
			MaterialId batchMaterialId = batchRecords.get(batchId).materialId;
			if (!materialId.equals(batchMaterialId)) {
				throw new ContractException(MaterialsError.MATERIAL_TYPE_MISMATCH);
			}
		}

		boolean checkAllBatchesHaveValues = propertyDefinition.getDefaultValue().isEmpty();

		/*
		 * if the property definition does not have a default value then every batch
		 * will need a property value assignment
		 */
		if (checkAllBatchesHaveValues) {
			BitSet coverageSet = new BitSet(batchRecords.size());

			for (Pair<BatchId, Object> pair : batchPropertyDefinitionInitialization.getPropertyValues()) {
				BatchId batchId = pair.getFirst();
				coverageSet.set(batchId.getValue());
			}

			for (BatchId batchId : batchRecords.keySet()) {
				BatchRecord batchRecord = batchRecords.get(batchId);
				if (batchRecord.materialId.equals(materialId)) {
					if (!coverageSet.get(batchId.getValue())) {
						throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
					}
				}
			}
		}

		// if the property definition does not define a default value
		if (checkAllBatchesHaveValues) {
			addNonDefaultBatchProperty(materialId, batchPropertyId);
		}
		// integrate the batch property value assignments
		for (Pair<BatchId, Object> pair : batchPropertyDefinitionInitialization.getPropertyValues()) {
			BatchId batchId = pair.getFirst();
			Object value = pair.getSecond();
			Map<BatchPropertyId, Object> map = batchPropertyMap.get(batchId);
			map.put(batchPropertyId, value);
		}

		if (dataManagerContext.subscribersExist(BatchPropertyDefinitionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new BatchPropertyDefinitionEvent(materialId, batchPropertyId));
		}

	}

	private void handleBatchPropertyUpdateMutationEvent(DataManagerContext dataManagerContext,
			BatchPropertyUpdateMutationEvent batchPropertyUpdateMutationEvent) {
		BatchId batchId = batchPropertyUpdateMutationEvent.batchId();
		BatchPropertyId batchPropertyId = batchPropertyUpdateMutationEvent.batchPropertyId();
		Object batchPropertyValue = batchPropertyUpdateMutationEvent.batchPropertyValue();

		validateBatchId(batchId);
		BatchRecord batchRecord = batchRecords.get(batchId);
		final MaterialId materialId = batchRecord.materialId;
		validateBatchPropertyId(materialId, batchPropertyId);
		final PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateBatchPropertyValueNotNull(batchPropertyValue);
		validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
		validateBatchIsNotOnOfferedStage(batchId);
		final Map<BatchPropertyId, Object> map = batchPropertyMap.get(batchId);

		if (dataManagerContext.subscribersExist(BatchPropertyUpdateEvent.class)) {
			Object previousPropertyValue = map.get(batchPropertyId);
			if (previousPropertyValue == null) {
				previousPropertyValue = propertyDefinition.getDefaultValue().get();
			}
			map.put(batchPropertyId, batchPropertyValue);
			dataManagerContext.releaseObservationEvent(
					new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, batchPropertyValue));
		} else {
			map.put(batchPropertyId, batchPropertyValue);
		}
	}

	private void handleBatchRemovalMutationEvent(DataManagerContext dataManagerContext,
			BatchRemovalMutationEvent batchRemovalMutationEvent) {
		BatchId batchId = batchRemovalMutationEvent.batchId();
		validateBatchId(batchId);
		validateBatchIsNotOnOfferedStage(batchId);
		dataManagerContext.addPlan((context) -> {
			destroyBatch(batchId);
		}, dataManagerContext.getTime());
		if (dataManagerContext.subscribersExist(BatchImminentRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new BatchImminentRemovalEvent(batchId));
		}
	}

	private void handleConvertStageToBatchMutationEvent(DataManagerContext dataManagerContext,
			ConvertStageToBatchMutationEvent convertStageToBatchMutationEvent) {
		StageConversionInfo stageConversionInfo = convertStageToBatchMutationEvent.stageConversionInfo;
		validateStageConversionInfoNotNull(stageConversionInfo);
		MaterialId materialId = stageConversionInfo.getMaterialId();
		BatchId batchId = convertStageToBatchMutationEvent.batchId();
		StageId stageId = stageConversionInfo.getStageId();
		double amount = stageConversionInfo.getAmount();

		validateMaterialId(materialId);
		validateStageId(stageId);
		StageRecord stageRecord = stageRecords.get(stageId);
		final MaterialsProducerId materialsProducerId = stageRecord.materialsProducerRecord.materialProducerId;
		validateStageIsNotOffered(stageId);
		validateNonnegativeFiniteMaterialAmount(amount);

		final Map<BatchPropertyId, Object> propertyValues = stageConversionInfo.getPropertyValues();
		boolean checkPropertyCoverage = !nonDefaultBearingBatchPropertyIds.get(materialId).isEmpty();

		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			validateBatchPropertyId(materialId, batchPropertyId);
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			final PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
			validateBatchPropertyValueNotNull(batchPropertyValue);
			validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
		}

		if (checkPropertyCoverage) {
			clearNonDefaultBatchChecks(materialId);
			for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
				markBatchPropertyAssigned(materialId, batchPropertyId);
			}
			verifyNonDefaultBatchChecks(materialId);
		}

		// add the new batch
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);

		final BatchRecord newBatchRecord = new BatchRecord(batchId);
		newBatchRecord.amount = amount;
		newBatchRecord.materialId = materialId;
		newBatchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(newBatchRecord);
		batchRecords.put(newBatchRecord.batchId, newBatchRecord);

		final Map<BatchPropertyId, Object> map = new LinkedHashMap<>();
		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			Object propertyValue = propertyValues.get(batchPropertyId);
			map.put(batchPropertyId, propertyValue);
		}
		batchPropertyMap.put(newBatchRecord.batchId, map);

		dataManagerContext.addPlan((context) -> {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchPropertyMap.remove(batchRecord.batchId);
				batchRecords.remove(batchRecord.batchId);
			}
			stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
			stageRecords.remove(stageId);

		}, dataManagerContext.getTime());

		if (dataManagerContext.subscribersExist(BatchAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new BatchAdditionEvent(batchId));
		}

		if (dataManagerContext.subscribersExist(BatchImminentRemovalEvent.class)) {
			for (BatchRecord batchRec : stageRecord.batchRecords) {
				dataManagerContext.releaseObservationEvent(new BatchImminentRemovalEvent(batchRec.batchId));
			}
		}
		if (dataManagerContext.subscribersExist(StageImminentRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageImminentRemovalEvent(stageId));
		}

	}

	private void handleConvertStageToResource(DataManagerContext dataManagerContext,
			ConvertStageToResourceMutationEvent convertStageToResourceMutationEvent) {
		ResourceId resourceId = convertStageToResourceMutationEvent.resourceId();
		StageId stageId = convertStageToResourceMutationEvent.stageId();
		long amount = convertStageToResourceMutationEvent.amount();
		validateResourceId(resourceId);
		validateStageId(stageId);
		validateStageIsNotOffered(stageId);
		StageRecord stageRecord = stageRecords.get(stageId);
		final MaterialsProducerId materialsProducerId = stageRecord.materialsProducerRecord.materialProducerId;
		validateNonnegativeResourceAmount(amount);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);
		long previousResourceLevel = mutableLong.getValue();
		validateResourceAdditionValue(previousResourceLevel, amount);

		dataManagerContext.addPlan((context) -> {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchPropertyMap.remove(batchRecord.batchId);
				batchRecords.remove(batchRecord.batchId);
			}
			stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
			stageRecords.remove(stageId);

		}, dataManagerContext.getTime());

		mutableLong.increment(amount);

		if (dataManagerContext.subscribersExist(MaterialsProducerResourceUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new MaterialsProducerResourceUpdateEvent(materialsProducerId,
					resourceId, previousResourceLevel, mutableLong.getValue()));
		}

		if (dataManagerContext.subscribersExist(BatchImminentRemovalEvent.class)) {
			for (BatchRecord batchRecord : stageRecord.batchRecords) {
				dataManagerContext.releaseObservationEvent(new BatchImminentRemovalEvent(batchRecord.batchId));
			}
		}
		if (dataManagerContext.subscribersExist(StageImminentRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageImminentRemovalEvent(stageId));
		}

	}

	private void handleMaterialIdAdditionMutationEvent(DataManagerContext dataManagerContext,
			MaterialIdAdditionMutationEvent materialIdAdditionMutationEvent) {
		MaterialId materialId = materialIdAdditionMutationEvent.materialId();
		validateNewMaterialId(materialId);

		materialIds.add(materialId);

		batchPropertyDefinitions.put(materialId, new LinkedHashMap<>());
		nonDefaultBearingBatchPropertyIds.put(materialId, new LinkedHashMap<>());
		nonDefaultChecksForBatches.put(materialId, new boolean[0]);

		if (dataManagerContext.subscribersExist(MaterialIdAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new MaterialIdAdditionEvent(materialId));
		}
	}

	private void handleMaterialsProducerAdditionMutationEvent(DataManagerContext dataManagerContext,
			MaterialsProducerAdditionMutationEvent materialsProducerAdditionMutationEvent) {
		MaterialsProducerConstructionData materialsProducerConstructionData = materialsProducerAdditionMutationEvent
				.materialsProducerConstructionData();

		// validate the new producer id
		MaterialsProducerId materialsProducerId = materialsProducerConstructionData.getMaterialsProducerId();
		validateNewMaterialsProducerId(materialsProducerId);

		// validate the included property value assignments
		Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsProducerConstructionData
				.getMaterialsProducerPropertyValues();
		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyValues.keySet()) {
			validateMaterialsProducerPropertyId(materialsProducerPropertyId);
			Object propertyValue = materialsProducerPropertyValues.get(materialsProducerPropertyId);
			final PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions
					.get(materialsProducerPropertyId);
			validateValueCompatibility(materialsProducerPropertyId, propertyDefinition, propertyValue);
		}

		/*
		 * if any of the property definitions don't have a default value, then the event
		 * must include those assignments
		 */
		boolean checkPropertyCoverage = !nonDefaultBearingProducerPropertyIds.isEmpty();
		if (checkPropertyCoverage) {
			clearNonDefaultProducerChecks();
			for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyValues.keySet()) {
				markProducerPropertyAssigned(materialsProducerPropertyId);
			}
			verifyNonDefaultChecksForProducers();
		}

		// integrate the new producer into the resources
		final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
		materialsProducerRecord.materialProducerId = materialsProducerId;
		for (final ResourceId resourceId : resourceIds) {
			materialsProducerRecord.materialProducerResources.put(resourceId, new MutableLong());
		}
		materialsProducerMap.put(materialsProducerId, materialsProducerRecord);

		// integrate the new producer into the property values
		Map<MaterialsProducerPropertyId, Object> propertyValueMap = new LinkedHashMap<>();
		materialsProducerPropertyMap.put(materialsProducerId, propertyValueMap);

		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyValues.keySet()) {
			Object propertyValue = materialsProducerPropertyValues.get(materialsProducerPropertyId);
			propertyValueMap.put(materialsProducerPropertyId, propertyValue);
		}

		Map<ResourceId, Long> resourceLevels = materialsProducerConstructionData.getResourceLevels();
		for (ResourceId resourceId : resourceLevels.keySet()) {
			Long resourceLevel = resourceLevels.get(resourceId);
			MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);
			mutableLong.increment(resourceLevel);
		}

		if (dataManagerContext.subscribersExist(MaterialsProducerAdditionEvent.class)) {
			// release notification of the materials producer addition
			MaterialsProducerAdditionEvent.Builder materialsProducerAdditionEventBuilder = //
					MaterialsProducerAdditionEvent.builder()//
							.setMaterialsProducerId(materialsProducerId);//

			List<Object> values = materialsProducerConstructionData.getValues(Object.class);
			for (Object value : values) {
				materialsProducerAdditionEventBuilder.addValue(value);
			}

			MaterialsProducerAdditionEvent materialsProducerAdditionEvent = materialsProducerAdditionEventBuilder
					.build();
			dataManagerContext.releaseObservationEvent(materialsProducerAdditionEvent);
		}

	}

	private void handleMaterialsProducerPropertyDefinitionMutationEvent(DataManagerContext dataManagerContext,
			MaterialsProducerPropertyDefinitionMutationEvent materialsProducerPropertyDefinitionMutationEvent) {

		MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization = materialsProducerPropertyDefinitionMutationEvent
				.materialsProducerPropertyDefinitionInitialization();
		validateMaterialsProducerPropertyDefinitionInitializationNotNull(
				materialsProducerPropertyDefinitionInitialization);
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyDefinitionInitialization
				.getMaterialsProducerPropertyId();
		PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitionInitialization
				.getPropertyDefinition();

		validateMaterialsProducerPropertyIdIsUnknown(materialsProducerPropertyId);
		boolean checkAllProducersHaveValues = propertyDefinition.getDefaultValue().isEmpty();

		// validate the producer property value assignments
		for (Pair<MaterialsProducerId, Object> pair : materialsProducerPropertyDefinitionInitialization
				.getPropertyValues()) {
			MaterialsProducerId materialsProducerId = pair.getFirst();
			validateMaterialsProducerId(materialsProducerId);
		}

		/*
		 * If the property definition does not have a default value then we need to have
		 * a property assignment for each producer
		 */
		if (checkAllProducersHaveValues) {

			final Map<MaterialsProducerId, Boolean> coverageSet = new LinkedHashMap<>();
			for (final MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
				coverageSet.put(materialsProducerId, false);
			}

			for (Pair<MaterialsProducerId, Object> pair : materialsProducerPropertyDefinitionInitialization
					.getPropertyValues()) {
				MaterialsProducerId materialsProducerId = pair.getFirst();
				coverageSet.put(materialsProducerId, true);
			}
			for (MaterialsProducerId materialsProducerId : coverageSet.keySet()) {
				if (!coverageSet.get(materialsProducerId)) {
					throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);
				}
			}
		}

		materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);

		if (checkAllProducersHaveValues) {
			addNonDefaultProducerProperty(materialsProducerPropertyId);
		}

		for (Pair<MaterialsProducerId, Object> pair : materialsProducerPropertyDefinitionInitialization
				.getPropertyValues()) {
			MaterialsProducerId materialsProducerId = pair.getFirst();
			/*
			 * we do not have to validate the value since it is guaranteed to be consistent
			 * with the property definition by contract.
			 */
			Object value = pair.getSecond();
			Map<MaterialsProducerPropertyId, Object> propertyMap = materialsProducerPropertyMap
					.get(materialsProducerId);
			propertyMap.put(materialsProducerPropertyId, value);
		}

		if (dataManagerContext.subscribersExist(MaterialsProducerPropertyDefinitionEvent.class)) {
			dataManagerContext
					.releaseObservationEvent(new MaterialsProducerPropertyDefinitionEvent(materialsProducerPropertyId));
		}

	}

	private void handleMaterialsProducerPropertyUpdateMutationEvent(DataManagerContext dataManagerContext,
			MaterialsProducerPropertyUpdateMutationEvent materialsProducerPropertyUpdateMutationEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyUpdateMutationEvent.materialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyUpdateMutationEvent
				.materialsProducerPropertyId();
		Object materialsProducerPropertyValue = materialsProducerPropertyUpdateMutationEvent
				.materialsProducerPropertyValue();

		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		final PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions
				.get(materialsProducerPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateMaterialProducerPropertyValueNotNull(materialsProducerPropertyValue);
		validateValueCompatibility(materialsProducerPropertyId, propertyDefinition, materialsProducerPropertyValue);
		Map<MaterialsProducerPropertyId, Object> propertyValueMap = materialsProducerPropertyMap
				.get(materialsProducerId);
		Object currentPopertyValue = propertyValueMap.get(materialsProducerPropertyId);

		if (dataManagerContext.subscribersExist(MaterialsProducerPropertyUpdateEvent.class)) {
			if (currentPopertyValue == null) {
				currentPopertyValue = propertyDefinition.getDefaultValue().get();
			}
			propertyValueMap.put(materialsProducerPropertyId, materialsProducerPropertyValue);
			dataManagerContext.releaseObservationEvent(new MaterialsProducerPropertyUpdateEvent(materialsProducerId,
					materialsProducerPropertyId, currentPopertyValue, materialsProducerPropertyValue));
		} else {
			propertyValueMap.put(materialsProducerPropertyId, materialsProducerPropertyValue);
		}

	}

	private void handleMoveBatchToInventoryMutationEvent(DataManagerContext dataManagerContext,
			MoveBatchToInventoryMutationEvent moveBatchToInventoryMutationEvent) {

		BatchId batchId = moveBatchToInventoryMutationEvent.batchId();
		validateBatchId(batchId);
		BatchRecord batchRecord = batchRecords.get(batchId);
		validateBatchIsStaged(batchId);
		final StageId stageId = batchRecord.stageRecord.stageId;
		validateStageIsNotOffered(stageId);
		batchRecord.stageRecord.batchRecords.remove(batchRecord);
		batchRecord.stageRecord = null;
		batchRecord.materialsProducerRecord.inventory.add(batchRecord);
		if (dataManagerContext.subscribersExist(StageMembershipRemovalEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageMembershipRemovalEvent(batchId, stageId));
		}
	}

	private void handleMoveBatchToStageMutationEvent(DataManagerContext dataManagerContext,
			MoveBatchToStageMutationEvent moveBatchToStageMutationEvent) {
		BatchId batchId = moveBatchToStageMutationEvent.batchId();
		StageId stageId = moveBatchToStageMutationEvent.stageId();
		validateBatchId(batchId);
		validateBatchIsNotStaged(batchId);
		validateStageId(stageId);
		validateStageIsNotOffered(stageId);
		validateBatchAndStageOwnersMatch(batchId, stageId);

		final BatchRecord batchRecord = batchRecords.get(batchId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		batchRecord.stageRecord = stageRecord;
		stageRecord.batchRecords.add(batchRecord);
		batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
		if (dataManagerContext.subscribersExist(StageMembershipAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageMembershipAdditionEvent(batchId, stageId));
		}
	}

	private void handleResourceIdAdditionEvent(DataManagerContext dataManagerContext,
			ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.resourceId();
		if (resourceId == null || resourceIds.contains(resourceId)) {
			return;
		}

		resourceIds.add(resourceId);

		for (MaterialsProducerRecord materialsProducerRecord : materialsProducerMap.values()) {
			materialsProducerRecord.materialProducerResources.put(resourceId, new MutableLong());
		}
	}

	private void handleStageAdditionMutationEvent(DataManagerContext dataManagerContext,
			StageAdditionMutationEvent stageAdditionMutationEvent) {
		StageId stageId = stageAdditionMutationEvent.stageId();
		MaterialsProducerId materialsProducerId = stageAdditionMutationEvent.materialsProducerId();
		validateMaterialsProducerId(materialsProducerId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final StageRecord stageRecord = new StageRecord(stageId);
		stageRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.stageRecords.add(stageRecord);
		stageRecords.put(stageRecord.stageId, stageRecord);
		if (dataManagerContext.subscribersExist(StageAdditionEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageAdditionEvent(stageRecord.stageId));
		}
	}

	private void handleStageOfferUpdateMutationEvent(DataManagerContext dataManagerContext,
			StageOfferUpdateMutationEvent stageOfferUpdateMutationEvent) {

		StageId stageId = stageOfferUpdateMutationEvent.stageId();
		boolean offer = stageOfferUpdateMutationEvent.offer();
		validateStageId(stageId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		if (dataManagerContext.subscribersExist(StageOfferUpdateEvent.class)) {
			boolean previousState = stageRecord.offered;
			stageRecord.offered = offer;
			dataManagerContext.releaseObservationEvent(new StageOfferUpdateEvent(stageId, previousState, offer));
		} else {
			stageRecord.offered = offer;
		}

	}

	private void handleStageRemovalMutationEvent(DataManagerContext dataManagerContext,
			StageRemovalMutationEvent stageRemovalMutationEvent) {
		boolean destroyBatches = stageRemovalMutationEvent.destroyBatches();
		StageId stageId = stageRemovalMutationEvent.stageId();
		validateStageId(stageId);
		validateStageIsNotOffered(stageId);
		StageRecord stageRecord = stageRecords.get(stageId);

		if (destroyBatches) {
			// plan for the removal of the stage and the batches
			dataManagerContext.addPlan((context) -> {
				for (final BatchRecord batchRecord : stageRecord.batchRecords) {
					batchPropertyMap.remove(batchRecord.batchId);
					batchRecords.remove(batchRecord.batchId);
				}
				stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
				stageRecords.remove(stageId);
			}, dataManagerContext.getTime());

			if (dataManagerContext.subscribersExist(BatchImminentRemovalEvent.class)) {
				for (BatchRecord batchRecord : stageRecord.batchRecords) {
					dataManagerContext.releaseObservationEvent(new BatchImminentRemovalEvent(batchRecord.batchId));
				}
			}
			if (dataManagerContext.subscribersExist(StageImminentRemovalEvent.class)) {
				dataManagerContext.releaseObservationEvent(new StageImminentRemovalEvent(stageId));
			}
		} else {
			// plan for the removal of the stage and return of the batches to
			// inventory
			dataManagerContext.addPlan((context) -> {
				for (final BatchRecord batchRecord : stageRecord.batchRecords) {
					batchRecord.stageRecord = null;
					batchRecord.materialsProducerRecord.inventory.add(batchRecord);
				}
				stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
				stageRecords.remove(stageId);
			}, dataManagerContext.getTime());

			if (dataManagerContext.subscribersExist(StageImminentRemovalEvent.class)) {
				dataManagerContext.releaseObservationEvent(new StageImminentRemovalEvent(stageId));
			}
		}

	}

	private void handleTransferOfferedStageMutationEvent(DataManagerContext dataManagerContext,
			TransferOfferedStageMutationEvent transferOfferedStageMutationEvent) {
		MaterialsProducerId materialsProducerId = transferOfferedStageMutationEvent.materialsProducerId();
		StageId stageId = transferOfferedStageMutationEvent.stageId();

		validateStageId(stageId);
		validateMaterialsProducerId(materialsProducerId);
		validateStageIsOffered(stageId);
		validateStageNotOwnedByReceivingMaterialsProducer(stageId, materialsProducerId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		final MaterialsProducerRecord currentMaterialsProducerRecord = stageRecord.materialsProducerRecord;
		MaterialsProducerId stageProducer = currentMaterialsProducerRecord.materialProducerId;
		final MaterialsProducerRecord newMaterialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		currentMaterialsProducerRecord.stageRecords.remove(stageRecord);
		newMaterialsProducerRecord.stageRecords.add(stageRecord);
		stageRecord.materialsProducerRecord = newMaterialsProducerRecord;
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			batchRecord.materialsProducerRecord = newMaterialsProducerRecord;
		}
		stageRecord.offered = false;
		if (dataManagerContext.subscribersExist(StageMaterialsProducerUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(
					new StageMaterialsProducerUpdateEvent(stageId, stageProducer, materialsProducerId));
		}
		if (dataManagerContext.subscribersExist(StageOfferUpdateEvent.class)) {
			dataManagerContext.releaseObservationEvent(new StageOfferUpdateEvent(stageId, true, false));
		}

	}

	private void handleTransferResourceToRegionMutationEvent(DataManagerContext dataManagerContext,
			TransferResourceToRegionMutationEvent transferResourceToRegionMutationEvent) {
		MaterialsProducerId materialsProducerId = transferResourceToRegionMutationEvent.materialsProducerId();
		ResourceId resourceId = transferResourceToRegionMutationEvent.resourceId();
		RegionId regionId = transferResourceToRegionMutationEvent.regionId();
		long amount = transferResourceToRegionMutationEvent.amount();
		validateResourceId(resourceId);
		validateRegionId(regionId);
		validateMaterialsProducerId(materialsProducerId);
		validateNonnegativeResourceAmount(amount);
		validateMaterialsProducerHasSufficientResource(materialsProducerId, resourceId, amount);
		final long currentResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(currentResourceLevel, amount);

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);

		if (dataManagerContext.subscribersExist(MaterialsProducerResourceUpdateEvent.class)) {
			long previousMaterialsProducerResourceLevel = mutableLong.getValue();
			mutableLong.decrement(amount);
			long currentMaterialsProducerResourceLevel = mutableLong.getValue();
			resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
			dataManagerContext.releaseObservationEvent(new MaterialsProducerResourceUpdateEvent(materialsProducerId,
					resourceId, previousMaterialsProducerResourceLevel, currentMaterialsProducerResourceLevel));
		} else {
			mutableLong.decrement(amount);
			resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
		}
	}

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		if (dataManagerContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		resourcesDataManager = dataManagerContext.getDataManager(ResourcesDataManager.class);
		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		this.dataManagerContext = dataManagerContext;

		dataManagerContext.subscribe(BatchAdditionMutationEvent.class, this::handleBatchAdditionMutationEvent);
		dataManagerContext.subscribe(MaterialIdAdditionMutationEvent.class,
				this::handleMaterialIdAdditionMutationEvent);
		dataManagerContext.subscribe(MaterialsProducerAdditionMutationEvent.class,
				this::handleMaterialsProducerAdditionMutationEvent);
		dataManagerContext.subscribe(StageAdditionMutationEvent.class, this::handleStageAdditionMutationEvent);
		dataManagerContext.subscribe(ConvertStageToBatchMutationEvent.class,
				this::handleConvertStageToBatchMutationEvent);
		dataManagerContext.subscribe(ConvertStageToResourceMutationEvent.class, this::handleConvertStageToResource);
		dataManagerContext.subscribe(BatchPropertyDefinitionMutationEvent.class,
				this::handleBatchPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(MaterialsProducerPropertyDefinitionMutationEvent.class,
				this::handleMaterialsProducerPropertyDefinitionMutationEvent);
		dataManagerContext.subscribe(MoveBatchToInventoryMutationEvent.class,
				this::handleMoveBatchToInventoryMutationEvent);
		dataManagerContext.subscribe(MoveBatchToStageMutationEvent.class, this::handleMoveBatchToStageMutationEvent);
		dataManagerContext.subscribe(BatchRemovalMutationEvent.class, this::handleBatchRemovalMutationEvent);
		dataManagerContext.subscribe(StageRemovalMutationEvent.class, this::handleStageRemovalMutationEvent);
		dataManagerContext.subscribe(BatchPropertyUpdateMutationEvent.class,
				this::handleBatchPropertyUpdateMutationEvent);
		dataManagerContext.subscribe(MaterialsProducerPropertyUpdateMutationEvent.class,
				this::handleMaterialsProducerPropertyUpdateMutationEvent);
		dataManagerContext.subscribe(StageOfferUpdateMutationEvent.class, this::handleStageOfferUpdateMutationEvent);
		dataManagerContext.subscribe(BatchMaterialTransferMutionEvent.class,
				this::handleBatchMaterialTransferMutionEvent);
		dataManagerContext.subscribe(TransferOfferedStageMutationEvent.class,
				this::handleTransferOfferedStageMutationEvent);
		dataManagerContext.subscribe(TransferResourceToRegionMutationEvent.class,
				this::handleTransferResourceToRegionMutationEvent);

		for (final MaterialId materialId : materialsPluginData.getMaterialIds()) {
			materialIds.add(materialId);

			Map<BatchPropertyId, PropertyDefinition> propMap = new LinkedHashMap<>();
			batchPropertyDefinitions.put(materialId, propMap);
			nonDefaultBearingBatchPropertyIds.put(materialId, new LinkedHashMap<>());
			for (final BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(materialId)) {
				final PropertyDefinition propertyDefinition = materialsPluginData.getBatchPropertyDefinition(materialId,
						batchPropertyId);
				propMap.put(batchPropertyId, propertyDefinition);
				Map<BatchPropertyId, Integer> map = nonDefaultBearingBatchPropertyIds.get(materialId);
				if (propertyDefinition.getDefaultValue().isEmpty()) {
					map.put(batchPropertyId, map.size());
				}
			}
			int nonDefaultPropertyCount = nonDefaultBearingBatchPropertyIds.get(materialId).size();
			nonDefaultChecksForBatches.put(materialId, new boolean[nonDefaultPropertyCount]);
		}

		for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			resourceIds.add(resourceId);
		}
		for (ResourceId resourceId : materialsPluginData.getResourceIds()) {
			if (!resourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID,
						resourceId + " in resource levels of materials producers");
			}
		}

		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData
				.getMaterialsProducerPropertyIds()) {
			PropertyDefinition propertyDefinition = materialsPluginData
					.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
			if (propertyDefinition.getDefaultValue().isEmpty()) {
				nonDefaultBearingProducerPropertyIds.put(materialsProducerPropertyId,
						nonDefaultBearingProducerPropertyIds.size());
			}
		}
		nonDefaultChecksForProducers = new boolean[nonDefaultBearingProducerPropertyIds.size()];

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
			materialsProducerRecord.materialProducerId = materialsProducerId;
			for (final ResourceId resourceId : resourceIds) {
				materialsProducerRecord.materialProducerResources.put(resourceId, new MutableLong());
			}
			materialsProducerMap.put(materialsProducerId, materialsProducerRecord);
			materialsProducerPropertyMap.put(materialsProducerId, new LinkedHashMap<>());
		}

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			Map<MaterialsProducerPropertyId, Object> materialsProducerPropertyValues = materialsPluginData
					.getMaterialsProducerPropertyValues(materialsProducerId);
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyValues
					.keySet()) {
				final Object value = materialsProducerPropertyValues.get(materialsProducerPropertyId);
				Map<MaterialsProducerPropertyId, Object> propertyToValueMap = materialsProducerPropertyMap
						.get(materialsProducerId);
				propertyToValueMap.put(materialsProducerPropertyId, value);
			}
		}

		for (final StageId stageId : materialsPluginData.getStageIds()) {
			final StageRecord stageRecord = new StageRecord(stageId);
			stageRecord.offered = materialsPluginData.isStageOffered(stageId);
			stageRecords.put(stageRecord.stageId, stageRecord);
		}

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
			List<StageId> materialsProducerStages = materialsPluginData.getMaterialsProducerStages(materialsProducerId);
			for (StageId stageId : materialsProducerStages) {
				StageRecord stageRecord = stageRecords.get(stageId);
				stageRecord.materialsProducerRecord = materialsProducerRecord;
				materialsProducerRecord.stageRecords.add(stageRecord);
			}
		}

		nextStageRecordId = materialsPluginData.getNextStageRecordId();

		for (final BatchId batchId : materialsPluginData.getBatchIds()) {

			final MaterialId materialId = materialsPluginData.getBatchMaterial(batchId);
			final double amount = materialsPluginData.getBatchAmount(batchId);
			final BatchRecord batchRecord = new BatchRecord(batchId);
			batchRecord.amount = amount;
			batchRecord.materialId = materialId;
			batchRecords.put(batchRecord.batchId, batchRecord);
			final Map<BatchPropertyId, Object> map = new LinkedHashMap<>();
			batchPropertyMap.put(batchRecord.batchId, map);

			Map<BatchPropertyId, Object> batchPropertyValues = materialsPluginData.getBatchPropertyValues(batchId);
			for (BatchPropertyId batchPropertyId : batchPropertyValues.keySet()) {
				final Object batchPropertyValue = batchPropertyValues.get(batchPropertyId);
				map.put(batchPropertyId, batchPropertyValue);
			}
		}
		nextBatchRecordId = materialsPluginData.getNextBatchRecordId();

		for (MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			List<BatchId> materialsProducerInventoryBatches = materialsPluginData
					.getMaterialsProducerInventoryBatches(materialsProducerId);
			for (BatchId batchId : materialsProducerInventoryBatches) {
				BatchRecord batchRecord = batchRecords.get(batchId);
				MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
				batchRecord.materialsProducerRecord = materialsProducerRecord;
				materialsProducerRecord.inventory.add(batchRecord);
			}

		}

		for (final StageId stageId : materialsPluginData.getStageIds()) {
			final Set<BatchId> batches = materialsPluginData.getStageBatches(stageId);
			for (final BatchId batchId : batches) {
				final BatchRecord batchRecord = batchRecords.get(batchId);
				if (batchRecord.stageRecord != null) {
					throw new ContractException(MaterialsError.BATCH_ALREADY_STAGED);
				}
				final StageRecord stageRecord = stageRecords.get(stageId);
				batchRecord.materialsProducerRecord = stageRecord.materialsProducerRecord;
				batchRecord.stageRecord = stageRecord;
				stageRecord.batchRecords.add(batchRecord);
				batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
			}
		}

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			for (final ResourceId resourceId : resourceIds) {
				final Long amount = materialsPluginData.getMaterialsProducerResourceLevel(materialsProducerId,
						resourceId);
				if (amount != null) {
					final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap
							.get(materialsProducerId);
					final MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);
					mutableLong.increment(amount);
				}
			}
		}
		dataManagerContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
		if (dataManagerContext.stateRecordingIsScheduled()) {
			dataManagerContext.subscribeToSimulationClose(this::recordSimulationState);
		}
	}

	/**
	 * Returns true if and only if the stage is offered.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 */
	public boolean isStageOffered(final StageId stageId) {
		validateStageId(stageId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		return stageRecord.offered;
	}

	private void markBatchPropertyAssigned(MaterialId materialId, BatchPropertyId batchPropertyId) {
		Map<BatchPropertyId, Integer> map = nonDefaultBearingBatchPropertyIds.get(materialId);
		Integer index = map.get(batchPropertyId);
		if (index != null) {
			boolean[] checkArray = nonDefaultChecksForBatches.get(materialId);
			checkArray[index] = true;
		}
	}

	private void markProducerPropertyAssigned(MaterialsProducerPropertyId materialsProducerPropertyId) {
		Integer nonDefaultPropertyIndex = nonDefaultBearingProducerPropertyIds.get(materialsProducerPropertyId);
		if (nonDefaultPropertyIndex != null) {
			nonDefaultChecksForProducers[nonDefaultPropertyIndex] = true;
		}
	}

	/**
	 * Returns true if and only if the material exists. Tolerates null.
	 */
	public boolean materialIdExists(final MaterialId materialId) {
		return (materialIds.contains(materialId));
	}

	/**
	 * Returns true if and only if the material producer id exists. Null tolerant.
	 */
	public boolean materialsProducerIdExists(final MaterialsProducerId materialsProducerId) {
		return materialsProducerMap.containsKey(materialsProducerId);
	}

	/**
	 * Returns true if and only if the material producer property id exists. Null
	 * tolerant.
	 */
	public boolean materialsProducerPropertyIdExists(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyDefinitions.keySet().contains(materialsProducerPropertyId);
	}

	/**
	 * <li>Removes a batch from a non-offered stage, placing it into the materials
	 * producer's inventory. Generates a corresponding
	 * {@linkplain StageMembershipRemovalEvent}
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#BATCH_NOT_STAGED} if
	 *                           the batch is not staged</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE }
	 *                           if the stage containing the batch is offered</li>
	 *
	 */
	public void moveBatchToInventory(BatchId batchId) {
		dataManagerContext.releaseMutationEvent(new MoveBatchToInventoryMutationEvent(batchId));
	}

	/**
	 * Removes a batch frominventory, placing it on a stage. Generates a
	 * corresponding {@linkplain StageMembershipAdditionEvent}
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#BATCH_ALREADY_STAGED}
	 *                           if the batch is already staged</li>
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the stage is offered</li>
	 *                           <li>{@linkplain MaterialsError#BATCH_STAGED_TO_DIFFERENT_OWNER}
	 *                           if batch and stage do not have the same owning
	 *                           materials producer</li>
	 *
	 *
	 */
	public void moveBatchToStage(BatchId batchId, StageId stageId) {
		dataManagerContext.releaseMutationEvent(new MoveBatchToStageMutationEvent(batchId, stageId));
	}

	private void recordSimulationState(DataManagerContext dataManagerContext) {

		MaterialsPluginData.Builder builder = MaterialsPluginData.builder();

		builder.setNextBatchRecordId(nextBatchRecordId);
		builder.setNextStageRecordId(nextStageRecordId);

		for (MaterialsProducerId materialsProducerId : materialsProducerPropertyMap.keySet()) {
			Map<MaterialsProducerPropertyId, Object> map = materialsProducerPropertyMap.get(materialsProducerId);
			for (MaterialsProducerPropertyId materialsProducerPropertyId : map.keySet()) {
				Object propertyValue = map.get(materialsProducerPropertyId);
				builder.setMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId,
						propertyValue);
			}
		}

		for (MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
			builder.addMaterialsProducerId(materialsProducerId);
			final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
			for (ResourceId resourId : materialsProducerRecord.materialProducerResources.keySet()) {
				MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourId);
				builder.setMaterialsProducerResourceLevel(materialsProducerId, resourId, mutableLong.getValue());
			}
		}

		for (MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions
					.get(materialsProducerPropertyId);
			builder.defineMaterialsProducerProperty(materialsProducerPropertyId, propertyDefinition);
		}

		for (MaterialId materialId : materialIds) {
			builder.addMaterial(materialId);
		}

		for (MaterialId materialId : batchPropertyDefinitions.keySet()) {
			Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
			for (BatchPropertyId batchPropertyId : map.keySet()) {
				PropertyDefinition propertyDefinition = map.get(batchPropertyId);
				builder.defineBatchProperty(materialId, batchPropertyId, propertyDefinition);
			}
		}

		for (BatchId batchId : batchRecords.keySet()) {
			BatchRecord batchRecord = batchRecords.get(batchId);
			builder.addBatch(batchId, batchRecord.materialId, batchRecord.amount);
		}

		for(MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
			MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
			for(BatchRecord batchRecord : materialsProducerRecord.inventory) {
				builder.addBatchToMaterialsProducerInventory(batchRecord.batchId, materialsProducerId);
			}
		}
		
		for (BatchId batchId : batchPropertyMap.keySet()) {
			Map<BatchPropertyId, Object> map = batchPropertyMap.get(batchId);
			for (BatchPropertyId batchPropertyId : map.keySet()) {
				Object propertyValue = map.get(batchPropertyId);
				builder.setBatchPropertyValue(batchId, batchPropertyId, propertyValue);
			}
		}

		for (StageId stageId : stageRecords.keySet()) {
			StageRecord stageRecord = stageRecords.get(stageId);
			builder.addStage(stageId, stageRecord.offered);
			for (BatchId batchId : getStageBatches(stageId)) {
				builder.addBatchToStage(stageId, batchId);
			}
		}

		for (final MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
			final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);

			for (StageRecord stageRecord : materialsProducerRecord.stageRecords) {
				StageId stageId = stageRecord.stageId;
				builder.addStageToMaterialProducer(stageId, materialsProducerId);
			}
		}

		dataManagerContext.releaseOutput(builder.build());

	}

	/**
	 * Removes the given batch. Generates a corresponding
	 * {@linkplain BatchImminentRemovalEvent}
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the batch is on an offered stage</li>
	 *
	 *
	 *
	 */
	public void removeBatch(BatchId batchId) {
		dataManagerContext.releaseMutationEvent(new BatchRemovalMutationEvent(batchId));
	}

	/**
	 *
	 * Destroys a non-offered stage and optionally destroys any associated batches.
	 * Generates the corresponding {@linkplain BatchImminentRemovalEvent} and
	 * {@linkplain StageImminentRemovalEvent} events.
	 *
	 * @throws ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID} if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if
	 *                           the stage id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if stage is offered</li>
	 */
	public void removeStage(StageId stageId, boolean destroyBatches) {
		dataManagerContext.releaseMutationEvent(new StageRemovalMutationEvent(stageId, destroyBatches));
	}

	/**
	 *
	 * Set a batch's property value. Generates a corresponding
	 * {@linkplain BatchPropertyUpdateEvent}
	 *
	 * @throws ContractException
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the batch id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the batch property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the batch property id is unknown</li>
	 *                           <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if
	 *                           batch property is not mutable</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the batch property value is null</li>
	 *                           <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE}
	 *                           if the batch property value is not compatible with
	 *                           the corresponding property definition</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the batch in on an offered stage</li>
	 *
	 *
	 *
	 */
	public void setBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue) {
		dataManagerContext.releaseMutationEvent(
				new BatchPropertyUpdateMutationEvent(batchId, batchPropertyId, batchPropertyValue));
	}

	/**
	 * Assigns a property value to a materials producer. Generates a corresponding
	 * {@linkplain MaterialsProducerPropertyUpdateEvent}
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if
	 *                           the materials producer property id is null</li>
	 *                           <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID}
	 *                           if the materials producer property id is
	 *                           unknown</li>
	 *                           <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if
	 *                           the materials producer property is immutable</li>
	 *                           <li>{@linkplain PropertyError#NULL_PROPERTY_VALUE}
	 *                           if the property value is null</li>
	 *                           {@linkplain PropertyError#INCOMPATIBLE_VALUE} if
	 *                           the property value is incompatible with the
	 *                           corresponding property definition</li>
	 *
	 *
	 */
	public void setMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId,
			MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		dataManagerContext.releaseMutationEvent(new MaterialsProducerPropertyUpdateMutationEvent(materialsProducerId,
				materialsProducerPropertyId, materialsProducerPropertyValue));
	}

	/**
	 * Sets the offer state of a stage. Generates a corresponding
	 * {@linkplain StageOfferUpdateEvent}
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID } if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID }
	 *                           if the stage id is unknown</li>
	 *
	 */
	public void setStageOfferState(StageId stageId, boolean offer) {
		dataManagerContext.releaseMutationEvent(new StageOfferUpdateMutationEvent(stageId, offer));
	}

	/**
	 * Returns true if and only if the stage exists. Null tolerant.
	 */
	public boolean stageExists(final StageId stageId) {
		return stageRecords.containsKey(stageId);
	}

	/**
	 * Transfers an amount of material from one batch to another batch of the same
	 * material type and material producer. Generates a corresponding
	 * {@linkplain BatchAmountUpdateEvent} for each batch.
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the source batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the source batch id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_BATCH_ID} if
	 *                           the destination batch id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if
	 *                           the destination batch id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#REFLEXIVE_BATCH_SHIFT}
	 *                           if the source and destination batches are the
	 *                           same</li>
	 *                           <li>{@linkplain MaterialsError#MATERIAL_TYPE_MISMATCH}
	 *                           if the batches do not have the same material
	 *                           type</li>
	 *                           <li>{@linkplain MaterialsError#BATCH_SHIFT_WITH_MULTIPLE_OWNERS}
	 *                           if the batches have different owning materials
	 *                           producers</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the source batch is on a stage is offered</li>
	 *                           <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE}
	 *                           if the destination batch is on a stage is
	 *                           offered</li>
	 *                           <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT}
	 *                           if the shift amount is not a finite number</li>
	 *                           <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT}
	 *                           if the shift amount is negative</li>
	 *                           <li>{@linkplain MaterialsError#INSUFFICIENT_MATERIAL_AVAILABLE}
	 *                           if the shift amount exceeds the available material
	 *                           on the source batch</li>
	 *                           <li>{@linkplain MaterialsError#MATERIAL_ARITHMETIC_EXCEPTION}
	 *                           if the shift amount would cause an overflow on the
	 *                           destination batch</li>
	 *
	 *
	 *                           </blockquote></li>
	 *
	 */

	public void transferMaterialBetweenBatches(BatchId sourceBatchId, BatchId destinationBatchId, double amount) {

		dataManagerContext
				.releaseMutationEvent(new BatchMaterialTransferMutionEvent(sourceBatchId, destinationBatchId, amount));
	}

	/**
	 *
	 * Transfers an offered stage from the current owning materials producer to
	 * another and marks the stage as not offered. Generates the corresponding
	 * {@linkplain StageMaterialsProducerUpdateEvent}
	 * {@linkplain StageOfferUpdateEvent}
	 *
	 * @throws ContractException
	 *
	 *
	 *                           <li>{@linkplain MaterialsError#NULL_STAGE_ID } if
	 *                           the stage id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID }
	 *                           if the stage id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID }
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID }
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#UNOFFERED_STAGE_NOT_TRANSFERABLE }
	 *                           if the stage is not offered</li>
	 *                           <li>{@linkplain MaterialsError#REFLEXIVE_STAGE_TRANSFER }
	 *                           if the source and destination materials producers
	 *                           are the same</li>
	 *
	 *
	 */
	public void transferOfferedStage(StageId stageId, MaterialsProducerId materialsProducerId) {
		dataManagerContext.releaseMutationEvent(new TransferOfferedStageMutationEvent(stageId, materialsProducerId));
	}

	/**
	 *
	 *
	 * Transfers a resource amount from a materials producer to a region. Generates
	 * a corresponding {@linkplain RegionResourceAdditionEvent} and
	 * {@linkplain MaterialsProducerResourceUpdateEvent}
	 *
	 * @throws ContractException *
	 *
	 *                           <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if
	 *                           the resource id is null</li>
	 *                           <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID}
	 *                           if the resource id is unknown</li>
	 *                           <li>{@linkplain RegionError#NULL_REGION_ID} if the
	 *                           region id is null</li>
	 *                           <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if
	 *                           the region id is unknown</li>
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is null</li>
	 *                           <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *                           if the materials producer id is unknown</li>
	 *                           <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT}
	 *                           if the materials amount is negative</li>
	 *                           <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *                           if the materials amount exceeds the resource level
	 *                           of the materials producer</li>
	 *                           <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *                           if the materials amount would cause an overflow of
	 *                           the regions resource level</li>
	 *
	 *
	 *
	 */
	public void transferResourceToRegion(MaterialsProducerId materialsProducerId, ResourceId resourceId,
			RegionId regionId, long amount) {
		dataManagerContext.releaseMutationEvent(
				new TransferResourceToRegionMutationEvent(materialsProducerId, resourceId, regionId, amount));
	}

	private void validateBatchAndStageOwnersMatch(final BatchId batchId, final StageId stageId) {
		final MaterialsProducerId batchProducerId = batchRecords
				.get(batchId).materialsProducerRecord.materialProducerId;
		final MaterialsProducerId stageProducerId = stageRecords
				.get(stageId).materialsProducerRecord.materialProducerId;
		if (!batchProducerId.equals(stageProducerId)) {
			throw new ContractException(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER);
		}
	}

	private void validateBatchConstructionInfoNotNull(final BatchConstructionInfo batchConstructionInfo) {
		if (batchConstructionInfo == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO);
		}
	}

	/*
	 * Precondition : batch must exist
	 */
	private void validateBatchHasSufficientMaterial(final BatchId batchId, final double amount) {
		if (batchRecords.get(batchId).amount < amount) {
			throw new ContractException(MaterialsError.INSUFFICIENT_MATERIAL_AVAILABLE);
		}
	}

	private void validateBatchHasSufficientUllage(BatchId batchId, double amount) {
		if (!Double.isFinite(batchRecords.get(batchId).amount + amount)) {
			throw new ContractException(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION);
		}
	}

	private void validateBatchId(final BatchId batchId) {

		if (batchId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_ID);
		}

		if (!batchExists(batchId)) {
			throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId);
		}
	}

	/*
	 * Preconditions : batch must exist
	 */
	private void validateBatchIsNotOnOfferedStage(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord != null) {
			if (batchRecord.stageRecord.offered) {
				throw new ContractException(MaterialsError.OFFERED_STAGE_UNALTERABLE);
			}
		}
	}

	private void validateBatchIsNotStaged(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord != null) {
			throw new ContractException(MaterialsError.BATCH_ALREADY_STAGED);
		}
	}

	private void validateBatchIsStaged(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord == null) {
			throw new ContractException(MaterialsError.BATCH_NOT_STAGED);
		}
	}

	private void validateBatchPropertyDefinition(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
	}

	private void validateBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		if (map == null || !map.containsKey(batchPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, batchPropertyId);
		}

	}

	private void validateBatchPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	private void validateDifferentBatchesForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		if (sourceBatchId.equals(destinationBatchId)) {
			throw new ContractException(MaterialsError.REFLEXIVE_BATCH_SHIFT);
		}
	}

	private void validateMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (!materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private void validateMaterialProducerPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_VALUE);
		}
	}

	/*
	 * Preconditions: The batches must exist
	 */
	private void validateMaterialsMatchForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialId sourceMaterialId = batchRecords.get(sourceBatchId).materialId;
		final MaterialId destinationMaterialId = batchRecords.get(destinationBatchId).materialId;

		if (!sourceMaterialId.equals(destinationMaterialId)) {
			throw new ContractException(MaterialsError.MATERIAL_TYPE_MISMATCH);
		}

	}

	private void validateMaterialsProducerHasSufficientResource(final MaterialsProducerId materialsProducerId,
			final ResourceId resourceId, final long amount) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		MutableLong mutableLong = materialsProducerRecord.materialProducerResources.get(resourceId);
		if (mutableLong.getValue() < amount) {
			throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private void validateMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsProducerMap.containsKey(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateMaterialsProducerPropertyDefinitionInitializationNotNull(
			MaterialsProducerPropertyDefinitionInitialization materialsProducerPropertyDefinitionInitialization) {
		if (materialsProducerPropertyDefinitionInitialization == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_DEFINITION_INITIALIZATION);
		}
	}

	private void validateMaterialsProducerPropertyId(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		if (!materialsProducerPropertyDefinitions.keySet().contains(materialsProducerPropertyId)) {
			throw new ContractException(PropertyError.UNKNOWN_PROPERTY_ID, materialsProducerPropertyId);
		}
	}

	private void validateMaterialsProducerPropertyIdIsUnknown(MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyDefinitions.keySet().contains(materialsProducerPropertyId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_ID);
		}
	}

	private void validateNewBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}

		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		if (map == null || map.containsKey(batchPropertyId)) {
			throw new ContractException(PropertyError.DUPLICATE_PROPERTY_DEFINITION);
		}

	}

	private void validateNewMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIAL, materialId);
		}
	}

	private void validateNewMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (materialsProducerMap.containsKey(materialsProducerId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateNonnegativeFiniteMaterialAmount(final double amount) {
		if (!Double.isFinite(amount)) {
			throw new ContractException(MaterialsError.NON_FINITE_MATERIAL_AMOUNT);
		}
		if (amount < 0) {
			throw new ContractException(MaterialsError.NEGATIVE_MATERIAL_AMOUNT);
		}
	}

	private void validateNonnegativeResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
		}
	}

	/*
	 * Preconditions : the batches must exist
	 */
	private void validateProducersMatchForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialsProducerId sourceMaterialsProducerId = batchRecords
				.get(sourceBatchId).materialsProducerRecord.materialProducerId;
		final MaterialsProducerId destinationMaterialsProducerId = batchRecords
				.get(destinationBatchId).materialsProducerRecord.materialProducerId;

		if (!sourceMaterialsProducerId.equals(destinationMaterialsProducerId)) {
			throw new ContractException(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS);
		}

	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	private void validateRegionId(final RegionId regionId) {

		if (regionId == null) {
			throw new ContractException(RegionError.NULL_REGION_ID);
		}

		if (!regionsDataManager.regionIdExists(regionId)) {
			throw new ContractException(RegionError.UNKNOWN_REGION_ID, regionId);
		}
	}

	private void validateResourceAdditionValue(final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (final ArithmeticException e) {
			throw new ContractException(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}

		if (!resourcesDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateStageConversionInfoNotNull(final StageConversionInfo stageConversionInfo) {
		if (stageConversionInfo == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_CONVERSION_INFO);
		}
	}

	private void validateStageId(final StageId stageId) {
		if (stageId == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_ID);
		}

		if (!stageRecords.containsKey(stageId)) {
			throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	private void validateStageIsNotOffered(final StageId stageId) {

		final StageRecord stageRecord = stageRecords.get(stageId);

		if (stageRecord.offered) {
			throw new ContractException(MaterialsError.OFFERED_STAGE_UNALTERABLE);
		}
	}

	private void validateStageIsOffered(final StageId stageId) {
		StageRecord stageRecord = stageRecords.get(stageId);
		if (!stageRecord.offered) {
			throw new ContractException(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE);
		}
	}

	private void validateStageNotOwnedByReceivingMaterialsProducer(final StageId stageId,
			final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerId stageProducer = stageRecords.get(stageId).materialsProducerRecord.materialProducerId;
		if (materialsProducerId.equals(stageProducer)) {
			throw new ContractException(MaterialsError.REFLEXIVE_STAGE_TRANSFER);
		}
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition,
			final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName()
							+ " and does not match definition of " + propertyId);
		}
	}

	private void verifyNonDefaultBatchChecks(MaterialId materialId) {

		boolean missingPropertyAssignments = false;

		boolean[] checkArray = nonDefaultChecksForBatches.get(materialId);

		for (boolean element : checkArray) {
			if (!element) {
				missingPropertyAssignments = true;
				break;
			}
		}

		if (missingPropertyAssignments) {
			StringBuilder sb = new StringBuilder();
			int index = -1;
			boolean firstMember = true;
			Map<BatchPropertyId, Integer> propertyMap = nonDefaultBearingBatchPropertyIds.get(materialId);
			for (BatchPropertyId batchPropertyId : propertyMap.keySet()) {
				index++;
				if (!checkArray[index]) {
					if (firstMember) {
						firstMember = false;
					} else {
						sb.append(", ");
					}
					sb.append(batchPropertyId);
				}
			}
			throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, sb.toString());
		}
	}

	private void verifyNonDefaultChecksForProducers() {

		boolean missingPropertyAssignments = false;

		for (boolean nonDefaultChecksForProducer : nonDefaultChecksForProducers) {
			if (!nonDefaultChecksForProducer) {
				missingPropertyAssignments = true;
				break;
			}
		}

		if (missingPropertyAssignments) {
			StringBuilder sb = new StringBuilder();
			int index = -1;
			boolean firstMember = true;
			for (MaterialsProducerPropertyId materialsProducerPropertyId : nonDefaultBearingProducerPropertyIds
					.keySet()) {
				index++;
				if (!nonDefaultChecksForProducers[index]) {
					if (firstMember) {
						firstMember = false;
					} else {
						sb.append(", ");
					}
					sb.append(materialsProducerPropertyId);
				}
			}
			throw new ContractException(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, sb.toString());
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MaterialsDataManager [batchPropertyDefinitions=");
		builder.append(batchPropertyDefinitions);
		builder.append(", nextBatchRecordId=");
		builder.append(nextBatchRecordId);
		builder.append(", nextStageRecordId=");
		builder.append(nextStageRecordId);
		builder.append(", materialIds=");
		builder.append(materialIds);
		builder.append(", materialsProducerPropertyDefinitions=");
		builder.append(materialsProducerPropertyDefinitions);
		builder.append(", materialsProducerMap=");
		builder.append(materialsProducerMap);
		builder.append(", materialsProducerPropertyMap=");
		builder.append(materialsProducerPropertyMap);
		builder.append(", batchPropertyMap=");
		builder.append(batchPropertyMap);
		builder.append(", batchRecords=");
		builder.append(batchRecords);
		builder.append(", resourceIds=");
		builder.append(resourceIds);
		builder.append(", stageRecords=");
		builder.append(stageRecords);
		builder.append("]");
		return builder.toString();
		
	}

}
