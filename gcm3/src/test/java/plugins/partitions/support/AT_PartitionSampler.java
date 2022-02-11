package plugins.partitions.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.compartments.support.CompartmentId;
import plugins.people.support.PersonId;
import plugins.regions.support.RegionId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

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

	/**
	 * Tests {@linkplain PartitionSampler#getLabelSet()
	 */
	@Test
	@UnitTestMethod(name = "getLabelSet", args = {})
	public void testGetLabelSet() {
		PartitionSampler partitionSampler = PartitionSampler.builder().setLabelSet(LabelSet	.builder()//
																							.setLabel(CompartmentId.class, "compartmentLabel")//
																							.setLabel(RegionId.class, "regionLabel").build())//
															.build();

		assertNotNull(partitionSampler);
		assertNotNull(partitionSampler.getLabelSet());
		assertTrue(partitionSampler.getLabelSet().isPresent());
		LabelSet labelSet = partitionSampler.getLabelSet().get();

		assertTrue(labelSet.getLabel(CompartmentId.class).isPresent());
		assertEquals("compartmentLabel", labelSet.getLabel(CompartmentId.class).get());
		assertTrue(labelSet.getLabel(RegionId.class).isPresent());
		assertEquals("regionLabel", labelSet.getLabel(RegionId.class).get());
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
