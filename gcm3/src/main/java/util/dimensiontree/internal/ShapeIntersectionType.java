package util.dimensiontree.internal;

/**
 * Represents the degree of intersection of the shape and a node.
 * 
 * @author Shawn Hatch
 *
 */
public enum ShapeIntersectionType {
	/**
	 * The shape and node have no intersection
	 */
	NONE,

	/**
	 * The shape may intersect the node. Members of the node will require
	 * further comparison to the shape.
	 */
	PARTIAL,

	/**
	 * The shape fully contains the node and all members of the node can be
	 * gathered without further comparison to the shape.
	 */
	COMPLETE;
}
