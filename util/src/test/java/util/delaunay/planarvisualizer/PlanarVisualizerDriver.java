package util.delaunay.planarvisualizer;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.math3.util.Pair;

import util.delaunay.PlanarDelaunaySolver;
import util.random.RandomGeneratorProvider;
import util.time.TimeElapser;
import util.vector.Vector2D;

/**
 * A demonstration application driver that displays a randomized set of 5000
 * points in the unit square.The display is on a 2D plane where the points are
 * connected to one another in a Delaunay triangle mesh.
 * 
 * 
 *
 */
public class PlanarVisualizerDriver {

	public static void main(String[] args) {
		long seed = 147623563453456L;
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(seed);

		int nodeCount = 5_000;

		Map<Object, Vector2D> dataMap = new LinkedHashMap<>();
		for (int i = 0; i < nodeCount; i++) {
			Vector2D v = new Vector2D(randomGenerator.nextDouble() * 1000 - 500, randomGenerator.nextDouble() * 1000 - 500);
			dataMap.put(new Object(), v);
		}

		TimeElapser timeElapser = new TimeElapser();
		List<Pair<Object, Object>> pairs = PlanarDelaunaySolver.solve(dataMap);
		System.out.println("PlanarDelaunaySolver time = " + timeElapser.getElapsedMilliSeconds());

		System.out.println("pairs = " + pairs.size());

		new PlanarVisualzerFrame(dataMap, pairs);
	}

}
