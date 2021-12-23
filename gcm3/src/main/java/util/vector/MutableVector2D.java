package util.vector;

import org.apache.commons.math3.util.FastMath;

import util.spherical.Chirality;

/**
 * A mutable 2-dimensional vector class supporting common 2D transforms.
 *
 * @author Shawn Hatch
 *
 */
public final class MutableVector2D {

	private static final long ZERO_LONG_BITS = Double.doubleToLongBits(0);

	public final static double NORMAL_LENGTH_TOLERANCE = 1E-13;

	/*
	 * corrects a value that should be in the interval [-1,1]
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

	private double x = 0;

	private double y = 0;

	/**
	 * Constructs a {@link MutableVector2D} where x and y are zero
	 */
	public MutableVector2D() {
	}

	/**
	 * Constructs a {@link MutableVector2D} with the given x and y values
	 */
	public MutableVector2D(final double x, final double y) {
		this.x = x;
		this.y = y;
	}

	/**
	 * Constructs a {@link MutableVector2D} from another {@link MutableVector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public MutableVector2D(final MutableVector2D v) {
		x = v.getX();
		y = v.getY();
	}

	/**
	 * Constructs a {@link MutableVector2D} from another {@link Vector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public MutableVector2D(final Vector2D v) {
		x = v.getX();
		y = v.getY();
	}

	/**
	 * Adds the given x and y values to the corresponding values of this
	 * {@link MutableVector2D}
	 */
	public void add(final double x, final double y) {
		this.x += x;
		this.y += y;
	}

	/**
	 * Adds the x and y of the given {@link MutableVector2D} to the
	 * corresponding values of this {@link MutableVector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public void add(final MutableVector2D v) {
		x += v.getX();
		y += v.getY();
	}

	/**
	 * Adds the x and y of the given {@link MutableVector2D} to the
	 * corresponding values of this {@link Vector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public void add(final Vector2D v) {
		x += v.getX();
		y += v.getY();
	}

	/**
	 * Add the given {@link MutableVector2D} as scaled by the scalar to this
	 * {@link MutableVector2D}
	 *
	 * For example, if v=(5,7) and w = (2,3), then v.addScaled(w,10) would yield
	 * v=(25,37)
	 *
	 */
	public void addScaled(final MutableVector2D v, final double scalar) {
		x += v.getX() * scalar;
		y += v.getY() * scalar;
	}

	/**
	 * Add the given {@link Vector2D} as scaled by the scalar to this
	 * {@link MutableVector2D}
	 *
	 * For example, if v=(5,7) and w = (2,3), then v.addScaled(w,10) would yield
	 * v=(25,37)
	 *
	 */
	public void addScaled(final Vector2D v, final double scalar) {
		x += v.getX() * scalar;
		y += v.getY() * scalar;
	}

	/**
	 * Returns the angle in radians between this {@link MutableVector2D} and the
	 * given {@link MutableVector2D}
	 */
	public double angle(final MutableVector2D v) {
		final double value = dot(v) / (v.length() * length());
		return FastMath.acos(crunch(value));
	}

	/**
	 * Returns the angle in radians between this {@link MutableVector2D} and the
	 * given {@link Vector2D}
	 */
	public double angle(final Vector2D v) {
		final double value = dot(v) / (v.length() * length());
		return FastMath.acos(crunch(value));
	}

	/**
	 * Assign the given values to this {@link MutableVector2D}
	 */
	public void assign(final double x, final double y) {
		this.x = x;
		this.y = y;

	}

	/**
	 * Assign the values of the given {@link MutableVector2D} to this
	 * {@link MutableVector2D}
	 */
	public void assign(final MutableVector2D v) {
		x = v.getX();
		y = v.getY();
	}

	/**
	 * Assign the values of the given {@link Vector2D} to this
	 * {@link MutableVector2D}
	 */
	public void assign(final Vector2D v) {
		x = v.getX();
		y = v.getY();
	}

