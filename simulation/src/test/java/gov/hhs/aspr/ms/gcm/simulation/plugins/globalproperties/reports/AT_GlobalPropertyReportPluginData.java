package gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.reports;

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
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.support.GlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.globalproperties.testsupport.TestGlobalPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_GlobalPropertyReportPluginData {

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		GlobalPropertyReportPluginData.builder()//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GlobalPropertyReportPluginData personPropertyReportPluginData = //
					GlobalPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, personPropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "setDefaultInclusion", args = {
			boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default value is true
		GlobalPropertyReportPluginData globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

		globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

		globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()// .
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, globalPropertyReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "includeGlobalProperty", args = {
			GlobalPropertyId.class })
	public void testIncludeGlobalProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-inclusion
		GlobalPropertyReportPluginData personPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.includeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedGlobalPropertyIds.clear();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.excludeGlobalProperty(globalPropertyId);
			builder.includeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyReportPluginData.builder().includeGlobalProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.Builder.class, name = "excludeGlobalProperty", args = {
			GlobalPropertyId.class })
	public void testExcludePersonProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-exclusion
		GlobalPropertyReportPluginData personPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.excludeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedGlobalPropertyIds.clear();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.includeGlobalProperty(globalPropertyId);
			builder.excludeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getExcludedProperties());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GlobalPropertyReportPluginData.builder().excludeGlobalProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GlobalPropertyReportPluginData globalPropertyReportPluginData = //
					GlobalPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, globalPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getIncludedProperties", args = {})
	public void testGetIncludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-inclusion
		GlobalPropertyReportPluginData personPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//

						.setReportLabel(reportLabel)//
						.build();
		assertTrue(personPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.includeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedGlobalPropertyIds.clear();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);

		builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.excludeGlobalProperty(globalPropertyId);
			builder.includeGlobalProperty(globalPropertyId);
		}

		personPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, personPropertyReportPluginData.getIncludedProperties());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getExcludedProperties", args = {})
	public void testGetExcludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-exclusion
		GlobalPropertyReportPluginData globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(globalPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<GlobalPropertyId> expectedGlobalPropertyIds = new LinkedHashSet<>();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);

		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.excludeGlobalProperty(globalPropertyId);
		}

		globalPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, globalPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedGlobalPropertyIds.clear();

		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);
		expectedGlobalPropertyIds.add(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);

		builder = GlobalPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (GlobalPropertyId globalPropertyId : expectedGlobalPropertyIds) {
			builder.includeGlobalProperty(globalPropertyId);
			builder.excludeGlobalProperty(globalPropertyId);
		}

		globalPropertyReportPluginData = builder.build();
		assertEquals(expectedGlobalPropertyIds, globalPropertyReportPluginData.getExcludedProperties());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default value is true
		GlobalPropertyReportPluginData globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

		globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, globalPropertyReportPluginData.getDefaultInclusionPolicy());

		globalPropertyReportPluginData = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, globalPropertyReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255448669162L);

		// build a GlobalPropertyReportPluginData from random inputs
		ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

		GlobalPropertyReportPluginData.Builder builder = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel);

		builder.includeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		builder.includeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_2_INTEGER_MUTABLE);
		builder.excludeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE);
		builder.excludeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_4_BOOLEAN_IMMUTABLE);

		builder.setDefaultInclusion(true).build();

		GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

		// show that the returned clone builder will build an identical instance if no
		// mutations are made
		GlobalPropertyReportPluginData.Builder cloneBuilder = globalPropertyReportPluginData.toBuilder();
		assertNotNull(cloneBuilder);
		assertEquals(globalPropertyReportPluginData, cloneBuilder.build());

		// show that the clone builder builds a distinct instance if any mutation is
		// made

		// includeGlobalProperty
		cloneBuilder = globalPropertyReportPluginData.toBuilder();
		cloneBuilder.includeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_3_DOUBLE_MUTABLE);
		assertNotEquals(globalPropertyReportPluginData, cloneBuilder.build());

		// excludeGlobalProperty
		cloneBuilder = globalPropertyReportPluginData.toBuilder();
		cloneBuilder.excludeGlobalProperty(TestGlobalPropertyId.GLOBAL_PROPERTY_1_BOOLEAN_MUTABLE);
		assertNotEquals(globalPropertyReportPluginData, cloneBuilder.build());

		// setDefaultInclusion
		cloneBuilder = globalPropertyReportPluginData.toBuilder();
		cloneBuilder.setDefaultInclusion(false);
		assertNotEquals(globalPropertyReportPluginData, cloneBuilder.build());

		// setDefaultInclusion
		cloneBuilder = globalPropertyReportPluginData.toBuilder();
		cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
		assertNotEquals(globalPropertyReportPluginData, cloneBuilder.build());

	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		ReportLabel reportLabel = new SimpleReportLabel(0);

		GlobalPropertyReportPluginData.Builder builder = //
				GlobalPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel);

		assertEquals(StandardVersioning.VERSION, builder.build().getVersion());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "checkVersionSupported", args = {
			String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(GlobalPropertyReportPluginData.checkVersionSupported(version));
			assertFalse(GlobalPropertyReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(GlobalPropertyReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(GlobalPropertyReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(GlobalPropertyReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8927105493557306870L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			GlobalPropertyReportPluginData reportPluginData = getRandomGlobalPropertyReportPluginData(
					randomGenerator.nextLong());
			assertFalse(reportPluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			GlobalPropertyReportPluginData reportPluginData = getRandomGlobalPropertyReportPluginData(
					randomGenerator.nextLong());
			assertFalse(reportPluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			GlobalPropertyReportPluginData reportPluginData = getRandomGlobalPropertyReportPluginData(
					randomGenerator.nextLong());
			assertTrue(reportPluginData.equals(reportPluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertyReportPluginData reportPluginData1 = getRandomGlobalPropertyReportPluginData(seed);
			GlobalPropertyReportPluginData reportPluginData2 = getRandomGlobalPropertyReportPluginData(seed);
			assertFalse(reportPluginData1 == reportPluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(reportPluginData1.equals(reportPluginData2));
				assertTrue(reportPluginData2.equals(reportPluginData1));
			}
		}

		// different inputs yield unequal GlobalPropertyReportPluginDatas
		Set<GlobalPropertyReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertyReportPluginData reportPluginData = getRandomGlobalPropertyReportPluginData(
					randomGenerator.nextLong());
			set.add(reportPluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8483458328908100435L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			GlobalPropertyReportPluginData reportPluginData1 = getRandomGlobalPropertyReportPluginData(seed);
			GlobalPropertyReportPluginData reportPluginData2 = getRandomGlobalPropertyReportPluginData(seed);

			assertEquals(reportPluginData1, reportPluginData2);
			assertEquals(reportPluginData1.hashCode(), reportPluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			GlobalPropertyReportPluginData reportPluginData = getRandomGlobalPropertyReportPluginData(
					randomGenerator.nextLong());
			hashCodes.add(reportPluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());
	}

	@Test
	@UnitTestMethod(target = GlobalPropertyReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {

			// build a GlobalPropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			GlobalPropertyReportPluginData.Builder builder = //
					GlobalPropertyReportPluginData.builder()//
							.setReportLabel(reportLabel);

			StringBuilder sb = new StringBuilder();

			sb.append("GlobalPropertyReportPluginData [data=").append("Data [reportLabel=").append(reportLabel)
					.append(", includedProperties=");

			Set<GlobalPropertyId> includedProperties = new LinkedHashSet<>();
			Set<GlobalPropertyId> excludedProperties = new LinkedHashSet<>();

			for (int j = 0; j < 10; j++) {
				TestGlobalPropertyId testGlobalPropertyId = TestGlobalPropertyId
						.getRandomGlobalPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includeGlobalProperty(testGlobalPropertyId);
					includedProperties.add(testGlobalPropertyId);
					excludedProperties.remove(testGlobalPropertyId);
				} else {
					builder.excludeGlobalProperty(testGlobalPropertyId);
					excludedProperties.add(testGlobalPropertyId);
					includedProperties.remove(testGlobalPropertyId);
				}
			}

			boolean defaultInclusionPolicy = randomGenerator.nextBoolean();
			builder.setDefaultInclusion(defaultInclusionPolicy).build();

			sb.append(includedProperties).append(", excludedProperties=").append(excludedProperties)
					.append(", defaultInclusionPolicy=").append(defaultInclusionPolicy).append(", locked=").append(true)
					.append("]").append("]");

			GlobalPropertyReportPluginData globalPropertyReportPluginData = builder.build();

			assertEquals(sb.toString(), globalPropertyReportPluginData.toString());

		}
	}

	private GlobalPropertyReportPluginData getRandomGlobalPropertyReportPluginData(Long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
		GlobalPropertyReportPluginData.Builder builder = GlobalPropertyReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
		builder.setReportLabel(reportLabel);

		List<TestGlobalPropertyId> testGlobalPropertyIds = TestGlobalPropertyId.getShuffledGlobalPropertyIds(randomGenerator);

		int n = randomGenerator.nextInt(6) + 1;
		for (int i = 0; i < n; i++) {
			TestGlobalPropertyId testGlobalPropertyId = testGlobalPropertyIds.get(i);

			if (randomGenerator.nextBoolean()) {
				builder.includeGlobalProperty(testGlobalPropertyId);
			} else {
				builder.excludeGlobalProperty(testGlobalPropertyId);
			}
		}

		builder.setDefaultInclusion(randomGenerator.nextBoolean());

		return builder.build();
	}
}
