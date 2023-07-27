package gov.hhs.aspr.ms.gcm.lessons.plugins.model.reports;

import java.util.List;

import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Material;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.MaterialsProducer;
import gov.hhs.aspr.ms.gcm.lessons.plugins.model.support.Resource;
import gov.hhs.aspr.ms.gcm.nucleus.ReportContext;
import gov.hhs.aspr.ms.gcm.plugins.materials.datamangers.MaterialsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.materials.events.StageOfferUpdateEvent;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.BatchId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.MaterialsProducerId;
import gov.hhs.aspr.ms.gcm.plugins.materials.support.StageId;
import gov.hhs.aspr.ms.gcm.plugins.people.support.PersonId;
import gov.hhs.aspr.ms.gcm.plugins.regions.datamanagers.RegionsDataManager;
import gov.hhs.aspr.ms.gcm.plugins.regions.support.RegionId;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.PeriodicReport;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportHeader;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportItem;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.plugins.resources.datamanagers.ResourcesDataManager;

public final class VaccineProductionReport extends PeriodicReport {

	private ReportHeader reportHeader;
	private MaterialsDataManager materialsDataManager;
	private RegionsDataManager regionsDataManager;
	private ResourcesDataManager resourcesDataManager;

	public VaccineProductionReport(final ReportLabel reportLabel, final ReportPeriod reportPeriod) {
		super(reportLabel, reportPeriod);

	}

	private static enum MaterialMode {
		INV, STAGED
	}

	private String getMaterialHeader(MaterialsProducerId materialsProducerId, MaterialId materialId,
			MaterialMode materialMode) {
		String result = materialsProducerId.toString() + "_" + materialId.toString() + "_" + materialMode;
		return result.toLowerCase();
	}

