package util.path;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import util.graph.Graph;
import util.path.Paths.EdgeCostEvaluator;
import util.path.Paths.TravelCostEvaluator;
import util.wrappers.MultiKey;

/**
 * Manages shortest path solutions for a given graph with reasonable efficiency
 * using maps of previous path solutions and their sub-paths.
 * 
 * 
 */
public class MapPathSolver<N, E> implements PathSolver<N, E> {

	private static class SubPath<T> {

		Path<T> basePath;

		Path<T> solvedPath;

		int startIndex;

		int stopIndex;
	}

	private Map<MultiKey, Path<E>> pathMap = new LinkedHashMap<>();

	private Graph<N, E> graph;

	private Map<MultiKey, SubPath<E>> subPathMap = new LinkedHashMap<>();

	private EdgeCostEvaluator<E> edgeCostEvaluator;

	private TravelCostEvaluator<N> travelCostEvaluator;

	/**
	 * Create a path solver for the given graph using the edge cost evaluator
	 * and travel cost evaluator
	 * 
	 * @throws NullPointerException
	 *             <li>if the graph is null</li>
	 *             <li>if the edge cost evaluator is null</li>
	 *             <li>if the travel cost evaluator is null</li>
	 * 
	 * 
	 */
	public MapPathSolver(Graph<N, E> graph, EdgeCostEvaluator<E> edgeCostEvaluator, TravelCostEvaluator<N> travelCostEvaluator) {
		if (graph == null) {
			throw new NullPointerException("graph is null");
		}
		if (edgeCostEvaluator == null) {
			throw new NullPointerException("edge cost evaluator is null");
		}
		if (travelCostEvaluator == null) {
			throw new NullPointerException("travel cost evaluator is null");
		}

		this.graph = graph;
		this.edgeCostEvaluator = edgeCostEvaluator;
		this.travelCostEvaluator = travelCostEvaluator;
	}

	@Override
	public Optional<Path<E>> getPath(N originNode, N destinationNode) {
		Path<E> result;
		MultiKey key = new MultiKey(originNode, destinationNode);
		SubPath<E> subPath = subPathMap.get(key);
		if (subPath != null) {
			if (subPath.solvedPath == null) {
				List<E> edges = subPath.basePath.getEdges();
				List<E> subEdges = new ArrayList<>();
				for (int i = subPath.startIndex; i < subPath.stopIndex; i++) {
					subEdges.add(edges.get(i));
				}
				Path.Builder<E> builder = Path.builder();
				subEdges.forEach(edge -> builder.addEdge(edge));
				subPath.solvedPath = builder.build();
			}
			result = subPath.solvedPath;
		} else {
			Optional<Path<E>> optional = Paths.getPath(graph, originNode, destinationNode, edgeCostEvaluator, travelCostEvaluator);
			if (!optional.isPresent()) {
				return Optional.empty();
			}

			result = optional.get();
			List<E> edges = result.getEdges();
			for (int i = 0; i < edges.size(); i++) {
				N startNode = graph.getOriginNode(edges.get(i));
				for (int j = i; j < edges.size(); j++) {
					N endNode = graph.getDestinationNode(edges.get(j));
					key = new MultiKey(startNode, endNode);
					subPath = new SubPath<>();
					subPath.basePath = result;
					subPath.startIndex = i;
					subPath.stopIndex = j + 1;
					subPathMap.put(key, subPath);
				}
			}
		}
		pathMap.put(key, result);
		return Optional.of(result);
	}

}
