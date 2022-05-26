package plugins.materials.datamangers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.math3.util.FastMath;

import nucleus.DataManager;
import nucleus.DataManagerContext;
import nucleus.NucleusError;
import nucleus.SimulationContext;
import plugins.materials.MaterialsPluginData;
import plugins.materials.events.BatchAdditionEvent;
import plugins.materials.events.BatchAmountUpdateEvent;
import plugins.materials.events.BatchImminentRemovalEvent;
import plugins.materials.events.BatchPropertyDefinitionEvent;
import plugins.materials.events.BatchPropertyUpdateEvent;
import plugins.materials.events.MaterialIdAdditionEvent;
import plugins.materials.events.MaterialsProducerAdditionEvent;
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
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
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
import plugins.util.properties.PropertyValueRecord;
import util.errors.ContractException;

/**
 * General manager for all material activities.
 *
 * @author Shawn Hatch
 *
 */
public final class MaterialsDataManager extends DataManager {

	/*
	 * Represents the batch
	 */
	private static class BatchRecord {

		private final BatchId batchId;
		/*
		 * The stage on which this batch is staged -- may be null
		 */
		private StageRecord stageRecord;
		/*
		 * The non-negative amount of this batch
		 */
		private double amount;
		/*
		 * The time when this batch was created
		 */
		private double creationTime;
		/*
		 * The non-null material for this batch
		 */
		private MaterialId materialId;
		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;

		private BatchRecord(final int index) {
			batchId = new BatchId(index);
		}
	}

	private static class ComponentResourceRecord {
		private final SimulationContext simulationContext;

		private long amount;

		private double assignmentTime;

		public ComponentResourceRecord(final SimulationContext simulationContext) {
			this.simulationContext = simulationContext;
			assignmentTime = simulationContext.getTime();
		}

		public void decrementAmount(final long amount) {
			if (amount < 0) {
				throw new RuntimeException("negative amount");
			}

			if (this.amount < amount) {
				throw new RuntimeException("cannot decrement to a negative level");
			}
			this.amount = Math.subtractExact(this.amount, amount);
			assignmentTime = simulationContext.getTime();
		}

		public long getAmount() {
			return amount;
		}

		public double getAssignmentTime() {
			return assignmentTime;
		}

		public void incrementAmount(final long amount) {
			if (amount < 0) {
				throw new RuntimeException("negative amount");
			}
			this.amount = Math.addExact(this.amount, amount);
			assignmentTime = simulationContext.getTime();
		}

	}

	/*
	 * Represents the materials producer
	 */
	private static class MaterialsProducerRecord {
		private final Map<ResourceId, ComponentResourceRecord> materialProducerResources = new LinkedHashMap<>();
		/*
		 * Identifier for the materials producer
		 */
		private MaterialsProducerId materialProducerId;

		/*
		 * Those batches owned by this materials producer that are not staged
		 */
		private final Set<BatchRecord> inventory = new LinkedHashSet<>();

		/*
		 * Those batches owned by this materials producer that are staged
		 */
		private final Set<StageRecord> stageRecords = new LinkedHashSet<>();

	}

	/*
	 * Represents the stage
	 */
	private static class StageRecord {
		/*
		 * The owning material producer
		 */
		private MaterialsProducerRecord materialsProducerRecord;

		private final StageId stageId;
		/*
		 * Flag marking that the stage has been offered up to other components.
		 * While true, this stage and its batches are immutable.
		 */
		private boolean offered;
		/*
		 * The set of batches that are staged on this stage
		 */
		private final Set<BatchRecord> batchRecords = new LinkedHashSet<>();

		private StageRecord(final int index) {
			stageId = new StageId(index);
		}
	}

	private boolean allProducerPropertyDefinitionsHaveDefaultValues = true;

	private final Set<MaterialsProducerPropertyId> materialsProducerPropertyIds = new LinkedHashSet<>();

	private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, PropertyValueRecord>> materialsProducerPropertyMap = new LinkedHashMap<>();

	private final Map<BatchId, Map<BatchPropertyId, PropertyValueRecord>> batchPropertyMap = new LinkedHashMap<>();

	/*
	 * The identifier for the next created batch
	 */
	private int nextBatchRecordId;

	/*
	 * The identifier for the next created stage
	 */
	private int nextStageRecordId;

	private final Map<BatchId, BatchRecord> batchRecords = new LinkedHashMap<>();

	/*
	 * <stage id, stage record>
	 */
	private final Map<StageId, StageRecord> stageRecords = new LinkedHashMap<>();

	/*
	 * <Materials Producer id, Materials Producer Record>
	 */
	private final Map<MaterialsProducerId, MaterialsProducerRecord> materialsProducerMap = new LinkedHashMap<>();

	private final Map<MaterialsProducerPropertyId, PropertyDefinition> materialsProducerPropertyDefinitions = new LinkedHashMap<>();

	private final Map<MaterialId, Map<BatchPropertyId, PropertyDefinition>> batchPropertyDefinitions = new LinkedHashMap<>();

	private final Map<MaterialId, Set<BatchPropertyId>> batchPropertyIdMap = new LinkedHashMap<>();

	private final Set<MaterialId> materialIds = new LinkedHashSet<>();

	private final Set<ResourceId> resourceIds = new LinkedHashSet<>();

	private DataManagerContext dataManagerContext;

