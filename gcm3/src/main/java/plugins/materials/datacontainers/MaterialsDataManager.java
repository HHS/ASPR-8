package plugins.materials.datacontainers;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import nucleus.Context;
import nucleus.NucleusError;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.properties.support.PropertyDefinition;
import plugins.properties.support.PropertyError;
import plugins.properties.support.PropertyValueRecord;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.ContractException;

/**
 * General manager for all material activities.
 *
 * @author Shawn Hatch
 *
 */
public final class MaterialsDataManager {

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
		private final Context context;

		private long amount;

		private double assignmentTime;

		public ComponentResourceRecord(final Context context) {
			this.context = context;
		}

		public void decrementAmount(final long amount) {
			if (amount < 0) {
				throw new RuntimeException("negative amount");
			}

			if (this.amount < amount) {
				throw new RuntimeException("cannot decrement to a negative level");
			}
			this.amount = Math.subtractExact(this.amount, amount);
			assignmentTime = context.getTime();
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
			assignmentTime = context.getTime();
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

	private final Set<MaterialsProducerPropertyId> materialsProducerPropertyIds = new LinkedHashSet<>();

	private final Map<MaterialsProducerId, Map<MaterialsProducerPropertyId, PropertyValueRecord>> materialsProducerPropertyMap = new LinkedHashMap<>();

	private final Map<BatchId, Map<BatchPropertyId, PropertyValueRecord>> batchPropertyMap = new LinkedHashMap<>();

	private final Map<MaterialId, Set<BatchPropertyId>> batchPropertyIdMap = new LinkedHashMap<>();

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

	private BatchId lastIssuedBatchId;

	private StageId lastIssuedStageId;

	private final Set<MaterialId> materialIds = new LinkedHashSet<>();

	private final Set<ResourceId> resourceIds = new LinkedHashSet<>();

	private final Context context;

	/**
	 * Constructs the data manager.
	 * 
	 * @throw {@link ContractException}
	 *        <li>{@linkplain NucleusError#NULL_CONTEXT} if the context is
	 *        null</li>
	 */
	public MaterialsDataManager(final Context context) {
		if (context == null) {
			throw new ContractException(NucleusError.NULL_CONTEXT);
		}

		this.context = context;

	}

	/**
	 * Adds a material id to the data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_MATERIAL} if the
	 *             material id was previously added</li>
	 */
	public void addMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
		if (materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIAL);
		}
		materialIds.add(materialId);
		batchPropertyIdMap.put(materialId, new LinkedHashSet<>());
		batchPropertyDefinitions.put(materialId, new LinkedHashMap<>());
	}

	/**
	 * Adds a materials producer id to this data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id was previously added</li>
	 *             <li>{@linkplain MaterialsError#MATERIALS_PRODUCER_PROPERTY_LOADING_ORDER}
	 *             if materials producer properties have already been added</li>
	 */
	public void addMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (materialsProducerMap.containsKey(materialsProducerId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_ID);
		}

		if (!materialsProducerPropertyIds.isEmpty()) {
			throw new ContractException(MaterialsError.MATERIALS_PRODUCER_PROPERTY_LOADING_ORDER);
		}

		final MaterialsProducerRecord materialsProducerRecord = new MaterialsProducerRecord();
		materialsProducerRecord.materialProducerId = materialsProducerId;
		for (final ResourceId resourceId : resourceIds) {
			materialsProducerRecord.materialProducerResources.put(resourceId, new ComponentResourceRecord(context));
		}
		materialsProducerMap.put(materialsProducerId, materialsProducerRecord);
		materialsProducerPropertyMap.put(materialsProducerId, new LinkedHashMap<>());
	}

	/**
	 * Adds a resource to this data manager.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain ResourceError#NULL_RESOURCE_ID} if the
	 *             resource id is null</li>
	 *             <li>{@linkplain ResourceError#DUPLICATE_RESOURCE_ID} if the
	 *             resource was previously added</li>
	 *             <li>{@linkplain MaterialsError#RESOURCE_LOADING_ORDER} if
	 *             materials producer id values have already been added</li>
	 */
	public void addResource(final ResourceId resourceId) {

		if (resourceId == null) {
			throw new ContractException(ResourceError.NULL_RESOURCE_ID);
		}
		if (resourceIds.contains(resourceId)) {
			throw new ContractException(ResourceError.DUPLICATE_RESOURCE_ID);
		}
		if (!materialsProducerMap.isEmpty()) {
			throw new ContractException(MaterialsError.RESOURCE_LOADING_ORDER);
		}
		resourceIds.add(resourceId);
	}
	
	/**
	 * Returns the set of resource ids that were added to this manager.
	 */
	public Set<ResourceId> getResourceIds(){
		return new LinkedHashSet<>(resourceIds);
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
	 * Returns the batch id of a new created batch that is stored in the
	 * inventory of the materials producer.
	 *
	 * Preconditions:
	 * <li>the material id must be valid</li>
	 * <li>the amount should be non-negative</li>
	 *
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producer id is null</li>
	 *             <li>if the materials producer id is unknown</li>
	 * 
	 */
	public BatchId createBatch(final MaterialsProducerId materialsProducerId, final MaterialId materialId, final double amount) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final BatchRecord batchRecord = new BatchRecord(nextBatchRecordId++);
		batchRecord.amount = amount;
		batchRecord.creationTime = context.getTime();
		batchRecord.materialId = materialId;
		batchRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.inventory.add(batchRecord);
		batchRecords.put(batchRecord.batchId, batchRecord);
		lastIssuedBatchId = batchRecord.batchId;

		final Map<BatchPropertyId, PropertyValueRecord> map = new LinkedHashMap<>();
		final Set<BatchPropertyId> batchPropertyIds = batchPropertyIdMap.get(materialId);
		for (final BatchPropertyId batchPropertyId : batchPropertyIds) {
			final PropertyDefinition propertyDefinition = getBatchPropertyDefinition(materialId, batchPropertyId);
			final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(context);
			propertyValueRecord.setPropertyValue(propertyDefinition.getDefaultValue().get());
			map.put(batchPropertyId, propertyValueRecord);
		}
		batchPropertyMap.put(batchRecord.batchId, map);

		return batchRecord.batchId;
	}

	/**
	 * Creates a new stage owned by the materials producer and returns the
	 * stage's id.
	 *
	 * @throws RuntimeException
	 *             <li>if the materials producer id is null</li>
	 *             <li>if the materials producer id is unknown</li>
	 *
	 */
	public StageId createStage(final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final StageRecord stageRecord = new StageRecord(nextStageRecordId++);
		stageRecord.materialsProducerRecord = materialsProducerRecord;
		materialsProducerRecord.stageRecords.add(stageRecord);
		stageRecords.put(stageRecord.stageId, stageRecord);
		lastIssuedStageId = stageRecord.stageId;
		return stageRecord.stageId;
	}

	/**
	 * Decrements the materials producer resource level by the given amount.
	 * 
	 * Preconditions:
	 * <li>the amount should be non-negative</li>
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producer id is null</li>
	 *             <li>if the materials producer id is unknown</li>
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 * 
	 * 
	 */
	public void decrementMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		componentResourceRecord.decrementAmount(amount);
	}

	/**
	 * Defines a batch property.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID} if the
	 *             material id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_BATCH_PROPERTY_DEFINITION}
	 *             if the batch property was previously defined</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *             <li>{@linkplain PropertyError#PROPERTY_DEFINITION_MISSING_DEFAULT}
	 *             if the property definition does not contain a default
	 *             value</li>
	 */
	public void defineBatchProperty(final MaterialId materialId, final BatchPropertyId batchPropertyId, final PropertyDefinition propertyDefinition) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (!materialIds.contains(materialId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIAL_ID);
		}

		if (batchPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}

		if (batchPropertyIdMap.get(materialId).contains(batchPropertyId)) {
			throw new ContractException(MaterialsError.DUPLICATE_BATCH_PROPERTY_DEFINITION);
		}

		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		if (!propertyDefinition.getDefaultValue().isPresent()) {
			throw new ContractException(PropertyError.PROPERTY_DEFINITION_MISSING_DEFAULT);
		}
		batchPropertyIdMap.get(materialId).add(batchPropertyId);
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		map.put(batchPropertyId, propertyDefinition);
	}

	/**
	 * Defines a materials producer property.
	 * 
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_PROPERTY_ID}
	 *             if the materials producer property id is null</li>
	 *             <li>{@linkplain MaterialsError#DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION}
	 *             if the materials producer property was previously
	 *             defined</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_DEFINITION} if
	 *             the property definition is null</li>
	 *
	 */
	public void defineMaterialsProducerProperty(final MaterialsProducerPropertyId materialsProducerPropertyId, final PropertyDefinition propertyDefinition) {

		if (materialsProducerPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}
		if (materialsProducerPropertyIds.contains(materialsProducerPropertyId)) {
			throw new ContractException(MaterialsError.DUPLICATE_MATERIALS_PRODUCER_PROPERTY_DEFINITION);
		}
		if (propertyDefinition == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_DEFINITION);
		}

		materialsProducerPropertyIds.add(materialsProducerPropertyId);
		for (final MaterialsProducerId materialsProducerId : materialsProducerMap.keySet()) {
			final Map<MaterialsProducerPropertyId, PropertyValueRecord> map = materialsProducerPropertyMap.get(materialsProducerId);
			final PropertyValueRecord propertyValueRecord = new PropertyValueRecord(context);
			map.put(materialsProducerPropertyId, propertyValueRecord);
		}

		materialsProducerPropertyDefinitions.put(materialsProducerPropertyId, propertyDefinition);
	}

	/**
	 * Destroys the batch . The batch may not be part of an offered stage.
	 *
	 * @throw RuntimeException
	 *        <li>if the batch id is null</li>
	 *        <li>if the batch id is unknown</li>
	 *
	 */
	public void destroyBatch(final BatchId batchId) {
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
	 * Destroys the stage, releasing any associated stages to inventory.
	 * 
	 *
	 * @throw RuntimeException
	 *        <li>if the batch id is null</li>
	 *        <li>if the batch id is unknown</li>
	 *
	 */
	public void destroyStage(final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			batchRecord.stageRecord = null;
			batchRecord.materialsProducerRecord.inventory.add(batchRecord);
		}
		stageRecord.materialsProducerRecord.stageRecords.remove(stageRecord);
		stageRecords.remove(stageId);
	}

	/**
	 * Returns the batch's amount.
	 * 
	 *
	 * Preconditions:
	 * <li>if the batch id must be valid</li>
	 * 
	 *
	 */
	public double getBatchAmount(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.amount;
	}

	/**
	 * Returns the batch's material.
	 * 
	 *
	 * Preconditions:
	 * <li>if the batch id must be valid</li>
	 * 
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		return (T) batchRecords.get(batchId).materialId;
	}

	/**
	 * Returns the batch's material producer.
	 * 
	 *
	 * Preconditions:
	 * <li>if the batch id must be valid</li>
	 * 
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> T getBatchProducer(final BatchId batchId) {
		return (T) batchRecords.get(batchId).materialsProducerRecord.materialProducerId;
	}

	/**
	 * Returns the property definition associated with the material id and batch
	 * property id.
	 * 
	 * Preconditions:
	 * <li>if the batch property id must be valid</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the material id is null</li>
	 *             <li>if the material id is unknown</li>
	 * 
	 *
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		return map.get(batchPropertyId);
	}

	/**
	 * Returns the batch property ids associated with the material id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the material id is null</li>
	 *             <li>if the material id is unknown</li>
	 * 
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		final Map<BatchPropertyId, PropertyDefinition> map = batchPropertyDefinitions.get(materialId);
		final Set<T> result = new LinkedHashSet<>(map.keySet().size());
		for (final BatchPropertyId batchPropertyId : map.keySet()) {
			result.add((T) batchPropertyId);
		}
		return result;
	}

	/**
	 * Returns the last time that the batch property value was assigned for the
	 * given batch.
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 *             <li>if the batch property id is null</li>
	 *             <li>if the batch property id is unknown</li>
	 *
	 */
	public double getBatchPropertyTime(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		final PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		return propertyValueRecord.getAssignmentTime();
	}

	/**
	 * Returns the batch property value given batch id and batch property id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 *             <li>if the batch property id is null</li>
	 *             <li>if the batch property id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T> T getBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId) {
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		final PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		return (T) propertyValueRecord.getValue();
	}

	/**
	 * Returns the batch property value given batch id and batch property id.
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 *
	 */
	public Optional<StageId> getBatchStageId(final BatchId batchId) {
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
	 * Returns the creation time of the batch.
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 *
	 */
	public double getBatchTime(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		return batchRecord.creationTime;
	}

	/**
	 * Returns the list of batches that are held in the inventory of the give
	 * materials producer.
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *
	 */
	public List<BatchId> getInventoryBatches(final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<BatchId> result = new ArrayList<>(materialsProducerRecord.inventory.size());
		for (final BatchRecord batchRecord : materialsProducerRecord.inventory) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	/**
	 * Returns the list of batches of the given material id that are held in the
	 * inventory of the give materials producer.
	 * 
	 * Preconditions:
	 * <li>the material id must be valid</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *
	 */
	public List<BatchId> getInventoryBatchesByMaterialId(final MaterialsProducerId materialsProducerId, final MaterialId materialId) {
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
	 * Returns the last issued batch id.
	 */
	public Optional<BatchId> getLastIssuedBatchId() {
		return Optional.ofNullable(lastIssuedBatchId);
	}

	/**
	 * Returns the last issued stage id.
	 */
	public Optional<StageId> getLastIssuedStageId() {
		return Optional.ofNullable(lastIssuedStageId);
	}

	/**
	 * Returns the set of material ids
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
	 * Returns the property definition associated with the materials producer
	 * property id.
	 * 
	 * Preconditions:
	 * <li>the materials producer property id must be valid</li>
	 * 
	 *
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyDefinitions.get(materialsProducerPropertyId);
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
	 * Returns the last assignment time for the property value associated with
	 * the given materials producer id and materials producer property id.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *             <li>if the materials producerId property id is null</li>
	 *             <li>if the materials producerId property id is unknown</li>
	 *
	 */
	public double getMaterialsProducerPropertyTime(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getAssignmentTime();
	}

	/**
	 * Returns the property value associated with the given materials producer
	 * id and materials producer property id.
	 * 
	 * Preconditions:
	 * <li>the material id must be valid</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *             <li>if the materials producerId property id is null</li>
	 *             <li>if the materials producerId property id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T> T getMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return (T) materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).getValue();
	}

	/**
	 * Returns the resource level for the given materials producer id and
	 * resource id.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *
	 */
	public long getMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		return componentResourceRecord.getAmount();
	}

	/**
	 * Returns the time when the current resource level was set for the given
	 * materials producer id and resource id.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 *
	 */
	public double getMaterialsProducerResourceTime(final MaterialsProducerId materialsProducerId, final ResourceId resourceId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		return componentResourceRecord.getAssignmentTime();
	}

	/**
	 * Returns the list of stages that have been offered for transfer from the
	 * given materials producer.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *
	 */
	public List<StageId> getOfferedStages(final MaterialsProducerId materialsProducerId) {
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
	 * Returns the list of batches associated with the given stage.
	 * 
	 * @throws RuntimeException
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 *
	 */
	public List<BatchId> getStageBatches(final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		final List<BatchId> result = new ArrayList<>(stageRecord.batchRecords.size());
		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			result.add(batchRecord.batchId);
		}
		return result;
	}

	/**
	 * Returns the list of batches associated with the given stage and material
	 * id.
	 * 
	 * Preconditions:
	 * <li>the material id must be valid</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 *
	 */
	public List<BatchId> getStageBatchesByMaterialId(final StageId stageId, final MaterialId materialId) {
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
	 * Returns the materials producer that created the given stage.
	 *
	 * 
	 * @throws RuntimeException
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 *
	 */
	@SuppressWarnings("unchecked")
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		return (T) stageRecords.get(stageId).materialsProducerRecord.materialProducerId;
	}

	/**
	 * Returns the list of stages associated with the given materials producer.
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *
	 */
	public List<StageId> getStages(final MaterialsProducerId materialsProducerId) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final List<StageId> result = new ArrayList<>(materialsProducerRecord.stageRecords.size());
		for (final StageRecord stageRecord : materialsProducerRecord.stageRecords) {
			result.add(stageRecord.stageId);
		}
		return result;
	}

	/**
	 * Increments the materials producer resource level by the given amount.
	 * 
	 * Preconditions:
	 * 
	 * <li>the amount must be non-negative</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the amount will cause an overflow</li>
	 *             <li>if the materials producerId id is null</li>
	 *             <li>if the materials producerId id is unknown</li>
	 *             <li>if the resource id is null</li>
	 *             <li>if the resource id is unknown</li>
	 */
	public void incrementMaterialsProducerResourceLevel(final MaterialsProducerId materialsProducerId, final ResourceId resourceId, final long amount) {
		final MaterialsProducerRecord materialsProducerRecord = materialsProducerMap.get(materialsProducerId);
		final ComponentResourceRecord componentResourceRecord = materialsProducerRecord.materialProducerResources.get(resourceId);
		componentResourceRecord.incrementAmount(amount);
	}

	/**
	 * Returns true if and only if the stage is offered for trade.
	 * 
	 * 
	 * @throws RuntimeException
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 *
	 */
	public boolean isStageOffered(final StageId stageId) {
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
	 * Returns true if and only if the material producer id has been added to
	 * this data manager. Null tolerant.
	 */
	public boolean materialsProducerIdExists(final MaterialsProducerId materialsProducerId) {
		return materialsProducerMap.containsKey(materialsProducerId);
	}

	/**
	 * Returns true if and only if the material producer property id exists.
	 */
	public boolean materialsProducerPropertyIdExists(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsProducerPropertyIds.contains(materialsProducerPropertyId);
	}

	/**
	 * Moves the batch to its materials producer's inventory.
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch is not associated with a stage</li>
	 *             <li>if the associated stage is being offered</li>
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 */
	public void moveBatchToInventory(final BatchId batchId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		final StageRecord stageRecord = batchRecord.stageRecord;
		if (stageRecord.offered) {
			throw new RuntimeException("batch cannot be moved to inventory when its stage is offered");
		}
		stageRecord.batchRecords.remove(batchRecord);
		batchRecord.stageRecord = null;
		batchRecord.materialsProducerRecord.inventory.add(batchRecord);
	}

	/**
	 * Moves the batch to its materials producer's stage.
	 *
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 *             <li>if the batch id is already associated with a stage</li>
	 */
	public void moveBatchToStage(final BatchId batchId, final StageId stageId) {
		final BatchRecord batchRecord = batchRecords.get(batchId);
		if (batchRecord.stageRecord != null) {
			throw new RuntimeException("batch already staged");
		}
		final StageRecord stageRecord = stageRecords.get(stageId);
		batchRecord.stageRecord = stageRecord;
		stageRecord.batchRecords.add(batchRecord);
		batchRecord.materialsProducerRecord.inventory.remove(batchRecord);
	}

	/**
	 * Sets a batch's property value
	 *
	 * Preconditions:
	 * <li>the batch property id must be valid</li>
	 * <li>the batch property value must be valid -- non-null and consistent
	 * with the associated property definition</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the batch id is null</li>
	 *             <li>if the batch id is unknown</li>
	 */
	public void setBatchPropertyValue(final BatchId batchId, final BatchPropertyId batchPropertyId, final Object batchPropertyValue) {
		final Map<BatchPropertyId, PropertyValueRecord> map = batchPropertyMap.get(batchId);
		PropertyValueRecord propertyValueRecord = map.get(batchPropertyId);
		if (propertyValueRecord == null) {
			propertyValueRecord = new PropertyValueRecord(context);
			map.put(batchPropertyId, propertyValueRecord);
		}
		propertyValueRecord.setPropertyValue(batchPropertyValue);
	}

	/**
	 * Sets the value of the materials producer property.
	 *
	 * Preconditions:
	 * 
	 * <li>the property value must be valid -- non-null and consistent with the
	 * associated property definition</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producer id is null</li>
	 *             <li>if the materials producer id is unknown</li>
	 *             <li>if the materials producer property id is null</li>
	 *             <li>if the materials producer property id is unknown</li>
	 * 
	 */
	public void setMaterialsProducerPropertyValue(final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			final Object materialsProducerPropertyValue) {
		materialsProducerPropertyMap.get(materialsProducerId).get(materialsProducerPropertyId).setPropertyValue(materialsProducerPropertyValue);
	}

	/**
	 * Sets the offer state of the stage.
	 * 
	 * @throws RuntimeException
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 */

	public void setStageOffer(final StageId stageId, final boolean offer) {
		final StageRecord stageRecord = stageRecords.get(stageId);
		stageRecord.offered = offer;
	}

	/**
	 * Transfers the given amount from one batch to another
	 *
	 * Preconditions:
	 * <li>the amount must be non-negative</li>
	 * <li>the amount must be not exceed the source batch's current level</li>
	 * <li>the amount must not cause an overflow of the destination batch's
	 * level</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the source batch id is null</li>
	 *             <li>if the source batch id is unknown</li>
	 *             <li>if the destination batch id is null</li>
	 *             <li>if the destination batch id is unknown</li>
	 */
	public void shiftBatchContent(final BatchId sourceBatchId, final BatchId destinationBatchId, final double amount) {
		final BatchRecord sourceBatchRecord = batchRecords.get(sourceBatchId);
		final BatchRecord destinationBatchRecord = batchRecords.get(destinationBatchId);
		sourceBatchRecord.amount -= amount;
		destinationBatchRecord.amount += amount;
	}

	/**
	 * Returns true if and only if the stage exists. Null tolerant.
	 */
	public boolean stageExists(final StageId stageId) {
		return stageRecords.containsKey(stageId);
	}

	/**
	 * Moves the stage and its associated batches to the materials producer and
	 * sets the offer state of the stage to false.
	 *
	 * Preconditions:
	 * <li>the materials producer should not be the current owner of the
	 * stage</li>
	 * <li>the stage must be in an offered state</li>
	 * 
	 * @throws RuntimeException
	 *             <li>if the materials producer id is null</li>
	 *             <li>if the materials producer id is unknown</li>
	 *             <li>if the stage id is null</li>
	 *             <li>if the stage id is unknown</li>
	 */
	public void transferOfferedStageToMaterialsProducer(final MaterialsProducerId materialsProducerId, final StageId stageId) {
		final StageRecord stageRecord = stageRecords.get(stageId);

		final MaterialsProducerRecord currentMaterialsProducerRecord = stageRecord.materialsProducerRecord;

		final MaterialsProducerRecord newMaterialsProducerRecord = materialsProducerMap.get(materialsProducerId);

		currentMaterialsProducerRecord.stageRecords.remove(stageRecord);

		newMaterialsProducerRecord.stageRecords.add(stageRecord);

		stageRecord.materialsProducerRecord = newMaterialsProducerRecord;

		for (final BatchRecord batchRecord : stageRecord.batchRecords) {
			batchRecord.materialsProducerRecord = newMaterialsProducerRecord;
		}

		stageRecord.offered = false;
	}

}
