package gov.hhs.aspr.ms.gcm.simulation.plugins.resources.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.simulation.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportPeriod;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.support.ResourceId;
import gov.hhs.aspr.ms.gcm.simulation.plugins.resources.testsupport.TestResourceId;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_ResourceReportPluginData {

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		ResourceReportPluginData.builder()//
				.setReportLabel(new SimpleReportLabel(getClass()))//
				.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		ResourceReportPluginData.builder()//
				.setReportPeriod(ReportPeriod.DAILY)//
				.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, resourceReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setReportPeriod", args = {
			ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, resourceReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "setDefaultInclusion", args = {
			boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, resourceReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "includeResource", args = {
			ResourceId.class })
	public void testIncludeResource() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(resourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().includeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.Builder.class, name = "excludeResource", args = {
			ResourceId.class })
	public void testExcludeResource() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(resourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// precondition: if the person property id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourceReportPluginData.builder().excludeResource(null);
		});
		assertEquals(ResourceError.NULL_RESOURCE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData.builder()//
							.setReportPeriod(ReportPeriod.DAILY)//
							.setReportLabel(expectedReportLabel)//
							.build();

			assertEquals(expectedReportLabel, resourceReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			ResourceReportPluginData resourceReportPluginData = //
					ResourceReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel)//
							.build();

			assertEquals(reportPeriod, resourceReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getIncludedResourceIds", args = {})
	public void testGetIncludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-inclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(resourceReportPluginData.getIncludedResourceIds().isEmpty());

		// show that inclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

		// show that inclusion will override exclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
			builder.includeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getIncludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getExcludedResourceIds", args = {})
	public void testGetExcludedResourceIds() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default is non-exclusion
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertTrue(resourceReportPluginData.getExcludedResourceIds().isEmpty());

		// show that exclusion alone works
		Set<ResourceId> expectedResourceIds = new LinkedHashSet<>();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

		// show that exclusion will override inclusion
		expectedResourceIds.clear();

		expectedResourceIds.add(TestResourceId.RESOURCE_1);
		expectedResourceIds.add(TestResourceId.RESOURCE_4);
		expectedResourceIds.add(TestResourceId.RESOURCE_2);

		builder = ResourceReportPluginData.builder()//
				.setReportPeriod(reportPeriod)//
				.setReportLabel(reportLabel);//

		for (ResourceId resourceId : expectedResourceIds) {
			builder.includeResource(resourceId);
			builder.excludeResource(resourceId);
		}

		resourceReportPluginData = builder.build();
		assertEquals(expectedResourceIds, resourceReportPluginData.getExcludedResourceIds());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		ResourceReportPluginData resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(true)//
						.build();
		assertEquals(true, resourceReportPluginData.getDefaultInclusionPolicy());

		resourceReportPluginData = //
				ResourceReportPluginData.builder()//
						.setReportPeriod(reportPeriod)//
						.setReportLabel(reportLabel)//
						.setDefaultInclusion(false)//
						.build();
		assertEquals(false, resourceReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a ResourceReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			ResourceReportPluginData.Builder builder = //
					ResourceReportPluginData.builder()//
							.setReportPeriod(reportPeriod)//
							.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includeResource(testResourceId);
				} else {
					builder.excludeResource(testResourceId);
				}
			}
			//force some values for later use
			builder.includeResource(TestResourceId.RESOURCE_1);
			builder.excludeResource(TestResourceId.RESOURCE_2);

			builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

			ResourceReportPluginData resourceReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			ResourceReportPluginData.Builder cloneBuilder = resourceReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(resourceReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// excludeResource
			cloneBuilder = resourceReportPluginData.toBuilder();
			cloneBuilder.excludeResource(TestResourceId.RESOURCE_1);
			assertNotEquals(resourceReportPluginData, cloneBuilder.build());
			
			// includeResource
			cloneBuilder = resourceReportPluginData.toBuilder();
			cloneBuilder.includeResource(TestResourceId.RESOURCE_2);
			assertNotEquals(resourceReportPluginData, cloneBuilder.build());

			// setDefaultInclusion
			cloneBuilder = resourceReportPluginData.toBuilder();
			cloneBuilder.setDefaultInclusion(!resourceReportPluginData.getDefaultInclusionPolicy());
			assertNotEquals(resourceReportPluginData, cloneBuilder.build());

			// setReportLabel
			cloneBuilder = resourceReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(resourceReportPluginData, cloneBuilder.build());

			// setReportPeriod
			cloneBuilder = resourceReportPluginData.toBuilder();
			cloneBuilder.setReportPeriod(resourceReportPluginData.getReportPeriod().next());
			assertNotEquals(resourceReportPluginData, cloneBuilder.build());

		}
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		ResourceReportPluginData pluginData = ResourceReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0))
				.setReportPeriod(ReportPeriod.DAILY)
				.build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "checkVersionSupported", args = {
			String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(ResourceReportPluginData.checkVersionSupported(version));
			assertFalse(ResourceReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(ResourceReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(ResourceReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(ResourceReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);

		// never equal to another type
		for (int i = 0; i < 30; i++) {
			ResourceReportPluginData pluginData = getRandomResourceReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(new Object()));
		}

		// never equal to null
		for (int i = 0; i < 30; i++) {
			ResourceReportPluginData pluginData = getRandomResourceReportPluginData(randomGenerator.nextLong());
			assertFalse(pluginData.equals(null));
		}

		// reflexive
		for (int i = 0; i < 30; i++) {
			ResourceReportPluginData pluginData = getRandomResourceReportPluginData(randomGenerator.nextLong());
			assertTrue(pluginData.equals(pluginData));
		}

		// symmetric, transitive, consistent
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceReportPluginData pluginData1 = getRandomResourceReportPluginData(seed);
			ResourceReportPluginData pluginData2 = getRandomResourceReportPluginData(seed);
			assertFalse(pluginData1 == pluginData2);
			for (int j = 0; j < 10; j++) {
				assertTrue(pluginData1.equals(pluginData2));
				assertTrue(pluginData2.equals(pluginData1));
			}
		}

		// different inputs yield unequal plugin datas
		Set<ResourceReportPluginData> set = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ResourceReportPluginData pluginData = getRandomResourceReportPluginData(randomGenerator.nextLong());
			set.add(pluginData);
		}
		assertEquals(100, set.size());
	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		// equal objects have equal hash codes
		for (int i = 0; i < 30; i++) {
			long seed = randomGenerator.nextLong();
			ResourceReportPluginData pluginData1 = getRandomResourceReportPluginData(seed);
			ResourceReportPluginData pluginData2 = getRandomResourceReportPluginData(seed);

			assertEquals(pluginData1, pluginData2);
			assertEquals(pluginData1.hashCode(), pluginData2.hashCode());
		}

		// hash codes are reasonably distributed
		Set<Integer> hashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 100; i++) {
			ResourceReportPluginData pluginData = getRandomResourceReportPluginData(randomGenerator.nextLong());
			hashCodes.add(pluginData.hashCode());
		}

		assertEquals(100, hashCodes.size());

	}

	@Test
	@UnitTestMethod(target = ResourceReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();
		builder.setReportLabel(new SimpleReportLabel("report label"));
		builder.setReportPeriod(ReportPeriod.DAILY);

		boolean include = false;
		for (TestResourceId testResourceId : TestResourceId.values()) {

			if (include) {
				builder.includeResource(testResourceId);
			} else {
				builder.excludeResource(testResourceId);
			}
			include = !include;
		}

		builder.setDefaultInclusion(true).build();

		ResourceReportPluginData resourceReportPluginData = builder.build();
		String actualValue = resourceReportPluginData.toString();
		String expectedValue = "ResourceReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=report label], reportPeriod=DAILY, includedResourceIds=[RESOURCE_2, RESOURCE_4], excludedResourceIds=[RESOURCE_1, RESOURCE_3, RESOURCE_5], defaultInclusionPolicy=true]]";
		assertEquals(expectedValue, actualValue);

	}

	private ResourceReportPluginData getRandomResourceReportPluginData(long seed) {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		ResourceReportPluginData.Builder builder = ResourceReportPluginData.builder();

		ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
		builder.setReportLabel(reportLabel);

		ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
		builder.setReportPeriod(reportPeriod);

		builder.setDefaultInclusion(randomGenerator.nextBoolean());

		List<TestResourceId> testResourceIds = Arrays.asList(TestResourceId.values());
		Random random = new Random(randomGenerator.nextLong());
		Collections.shuffle(testResourceIds, random);

		int n = randomGenerator.nextInt(testResourceIds.size()) + 1;
		for (int i = 0; i < n; i++) {
			TestResourceId testResourceId = testResourceIds.get(i);
			if (randomGenerator.nextBoolean()) {
				builder.includeResource(testResourceId);
			} else {
				builder.excludeResource(testResourceId);
			}
		}

		return builder.build();
	}
}
