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
		MaterialsProducerPropertyReportPluginData	.builder()//
													.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = //
					MaterialsProducerPropertyReportPluginData	.builder()//
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
					MaterialsProducerPropertyReportPluginData	.builder()//
																.setReportLabel(expectedReportLabel)//
																.build();

			assertEquals(expectedReportLabel, materialsProducerPropertyReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a MaterialsProducerPropertyReportPluginData from random
			// inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			MaterialsProducerPropertyReportPluginData.Builder builder = //
					MaterialsProducerPropertyReportPluginData	.builder()//
																.setReportLabel(reportLabel);

			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData = builder.build();

			// create the clone builder and have it build
			MaterialsProducerPropertyReportPluginData cloneMaterialsProducerPropertyReportPluginData = materialsProducerPropertyReportPluginData.getCloneBuilder().build();

			// the result should equal the original if the clone builder was
			// initialized with the correct state
			assertEquals(materialsProducerPropertyReportPluginData, cloneMaterialsProducerPropertyReportPluginData);

		}
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a MaterialsProducerPropertyReportPluginData from the same
			// random
			// inputs
			MaterialsProducerPropertyReportPluginData.Builder builder1 = MaterialsProducerPropertyReportPluginData.builder();
			MaterialsProducerPropertyReportPluginData.Builder builder2 = MaterialsProducerPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData1 = builder1.build();
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData2 = builder2.build();

			assertEquals(materialsProducerPropertyReportPluginData1, materialsProducerPropertyReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			materialsProducerPropertyReportPluginData2 = //
					materialsProducerPropertyReportPluginData1	.getCloneBuilder()//
																.setReportLabel(reportLabel)//
																.build();
			assertNotEquals(materialsProducerPropertyReportPluginData2, materialsProducerPropertyReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerPropertyReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a MaterialsProducerPropertyReportPluginData from the same
			// random
			// inputs
			MaterialsProducerPropertyReportPluginData.Builder builder1 = MaterialsProducerPropertyReportPluginData.builder();
			MaterialsProducerPropertyReportPluginData.Builder builder2 = MaterialsProducerPropertyReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData1 = builder1.build();
			MaterialsProducerPropertyReportPluginData materialsProducerPropertyReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = materialsProducerPropertyReportPluginData1.hashCode();
			assertEquals(hashCode, materialsProducerPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerPropertyReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerPropertyReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(materialsProducerPropertyReportPluginData1.hashCode(), materialsProducerPropertyReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(materialsProducerPropertyReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size() > 40);

	}

}
