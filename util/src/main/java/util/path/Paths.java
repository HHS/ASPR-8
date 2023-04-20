package util.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;

import util.graph.Graph;
import util.path.Path.Builder;

/**
 * 
 * Solves a shortest path through a graph from an origin node to a destination
 * node.The solver uses two optional, auxiliary, client supplied objects: 1) an
 * EdgeCostEvaluator which returns the cost of an edge and 2) a
 * TravelCostEvaluator which determines the shortest possible cost across the
 * graph from one node to another.
 * 
 * The EdgeCostEvaluator should always return a non-negative value and should
 * return a stable value over the life-span of this utility. If the
 * EdgeCostEvaluator is null, the cost of an edge is set arbitrarily to 1.
 * 
 * The TravelCostEvaluator is also optional and exists to give the solver
 * insight into long distance costs. It should return a stable non-negative
 * value.
 * 
 * A typical TravelCostEvaluator example would be for nodes in a graph that
 * represent physical positions. The TravelCostEvaluator could return the
 * straight-line minimum distance between nodes. Note that the nodes do not have
 * to share an edge nor even be connected in the graph. By supplying a
 * TravelCostEvaluator, the client can greatly improve this solver's performance
 * in large networks where nodes represent positions in space. Essentially, the
 * TravelCostEvaluator allows the algorithm to expand its exploration of the
 * graph in an ellipse whose major axis is aligned to the origin and
 * destination, rather than doing a expanding sphere search.
 * 
 * If a path cannot be found then a degenerate, node-less path is returned.
 * 
 * 
 */
public final class Paths {

	public interface EdgeCostEvaluator<E> {
		public double getEdgeCost(E edge);
	}

	public static interface TravelCostEvaluator<N> {
		public double getMinimumCost(N originNode, N destination);
	}

	private static class CostedNode<N, E> {

		CostedNode(N n, double auxillaryCost) {
			this.n = n;
			this.auxillaryCost = auxillaryCost;
		}

		private boolean visited;

		private final N n;

		private double cost = -1;

		private final double auxillaryCost;

		private E edge;

	}

	private static class PrioritizedNode<N, E> implements Comparable<PrioritizedNode<N, E>> {

		private PrioritizedNode(CostedNode<N, E> nodeWrapper) {
			this.costedNode = nodeWrapper;
			this.cost = nodeWrapper.cost + nodeWrapper.auxillaryCost;
		}

		private final CostedNode<N, E> costedNode;

		private final double cost;

		@Override
		public int compareTo(PrioritizedNode<N, E> other) {
			return Double.compare(cost, other.cost);
		}

	}

	private Paths() {

	}

	/**
	 * Returns an Optional containing a Path of E if such path could be found.
	 * 
	 * @throws NullPointerException
	 *             <li>if the graph is null</li>
	 *             <li>if the origin node is null</li>
	 *             <li>if the destination node null</li>
	 *             <li>if the edge cost evaluator is null</li>
	 *             <li>if the travel cost evaluator is null</li>
	 * 
	 */
	public static <N, E> Optional<Path<E>> getPath(Graph<N, E> graph, N originNode, N destinationNode, EdgeCostEvaluator<E> edgeCostEvaluator, TravelCostEvaluator<N> travelCostEvaluator) {

		if (graph == null) {
			throw new NullPointerException("graph is null");
		}

		if (originNode == null) {
			throw new NullPointerException("origin node is null");
		}

		if (destinationNode == null) {
			throw new NullPointerException("destination node is null");
		}

		if (edgeCostEvaluator == null) {
			throw new NullPointerException("edge cost evaluator is null");
		}

		if (travelCostEvaluator == null) {
			throw new NullPointerException("travel cost evaluator is null");
		}

		if (!graph.containsNode(originNode)) {
			return Optional.empty();
		}

		if (!graph.containsNode(destinationNode)) {
			return Optional.empty();
		}

		// The use of a HashMap instead of a linked hash map is intentional and
		// implementors should guard against repercussions of iteration over
		// this map.
		final Map<N, CostedNode<N, E>> map = new HashMap<>();
		PriorityQueue<PrioritizedNode<N, E>> priorityQueue = new PriorityQueue<>();

		CostedNode<N, E> originNodeWrapper = new CostedNode<>(originNode, travelCostEvaluator.getMinimumCost(originNode, destinationNode));
		map.put(originNode, originNodeWrapper);

		// Note that the first node placed on the queue will be unvisited and
		// therefore has an invalid cost. This should not matter since it is the
		// only node on the queue
		priorityQueue.add(new PrioritizedNode<>(originNodeWrapper));

		while (!priorityQueue.isEmpty()) {

			// pop off the first element
			CostedNode<N, E> pushNodeWrapper = priorityQueue.remove().costedNode;
			if (pushNodeWrapper.visited) {
				continue;
			}
			pushNodeWrapper.visited = true;

			CostedNode<N, E> destinationNodeWrapper = map.get(destinationNode);
			if (destinationNodeWrapper != null) {
				if (destinationNodeWrapper.cost >= 0) {
					if (pushNodeWrapper.cost >= destinationNodeWrapper.cost) {
						break;
					}
				}
			}

			for (E edge : graph.getOutboundEdges(pushNodeWrapper.n)) {
				N targetNode = graph.getDestinationNode(edge);
				CostedNode<N, E> targetNodeWrapper = map.get(targetNode);
				double edgeCost = edgeCostEvaluator.getEdgeCost(edge);

				if (Double.isInfinite(edgeCost)) {
					continue;
				}
				if (pushNodeWrapper.cost >= 0) {
					edgeCost += pushNodeWrapper.cost;
				}

				if (targetNodeWrapper == null) {
					targetNodeWrapper = new CostedNode<>(targetNode, travelCostEvaluator.getMinimumCost(targetNode, destinationNode));
					targetNodeWrapper.cost = edgeCost;
					targetNodeWrapper.edge = edge;
					map.put(targetNode, targetNodeWrapper);
					priorityQueue.add(new PrioritizedNode<>(targetNodeWrapper));
				} else {
					if ((targetNodeWrapper.cost < 0) || (edgeCost < targetNodeWrapper.cost)) {
						targetNodeWrapper.cost = edgeCost;
						targetNodeWrapper.edge = edge;
						priorityQueue.add(new PrioritizedNode<>(targetNodeWrapper));
					}
				}
			}
		}

		CostedNode<N, E> destinationWrapper = map.get(destinationNode);
		List<E> edges = new ArrayList<>();
		// assess whether we have a solution

		if ((destinationWrapper != null) && (destinationWrapper.cost >= 0)) {

			N node = destinationNode;

			while (true) {

				CostedNode<N, E> visitedNodeWrapper = map.get(node);
				E edge = visitedNodeWrapper.edge;
				visitedNodeWrapper.edge = null;
				if (edge == null) {
					break;
				}
				edges.add(edge);
				node = graph.getOriginNode(edge);
				if (node.equals(originNode)) {
					break;
				}
			}
			Collections.reverse(edges);
		}

		if (edges.isEmpty()) {
			return Optional.empty();
		}

		Builder<E> builder = Path.builder();
		for (E edge : edges) {
			builder.addEdge(edge);
		}

		return Optional.of(builder.build());
	}

	public static <E> double getCost(Path<E> path, EdgeCostEvaluator<E> edgeCostEvaluator) {
		double result = 0;
		for (E edge : path.getEdges()) {
			result += edgeCostEvaluator.getEdgeCost(edge);
		}
		return result;
	}

}