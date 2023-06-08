package gov.hhs.aspr.gcm.translation.protobuf.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.BatchStatusReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerPropertyReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.MaterialsProducerResourceReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.materials.input.StageReportPluginDataInput;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.people.PeopleTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.properties.PropertiesTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.regions.RegionsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.reports.ReportsTranslator;
import gov.hhs.aspr.gcm.translation.protobuf.plugins.resources.ResourcesTranslator;
import gov.hhs.aspr.translation.core.TranslationController;
import gov.hhs.aspr.translation.protobuf.core.ProtobufTranslationEngine;
import gov.hhs.aspr.translation.core.testsupport.TestResourceHelper;
import plugins.materials.MaterialsPluginData;
import plugins.materials.reports.BatchStatusReportPluginData;
import plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import plugins.materials.reports.StageReportPluginData;
import plugins.materials.testsupport.MaterialsTestPluginFactory;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestForCoverage;

public class IT_MaterialsTranslator {
	Path basePath = TestResourceHelper.getResourceDir(this.getClass());
	Path filePath = TestResourceHelper.makeTestOutputDir(basePath);

	@Test
	@UnitTestForCoverage
	public void testMaterialsTranslator() {
		String fileName = "pluginData.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslationController translatorController = TranslationController.builder()
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), MaterialsPluginData.class)

				.build();

		int numBatches = 50;
		int numStages = 10;
		int numBatchesInStage = 30;
		long seed = 524805676405822016L;

		MaterialsPluginData expectedPluginData = MaterialsTestPluginFactory.getStandardMaterialsPluginData(numBatches,
				numStages, numBatchesInStage, seed);
		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();
		MaterialsPluginData actualPluginData = translatorController.getFirstObject(MaterialsPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestForCoverage
	public void testBatchStatusReportPluginDataTranslatorSpec() {
		String fileName = "batchStatusReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslationController translatorController = TranslationController.builder()
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), BatchStatusReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), BatchStatusReportPluginData.class)
				.build();

		BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("batch status report label");

		builder.setReportLabel(reportLabel);

		BatchStatusReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();

		BatchStatusReportPluginData actualPluginData = translatorController
				.getFirstObject(BatchStatusReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);

	}

	@Test
	@UnitTestForCoverage
	public void testMaterialsProducerPropertyReportPluginDataTranslatorSpec() {
		String fileName = "materialsProducerPropertyReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslationController translatorController = TranslationController.builder()
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsProducerPropertyReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName),
						MaterialsProducerPropertyReportPluginData.class)
				.build();

		MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();
		ReportLabel reportLabel = new SimpleReportLabel("materials producer property report report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerPropertyReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);

		translatorController.readInput();

		MaterialsProducerPropertyReportPluginData actualPluginData = translatorController
				.getFirstObject(MaterialsProducerPropertyReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestForCoverage
	public void testMaterialsProducerResourceReportPluginDataTranslatorSpec() {
		String fileName = "materialsProducerResourceReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslationController translatorController = TranslationController.builder()
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), MaterialsProducerResourceReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), MaterialsProducerResourceReportPluginData.class)
				.build();

		MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("materials producer resource report label");

		builder.setReportLabel(reportLabel);

		MaterialsProducerResourceReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);
		translatorController.readInput();

		MaterialsProducerResourceReportPluginData actualPluginData = translatorController
				.getFirstObject(MaterialsProducerResourceReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);
	}

	@Test
	@UnitTestForCoverage
	public void testStageReportPluginDataTranslatorSpec() {
		String fileName = "stageReport.json";

		TestResourceHelper.createTestOutputFile(filePath, fileName);

		TranslationController translatorController = TranslationController.builder()
				.setTranslationEngineBuilder(ProtobufTranslationEngine.builder())
				.addTranslator(MaterialsTranslator.getTranslator())
				.addTranslator(ReportsTranslator.getTranslator())
				.addTranslator(PropertiesTranslator.getTranslator())
				.addTranslator(ResourcesTranslator.getTranslator())
				.addTranslator(RegionsTranslator.getTranslator())
				.addTranslator(PeopleTranslator.getTranslator())
				.addInputFilePath(filePath.resolve(fileName), StageReportPluginDataInput.class)
				.addOutputFilePath(filePath.resolve(fileName), StageReportPluginData.class)
				.build();

		StageReportPluginData.Builder builder = StageReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel("stage report label");

		builder.setReportLabel(reportLabel);

		StageReportPluginData expectedPluginData = builder.build();

		translatorController.writeOutput(expectedPluginData);
		translatorController.readInput();

		StageReportPluginData actualPluginData = translatorController.getFirstObject(StageReportPluginData.class);

		assertEquals(expectedPluginData, actualPluginData);

	}

}
