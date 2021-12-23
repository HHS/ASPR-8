package util.spherical;

/**
 * A RuntimeException thrown when a {@link SphericalPolygon} cannot be formed
 * from its {@link SphericalPoint} values.
 */
public class MalformedSphericalPolygonException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public MalformedSphericalPolygonException() {
		super();
	}

	public MalformedSphericalPolygonException(String message) {
		super(message);
	}

}
