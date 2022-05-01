package util.vector;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;

/**
 * A immutable vector class representing 3-dimensional components: v = (x,y,z).
 *
 * @author Shawn R. Hatch
 */
@Immutable
public final class Vector3D {

	private static final long ZERO_LONG_BITS = Double.doubleToLongBits(0);

	public final static double NORMAL_LENGTH_TOLERANCE = 1E-13;

	public final static double PERPENDICUALR_ANGLE_TOLERANCE = 1E-13;

	/*
	 * A function that returns the value if the value is in the interval [-1,1].
	 * Returns the nearest value from the interval otherwise.
	 */
	private static double crunch(final double value) {

		if (value > 1) {
			return 1;
		}
		if (value < -1) {
			return -1;
		}
		return value;
	}

	/*
	 * A function that masks Double.doubleToLongBits() by forcing the long bits
	 * representation of -0 to be that of +0.
	 */
	private static long toLongBits(final double value) {
		if (value == 0) {
			return ZERO_LONG_BITS;
		} else {
			return Double.doubleToLongBits(value);
		}
	}

	private final double x;

	private final double y;

	private final double z;

	/**
	 * Creates a {@link Vector3D} with component values (0,0,0).
	 */
	public Vector3D() {
		x = 0;
		y = 0;
		z = 0;
	}

