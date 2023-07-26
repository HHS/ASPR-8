package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import plugins.people.support.PersonId;
import plugins.stochastics.support.RandomNumberGeneratorId;
import plugins.stochastics.testsupport.TestRandomGeneratorId;
import util.annotations.UnitTestMethod;

public class AT_GroupSampler {

	@Test
	@UnitTestMethod(target = GroupSampler.Builder.class, name = "build", args = {})
	public void testBuild() {
		// Test covered by other tests in this class
		GroupSampler groupSampler = GroupSampler.builder().build();
		assertNotNull(groupSampler);
	}

	@Test
	@UnitTestMethod(target = GroupSampler.class, name = "builder", args = {})
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

	@Test
	@UnitTestMethod(target = GroupSampler.class, name = "getExcludedPerson", args = {})
	public void testGetExcludedPerson() {
		GroupSampler groupSampler = GroupSampler.builder().setExcludedPersonId(new PersonId(67)).build();
		assertNotNull(groupSampler);
		assertNotNull(groupSampler.getExcludedPerson());
		assertTrue(groupSampler.getExcludedPerson().isPresent());
		assertEquals(67, groupSampler.getExcludedPerson().get().getValue());
	}

	@Test
	@UnitTestMethod(target = GroupSampler.class, name = "getRandomNumberGeneratorId", args = {})
	public void testGetRandomNumberGeneratorId() {
		GroupSampler groupSampler = GroupSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.DASHER).setRandomNumberGeneratorId(TestRandomGeneratorId.VIXEN).build();

		assertNotNull(groupSampler);
		assertNotNull(groupSampler.getRandomNumberGeneratorId());
		assertTrue(groupSampler.getRandomNumberGeneratorId().isPresent());
		assertEquals(TestRandomGeneratorId.VIXEN, groupSampler.getRandomNumberGeneratorId().get());
	}

	@Test
	@UnitTestMethod(target = GroupSampler.class, name = "getWeightingFunction", args = {})
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

	@Test
	@UnitTestMethod(target = GroupSampler.Builder.class, name = "setRandomNumberGeneratorId", args = { RandomNumberGeneratorId.class })
	public void testSetRandomNumberGeneratorId() {
		GroupSampler groupSampler = GroupSampler.builder().build();

		assertNotNull(groupSampler);
		// Show that when not set, the RandomNumberGeneratorId is not present.
		assertNotNull(groupSampler.getRandomNumberGeneratorId());
		assertFalse(groupSampler.getRandomNumberGeneratorId().isPresent());

		// Show that when set, the RandomNumberGeneratorId is Present and set to
		// what we
		// set it to
		RandomNumberGeneratorId expectedValue = TestRandomGeneratorId.DASHER;
		groupSampler = GroupSampler.builder().setRandomNumberGeneratorId(TestRandomGeneratorId.DASHER).build();
		assertNotNull(groupSampler.getRandomNumberGeneratorId());
		assertTrue(groupSampler.getRandomNumberGeneratorId().isPresent());
		assertEquals(expectedValue, groupSampler.getRandomNumberGeneratorId().get());
	}

	@Test
	@UnitTestMethod(target = GroupSampler.Builder.class, name = "setExcludedPersonId", args = { PersonId.class })
	public void testSetExcludedPersonId() {
		GroupSampler groupSampler = GroupSampler.builder().build();

		assertNotNull(groupSampler);
		// Show that when not set, the ExcludedPerson is not present.
		assertNotNull(groupSampler.getExcludedPerson());
		assertFalse(groupSampler.getExcludedPerson().isPresent());

		// Show that when set, the ExcludedPerson is Present and set to what we
		// set it
		// to
		PersonId expectedValue = new PersonId(68);
		groupSampler = GroupSampler.builder().setExcludedPersonId(expectedValue).build();
		assertNotNull(groupSampler.getExcludedPerson());
		assertTrue(groupSampler.getExcludedPerson().isPresent());
		assertEquals(expectedValue, groupSampler.getExcludedPerson().get());
	}

	@Test
	@UnitTestMethod(target = GroupSampler.Builder.class, name = "setGroupWeightingFunction", args = { GroupWeightingFunction.class })
	public void testSetGroupWeightingFunction() {

		GroupSampler groupSampler = GroupSampler.builder().build();

		assertNotNull(groupSampler);

		// Show that when not set, the weighting function is not present
		assertNotNull(groupSampler.getWeightingFunction());
		assertFalse(groupSampler.getWeightingFunction().isPresent());

		double expectedValue = 12.8;
		groupSampler = GroupSampler.builder().setGroupWeightingFunction((context, personId, groupId) -> expectedValue).build();

		assertNotNull(groupSampler);

		// Show that when set, the weighting function is present and is set to
		// the
		// expected value
		assertNotNull(groupSampler.getWeightingFunction());
		assertTrue(groupSampler.getWeightingFunction().isPresent());
		GroupWeightingFunction groupWeightingFunction = groupSampler.getWeightingFunction().get();
		assertNotNull(groupWeightingFunction);

		assertEquals(expectedValue, groupWeightingFunction.getWeight(null, null, null), 0);
	}

}
