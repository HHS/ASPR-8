package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import util.errors.ContractException;

/**
 * An event indicating that a material type has been added
 *
 */
@Immutable
public record MaterialIdAdditionEvent(MaterialId materialId) implements Event {

	/**
	 * Constructs the event
	 *
	 * @throws ContractException <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *                           material id is null</li>
	 */
	public MaterialIdAdditionEvent {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

	}

}
