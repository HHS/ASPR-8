package manual.delaunay.planarvisualizer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.Timer;

import org.apache.commons.math3.util.FastMath;
import org.apache.commons.math3.util.Pair;

import util.vector.MutableVector2D;
import util.vector.Vector2D;

public class PlanarVisualizerPanel extends JPanel {
	private static final long serialVersionUID = -8746718644903555611L;
	private Timer timer;
	private boolean paintOnTimer;
	private final int timerDelayMilliseconds = 1;

	private final PointModelProvider<?> pointModelProvider;

	public <T> PlanarVisualizerPanel(Map<T, Vector2D> dataMap, List<Pair<T, T>> links) {

		pointModelProvider = new PointModelProvider<>(dataMap, links);

		addMouseListener(new MouseListenerImpl());

		setBackground(Color.darkGray);

		timer = new Timer(timerDelayMilliseconds, new ActionListenerImpl());
		timer.start();

	}

	private class ActionListenerImpl implements ActionListener {

		@Override
		public void actionPerformed(ActionEvent e) {
			// if (paintOnTimer) {
			// buildPath();
			// repaint();
			// }
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g;
		displayModel(g2);
	}

	private static class Link {
		private int first;
		private int second;

		public Link(int first, int second) {
			super();
			this.first = first;
			this.second = second;
		}

	}

	private class PointModelProvider<T> {
		private final Map<T, Vector2D> dataMap;
		private List<Point> points;
		private int lastWidth;
		private int lastHeight;
		private List<Link> links = new ArrayList<>();
		private Set<Integer> linkIndices = new LinkedHashSet<>();

		public PointModelProvider(Map<T, Vector2D> dataMap, List<Pair<T, T>> links) {
			this.dataMap = dataMap;

			Map<T, Integer> map = new LinkedHashMap<>();

			for (T t : dataMap.keySet()) {
				map.put(t, map.size());
			}

			for (Pair<T, T> pair : links) {
				this.links.add(new Link(map.get(pair.getFirst()), map.get(pair.getSecond())));
			}

			for (Link link : this.links) {
				linkIndices.add(link.first);
				linkIndices.add(link.second);
			}
		}

		public boolean isLinkIndex(int index) {
			return linkIndices.contains(index);
		}

		public List<Point> getPoints() {
			int currentWidth = getWidth();
			int currentHeight = getHeight();
			if (currentHeight != lastHeight || currentWidth != lastWidth || points == null) {
				lastWidth = currentWidth;
				lastHeight = currentHeight;
				buildPoints();
			}
			return points;
		}

		public List<Link> getLinks() {
			return links;
		}

		private void buildPoints() {

			MutableVector2D upperLeftDataPosition = new MutableVector2D(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
			MutableVector2D lowerRightDataPosition = new MutableVector2D(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);

			for (T t : dataMap.keySet()) {
				Vector2D v = dataMap.get(t);

				if (v.getX() < upperLeftDataPosition.getX()) {
					upperLeftDataPosition.setX(v.getX());
				}

				if (v.getY() < upperLeftDataPosition.getY()) {
					upperLeftDataPosition.setY(v.getY());
				}

				if (v.getX() > lowerRightDataPosition.getX()) {
					lowerRightDataPosition.setX(v.getX());
				}
				if (v.getY() > lowerRightDataPosition.getY()) {
					lowerRightDataPosition.setY(v.getY());
				}
			}

			double deltaX = lowerRightDataPosition.getX() - upperLeftDataPosition.getX();
			double deltaY = lowerRightDataPosition.getY() - upperLeftDataPosition.getY();

			double sX = lastWidth;
			sX /= deltaX;

			double sY = lastHeight;
			sY /= deltaY;

			/*
			 * Adjust the scalar factor so the points are not on the edge of the
			 * screen
			 */
			double scalar = Math.min(sX, sY) * 0.9;

			MutableVector2D screenCenter = new MutableVector2D(lastWidth, lastHeight);
			screenCenter.scale(0.5);

			/*
			 * dataCenter is the center of the box that bounds the data, rather
			 * than the centroid of the data
			 */
			MutableVector2D dataCenter = new MutableVector2D(lowerRightDataPosition);
			dataCenter.add(upperLeftDataPosition);
			dataCenter.scale(0.5);

			/*
			 * Map each point in the data to the screen coordinates. Note that
			 * this projection takes data center to screen center.
			 */
			points = new ArrayList<>();
			for (T t : dataMap.keySet()) {
				MutableVector2D v = new MutableVector2D(dataMap.get(t));
				v.sub(dataCenter);
				v.scale(scalar);
				v.add(screenCenter);
				Point point = new Point((int) v.getX(), (int) v.getY());
				points.add(point);
			}
		}

	}

