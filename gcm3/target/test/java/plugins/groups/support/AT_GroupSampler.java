package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test class for {@link GroupSamplerInfo}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = GroupSampler.class)
public class AT_GroupSampler {

	/**
	 * Tests {@linkplain GroupSampler#builder()
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {

		GroupSampler groupSampler = GroupSampler.builder().build();

		assertNotNull(groupSampler);

		assertNotNull(groupSampler.getExcludedPerson());
		assertFalse(groupSampler.getExcludedPerson().isPresent());

		assertNotNull(groupSampler.getWeightingFunction());
		assertFalse(groupSampler.getWeightingFunction().isPresent());

		assertNotNull(groupSampler.getRandomNumberGeneratorId());
		assertFalse(groupSampler.getRandomNumberGeneratorId().isPresent());

	}

	/**
	 * Tests {@linkplain GroupSampler#getExcludedPerson()
	 */
	@Test
	@UnitTestMethod(name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(new PersonId(67)).build();
		assertNotNull(groupSampler);
		assertNotNull(groupSampler.getExcludedPerson());
		assertTrue(groupSampler.getExcludedPerson().isPresent());
		assertEquals(67, groupSampler.getExcludedPerson().get().getValue());
	}

	/**
	 * Tests {@linkplain GroupSamplerInfo#getRandomNumberGeneratorId()
	 */
	@Test
	@UnitTestMethod(name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		GroupSampler groupSampler = GroupSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.DASHER).setRandomNumberGeneratorId(TestRandomGeneratorId.VIXEN).build();

		assertNotNull(groupSampler);
		assertNotNull(groupSampler.getRandomNumberGeneratorId());
		assertTrue(groupSampler.getRandomNumberGeneratorId().isPresent());
		assertEquals(TestRandomGeneratorId.VIXEN, groupSampler.getRandomNumberGeneratorId().get());
	}

	/**
	 * Tests {@linkplain GroupSamplerInfo#getWeightingFunction()
	 */
	@Test
	@UnitTestMethod(name = "getWeightingFunction", args = {})
	public void testGetLabelSetWeightingFunction() {

		double expectedValue = 17.5;
		GroupSampler groupSampler = GroupSampler.builder().setGroupWeightingFunction((context, personId, groupId) -> expectedValue).build();

		assertNotNull(groupSampler);
		assertNotNull(groupSampler.getWeightingFunction());
		assertTrue(groupSampler.getWeightingFunction().isPresent());
		GroupWeightingFunction groupWeightingFunction = groupSampler.getWeightingFunction().get();
		assertNotNull(groupWeightingFunction);

		assertEquals(expectedValue, groupWeightingFunction.getWeight(null, null, null), 0);
	}

}
