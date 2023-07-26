package plugins.resources.reports;

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

public class AT_ResourcePropertyReportPluginData {

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		ResourcePropertyReportPluginData.Builder builder = ResourcePropertyReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		ResourcePropertyReportPluginData.builder()//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourcePropertyReportPluginData resourcePropertyReportPluginData = //
					ResourcePropertyReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, resourcePropertyReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			ResourcePropertyReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourcePropertyReportPluginData resourcePropertyReportPluginData = //
					ResourcePropertyReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, resourcePropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a ResourcePropertyReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			ResourcePropertyReportPluginData.Builder builder = //
					ResourcePropertyReportPluginData.builder()//
													.setReportLabel(reportLabel);

			ResourcePropertyReportPluginData resourcePropertyReportPluginData = builder.build();

			// create the clone builder and have it build
			ResourcePropertyReportPluginData cloneResourcePropertyReportPluginData = resourcePropertyReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(resourcePropertyReportPluginData, cloneResourcePropertyReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a ResourcePropertyReportPluginData from the same random
			// inputs
			ResourcePropertyReportPluginData.Builder builder1 = ResourcePropertyReportPluginData.builder();
			ResourcePropertyReportPluginData.Builder builder2 = ResourcePropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			ResourcePropertyReportPluginData resourcePropertyReportPluginData1 = builder1.build();
			ResourcePropertyReportPluginData resourcePropertyReportPluginData2 = builder2.build();

			assertEquals(resourcePropertyReportPluginData1, resourcePropertyReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			resourcePropertyReportPluginData2 = //
					resourcePropertyReportPluginData1	.getCloneBuilder()//
														.setReportLabel(reportLabel)//
														.build();
			assertNotEquals(resourcePropertyReportPluginData2, resourcePropertyReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a ResourcePropertyReportPluginData from the same random
			// inputs
			ResourcePropertyReportPluginData.Builder builder1 = ResourcePropertyReportPluginData.builder();
			ResourcePropertyReportPluginData.Builder builder2 = ResourcePropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);


			ResourcePropertyReportPluginData resourcePropertyReportPluginData1 = builder1.build();
			ResourcePropertyReportPluginData resourcePropertyReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = resourcePropertyReportPluginData1.hashCode();
			assertEquals(hashCode, resourcePropertyReportPluginData1.hashCode());
			assertEquals(hashCode, resourcePropertyReportPluginData1.hashCode());
			assertEquals(hashCode, resourcePropertyReportPluginData1.hashCode());
			assertEquals(hashCode, resourcePropertyReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(resourcePropertyReportPluginData1.hashCode(), resourcePropertyReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(resourcePropertyReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size()>40);

	}
	
	
 
	@Test
	@UnitTestMethod(target = ResourcePropertyReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			ResourcePropertyReportPluginData resourcePropertyReportPluginData = //
					ResourcePropertyReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			String actualValue =  resourcePropertyReportPluginData.toString();
			String expectedValue =  "ResourcePropertyReportPluginData [data=Data [reportLabel=SimpleReportLabel [value="+i+"], locked=true]]";
			
			assertEquals(expectedValue, actualValue);
		}

	}
}
