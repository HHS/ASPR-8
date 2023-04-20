package util.spherical;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import util.dimensiontree.VolumetricDimensionTree;
import util.vector.MutableVector3D;

/**
 * Represents an immutable, non-crossing polygon on the surface of a unit
 * sphere.
 * 
 *
 */

@Immutable
public class SphericalPolygon {

	private final static int SEARCH_TREE_THRESHOLD = 30;

	/*
	 * static class for containing the contributed points that will the form the
	 * polygon
	 */
	private static class Data {
		private List<SphericalPoint> sphericalPoints = new ArrayList<>();
		boolean useSearchTree = true;
	}

	/**
	 * Builder class for {@link SphericalPolygon}
	 * 
	 *
	 */
	public static class Builder {

		/**
		 * Adds a {@link SphericalPoint} to the {@link SphericalPolygon}. Order
		 * of addition dictates the order of the vertices of the polygon.
		 * Winding order may be either left or right handed.
		 */
		public Builder addSphericalPoint(SphericalPoint sphericalPoint) {
			data.sphericalPoints.add(sphericalPoint);
			return this;
		}

		/**
		 * Sets the useSearchTree policy for the {@link SphericalPolygon}.
		 * Default is true;
		 */
		public Builder setUseSearchTree(boolean useSearchTree) {
			data.useSearchTree = useSearchTree;
			return this;
		}

		private Data data = new Data();

		/*
		 * Hidden constructor
		 */
		private Builder() {

		}

		/**
		 * Builds a {@link SphericalPolygon} from the supplied
		 * {@link SphericalPoint} values.
		 * 
		 * @throws MalformedSphericalPolygonException
		 * 
		 *             <li>if any of the {@link SphericalPoint} values are null
		 *             <li>if there are fewer than three {@link SphericalPoint}
		 *             values
		 *             <li>if the {@link SphericalPoint} values form a crossing
		 *             polygon
		 *             <li>if the polygon is ambiguous
		 * 
		 */
		public SphericalPolygon build() {
			return new SphericalPolygon(data);
		}
	}

	/**
	 * Returns a new builder instance for {@link SphericalTriangle}
	 */
	public static Builder builder() {
		return new Builder();
	}

	private final List<SphericalPoint> sphericalPoints;

	private final List<SphericalArc> sphericalArcs = new ArrayList<>();

	/**
	 * Returns the a list of {@link SphericalTriangle} values that cover this
	 * {@link SphericalPolygon} without overlap.
	 */
	public List<SphericalTriangle> getSphericalTriangles() {
		return new ArrayList<>(sphericalTriangles);
	}

	private final List<SphericalTriangle> sphericalTriangles;

	private final Chirality chirality;

	private final SphericalPoint centroid;

	private final double radius;

	private final VolumetricDimensionTree<SphericalTriangle> searchTree;

	/**
	 * Returns the {@link Chirality} of this {@link SphericalPolygon} relative
	 * to the natural order of its SphericalPoints.
	 */
	public Chirality getChirality() {
		return chirality;
	}

