package util.dimensiontree.internal;

/**
 * Represents a sphere in the dimension of the tree.
 * 
 * @author Shawn Hatch
 *
 */
public class Sphere implements Shape {

	private final double radius;
	private final double sqRadius;

	private double[] position = new double[0];

	public Sphere(double radius, double[] position) {
		this.radius = radius;
		this.sqRadius = radius * radius;
		this.position = position.clone();
	}

	@Override
	public boolean containsPosition(double[] position) {

		double distance = 0;
		for (int i = 0; i < position.length; i++) {
			double d = this.position[i] - position[i];
			distance += d * d;
		}
		return distance < sqRadius;
	}

	@Override
	public <T> ShapeIntersectionType intersectsBox(Node<T> node) {

		double squareDistanceToBoxCenter = 0;
		for (int i = 0; i < position.length; i++) {
			double value = position[i] - (node.upperBounds[i] + node.lowerBounds[i]) / 2;
			squareDistanceToBoxCenter += value * value;
		}

		if (SquareRootInequality.evaluate(node.squareRadius, sqRadius, squareDistanceToBoxCenter)) {
			return ShapeIntersectionType.NONE;
		}

		if (SquareRootInequality.evaluate(squareDistanceToBoxCenter, node.squareRadius, sqRadius)) {
			return ShapeIntersectionType.COMPLETE;
		}

		return ShapeIntersectionType.PARTIAL;
	}

	public double[] position() {
		return position.clone();
	}

	public double radius() {
		return radius;
	}

}