	/**
	 * Returns 1 if the acute angle from this {@link MutableVector2D} to the
	 * given {@link MutableVector2D} is clockwise and -1 if it is counter
	 * clockwise. Returns 0 if the angle is zero.
	 */
	public int cross(final MutableVector2D v) {
		final double direction = (x * v.y) - (v.x * y);
		if (direction < 0) {
			return -1;
		}
		if (direction > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns 1 if the acute angle from this {@link MutableVector2D} to the
	 * given {@link Vector2D} is clockwise and -1 if it is counter clockwise.
	 * Returns 0 if the angle is zero.
	 */
	public int cross(final Vector2D v) {
		final double direction = (x * v.getY()) - (v.getX() * y);
		if (direction < 0) {
			return -1;
		}
		if (direction > 0) {
			return 1;
		}
		return 0;
	}

	/**
	 * Returns the distance between this {@link MutableVector2D} and the given
	 * {@link MutableVector2D}
	 */
	public double distanceTo(final MutableVector2D v) {
		return FastMath.sqrt(((x - v.x) * (x - v.x)) + ((y - v.y) * (y - v.y)));
	}

	/**
	 * Returns the distance between this {@link MutableVector2D} and the given
	 * {@link Vector2D}
	 */
	public double distanceTo(final Vector2D v) {
		return FastMath.sqrt(((x - v.getX()) * (x - v.getX())) + ((y - v.getY()) * (y - v.getY())));
	}

	/**
	 * Returns the dot product of this {@link MutableVector2D} and the given
	 * {@link MutableVector2D}
	 */
	public double dot(final MutableVector2D v) {
		return (x * v.x) + (y * v.y);
	}

	/**
	 * Returns the dot product of this {@link MutableVector2D} and the given
	 * {@link Vector2D}
	 */
	public double dot(final Vector2D v) {
		return (x * v.getX()) + (y * v.getY());
	}

	/**
	 * Equals contract of {@link MutableVector2D}. Two vectors a and b are equal
	 * if their x and y values convert to long bits in the same way. An
	 * exception is made for -0, which is calculated as if its long bits were
	 * representing +0. This is done to give a more intuitive meaning of equals.
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
		final MutableVector2D other = (MutableVector2D) obj;
		if (toLongBits(x) != toLongBits(other.x)) {
			return false;
		}
		if (toLongBits(y) != toLongBits(other.y)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the value at the given index: 0->x, 1->y
	 */
	public double get(final int index) {
		switch (index) {
		case 0:
			return x;
		case 1:
			return y;
		default:
			throw new RuntimeException("index out of bounds " + index);
		}
	}

	/**
	 * Returns the x component value of this {@link MutableVector2D}
	 */
	public double getX() {
		return x;
	}

	/**
	 * Returns the y component value of this {@link MutableVector2D}
	 */
	public double getY() {
		return y;
	}

	/**
	 * Returns a hash code consistent with the equals contract
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
		return result;
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are positive or
	 * negative infinity.
	 *
	 * @return <tt>true</tt> if the vector is infinite.
	 */
	public boolean isInfinite() {
		return Double.isInfinite(x) || Double.isInfinite(y);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are NaN 'Not a
	 * Number'.
	 */
	public boolean isNaN() {
		return Double.isNaN(x) || Double.isNaN(y);
	}

	/**
	 * Returns true if and only if this {@link MutableVector2D} is finite and
	 * has a length within {@link MutableVector2D#NORMAL_LENGTH_TOLERANCE} of
	 * unit length.
	 */
	public boolean isNormal() {
		final double len = length();
		return Double.isFinite(len) && (FastMath.abs(len - 1) < NORMAL_LENGTH_TOLERANCE);
	}

	/**
	 * Returns <tt>true</tt> if any of the vector components are not NaN (i.e.
	 * 'Not a Number') and not infinite.
	 */
	public boolean isFinite() {
		return !isNaN() && !isInfinite();
	}

	/**
	 * Returns the length of this {@link MutableVector2D}
	 */
	public double length() {
		return FastMath.sqrt((x * x) + (y * y));
	}

	/**
	 * Scales this {@link MutableVector2D} so that its length is 1.
	 */
	public void normalize() {
		final double len = length();
		x = x / len;
		y = y / len;
	}

	/**
	 * Rotates this {@link MutableVector2D} by 90 degrees under either a
	 * clockwise(left handed) or counter clockwise(right handed) rotation.
	 */
	public void perpendicularRotation(final Chirality chirality) {
		double newx, newy;
		if (chirality == Chirality.LEFT_HANDED) {
			newx = y;
			newy = -x;
		} else {
			newx = -y;
			newy = x;
		}
		x = newx;
		y = newy;
	}

	/**
	 * Reverses the direction of the this Vector2D. This is equivalent to
	 * scaling by -1.
	 */
	public void reverse() {
		x *= -1;
		y *= -1;
	}

	/**
	 * Rotates this {@link MutableVector2D} about the origin by the given angle
	 * in radians in a counter clockwise(right handed) manner.
	 */
	public void rotate(final double theta) {
		final MutableVector2D v = new MutableVector2D(-y, x);
		v.scale(FastMath.sin(theta));
		scale(FastMath.cos(theta));
		add(v);
	}

	/**
	 * Rotates this {@link MutableVector2D} about the origin through the acute
	 * angle to the given {@link MutableVector2D} by the given angle in radians.
	 */
	public void rotateToward(final MutableVector2D v, final double theta) {
		rotate(cross(v) * theta);
	}

	/**
	 * Rotates this {@link MutableVector2D} about the origin through the acute
	 * angle to the given {@link Vector2D} by the given angle in radians.
	 */
	public void rotateToward(final Vector2D v, final double theta) {
		rotate(cross(v) * theta);
	}

	/**
	 * Scales this {@link MutableVector2D} by the scalar
	 */
	public void scale(final double scalar) {
		x *= scalar;
		y *= scalar;
	}

	/**
	 * Sets the x component of this {@link MutableVector2D}
	 */
	public void setX(final double x) {
		this.x = x;
	}

	/**
	 * Sets the y component of this {@link MutableVector2D}
	 */
	public void setY(final double y) {
		this.y = y;
	}

	/**
	 * Returns the square distance between this {@link MutableVector2D} and the
	 * given {@link MutableVector2D}
	 */
	public double squareDistanceTo(final MutableVector2D v) {
		return ((x - v.x) * (x - v.x)) + ((y - v.y) * (y - v.y));
	}

	/**
	 * Returns the square distance between this {@link MutableVector2D} and the
	 * given {@link Vector2D}
	 */
	public double squareDistanceTo(final Vector2D v) {
		return ((x - v.getX()) * (x - v.getX())) + ((y - v.getY()) * (y - v.getY()));
	}

	/**
	 * Returns the square length of this {@link MutableVector2D}
	 */
	public double squareLength() {
		return (x * x) + (y * y);
	}

	/**
	 * Subtracts the given x and y values from the corresponding values of this
	 * {@link MutableVector2D}
	 */
	public void sub(final double x, final double y) {
		this.x -= x;
		this.y -= y;
	}

	/**
	 * Subtracts the x and y of the given {@link MutableVector2D} from the
	 * corresponding values of this {@link MutableVector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public void sub(final MutableVector2D v) {
		x -= v.x;
		y -= v.y;
	}

	/**
	 * Subtracts the x and y of the given {@link MutableVector2D} from the
	 * corresponding values of this {@link Vector2D}
	 *
	 * @throws NullPointerException
	 *             <li>if v is null
	 */
	public void sub(final Vector2D v) {
		x -= v.getX();
		y -= v.getY();
	}

	/**
	 * Returns a length 2 array of double [x,y]
	 */
	public double[] toArray() {
		return new double[] { x, y };
	}

	/**
	 * Returns the string representation in the form
	 *
	 * Vector2D [x=2.57,y=-34.1]
	 */
	@Override
	public String toString() {
		return "Vector2D [x=" + x + ", y=" + y + "]";
	}

	/**
	 * Sets each component of this {@link MutableVector2D} to zero. <b>Note:</b>
	 * This is the same as calling <tt>v.assign(0,0)</tt>.
	 */
	public void zero() {
		x = 0;
		y = 0;
	}

	public final static double PERPENDICUALR_ANGLE_TOLERANCE = 1E-13;

	/**
	 * Returns true if and only if this {@link MutableVector2D} is perpendicular
	 * to the given {@link MutableVector2D} within the
	 * {@link Vector3D#PERPENDICUALR_ANGLE_TOLERANCE}
	 */
	public boolean isPerpendicularTo(MutableVector2D v) {
		return FastMath.abs(angle(v) - FastMath.PI / 2) < PERPENDICUALR_ANGLE_TOLERANCE;
	}

	/**
	 * Returns true if and only if this {@link Vector2D} is perpendicular to the
	 * given {@link Vector2D} within the
	 * {@link Vector3D#PERPENDICUALR_ANGLE_TOLERANCE}
	 */
	public boolean isPerpendicularTo(Vector2D v) {
		return FastMath.abs(angle(v) - FastMath.PI / 2) < PERPENDICUALR_ANGLE_TOLERANCE;
	}

}
