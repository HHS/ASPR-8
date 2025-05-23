package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.MaterialsProducerAdditionEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsProducerPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;

/**
 * A Report that displays assigned materials producer property values over time.
 * Fields Time -- the time in days when the materials producer property was set
 * MaterialsProducer -- the materials producer identifier Property -- the region
 * property identifier Value -- the value of the region property
 */
public final class MaterialsProducerPropertyReport {

	private final ReportLabel reportLabel;

	public MaterialsProducerPropertyReport(
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData) {
		this.reportLabel = materialsProducerPropertyReportPluginData.getReportLabel();
	}

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader.builder()//
					.setReportLabel(reportLabel)//
					.add("time")//
					.add("materials_producer")//
					.add("property")//
					.add("value")//
					.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerPropertyUpdateEvent(ReportContext reportContext,
			MaterialsProducerPropertyUpdateEvent materialsProducerPropertyUpdateEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyUpdateEvent.materialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyUpdateEvent
				.materialsProducerPropertyId();
		Object currentPropertyValue = materialsProducerPropertyUpdateEvent.currentPropertyValue();
		writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, currentPropertyValue);
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerPropertyUpdateEvent.class,
				this::handleMaterialsProducerPropertyUpdateEvent);
		reportContext.subscribe(MaterialsProducerAdditionEvent.class, this::handleMaterialsProducerAdditionEvent);
		if (reportContext.stateRecordingIsScheduled()) {
			reportContext.subscribeToSimulationClose(this::recordSimulationState);
		}

		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);

		for (final MaterialsProducerId materialsProducerId : materialsDataManager.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataManager
					.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsDataManager
						.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId,
						materialsProducerPropertyValue);
			}
		}

		// release report header
		reportContext.releaseOutput(getReportHeader());
	}

	private void recordSimulationState(ReportContext reportContext) {
		MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();
		builder.setReportLabel(reportLabel);
		reportContext.releaseOutput(builder.build());
	}

	private void handleMaterialsProducerAdditionEvent(ReportContext reportContext,
			MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsDataManager materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataManager
				.getMaterialsProducerPropertyIds()) {
			final Object materialsProducerPropertyValue = materialsDataManager
					.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
			writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId,
					materialsProducerPropertyValue);
		}
	}

	private void writeProperty(ReportContext reportContext, final MaterialsProducerId materialsProducerId,
			final MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder//
				.setReportLabel(reportLabel)//
				.addValue(reportContext.getTime())//
				.addValue(materialsProducerId.toString())//
				.addValue(materialsProducerPropertyId.toString())//
				.addValue(materialsProducerPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}