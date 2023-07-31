package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link PartitionSampler}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = PartitionSampler.class)
public class AT_PartitionSampler {

	/**
	 * Tests {@linkplain PartitionSampler#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		assertNotNull(PartitionSampler.builder());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getExcludedPerson()
	 */
	@Test
	@UnitTestMethod(name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setExcludedPerson(new PersonId(67)).build();
		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getExcludedPerson());
		assertTrue(partitionSampler.getExcludedPerson().isPresent());
		assertEquals(67, partitionSampler.getExcludedPerson().get().getValue());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getRandomNumberGeneratorId()
	 */
	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.DASHER).setRandomNumberGeneratorId(TestRandomGeneratorId.VIXEN).build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getRandomNumberGeneratorId());
		assertTrue(partitionSampler.getRandomNumberGeneratorId().isPresent());
		assertEquals(TestRandomGeneratorId.VIXEN, partitionSampler.getRandomNumberGeneratorId().get());
	}
	
	private static enum Dimensions{
		DIM_1,DIM_2;
	}

	/**
	 * Tests {@linkplain PartitionSampler#getLabelSet()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSet", args = {})
	public void testGetLabelSet() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setLabelSet(LabelSet	.builder()//
																							.setLabel(Dimensions.DIM_1, "compartmentLabel")//
																							.setLabel(Dimensions.DIM_2, "regionLabel").build())//
															.build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getLabelSet());
		assertTrue(partitionSampler.getLabelSet().isPresent());
		LabelSet labelSet = partitionSampler.getLabelSet().get();

		assertTrue(labelSet.getLabel(Dimensions.DIM_1).isPresent());
		assertEquals("compartmentLabel", labelSet.getLabel(Dimensions.DIM_1).get());
		assertTrue(labelSet.getLabel(Dimensions.DIM_2).isPresent());
		assertEquals("regionLabel", labelSet.getLabel(Dimensions.DIM_2).get());
	}

	/**
	 * Tests {@linkplain PartitionSampler#getLabelSetWeightingFunction()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSetWeightingFunction", args = {})
	public void testGetLabelSetWeightingFunction() {

		double expectedValue = 17.5;

		PartitionSampler partitionSampler = PartitionSampler.builder().setLabelSetWeightingFunction((context, labelSet) -> expectedValue).build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getLabelSetWeightingFunction());
		assertTrue(partitionSampler.getLabelSetWeightingFunction().isPresent());
		LabelSetWeightingFunction labelSetWeightingFunction = partitionSampler.getLabelSetWeightingFunction().get();
		assertNotNull(labelSetWeightingFunction);

		assertEquals(expectedValue, labelSetWeightingFunction.getWeight(null, null), 0);
	}

}