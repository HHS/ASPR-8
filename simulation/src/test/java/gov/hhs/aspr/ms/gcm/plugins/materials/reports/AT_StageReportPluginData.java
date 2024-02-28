package gov.hhs.aspr.ms.gcm.plugins.materials.reports;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
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
	@UnitTestMethod(target = StageReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

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
			StageReportPluginData.Builder cloneBuilder = stageReportPluginData.getCloneBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(stageReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = stageReportPluginData.getCloneBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(stageReportPluginData, cloneBuilder.build());

			
		}
	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a StageReportPluginData from the same random
			// inputs
			StageReportPluginData.Builder builder1 = StageReportPluginData.builder();
			StageReportPluginData.Builder builder2 = StageReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData1 = builder1.build();
			StageReportPluginData stageReportPluginData2 = builder2.build();

			assertEquals(stageReportPluginData1, stageReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			stageReportPluginData2 = //
					stageReportPluginData1.getCloneBuilder()//
							.setReportLabel(reportLabel)//
							.build();
			assertNotEquals(stageReportPluginData2, stageReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a StageReportPluginData from the same random
			// inputs
			StageReportPluginData.Builder builder1 = StageReportPluginData.builder();
			StageReportPluginData.Builder builder2 = StageReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			StageReportPluginData stageReportPluginData1 = builder1.build();
			StageReportPluginData stageReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = stageReportPluginData1.hashCode();
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());
			assertEquals(hashCode, stageReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(stageReportPluginData1.hashCode(), stageReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(stageReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are unique
		 * values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size() > 40);

	}

	@Test
	@UnitTestMethod(target = StageReportPluginData.class, name = "toString", args = {})
	public void testToString() {

		StageReportPluginData stageReportPluginData = StageReportPluginData.builder().setReportLabel(new SimpleReportLabel("some label")).build();
		
		String expectedValue = "StageReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=some label], locked=true]]";
		String actualValue = stageReportPluginData.toString();
		assertEquals(expectedValue, actualValue);

	}

}
