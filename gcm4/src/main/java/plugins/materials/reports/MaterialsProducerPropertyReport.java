package plugins.materials.reports;

import nucleus.ReportContext;
import nucleus.SimulationStateContext;
import plugins.materials.datamangers.MaterialsDataManager;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportItem;
import plugins.reports.support.ReportLabel;

/**
 * A Report that displays assigned materials producer property values over time.
 *
 *
 * Fields
 *
 * Time -- the time in days when the materials producer property was set
 *
 * MaterialsProducer -- the materials producer identifier
 *
 * Property -- the region property identifier
 *
 * Value -- the value of the region property
 *
 *
 */
public final class MaterialsProducerPropertyReport {

	private final ReportLabel reportLabel;

	public MaterialsProducerPropertyReport(MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData) {
		this.reportLabel = materialsProducerPropertyReportPluginData.getReportLabel();
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("time")//
										.add("materials_producer")//
										.add("property")//
										.add("value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerPropertyUpdateEvent(ReportContext reportContext, MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyUpdateEvent.materialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyUpdateEvent.materialsProducerPropertyId();
		Object currentPropertyValue = materialsProducerPropertyUpdateEvent.currentPropertyValue();
		writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, currentPropertyValue);
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerPropertyUpdateEvent.class, this::handleMaterialsProducerPropertyUpdateEvent);
		reportContext.subscribe(MaterialsProducerAdditionEvent.class, this::handleMaterialsProducerAdditionEvent);
		reportContext.subscribeToSimulationState(this::recordSimulationState);

		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);

		for (final MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataManager.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
			}
		}
	}
	
	private void recordSimulationState(ReportContext reportContext, SimulationStateContext simulationStateContext) {
		MaterialsProducerPropertyReportPluginData.Builder builder = simulationStateContext.get(MaterialsProducerPropertyReportPluginData.Builder.class);
		builder.setReportLabel(reportLabel);
	}

	private void handleMaterialsProducerAdditionEvent(ReportContext reportContext, MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataManager.getMaterialsProducerPropertyIds()) {
			final Object materialsProducerPropertyValue = materialsDataManager.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
			writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
		}
	}

	private void writeProperty(ReportContext reportContext, final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			Object materialsProducerPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportLabel(reportLabel);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}