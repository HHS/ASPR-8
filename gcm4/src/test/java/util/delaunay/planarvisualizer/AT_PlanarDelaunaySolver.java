package util.delaunay.planarvisualizer;

import java.util.Map;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTestMethod;
import util.delaunay.PlanarDelaunaySolver;

public class AT_PlanarDelaunaySolver {

	@Test
	@UnitTestMethod(target = PlanarDelaunaySolver.class, name = "solve", args = { Map.class }, tags = { UnitTag.MANUAL })
	public void testSolve() {
		// Should be manually tested. See PlanarVisualzerFrame for a manual
		// demonstration that displays points on a 2D square.
	}
}
