package util.spherical;

/**
 * A RuntimeException thrown when a {@link SphericalPoint} cannot be formed due
 * to a non-normalizable input.
 *
 */
public class MalformedSphericalPointException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalPointException(String message) {
		super(message);
	}

}
