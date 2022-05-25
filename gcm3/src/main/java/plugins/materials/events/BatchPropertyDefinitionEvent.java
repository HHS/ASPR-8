package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import util.errors.ContractException;

/**
 * An event indicating the addition of a batch property for the given material.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class BatchPropertyDefinitionEvent implements Event{

	private BatchPropertyId batchPropertyId;
	private MaterialId materialId;

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain MaterialsError#NULL_BATCH_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 */
	public BatchPropertyDefinitionEvent(MaterialId materialId, BatchPropertyId batchPropertyId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
		if (batchPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_BATCH_PROPERTY_ID);
		}
		this.materialId = materialId;
		this.batchPropertyId = batchPropertyId;
	}

	/**
	 * Returns the batch property id
	 */
	public BatchPropertyId getBatchPropertyId() {
		return batchPropertyId;
	}

	/**
	 * Returns the material id
	 */
	public MaterialId getMaterialId() {
		return materialId;
	}

}
