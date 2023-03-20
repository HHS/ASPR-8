package plugins.groups.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.support.GroupError;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.support.GroupTypeId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.ReportPeriod;
import plugins.reports.support.SimpleReportLabel;
import plugins.util.properties.PropertyError;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;
import util.wrappers.MultiKey;

public class AT_GroupPropertyReportPluginData {

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report period is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		GroupPropertyReportPluginData	.builder()//
										.setReportLabel(new SimpleReportLabel(getClass()))//
										.build());
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// precondition test: if the report label is not assigned
		contractException = assertThrows(ContractException.class, () -> //
		GroupPropertyReportPluginData	.builder()//
										.setReportPeriod(ReportPeriod.DAILY)//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GroupPropertyReportPluginData groupPropertyReportPluginData = //
					GroupPropertyReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, groupPropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "setReportPeriod", args = { ReportPeriod.class })
	public void testSetReportPeriod() {

		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			GroupPropertyReportPluginData groupPropertyReportPluginData = //
					GroupPropertyReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, groupPropertyReportPluginData.getReportPeriod());
		}

		// precondition: if the report period is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().setReportPeriod(null);
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "setDefaultInclusion", args = { boolean.class })
	public void testSetDefaultInclusion() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, groupPropertyReportPluginData.getDefaultInclusionPolicy());

		groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, groupPropertyReportPluginData.getDefaultInclusionPolicy());

		groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, groupPropertyReportPluginData.getDefaultInclusionPolicy());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "includeGroupProperty", args = { GroupPropertyId.class })
	public void testIncludeGroupProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(groupPropertyReportPluginData.getIncludedProperties(TestGroupTypeId.GROUP_TYPE_1).isEmpty());

		// show that inclusion alone works
		Set<TestGroupPropertyId> expectedGroupPropertyIds = new LinkedHashSet<>();
		Set<MultiKey> expectedMultiKeys = new LinkedHashSet<>();

		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		Set<MultiKey> actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getIncludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// show that inclusion will override exclusion

		builder = GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		groupPropertyReportPluginData = builder.build();

		actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getIncludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// precondition: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().includeGroupProperty(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition: if the group property id is null
		contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().includeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.Builder.class, name = "excludeGroupProperty", args = { GroupPropertyId.class })
	public void testExcludeGroupProperty() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(groupPropertyReportPluginData.getIncludedProperties(TestGroupTypeId.GROUP_TYPE_1).isEmpty());

		// show that exclusion alone works
		Set<TestGroupPropertyId> expectedGroupPropertyIds = new LinkedHashSet<>();
		Set<MultiKey> expectedMultiKeys = new LinkedHashSet<>();

		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		Set<MultiKey> actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getExcludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// show that exclusion will override exclusion

		builder = GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);

			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		groupPropertyReportPluginData = builder.build();

		actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getExcludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// precondition: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().excludeGroupProperty(null, TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

		// precondition: if the group property id is null
		contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData.builder().excludeGroupProperty(TestGroupTypeId.GROUP_TYPE_1, null);
		});
		assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			GroupPropertyReportPluginData groupPropertyReportPluginData = //
					GroupPropertyReportPluginData	.builder()//
													.setReportPeriod(ReportPeriod.DAILY)//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, groupPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getReportPeriod", args = {})
	public void testGetReportPeriod() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		for (ReportPeriod reportPeriod : ReportPeriod.values()) {

			GroupPropertyReportPluginData groupPropertyReportPluginData = //
					GroupPropertyReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel)//
													.build();

			assertEquals(reportPeriod, groupPropertyReportPluginData.getReportPeriod());
		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getIncludedProperties", args = {})
	public void testGetIncludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(groupPropertyReportPluginData.getIncludedProperties(TestGroupTypeId.GROUP_TYPE_1).isEmpty());

		// show that inclusion alone works
		Set<TestGroupPropertyId> expectedGroupPropertyIds = new LinkedHashSet<>();
		Set<MultiKey> expectedMultiKeys = new LinkedHashSet<>();

		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		Set<MultiKey> actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getIncludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// show that inclusion will override exclusion

		builder = GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		groupPropertyReportPluginData = builder.build();

		actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getIncludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// precondition: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData groupPropertyReportPluginData2 = GroupPropertyReportPluginData.builder().setReportLabel(reportLabel).setReportPeriod(reportPeriod).build();
			groupPropertyReportPluginData2.getIncludedProperties(null);
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getExcludedProperties", args = {})
	public void testGetExcludedProperties() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertTrue(groupPropertyReportPluginData.getIncludedProperties(TestGroupTypeId.GROUP_TYPE_1).isEmpty());

		// show that exclusion alone works
		Set<TestGroupPropertyId> expectedGroupPropertyIds = new LinkedHashSet<>();
		Set<MultiKey> expectedMultiKeys = new LinkedHashSet<>();

		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK);
		expectedGroupPropertyIds.add(TestGroupPropertyId.GROUP_PROPERTY_2_3_DOUBLE_MUTABLE_TRACK);

		GroupPropertyReportPluginData.Builder builder = GroupPropertyReportPluginData	.builder()//
																						.setReportPeriod(reportPeriod)//
																						.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		Set<MultiKey> actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getExcludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// show that exclusion will override exclusion

		builder = GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel);//

		for (TestGroupPropertyId testGroupPropertyId : expectedGroupPropertyIds) {
			builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
			builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);

			expectedMultiKeys.add(new MultiKey(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId));
		}

		groupPropertyReportPluginData = builder.build();

		actualMultiKeys = new LinkedHashSet<>();
		groupPropertyReportPluginData = builder.build();
		for (GroupTypeId groupTypeId : groupPropertyReportPluginData.getGroupTypeIds()) {
			for (GroupPropertyId groupPropertyId : groupPropertyReportPluginData.getExcludedProperties(groupTypeId)) {
				actualMultiKeys.add(new MultiKey(groupTypeId, groupPropertyId));
			}
		}

		assertEquals(expectedMultiKeys, actualMultiKeys);

		// precondition: if the group type id is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			GroupPropertyReportPluginData groupPropertyReportPluginData2 = GroupPropertyReportPluginData.builder().setReportLabel(reportLabel).setReportPeriod(reportPeriod).build();
			groupPropertyReportPluginData2.getExcludedProperties(null);
		});
		assertEquals(GroupError.NULL_GROUP_TYPE_ID, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getDefaultInclusionPolicy", args = {})
	public void testGetDefaultInclusionPolicy() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;

		// show the default value is true
		GroupPropertyReportPluginData groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.build();
		assertEquals(true, groupPropertyReportPluginData.getDefaultInclusionPolicy());

		groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(true)//
												.build();
		assertEquals(true, groupPropertyReportPluginData.getDefaultInclusionPolicy());

		groupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.setDefaultInclusion(false)//
												.build();
		assertEquals(false, groupPropertyReportPluginData.getDefaultInclusionPolicy());
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		ReportPeriod reportPeriod = ReportPeriod.DAILY;
		TestGroupPropertyId propertyId1 = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
		TestGroupPropertyId propertyId2 = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
		// show the default value is true
		GroupPropertyReportPluginData filledGroupPropertyReportPluginData = //
				GroupPropertyReportPluginData	.builder()//
												.setReportPeriod(reportPeriod)//
												.setReportLabel(reportLabel)//
												.includeGroupProperty(propertyId1.getTestGroupTypeId(), propertyId1)//
												.excludeGroupProperty(propertyId2.getTestGroupTypeId(), propertyId2)//
												.setDefaultInclusion(false)//
												.build();//

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledGroupPropertyReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		// the report period is not set
		contractException = assertThrows(ContractException.class, () -> {
			filledGroupPropertyReportPluginData	.getEmptyBuilder()//
												.setReportLabel(new SimpleReportLabel("report label"))//
												.build();
		});
		assertEquals(ReportError.NULL_REPORT_PERIOD, contractException.getErrorType());

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");
		reportPeriod = ReportPeriod.END_OF_SIMULATION;

		GroupPropertyReportPluginData groupPropertyReportPluginData1 = //
				filledGroupPropertyReportPluginData	.getEmptyBuilder()//
													.setReportLabel(reportLabel)//
													.setReportPeriod(reportPeriod)//
													.build();
		GroupPropertyReportPluginData groupPropertyReportPluginData2 = //
				GroupPropertyReportPluginData	.builder()//
												.setReportLabel(reportLabel)//
												.setReportPeriod(reportPeriod)//
												.build();

		assertEquals(groupPropertyReportPluginData1, groupPropertyReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);

		for (int i = 0; i < 10; i++) {

			// build a GroupPropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());
			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];

			GroupPropertyReportPluginData.Builder builder = //
					GroupPropertyReportPluginData	.builder()//
													.setReportPeriod(reportPeriod)//
													.setReportLabel(reportLabel);

			for (int j = 0; j < 10; j++) {
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				} else {
					builder.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				}
			}

			builder.setDefaultInclusion(randomGenerator.nextBoolean()).build();

			GroupPropertyReportPluginData groupPropertyReportPluginData = builder.build();

			// create the clone builder and have it build
			GroupPropertyReportPluginData cloneGroupPropertyReportPluginData = groupPropertyReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(groupPropertyReportPluginData, cloneGroupPropertyReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a GroupPropertyReportPluginData from the same random
			// inputs
			GroupPropertyReportPluginData.Builder builder1 = GroupPropertyReportPluginData.builder();
			GroupPropertyReportPluginData.Builder builder2 = GroupPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			for (int j = 0; j < 10; j++) {
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
					builder2.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				} else {
					builder1.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
					builder2.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			GroupPropertyReportPluginData groupPropertyReportPluginData1 = builder1.build();
			GroupPropertyReportPluginData groupPropertyReportPluginData2 = builder2.build();

			assertEquals(groupPropertyReportPluginData1, groupPropertyReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the default inclusion
			groupPropertyReportPluginData2 = //
					groupPropertyReportPluginData1	.getCloneBuilder()//
													.setDefaultInclusion(!defaultInclusion)//
													.build();
			assertNotEquals(groupPropertyReportPluginData2, groupPropertyReportPluginData1);

			// change the report period
			int ord = reportPeriod.ordinal() + 1;
			ord = ord % ReportPeriod.values().length;
			reportPeriod = ReportPeriod.values()[ord];
			groupPropertyReportPluginData2 = //
					groupPropertyReportPluginData1	.getCloneBuilder()//
													.setReportPeriod(reportPeriod)//
													.build();
			assertNotEquals(groupPropertyReportPluginData2, groupPropertyReportPluginData1);

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			groupPropertyReportPluginData2 = //
					groupPropertyReportPluginData1	.getCloneBuilder()//
													.setReportLabel(reportLabel)//
													.build();
			assertNotEquals(groupPropertyReportPluginData2, groupPropertyReportPluginData1);

			// change an included property id
			for (GroupTypeId groupTypeId : groupPropertyReportPluginData1.getGroupTypeIds()) {
				if (!groupPropertyReportPluginData1.getIncludedProperties(groupTypeId).isEmpty()) {
					GroupPropertyId groupPropertyId = groupPropertyReportPluginData1.getIncludedProperties(groupTypeId).iterator().next();
					groupPropertyReportPluginData2 = //
							groupPropertyReportPluginData1	.getCloneBuilder()//
															.excludeGroupProperty(groupTypeId, groupPropertyId)//
															.build();
					assertNotEquals(groupPropertyReportPluginData2, groupPropertyReportPluginData1);
				}
			}
			// change an excluded property id
			for (GroupTypeId groupTypeId : groupPropertyReportPluginData1.getGroupTypeIds()) {
				if (!groupPropertyReportPluginData1.getExcludedProperties(groupTypeId).isEmpty()) {
					GroupPropertyId groupPropertyId = groupPropertyReportPluginData1.getExcludedProperties(groupTypeId).iterator().next();
					groupPropertyReportPluginData2 = //
							groupPropertyReportPluginData1	.getCloneBuilder()//
															.includeGroupProperty(groupTypeId, groupPropertyId)//
															.build();
					assertNotEquals(groupPropertyReportPluginData2, groupPropertyReportPluginData1);
				}
			}
		}

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a GroupPropertyReportPluginData from the same random
			// inputs
			GroupPropertyReportPluginData.Builder builder1 = GroupPropertyReportPluginData.builder();
			GroupPropertyReportPluginData.Builder builder2 = GroupPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);
			builder2.setReportPeriod(reportPeriod);

			for (int j = 0; j < 10; j++) {
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);
				if (randomGenerator.nextBoolean()) {
					builder1.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
					builder2.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				} else {
					builder1.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
					builder2.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();
			builder2.setDefaultInclusion(defaultInclusion).build();

			GroupPropertyReportPluginData groupPropertyReportPluginData1 = builder1.build();
			GroupPropertyReportPluginData groupPropertyReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = groupPropertyReportPluginData1.hashCode();
			assertEquals(hashCode, groupPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, groupPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, groupPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, groupPropertyReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(groupPropertyReportPluginData1.hashCode(), groupPropertyReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(groupPropertyReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertEquals(50, observedHashCodes.size());

	}

	@Test
	@UnitTestMethod(target = GroupPropertyReportPluginData.class, name = "getGroupTypeIds", args = { ReportLabel.class })
	public void testGetGroupTypeIds() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(626906625322362256L);

		for (int i = 0; i < 50; i++) {
			GroupPropertyReportPluginData.Builder builder1 = GroupPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);

			ReportPeriod reportPeriod = ReportPeriod.values()[randomGenerator.nextInt(ReportPeriod.values().length)];
			builder1.setReportPeriod(reportPeriod);

			Set<GroupTypeId> expectedGroupTypeIds = new LinkedHashSet<>();

			int propertyCount = randomGenerator.nextInt(3) + 1;
			for (int j = 0; j < propertyCount; j++) {
				TestGroupPropertyId testGroupPropertyId = TestGroupPropertyId.getRandomTestGroupPropertyId(randomGenerator);
				expectedGroupTypeIds.add(testGroupPropertyId.getTestGroupTypeId());
				if (randomGenerator.nextBoolean()) {
					builder1.includeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				} else {
					builder1.excludeGroupProperty(testGroupPropertyId.getTestGroupTypeId(), testGroupPropertyId);
				}
			}

			boolean defaultInclusion = randomGenerator.nextBoolean();
			builder1.setDefaultInclusion(defaultInclusion).build();

			GroupPropertyReportPluginData groupPropertyReportPluginData = builder1.build();

			assertEquals(expectedGroupTypeIds, groupPropertyReportPluginData.getGroupTypeIds());
		}

	}

}
