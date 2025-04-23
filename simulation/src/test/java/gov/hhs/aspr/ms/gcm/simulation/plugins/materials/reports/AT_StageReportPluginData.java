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

public class AT_StageReportPluginData {

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		StageReportPluginData.Builder builder = StageReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		StageReportPluginData.builder()//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			StageReportPluginData stageReportPluginData = //
					StageReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, stageReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			StageReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			StageReportPluginData stageReportPluginData = //
					StageReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, stageReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a StageReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			StageReportPluginData.Builder builder = //
					StageReportPluginData.builder()//
							.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			StageReportPluginData.Builder cloneBuilder = stageReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(stageReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = stageReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(stageReportPluginData, cloneBuilder.build());

			
		}
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		StageReportPluginData pluginData = StageReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0)).build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(StageReportPluginData.checkVersionSupported(version));
			assertFalse(StageReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(StageReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(StageReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(StageReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			StageReportPluginData pluginData = getRandomStageReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			StageReportPluginData pluginData = getRandomStageReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			StageReportPluginData pluginData = getRandomStageReportPluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StageReportPluginData pluginData1 = getRandomStageReportPluginData(seed);
			StageReportPluginData pluginData2 = getRandomStageReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<StageReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StageReportPluginData pluginData = getRandomStageReportPluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			StageReportPluginData pluginData1 = getRandomStageReportPluginData(seed);
			StageReportPluginData pluginData2 = getRandomStageReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			StageReportPluginData pluginData = getRandomStageReportPluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "toString", args = {})
	public void testToString() {

		StageReportPluginData stageReportPluginData = StageReportPluginData.builder().setReportLabel(new SimpleReportLabel("some label")).build();
		
		String expectedValue = "StageReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=some label], locked=true]]";
		String actualValue = stageReportPluginData.toString();
		assertEquals(expectedValue, actualValue);

	}

	private StageReportPluginData getRandomStageReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		StageReportPluginData pluginData = StageReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(randomGenerator.nextInt(Integer.MAX_VALUE)))
				.build();

		return pluginData;
	}
}
