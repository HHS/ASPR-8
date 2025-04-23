package gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_MaterialsProducerPropertyReportPluginData {

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		MaterialsProducerPropertyReportPluginData.Builder builder = MaterialsProducerPropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		MaterialsProducerPropertyReportPluginData.builder()//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = //
					MaterialsProducerPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, materialsProducerPropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerPropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = //
					MaterialsProducerPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, materialsProducerPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a MaterialsProducerPropertyReportPluginData from random
			// inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			MaterialsProducerPropertyReportPluginData.Builder builder = //
					MaterialsProducerPropertyReportPluginData.builder()//
							.setReportLabel(reportLabel);

			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			MaterialsProducerPropertyReportPluginData.Builder cloneBuilder = materialsProducerPropertyReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(materialsProducerPropertyReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = materialsProducerPropertyReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(materialsProducerPropertyReportPluginData, cloneBuilder.build());


		}
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		MaterialsProducerPropertyReportPluginData pluginData = MaterialsProducerPropertyReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0)).build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(MaterialsProducerPropertyReportPluginData.checkVersionSupported(version));
			assertFalse(MaterialsProducerPropertyReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(MaterialsProducerPropertyReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(MaterialsProducerPropertyReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(MaterialsProducerPropertyReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			MaterialsProducerPropertyReportPluginData pluginData = getRandomMaterialsProducerPropertyReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			MaterialsProducerPropertyReportPluginData pluginData = getRandomMaterialsProducerPropertyReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			MaterialsProducerPropertyReportPluginData pluginData = getRandomMaterialsProducerPropertyReportPluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			MaterialsProducerPropertyReportPluginData pluginData1 = getRandomMaterialsProducerPropertyReportPluginData(seed);
			MaterialsProducerPropertyReportPluginData pluginData2 = getRandomMaterialsProducerPropertyReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<MaterialsProducerPropertyReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			MaterialsProducerPropertyReportPluginData pluginData = getRandomMaterialsProducerPropertyReportPluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			MaterialsProducerPropertyReportPluginData pluginData1 = getRandomMaterialsProducerPropertyReportPluginData(seed);
			MaterialsProducerPropertyReportPluginData pluginData2 = getRandomMaterialsProducerPropertyReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			MaterialsProducerPropertyReportPluginData pluginData = getRandomMaterialsProducerPropertyReportPluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = //
				MaterialsProducerPropertyReportPluginData.builder()//
						.setReportLabel(new SimpleReportLabel("report label"))//
						.build();
		String actualValue = materialsProducerPropertyReportPluginData.toString();
		String expectedValue = "MaterialsProducerPropertyReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=report label], locked=true]]";
		assertEquals(expectedValue, actualValue);
	}

	private MaterialsProducerPropertyReportPluginData getRandomMaterialsProducerPropertyReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		MaterialsProducerPropertyReportPluginData pluginData = MaterialsProducerPropertyReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel(randomGenerator.nextInt(Integer.MAX_VALUE)))//
				.build();

		return pluginData;
	}

}
