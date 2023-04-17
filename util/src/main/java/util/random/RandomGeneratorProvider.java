package util.random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

public class RandomGeneratorProvider {
	
	private RandomGeneratorProvider() {
	}

	public static RandomGenerator getRandomGenerator(long seed) {
		return new Well44497b(seed);
	}

}
