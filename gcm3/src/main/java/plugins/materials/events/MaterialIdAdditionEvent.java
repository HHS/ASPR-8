package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import util.errors.ContractException;

/**
 * An event indicating that a material type has been added
 * 
 * @author Shawn Hatch
 */
@Immutable
public class MaterialIdAdditionEvent implements Event {
	
	private final MaterialId materialId;

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIAL_ID} if the
	 *             material id is null</li>
	 * 
	 */
	public MaterialIdAdditionEvent(MaterialId materialId) {
		if (materialId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIAL_ID);
		}

		this.materialId = materialId;
	}

	/**
	 * Returns the recently added material id
	 */
	public MaterialId getMaterialId() {
		return materialId;
	}

}
