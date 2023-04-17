package util.spherical;

import net.jcip.annotations.Immutable;
import util.vector.MutableVector3D;
import util.vector.Vector3D;

/**
 * Represents an immutable point on the unit sphere. Instances are created via
 * included builder class.
 *
 *
 */
@Immutable
public final class SphericalPoint {

	/**
	 * Creates a new {@link SphericalPoint} from the given {@link Vector3D}
	 * 
	 * @throws MalformedSphericalPointException
	 *             <li>if the given {@link Vector3D} is not normalizable
	 * @throws NullPointerException
	 *             <li>if the given {@link Vector3D} is null
	 * 
	 */
	public SphericalPoint(Vector3D v) {

		position = v.normalize();

		if (!position.isNormal()) {
			throw new MalformedSphericalPointException("data cannot be normalized onto the unit sphere");
		}

	}

	/**
	 * Creates a new {@link SphericalPoint} from the given {@link muVector3D}
	 * 
	 * @throws MalformedSphericalPointException
	 *             <li>if the given {@link Vector3D} is not normalizable
	 */
	public SphericalPoint(MutableVector3D v) {
		position = new Vector3D(v).normalize();
		if (!position.isNormal()) {
			throw new MalformedSphericalPointException("data cannot be normalized onto the unit sphere");
		}
	}

	private final Vector3D position;

	/**
	 * Returns this {@link SphericalPoint} as a {@link MutableVector3D}
	 */
	public Vector3D getPosition() {
		return position;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SphericalPoint [position=");
		builder.append(position);
		builder.append("]");
		return builder.toString();
	}

}
