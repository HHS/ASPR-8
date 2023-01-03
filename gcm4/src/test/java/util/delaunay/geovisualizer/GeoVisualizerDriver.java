package util.delaunay.geovisualizer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.util.Pair;

import util.delaunay.GeoDelaunaySolver;
import util.earth.LatLon;
import util.time.TimeElapser;

/**
 * A demonstration application driver that displays population data consisting
 * of fields in a tab delimited file:
 * 
 * <pre>
 *     
 * id(string)
 * lon(degrees)
 * lat(degrees)
 * population(int)
 * </pre>
 * 
 * 
 * The display is on a 3D spherical earth where the points are connected to one
 * another in a Delaunay triangle mesh. An example file in contained in the test
 * resources of this project.
 * 
 * 
 *
 */
public class GeoVisualizerDriver {

	private GeoVisualizerDriver() {

	}

	public static final class ClientData {

		private final String id;

		private final int population;

		public ClientData(String id, int population) {
			this.id = id;
			this.population = population;
		}

		public String getId() {
			return id;
		}

		public int getPopulation() {
			return population;
		}

	}

	public static void main(String[] args) throws IOException {

		Path tractsFile = Paths.get(args[0]);

		Map<ClientData, LatLon> dataMap = new LinkedHashMap<>();

		Files.readAllLines(tractsFile).stream().skip(1).forEach(line -> {
			String[] strings = line.split(",");
			String id = strings[0];
			double lon = Double.parseDouble(strings[1]);
			double lat = Double.parseDouble(strings[2]);
			int population = Integer.parseInt(strings[3]);
			ClientData clientData = new ClientData(id, population);
			LatLon latLon = new LatLon(lat, lon);
			dataMap.put(clientData, latLon);
		});

		TimeElapser timeElapser = new TimeElapser();

		List<Pair<ClientData, ClientData>> pairs = GeoDelaunaySolver.solve(dataMap);

		System.out.println("Solver time = " + timeElapser.getElapsedSeconds() + " seconds");

		new GeoVisualzerFrame(dataMap, pairs);
	}

}
