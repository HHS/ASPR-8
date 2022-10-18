package tools;

import java.util.Random;

import org.apache.commons.math3.util.FastMath;
/**
 * Produces 20 non-negative random longs. Negatives present copy/paste issues.
 * @author Shawn Hatch
 *
 */
public final class SeedGenerator {

	private SeedGenerator() {

	}

	public static void main(String[] args) {
		Random random = new Random();
		for (int i = 0; i < 20; i++) {
			System.out.println(FastMath.abs(random.nextLong())+"L");
		}
	}

}
