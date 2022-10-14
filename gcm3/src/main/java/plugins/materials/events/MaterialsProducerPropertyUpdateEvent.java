package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;

@Immutable
public class MaterialsProducerPropertyUpdateEvent implements Event {
	private final MaterialsProducerId materialsProducerId;
	private final MaterialsProducerPropertyId materialsProducerPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public MaterialsProducerPropertyUpdateEvent(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object previousPropertyValue,
			Object currentPropertyValue) {
		super();
		this.materialsProducerId = materialsProducerId;
		this.materialsProducerPropertyId = materialsProducerPropertyId;
		this.previousPropertyValue = previousPropertyValue;
		this.currentPropertyValue = currentPropertyValue;
	}

	public MaterialsProducerId getMaterialsProducerId() {
		return materialsProducerId;
	}

	public MaterialsProducerPropertyId getMaterialsProducerPropertyId() {
		return materialsProducerPropertyId;
	}

	public Object getPreviousPropertyValue() {
		return previousPropertyValue;
	}

	public Object getCurrentPropertyValue() {
		return currentPropertyValue;
	}

	@Override
	public String toString() {
		return "MaterialsProducerPropertyUpdateEvent [materialsProducerId=" + materialsProducerId + ", materialsProducerPropertyId=" + materialsProducerPropertyId + ", previousPropertyValue="
				+ previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	@Override
	public Object getPrimaryKeyValue() {
		return this.materialsProducerPropertyId;
	}
	
}