	@Override
	protected void prepare(ReportContext reportContext) {

		materialsDataManager = reportContext.getDataManager(MaterialsDataManager.class);
		resourcesDataManager = reportContext.getDataManager(ResourcesDataManager.class);
		regionsDataManager = reportContext.getDataManager(RegionsDataManager.class);

		final ReportHeader.Builder reportHeaderBuilder = ReportHeader.builder();
		addTimeFieldHeaders(reportHeaderBuilder);//

		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.ANTIGEN_PRODUCER, Material.VIRUS, MaterialMode.INV));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.ANTIGEN_PRODUCER, Material.GROWTH_MEDIUM, MaterialMode.INV));

		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.ANTIGEN_PRODUCER, Material.VIRUS, MaterialMode.STAGED));
		reportHeaderBuilder.add(
				getMaterialHeader(MaterialsProducer.ANTIGEN_PRODUCER, Material.GROWTH_MEDIUM, MaterialMode.STAGED));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.ANTIGEN_PRODUCER, Material.ANTIGEN, MaterialMode.STAGED));

		reportHeaderBuilder.add("antigen production");

		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.ANTIGEN, MaterialMode.INV));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.ADJUVANT, MaterialMode.INV));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.PRESERVATIVE, MaterialMode.INV));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.STABILIZER, MaterialMode.INV));

		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.ANTIGEN, MaterialMode.STAGED));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.ADJUVANT, MaterialMode.STAGED));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.PRESERVATIVE, MaterialMode.STAGED));
		reportHeaderBuilder
				.add(getMaterialHeader(MaterialsProducer.VACCINE_PRODUCER, Material.STABILIZER, MaterialMode.STAGED));

		String header = MaterialsProducer.VACCINE_PRODUCER + "_" + "vaccine";
		header = header.toLowerCase();
		reportHeaderBuilder.add(header);

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			header = regionId + "_" + "vaccine";
			header = header.toLowerCase();
			reportHeaderBuilder.add(header);
		}
		header = "vaccine_people";
		reportHeaderBuilder.add(header);

		reportHeader = reportHeaderBuilder.build();

		reportContext.subscribe(StageOfferUpdateEvent.class, this::handleStageOfferUpdateEvent);

	}

	private double periodAntigenProduction;

	private void handleStageOfferUpdateEvent(ReportContext reportContext, StageOfferUpdateEvent stageOfferUpdateEvent) {
		if (stageOfferUpdateEvent.currentOfferState()) {
			StageId stageId = stageOfferUpdateEvent.stageId();
			MaterialsProducerId materialsProducerId = materialsDataManager.getStageProducer(stageId);
			if (materialsProducerId.equals(MaterialsProducer.ANTIGEN_PRODUCER)) {
				for (BatchId batchId : materialsDataManager.getStageBatches(stageId)) {
					MaterialId batchMaterial = materialsDataManager.getBatchMaterial(batchId);
					if (batchMaterial.equals(Material.ANTIGEN)) {
						double batchAmount = materialsDataManager.getBatchAmount(batchId);
						periodAntigenProduction += batchAmount;
					}
				}
			}
		}
	}

	private double getMaterialInventory(MaterialsProducerId materialsProducerId, MaterialId materialId) {
		List<BatchId> batches = materialsDataManager.getInventoryBatchesByMaterialId(materialsProducerId, materialId);
		double result = 0;
		for (BatchId batchId : batches) {
			result += materialsDataManager.getBatchAmount(batchId);
		}
		return result;
	}

	private double getStagedMaterialInventory(MaterialsProducerId materialsProducerId, MaterialId materialId) {
		List<StageId> stages = materialsDataManager.getStages(materialsProducerId);

		double result = 0;
		for (StageId stageId : stages) {
			List<BatchId> batches = materialsDataManager.getStageBatchesByMaterialId(stageId, materialId);
			for (BatchId batchId : batches) {
				result += materialsDataManager.getBatchAmount(batchId);
			}
		}
		return result;
	}

	private long getVaccineCountDistributedToPeople() {
		long result = 0;
		for (PersonId personId : resourcesDataManager.getPeopleWithResource(Resource.VACCINE)) {
			result += resourcesDataManager.getPersonResourceLevel(Resource.VACCINE, personId);
		}
		return result;
	}

	@Override
	protected void flush(final ReportContext reportContext) {
		final ReportItem.Builder reportItemBuilder = ReportItem.builder();
		reportItemBuilder.setReportLabel(getReportLabel());
		reportItemBuilder.setReportHeader(reportHeader);
		fillTimeFields(reportItemBuilder);

		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.ANTIGEN_PRODUCER, Material.VIRUS));
		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.ANTIGEN_PRODUCER, Material.GROWTH_MEDIUM));

		reportItemBuilder.addValue(getStagedMaterialInventory(MaterialsProducer.ANTIGEN_PRODUCER, Material.VIRUS));
		reportItemBuilder
				.addValue(getStagedMaterialInventory(MaterialsProducer.ANTIGEN_PRODUCER, Material.GROWTH_MEDIUM));
		reportItemBuilder.addValue(getStagedMaterialInventory(MaterialsProducer.ANTIGEN_PRODUCER, Material.ANTIGEN));
		reportItemBuilder.addValue(periodAntigenProduction);
		periodAntigenProduction = 0;

		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.ANTIGEN));
		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.ADJUVANT));
		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.PRESERVATIVE));
		reportItemBuilder.addValue(getMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.STABILIZER));

		reportItemBuilder.addValue(getStagedMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.ANTIGEN));
		reportItemBuilder.addValue(getStagedMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.ADJUVANT));
		reportItemBuilder
				.addValue(getStagedMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.PRESERVATIVE));
		reportItemBuilder.addValue(getStagedMaterialInventory(MaterialsProducer.VACCINE_PRODUCER, Material.STABILIZER));

		reportItemBuilder.addValue(materialsDataManager
				.getMaterialsProducerResourceLevel(MaterialsProducer.VACCINE_PRODUCER, Resource.VACCINE));

		for (RegionId regionId : regionsDataManager.getRegionIds()) {
			reportItemBuilder.addValue(resourcesDataManager.getRegionResourceLevel(regionId, Resource.VACCINE));
		}
		reportItemBuilder.addValue(getVaccineCountDistributedToPeople());

		final ReportItem reportItem = reportItemBuilder.build();
		reportContext.releaseOutput(reportItem);
	}

}
