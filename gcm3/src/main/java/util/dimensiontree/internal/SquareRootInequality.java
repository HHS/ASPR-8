package util.dimensiontree.internal;

/**
 * A utility class for determining if sqrt(a) + sqrt(b) < sqrt(c) without
 * calculating square roots.
 * 
 * @author Shawn Hatch
 *
 */
public final class SquareRootInequality {

	private SquareRootInequality() {

	}

	/**
	 * Returns true if and only if sqrt(a) + sqrt(b) < sqrt(c). Used in distance
	 * comparisons where square distances are known and calculating square roots
	 * should be avoided.
	 */
	public static boolean evaluate(double aSquare, double bSquare, double cSquare) {
		/*
		 * We want to know when a+b<c. However, calculating these values would
		 * require using square roots and we can achieve better performance by
		 * only using square values.
		 * 
		 * a+b < c becomes...
		 * 
		 * a^2 + 2ab + b^2 < c^2 becomes...
		 * 
		 * 2ab < c^2 - a^2 - b^2 becomes...
		 * 
		 * 4 a^2 b^2 < (c^2 - a^2 - b^2)^2 with the caveat that c^2 - a^2 - b^2
		 * > 0
		 */
		double d = cSquare - aSquare - bSquare;
		return d >= 0 && 4 * aSquare * bSquare < d * d;
	}

}
