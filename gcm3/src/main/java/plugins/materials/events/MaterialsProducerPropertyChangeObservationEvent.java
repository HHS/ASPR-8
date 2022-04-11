package plugins.materials.events;

import net.jcip.annotations.Immutable;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import nucleus.SimulationContext;
import nucleus.util.ContractException;
import plugins.materials.datamangers.MaterialsDataManager;
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
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class).get();
		if (!materialsDataManager.materialsProducerIdExists(materialsProducerId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	private static void validateMaterialsProducerPropertyId(SimulationContext simulationContext, MaterialsProducerPropertyId materialsProducerPropertyId) {
		if (materialsProducerPropertyId == null) {
			throw new ContractException(MaterialsError.NULL_MATERIALS_PRODUCER_PROPERTY_ID);
		}
		MaterialsDataManager materialsDataManager = simulationContext.getDataManager(MaterialsDataManager.class).get();
		if (!materialsDataManager.materialsProducerPropertyIdExists(materialsProducerPropertyId)) {
			throw new ContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_PROPERTY_ID);
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
