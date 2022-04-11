package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;
import plugins.materials.support.MaterialId;
import util.RandomGeneratorProvider;

@UnitTest(target = TestMaterialId.class)
public class AT_TestMaterialId {

	@Test
	@UnitTestMethod(name = "getRandomMaterialId", args = { RandomGenerator.class })
	public void testGetRandomMaterialId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(3905846017447134736L);
		for (int i = 0; i < 10; i++) {
			assertNotNull(TestMaterialId.getRandomMaterialId(randomGenerator));
		}
	}

	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestMaterialId.values().length, TestMaterialId.size());
	}

	@Test
	@UnitTestMethod(name = "next", args = {})
	public void testNext() {
		TestMaterialId[] values = TestMaterialId.values();
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[(i + 1) % values.length], values[i].next());
		}
	}

	@Test
	@UnitTestMethod(name = "getUnknownMaterialId", args = {})
	public void testGetUnknownMaterialId() {
		MaterialId unknownMaterialId = TestMaterialId.getUnknownMaterialId();
		assertNotNull(unknownMaterialId);
		MaterialId unknownMaterialId2 = TestMaterialId.getUnknownMaterialId();
		assertNotEquals(unknownMaterialId, unknownMaterialId2);
	}

}
