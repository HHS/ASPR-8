package plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.reports.support.ReportError;
import plugins.reports.support.ReportLabel;
import plugins.reports.support.SimpleReportLabel;
import util.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

public class AT_BatchStatusReportPluginData {

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		BatchStatusReportPluginData.Builder builder = BatchStatusReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		BatchStatusReportPluginData.builder()//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			BatchStatusReportPluginData batchStatusReportPluginData = //
					BatchStatusReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, batchStatusReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			BatchStatusReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			BatchStatusReportPluginData batchStatusReportPluginData = //
					BatchStatusReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, batchStatusReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "getEmptyBuilder", args = {})
	public void testGetEmptyBuilder() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");

		BatchStatusReportPluginData filledBatchStatusReportPluginData = //
				BatchStatusReportPluginData.builder()//
												.setReportLabel(reportLabel)//
												.build();

		// show that the empty builder is indeed empty

		// the report label is not set
		ContractException contractException = assertThrows(ContractException.class, () -> {
			filledBatchStatusReportPluginData.getEmptyBuilder().build();
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

		

		// After filling the report label and report period we should get the
		// same results as if starting from an empty builder
		reportLabel = new SimpleReportLabel("another label");

		BatchStatusReportPluginData batchStatusReportPluginData1 = //
				filledBatchStatusReportPluginData	.getEmptyBuilder()//
														.setReportLabel(reportLabel)//
														.build();
		BatchStatusReportPluginData batchStatusReportPluginData2 = //
				BatchStatusReportPluginData.builder()//
												.setReportLabel(reportLabel)//
												.build();

		assertEquals(batchStatusReportPluginData1, batchStatusReportPluginData2);
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a BatchStatusReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			BatchStatusReportPluginData.Builder builder = //
					BatchStatusReportPluginData.builder()//
													.setReportLabel(reportLabel);

			BatchStatusReportPluginData batchStatusReportPluginData = builder.build();

			// create the clone builder and have it build
			BatchStatusReportPluginData cloneBatchStatusReportPluginData = batchStatusReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(batchStatusReportPluginData, cloneBatchStatusReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a BatchStatusReportPluginData from the same random
			// inputs
			BatchStatusReportPluginData.Builder builder1 = BatchStatusReportPluginData.builder();
			BatchStatusReportPluginData.Builder builder2 = BatchStatusReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			BatchStatusReportPluginData batchStatusReportPluginData1 = builder1.build();
			BatchStatusReportPluginData batchStatusReportPluginData2 = builder2.build();

			assertEquals(batchStatusReportPluginData1, batchStatusReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			batchStatusReportPluginData2 = //
					batchStatusReportPluginData1	.getCloneBuilder()//
														.setReportLabel(reportLabel)//
														.build();
			assertNotEquals(batchStatusReportPluginData2, batchStatusReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a BatchStatusReportPluginData from the same random
			// inputs
			BatchStatusReportPluginData.Builder builder1 = BatchStatusReportPluginData.builder();
			BatchStatusReportPluginData.Builder builder2 = BatchStatusReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);


			BatchStatusReportPluginData batchStatusReportPluginData1 = builder1.build();
			BatchStatusReportPluginData batchStatusReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = batchStatusReportPluginData1.hashCode();
			assertEquals(hashCode, batchStatusReportPluginData1.hashCode());
			assertEquals(hashCode, batchStatusReportPluginData1.hashCode());
			assertEquals(hashCode, batchStatusReportPluginData1.hashCode());
			assertEquals(hashCode, batchStatusReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(batchStatusReportPluginData1.hashCode(), batchStatusReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(batchStatusReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size()>40);

	}

}
