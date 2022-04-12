package util.vector;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import util.delaunay.planarvisualizer.PlanarVisualzerFrame;
import util.random.RandomGeneratorProvider;
import util.vector.Circle2D.SolutionAlgorithm;

/**
 * A manual test class for {@linkplain Circle2D} comparing the performance of
 * the various algorithms for forming a minimal bounding circle about a list of
 * 2D positions.
 * 
 * @author Shawn Hatch
 *
 */

public class MT_CircleTest {
	

	private static Circle2D generateCircle(RandomGenerator randomGenerator, SolutionAlgorithm solutionAlgorithm, List<Vector2D> points) {
		Circle2D circle2d = new Circle2D(points, solutionAlgorithm);
		// System.out.println(solutionAlgorithm.toString() + " " + circle2d);
		for (int i = 0; i < points.size(); i++) {
			Vector2D point = points.get(i);
			assertTrue(circle2d.contains(point));
		}
		return circle2d;
	}

	private void testWithStats() {


		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(134563453453453L);

		StringBuilder sb = new StringBuilder();
		for (SolutionAlgorithm solutionAlgorithm : SolutionAlgorithm.values()) {
			sb.append(solutionAlgorithm);
			sb.append("\t");
		}
		System.out.println(sb.toString());

		for (int i = 0; i < 100; i++) {
			int n = 300;
			List<Vector2D> points = new ArrayList<>();
			for (int j = 0; j < n; j++) {
				Vector2D point = new Vector2D(randomGenerator.nextDouble(), randomGenerator.nextDouble() / 10);
				points.add(point);
			}

			Map<SolutionAlgorithm, Double> map = new LinkedHashMap<>();
			for (SolutionAlgorithm solutionAlgorithm : SolutionAlgorithm.values()) {
				Circle2D circle2d = generateCircle(randomGenerator, solutionAlgorithm, points);
				map.put(solutionAlgorithm, circle2d.getRadius());
			}
			sb = new StringBuilder();
			for (SolutionAlgorithm solutionAlgorithm : SolutionAlgorithm.values()) {
				sb.append(map.get(solutionAlgorithm));
				sb.append("\t");
			}
			System.out.println(sb.toString());

		}

	}

	/**
	 * Non-JUnit test that displays the various solutions in a swing application
	 */
	public static void main(String[] args) {
		new MT_CircleTest().testWithStats();

		

		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(134563453453453L);
		int n = 30;
		List<Vector2D> points = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			Vector2D point = new Vector2D(randomGenerator.nextDouble(), randomGenerator.nextDouble());
			points.add(point);
		}

		Map<Integer, Vector2D> dataMap = new LinkedHashMap<>();
		int base = 0;
		for (int i = 0; i < points.size(); i++) {
			dataMap.put(i + base, points.get(i));
		}

		int cirleSteps = 100;

		base += points.size();
		List<Pair<Integer, Integer>> pairs = new ArrayList<>();

		for (SolutionAlgorithm solutionAlgorithm : SolutionAlgorithm.values()) {
			Circle2D circle2d = generateCircle(randomGenerator, solutionAlgorithm, points);

			for (int i = 0; i < cirleSteps; i++) {
				Vector2D center = circle2d.getCenter();
				double angle = FastMath.PI * 2 * i / cirleSteps;
				Vector2D v = new Vector2D(circle2d.getRadius(), 0).rotate(angle).add(center);
				dataMap.put(base + i, v);
			}

			for (int i = 0; i < cirleSteps - 1; i++) {
				pairs.add(new Pair<>(i + base, i + base + 1));
			}

			base += cirleSteps;

		}

		new PlanarVisualzerFrame(dataMap, pairs);
	}

	private MT_CircleTest() {

	}

}
