package gov.hhs.aspr.ms.gcm.simulation.plugins.regions.reports;

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
import gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.PropertyError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.support.RegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.regions.testsupport.TestRegionPropertyId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_RegionPropertyReportPluginData {

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		RegionPropertyReportPluginData.builder()//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			RegionPropertyReportPluginData regionPropertyReportPluginData = //
					RegionPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, regionPropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionPropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.Builder.class, name = "setDefaultInclusion", args = {
			boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default value is true
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, regionPropertyReportPluginData.getDefaultInclusionPolicy());

		regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, regionPropertyReportPluginData.getDefaultInclusionPolicy());

		regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()// .
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, regionPropertyReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.Builder.class, name = "includeRegionProperty", args = {
			RegionPropertyId.class })
	public void testIncludeRegionProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-inclusion
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(regionPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.includeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedRegionPropertyIds.clear();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.excludeRegionProperty(regionPropertyId);
			builder.includeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getIncludedProperties());

		// precondition: if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionPropertyReportPluginData.builder().includeRegionProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.Builder.class, name = "excludeRegionProperty", args = {
			RegionPropertyId.class })
	public void testExcludeRegionProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-exclusion
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(regionPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.excludeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedRegionPropertyIds.clear();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.includeRegionProperty(regionPropertyId);
			builder.excludeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getExcludedProperties());

		// precondition: if the region property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			RegionPropertyReportPluginData.builder().excludeRegionProperty(null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			RegionPropertyReportPluginData regionPropertyReportPluginData = //
					RegionPropertyReportPluginData.builder()//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, regionPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "getIncludedProperties", args = {})
	public void testGetIncludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-inclusion
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//

						.setReportLabel(reportLabel)//
						.build();
		assertTrue(regionPropertyReportPluginData.getIncludedProperties().isEmpty());

		// show that inclusion alone works
		Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.includeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getIncludedProperties());

		// show that inclusion will override exclusion
		expectedRegionPropertyIds.clear();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.excludeRegionProperty(regionPropertyId);
			builder.includeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getIncludedProperties());

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "getExcludedProperties", args = {})
	public void testGetExcludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default is non-exclusion
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(regionPropertyReportPluginData.getExcludedProperties().isEmpty());

		// show that exclusion alone works
		Set<RegionPropertyId> expectedRegionPropertyIds = new LinkedHashSet<>();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		RegionPropertyReportPluginData.Builder builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.excludeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getExcludedProperties());

		// show that exclusion will override inclusion
		expectedRegionPropertyIds.clear();

		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		expectedRegionPropertyIds.add(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);

		builder = RegionPropertyReportPluginData.builder()//
				.setReportLabel(reportLabel);//

		for (RegionPropertyId regionPropertyId : expectedRegionPropertyIds) {
			builder.includeRegionProperty(regionPropertyId);
			builder.excludeRegionProperty(regionPropertyId);
		}

		regionPropertyReportPluginData = builder.build();
		assertEquals(expectedRegionPropertyIds, regionPropertyReportPluginData.getExcludedProperties());

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		// show the default value is true
		RegionPropertyReportPluginData regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, regionPropertyReportPluginData.getDefaultInclusionPolicy());

		regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, regionPropertyReportPluginData.getDefaultInclusionPolicy());

		regionPropertyReportPluginData = //
				RegionPropertyReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, regionPropertyReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {

			// build a RegionPropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			RegionPropertyReportPluginData.Builder builder = //
					RegionPropertyReportPluginData.builder()//
							.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId
						.getRandomRegionPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includeRegionProperty(testRegionPropertyId);
				} else {
					builder.excludeRegionProperty(testRegionPropertyId);
				}
			}
			// force some values for later
			builder.includeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
			builder.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);

			builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

			RegionPropertyReportPluginData regionPropertyReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			RegionPropertyReportPluginData.Builder cloneBuilder = regionPropertyReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(regionPropertyReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// excludeRegionProperty
			cloneBuilder = regionPropertyReportPluginData.toBuilder();
			cloneBuilder.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
			assertNotEquals(regionPropertyReportPluginData, cloneBuilder.build());

			// includeRegionProperty
			cloneBuilder = regionPropertyReportPluginData.toBuilder();
			cloneBuilder.includeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
			assertNotEquals(regionPropertyReportPluginData, cloneBuilder.build());

			// setDefaultInclusion
			cloneBuilder = regionPropertyReportPluginData.toBuilder();
			cloneBuilder.setDefaultInclusion(!regionPropertyReportPluginData.getDefaultInclusionPolicy());
			assertNotEquals(regionPropertyReportPluginData, cloneBuilder.build());

			// setReportLabel
			cloneBuilder = regionPropertyReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(regionPropertyReportPluginData, cloneBuilder.build());
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		RegionPropertyReportPluginData pluginData = RegionPropertyReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0)).build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "checkVersionSupported", args = {
			String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(RegionPropertyReportPluginData.checkVersionSupported(version));
			assertFalse(RegionPropertyReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(RegionPropertyReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(RegionPropertyReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(RegionPropertyReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7376599865331451959L);
		for (int i = 0; i < 10; i++) {
			// build a RegionPropertyReportPluginData from the same random
			// inputs
			RegionPropertyReportPluginData.Builder builder1 = RegionPropertyReportPluginData.builder();
			RegionPropertyReportPluginData.Builder builder2 = RegionPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId
						.getRandomRegionPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeRegionProperty(testRegionPropertyId);
					builder2.includeRegionProperty(testRegionPropertyId);
				} else {
					builder1.excludeRegionProperty(testRegionPropertyId);
					builder2.excludeRegionProperty(testRegionPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			RegionPropertyReportPluginData regionPropertyReportPluginData1 = builder1.build();
			RegionPropertyReportPluginData regionPropertyReportPluginData2 = builder2.build();

			assertEquals(regionPropertyReportPluginData1, regionPropertyReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the default inclusion
			regionPropertyReportPluginData2 = //
					regionPropertyReportPluginData1.toBuilder()//
							.setDefaultInclusion(!defaultInclusion)//
							.build();
			assertNotEquals(regionPropertyReportPluginData2, regionPropertyReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			regionPropertyReportPluginData2 = //
					regionPropertyReportPluginData1.toBuilder()//
							.setReportLabel(reportLabel)//
							.build();
			assertNotEquals(regionPropertyReportPluginData2, regionPropertyReportPluginData1);

			// change an included property id
			if (!regionPropertyReportPluginData1.getIncludedProperties().isEmpty()) {
				RegionPropertyId regionPropertyId = regionPropertyReportPluginData1.getIncludedProperties().iterator()
						.next();
				regionPropertyReportPluginData2 = //
						regionPropertyReportPluginData1.toBuilder()//
								.excludeRegionProperty(regionPropertyId)//
								.build();
				assertNotEquals(regionPropertyReportPluginData2, regionPropertyReportPluginData1);
			}
			// change an excluded property id
			if (!regionPropertyReportPluginData1.getExcludedProperties().isEmpty()) {
				RegionPropertyId regionPropertyId = regionPropertyReportPluginData1.getExcludedProperties().iterator()
						.next();
				regionPropertyReportPluginData2 = //
						regionPropertyReportPluginData1.toBuilder()//
								.includeRegionProperty(regionPropertyId)//
								.build();
				assertNotEquals(regionPropertyReportPluginData2, regionPropertyReportPluginData1);
			}

		}

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8483458328908100435L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a RegionPropertyReportPluginData from the same random
			// inputs
			RegionPropertyReportPluginData.Builder builder1 = RegionPropertyReportPluginData.builder();
			RegionPropertyReportPluginData.Builder builder2 = RegionPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestRegionPropertyId testRegionPropertyId = TestRegionPropertyId
						.getRandomRegionPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeRegionProperty(testRegionPropertyId);
					builder2.includeRegionProperty(testRegionPropertyId);
				} else {
					builder1.excludeRegionProperty(testRegionPropertyId);
					builder2.excludeRegionProperty(testRegionPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			RegionPropertyReportPluginData regionPropertyReportPluginData1 = builder1.build();
			RegionPropertyReportPluginData regionPropertyReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = regionPropertyReportPluginData1.hashCode();
			assertEquals(hashCode, regionPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, regionPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, regionPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, regionPropertyReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(regionPropertyReportPluginData1.hashCode(), regionPropertyReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(regionPropertyReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are unique
		 * values -- this is dependent on the random seed
		 */
		assertEquals(50, observedHashCodes.size());

	}

	@Test
	@UnitTestMethod(target = RegionPropertyReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		RegionPropertyReportPluginData.Builder builder = //
				RegionPropertyReportPluginData.builder();//
		builder.setReportLabel(reportLabel);//
		builder.setDefaultInclusion(true);
		builder.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_1_BOOLEAN_MUTABLE);
		builder.excludeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_2_INTEGER_MUTABLE);
		builder.includeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_3_DOUBLE_MUTABLE);
		builder.includeRegionProperty(TestRegionPropertyId.REGION_PROPERTY_4_BOOLEAN_IMMUTABLE);
		RegionPropertyReportPluginData regionPropertyReportPluginData = builder.build();
		String actualValue = regionPropertyReportPluginData.toString();

		String expectedValue = "RegionPropertyReportPluginData [data=Data ["
				+ "reportLabel=SimpleReportLabel [value=report label], "
				+ "includedProperties=[REGION_PROPERTY_3_DOUBLE_MUTABLE, REGION_PROPERTY_4_BOOLEAN_IMMUTABLE], "
				+ "excludedProperties=[REGION_PROPERTY_1_BOOLEAN_MUTABLE, REGION_PROPERTY_2_INTEGER_MUTABLE], "
				+ "defaultInclusionPolicy=true]]";

		assertEquals(expectedValue, actualValue);

	}
}
