package plugins.materials.dataviews;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import nucleus.DataView;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * Data view of the MaterialsDataManager
 *
 */
public final class MaterialsDataView implements DataView {

	private final MaterialsDataManager materialsDataManager;

	/**
	 * Constructs this view from the corresponding data manager
	 * 
	 */
	public MaterialsDataView(MaterialsDataManager materialsDataManager) {
		this.materialsDataManager = materialsDataManager;
	}

	/**
	 * Returns the set materials producer property ids
	 */
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		return materialsDataManager.getMaterialsProducerPropertyIds();
	}

	/**
	 * Returns the material id values for the simulation
	 */
	public <T extends MaterialId> Set<T> getMaterialIds() {
		return materialsDataManager.getMaterialIds();
	}

	/**
	 * Returns true if and only if the batch exists. Null tolerant.
	 */
	public boolean batchExists(final BatchId batchId) {
		return materialsDataManager.batchExists(batchId);
	}

	/**
	 * Returns true if and only if the batch property exists. Null tolerant.
	 * 
	 */
	public boolean batchPropertyIdExists(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		return materialsDataManager.batchPropertyIdExists(materialId, batchPropertyId);
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
		return materialsDataManager.getBatchAmount(batchId);
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
	public <T extends MaterialId> T getBatchMaterial(final BatchId batchId) {
		return materialsDataManager.getBatchMaterial(batchId);
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
	public <T extends MaterialsProducerId> T getBatchProducer(final BatchId batchId) {
		return materialsDataManager.getBatchProducer(batchId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the batch
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             batch property id is unknown</li>
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		return materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
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
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		return materialsDataManager.getBatchPropertyIds(materialId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the batch
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             batch property id is unknown</li>
	 */
	public double getBatchPropertyTime(BatchId batchId, BatchPropertyId batchPropertyId) {
		return materialsDataManager.getBatchPropertyTime(batchId, batchPropertyId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the batch
	 *             property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             batch property id is unknown</li>
	 */

	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId) {
		return materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
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
		return materialsDataManager.getBatchStageId(batchId);
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
		return materialsDataManager.getBatchTime(batchId);
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
		return materialsDataManager.getInventoryBatches(materialsProducerId);
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
		return materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
	}

	/**
	 * Returns the set of material producer ids
	 */
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		return materialsDataManager.getMaterialsProducerIds();
	}

	/**
	 * Returns the property definition for the given
	 * {@link MaterialsProducerPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             materials producer property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             materials producer property id is unknown</li>
	 */
	public PropertyDefinition getMaterialsProducerPropertyDefinition(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
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
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             materials producer property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             materials producer property id is unknown</li>
	 */

	public double getMaterialsProducerPropertyTime(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataManager.getMaterialsProducerPropertyTime(materialsProducerId, materialsProducerPropertyId);

	}

	/**
	 * Returns the value of the materials producer property.
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIALS_PRODUCER_ID}
	 *             if the materials producer id is unknown</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             materials producer property id is null</li>
	 *             <li>{@linkplain PropertyError#UNKNOWN_PROPERTY_ID} if the
	 *             materials producer property id is unknown</li>
	 */
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
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
		return materialsDataManager.getMaterialsProducerResourceLevel(materialsProducerId, resourceId);
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
		return materialsDataManager.getMaterialsProducerResourceTime(materialsProducerId, resourceId);
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
		return materialsDataManager.getOfferedStages(materialsProducerId);
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
		return materialsDataManager.getStageBatches(stageId);
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
		return materialsDataManager.getStageBatchesByMaterialId(stageId, materialId);
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
	public <T extends MaterialsProducerId> T getStageProducer(final StageId stageId) {
		return materialsDataManager.getStageProducer(stageId);
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
		return materialsDataManager.getStages(materialsProducerId);
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
		return materialsDataManager.isStageOffered(stageId);
	}

	/**
	 * Returns true if and only if the material exists. Tolerates null.
	 */
	public boolean materialIdExists(final MaterialId materialId) {
		return materialsDataManager.materialIdExists(materialId);
	}

	/**
	 * Returns true if and only if the material producer id exists. Null
	 * tolerant.
	 */
	public boolean materialsProducerIdExists(final MaterialsProducerId materialsProducerId) {
		return materialsDataManager.materialsProducerIdExists(materialsProducerId);
	}

	/**
	 * Returns true if and only if the material producer property id exists.
	 * Null tolerant.
	 */
	public boolean materialsProducerPropertyIdExists(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataManager.materialsProducerPropertyIdExists(materialsProducerPropertyId);
	}

	/**
	 * Returns true if and only if the stage exists. Null tolerant.
	 */
	public boolean stageExists(final StageId stageId) {
		return materialsDataManager.stageExists(stageId);
	}

}