	/**
	 * Constructs the data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PLUGIN_DATA} if
	 *             the material plugin data is null</li>
	 */
	public MaterialsDataManager(MaterialsPluginData materialsPluginData) {
		if (materialsPluginData == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PLUGIN_DATA);
		}
		this.materialsPluginData = materialsPluginData;
	}

	private final MaterialsPluginData materialsPluginData;
	private ResourcesDataManager resourcesDataManager;
	private RegionsDataManager regionsDataManager;

	@Override
	public void init(DataManagerContext dataManagerContext) {
		super.init(dataManagerContext);
		if (dataManagerContext == null) {
			throw new ContractException(NucleusError.NULL_SIMULATION_CONTEXT);
		}
		resourcesDataManager = dataManagerContext.getDataManager(ResourcesDataManager.class);
		regionsDataManager = dataManagerContext.getDataManager(RegionsDataManager.class);

		this.dataManagerContext = dataManagerContext;

		dataManagerContext.addEventLabeler(StageOfferUpdateEvent.getEventLabelerForStage());
		dataManagerContext.addEventLabeler(StageMaterialsProducerUpdateEvent.getEventLabelerForDestination());
		dataManagerContext.addEventLabeler(StageMaterialsProducerUpdateEvent.getEventLabelerForSource());
		dataManagerContext.addEventLabeler(StageMaterialsProducerUpdateEvent.getEventLabelerForStage());
		dataManagerContext.addEventLabeler(MaterialsProducerPropertyUpdateEvent.getEventLabelerForMaterialsProducerAndProperty());
		dataManagerContext.addEventLabeler(MaterialsProducerResourceUpdateEvent.getEventLabelerForMaterialsProducerAndResource());
		dataManagerContext.addEventLabeler(MaterialsProducerResourceUpdateEvent.getEventLabelerForResource());

		for (final MaterialId materialId : materialsPluginData.getMaterialIds()) {
			materialIds.add(materialId);
			batchPropertyIdMap.put(materialId, new LinkedHashSet<>());
			batchPropertyDefinitions.put(materialId, new LinkedHashMap<>());
			for (final BatchPropertyId batchPropertyId : materialsPluginData.getBatchPropertyIds(materialId)) {
				final PropertyDefinition propertyDefinition = materialsPluginData.getBatchPropertyDefinition(materialId, batchPropertyId);
				batchPropertyIdMap.get(materialId).add(batchPropertyId);
				final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
				map.put(batchPropertyId, propertyDefinition);
			}
		}

		for (ResourceId resourceId : resourcesDataManager.getResourceIds()) {
			resourceIds.add(resourceId);
		}
		for (ResourceId resourceId : materialsPluginData.getResourceIds()) {
			if (!resourceIds.contains(resourceId)) {
				throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId + " in resource levels of materials producers");
			}
		}

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
			materialsProducerRecord.materialProducerId = materialsProducerId;
			for (final ResourceId resourceId : resourceIds) {
				materialsProducerRecord.materialProducerResources.put(resourceId, new ComponentResourceRecord(dataManagerContext));
			}
			materialsProducerMap.put(materialsProducerId, materialsProducerRecord);
			materialsProducerPropertyMap.put(materialsProducerId, new LinkedHashMap<>());
		}

		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
			PropertyDefinition propertyDefinition = materialsPluginData.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
			allProducerPropertyDefinitionsHaveDefaultValues &= propertyDefinition.getDefaultValue().isPresent();
			materialsProducerPropertyIds.add(materialsProducerPropertyId);
			for (final MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
				final Map<MaterialsProducerPropertyId, PropertyValueRecord> map = materialsProducerPropertyMap.get(materialsProducerId);
				final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				map.put(materialsProducerPropertyId, propertyValueRecord);
			}
			materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
		}

		/*
		 * Load the remaining data from the scenario that generally corresponds
		 * to mutations available to components so that reporting will properly
		 * reflect these data. The adding of resources directly to people and
		 * material producers is covered here but do not correspond to mutations
		 * allowed to components.
		 */

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsPluginData.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsPluginData.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).setPropertyValue(materialsProducerPropertyValue);
			}
		}

		nextStageRecordId = -1;
		for (final StageId stageId : materialsPluginData.getStageIds()) {
			final MaterialsProducerId materialsProducerId = materialsPluginData.getStageMaterialsProducer(stageId);
			final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
			final StageRecord stageRecord = new StageRecord(stageId.getValue());
			nextStageRecordId = FastMath.max(nextStageRecordId, stageRecord.stageId.getValue());
			stageRecord.materialsProducerRecord = materialsProducerRecord;
			stageRecord.offered = materialsPluginData.isStageOffered(stageId);
			materialsProducerRecord.stageRecords.add(stageRecord);
			stageRecords.put(stageRecord.stageId, stageRecord);
		}
		nextStageRecordId++;

		nextBatchRecordId = -1;
		for (final BatchId batchId : materialsPluginData.getBatchIds()) {
			final MaterialsProducerId materialsProducerId = materialsPluginData.getBatchMaterialsProducer(batchId);
			final MaterialId materialId = materialsPluginData.getBatchMaterial(batchId);
			final double amount = materialsPluginData.getBatchAmount(batchId);
			final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
			final BatchRecord batchRecord = new BatchRecord(batchId.getValue());
			nextBatchRecordId = FastMath.max(nextBatchRecordId, batchRecord.batchId.getValue());
			batchRecord.amount = amount;
			batchRecord.creationTime = dataManagerContext.getTime();
			batchRecord.materialId = materialId;
			batchRecord.materialsProducerRecord = materialsProducerRecord;
			materialsProducerRecord.inventory.add(batchRecord);
			batchRecords.put(batchRecord.batchId, batchRecord);
			final Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
			final Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);
			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				final PropertyDefinition propertyDefinition = getBatchPropertyDefinition(materialId, batchPropertyId);
				final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue());
				map.put(batchPropertyId, propertyValueRecord);
			}
			batchPropertyMap.put(batchRecord.batchId, map);
		}
		nextBatchRecordId++;

		for (final StageId stageId : materialsPluginData.getStageIds()) {
			final Set<BatchId> batches = materialsPluginData.getStageBatches(stageId);
			for (final BatchId batchId : batches) {
				final BatchRecord batchRecord = batchRecords.get(batchId);
				if (batchRecord.stageRecord != null) {
					throw new ContractException(MaterialsError.BATCH_ALREADY_STAGED);
				}
				final StageRecord stageRecord = stageRecords.get(stageId);

				batchRecord.stageRecord = stageRecord;
				stageRecord.batchRecords.add(batchRecord);
				batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
			}
		}

		for (final BatchId batchId : materialsPluginData.getBatchIds()) {
			final MaterialId materialId = materialsPluginData.getBatchMaterial(batchId);
			final Set<BatchPropertyId> batchPropertyIds = materialsPluginData.getBatchPropertyIds(materialId);
			for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
				final Object batchPropertyValue = materialsPluginData.getBatchPropertyValue(batchId, batchPropertyId);
				final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
				PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
				if (propertyValueRecord == null) {
					propertyValueRecord = new PropertyValueRecord(dataManagerContext);
					map.put(batchPropertyId, propertyValueRecord);
				}
				propertyValueRecord.setPropertyValue(batchPropertyValue);
			}
		}

		for (final MaterialsProducerId materialsProducerId : materialsPluginData.getMaterialsProducerIds()) {
			for (final ResourceId resourceId : resourceIds) {
				final Long amount = materialsPluginData.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
				if (amount != null) {
					final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
					final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
					componentResourceRecord.incrementAmount(amount);
				}
			}
		}
		dataManagerContext.subscribe(ResourceIdAdditionEvent.class, this::handleResourceIdAdditionEvent);
	}

	private void handleResourceIdAdditionEvent(DataManagerContext dataManagerContext, ResourceIdAdditionEvent resourceIdAdditionEvent) {
		ResourceId resourceId = resourceIdAdditionEvent.getResourceId();
		if (resourceId == null || resourceIds.contains(resourceId)) {
			return;
		}

		resourceIds.add(resourceId);

		for (MaterialsProducerRecord materialsProducerRecord : materialsProducerMap.values()) {
			materialsProducerRecord.materialProducerResources.put(resourceId, new ComponentResourceRecord(dataManagerContext));
		}
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

	/**
	 * Returns the amount of material in the batch.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 */
	public double getBatchAmount(final BatchId batchId) {
		validateBatchId(batchId);
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.amount;
	}

	private void validateBatchId(final BatchId batchId) {

		if (batchId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_ID);
		}

		if (!batchExists(batchId)) {
			throw new ContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId);
		}
	}

	/**
	 * Returns the type of material in the batch.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
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
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
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
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown</li>
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		validateMaterialId(materialId);
		validateBatchPropertyId(materialId, batchPropertyId);
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		return map.get(batchPropertyId);
	}

	private void validateNewBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}

		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		if (map == null || map.containsKey(batchPropertyId)) {
			throw new ContractException(MaterialsError.DUPLICATE_BATCH_PROPERTY_DEFINITION);
		}

	}

	private void validateBatchPropertyDefinition(PropertyDefinition propertyDefinition) {
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}
		if (propertyDefinition.getDefaultValue().isEmpty()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
	}

	/**
	 * Defines a new batch property
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_BATCH_PROPERTY_DEFINITION}
	 *             if the batch property id is already present</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not have a default value</li>
	 */
	public void defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, PropertyDefinition propertyDefinition) {
		validateMaterialId(materialId);
		validateNewBatchPropertyId(materialId, batchPropertyId);
		validateBatchPropertyDefinition(propertyDefinition);
		batchPropertyIdMap.get(materialId).add(batchPropertyId);
		batchPropertyDefinitions.get(materialId).put(batchPropertyId, propertyDefinition);

		for (BatchId batchId : batchRecords.keySet()) {
			BatchRecord batchRecord = batchRecords.get(batchId);
			if (batchRecord.materialId.equals(materialId)) {
				Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
				PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
				propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
				map.put(batchPropertyId, propertyValueRecord);
			}
		}
		dataManagerContext.releaseEvent(new BatchPropertyDefinitionEvent(materialId, batchPropertyId));
	}

	private void validateMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (!materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private void validateBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}

		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		if (map == null || !map.containsKey(batchPropertyId)) {
			throw new ContractException(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID);
		}

	}

	/**
	 * Returns the {@link BatchPropertyId} values for the given
	 * {@link MaterialId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             materials id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID
	 *             S_PRODUCER_ID} if the materials id is unknown</li>
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
	 * Returns the time when the of the batch property was last assigned. It is
	 * the caller's responsibility to validate the inputs.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown</li>
	 */
	public double getBatchPropertyTime(BatchId batchId, BatchPropertyId batchPropertyId) {
		validateBatchId(batchId);
		final MaterialId materialId = getBatchMaterial(batchId);
		validateBatchPropertyId(materialId, batchPropertyId);
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		final PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		return propertyValueRecord.getAssignmentTime();
	}

	/**
	 * Returns the value of the batch property. It is the caller's
	 * responsibility to validate the inputs.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown</li>
	 */

	@SuppressWarnings("unchecked")
	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId) {
		validateBatchId(batchId);
		final MaterialId batchMaterial = batchRecords.get(batchId).materialId;
		validateBatchPropertyId(batchMaterial, batchPropertyId);
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		final PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		return (T) propertyValueRecord.getValue();
	}

	/**
	 * Returns the stage id the batch. Returns null if the batch is not in a
	 * stage.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
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
	 * Returns the creation time for the batch.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 * 
	 */
	public double getBatchTime(final BatchId batchId) {
		validateBatchId(batchId);
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.creationTime;
	}

	/**
	 * Returns as a list the set of batch ids matching the materials producer
	 * where the batches are not staged.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
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

	private void validateMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsProducerMap.containsKey(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	/**
	 * Returns as a list the set of batch ids matching the materials producer
	 * and material id where the batches are not staged.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId) {
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

	private void validateNewMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (materialsProducerMap.containsKey(materialsProducerId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateAllMaterialsProducerPropertiesHaveDefaultValues() {
		if (!allProducerPropertyDefinitionsHaveDefaultValues) {
			throw new ContractException(MaterialsError.MATERIALS_PRODUCER_ADDITION_BLOCKED);
		}
	}

	/**
	 * Add a material producer
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is already present</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_PRODUCER_ADDITION_BLOCKED}
	 *             if any of the the materials producer properties does not have
	 *             a default value</li>
	 */
	public void addMaterialsProducer(MaterialsProducerId materialsProducerId) {
		validateNewMaterialsProducerId(materialsProducerId);
		validateAllMaterialsProducerPropertiesHaveDefaultValues();

		// integrate the new producer into the resources
		final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
		materialsProducerRecord.materialProducerId = materialsProducerId;
		for (final ResourceId resourceId : resourceIds) {
			materialsProducerRecord.materialProducerResources.put(resourceId, new ComponentResourceRecord(dataManagerContext));
		}
		materialsProducerMap.put(materialsProducerId, materialsProducerRecord);

		// integrate the new producer into the property values
		Map<MaterialsProducerPropertyId, PropertyValueRecord> propertyValueMap = new LinkedHashMap<>();
		materialsProducerPropertyMap.put(materialsProducerId, propertyValueMap);
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
			PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
			Object defaultValue = propertyDefinition.getDefaultValue().get();
			PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(defaultValue);
			propertyValueMap.put(materialsProducerPropertyId, propertyValueRecord);
		}

		// release notification of the materials producer addition
		dataManagerContext.releaseEvent(new MaterialsProducerAdditionEvent(materialsProducerId));
	}

	/**
	 * Returns the property definition for the given
	 * {@link MaterialsProducerPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown</li>
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
	}

	private void validateMaterialsProducerPropertyId(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}

		if (!materialsProducerPropertyIds.contains(materialsProducerPropertyId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
		}
	}

	/**
	 * Returns the set materials producer property ids
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		final Set<T> result = new LinkedHashSet<>(materialsProducerPropertyDefinitions.keySet().size());
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsProducerPropertyDefinitions.keySet()) {
			result.add((T) materialsProducerPropertyId);
		}
		return result;
	}

	/**
	 * Returns the time when the of the materials producer property was last
	 * assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown</li>
	 */

	public double getMaterialsProducerPropertyTime(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getAssignmentTime();
	}

	/**
	 * Returns the value of the materials producer property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown</li>
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return (T) materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getValue();
	}

	/**
	 * Returns the materials producer resource level.
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public long getMaterialsProducerResourceLevel(MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		validateMaterialsProducerId(materialsProducerId);
		validateResourceId(resourceId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		return componentResourceRecord.getAmount();
	}

	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}

		if (!resourcesDataManager.resourceIdExists(resourceId)) {
			throw new ContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	/**
	 * Returns the simulation time when the materials producer resource level
	 * was last assigned.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 */
	public double getMaterialsProducerResourceTime(MaterialsProducerId materialsProducerId, ResourceId resourceId) {
		validateMaterialsProducerId(materialsProducerId);
		validateResourceId(resourceId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		return componentResourceRecord.getAssignmentTime();
	}

	/**
	 * Returns as a list the set of stage ids owned by the material producer
	 * where the stage is being offered.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
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
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
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

	private void validateStageId(final StageId stageId) {
		if (stageId == null) {
			throw new ContractException(MaterialsError.NULL_STAGE_ID);
		}

		if (!stageRecords.containsKey(stageId)) {
			throw new ContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

	/**
	 * Returns as a list the set of batch ids matching the stage and material
	 * type.
	 * 
	 *
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
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
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
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
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
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

	/**
	 * Returns true if and only if the stage is offered.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 */
	public boolean isStageOffered(final StageId stageId) {
		validateStageId(stageId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		return stageRecord.offered;
	}

	/**
	 * Returns true if and only if the material exists. Tolerates null.
	 */
	public boolean materialIdExists(final MaterialId materialId) {
		return (materialIds.contains(materialId));
	}

	/**
	 * Returns true if and only if the material producer id exists. Null
	 * tolerant.
	 */
	public boolean materialsProducerIdExists(final MaterialsProducerId materialsProducerId) {
		return materialsProducerMap.containsKey(materialsProducerId);
	}

	/**
	 * Returns true if and only if the material producer property id exists.
	 * Null tolerant.
	 */
	public boolean materialsProducerPropertyIdExists(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyIds.contains(materialsProducerPropertyId);
	}

	/**
	 * Returns true if and only if the stage exists. Null tolerant.
	 */
	public boolean stageExists(final StageId stageId) {
		return stageRecords.containsKey(stageId);
	}

	/**
	 * Creates a batch from the {@linkplain BatchConstructionInfo} contained in
	 * the event. Sets batch properties found in the batch construction info.
	 * Generates a corresponding {@linkplain BatchAdditionEvent} event
	 * 
	 * @throws ContractException
	 *
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_CONSTRUCTION_INFO}
	 *             if the batch construction info in the event is null</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id in the batch construction info is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id in the batch construction info is unknown</li>
	 *             <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the amount in the batch construction info is not finite</li>
	 *             <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if
	 *             the amount in the batch construction info is negative</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch construction info contains a null batch property
	 *             id</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch construction info contains an unknown batch
	 *             property id</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_VALUE} if
	 *             the batch construction info contains a null batch property
	 *             value</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             batch construction info contains a batch property value that
	 *             is incompatible with the corresponding property def</li>
	 *
	 * 
	 */
	public BatchId addBatch(BatchConstructionInfo batchConstructionInfo) {
		validateBatchConstructionInfoNotNull(batchConstructionInfo);
		final MaterialId materialId = batchConstructionInfo.getMaterialId();
		MaterialsProducerId materialsProducerId = batchConstructionInfo.getMaterialsProducerId();
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialId(materialId);
		final double amount = batchConstructionInfo.getAmount();
		validateNonnegativeFiniteMaterialAmount(amount);
		final Map<BatchPropertyId, Object> propertyValues = batchConstructionInfo.getPropertyValues();
		for (final BatchPropertyId batchPropertyId : propertyValues.keySet()) {
			validateBatchPropertyId(materialId, batchPropertyId);
			final Object batchPropertyValue = propertyValues.get(batchPropertyId);
			final PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
			validateBatchPropertyValueNotNull(batchPropertyValue);
			validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
		}
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final BatchRecord batchRecord = new BatchRecord(nextBatchRecordId++);
		batchRecord.amount = amount;
		batchRecord.creationTime = dataManagerContext.getTime();
		batchRecord.materialId = materialId;
		batchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(batchRecord);
		batchRecords.put(batchRecord.batchId, batchRecord);

		final Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		final Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);
		for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
			final PropertyDefinition propertyDefinition = getBatchPropertyDefinition(materialId, batchPropertyId);
			final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			Object batchPropertyValue = propertyValues.get(batchPropertyId);
			if (batchPropertyValue == null) {
				batchPropertyValue = propertyDefinition.getDefaultValue();
			}
			propertyValueRecord.setPropertyValue(batchPropertyValue);
			map.put(batchPropertyId, propertyValueRecord);
		}
		batchPropertyMap.put(batchRecord.batchId, map);
		BatchId result = batchRecord.batchId;
		dataManagerContext.releaseEvent(new BatchAdditionEvent(batchRecord.batchId));
		return result;
	}

	private void validateValueCompatibility(final Object propertyId, final PropertyDefinition propertyDefinition, final Object propertyValue) {
		if (!propertyDefinition.getType().isAssignableFrom(propertyValue.getClass())) {
			throw new ContractException(PropertyError.INCOMPATIBLE_VALUE,
					"Property value " + propertyValue + " is not of type " + propertyDefinition.getType().getName() + " and does not match definition of " + propertyId);
		}
	}

	private void validateBatchPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_PROPERTY_VALUE);
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

	private void validateBatchConstructionInfoNotNull(final BatchConstructionInfo batchConstructionInfo) {
		if (batchConstructionInfo == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_CONSTRUCTION_INFO);
		}
	}

	/**
	 * Creates a batch. Generates a corresponding
	 * {@linkplain BatchAdditionEvent} event
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id in the batch construction info is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id in the batch construction info is unknown</li>
	 *             <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the amount in the batch construction info is not finite</li>
	 *             <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if
	 *             the amount in the batch construction info is negative</li>
	 * 
	 */
	public BatchId addBatch(MaterialsProducerId materialsProducerId, MaterialId materialId, double amount) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialId(materialId);
		validateNonnegativeFiniteMaterialAmount(amount);

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final BatchRecord batchRecord = new BatchRecord(nextBatchRecordId++);
		batchRecord.amount = amount;
		batchRecord.creationTime = dataManagerContext.getTime();
		batchRecord.materialId = materialId;
		batchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(batchRecord);
		batchRecords.put(batchRecord.batchId, batchRecord);

		final Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		final Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);
		for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
			final PropertyDefinition propertyDefinition = getBatchPropertyDefinition(materialId, batchPropertyId);
			final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue());
			map.put(batchPropertyId, propertyValueRecord);
		}
		batchPropertyMap.put(batchRecord.batchId, map);

		BatchId batchId = batchRecord.batchId;
		dataManagerContext.releaseEvent(new BatchAdditionEvent(batchId));
		return batchId;
	}

	/**
	 * Transfers an amount of material from one batch to another batch of the
	 * same material type and material producer. Generates a corresponding
	 * {@linkplain BatchAmountUpdateEvent} for each batch.
	 * 
	 * @throws ContractException
	 *
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the source
	 *             batch id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the
	 *             source batch id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the
	 *             destination batch id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the
	 *             destination batch id is unknown</li>
	 *             <li>{@linkplain MaterialsError#REFLEXIVE_BATCH_SHIFT} if the
	 *             source and destination batches are the same</li>
	 *             <li>{@linkplain MaterialsError#MATERIAL_TYPE_MISMATCH} if the
	 *             batches do not have the same material type</li>
	 *             <li>{@linkplain MaterialsError#BATCH_SHIFT_WITH_MULTIPLE_OWNERS}
	 *             if the batches have different owning materials producers</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the source batch is on a stage is offered</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the destination batch is on a stage is offered</li>
	 *             <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the shift amount is not a finite number</li>
	 *             <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if
	 *             the shift amount is negative</li>
	 *             <li>{@linkplain MaterialsError#INSUFFICIENT_MATERIAL_AVAILABLE}
	 *             if the shift amount exceeds the available material on the
	 *             source batch</li>
	 *             <li>{@linkplain MaterialsError#MATERIAL_ARITHMETIC_EXCEPTION}
	 *             if the shift amount would cause an overflow on the
	 *             destination batch</li>
	 * 
	 * 
	 *             </blockquote></li>
	 * 
	 */

	public void transferMaterialBetweenBatches(BatchId sourceBatchId, BatchId destinationBatchId, double amount) {
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

		double previousSourceAmount = sourceBatchRecord.amount;
		double previousDestinationAmount = destinationBatchRecord.amount;

		sourceBatchRecord.amount -= amount;
		destinationBatchRecord.amount += amount;

		double currentSourceAmount = sourceBatchRecord.amount;
		double currentDestinationAmount = destinationBatchRecord.amount;

		dataManagerContext.releaseEvent(new BatchAmountUpdateEvent(sourceBatchId, previousSourceAmount, currentSourceAmount));
		dataManagerContext.releaseEvent(new BatchAmountUpdateEvent(destinationBatchId, previousDestinationAmount, currentDestinationAmount));
	}

	private void validateBatchHasSufficientUllage(BatchId batchId, double amount) {
		if (!Double.isFinite(batchRecords.get(batchId).amount + amount)) {
			throw new ContractException(MaterialsError.MATERIAL_ARITHMETIC_EXCEPTION);
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

	/*
	 * Preconditions : the batches must exist
	 */
	private void validateProducersMatchForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		final MaterialsProducerId sourceMaterialsProducerId = batchRecords.get(sourceBatchId).materialsProducerRecord.materialProducerId;
		final MaterialsProducerId destinationMaterialsProducerId = batchRecords.get(destinationBatchId).materialsProducerRecord.materialProducerId;

		if (!sourceMaterialsProducerId.equals(destinationMaterialsProducerId)) {
			throw new ContractException(MaterialsError.BATCH_SHIFT_WITH_MULTIPLE_OWNERS);
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

	private void validateDifferentBatchesForShift(final BatchId sourceBatchId, final BatchId destinationBatchId) {
		if (sourceBatchId.equals(destinationBatchId)) {
			throw new ContractException(MaterialsError.REFLEXIVE_BATCH_SHIFT);
		}
	}

	/**
	 * Removes the given batch. Generates a corresponding
	 * {@linkplain BatchImminentRemovalEvent}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the batch is on an offered stage</li>
	 * 
	 * 
	 * 
	 */
	public void removeBatch(BatchId batchId) {
		validateBatchId(batchId);
		validateBatchIsNotOnOfferedStage(batchId);
		dataManagerContext.addPlan((context) -> destroyBatch(batchId), dataManagerContext.getTime());
		dataManagerContext.releaseEvent(new BatchImminentRemovalEvent(batchId));
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
	 * 
	 * Set a batch's property value. Generates a corresponding
	 * {@linkplain BatchPropertyUpdateEvent}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown</li>
	 *             <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if batch
	 *             property is not mutable</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_VALUE} if
	 *             the batch property value is null</li>
	 *             <li>{@linkplain PropertyError#INCOMPATIBLE_VALUE} if the
	 *             batch property value is not compatible with the corresponding
	 *             property definition</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the batch in on an offered stage</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the
	 *             requesting agent is not the owning materials producer</li>
	 * 
	 * 
	 * 
	 */
	public void setBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId, Object batchPropertyValue) {
		validateBatchId(batchId);
		BatchRecord batchRecord = batchRecords.get(batchId);
		final MaterialId materialId = batchRecord.materialId;
		validateBatchPropertyId(materialId, batchPropertyId);
		final PropertyDefinition propertyDefinition = batchPropertyDefinitions.get(materialId).get(batchPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateBatchPropertyValueNotNull(batchPropertyValue);
		validateValueCompatibility(batchPropertyId, propertyDefinition, batchPropertyValue);
		validateBatchIsNotOnOfferedStage(batchId);
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		final PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		Object previousPropertyValue = propertyValueRecord.getValue();
		propertyValueRecord.setPropertyValue(batchPropertyValue);
		dataManagerContext.releaseEvent(new BatchPropertyUpdateEvent(batchId, batchPropertyId, previousPropertyValue, batchPropertyValue));
	}

	private void validatePropertyMutability(final PropertyDefinition propertyDefinition) {
		if (!propertyDefinition.propertyValuesAreMutable()) {
			throw new ContractException(PropertyError.IMMUTABLE_VALUE);
		}
	}

	/**
	 * Assigns a property value to a materials producer. Generates a
	 * corresponding {@linkplain MaterialsProducerPropertyUpdateEvent}
	 * 
	 * @throws ContractException
	 *
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is unknown</li>
	 *             <li>{@linkplain PropertyError#IMMUTABLE_VALUE} if the
	 *             materials producer property is immutable</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_VALUE}
	 *             if the property value is null</li>
	 *             {@linkplain PropertyError#INCOMPATIBLE_VALUE} if the property
	 *             value is incompatible with the corresponding property
	 *             definition</li>
	 * 
	 * 
	 */
	public void setMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		final PropertyDefinition propertyDefinition = materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
		validatePropertyMutability(propertyDefinition);
		validateMaterialProducerPropertyValueNotNull(materialsProducerPropertyValue);
		validateValueCompatibility(materialsProducerPropertyId, propertyDefinition, materialsProducerPropertyValue);
		PropertyValueRecord propertyValueRecord = materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId);
		Object oldPropertyValue = propertyValueRecord.getValue();
		propertyValueRecord.setPropertyValue(materialsProducerPropertyValue);
		dataManagerContext.releaseEvent(new MaterialsProducerPropertyUpdateEvent(materialsProducerId, materialsProducerPropertyId, oldPropertyValue, materialsProducerPropertyValue));
	}

	private void validateMaterialProducerPropertyValueNotNull(final Object propertyValue) {
		if (propertyValue == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_VALUE);
		}
	}

	/**
	 * <li>Removes a batch from a non-offered stage, placing it into the
	 * materials producer's inventory. Generates a corresponding
	 * {@linkplain StageMembershipRemovalEvent}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the
	 *             requesting agent is not the owning materials producer</li>
	 *             <li>{@linkplain MaterialsError#BATCH_NOT_STAGED} if the batch
	 *             is not staged</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE } if
	 *             the stage containing the batch is offered</li>
	 * 
	 */
	public void moveBatchToInventory(BatchId batchId) {
		validateBatchId(batchId);
		BatchRecord batchRecord = batchRecords.get(batchId);
		validateBatchIsStaged(batchId);
		final StageId stageId = batchRecord.stageRecord.stageId;
		validateStageIsNotOffered(stageId);
		batchRecord.stageRecord.batchRecords.remove(batchRecord);
		batchRecord.stageRecord = null;
		batchRecord.materialsProducerRecord.inventory.add(batchRecord);
		dataManagerContext.releaseEvent(new StageMembershipRemovalEvent(batchId, stageId));
	}

	private void validateStageIsNotOffered(final StageId stageId) {

		final StageRecord stageRecord = stageRecords.get(stageId);

		if (stageRecord.offered) {
			throw new ContractException(MaterialsError.OFFERED_STAGE_UNALTERABLE);
		}
	}

	private void validateBatchIsStaged(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord == null) {
			throw new ContractException(MaterialsError.BATCH_NOT_STAGED);
		}
	}

	/**
	 * Removes a batch frominventory, placing it on a stage. Generates a
	 * corresponding {@linkplain StageMembershipAdditionEvent}
	 * 
	 * @throws ContractException
	 *
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_ID} if the batch id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_ID} if the batch
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#BATCH_ALREADY_STAGED} if the
	 *             batch is already staged</li>
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the stage is offered</li>
	 *             <li>{@linkplain MaterialsError#BATCH_STAGED_TO_DIFFERENT_OWNER}
	 *             if batch and stage do not have the same owning materials
	 *             producer</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the
	 *             requesting agent is not the owning material producer</li>
	 * 
	 * 
	 */
	public void moveBatchToStage(BatchId batchId, StageId stageId) {

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
		dataManagerContext.releaseEvent(new StageMembershipAdditionEvent(batchId, stageId));
	}

	private void validateBatchAndStageOwnersMatch(final BatchId batchId, final StageId stageId) {
		final MaterialsProducerId batchProducerId = batchRecords.get(batchId).materialsProducerRecord.materialProducerId;
		final MaterialsProducerId stageProducerId = stageRecords.get(stageId).materialsProducerRecord.materialProducerId;
		if (!batchProducerId.equals(stageProducerId)) {
			throw new ContractException(MaterialsError.BATCH_STAGED_TO_DIFFERENT_OWNER);
		}
	}

	private void validateBatchIsNotStaged(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord != null) {
			throw new ContractException(MaterialsError.BATCH_ALREADY_STAGED);
		}
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
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID } if the stage
	 *             id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID } if the
	 *             stage id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID }
	 *             if the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID }
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain MaterialsError#UNOFFERED_STAGE_NOT_TRANSFERABLE }
	 *             if the stage is not offered</li>
	 *             <li>{@linkplain MaterialsError#REFLEXIVE_STAGE_TRANSFER } if
	 *             the source and destination materials producers are the
	 *             same</li>
	 * 
	 * 
	 */
	public void transferOfferedStage(StageId stageId, MaterialsProducerId materialsProducerId) {
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
		dataManagerContext.releaseEvent(new StageMaterialsProducerUpdateEvent(stageId, stageProducer, materialsProducerId));
		dataManagerContext.releaseEvent(new StageOfferUpdateEvent(stageId, true, false));
	}

	private void validateStageNotOwnedByReceivingMaterialsProducer(final StageId stageId, final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerId stageProducer = stageRecords.get(stageId).materialsProducerRecord.materialProducerId;
		if (materialsProducerId.equals(stageProducer)) {
			throw new ContractException(MaterialsError.REFLEXIVE_STAGE_TRANSFER);
		}
	}

	private void validateStageIsOffered(final StageId stageId) {
		StageRecord stageRecord = stageRecords.get(stageId);
		if (!stageRecord.offered) {
			throw new ContractException(MaterialsError.UNOFFERED_STAGE_NOT_TRANSFERABLE);
		}
	}

	/**
	 * 
	 * 
	 * Transfers a resource amount from a materials producer to a region.
	 * Generates a corresponding {@linkplain RegionResourceAdditionEvent} and
	 * {@linkplain MaterialsProducerResourceUpdateEvent}
	 * 
	 * @throws ContractException
	 *             *
	 * 
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain RegionError#NULL_REGION_ID} if the region id
	 *             is null</li>
	 *             <li>{@linkplain RegionError#UNKNOWN_REGION_ID} if the region
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the materials amount is negative</li>
	 *             <li>{@linkplain ResourceError#INSUFFICIENT_RESOURCES_AVAILABLE}
	 *             if the materials amount exceeds the resource level of the
	 *             materials producer</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the materials amount would cause an overflow of the
	 *             regions resource level</li>
	 * 
	 * 
	 * 
	 */
	public void transferResourceToRegion(MaterialsProducerId materialsProducerId, ResourceId resourceId, RegionId regionId, long amount) {

		validateResourceId(resourceId);
		validateRegionId(regionId);
		validateMaterialsProducerId(materialsProducerId);
		validateNonnegativeResourceAmount(amount);
		validateMaterialsProducerHasSufficientResource(materialsProducerId, resourceId, amount);
		final long currentResourceLevel = resourcesDataManager.getRegionResourceLevel(regionId, resourceId);
		validateResourceAdditionValue(currentResourceLevel, amount);

		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		long previousMaterialsProducerResourceLevel = componentResourceRecord.getAmount();
		componentResourceRecord.decrementAmount(amount);
		long currentMaterialsProducerResourceLevel = componentResourceRecord.getAmount();
		resourcesDataManager.addResourceToRegion(resourceId, regionId, amount);
		dataManagerContext.releaseEvent(new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId, previousMaterialsProducerResourceLevel, currentMaterialsProducerResourceLevel));
	}

	private void validateResourceAdditionValue(final long currentResourceLevel, final long amount) {
		try {
			Math.addExact(currentResourceLevel, amount);
		} catch (final ArithmeticException e) {
			throw new ContractException(ResourceError.RESOURCE_ARITHMETIC_EXCEPTION);
		}
	}

	private void validateMaterialsProducerHasSufficientResource(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		if (componentResourceRecord.getAmount() < amount) {
			throw new ContractException(ResourceError.INSUFFICIENT_RESOURCES_AVAILABLE);
		}
	}

	private void validateNonnegativeResourceAmount(final long amount) {
		if (amount < 0) {
			throw new ContractException(ResourceError.NEGATIVE_RESOURCE_AMOUNT);
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

	/**
	 * Creates a stage. Generates a corresponding
	 * {@linkplain StageAdditionEvent}
	 * 
	 * @throws ContractException
	 * 
	 *             <li>{@linkplain MaterialsError.NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is unknown</li>
	 * 
	 *             <li>{@linkplain MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 * 
	 */
	public StageId addStage(final MaterialsProducerId materialsProducerId) {
		validateMaterialsProducerId(materialsProducerId);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final StageRecord stageRecord = new StageRecord(nextStageRecordId++);
		stageRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.stageRecords.add(stageRecord);
		stageRecords.put(stageRecord.stageId, stageRecord);
		dataManagerContext.releaseEvent(new StageAdditionEvent(stageRecord.stageId));
		return stageRecord.stageId;
	}

	/**
	 * 
	 * Destroys a non-offered stage and optionally destroys any associated
	 * batches. Generates the corresponding
	 * {@linkplain BatchImminentRemovalEvent} and
	 * {@linkplain StageImminentRemovalEvent} events.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             stage is offered</li>
	 */
	public void removeStage(StageId stageId, boolean destroyBatches) {
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

			for (BatchRecord batchRecord : stageRecord.batchRecords) {
				dataManagerContext.releaseEvent(new BatchImminentRemovalEvent(batchRecord.batchId));
			}
			dataManagerContext.releaseEvent(new StageImminentRemovalEvent(stageId));
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

			dataManagerContext.releaseEvent(new StageImminentRemovalEvent(stageId));
		}
	}

	/**
	 * Sets the offer state of a stage. Generates a corresponding
	 * {@linkplain StageOfferUpdateEvent}
	 * 
	 * @throws ContractException
	 *
	 *
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID } if the stage
	 *             id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID } if the
	 *             stage id is unknown</li>
	 * 
	 */
	public void setStageOfferState(StageId stageId, boolean offer) {
		validateStageId(stageId);
		final StageRecord stageRecord = stageRecords.get(stageId);
		boolean previousState = stageRecord.offered;
		stageRecord.offered = offer;
		dataManagerContext.releaseEvent(new StageOfferUpdateEvent(stageId, previousState, offer));
	}

	/**
	 * 
	 * Converts a non-offered stage, including its associated batches, into a
	 * new batch of the given material. The new batch is placed into inventory.
	 * Generates a corresponding {@linkplain BatchImminentRemovalEvent},
	 * {@linkplain BatchAdditionEvent} and
	 * {@linkplain StageImminentRemovalEvent} events
	 * 
	 * @throws ContractException
	 *
	 * 
	 * 
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if stage id
	 *             is unknown</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the
	 *             requesting agent is not the owning materials producer</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the stage is offered</li>
	 *             <li>{@linkplain MaterialsError#NON_FINITE_MATERIAL_AMOUNT} if
	 *             the material amount is not finite</li>
	 *             <li>{@linkplain MaterialsError#NEGATIVE_MATERIAL_AMOUNT} if
	 *             the material amount is negative</li>
	 * 
	 * 
	 */
	public BatchId convertStageToBatch(StageId stageId, MaterialId materialId, double amount) {

		validateMaterialId(materialId);
		validateStageId(stageId);
		StageRecord stageRecord = stageRecords.get(stageId);
		final MaterialsProducerId materialsProducerId = stageRecord.materialsProducerRecord.materialProducerId;
		validateStageIsNotOffered(stageId);
		validateNonnegativeFiniteMaterialAmount(amount);

		// add the new batch
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final BatchRecord newBatchRecord = new BatchRecord(nextBatchRecordId++);
		newBatchRecord.amount = amount;
		newBatchRecord.creationTime = dataManagerContext.getTime();
		newBatchRecord.materialId = materialId;
		newBatchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(newBatchRecord);
		batchRecords.put(newBatchRecord.batchId, newBatchRecord);

		final Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		final Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);
		for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
			final PropertyDefinition propertyDefinition = getBatchPropertyDefinition(materialId, batchPropertyId);
			final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(dataManagerContext);
			propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue());
			map.put(batchPropertyId, propertyValueRecord);
		}
		batchPropertyMap.put(newBatchRecord.batchId, map);

		BatchId batchId = newBatchRecord.batchId;

		dataManagerContext.addPlan((context) -> {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchPropertyMap.remove(batchRecord.batchId);
				batchRecords.remove(batchRecord.batchId);
			}
			stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
			stageRecords.remove(stageId);

		}, dataManagerContext.getTime());

		dataManagerContext.releaseEvent(new BatchAdditionEvent(batchId));
		for (BatchRecord batchRec : stageRecord.batchRecords) {
			dataManagerContext.releaseEvent(new BatchImminentRemovalEvent(batchRec.batchId));
		}
		dataManagerContext.releaseEvent(new StageImminentRemovalEvent(stageId));
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
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#UNKNOWN_RESOURCE_ID} if the
	 *             resource id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_STAGE_ID} if the stage id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_STAGE_ID} if the stage
	 *             id is unknown</li>
	 *             <li>{@linkplain MaterialsError#OFFERED_STAGE_UNALTERABLE} if
	 *             the stage is offered</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_OWNERSHIP} if the
	 *             requesting agent is not the owning materials producer</li>
	 *             <li>{@linkplain ResourceError#NEGATIVE_RESOURCE_AMOUNT} if
	 *             the the resource amount is negative</li>
	 *             <li>{@linkplain ResourceError#RESOURCE_ARITHMETIC_EXCEPTION}
	 *             if the resource amount would cause an overflow of the
	 *             materials producer's resource level</li>
	 * 
	 * 
	 */
	public void convertStageToResource(StageId stageId, ResourceId resourceId, long amount) {

		validateResourceId(resourceId);
		validateStageId(stageId);
		validateStageIsNotOffered(stageId);
		StageRecord stageRecord = stageRecords.get(stageId);
		final MaterialsProducerId materialsProducerId = stageRecord.materialsProducerRecord.materialProducerId;
		validateNonnegativeResourceAmount(amount);
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		long previousResourceLevel = componentResourceRecord.getAmount();
		validateResourceAdditionValue(previousResourceLevel, amount);

		dataManagerContext.addPlan((context) -> {
			for (final BatchRecord batchRecord : stageRecord.batchRecords) {
				batchPropertyMap.remove(batchRecord.batchId);
				batchRecords.remove(batchRecord.batchId);
			}
			stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
			stageRecords.remove(stageId);

		}, dataManagerContext.getTime());
		componentResourceRecord.incrementAmount(amount);

		long currentResourceLevel = componentResourceRecord.getAmount();
		dataManagerContext.releaseEvent(new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId, previousResourceLevel, currentResourceLevel));

		for (BatchRecord batchRecord : stageRecord.batchRecords) {
			dataManagerContext.releaseEvent(new BatchImminentRemovalEvent(batchRecord.batchId));
		}
		dataManagerContext.releaseEvent(new StageImminentRemovalEvent(stageId));

	}
	
	private void validateNewMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIAL, materialId);
		}
	}

	/**
	 * Adds a new material type
	 * 
	 * @throws ContractException
	 * <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the material id is null</li>
	 * <li>{@linkplain MaterialsError#DUPLICATE_MATERIAL} if the material id is already present</li>
	 * 
	 */
	public void addMaterialId(MaterialId materialId) {
		validateNewMaterialId(materialId);

		materialIds.add(materialId);
		batchPropertyIdMap.put(materialId, new LinkedHashSet<>());
		batchPropertyDefinitions.put(materialId, new LinkedHashMap<>());
		
		dataManagerContext.releaseEvent(new MaterialIdAdditionEvent(materialId));

	}

}
