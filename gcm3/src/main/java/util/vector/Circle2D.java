package util.vector;

import java.util.List;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;
import util.spherical.Chirality;

/**
 * A utility for calculating the smallest circle that encompasses a set of 2D
 * points.
 * 
 * The utility can use any of four algorithms for forming the circle.
 * 
 * CENTROID - The centroid of the points is used as the center of the circle with
 * the radius being the largest distance from any point to that centroid.
 * Solution is order O(n).
 * 
 * N3 - The order O(n^3) version of the N4 algorithm. It is a bit more complex
 * than N4, but is just a streamlined version of N4 and should return the same
 * circle within precision error.
 * 
 * N4 - The order O(n^4) algorithm that returns the smallest circle that will
 * contain all of the points. This will return the same solution as Nimrod
 * Megiddo's O(n) algorithm.
 * 
 * COLLAPSING_BUBBLE - An order O(n) algorithm that returns the same solution as
 * N4 approximately 80+% of the time. The remaining ~20% the algorithm returns
 * circles that are generally a fraction of percent larger than the optimal
 * solution of N4 and Megiddo's O(n) algorithm.
 * 
 * Future improvements to this class would likely include an implementation of
 * Megiddo's O(n) algorithm as well as a version of this utility for spherical
 * polygons.
 * 
 * @author Shawn Hatch
 *
 */
@Immutable
public class Circle2D {

	public static enum SolutionAlgorithm {
		CENTROID, N3, N4, COLLAPSING_BUBBLE
	}

	private final Vector2D center;

