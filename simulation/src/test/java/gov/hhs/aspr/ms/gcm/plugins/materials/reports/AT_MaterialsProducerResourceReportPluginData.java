package gov.hhs.aspr.ms.gcm.plugins.materials.reports;

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

import gov.hhs.aspr.ms.gcm.nucleus.StandardVersioning;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportError;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.ReportLabel;
import gov.hhs.aspr.ms.gcm.plugins.reports.support.SimpleReportLabel;
import gov.hhs.aspr.ms.util.annotations.UnitTestMethod;
import gov.hhs.aspr.ms.util.errors.ContractException;
import gov.hhs.aspr.ms.util.random.RandomGeneratorProvider;

public class AT_MaterialsProducerResourceReportPluginData {

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "builder", args = {})
	public void testBuilder() {
		MaterialsProducerResourceReportPluginData.Builder builder = MaterialsProducerResourceReportPluginData.builder();
		assertNotNull(builder);
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.Builder.class, name = "build", args = {})
	public void testBuild() {
		// the specific capabilities are covered elsewhere

		// precondition test: if the report label is not assigned
		ContractException contractException = assertThrows(ContractException.class, () -> //
		MaterialsProducerResourceReportPluginData.builder()//
										.build());
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.Builder.class, name = "setReportLabel", args = { ReportLabel.class })
	public void testSetReportLabel() {

		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = //
					MaterialsProducerResourceReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, materialsProducerResourceReportPluginData.getReportLabel());
		}

		// precondition: if the report label is null
		ContractException contractException = assertThrows(ContractException.class, () -> {
			MaterialsProducerResourceReportPluginData.builder().setReportLabel(null);
		});
		assertEquals(ReportError.NULL_REPORT_LABEL, contractException.getErrorType());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "getReportLabel", args = {})
	public void testGetReportLabel() {
		for (int i = 0; i < 30; i++) {
			ReportLabel expectedReportLabel = new SimpleReportLabel(i);
			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = //
					MaterialsProducerResourceReportPluginData.builder()//
													.setReportLabel(expectedReportLabel)//
													.build();

			assertEquals(expectedReportLabel, materialsProducerResourceReportPluginData.getReportLabel());
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "getCloneBuilder", args = {})
	public void testGetCloneBuilder() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8951274294108578550L);
		for (int i = 0; i < 10; i++) {

			// build a MaterialsProducerResourceReportPluginData from random inputs
			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt());

			MaterialsProducerResourceReportPluginData.Builder builder = //
					MaterialsProducerResourceReportPluginData.builder()//
													.setReportLabel(reportLabel);

			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = builder.build();

			// show that the returned clone builder will build an identical instance if no
			// mutations are made
			MaterialsProducerResourceReportPluginData.Builder cloneBuilder = materialsProducerResourceReportPluginData.getCloneBuilder();
			assertNotNull(cloneBuilder);
			assertEquals(materialsProducerResourceReportPluginData, cloneBuilder.build());

			// show that the clone builder builds a distinct instance if any mutation is
			// made

			// setReportLabel
			cloneBuilder = materialsProducerResourceReportPluginData.getCloneBuilder();
			cloneBuilder.setReportLabel(new SimpleReportLabel("asdf"));
			assertNotEquals(materialsProducerResourceReportPluginData, cloneBuilder.build());


		}
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "getVersion", args = {})
	public void testGetVersion() {
		MaterialsProducerResourceReportPluginData pluginData = MaterialsProducerResourceReportPluginData.builder()
				.setReportLabel(new SimpleReportLabel(0)).build();

		assertEquals(StandardVersioning.VERSION, pluginData.getVersion());
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "checkVersionSupported", args = { String.class })
	public void testCheckVersionSupported() {
		List<String> versions = Arrays.asList("", "4.0.0", "4.1.0", StandardVersioning.VERSION);

		for (String version : versions) {
			assertTrue(MaterialsProducerResourceReportPluginData.checkVersionSupported(version));
			assertFalse(MaterialsProducerResourceReportPluginData.checkVersionSupported(version + "badVersion"));
			assertFalse(MaterialsProducerResourceReportPluginData.checkVersionSupported("badVersion"));
			assertFalse(MaterialsProducerResourceReportPluginData.checkVersionSupported(version + "0"));
			assertFalse(MaterialsProducerResourceReportPluginData.checkVersionSupported(version + ".0.0"));
		}
	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "equals", args = { Object.class })
	public void testEquals() {

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7759639255438669162L);
		for (int i = 0; i < 10; i++) {
			// build a MaterialsProducerResourceReportPluginData from the same random
			// inputs
			MaterialsProducerResourceReportPluginData.Builder builder1 = MaterialsProducerResourceReportPluginData.builder();
			MaterialsProducerResourceReportPluginData.Builder builder2 = MaterialsProducerResourceReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);

			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData1 = builder1.build();
			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData2 = builder2.build();

			assertEquals(materialsProducerResourceReportPluginData1, materialsProducerResourceReportPluginData2);

			// show that plugin datas with different inputs are not equal

			// change the report label
			reportLabel = new SimpleReportLabel(1000);
			materialsProducerResourceReportPluginData2 = //
					materialsProducerResourceReportPluginData1	.getCloneBuilder()//
														.setReportLabel(reportLabel)//
														.build();
			assertNotEquals(materialsProducerResourceReportPluginData2, materialsProducerResourceReportPluginData1);
		}

	}

	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "hashCode", args = {})
	public void testHashCode() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(9079768427072825406L);

		Set<Integer> observedHashCodes = new LinkedHashSet<>();
		for (int i = 0; i < 50; i++) {
			// build a MaterialsProducerResourceReportPluginData from the same random
			// inputs
			MaterialsProducerResourceReportPluginData.Builder builder1 = MaterialsProducerResourceReportPluginData.builder();
			MaterialsProducerResourceReportPluginData.Builder builder2 = MaterialsProducerResourceReportPluginData.builder();

			ReportLabel reportLabel = new SimpleReportLabel(randomGenerator.nextInt(100));
			builder1.setReportLabel(reportLabel);
			builder2.setReportLabel(reportLabel);


			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData1 = builder1.build();
			MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData2 = builder2.build();

			// show that the hash code is stable
			int hashCode = materialsProducerResourceReportPluginData1.hashCode();
			assertEquals(hashCode, materialsProducerResourceReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerResourceReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerResourceReportPluginData1.hashCode());
			assertEquals(hashCode, materialsProducerResourceReportPluginData1.hashCode());

			// show that equal objects have equal hash codes
			assertEquals(materialsProducerResourceReportPluginData1.hashCode(), materialsProducerResourceReportPluginData2.hashCode());

			// collect the hashcode
			observedHashCodes.add(materialsProducerResourceReportPluginData1.hashCode());
		}

		/*
		 * The hash codes should be dispersed -- we only show that they are
		 * unique values -- this is dependent on the random seed
		 */
		assertTrue(observedHashCodes.size()>40);

	}
	
	@Test
	@UnitTestMethod(target = MaterialsProducerResourceReportPluginData.class, name = "toString", args = {})
	public void testToString() {
		MaterialsProducerResourceReportPluginData materialsProducerResourceReportPluginData = //
				MaterialsProducerResourceReportPluginData.builder()//
						.setReportLabel(new SimpleReportLabel("report label"))//						
						.build();
		String actualValue = materialsProducerResourceReportPluginData.toString();	
		String expectedValue = "MaterialsProducerResourceReportPluginData [data=Data [reportLabel=SimpleReportLabel [value=report label], locked=true]]";
		assertEquals(expectedValue, actualValue);
	}


}
