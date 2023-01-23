package plugins.materials.actors;

import nucleus.ReportContext;
import plugins.materials.dataviews.MaterialsDataView;
import plugins.materials.events.MaterialsProducerAdditionEvent;
import plugins.materials.events.MaterialsProducerPropertyUpdateEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.reports.support.ReportHeader;
import plugins.reports.support.ReportId;
import plugins.reports.support.ReportItem;

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

	private final ReportId reportId;

	public MaterialsProducerPropertyReport(ReportId reportId) {
		this.reportId = reportId;
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

		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class);

		for (final MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataView.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
			}
		}
	}

	private void handleMaterialsProducerAdditionEvent(ReportContext reportContext, MaterialsProducerAdditionEvent materialsProducerAdditionEvent) {
		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class);
		MaterialsProducerId materialsProducerId = materialsProducerAdditionEvent.getMaterialsProducerId();
		for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataView.getMaterialsProducerPropertyIds()) {
			final Object materialsProducerPropertyValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
			writeProperty(reportContext, materialsProducerId, materialsProducerPropertyId, materialsProducerPropertyValue);
		}
	}

	private void writeProperty(ReportContext reportContext, final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId,
			Object materialsProducerPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportId);
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}