	private final double radius;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Circle2D [radius=");
		builder.append(radius);
		builder.append(", center=");
		builder.append(center);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * Returns the radius of this circle
	 */
	public double getRadius() {
		return radius;
	}

	/**
	 * Returns true if and only if both the center and radius of this circle
	 * contain finite values
	 */
	public boolean isFinite() {
		return Double.isFinite(radius) && center.isFinite();
	}

	/**
	 * Returns the center of this circle
	 */
	public Vector2D getCenter() {
		return center;
	}

	private Circle2D() {
		center = new Vector2D();
		radius = Double.POSITIVE_INFINITY;
	}

	private Circle2D(Vector2D a) {
		center = a;
		radius = 0;
	}

	private Circle2D(Vector2D a, double radius) {
		center = a;
		if (radius < 0) {			
			throw new RuntimeException("negative radius");
		}
		this.radius = radius;
	}

	private Circle2D(Vector2D a, Vector2D b) {
		center = a.add(b).scale(0.5);
		radius = FastMath.max(center.distanceTo(a), center.distanceTo(b));
	}

	/**
	 * Returns true if and only if the given point is contained in this circle.
	 */
	public boolean contains(Vector2D point) {
		return center.distanceTo(point) <= radius;
	}

	/**
	 * Returns true if and only if no point is outside of this circle. An empty
	 * list of points will return true.
	 */
	public boolean contains(List<Vector2D> points) {
		for (Vector2D point : points) {
			if (!contains(point)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Constructs a {@link Circle2D} from the given points via the selected
	 * algorithm.
	 * 
	 * N3 and N4 will return the smallest circle possible. N3 is O(n^3) while N4
	 * is O(n^4).
	 * 
	 * CENTROID is the fastest O(n), but in practice may occasionally return a
	 * circle that may be up to 30% larger than the optimal solution provided by
	 * N3
	 * 
	 * COLLAPSING_BUBBLE a O(n) algorithm that is a bit slower than CENTROID but
	 * returns the smallest possible circle about 80% of the time. The remaining
	 * cases will be generally a fraction of a percent larger than optimal. This
	 * is the current best option for most purposes.
	 */
	public Circle2D(List<Vector2D> points, SolutionAlgorithm solutionAlgorithm) {
		/*
		 * The presumption is that the points will nearly always be distinct.
		 * Some of the algorithms below work only when given at least three
		 * points, even when they are not distinct. Thus we handle the first
		 * three cases here.
		 */

		Circle2D c;
		switch (points.size()) {
		case 0:
			c = new Circle2D();
			break;
		case 1:
			c = new Circle2D(points.get(0));
			break;
		case 2:
			c = new Circle2D(points.get(0), points.get(1));
			break;
		default:
			switch (solutionAlgorithm) {
			case CENTROID:
				c = getCentroidSolution(points);
				break;
			case N3:
				c = getN3Solution(points);
				break;
			case N4:
				c = getN4Solution(points);
				break;
			case COLLAPSING_BUBBLE:
				c = getCollapsingBubbleSolution(points);
				break;
			default:				
				throw new RuntimeException("unhandled case " + solutionAlgorithm);
			}
			break;
		}
		center = c.center;
		radius = c.radius;
	}

	private Circle2D getCentroidSolution(List<Vector2D> points) {
		Circle2D c;

		MutableVector2D v = new MutableVector2D();
		for (Vector2D point : points) {
			v.add(point);
		}
		v.scale(1.0 / points.size());

		Vector2D center = new Vector2D(v);
		double r = Double.NEGATIVE_INFINITY;
		for (Vector2D point : points) {
			r = FastMath.max(r, center.distanceTo(point));
		}
		c = new Circle2D(center, r);

		return c;
	}

	/*
	 * Uses a centroid calculation to find a first point of contact between the
	 * circle and the points. This circle is then collapsed by moving the center
	 * toward the first contact point until a second contact point is
	 * established. The center is then moved toward the midpoint between the
	 * first two contact points until either a third contact point is found or
	 * the center reaches the midpoint of the chord.
	 */
	private Circle2D getCollapsingBubbleSolution(List<Vector2D> points) {

		// cen - the evolving best solution to the center

		// a - the first contact point
		// b - the second contact point
		// c - the third contact point - may not exist

		// Establish the first contact point

		Vector2D a = null;

		Vector2D cen = new Vector2D();
		for (Vector2D point : points) {
			cen = cen.add(point);
		}
		cen = cen.scale(1.0 / points.size());

		double r = Double.NEGATIVE_INFINITY;

		for (int i = 0; i < points.size(); i++) {
			Vector2D point = points.get(i);
			double distance = cen.distanceTo(point);
			if (distance > r) {
				a = point;				
				r = distance;
			}
		}

		// Establish the second contact point
		Vector2D b = null;
		double minJ = 1;
		for (int i = 0; i < points.size(); i++) {
			Vector2D v = points.get(i);
			if (v != a) {
				Vector2D m = a.add(v).scale(0.5);
				Vector2D q = v.sub(a);
				double j = m.sub(cen).dot(q) / a.sub(cen).dot(q);
				if (j > 0 && j <= 1) {
					if (j < minJ) {
						b = v;
						minJ = j;
					}
				}
			}
		}
		cen = a.sub(cen).scale(minJ).add(cen);
		r = cen.distanceTo(a);

		// Establish the third contact point
		Vector2D m1 = a.add(b).scale(0.5);
		minJ = 1;
		for (int i = 0; i < points.size(); i++) {
			Vector2D v = points.get(i);
			Vector2D g = v.sub(a);
			if (v != a && v != b) {
				Vector2D m2 = a.add(v).scale(0.5);
				double j = m2.sub(cen).dot(g) / m1.sub(cen).dot(g);
				if (j >= 0 && j <= 1) {
					if (j < minJ) {
						minJ = j;
					}
				}
			}
		}
		cen = m1.sub(cen).scale(minJ).add(cen);

		/*
		 * determine the radius from all the points to ensure every point will
		 * be inside the circle
		 */
		r = Double.NEGATIVE_INFINITY;
		for (int i = 0; i < points.size(); i++) {
			Vector2D point = points.get(i);
			double distance = cen.distanceTo(point);
			if (distance > r) {
				a = point;
				r = distance;
			}
		}
		return new Circle2D(cen, r);
	}

	private static class Interval {
		private final double lowerBound;
		private final double upperBound;

		public Interval(double lowerBound, double upperBound) {
			this.lowerBound = lowerBound;
			this.upperBound = upperBound;
		}

		public Interval intersect(Interval interval) {
			return new Interval(FastMath.max(lowerBound, interval.lowerBound), FastMath.min(upperBound, interval.upperBound));
		}

		public boolean isNaN() {
			return Double.isNaN(lowerBound) || Double.isNaN(upperBound);
		}

		public boolean isEmpty() {
			return isNaN() || lowerBound > upperBound;
		}

		@Override
		public String toString() {
			StringBuilder builder = new StringBuilder();
			builder.append("Interval [lowerBound=");
			builder.append(lowerBound);
			builder.append(", upperBound=");
			builder.append(upperBound);
			builder.append("]");
			return builder.toString();
		}
	}

	/*
	 * Returns the range of positions that could act as the center of a circle
	 * that would have a and b on its circumference and would contain c in its
	 * interior.
	 * 
	 * The positions are returned as an interval [j,inf) or (-inf,j] where j is
	 * the signed distance from the midpoint of the chord formed by a and b and
	 * the right hand perpendicular bisector of that chord.
	 */
	private Interval getInterval(Vector2D a, Vector2D b, Vector2D c) {
		Interval result;
		Vector2D m1 = a.add(b).scale(0.5);
		Vector2D p1 = b.sub(a).perpendicularRotation(Chirality.RIGHT_HANDED).normalize();
		Vector2D m2 = a.add(c).scale(0.5);
		Vector2D f = a.sub(c);
		double j = m2.sub(m1).dot(f) / p1.dot(f);
		if (p1.dot(c.sub(m1)) > 0) {
			result = new Interval(j, Double.POSITIVE_INFINITY);
		} else {
			result = new Interval(Double.NEGATIVE_INFINITY, j);
		}
		return result;
	}

	/*
	 * Returns the smallest circle that has a and b on its circumference where
	 * the signed distance along the right hand perpendicular bisector of the
	 * chord formed by a and b is a value in the given interval. This will only
	 * be valid if the interval was formed from a and b in the getInterval()
	 * method.
	 * 
	 */
	private Circle2D getCircle(Vector2D a, Vector2D b, Interval interval) {
		Vector2D m = a.add(b).scale(0.5);
		Vector2D p = b.sub(a).perpendicularRotation(Chirality.RIGHT_HANDED).normalize();
		double j = 0;
		if (interval.lowerBound > 0) {
			j = interval.lowerBound;
		} else if (interval.upperBound < 0) {
			j = interval.upperBound;
		}
		Vector2D cen = p.scale(j).add(m);
		double r = FastMath.max(cen.distanceTo(a), cen.distanceTo(b));
		return new Circle2D(cen, r);
	}

	/*
	 * For each pair of points, determine for each of the remaining points where
	 * center of the smallest circle might be. The result will be the smallest
	 * such circle.
	 */
	private Circle2D getN3Solution(List<Vector2D> points) {
		/*
		 * Create a default solution
		 */
		Circle2D result = new Circle2D();
		/*
		 * Loop through each pair of points. If the pair can be on a the
		 * circumference of a circle that contains all the points, the smallest
		 * such circle will be produced.
		 */
		for (int i = 0; i < points.size() - 1; i++) {
			for (int j = i + 1; j < points.size(); j++) {
				/*
				 * Assume that the full set of points that form the
				 * perpendicular bisector of the chord formed by points i and j
				 * will be valid centers a circle containing the remaining
				 * points.
				 * 
				 */
				Interval intervalForIJ = new Interval(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
				for (int k = 0; k < points.size(); k++) {
					if (k != i && k != j) {
						/*
						 * Determine the subset of the bisector that will allow
						 * the kth point to be in a circle that has point i and
						 * point j on its circumference
						 */
						Interval intervalForK = getInterval(points.get(i), points.get(j), points.get(k));

						/*
						 * Intersect this set of the points with the set so far
						 * collected. If a NaN has occured in the interval
						 * solution, we should ignore that solution. This
						 * indicates that the kth point is VERY close to either
						 * the ith or jth point.
						 */
						if (!intervalForK.isNaN()) {
							intervalForIJ = intervalForIJ.intersect(intervalForK);
						}
						/*
						 * If the interval for the i,j pair becomes empty, we
						 * need not examine the remaining kth points
						 */
						if (intervalForIJ.isEmpty()) {
							break;
						}
					}
				}
				/*
				 * If the interval is not empty, then determine the center from
				 * the interval
				 */
				if (!intervalForIJ.isEmpty()) {
					Circle2D circle = getCircle(points.get(i), points.get(j), intervalForIJ);
					// if this is a better solution, then take it as the result.
					if (circle.isFinite() && circle.radius < result.radius) {
						result = circle;
					}
				}
			}
		}

		// Precision issues can sometimes cause the radius of the result to fall
		// somewhat short, so we ensure the radius will include all of the
		// points.
		double r = Double.NEGATIVE_INFINITY;
		for (Vector2D point : points) {
			r = FastMath.max(r, result.center.distanceTo(point));
		}

		return new Circle2D(result.center, r);
	}

	private Circle2D getN4Solution(List<Vector2D> points) {

		/*
		 * Create a default solution that is not finite.
		 */
		Circle2D solution = new Circle2D();

		/*
		 * Any circle that contains all the points but does not have any point
		 * on its circumference is not optimal. Similarly, if the circle contain
		 * a single point, then the circle could be made smaller. A circle
		 * having two of the points on its circumference may only be optimal if
		 * those two points form a bisecting chord of the circle. The center of
		 * such a circle will be at the midpoint of that chord. For a circle
		 * having three or more points on its circumference, we need only to use
		 * two distinct chords -- thus three distinct points -- to determine the
		 * center.
		 * 
		 * 
		 * We will examine each pair of points for a potential solution,
		 * checking that the remaining points fall inside the circle formed. If
		 * we fail to find a solution using a pairs, we must consider all
		 * triplets of points.
		 * 
		 */

		for (int i = 0; i < points.size() - 1; i++) {
			for (int j = i + 1; j < points.size(); j++) {
				/*
				 * Form a circle from the ith and jth points that will have its
				 * center at the midpoint between them
				 */
				Circle2D circle = new Circle2D(points.get(i), points.get(j));
				boolean allPointsContained = true;
				for (Vector2D point : points) {
					if (!circle.contains(point)) {
						allPointsContained = false;
						break;
					}
				}
				if (allPointsContained && circle.radius < solution.radius) {
					solution = circle;
				}
			}
		}

		if (solution.isFinite()) {
			/*
			 * The original default solution was infinite. If it is now finite
			 * then it had to have been replaced and we thus already have the
			 * optimal solution.
			 */
			return solution;
		}

		for (int i = 0; i < points.size() - 2; i++) {
			for (int j = i + 1; j < points.size() - 1; j++) {
				for (int k = j + 1; k < points.size(); k++) {
					/*
					 * We intersect the perpendicular bisectors of the chord ab
					 * and chord bc to find the center.
					 * 
					 * We are solving the equation m1 + h1*p1 = m2+ h2*p2 where
					 * m1 and m2 are the midpoints of the chords. p1 and p2 are
					 * the perpendicular bisectors of the chords and h1 and h2
					 * are the scalar value needed to form the intersection of
					 * the bisectors.
					 * 
					 * We form d as the vector from a to b. It is perpendicular
					 * to the bisector of chord ab.
					 * 
					 * Thus, (m1 + h1*p1) o d = (m2+ h2*p2) o d = center of
					 * circle
					 * 
					 * Since p1 and d are perpendicular, we have
					 * 
					 * m1 o d = (m2+ h2*p2) o d, so h2 = (m1-m2) o d / p2 o d
					 * 
					 * This allows us to solve for the center. Note that we may
					 * drop the calculation of p1 and h1.
					 * 
					 */

					Vector2D a = points.get(i);
					Vector2D b = points.get(j);
					Vector2D c = points.get(k);
					Vector2D m1 = a.add(b).scale(0.5);
					Vector2D m2 = b.add(c).scale(0.5);
					Vector2D d = b.sub(a);
					Vector2D p = b.sub(c).perpendicularRotation(Chirality.RIGHT_HANDED);
					double h = m1.sub(m2).dot(d) / p.dot(d);
					Vector2D cen = p.scale(h).add(m2);
					double radius = FastMath.max(cen.distanceTo(a), cen.distanceTo(b));
					radius = FastMath.max(radius, cen.distanceTo(c));
					Circle2D circle = new Circle2D(cen, radius);

					boolean allPointsContained = true;
					for (Vector2D point : points) {
						if (!circle.contains(point)) {
							allPointsContained = false;
							break;
						}
					}
					if (allPointsContained && circle.radius < solution.radius) {
						solution = circle;
					}
				}
			}
		}

		return solution;
	}

}
