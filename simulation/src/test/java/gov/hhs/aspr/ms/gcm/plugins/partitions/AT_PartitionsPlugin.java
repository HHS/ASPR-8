package gov.hhs.aspr.ms.gcm.plugins.partitions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.nucleus.NucleusError;
import gov.hhs.aspr.ms.gcm.nucleus.Plugin;
import gov.hhs.aspr.ms.gcm.nucleus.PluginData;
import gov.hhs.aspr.ms.gcm.nucleus.PluginId;
import gov.hhs.aspr.ms.gcm.plugins.partitions.datamanagers.PartitionsPluginData;
import gov.hhs.aspr.ms.gcm.plugins.partitions.support.PartitionError;
import gov.hhs.aspr.ms.gcm.plugins.people.PeoplePluginId;
import gov.hhs.aspr.ms.gcm.plugins.stochastics.StochasticsPluginId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;

public final class AT_PartitionsPlugin {

	@Test
	@UnitTestMethod(target = PartitionsPlugin.Builder.class, name = "setPartitionsPluginData", args = { PartitionsPluginData.class })
	public void testSetPartitionsPluginData() {

		PartitionsPluginData partitionsPluginData1 = PartitionsPluginData.builder().build();
		PartitionsPluginData partitionsPluginData2 = PartitionsPluginData.builder().build();
		PartitionsPluginData partitionsPluginData3 = PartitionsPluginData.builder().build();

		Plugin partitionsPlugin = PartitionsPlugin.builder()//
				.setPartitionsPluginData(partitionsPluginData1)//
				.setPartitionsPluginData(partitionsPluginData2)//
				.setPartitionsPluginData(partitionsPluginData3)//
				.getPartitionsPlugin();

		List<PluginData> pluginDatas = partitionsPlugin.getPluginDatas();
		assertNotNull(pluginDatas);
		assertEquals(1,pluginDatas.size());
		PluginData pluginData = pluginDatas.get(0);		
		assertEquals(partitionsPluginData3, pluginData);

	}
	
	@Test
	@UnitTestMethod(target = PartitionsPlugin.Builder.class, name = "addPluginDependency", args = { PluginId.class })
	public void testAddPluginDependency() {

		PluginId somePluginId = new PluginId() {
		};

		Plugin partitionsPlugin = PartitionsPlugin.builder()//
				.addPluginDependency(somePluginId)//
				.setPartitionsPluginData(PartitionsPluginData.builder().build())//
				.getPartitionsPlugin();

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);
		expectedDependencies.add(somePluginId);
		
		assertEquals(expectedDependencies, partitionsPlugin.getPluginDependencies());

	}

	@Test
	@UnitTestMethod(target = PartitionsPlugin.class, name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionsPlugin.builder());
	}

	@Test
	@UnitTestMethod(target = PartitionsPlugin.Builder.class, name = "getPartitionsPlugin", args = {})
	public void testGetPartitionsPlugin() {

		PartitionsPluginData partitionsPluginData = PartitionsPluginData.builder().build();

		Plugin partitionsPlugin = PartitionsPlugin.builder()//
				.setPartitionsPluginData(partitionsPluginData)//
				.getPartitionsPlugin();

		assertEquals(1, partitionsPlugin.getPluginDatas().size());
		assertTrue(partitionsPlugin.getPluginDatas().contains(partitionsPluginData));

		assertEquals(PartitionsPluginId.PLUGIN_ID, partitionsPlugin.getPluginId());

		Set<PluginId> expectedDependencies = new LinkedHashSet<>();
		expectedDependencies.add(PeoplePluginId.PLUGIN_ID);
		expectedDependencies.add(StochasticsPluginId.PLUGIN_ID);
		assertEquals(expectedDependencies, partitionsPlugin.getPluginDependencies());
		
		//precondition test: if a plugin dependency is null		
		assertThrows(ContractException.class, ()->{
			PartitionsPlugin.builder()//
					.setPartitionsPluginData(PartitionsPluginData.builder().build())//
					.addPluginDependency(null)
					.getPartitionsPlugin();
		});
		
		//precondition test: if a plugin dependency is null		
		ContractException contractException = assertThrows(ContractException.class, ()->{
			PartitionsPlugin.builder()//
					.setPartitionsPluginData(PartitionsPluginData.builder().build())//
					.addPluginDependency(null)
					.getPartitionsPlugin();
		});
		assertEquals(NucleusError.NULL_PLUGIN_ID,contractException.getErrorType());

		//precondition test: if a plugin dependency is null		
		contractException = assertThrows(ContractException.class, ()->{
			PartitionsPlugin.builder()//
					.setPartitionsPluginData(null)//					
					.getPartitionsPlugin();
		});
		assertEquals(PartitionError.NULL_PARTITION_PLUGIN_DATA,contractException.getErrorType());

	}

}
