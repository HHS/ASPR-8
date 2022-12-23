package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.util.properties.PropertyError;
import util.errors.ContractException;

/**
 * An event released by the materials data manager whenever a materials producer property
 * definition is added to the simulation.
 *
 * @author Shawn Hatch
 */
@Immutable
public record MaterialsProducerPropertyDefinitionEvent(
		MaterialsProducerPropertyId materialsProducerPropertyId) implements Event {

	/**
	 * Creates the event.
	 *
	 * @throws ContractException <li>{@linkplain PropertyError#NULL_PROPERTY_ID if the
	 *                           property id is null</li>
	 */
	public MaterialsProducerPropertyDefinitionEvent {

		if (materialsProducerPropertyId == null) {
			throw new ContractException(PropertyError.NULL_PROPERTY_ID);
		}
	}

}
