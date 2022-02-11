package manual.delaunay.planarvisualizer;

import java.awt.Frame;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;

import org.apache.commons.math3.util.Pair;

import util.vector.Vector2D;

public class PlanarVisualzerFrame extends JFrame {
	private static final long serialVersionUID = -5106781364986923139L;

	public <T> PlanarVisualzerFrame(Map<T, Vector2D> dataMap, List<Pair<T, T>> links) {
		super();
		setSize(1500, 1000);
		setLocation(0, 0);
		setExtendedState(Frame.MAXIMIZED_BOTH);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		PlanarVisualizerPanel planarVisualizerPanel = new PlanarVisualizerPanel(dataMap, links);
		setContentPane(planarVisualizerPanel);
		// give the panel focus so that key listeners will work
		planarVisualizerPanel.setFocusable(true);
		setVisible(true);
	}

}
