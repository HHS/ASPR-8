package plugins.materials.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = TestMaterialsProducerId.class)
public class AT_TestMaterialsProducerId {

	@Test
	@UnitTestMethod(name = "getRandomMaterialsProducerId", args = { RandomGenerator.class })
	public void testGetRandomMaterialsProducerId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2141886758156469650L);
		for (int i = 0; i < 10; i++) {
			assertNotNull(TestMaterialsProducerId.getRandomMaterialsProducerId(randomGenerator));
		}
	}

	@Test
	@UnitTestMethod(name = "size", args = {})
	public void testSize() {
		assertEquals(TestMaterialsProducerId.values().length, TestMaterialId.size());
	}

	@Test
	@UnitTestMethod(name = "next", args = {})
	public void testNext() {
		TestMaterialsProducerId[] values = TestMaterialsProducerId.values();
		for (int i = 0; i < values.length; i++) {
			assertEquals(values[(i + 1) % values.length], values[i].next());
		}
	}

	@Test
	@UnitTestMethod(name = "getUnknownMaterialsProducerId", args = {})
	public void testGetUnknownMaterialsProducerId() {
		MaterialsProducerId unknownMaterialsProducerId = TestMaterialsProducerId.getUnknownMaterialsProducerId();
		assertNotNull(unknownMaterialsProducerId);
		MaterialsProducerId unknownMaterialsProducerId2 = TestMaterialsProducerId.getUnknownMaterialsProducerId();
		assertNotEquals(unknownMaterialsProducerId, unknownMaterialsProducerId2);
	}
}
