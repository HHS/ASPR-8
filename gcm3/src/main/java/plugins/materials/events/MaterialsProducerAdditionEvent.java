package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import util.errors.ContractException;
/**
 * An event indicating that a materials producer has been added
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class MaterialsProducerAdditionEvent implements Event {
	private final MaterialsProducerId materialsProducerId;

	/**
	 * Constructs the event
	 * 
	 * @throws ContractException
	 *             <li>{@linkplain MaterialsError#NULL_MATERIALS_PRODUCER_ID} if
	 *             the materials producer id is null</li>
	 * 
	 * 
	 */
	public MaterialsProducerAdditionEvent(MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}

		this.materialsProducerId = materialsProducerId;
	}

	/**
	 * Returns the id of a recently added materials producer
	 */
	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

}
