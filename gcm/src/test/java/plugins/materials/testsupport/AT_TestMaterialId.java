package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialId;
import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

public class AT_TestMaterialId {

	@Test
	@UnitTestMethod(target = TestMaterialId.class, name = "getRandomMaterialId", args = { RandomGenerator.class })
	public void testGetRandomMaterialId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3905846017447134736L);
		for (int i = 0; i < 10; i++) {
			assertNotNull(TestMaterialId.getRandomMaterialId(randomGenerator));
		}
	}

	@Test
	@UnitTestMethod(target = TestMaterialId.class, name = "size", args = {})
	public void testSize() {
		assertEquals(TestMaterialId.values().length, TestMaterialId.size());
	}

	@Test
	@UnitTestMethod(target = TestMaterialId.class, name = "next", args = {})
	public void testNext() {
		TestMaterialId[] values = TestMaterialId.values();
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[(i + 1) % values.length], values[i].next());
		}
	}

	@Test
	@UnitTestMethod(target = TestMaterialId.class, name = "getUnknownMaterialId", args = {})
	public void testGetUnknownMaterialId() {
		MaterialId unknownMaterialId = TestMaterialId.getUnknownMaterialId();
		assertNotNull(unknownMaterialId);
		MaterialId unknownMaterialId2 = TestMaterialId.getUnknownMaterialId();
		assertNotEquals(unknownMaterialId, unknownMaterialId2);
	}

}
