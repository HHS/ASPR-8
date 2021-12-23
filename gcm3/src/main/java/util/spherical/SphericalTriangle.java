package util.spherical;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import util.vector.MutableVector3D;
import util.vector.Vector3D;

/**
 * Represents an immutable triangle on the unit sphere.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class SphericalTriangle {

	/**
	 * Returns the shortest distance from the point to this triangle. Points on
	 * the inside of the triangle will return 0.
	 */
	public double distanceTo(SphericalPoint sphericalPoint) {

		if (contains(sphericalPoint)) {
			return 0;
		}
		double result = Double.POSITIVE_INFINITY;
		for (SphericalArc sphericalArc : sphericalArcs) {
			result = FastMath.min(result, sphericalArc.distanceTo(sphericalPoint));
		}
		return result;
	}

	private static double getTangentialAngle(Vector3D v1, Vector3D v2, Vector3D v3) {
		MutableVector3D p = new MutableVector3D();
		p.assign(v1);
		p.cross(v2);
		p.cross(v2);

		MutableVector3D q = new MutableVector3D();
		q.assign(v3);
		q.cross(v2);
		q.cross(v2);

		return p.angle(q);
	}

	/**
	 * Constructs a {@link SphericalTriangle} from the given
	 * {@link SphericalPoint} values.
	 */
	public SphericalTriangle(SphericalPoint sphericalPoint1, SphericalPoint sphericalPoint2, SphericalPoint sphericalPoint3) {

		if (sphericalPoint1 == null) {
			throw new MalformedSphericalTriangleException("null/no spherical point contributed to build of spherical triangle");
		}

		if (sphericalPoint2 == null) {
			throw new MalformedSphericalTriangleException("null/no spherical point contributed to build of spherical triangle");
		}

		if (sphericalPoint3 == null) {
			throw new MalformedSphericalTriangleException("null/no spherical point contributed to build of spherical triangle");
		}

		sphericalPoints = new SphericalPoint[] { sphericalPoint1, sphericalPoint2, sphericalPoint3 };

		sphericalArcs = new SphericalArc[3];

		for (int i = 0; i < 3; i++) {
			SphericalPoint p1 = sphericalPoints[i];
			SphericalPoint p2 = sphericalPoints[(i + 1) % 3];
			sphericalArcs[i] = new SphericalArc(p1, p2);
		}

		chirality = sphericalArcs[0].getChirality(sphericalPoints[2]);

		final Vector3D v0 = sphericalPoints[0].getPosition();
		final Vector3D v1 = sphericalPoints[1].getPosition();
		final Vector3D v2 = sphericalPoints[2].getPosition();

		final MutableVector3D perp = new MutableVector3D(v0);
		perp.cross(v1);
		final boolean leftHanded = perp.dot(v2) < 0;

		MutableVector3D midPoint = new MutableVector3D(v0);
		midPoint.add(v1);
		final MutableVector3D c = new MutableVector3D(v0);
		c.cross(v1);
		c.cross(midPoint);

		midPoint = new MutableVector3D(v1);
		midPoint.add(v2);
		final MutableVector3D d = new MutableVector3D(v1);
		d.cross(v2);
		d.cross(midPoint);

		c.cross(d);
		c.normalize();

		if (leftHanded) {
			c.reverse();
		}
		radius = c.angle(v0);

		centroid = new Vector3D(c);

		double alpha = getTangentialAngle(v0, v1, v2);
		double beta = getTangentialAngle(v1, v2, v0);
		double gamma = getTangentialAngle(v2, v0, v1);

		area = (alpha + beta + gamma) - FastMath.PI;

	}

	private final SphericalArc[] sphericalArcs;

	private final SphericalPoint[] sphericalPoints;

	private final Vector3D centroid;

	private final double radius;

	private final double area;

	private final Chirality chirality;

	/**
	 * Returns true if and only if the given {@link SphericalPoint} is in the
	 * interior or the arcs of this {@link SphericalTriangle}
	 */
	public boolean contains(SphericalPoint sphericalPoint) {
		for (int i = 0; i < 3; i++) {
			if (sphericalArcs[i].getChirality(sphericalPoint) != chirality) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns the area of this {@link SphericalTriangle}
	 */
	public double getArea() {
		return area;
	}

	/**
	 * Returns the centroid point (a unit vector) of this
	 * {@link SphericalTriangle}
	 */
	public Vector3D getCentroid() {
		return centroid;
	}

	/**
	 * Returns the radius of this {@link SphericalTriangle}. The radius is
	 * defined as the distance to the centroid from the vertices.
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Returns the {@link Chirality} of this {@link SphericalTriangle} relative
	 * to the natural order of its SphericalPoints.
	 */
	public Chirality getChirality() {
		return chirality;
	}

	/**
	 * Returns true if this {@link SphericalTriangle} overlaps with any part of
	 * the given {@link SphericalTriangle}
	 */
	public boolean intersects(SphericalTriangle sphericalTriangle) {
		for (int i = 0; i < 3; i++) {
			if (contains(sphericalTriangle.getSphericalPoint(i))) {
				return true;
			}
			if (sphericalTriangle.contains(getSphericalPoint(i))) {
				return true;
			}
		}

		for (int i = 0; i < 3; i++) {
			if (intersects(sphericalTriangle.getSphericalArc(i))) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Returns the {@link SphericalArc} that corresponds to the index. These
	 * arcs are formed from the original {@link SphericalPoint} members that
	 * define this {@link SphericalTriangle}. Arc[0] is formed(in order) from
	 * Point[0] and Point[1]. Arc[1] is formed(in order) from Point[1] and
	 * Point[2]. Arc[2] is formed(in order) from Point[2] and Point[0].
	 * 
	 * @param index
	 *            Values may be 0, 1 or 2
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if any other index is used
	 */
	public SphericalArc getSphericalArc(int index) {
		return sphericalArcs[index];
	}

	/**
	 * Returns the {@link SphericalPoint} that corresponds to the index and was
	 * used to construct this {@link SphericalTriangle}
	 * 
	 * @param index
	 *            Values may be 0, 1 or 2
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if any other index is used
	 * 
	 */
	public SphericalPoint getSphericalPoint(int index) {
		return sphericalPoints[index];
	}

	/**
	 * Returns true if this {@link SphericalTriangle} intersects the given
	 * {@link SphericalArc}
	 */
	public boolean intersects(SphericalArc sphericalArc) {
		for (int i = 0; i < 2; i++) {
			if (contains(sphericalArc.getSphericalPoint(i))) {
				return true;
			}
		}

		for (int i = 0; i < 3; i++) {
			if (sphericalArcs[i].intersectsArc(sphericalArc)) {
				return true;
			}
		}

		return false;
	}
}
