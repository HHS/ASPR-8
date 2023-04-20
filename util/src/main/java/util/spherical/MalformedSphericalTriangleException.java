package util.spherical;

/**
 * A RuntimeException thrown when a {@link SphericalTriangle} cannot be formed.
 *
 */
public class MalformedSphericalTriangleException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalTriangleException() {
		super();
	}

	public MalformedSphericalTriangleException(String message) {
		super(message);
	}

}
