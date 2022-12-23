package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An event indicating the addition of a batch property for the given material.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public record BatchPropertyDefinitionEvent(MaterialId materialId, BatchPropertyId batchPropertyId) implements Event{

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 *             <li>{@linkplain PropertyError#NULL_PROPERTY_ID} if the
	 *             batch property id is null</li>
	 */
	public BatchPropertyDefinitionEvent {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}
		if (batchPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
