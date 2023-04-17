package util.vector;

/**
 * A RuntimeException thrown when a vector is not normal(length = 1);
 */
public class NonNormalVectorException extends RuntimeException {

	private static final long serialVersionUID = 5709709658484048137L;

	public NonNormalVectorException() {
		super();
	}

	public NonNormalVectorException(final String message) {
		super(message);
	}

}
