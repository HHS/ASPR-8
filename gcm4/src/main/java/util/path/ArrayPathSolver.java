package util.path;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import util.graph.Graph;
import util.path.Paths.EdgeCostEvaluator;
import util.path.Paths.TravelCostEvaluator;

/**
 * Manages shortest path solutions for a given graph with reasonable efficiency
 * using arrays of previous path solutions and their sub-paths.
 * 
 * 
 */
public final class ArrayPathSolver<N, E> implements PathSolver<N, E> {

	private Graph<N, E> graph;

	private EdgeCostEvaluator<E> edgeCostEvaluator;

	private TravelCostEvaluator<N> travelCostEvaluator;

	@SuppressWarnings("unchecked")
	public ArrayPathSolver(Graph<N, E> graph, EdgeCostEvaluator<E> edgeCostEvaluator, TravelCostEvaluator<N> travelCostEvaluator) {
		this.graph = graph;
		this.edgeCostEvaluator = edgeCostEvaluator;
		this.travelCostEvaluator = travelCostEvaluator;
		int n = graph.nodeCount();
		navigationArray = (E[][]) Array.newInstance(Object.class, n, n);
		for (N node : graph.getNodes()) {
			nodeMap.put(node, nodeMap.size());
		}
	}

	private E[][] navigationArray;

	private Map<N, Integer> nodeMap = new LinkedHashMap<>();

	@Override
	public Optional<Path<E>> getPath(N originNode, N destinationNode) {

		if (!graph.containsNode(originNode)) {
			return Optional.empty();
		}

		if (!graph.containsNode(destinationNode)) {
			return Optional.empty();
		}

		Integer originIndex = nodeMap.get(originNode);
		Integer destinationIndex = nodeMap.get(destinationNode);
		E e = navigationArray[originIndex][destinationIndex];
		if (e == null) {
			if (!solve(originNode, destinationNode)) {
				return Optional.empty();
			}
		}

		Path.Builder<E> pathBuilder = Path.builder();
		while (true) {
			e = navigationArray[originIndex][destinationIndex];
			if (e == null) {
				return Optional.empty();
			}
			pathBuilder.addEdge(e);
			originIndex = nodeMap.get(graph.getDestinationNode(e));
			if (originIndex == destinationIndex) {
				break;
			}
		}
		return Optional.of(pathBuilder.build());
	}

	private boolean solve(N origin, N destination) {

		Optional<Path<E>> optional = Paths.getPath(graph, origin, destination, edgeCostEvaluator, travelCostEvaluator);
		if (!optional.isPresent()) {
			return false;
		}

		Path<E> path = optional.get();

		List<N> originList = new ArrayList<>();
		List<N> destinationList = new ArrayList<>();
		List<E> edges = path.getEdges();
		for (E edge : edges) {
			originList.add(graph.getOriginNode(edge));
			destinationList.add(graph.getDestinationNode(edge));
		}

		int n = originList.size();
		for (int i = 0; i < n; i++) {
			int sourceIndex = nodeMap.get(originList.get(i));
			for (int j = i; j < n; j++) {
				int targetIndex = nodeMap.get(destinationList.get(j));
				navigationArray[sourceIndex][targetIndex] = edges.get(i);
			}
		}
		return true;

	}

}