	private void displayModel(Graphics2D g2) {

		// Transform the data model into a point model that will fit on the
		// screen
		List<Point> points = pointModelProvider.getPoints();

		double baseWidth = Math.min(getWidth(), getHeight());
		// int modelEdgeWidth = (int) Math.max(1, baseWidth);
		// int pathEdgeWidth = 2 * modelEdgeWidth;
		// int nodeRadius = 5 * modelEdgeWidth;

		baseWidth = baseWidth / FastMath.sqrt(points.size());

		int pathEdgeWidth = (int) (baseWidth / 10);
		pathEdgeWidth = FastMath.max(pathEdgeWidth, 1);
		pathEdgeWidth = FastMath.min(pathEdgeWidth, 10);
		int nodeRadius = (int) (baseWidth / 10);
		nodeRadius = FastMath.max(nodeRadius, 1);
		nodeRadius = FastMath.min(nodeRadius, 10);

		// paint the point links
		g2.setStroke(new BasicStroke(pathEdgeWidth));
		g2.setColor(Color.yellow);
		List<Link> links = pointModelProvider.getLinks();
		for (Link link : links) {
			Point originPoint = points.get(link.first);
			g2.fillOval(originPoint.x - nodeRadius, originPoint.y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);

			Point destinationPoint = points.get(link.second);
			g2.fillOval(destinationPoint.x - nodeRadius, destinationPoint.y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);

			g2.drawLine(originPoint.x, originPoint.y, destinationPoint.x, destinationPoint.y);
		}

		// paint the nodes
		g2.setColor(Color.red);
		for (int i = 0; i < points.size(); i++) {
			if (!pointModelProvider.isLinkIndex(i)) {
				Point point = points.get(i);
				g2.fillOval(point.x - nodeRadius, point.y - nodeRadius, 2 * nodeRadius, 2 * nodeRadius);
			}
		}

		// if (pointModel.pointCount() < 20) {
		// g2.setColor(Color.white);
		// g2.setFont(new Font("Serif", Font.BOLD, 20));
		//// PointModel<PlanarPoint> planarPointModel =
		// pointModelProvider.getPlanarPointModel();
		// for (int i = 0; i < pointModel.pointCount(); i++) {
		// Point point = pointModel.getPoint(i);
		//// PlanarPoint planarPoint = planarPointModel.getPoint(i);
		// StringBuilder sb = new StringBuilder();
		// sb.append(Integer.toString(i));
		//// sb.append(" ( ");
		//// sb.append(planarPoint.getX());
		//// sb.append(" , ");
		//// sb.append(planarPoint.getY());
		//// sb.append(" )");
		// g2.drawString(sb.toString(), point.x - nodeRadius, point.y -
		// nodeRadius);
		// }
		// }

	}

	private void handleMouseClicked(MouseEvent e) {
		switch (e.getButton()) {
		case MouseEvent.BUTTON1:
			repaint();
			break;
		case MouseEvent.BUTTON2:
			paintOnTimer = !paintOnTimer;
			break;
		case MouseEvent.BUTTON3:
			// buildPath();
			break;
		default:
			return;
		}

		repaint();
	}

	private class MouseListenerImpl implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			handleMouseClicked(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {
			// do nothing

		}

		@Override
		public void mouseReleased(MouseEvent e) {
			// do nothing

		}

		@Override
		public void mouseEntered(MouseEvent e) {
			// do nothing
		}

		@Override
		public void mouseExited(MouseEvent e) {
			// do nothing

		}

	}

}
