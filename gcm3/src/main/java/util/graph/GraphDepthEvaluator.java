package util.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.math3.util.FastMath;

import net.jcip.annotations.Immutable;

/**
 * GraphDepthEvaluator is a convenience utility designed to work with acyclic
 * graphs that represent dependencies between nodes. Its is used to establish an
 * ordering of nodes such that nodes that have no dependencies on other nodes
 * are deemed as rank zero, nodes that depend only on rank zero nodes are deemed
 * rank 1 and so on.
 * 
 * Dependency in the graph is represented by edges. An edge directed from node A
 * to node B is taken to mean that A depends on B.
 * 
 * @author Shawn Hatch
 * 
 */
@Immutable
public final class GraphDepthEvaluator<N> {

	private <E> GraphDepthEvaluator(Graph<N, E> graph) {
		MutableGraph<N, E> mutableGraph = new MutableGraph<>();
		mutableGraph.addAll(graph);
		int depth = 0;
		while (mutableGraph.nodeCount() > 0) {
			List<N> nodes = new ArrayList<>();

			for (N node : mutableGraph.getNodes()) {
				if (mutableGraph.getOutboundEdgeCount(node) == 0) {
					nodes.add(node);
				}
			}

			for (N node : nodes) {
				mutableGraph.removeNode(node);
			}

			depthToNodeSetMap.put(depth, nodes);
			for (N node : nodes) {
				nodeToDepthMap.put(node, depth);
			}
			depth++;

		}
		maxDepth = FastMath.max(0, depth - 1);

	}

	private final Map<Integer, List<N>> depthToNodeSetMap = new TreeMap<>();

	private final Map<N, Integer> nodeToDepthMap = new LinkedHashMap<>();

	private final int maxDepth;

	/**
	 * Returns the maximum depth for any node in the graph. Returns 0 for empty
	 * graphs.
	 */
	public int getMaxDepth() {
		return maxDepth;
	}

	/**
	 * Returns the depth for the given node. Depth 0 nodes have no predecessors.
	 * Depth k nodes have predecessors of depth less than k for k>0.
	 */
	public int getDepth(N node) {
		Integer result = nodeToDepthMap.get(node);
		if (result == null) {
			result = -1;
		}
		return result;
	}

	/**
	 * Returns the nodes associated with the given depth. Will return an empty
	 * list for depth values that are negative or exceed the max depth
	 */
	public List<N> getNodesForDepth(int depth) {
		List<N> result = new ArrayList<>();
		List<N> nodes = depthToNodeSetMap.get(depth);
		if (nodes != null) {
			result.addAll(nodes);
		}
		return result;
	}

	/**
	 * Returns the nodes of the graph in their ascending rank orders. Within a
	 * rank, the order is arbitrary but repeatable across instances of
	 * {@link GraphDepthEvaluator} for any given {@link Graph}.
	 */
	public List<N> getNodesInRankOrder() {
		List<N> result = new ArrayList<>();
		for (List<N> nodes : depthToNodeSetMap.values()) {
			result.addAll(nodes);
		}
		return result;
	}

	/**
	 * Static constructor for {@link GraphDepthEvaluator} that orders the nodes
	 * of the given graph by the dependency relationship represented by the
	 * graph's edges. Returns {@link Optional#empty()} if the graph contains any
	 * cycles.
	 */
	public static <N, E> Optional<GraphDepthEvaluator<N>> getGraphDepthEvaluator(Graph<N, E> graph) {
		if (Graphs.getGraphCyclisity(graph) == Graphs.GraphCyclisity.ACYCLIC) {
			GraphDepthEvaluator<N> result = new GraphDepthEvaluator<>(graph);
			return Optional.of(result);
		}
		return Optional.empty();
	}

}
