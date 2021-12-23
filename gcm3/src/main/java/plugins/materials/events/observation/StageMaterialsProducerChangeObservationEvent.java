package plugins.materials.events.observation;

import net.jcip.annotations.Immutable;
import nucleus.Context;
import nucleus.Event;
import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.EventLabelerId;
import nucleus.MultiKeyEventLabel;
import nucleus.SimpleEventLabeler;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.support.MaterialsError;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.StageId;

@Immutable
public class StageMaterialsProducerChangeObservationEvent implements Event {
	private final StageId stageId;
	private final MaterialsProducerId previousMaterialsProducerId;
	private final MaterialsProducerId currentMaterialsProducerId;

	public StageMaterialsProducerChangeObservationEvent(StageId stageId, MaterialsProducerId previousMaterialsProducerId, MaterialsProducerId currentMaterialsProducerId) {
		super();
		this.stageId = stageId;
		this.previousMaterialsProducerId = previousMaterialsProducerId;
		this.currentMaterialsProducerId = currentMaterialsProducerId;
	}

	public StageId getStageId() {
		return stageId;
	}

	public MaterialsProducerId getPreviousMaterialsProducerId() {
		return previousMaterialsProducerId;
	}

	public MaterialsProducerId getCurrentMaterialsProducerId() {
		return currentMaterialsProducerId;
	}

	@Override
	public String toString() {
		return "StageMaterialsProducerChangeObservationEvent [stageId=" + stageId + ", previousMaterialsProducerId=" + previousMaterialsProducerId + ", currentMaterialsProducerId=" + currentMaterialsProducerId + "]";
	}

	private static enum LabelerId implements EventLabelerId {
		SOURCE, DESTINATION, STAGE, ALL
	}

	private static void validateStageId(Context context, StageId stageId) {
		if (stageId == null) {
			context.throwContractException(MaterialsError.NULL_STAGE_ID);
		}
		MaterialsDataView materialsDataView = context.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.stageExists(stageId)) {
			context.throwContractException(MaterialsError.UNKNOWN_STAGE_ID);
		}
	}

	private static void validateMaterialsProducerId(Context context, MaterialsProducerId materialsProducerId) {
		if (materialsProducerId == null) {
			context.throwContractException(MaterialsError.NULL_MATERIALS_PRODUCER_ID);
		}
		MaterialsDataView materialsDataView = context.getDataView(MaterialsDataView.class).get();
		if (!materialsDataView.materialsProducerIdExists(materialsProducerId)) {
			context.throwContractException(MaterialsError.UNKNOWN_MATERIALS_PRODUCER_ID);
		}
	}

	public static EventLabel<StageMaterialsProducerChangeObservationEvent> getEventLabelByDestination(Context context, MaterialsProducerId destinationMaterialsProducerId) {
		validateMaterialsProducerId(context, destinationMaterialsProducerId);
		return new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.DESTINATION, StageMaterialsProducerChangeObservationEvent.class, destinationMaterialsProducerId);
	}

	public static EventLabeler<StageMaterialsProducerChangeObservationEvent> getEventLabelerForDestination() {
		return new SimpleEventLabeler<>(LabelerId.DESTINATION, StageMaterialsProducerChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.DESTINATION, StageMaterialsProducerChangeObservationEvent.class, event.getCurrentMaterialsProducerId()));
	}

	public static EventLabel<StageMaterialsProducerChangeObservationEvent> getEventLabelBySource(Context context, MaterialsProducerId sourceMaterialsProducerId) {
		validateMaterialsProducerId(context, sourceMaterialsProducerId);
		return new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.SOURCE, StageMaterialsProducerChangeObservationEvent.class, sourceMaterialsProducerId);
	}

	public static EventLabeler<StageMaterialsProducerChangeObservationEvent> getEventLabelerForSource() {
		return new SimpleEventLabeler<>(LabelerId.SOURCE, StageMaterialsProducerChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.SOURCE, StageMaterialsProducerChangeObservationEvent.class, event.getPreviousMaterialsProducerId()));
	}

	public static EventLabel<StageMaterialsProducerChangeObservationEvent> getEventLabelByStage(Context context, StageId stageId) {
		validateStageId(context, stageId);
		return new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.STAGE, StageMaterialsProducerChangeObservationEvent.class, stageId);
	}

	public static EventLabeler<StageMaterialsProducerChangeObservationEvent> getEventLabelerForStage() {
		return new SimpleEventLabeler<>(LabelerId.STAGE, StageMaterialsProducerChangeObservationEvent.class, (context, event) -> new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.STAGE, StageMaterialsProducerChangeObservationEvent.class, event.getStageId()));
	}

	private final static EventLabel<StageMaterialsProducerChangeObservationEvent> ALL_LABEL = new MultiKeyEventLabel<>(StageMaterialsProducerChangeObservationEvent.class, LabelerId.ALL, StageMaterialsProducerChangeObservationEvent.class);

	public static EventLabel<StageMaterialsProducerChangeObservationEvent> getEventLabelByAll(Context context) {
		return ALL_LABEL;
	}

	public static EventLabeler<StageMaterialsProducerChangeObservationEvent> getEventLabelerForAll() {
		return new SimpleEventLabeler<>(LabelerId.ALL, StageMaterialsProducerChangeObservationEvent.class, (context, event) -> ALL_LABEL);
	}

}
