package util.path;

import java.util.Optional;

/**
 * Interface for path solvers that retain previously solved paths.
 * 
 *
 * 
 */
public interface PathSolver<N, E> {

	public Optional<Path<E>> getPath(N originNode, N destinationNode);

}
