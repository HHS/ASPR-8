package gov.hhs.aspr.ms.gcm.plugins.materials.events;

import gov.hhs.aspr.ms.gcm.nucleus.Event;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.util.errors.ContractException;
import net.jcip.annotations.Immutable;

/**
 * An event indicating that a material type has been added
 */
@Immutable
public record MaterialIdAdditionEvent(MaterialId materialId) implements Event {

	/**
	 * Constructs the event
	 *
	 * @throws ContractException {@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *                           material id is null
	 */
	public MaterialIdAdditionEvent {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

	}

}
