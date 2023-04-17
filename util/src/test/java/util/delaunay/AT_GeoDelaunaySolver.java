package util.delaunay;

import java.util.Map;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;

public class AT_GeoDelaunaySolver {

	@Test
	@UnitTestMethod(target = GeoDelaunaySolver.class, name = "solve", args = { Map.class }, tags = { UnitTag.MANUAL })
	public void testSolve() {
		// Should be manually tested. See GeoVisualizerDriver for a manual
		// demonstration that displays points on a 3D globe.
	}
}
