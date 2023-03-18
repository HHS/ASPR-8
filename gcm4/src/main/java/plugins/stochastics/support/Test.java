package plugins.stochastics.support;

public class Test {

	public static void main(String[] args) {

		Well44497bSeed well44497bSeed = Well44497bSeed.builder().setSeed(524805676405822016L).build();

		CopyableWell44497b cr = new CopyableWell44497b(well44497bSeed);

		/* changes internal state of cr */

		for (int i = 0; i < 10; i++) {
			System.out.println(cr.nextInt(50));
		}

		well44497bSeed = cr.getWell44497bSeed();
		CopyableWell44497b copy = new CopyableWell44497b(well44497bSeed);

		System.out.println("\nTEST: INTEGER\n");

		for (int i = 0; i < 10; i++) {

			System.out.println("CR\t= " +

					cr.nextInt(50) + "\nCOPY\t= " +

					copy.nextInt(50) + "\n");
		}

		well44497bSeed = copy.getWell44497bSeed();

		CopyableWell44497b anotherCopy = new CopyableWell44497b(well44497bSeed);

		System.out.println("\nTEST: DOUBLE\n");

		for (int i = 0; i < 10; i++) {

			System.out.println("CR\t= " +

					cr.nextDouble() + "\nA_COPY\t= " +

					copy.nextDouble() + "\nANotherCOPY\t " +

					anotherCopy.nextDouble() + "\n");
		}

	}
}