	/**
	 * Returns true if and only if the given {@link SphericalPoint} is in the
	 * interior or the arcs of this {@link SphericalPolygon}
	 */
	public boolean containsPosition(SphericalPoint sphericalPoint) {
		if (searchTree == null) {
			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					return true;
				}
			}
		} else {
			List<SphericalTriangle> membersInSphere = searchTree.getMembersInSphere(0, sphericalPoint.getPosition().toArray());
			for (SphericalTriangle sphericalTriangle : membersInSphere) {
				if (sphericalTriangle.contains(sphericalPoint)) {
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalPolygon}
	 */
	public boolean intersects(SphericalPolygon sphericalPolygon) {
		for (SphericalTriangle triangle1 : sphericalTriangles) {
			for (SphericalTriangle triangle2 : sphericalPolygon.getSphericalTriangles()) {
				if (triangle1.intersects(triangle2)) {
					return true;
				}
			}
		}
		return false;
	}

	public List<SphericalArc> getSphericalArcs() {
		return new ArrayList<>(sphericalArcs);

	}

	public List<SphericalPoint> getSphericalPoints() {
		return new ArrayList<>(sphericalPoints);
	}

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalArc}
	 */
	public boolean intersects(SphericalArc sphericalArc) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalArc)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns true if this {@link SphericalPolygon} overlaps with any part of
	 * the given {@link SphericalTriangle}
	 */
	public boolean intersects(SphericalTriangle sphericalTriangle) {
		for (SphericalTriangle triangle : sphericalTriangles) {
			if (triangle.intersects(sphericalTriangle)) {
				return true;
			}
		}
		return false;
	}

	private static SphericalTriangle popEar(int index, List<SphericalPoint> points, Chirality chirality) {
		// form the triangle from the index
		SphericalTriangle t = new SphericalTriangle(points.get((index + -1 + points.size()) % points.size()), points.get(index), points.get((index + 1) % points.size()));

		/*
		 * if the triangle does not agree with chirality then we are done
		 */
		if (t.getChirality() != chirality) {
			return null;
		}
		/*
		 * if the triangle's new side crosses any of the non-attached legs, then
		 * we are done
		 */

		// here are the indices of the attached legs. We will not attempt to
		// intersect them with the newly formed arc
		int excludedIndex1 = (index - 2 + points.size()) % points.size();
		int excludedIndex2 = (index - 1 + points.size()) % points.size();
		int excludedIndex3 = index;
		int excludedIndex4 = (index + 1) % points.size();

		SphericalArc sphericalArcFormedByTriangle = t.getSphericalArc(2);

		for (int i = 0; i < points.size(); i++) {
			if (i != excludedIndex1 && i != excludedIndex2 && i != excludedIndex3 && i != excludedIndex4) {
				int j = (i + 1) % points.size();
				SphericalArc potentiallyIntersectingArc = new SphericalArc(points.get(i), points.get(j));
				if (sphericalArcFormedByTriangle.intersectsArc(potentiallyIntersectingArc)) {
					return null;
				}
			}
		}

		// if any of the other nodes are in the triangle, then we are done
		for (int i = 0; i < points.size(); i++) {
			if (i != excludedIndex2 && i != excludedIndex3 && i != excludedIndex4) {
				if (t.contains(points.get(i))) {
					return null;
				}
			}

		}

		return t;
	}

	private static List<SphericalTriangle> triangulate(List<SphericalPoint> sphericalPoints, Chirality chirality) {

		/*
		 * We will attempt to remove nodes from the list one by one by popping
		 * an ear off the polygon -- that is, we will remove a triangle and get
		 * an increasingly simple polygon. If all goes well, we will be left
		 * with a degenerate polygon (one that has no nodes)
		 * 
		 */

		List<SphericalTriangle> result = new ArrayList<>();
		List<SphericalPoint> points = new ArrayList<>(sphericalPoints);

		/*
		 * Roll around the polygon, trying to pop off a node and its
		 * corresponding triangle. Popping an ear is successful only when the
		 * triangle so formed is fully inside the polygon. If we fail
		 * repeatedly, we stop once we have come full circle.
		 * 
		 */

		int index = 0;
		int failurecount = 0;
		while ((points.size() > 2) && (failurecount < points.size())) {
			SphericalTriangle sphericalTriangle = popEar(index, points, chirality);
			if (sphericalTriangle != null) {
				points.remove(index);
				index = index % points.size();
				result.add(sphericalTriangle);
				failurecount = 0;
			} else {
				failurecount++;
				index = (index + 1) % points.size();
			}
		}

		// if there are any nodes remaining,
		// then return false and clear out any triangles
		// that were formed

		if (points.size() > 2) {
			result.clear();
		}
		return result;
	}

	private SphericalPolygon(Data data) {

		if (data.sphericalPoints.size() < 3) {
			throw new MalformedSphericalPolygonException("spherical polygons require at least three points");
		}

		for (SphericalPoint sphericalPoint : data.sphericalPoints) {
			if (sphericalPoint == null) {
				throw new MalformedSphericalPolygonException("spherical polygons require non-null spherical points");
			}
		}

		MutableVector3D v = new MutableVector3D();
		for (SphericalPoint sphericalPoint : data.sphericalPoints) {
			v.add(sphericalPoint.getPosition());
		}
		centroid = new SphericalPoint(v);

		if (!centroid.getPosition().isFinite()) {
			throw new MalformedSphericalPolygonException("the spherical polygon formed from the given spherical points is ambiguous");
		}

		double r = Double.NEGATIVE_INFINITY;
		for (SphericalPoint sphericalPoint : data.sphericalPoints) {
			r = FastMath.max(r, centroid.getPosition().angle(sphericalPoint.getPosition()));
		}
		radius = r;

		Chirality chirality = Chirality.RIGHT_HANDED;
		List<SphericalTriangle> triangles = triangulate(data.sphericalPoints, Chirality.RIGHT_HANDED);
		if (triangles.size() == 0) {
			chirality = Chirality.LEFT_HANDED;
			triangles = triangulate(data.sphericalPoints, Chirality.LEFT_HANDED);
		}

		if (triangles.size() == 0) {
			throw new MalformedSphericalPolygonException("the spherical points form a crossing polygon");
		}

		this.chirality = chirality;
		sphericalTriangles = triangles;

		sphericalPoints = data.sphericalPoints;

		for (int i = 0; i < data.sphericalPoints.size(); i++) {
			int j = (i + 1) % data.sphericalPoints.size();
			sphericalArcs.add(new SphericalArc(data.sphericalPoints.get(i), data.sphericalPoints.get(j)));
		}

		if (sphericalTriangles.size() > SEARCH_TREE_THRESHOLD && data.useSearchTree) {
			searchTree = VolumetricDimensionTree.builder()//
												.setFastRemovals(true)//
												.setLowerBounds(new double[] { -1, -1, -1 })//
												.setUpperBounds(new double[] { 1, 1, 1 })//
												.build();//

			for (SphericalTriangle sphericalTriangle : sphericalTriangles) {
				double[] triangleCenter = sphericalTriangle.getCentroid().toArray();
				searchTree.add(triangleCenter, sphericalTriangle.getRadius(), sphericalTriangle);
			}
		} else {
			searchTree = null;
		}

	}

	/**
	 * Returns the radius of this {@link SphericalPolygon}. All vertices of this
	 * polygon lie with the radius around the centroid.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Returns the center of this {@link SphericalPolygon}. All vertices of this
	 * polygon lie with the radius around the centroid. The centroid is
	 * calculated as the numerical average of the points that compose the
	 * polygon.
	 */
	public SphericalPoint getCentroid() {
		return centroid;
	}
}