	/**
	 * Creates a {@link Vector3D} with the component values (x,y,z).
	 *
	 */
	public Vector3D(final double x, final double y, final double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	/**
	 * Creates a {@link Vector3D} instance that is initialized with the
	 * coordinates values of the provided {@link MutableVector3D}.
	 */

	public Vector3D(final MutableVector3D mutableVector3D) {
		x = mutableVector3D.getX();
		y = mutableVector3D.getY();
		z = mutableVector3D.getZ();
	}

	/**
	 * Creates a new {@link Vector3D} instance that is initialized with the
	 * coordinates values of the provided {@link Vector3D}.
	 */
	public Vector3D(final Vector3D v) {
		x = v.x;
		y = v.y;
		z = v.z;
	}

	/**
	 * Returns a new {@link Vector3D} instance, adding the given values.
	 * 
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>
	 * <ul>
	 * <li><tt>v1.add(1,-1,-1) = (-1+1,2-1,6-1) = (0,1,5)</tt>.
	 * <li><tt>v1.add(0,0,0) = (-1,2,6)</tt>.
	 * </ul>
	 */
	public Vector3D add(final double x, final double y, final double z) {
		return new Vector3D(this.x + x, this.y + y, this.z + z);
	}

	/**
	 * Returns a new {@link Vector3D} instance, adding the given
	 * {@link Vector3D}.
	 * 
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.add(v2) = (-1+1,2-1,6-1) = (0,1,5)</tt>.
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.add(v2) = (-1,2,6)</tt>.
	 * </ul>
	 *
	 * @param v
	 *            the vector to be added.
	 */
	public Vector3D add(final Vector3D v) {
		return new Vector3D(x + v.x, y + v.y, z + v.z);
	}

	/**
	 * Returns a new {@link Vector3D} instance that is the sum of this
	 * {@link Vector2D} instance and the scaled values of the given
	 * {@link Vector3D}.
	 * 
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <ul>
	 * <li>
	 * <tt>v1.addScaled(v2,5) = (-1+5*(1),2+5*(-1),6+5*(-1)) = (-1+5,2-5,6-5) = (4,-3,1)</tt>.
	 * <li>
	 * <tt>v1.addScaled(v2,0) = (-1+0*(1),2+0*(-1),6+0*(-1)) = (-1+0,2+0,6+0) = (-1,2,6)</tt>.
	 * </ul>
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.addScaled(v2,5) = (-1,2,6)</tt>.
	 * </ul>
	 *
	 * @param v
	 *            the vector to be added.
	 * @param s
	 *            the magnitude of the scale.
	 */
	public Vector3D addScaled(final Vector3D v, final double scalar) {
		return new Vector3D(x + (scalar * v.x), y + (scalar * v.y), z + (scalar * v.z));
	}

	/**
	 * Returns the measure in radians of the lesser angle between this
	 * {@link Vector3D} and the given {@link Vector3D}.
	 * <p>
	 * Recall, the angle (in radians) between two vectors can be determined
	 * by:<br>
	 * Alpha = ArcCos[Dot[v1, v2] / (Norm[v1]*Norm[v2])]
	 * <p>
	 * If the provided vector is a 'zero vector' (i.e. <tt>(0,0,0)</tt>) the
	 * angle in radians will result in a <tt>NaN</tt>. If the provided vector is
	 * equal to this vector the angle in radians will be <tt>0.0</tt>.
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.angleInRadians(v2) = 2.5175154010533864</tt>
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.angleInRadians(v2) = NaN</tt>
	 * <li><tt>v1.angleInRadians(v1) = 0.0</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Dot_product#Geometric_definition">http://en.wikipedia.org/wiki/Dot_product#Geometric_definition</a>
	 * @see <a href=
	 *      "http://mathworld.wolfram.com/DotProduct.html">http://mathworld.wolfram.com/DotProduct.html</a>
	 *
	 */
	public double angle(final Vector3D v) {
		return FastMath.acos(crunch(dot(v) / (length() * v.length())));
	}

	/**
	 * Returns a new {@link Vector3D} that is the right handed cross product of
	 * this {@link Vector3D} and the given {@link Vector3D}.
	 * <p>
	 * Recall, that given two vectors that the cross product is the vector that
	 * is perpendicular to them. The cross product vector is therefore normal to
	 * the plane formed by the given two vectors. This is very useful for
	 * calculating a surface normal at a particular point given two distinct
	 * tangent vectors.
	 * <p>
	 * The calculation performed acts as a 'right cross product', meaning that
	 * 'this' vector is the 'left' vector and the given vector is the 'right'
	 * vector for purposes of calculating the cross product.
	 * <p>
	 * The cross product, <tt>v1 X v2</tt>, formulation is therefore,
	 *
	 * <pre>
	 * <tt>v1.cross(v2)</tt>
	 *
	 * = Norm[v1]Norm[v2]Sin[Alpha]
	 *
	 *        { i ,  j ,  k }
	 * = Det[ {v1x, v1y, v1z} ]
	 *        {v2x, v2y, v2z}
	 *
	 *   {  0 , -v1z,  v1y}    {v2x}
	 * = { v1z,   0 , -v1x} ] [{v2y}]
	 *   {-v1y,  v1z,   0 }    {v2z)
	 *
	 *     {(-v1z)(v2y) + (v1y)(v2z)}
	 * = [ {(v1z)(v2x)  - (v1x)(v2z)}  ]
	 *     {(-v1y)(v2x) + (v1z)(v2y)}
	 * </pre>
	 *
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>: <tt>v1.cross(v2) = (4,5,-1)</tt>.
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.cross(v2) = (0,0,0)</tt>.
	 * <li><tt>v1.cross(v1) = (0,0,0)</tt>.
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Cross_product">http://en.wikipedia.org/wiki/Cross_product</a>
	 * @see <a href=
	 *      "http://mathworld.wolfram.com/CrossProduct.html">http://mathworld.wolfram.com/CrossProduct.html</a>
	 *
	 * @param v
	 *            the 'right' cross vector.
	 */
	public Vector3D cross(final Vector3D v) {
		final double crossx = (y * v.z) - (v.y * z);
		final double crossy = (v.x * z) - (x * v.z);
		final double crossz = (x * v.y) - (v.x * y);
		return new Vector3D(crossx, crossy, crossz);
	}

	/**
	 * Returns the distance between this {@link Vector3D} and the given
	 * {@link Vector3D}.
	 * <p>
	 * This is Euclidean Distance (e.g. SQRT[(a-x)^2 + (b-y)^2 + (c-z)^2])
	 * value.
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.distanceTo(v2) = 7.874007874011811</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Euclidean_vector#Length">http://en.wikipedia.org/wiki/Euclidean_vector#Length</a>
	 *
	 * @param v
	 *            the vector to find the distance to.
	 *
	 * @return the distance between this vector and the given vector.
	 */
	public double distanceTo(final Vector3D v) {
		return FastMath.sqrt(sqr(v.x - x) + sqr(v.y - y) + sqr(v.z - z));
	}

	/**
	 * Returns the dot product of this {@link Vector3D} and the given
	 * {@link Vector3D}.
	 * <p>
	 * Recall that the Dot Product is also called the "Scalar Product" or "Inner
	 * Product".
	 * <p>
	 * The dot product, <tt>v1.v2</tt>, formulation is therefore,
	 *
	 * <pre>
	 * <tt>v1.dot(v2)</tt>
	 *
	 * = Norm[v1]Norm[v2]Sin[Alpha]
	 *
	 * = Transpose[v1]*v2
	 *
	 * = (v1.x * v2.x) + (v1.y * v2.y) + (v1.z * v2.z)
	 * </pre>
	 *
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.dot(v2) =(-1)*1+2*(-1)+6*(-1)= -9.0</tt>
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.dot(v2) = 0.0</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Dot_product">http://en.wikipedia.org/wiki/Dot_product</a>
	 * @see <a href=
	 *      "http://mathworld.wolfram.com/DotProduct.html">http://mathworld.wolfram.com/DotProduct.html</a>
	 *
	 * @param v
	 *            the vector for the dot product.
	 * @return the dot product
	 */
	public double dot(final Vector3D v) {
		return (x * v.x) + (y * v.y) + (z * v.z);
	}

	/**
	 * Equals contract of {@link Vector3D}. Two vectors a and b are equal if
	 * their x,y and z values convert to long bits in the same way. An exception
	 * is made for -0, which is calculated as if its long bits were representing
	 * +0. This is done to give a more intuitive meaning of equals.
	 */
	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final Vector3D other = (Vector3D) obj;
		if (toLongBits(x) != toLongBits(other.x)) {
			return false;
		}
		if (toLongBits(y) != toLongBits(other.y)) {
			return false;
		}
		if (toLongBits(z) != toLongBits(other.z)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the value at the given index: 0->x, 1->y, 2->z
	 */
	public double get(final int index) {
		switch (index) {
		case 0:
			return x;
		case 1:
			return y;
		case 2:
			return z;
		default:			
			throw new RuntimeException("index out of bounds " + index);
		}
	}

	/**
	 * Returns the x component of this vector
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y component of this vector
	 */
	public double getY() {
		return y;
	}

	/**
	 * Returns the z component of this vector
	 */
	public double getZ() {
		return z;
	}

	/**
	 * Hash code consistent with equals().
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		long temp;
		temp = toLongBits(x);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = toLongBits(y);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		temp = toLongBits(z);
		result = (prime * result) + (int) (temp ^ (temp >>> 32));
		return result;
	}

	/**
	 * Returns <tt>true</tt> if and only if all of the components are finite(not
	 * NaN, not infinite).
	 *
	 */
	public boolean isFinite() {
		return !isNaN() && !isInfinite();
	}

	/**
	 * Returns <tt>true</tt> if and only if any component is infinite.
	 *
	 */
	public boolean isInfinite() {
		return Double.isInfinite(x) || Double.isInfinite(y) || Double.isInfinite(z);
	}

	/**
	 * Returns <tt>true</tt> if and only if any component is NaN.
	 *
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
	}

	/**
	 * Returns true if and only if this vector is finite and has a length within
	 * {@link Vector3D#NORMAL_LENGTH_TOLERANCE} of unit length.
	 */
	public boolean isNormal() {
		final double len = length();
		return isFinite() && Double.isFinite(len) && (FastMath.abs(len - 1) < NORMAL_LENGTH_TOLERANCE);
	}

	/**
	 * Returns true if and only if this {@link Vector3D} is perpendicular to the
	 * given {@link Vector3D} within the
	 * {@link Vector3D#PERPENDICUALR_ANGLE_TOLERANCE}
	 */
	public boolean isPerpendicularTo(Vector3D v) {
		return FastMath.abs(angle(v) - FastMath.PI / 2) < PERPENDICUALR_ANGLE_TOLERANCE;
	}

	/**
	 * Returns the length of the vector, i.e. its norm.
	 * <p>
	 * This is vector length (e.g. SQRT[x^2 + y^2 + z^2]) value.
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>:
	 * <tt>v1.length() = SQRT[41] = 6.4031242374328485</tt>
	 * <li><tt>v1 = (0,0,0)</tt>: <tt>v1.length() = SQRT[0] = 0.0</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Euclidean_vector#Length">http://en.wikipedia.org/wiki/Euclidean_vector#Length</a>
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm</a>
	 */
	public double length() {
		return FastMath.sqrt((x * x) + (y * y) + (z * z));
	}

	/**
	 * Returns a new {@link Vector3D} instance that if equal to this
	 * {@link Vector3D} scaled to unit length.
	 * <p>
	 * Normalizing a vector scales the vector to a unit vector. If the vector
	 * has a length of zero, a {@link Vector3D} (NaN,NaN,NaN) will result.
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>:
	 * <tt>v1.normalize() = (-0.15617376188860607, 0.31234752377721214, 0.9370425713316364)</tt>
	 * <li><tt>v1 = (0,0,0)</tt>: <tt>v1.normalize() = (NaN, NaN, NaN)</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Normalized_vector">http://en.wikipedia.org/wiki/Normalized_vector</a>
	 */
	public Vector3D normalize() {
		final double invLength = 1.0 / length();
		return new Vector3D(x * invLength, y * invLength, z * invLength);
	}

	/**
	 * Returns a new {@link Vector3D} instance that is this {@link Vector3D}
	 * scaled by -1.
	 *
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>: <tt>v1.reverse() = (1,-2,-6)</tt>
	 * <li><tt>v1 = (0,0,0)</tt>: <tt>v1.reverse() = (0,0,0)</tt>
	 * </ul>
	 */
	public Vector3D reverse() {
		return scale(-1);
	}

	/**
	 * Returns a new {@link Vector3D} instance that is the result of Rotating
	 * this {@link Vector3D} about the given {@link Vector3D} rotator at the
	 * given angle in radians via a right hand rotation (i.e.
	 * counter-clockwise).
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>, <tt>v2 = (1,-1,-1)</tt>,
	 * <tt>v1.rotate(v2, theta)</tt>:
	 * <ul>
	 * <li><tt>theta = 0.0</tt> -> <tt>(-1.0, 2.0, 6.0)</tt>
	 * <li><tt>theta = Math.PI/2.0 = 1.5707963267948966</tt> ->
	 * (-5.309401076758505, 0.11324865405187179, 3.577350269189627)</tt>
	 * <li><tt>theta = Math.PI = 3.141592653589793</tt> -> (-5.000000000000002,
	 * 4.000000000000002, 1.7763568394002505E-15)</tt>
	 * <li><tt>theta = (3.0/2.0)*Math.PI = 4.71238898038469</tt> ->
	 * (-0.690598923241498, 5.88675134594813, 2.4226497308103747)</tt>
	 * <li><tt>theta = 2.0*Math.PI = 6.283185307179586</tt> ->
	 * (-0.9999999999999996, 2.000000000000001, 6.0)</tt>
	 * </ul>
	 * <li><tt>v1 = (-1,2,6)</tt>, <tt>v2 = (0,0,0)</tt>,
	 * <tt>theta = Math.PI = 3.141592653589793</tt>,
	 * <tt>v1.rotate(v2, theta)</tt> -> <tt>(NaN, NaN, NaN)</tt>
	 * <li><tt>v1 = (0,0,0)</tt>, <tt>v2 = (1,-1,-1)</tt>,
	 * <tt>theta = Math.PI = 3.141592653589793</tt>,
	 * <tt>v1.rotate(v2, theta)</tt> -> <tt>(0,0,0)</tt>
	 * </ul>
	 *
	 * @param rotator
	 *            the vector to rotate about.
	 * @param theta
	 *            the angle of rotation according to a right hand coordinate
	 *            system (i.e. counter-clockwise).
	 */
	public Vector3D rotateAbout(final Vector3D rotator, final double theta) {
		final MutableVector3D m = new MutableVector3D(this);
		final MutableVector3D r = new MutableVector3D(rotator);
		m.rotateAbout(r, theta);
		return new Vector3D(m);
	}

	/**
	 * Returns a new {@link Vector3D} instance that result from rotating this
	 * {@link Vector3D} toward the given {@link Vector3D} at the given angle in
	 * radians.
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>, <tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.rotateToward(v2, theta)</tt> produces:
	 * <ul>
	 * <li><tt>theta = 0.0</tt> -> (-0.9999999999999999, 2.0, 6.0)</tt>
	 * <li><tt>theta = Math.PI/2.0 = 1.5707963267948966</tt> ->
	 * (4.937707198786941, -3.548977049128114, 2.005943549507195)</tt>
	 * <li><tt>theta = Math.PI = 3.141592653589793</tt> -> (1.0000000000000007,
	 * -2.0000000000000004, -6.0)</tt>
	 * <li><tt>theta = (3.0/2.0)*Math.PI = 4.71238898038469</tt> ->
	 * (-4.937707198786941, 3.5489770491281134, -2.0059435495071956)</tt>
	 * <li><tt>theta = 2.0*Math.PI = 6.283185307179586</tt> ->
	 * (-1.000000000000001, 2.000000000000001, 5.999999999999999)</tt>
	 * </ul>
	 * <li><tt>v1 = (-1,2,6)</tt>, <tt>v2 = (0,0,0)</tt>,
	 * <tt>theta = Math.PI = 3.141592653589793</tt>,
	 * <tt>v1.rotate(v2, theta)</tt> -> <tt>(NaN, NaN, NaN)</tt>
	 * <li><tt>v1 = (0,0,0)</tt>, <tt>v2 = (1,-1,-1)</tt>,
	 * <tt>theta = Math.PI = 3.141592653589793</tt>,
	 * <tt>v1.rotate(v2, theta)</tt> -> <tt>(NaN, NaN, NaN)</tt>
	 * </ul>
	 * </ul>
	 *
	 * @param v
	 *            the vector to rotate toward.
	 * @param theta
	 *            the angle of rotation
	 */
	public Vector3D rotateToward(final Vector3D v, final double theta) {
		final MutableVector3D m = new MutableVector3D(this);
		m.rotateToward(new MutableVector3D(v), theta);
		return new Vector3D(m);
	}

	/**
	 * Returns a new {@link Vector3D} instance resulting from the scaling of
	 * this {@link Vector3D} by the given scaling factor.
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v1.scale(2) = (2,4,-12)</tt>
	 * <li><tt>v1.scale(-1) = (1,-2,-6)</tt>, <b>Note:</b> This is the same as
	 * <tt>v1.reverse()</tt>
	 * <li><tt>v1.scale(1) = (-1,2,6)</tt>
	 * <li><tt>v1.scale(0) = (0,0,0)</tt>
	 * </ul>
	 *
	 * @param m
	 *            the magnitude, e.g. scaling factor.
	 */
	public Vector3D scale(final double scalar) {
		return new Vector3D(x * scalar, y * scalar, z * scalar);
	}

	/*
	 * Squares the provided value.
	 *
	 * @param value to be squared
	 * 
	 * @return the value squared
	 */
	private double sqr(final double value) {
		return value * value;
	}

	/**
	 * Returns the square of the distance from this {@link Vector3D} to the
	 * given {@link Vector3D}.
	 * <p>
	 * This is Euclidean Distance squared (e.g. (a-x)^2 + (b-y)^2 + (c-z)^2)
	 * value.
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>:
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>: <tt>v1.squareDistanceTo(v2) = 62.0</tt>
	 * </ul>
	 */
	public double squareDistanceTo(final Vector3D v) {
		return ((v.x - x) * (v.x - x)) + ((v.y - y) * (v.y - y)) + ((v.z - z) * (v.z - z));
	}

	/**
	 * Returns the length of this {@link Vector3D} squared.
	 * <p>
	 * This is vector norm squared (e.g. x^2 + y^2 + z^2).
	 * <p>
	 * <b>Examples:</b>
	 * <ul>
	 * <li><tt>v1 = (-1,2,6)</tt>: <tt>v1.normSquared() = 41</tt>
	 * <li><tt>v1 = (0,0,0)</tt>: <tt>v1.normSquared() = 0</tt>
	 * </ul>
	 *
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Euclidean_vector#Length">http://en.wikipedia.org/wiki/Euclidean_vector#Length</a>
	 * @see <a href=
	 *      "http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm">http://en.wikipedia.org/wiki/Norm_(mathematics)#Euclidean_norm</a>
	 */
	public double squareLength() {
		return (x * x) + (y * y) + (z * z);
	}

	/**
	 * Returns a new {@link Vector3D} instance resulting from the subtraction of
	 * the scalar components from this {@link Vector3D}.
	 * <p>
	 * <b>Examples:</b> Given that <tt>v1 = (-1,2,6)</tt>
	 * <ul>
	 * <li><tt>v1.sub(1,-1,-1) = (-1-1,2-(-1),6-(-1)) = (-2,3,7)</tt>
	 * <li><tt>v1.sub(0,0,0) = (-1,2,6)</tt>
	 * </ul>
	 */
	public Vector3D sub(final double x, final double y, final double z) {
		return new Vector3D(this.x - x, this.y - y, this.z - z);
	}

	/**
	 * Returns a new {@link Vector3D} instance resulting from the subtraction of
	 * the given {@link Vector3D} from this {@link Vector3D}.
	 * <p>
	 * <b>Examples:</b> <tt>v1 = (-1,2,6)</tt>
	 * <ul>
	 * <li><tt>v2 = (1,-1,-1)</tt>:
	 * <tt>v1.sub(v2) = (-1-1,2-(-1),6-(-1)) = (-2,3,7)</tt>
	 * <li><tt>v2 = (0,0,0)</tt>: <tt>v1.sub(v2) = (-1,2,6)</tt>
	 * </ul>
	 *
	 * @param v
	 *            the vector to be added.
	 */
	public Vector3D sub(final Vector3D v) {
		return new Vector3D(x - v.x, y - v.y, z - v.z);
	}

	/**
	 * Returns a length 3 array of double [x,y,z] from this {@link Vector3D}
	 */
	public double[] toArray() {
		return new double[] { x, y, z };
	}

	/**
	 * Returns the string representation of this {@link Vector3D} in the form
	 *
	 * Vector3D [x=2.57,y=-34.1,z=8.73]
	 */
	@Override
	public String toString() {
		return "Vector3D [x=" + x + ", y=" + y + ", z=" + z + "]";
	}
}