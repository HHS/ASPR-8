package plugins.materials.events.mutation;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;

/**
 * Sets property value for the given materials producer and property.
 */
@Immutable
public final class MaterialsProducerPropertyValueAssignmentEvent implements Event {

	private final MaterialsProducerId materialsProducerId;

	private final MaterialsProducerPropertyId materialsProducerPropertyId;

	private final Object materialsProducerPropertyValue;

	
	public MaterialsProducerPropertyValueAssignmentEvent(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		super();
		this.materialsProducerId = materialsProducerId;
		this.materialsProducerPropertyId = materialsProducerPropertyId;
		this.materialsProducerPropertyValue = materialsProducerPropertyValue;
	}

	public Object getMaterialsProducerPropertyValue() {
		return materialsProducerPropertyValue;
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

	public MaterialsProducerPropertyId getMaterialsProducerPropertyId() {
		return materialsProducerPropertyId;
	}

}
