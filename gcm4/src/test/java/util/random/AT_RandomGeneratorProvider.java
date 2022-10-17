package util.random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

@UnitTest(target = RandomGeneratorProvider.class)
public class AT_RandomGeneratorProvider {

	@Test
	@UnitTestMethod(name = "getRandomGenerator", args = { long.class })
	public void testGetRandomGenerator() {
		//show that a random generator is returned and that each is different, but repeatable
		Set<Long> initialValues = new LinkedHashSet<>();		
		for (long seed = 0L; seed < 10L; seed++) {
			RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
			assertNotNull(randomGenerator);
			boolean added = initialValues.add(randomGenerator.nextLong());
			assertTrue(added);
			
			RandomGenerator duplicateRandomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);
			assertNotNull(duplicateRandomGenerator);
			added = initialValues.add(duplicateRandomGenerator.nextLong());
			assertFalse(added);
			
		}
	}
}
