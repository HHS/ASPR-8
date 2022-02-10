package plugins.materials.datacontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import nucleus.SimulationContext;
import nucleus.DataView;
import plugins.materials.support.BatchId;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.support.StageId;
import plugins.properties.support.PropertyDefinition;
import plugins.resources.datacontainers.ResourceDataView;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import util.ContractException;

/**
 * General manager for all material activities.
 * 
 * @author Shawn Hatch
 *
 */
public final class MaterialsDataView implements DataView {
	private final MaterialsDataManager materialsDataManager;
	private final SimulationContext simulationContext;
	private final ResourceDataView resourceDataView;

	public MaterialsDataView(SimulationContext simulationContext, MaterialsDataManager materialsDataManager) {
		this.simulationContext = simulationContext;
		this.materialsDataManager = materialsDataManager;
		resourceDataView = simulationContext.getDataView(ResourceDataView.class).get();

	}

	/**
	 * Returns true if and only if the batch exists. Null tolerant.
	 * 
	 * @param batchId
	 *            cannot be null
	 */
	public boolean batchExists(final BatchId batchId) {
		return materialsDataManager.batchExists(batchId);
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
		return materialsDataManager.getBatchTime(batchId);
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
		validateBatchId(batchId);
		return materialsDataManager.getBatchMaterial(batchId);
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
		return materialsDataManager.getBatchStageId(batchId);
	}

	/**
	 * Returns true if and only if the stage exists.
	 */
	public boolean stageExists(final StageId stageId) {
		return materialsDataManager.stageExists(stageId);
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
		return materialsDataManager.isStageOffered(stageId);
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
		return materialsDataManager.getStages(materialsProducerId);
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
		return materialsDataManager.getOfferedStages(materialsProducerId);
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
		return materialsDataManager.getStageBatchesByMaterialId(stageId, materialId);
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
		return materialsDataManager.getStageBatches(stageId);
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
		return materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
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
		return materialsDataManager.getInventoryBatches(materialsProducerId);
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
		validateStageId(stageId);
		return materialsDataManager.getStageProducer(stageId);
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
		validateBatchId(batchId);
		return materialsDataManager.getBatchProducer(batchId);
	}

	/**
	 * Returns the material id values for the simulation
	 */
	public <T extends MaterialId> Set<T> getMaterialIds() {
		return materialsDataManager.getMaterialIds();
	}

	/**
	 * Returns the last batch id added to the simulation
	 */
	public Optional<BatchId> getLastIssuedBatchId() {
		return materialsDataManager.getLastIssuedBatchId();
	}

	/**
	 * Returns the last stage id added to the simulation
	 */
	public Optional<StageId> getLastIssuedStageId() {
		return materialsDataManager.getLastIssuedStageId();
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
		validateMaterialsProducerId(materialsProducerId);
		validateResourceId(resourceId);
		return materialsDataManager.getMaterialsProducerResourceTime(materialsProducerId, resourceId);
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
		return materialsDataManager.getMaterialsProducerPropertyDefinition(materialsProducerPropertyId);
	}

	/**
	 * Returns the {@link MaterialsProducerPropertyId} values
	 */
	public <T extends MaterialsProducerPropertyId> Set<T> getMaterialsProducerPropertyIds() {
		return materialsDataManager.getMaterialsProducerPropertyIds();
	}

	/**
	 * Returns the property definition for the given {@link MaterialId} and
	 * {@link BatchPropertyId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *             the material id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID}
	 *             if the material id is unknown</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the batch property id
	 *             is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if the batch
	 *             property id is unknown</li>
	 */
	public PropertyDefinition getBatchPropertyDefinition(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		validateMaterialId(materialId);
		validateBatchPropertyId(materialId, batchPropertyId);
		return materialsDataManager.getBatchPropertyDefinition(materialId, batchPropertyId);
	}

	/**
	 * Returns the {@link BatchPropertyId} values for the given
	 * {@link MaterialId}
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *             the materials id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_MATERIAL_ID S_PRODUCER_ID}
	 *             if the materials id is unknown</li>
	 */
	public <T extends BatchPropertyId> Set<T> getBatchPropertyIds(final MaterialId materialId) {
		validateMaterialId(materialId);
		return materialsDataManager.getBatchPropertyIds(materialId);
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
	public <T> T getMaterialsProducerPropertyValue(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(materialsProducerId);
		validateMaterialsProducerPropertyId(materialsProducerPropertyId);
		return materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
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
		return materialsDataManager.getMaterialsProducerPropertyTime(materialsProducerId, materialsProducerPropertyId);
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
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 *             <li>{@linkplain MaterialsError#UNKNOWN_BATCH_PROPERTY_ID} if
	 *             the batch property id is unknown</li>
	 */

	public <T> T getBatchPropertyValue(BatchId batchId, BatchPropertyId batchPropertyId) {
		validateBatchId(batchId);
		final MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
		validateBatchPropertyId(batchMaterial, batchPropertyId);
		return materialsDataManager.getBatchPropertyValue(batchId, batchPropertyId);
	}

	/**
	 * Returns true if and only if the material producer id exists. Null tolerant.
	 */
	public boolean materialsProducerIdExists(MaterialsProducerId materialsProducerId) {
		return materialsDataManager.materialsProducerIdExists(materialsProducerId);
	}

	/**
	 * Returns true if and only if the material producer property id exists. Null tolerant.
	 */
	public boolean materialsProducerPropertyIdExists(MaterialsProducerPropertyId materialsProducerPropertyId) {
		return materialsDataManager.materialsProducerPropertyIdExists(materialsProducerPropertyId);
	}

	/**
	 * Returns the {@link MaterialsProducerPropertyId} values
	 */
	public <T extends MaterialsProducerId> Set<T> getMaterialsProducerIds() {
		return materialsDataManager.getMaterialsProducerIds();
	}

	private void validateBatchId(final BatchId batchId) {

		if (batchId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_BATCH_ID);
		}

		if (!batchExists(batchId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_BATCH_ID, batchId);
		}
	}

	private void validateBatchPropertyId(final MaterialId materialId, final BatchPropertyId batchPropertyId) {
		if (batchPropertyId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}

		if (!materialsDataManager.batchPropertyIdExists(materialId, batchPropertyId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_BATCH_PROPERTY_ID);
		}

	}

	private void validateMaterialId(final MaterialId materialId) {
		if (materialId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		if (!materialsDataManager.materialIdExists(materialId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIAL_ID, materialId);
		}
	}

	private void validateMaterialsProducerId(final MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		if (!materialsDataManager.materialsProducerIdExists(materialsProducerId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID, materialsProducerId);
		}
	}

	private void validateMaterialsProducerPropertyId(final MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}

		if (!materialsDataManager.materialsProducerPropertyIdExists(materialsProducerPropertyId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID, materialsProducerPropertyId);
		}
	}

	private void validateResourceId(final ResourceId resourceId) {
		if (resourceId == null) {
			simulationContext.throwContractException(ResourceError.NULL_RESOURCE_ID);
		}

		if (!resourceDataView.resourceIdExists(resourceId)) {
			simulationContext.throwContractException(ResourceError.UNKNOWN_RESOURCE_ID, resourceId);
		}
	}

	private void validateStageId(final StageId stageId) {
		if (stageId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_STAGE_ID);
		}

		if (!stageExists(stageId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_STAGE_ID, stageId);
		}
	}

}
