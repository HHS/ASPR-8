package util.spherical;

/**
 * A RuntimeException thrown when a {@link SphericalArc} cannot be formed due to
 * a null input or by two vertices that are too close together.
 *
 */
public class MalformedSphericalArcException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalArcException() {
		super();
	}

	public MalformedSphericalArcException(String message) {
		super(message);
	}

}
