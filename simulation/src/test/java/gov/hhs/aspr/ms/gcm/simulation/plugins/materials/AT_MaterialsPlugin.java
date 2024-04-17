package gov.hhs.aspr.ms.gcm.simulation.plugins.materials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.simulation.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.datamangers.MaterialsPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.BatchStatusReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerPropertyReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.MaterialsProducerResourceReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.StageReportPluginData;
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.support.MaterialsError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.RegionsPluginId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.ResourcesPluginId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public class AT_MaterialsPlugin {

	@Test
	@UnitTestMethod(target = MaterialsPlugin.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(MaterialsPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "getMaterialsPlugin", args = {})
	public void testGetMaterialsPlugin() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		Plugin materialsPlugin = MaterialsPlugin.builder().setMaterialsPluginData(materialsPluginData)
				.getMaterialsPlugin();

		assertEquals(1, materialsPlugin.getPluginDatas().size());
		assertTrue(materialsPlugin.getPluginDatas().contains(materialsPluginData));

		assertEquals(MaterialsPluginId.PLUGIN_ID, materialsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();

		expectedDependencies.add(RegionsPluginId.PLUGIN_ID);
		expectedDependencies.add(ResourcesPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, materialsPlugin.getPluginDependencies());

		// precondition test: if the materials plugin data is null
		ContractException contractException = assertThrows(ContractException.class,
				() -> MaterialsPlugin.builder().getMaterialsPlugin());
		assertEquals(MaterialsError.NULL_MATERIALS_PLUGIN_DATA, contractException.getErrorType());

	}

	
	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "setBatchStatusReportPluginData", args = {BatchStatusReportPluginData.class})
	public void testSetBatchStatusReportPluginData() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		
		BatchStatusReportPluginData batchStatusReportPluginData = BatchStatusReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("BatchStatusReport"))//
				.build();
		
		Plugin materialsPlugin = MaterialsPlugin.builder()//
				.setMaterialsPluginData(materialsPluginData)//
				.setBatchStatusReportPluginData(batchStatusReportPluginData)//
				.getMaterialsPlugin();//

		assertTrue(materialsPlugin.getPluginDatas().contains(batchStatusReportPluginData));
	}

	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "setMaterialsPluginData", args = {MaterialsPluginData.class})
	public void testSetMaterialsPluginData() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		
		Plugin materialsPlugin = MaterialsPlugin.builder()//
				.setMaterialsPluginData(materialsPluginData)//		
				.getMaterialsPlugin();//

		assertTrue(materialsPlugin.getPluginDatas().contains(materialsPluginData));
	}

	
	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "setMaterialsProducerPropertyReportPluginData", args = {MaterialsProducerPropertyReportPluginData.class})
	public void testSetMaterialsProducerPropertyReportPluginData() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		
		MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = MaterialsProducerPropertyReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("MaterialsProducerPropertyReport"))//				
				.build();
		
		Plugin materialsPlugin = MaterialsPlugin.builder()//
				.setMaterialsPluginData(materialsPluginData)//
				.setMaterialsProducerPropertyReportPluginData(materialsProducerPropertyReportPluginData)//
				.getMaterialsPlugin();//

		assertTrue(materialsPlugin.getPluginDatas().contains(materialsProducerPropertyReportPluginData));
	}
	
	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "setMaterialsProducerResourceReportPluginData", args = {MaterialsProducerResourceReportPluginData.class})
	public void testSetMaterialsProducerResourceReportPluginData() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		
		MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = MaterialsProducerResourceReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("MaterialsProducerResourceReport"))//				
				.build();
		
		Plugin materialsPlugin = MaterialsPlugin.builder()//
				.setMaterialsPluginData(materialsPluginData)//
				.setMaterialsProducerResourceReportPluginData(materialsProducerResourceReportPluginData)//
				.getMaterialsPlugin();//

		assertTrue(materialsPlugin.getPluginDatas().contains(materialsProducerResourceReportPluginData));
	}
	
	@Test
	@UnitTestMethod(target = MaterialsPlugin.Builder.class, name = "setStageReportPluginData", args = {StageReportPluginData.class})
	public void testSetStageReportPluginData() {
		MaterialsPluginData materialsPluginData = MaterialsPluginData.builder().build();
		
		StageReportPluginData stageReportPluginData = StageReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel("MaterialsProducerResourceReport"))//				
				.build();
		
		Plugin materialsPlugin = MaterialsPlugin.builder()//
				.setMaterialsPluginData(materialsPluginData)//
				.setStageReportPluginData(stageReportPluginData)//
				.getMaterialsPlugin();//

		assertTrue(materialsPlugin.getPluginDatas().contains(stageReportPluginData));
	}

}
