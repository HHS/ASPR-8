package util.vector;

/**
 * A RuntimeException thrown when a vector is not normal(length = 1);
 */
public class NonNormalVectorException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NonNormalVectorException() {
		super();
	}

	public NonNormalVectorException(final String message) {
		super(message);
	}

}
