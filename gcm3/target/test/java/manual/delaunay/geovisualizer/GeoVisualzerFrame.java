package manual.delaunay.geovisualizer;

import java.awt.Frame;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.math3.util.Pair;

import util.earth.LatLon;

public class GeoVisualzerFrame extends JFrame {
	private static final long serialVersionUID = -5106781364986923139L;

	public <T> GeoVisualzerFrame(Map<T, LatLon> dataMap, List<Pair<T, T>> links) {
		super();
		setSize(500, 500);
		setLocation(0, 0);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GeoVisualizerPanel geoVisualizerPanel = new GeoVisualizerPanel(dataMap, links);
		setContentPane(geoVisualizerPanel);
		// give the panel focus so that key listeners will work
		geoVisualizerPanel.setFocusable(true);
		setVisible(true);
	}

}
