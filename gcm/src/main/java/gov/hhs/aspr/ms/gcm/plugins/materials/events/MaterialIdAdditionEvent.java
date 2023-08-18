package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import net.jcip.annotations.Immutable;
import util.errors.ContractException;

/**
 * An event indicating that a material type has been added
 */
@Immutable
public record MaterialIdAdditionEvent(MaterialId materialId) implements Event {

	/**
	 * Constructs the event
	 *
	 * @throws util.errors.ContractException
	 *                           <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if
	 *                           the material id is null</li>
	 *                           </ul>
	 */
	public MaterialIdAdditionEvent {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

	}

}
