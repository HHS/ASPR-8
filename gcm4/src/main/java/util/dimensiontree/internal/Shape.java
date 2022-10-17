package util.dimensiontree.internal;

/**
 * General interface for shapes used for gathering member from the tree.
 * 
 * @author Shawn Hatch
 *
 */
public interface Shape {

	/**
	 * Returns true if the position is located inside this shape
	 */
	public boolean containsPosition(double[] position);

	/**
	 * Returns a ShapeIntersectionType that is the shapes determination of the
	 * overlap of the shape and the given node. Used to streamline the process
	 * of gathering members from nodes during intersection tests.
	 */
	public <T> ShapeIntersectionType intersectsBox(Node<T> node);
}
