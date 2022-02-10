package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.SimulationContext;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;

@Immutable
public class MaterialsProducerPropertyChangeObservationEvent implements Event {
	private final MaterialsProducerId materialsProducerId;
	private final MaterialsProducerPropertyId materialsProducerPropertyId;
	private final Object previousPropertyValue;
	private final Object currentPropertyValue;

	public MaterialsProducerPropertyChangeObservationEvent(MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId, Object previousPropertyValue, Object currentPropertyValue) {
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
		return "MaterialsProducerPropertyChangeObservationEvent [materialsProducerId=" + materialsProducerId + ", materialsProducerPropertyId=" + materialsProducerPropertyId + ", previousPropertyValue=" + previousPropertyValue + ", currentPropertyValue=" + currentPropertyValue + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		PRODUCER_PROPERTY
	}

	private static void validateMaterialsProducerId(SimulationContext simulationContext, MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataView materialsDataView = simulationContext.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.materialsProducerIdExists(materialsProducerId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	private static void validateMaterialsProducerPropertyId(SimulationContext simulationContext, MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			simulationContext.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}
		MaterialsDataView materialsDataView = simulationContext.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.materialsProducerPropertyIdExists(materialsProducerPropertyId)) {
			simulationContext.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID);
		}
	}

	public static EventLabel<MaterialsProducerPropertyChangeObservationEvent> getEventLabelByMaterialsProducerAndProperty(SimulationContext simulationContext, MaterialsProducerId materialsProducerId, MaterialsProducerPropertyId materialsProducerPropertyId) {
		validateMaterialsProducerId(simulationContext, materialsProducerId);
		validateMaterialsProducerPropertyId(simulationContext, materialsProducerPropertyId);

		return new MultiKeyEventLabel<>(materialsProducerPropertyId, LabelerId.PRODUCER_PROPERTY, MaterialsProducerPropertyChangeObservationEvent.class, materialsProducerId, materialsProducerPropertyId);
	}

	public static EventLabeler<MaterialsProducerPropertyChangeObservationEvent> getEventLabelerForMaterialsProducerAndProperty() {
		return new SimpleEventLabeler<>(
				LabelerId.PRODUCER_PROPERTY,
				MaterialsProducerPropertyChangeObservationEvent.class,
				(context, event) -> new MultiKeyEventLabel<>(event.getMaterialsProducerPropertyId(), LabelerId.PRODUCER_PROPERTY, MaterialsProducerPropertyChangeObservationEvent.class, event.getMaterialsProducerId(), event.getMaterialsProducerPropertyId()));
	}

	@Override
	public Object getPrimaryKeyValue() {
		return this.materialsProducerPropertyId;
	}

}
