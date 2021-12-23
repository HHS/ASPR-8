package util.dimensiontree.internal;

import org.apache.commons.math3.util.FastMath;

/**
 * Represents a rectangular box in the dimension of the tree.
 * 
 * @author Shawn Hatch
 *
 */
public class Rectanguloid implements Shape {

	private double[] position = new double[0];

	private double[] bounds = new double[0];

	public Rectanguloid(double[] lowerBounds, double[] upperBounds) {
		position = new double[lowerBounds.length];
		bounds = new double[lowerBounds.length];
		for (int i = 0; i < position.length; i++) {
			position[i] = (upperBounds[i] + lowerBounds[i]) * 0.5;
			bounds[i] = (upperBounds[i] - lowerBounds[i]) * 0.5;
		}
	}

	public double[] bounds() {
		return bounds.clone();
	}

	@Override
	public boolean containsPosition(double[] position) {
		for (int i = 0; i < position.length; i++) {
			if (FastMath.abs(this.position[i] - position[i]) > bounds[i]) {
				return false;
			}
		}
		return true;
	}

	@Override
	public <T> ShapeIntersectionType intersectsBox(Node<T> node) {
		// double[] lowerBounds, double[] upperBounds
		int containmentCount = 0;
		for (int i = 0; i < position.length; i++) {
			if (position[i] + bounds[i] < node.lowerBounds[i]) {
				return ShapeIntersectionType.NONE;
			}
			if (position[i] - bounds[i] > node.upperBounds[i]) {
				return ShapeIntersectionType.NONE;
			}

			if ((position[i] + bounds[i] > node.upperBounds[i]) && (position[i] - bounds[i] < node.lowerBounds[i])) {
				containmentCount++;
			}
		}
		if (containmentCount == position.length) {
			return ShapeIntersectionType.COMPLETE;
		}
		return ShapeIntersectionType.PARTIAL;

	}

	public double[] position() {
		return position.clone();
	}
}
