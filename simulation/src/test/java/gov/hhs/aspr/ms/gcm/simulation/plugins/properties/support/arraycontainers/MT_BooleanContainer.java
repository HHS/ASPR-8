package gov.hhs.aspr.ms.gcm.simulation.plugins.properties.support.arraycontainers;

/*
 * Manual performance and tuning test turned off until adoption of JOL or some other appropriate library for assessing memory use in post-8 java.
 */

//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.LinkedHashSet;
//import java.util.List;
//import java.util.Random;
//
//import plugins.util.properties.arraycontainers.BooleanContainer;

/**
 * Manual test class for {@link BooleanContainer}
 * 
 * A manual unit test used to determine the threshold values used to transition
 * a indexed population from using a LinkedHashSet to a BooleanContainer.
 * Analysis of the results of this test indicate that an indexed population that
 * contains less than 0.5% of the total population should generally be stored
 * using the LinkedHashSet and those have more than 1% should be stored using a
 * BooleanContainer.
 *
 *
 */
public class MT_BooleanContainer {
	// private static void testPopProportion(final int populationSize, final
	// double proportion, final Random random) {
	//
	// final List<Integer> list = new ArrayList<>();
	// for (int i = 0; i < populationSize; i++) {
	// list.add(i);
	// }
	//
	// Collections.shuffle(list, random);
	//
	// final LinkedHashSet<Integer> set = new LinkedHashSet<>();
	//
	// final int q = (int) (proportion * populationSize);
	// for (int i = 0; i < q; i++) {
	// set.add(list.get(i));
	// }
	// MemSizer memSizer = new MemSizer(false);
	// memSizer.excludeClass(Class.class);
	// final long hashSetSize = memSizer.getByteCount(set);
	//
	// final BooleanContainer bc = new BooleanContainer(false);
	// for (int i = 0; i < populationSize; i++) {
	// bc.set(i, true);
	// }
	//
	// memSizer = new MemSizer(false);
	// memSizer.excludeClass(Class.class);
	// final long bcSize = memSizer.getByteCount(bc);
	//
	// double ratio = hashSetSize;
	// ratio /= bcSize;
	//
	// final StringBuilder sb = new StringBuilder();
	// sb.append(populationSize);
	// sb.append("\t");
	// sb.append(proportion);
	// sb.append("\t");
	// sb.append(hashSetSize);
	// sb.append("\t");
	// sb.append(bcSize);
	// sb.append("\t");
	// sb.append(ratio);
	// System.out.println(sb);
	//
	// }
	//
	// private void test() {
	// final StringBuilder sb = new StringBuilder();
	// sb.append("populationSize");
	// sb.append("\t");
	// sb.append("proportion");
	// sb.append("\t");
	// sb.append("hashSetSize");
	// sb.append("\t");
	// sb.append("bcSize");
	// sb.append("\t");
	// sb.append("ratio");
	// System.out.println(sb);
	//
	// final Random random = new Random(3465645645645656L);
	// final int[] populations = { 10_000, 100_000, 1_000_000, 5_000_000,
	// 10_000_000, 50_000_000, 100_000_000 };
	// final double[] proportions = { 0.001, 0.002, 0.003, 0.004, 0.005, 0.01,
	// 0.02 };
	// for (final int population : populations) {
	// for (final double proportion : proportions) {
	// testPopProportion(population, proportion, random);
	// }
	// }
	// }
	//
	// private void testMemory() {
	// BooleanContainer booleanContainer = new BooleanContainer(false);
	// Random random = new Random();
	// for (int i = 0; i < 100_000; i++) {
	// booleanContainer.set(i, random.nextBoolean());
	// }
	//
	// MemSizer memSizer = new MemSizer(false);
	// memSizer.excludeClass(Class.class);
	// long byteCount = memSizer.getByteCount(booleanContainer);
	// System.out.println("byteCount = " + byteCount);
	//
	// }
	//
	// private MT_BooleanContainer() {
	//
	// }
	//
	// public static void main(String[] args) {
	// MT_BooleanContainer mt_BooleanContainer = new MT_BooleanContainer();
	// mt_BooleanContainer.test();
	// mt_BooleanContainer.testMemory();
	// }
}
