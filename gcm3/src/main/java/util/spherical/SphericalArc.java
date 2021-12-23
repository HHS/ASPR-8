package util.spherical;

import java.util.Arrays;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import util.vector.MutableVector3D;
import util.vector.Vector3D;

/**
 * Represents an immutable great arc segment on the unit sphere defined by two
 * distinct SphericalPoints. Instances are created via included builder class.
 * 
 * @author Shawn Hatch
 *
 */

@Immutable
public class SphericalArc {

	/**
	 * Hidden constructor
	 * 
	 * @throws MalformedSphericalArcException
	 *             <li>if the two {@link SphericalPoint} values are too close
	 *             together to properly form a normal vector.
	 * 
	 * @throws NullPointerException
	 *             <li>if either {@link SphericalPoint} is null
	 */
	public SphericalArc(SphericalPoint sphericalPoint1, SphericalPoint sphericalPoint2) {

		if (sphericalPoint1 == null) {
			throw new NullPointerException("null spherical point contributed to build of spherical arc");
		}

		if (sphericalPoint2 == null) {
			throw new NullPointerException("null spherical point contributed to build of spherical arc");
		}

		sphericalPoints = new SphericalPoint[] { sphericalPoint1, sphericalPoint2 };
		perp = sphericalPoint1.getPosition().cross(sphericalPoint2.getPosition()).normalize();
		if (!perp.isNormal()) {
			throw new MalformedSphericalArcException("spherical points too similar to form arc");
		}
		if (!perp.isPerpendicularTo(sphericalPoint1.getPosition())) {
			throw new MalformedSphericalArcException("spherical points too similar to form arc");
		}
		if (!perp.isPerpendicularTo(sphericalPoint2.getPosition())) {
			throw new MalformedSphericalArcException("spherical points too similar to form arc");
		}
		length = sphericalPoint1.getPosition().angle(sphericalPoint2.getPosition());
	}

	private final SphericalPoint[] sphericalPoints;

	/**
	 * Returns the {@link Chirality} of a SphericalPoint relative to this
	 * SphericalArc in the natural order of its SphericalPoints.
	 * 
	 * @throws NullPointerException
	 *             <li>if the given {@link SphericalPoint} is null
	 * 
	 * @param sphericalPoint
	 * @return
	 */
	public Chirality getChirality(SphericalPoint sphericalPoint) {

		double dot = perp.dot(sphericalPoint.getPosition());

		if (dot >= 0) {
			return Chirality.RIGHT_HANDED;
		}
		return Chirality.LEFT_HANDED;
	}

	private final double length;

	private final Vector3D perp;

	/**
	 * Returns true if and only if this SphericalArc intersects the given
	 * SphericalArc at a point
	 * 
	 * @throws NullPointerException
	 *             <li>if the given arc is null
	 * 
	 * @param arc
	 * @return
	 */
	public boolean intersectsArc(SphericalArc arc) {
		Chirality chirality1 = getChirality(arc.getSphericalPoint(0));
		Chirality chirality2 = getChirality(arc.getSphericalPoint(1));
		Chirality chirality3 = arc.getChirality(getSphericalPoint(0));
		Chirality chirality4 = arc.getChirality(getSphericalPoint(1));
		return (chirality1 != chirality2) && (chirality3 != chirality4);
	}

	/**
	 * Returns the SphericalPoint associated with the index
	 * 
	 * @param index
	 *            Values may be 0 or 1
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if any other index is used
	 */
	public SphericalPoint getSphericalPoint(int index) {
		return sphericalPoints[index];
	}

	/**
	 * Returns the arc length of this SphericalArc
	 * 
	 * @return
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Returns the intersection with the given SphericalArc. If no intersection
	 * exists, returns null
	 * 
	 * @param arc
	 * @return
	 */
	public SphericalPoint getInterSection(SphericalArc arc) {

		if (!intersectsArc(arc)) {
			return null;
		}
		// we know the arcs intersect
		Vector3D a = sphericalPoints[0].getPosition();
		Vector3D b = sphericalPoints[1].getPosition();

		/*
		 * The normal vector to each arc's plane will be perpendicular to the
		 * solution, thus the solution is the cross product of these two
		 * normals, or their reverse.
		 */
		Vector3D solution = perp.cross(arc.perp);

		/*
		 * Does the solution lie on the first arc? It will lie on the arc's
		 * plane. Thus, if we can show that the intersection lies between the
		 * arc's end points then the solution is correct. Otherwise, the
		 * solution must be reversed.
		 * 
		 */
		boolean containedOnArc = a.cross(solution).dot(perp) >= 0 && b.cross(solution).dot(perp) <= 0;
		if (!containedOnArc) {
			solution = solution.reverse();
		}

		return new SphericalPoint(solution);
	}

	/**
	 * Returns the distance from the given {@linkplain SphericalPoint} to this
	 * 
	 * @throws NullPointerException
	 *             <li>if the given {@link SphericalPoint} is null
	 */
	public double distanceTo(SphericalPoint sphericalPoint) {
		Vector3D a = sphericalPoints[0].getPosition();
		Vector3D b = sphericalPoints[1].getPosition();

		/*
		 * Create a normal unit vector to the plane containing the two end
		 * points --
		 */
		MutableVector3D p = new MutableVector3D(a);
		p.cross(b);
		p.normalize();

		/*
		 * Create a vector for the input
		 */
		MutableVector3D q = new MutableVector3D(sphericalPoint.getPosition());

		// Create a new vector that is q's image in the plane of this arc
		MutableVector3D qOnPlane = new MutableVector3D(p);
		qOnPlane.scale(-p.dot(q));
		qOnPlane.add(q);

		/*
		 * Calculate the angle from q to its image on the plane
		 */
		double angle = q.angle(qOnPlane);

		/*
		 * If q lies between the end points, then return angle, otherwise return
		 * the angle between q and the closest end point.
		 */

		MutableVector3D v = new MutableVector3D(qOnPlane);
		v.cross(a);
		double aDot = v.dot(p);// positive if clockwise from a
		if (aDot <= 0) {
			v.assign(qOnPlane);
			v.cross(b);
			double bDot = v.dot(p);// positive if clockwise from b
			if (bDot >= 0) {
				/*
				 * qOnPlane, from the perspective of the normal, is CCW from A
				 * and CW from B, so it lies between them
				 */
				return angle;
			}
		}

		/*
		 * q's projection onto the plane does not lie between the end points
		 */

		return FastMath.min(q.angle(a), q.angle(b));

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SphericalArc [sphericalPoints=");
		builder.append(Arrays.toString(sphericalPoints));
		builder.append("]");
		return builder.toString();
	}

}
