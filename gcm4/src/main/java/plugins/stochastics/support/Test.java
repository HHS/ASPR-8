package plugins.stochastics.support;

import java.util.Random;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.random.Well44497b;

public class Test {

	public static void main(String[] args) {
		
		Random random = new Random(2352345345757564564L);
		System.out.println(random);
		
		random.nextLong();
		
		RandomGenerator rng = new Well44497b(2352345345757564564L);
		System.out.println(rng.toString());
		

		WellState wellState = WellState.builder().setSeed(2352345345757564564L).build();

		WellRNG cr = new WellRNG(wellState);

		/* changes internal state of cr */

		for (int i = 0; i < 10; i++) {
			System.out.println(cr.nextInt(50));
			System.out.println(cr.getWellState());
			
		}
		
		

		wellState = cr.getWellState();
		WellRNG copy = new WellRNG(wellState);

		System.out.println("\nTEST: INTEGER\n");

		for (int i = 0; i < 10; i++) {

			System.out.println("CR\t= " +

					cr.nextInt(50) + "\nCOPY\t= " +

					copy.nextInt(50) + "\n");
		}

		wellState = copy.getWellState();
		
		System.out.println(
		wellState.getVArray().length);

		WellRNG anotherCopy = new WellRNG(wellState);

		System.out.println("\nTEST: DOUBLE\n");

		for (int i = 0; i < 10; i++) {

			System.out.println("CR\t= " +

					cr.nextDouble() + "\nA_COPY\t= " +

					copy.nextDouble() + "\nANotherCOPY\t " +

					anotherCopy.nextDouble() + "\n");
		}

	}
}
