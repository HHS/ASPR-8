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
import gov.hhs.aspr.ms.gcm.simulation.plugins.materials.reports.BatchStatusReportPluginData.Builder;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.simulation.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

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
	@UnitTestMethod(target = BatchStatusReportPluginData.Builder.class, name = "setReportLabel", args = {
			ReportLabel.class })
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
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "toBuilder", args = {})
	public void testToBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a BatchStatusReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			BatchStatusReportPluginData.Builder builder = //
					BatchStatusReportPluginData.builder()//
							.setReportLabel(reportLabel);

			BatchStatusReportPluginData batchStatusReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			Builder cloneBuilder = batchStatusReportPluginData.toBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(batchStatusReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = batchStatusReportPluginData.toBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(batchStatusReportPluginData, cloneBuilder.build());

		}
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		BatchStatusReportPluginData pluginData = BatchStatusReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0)).build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList(StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(BatchStatusReportPluginData.checkVersionSupported(version));
			assertFalse(BatchStatusReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(BatchStatusReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(BatchStatusReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(BatchStatusReportPluginData.checkVersionSupported(version + ".0.0"));
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
					batchStatusReportPluginData1.toBuilder()//
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
		 * The hash codes should be dispersed -- we only show that they are unique
		 * values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size() > 40);

	}

	@Test
	@UnitTestMethod(target = BatchStatusReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		ReportLabel reportLabel = new SimpleReportLabel("report label");
		BatchStatusReportPluginData batchStatusReportPluginData = //
				BatchStatusReportPluginData.builder()//
						.setReportLabel(reportLabel)//
						.build();
		String expectedValue = "BatchStatusReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=report label], locked=true]]";
		String actualValue = batchStatusReportPluginData.toString();
		assertEquals(expectedValue, actualValue);
	}

}
