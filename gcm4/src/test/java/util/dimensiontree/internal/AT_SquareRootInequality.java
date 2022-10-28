package util.dimensiontree.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

@UnitTest(target = SquareRootInequality.class)
public class AT_SquareRootInequality {

	@Test
	@UnitTestMethod(name = "evaluate", args = { double.class, double.class, double.class })
	public void testEvaluate() {
		/*
		 * We calculate the truth of the inequality :sqrt(a) + sqrt(b) < sqrt(c)
		 * for random triplets, using the slower sqrt function, comparing it to
		 * fast static method.
		 * 
		 * We will need to execute quite a few examples to generate a
		 * handful(~5%) of triplets where the result will be true.
		 */

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(4010906383077881190L);
		
		for (int i = 0; i < 10_000; i++) {
			
			double a = randomGenerator.nextDouble();
			double b = randomGenerator.nextDouble();
			double c = randomGenerator.nextDouble();

			boolean expectedValue = FastMath.sqrt(a) + FastMath.sqrt(b) < FastMath.sqrt(c);
			boolean acturalValue = SquareRootInequality.evaluate(a, b, c);

			assertEquals(expectedValue, acturalValue);
		}
		
	}
}
