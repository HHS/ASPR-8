package plugins.materials.reports;

import nucleus.ReportContext;
import plugins.materials.datacontainers.MaterialsDataView;
import plugins.materials.events.observation.MaterialsProducerPropertyChangeObservationEvent;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.reports.support.ReportHeader;
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
 * @author Shawn Hatch
 *
 */
public final class MaterialsProducerPropertyReport {

	private ReportHeader reportHeader;

	private ReportHeader getReportHeader() {
		if (reportHeader == null) {
			reportHeader = ReportHeader	.builder()//
										.add("Time")//
										.add("MaterialsProducer")//
										.add("Property")//
										.add("Value")//
										.build();//
		}
		return reportHeader;
	}

	private void handleMaterialsProducerPropertyChangeObservationEvent(ReportContext reportContext,MaterialsProducerPropertyChangeObservationEvent materialsProducerPropertyChangeObservationEvent) {
		MaterialsProducerId materialsProducerId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerId();
		MaterialsProducerPropertyId materialsProducerPropertyId = materialsProducerPropertyChangeObservationEvent.getMaterialsProducerPropertyId();
		Object currentPropertyValue = materialsProducerPropertyChangeObservationEvent.getCurrentPropertyValue();
		writeProperty(reportContext,materialsProducerId, materialsProducerPropertyId,currentPropertyValue);
	}

	public void init(final ReportContext reportContext) {

		reportContext.subscribe(MaterialsProducerPropertyChangeObservationEvent.class,this::handleMaterialsProducerPropertyChangeObservationEvent);

		MaterialsDataView materialsDataView = reportContext.getDataView(MaterialsDataView.class).get();
		
		for (final MaterialsProducerId materialsProducerId : materialsDataView.getMaterialsProducerIds()) {
			for (final MaterialsProducerPropertyId materialsProducerPropertyId : materialsDataView.getMaterialsProducerPropertyIds()) {
				final Object materialsProducerPropertyValue = materialsDataView.getMaterialsProducerPropertyValue(materialsProducerId, materialsProducerPropertyId);
				writeProperty(reportContext,materialsProducerId, materialsProducerPropertyId,materialsProducerPropertyValue);
			}
		}
	}

	private void writeProperty(ReportContext reportContext,final MaterialsProducerId materialsProducerId, final MaterialsProducerPropertyId materialsProducerPropertyId, Object materialsProducerPropertyValue) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportHeader(getReportHeader());
		reportItemBuilder.setReportId(reportContext.getCurrentReportId());
		reportItemBuilder.addValue(reportContext.getTime());
		reportItemBuilder.addValue(materialsProducerId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyId.toString());
		reportItemBuilder.addValue(materialsProducerPropertyValue);
		reportContext.releaseOutput(reportItemBuilder.build());
	}

}