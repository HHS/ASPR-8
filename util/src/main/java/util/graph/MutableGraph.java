package util.graph;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 
 * A generics-based, mutable graph of nodes(N) and edges(E). All iterators are
 * immutable and will throw UnsupportedOperationException runtime exception on
 * remove().
 * 
 *
 */
public final class MutableGraph<N, E> {

	private Set<N> nodes = new LinkedHashSet<>();

	private Set<E> edges = new LinkedHashSet<>();

	private Map<N, Set<E>> inEdges = new LinkedHashMap<>();

	private Map<N, Set<E>> outEdges = new LinkedHashMap<>();

	private Map<E, N> originNodeMap = new LinkedHashMap<>();

	private Map<E, N> destinationNodeMap = new LinkedHashMap<>();

	private Map<N, Map<N, Set<E>>> edgesMap = new LinkedHashMap<>();

	/**
	 * Adds the content of the given {@link Graph} to this {@link MutableGraph}
	 */
	public void addAll(Graph<N, E> graph) {

		for (N node : graph.getNodes()) {
			addNode(node);
		}

		for (E edge : graph.getEdges()) {
			N originNode = graph.getOriginNode(edge);
			N destinationNode = graph.getDestinationNode(edge);
			addEdge(edge, originNode, destinationNode);
		}

	}

	/*
	 * Returns a {@link Graph} backed by this {@link MutableGraph}. Mutations in
	 * this {@link MutableGraph} will be reflected in the resultant {@link
	 * Graph}
	 * 
	 * This method is package access only and should only be used to reduce the
	 * overhead costs of creating Graphs from MutableGraphs.
	 */
	Graph<N, E> asGraph() {
		return new Graph<>(this);
	}

	/**
	 * Returns a new {@link Graph} instance from the contents of this
	 * {@link MutableGraph}
	 */
	public Graph<N, E> toGraph() {
		Graph.Builder<N, E> builder = Graph.builder();
		builder.addAll(this);
		return builder.build();
	}

	/**
	 * Adds the content of the given {@link MutableGraph} to this
	 * {@link MutableGraph}
	 */
	public void addAll(MutableGraph<N, E> graph) {

		for (N node : graph.getNodes()) {
			addNode(node);
		}

		for (E edge : graph.getEdges()) {
			N originNode = graph.getOriginNode(edge);
			N destinationNode = graph.getDestinationNode(edge);
			addEdge(edge, originNode, destinationNode);
		}

	}

	/**
	 * Adds the edge to this graph, possibly replacing the edge if it is already
	 * in the graph. Adds the nodes if required.
	 */
	public void addEdge(E edge, N originNode, N destinationNode) {
		removeEdge(edge);
		addNode(originNode);
		addNode(destinationNode);
		edges.add(edge);
		inEdges.get(destinationNode).add(edge);
		outEdges.get(originNode).add(edge);
		originNodeMap.put(edge, originNode);
		destinationNodeMap.put(edge, destinationNode);

		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			map = new LinkedHashMap<>();
			edgesMap.put(originNode, map);
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			someEdges = new LinkedHashSet<>();
			map.put(destinationNode, someEdges);
		}
		someEdges.add(edge);

	}

	/**
	 * Adds the node to this graph.
	 */
	public void addNode(N node) {
		if (!nodes.contains(node)) {
			nodes.add(node);
			inEdges.put(node, new LinkedHashSet<E>());
			outEdges.put(node, new LinkedHashSet<E>());
		}
	}

	/**
	 * Returns true if and only if the edge is contained in the graph.
	 * 
	 * @param edge
	 * @return
	 */
	public boolean containsEdge(Object edge) {
		return edges.contains(edge);
	}

	/**
	 * Returns true if and only if the node is contained in the graph.
	 * 
	 * @param node
	 * @return
	 */
	public boolean containsNode(Object node) {
		return nodes.contains(node);
	}

	/**
	 * Returns the number of edges in this graph
	 * 
	 * @return
	 */
	public int edgeCount() {
		return edges.size();
	}

	/**
	 * Returns true if and only if the given edge connects the given origin and
	 * destination.
	 * 
	 * @param edge
	 * @param origin
	 * @param destination
	 * @return
	 */
	public boolean formsEdgeRelationship(Object edge, Object origin, Object destination) {
		N originNode = originNodeMap.get(edge);
		if (originNode == null) {
			return false;
		}
		if (!originNode.equals(origin)) {
			return false;
		}
		N destinationNode = destinationNodeMap.get(edge);

		if (!destinationNode.equals(destination)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns the destination node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getDestinationNode(E edge) {
		return destinationNodeMap.get(edge);
	}

	/**
	 * Supplies an iterator over all edges in the graph.
	 * 
	 * @return
	 */
	public List<E> getEdges() {
		return new ArrayList<>(edges);
	}

	/**
	 * Supplies a list over all edges between the given nodes.
	 * 
	 * @return
	 */
	public List<E> getEdges(N originNode, N destinationNode) {
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			return new ArrayList<>();
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(someEdges);
	}

	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getInboundEdgeCount(N node) {
		Set<E> set = inEdges.get(node);
		if (set == null) {
			return 0;
		}
		return set.size();
	}

	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * destination.
	 * 
	 * @param node
	 * @return
	 */
	public List<E> getInboundEdges(N node) {
		Set<E> set = inEdges.get(node);
		if (set == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(inEdges.get(node));
	}

	/**
	 * Supplies an iterator over all nodes in the graph.
	 * 
	 * @return
	 */
	public List<N> getNodes() {
		return new ArrayList<>(nodes);
	}

	/**
	 * Returns the origin node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getOriginNode(E edge) {
		return originNodeMap.get(edge);
	}

	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getOutboundEdgeCount(N node) {
		Set<E> set = outEdges.get(node);
		if (set == null) {
			return 0;
		}
		return set.size();

	}

	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * origin.
	 * 
	 * @param node
	 * @return
	 */
	public List<E> getOutboundEdges(N node) {
		Set<E> set = outEdges.get(node);
		if (set == null) {
			return new ArrayList<>();
		}
		return new ArrayList<>(outEdges.get(node));
	}

	/**
	 * Returns true if and only if the nodeCount() is zero
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return nodeCount() == 0;
	}

	/**
	 * Returns the number of nodes in this graph.
	 * 
	 * @return
	 */
	public int nodeCount() {
		return nodes.size();
	}

	/**
	 * Removes the edge from the graph.
	 */
	public void removeEdge(E edge) {
		if (!edges.contains(edge)) {
			return;
		}

		N originNode = originNodeMap.get(edge);
		outEdges.get(originNode).remove(edge);

		N destinationNode = destinationNodeMap.get(edge);
		inEdges.get(destinationNode).remove(edge);

		edges.remove(edge);
		destinationNodeMap.remove(edge);
		originNodeMap.remove(edge);

		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map != null) {
			Set<E> someEdges = map.get(destinationNode);
			if (someEdges != null) {
				someEdges.remove(edge);
				if (someEdges.size() == 0) {
					map.remove(destinationNode);
				}
			}
			if (map.size() == 0) {
				edgesMap.remove(originNode);
			}
		}
	}

	/**
	 * Removes the node from the graph, removing any associated edges.
	 */
	public void removeNode(N node) {
		if (!nodes.contains(node)) {
			return;
		}
		List<E> list = new ArrayList<>();
		list.addAll(inEdges.get(node));
		list.addAll(outEdges.get(node));
		for (E edge : list) {
			removeEdge(edge);
		}
		inEdges.remove(node);
		outEdges.remove(node);
		nodes.remove(node);

	}

	/**
	 * Returns the number of edges in this graph from the origin node to the
	 * destination node.
	 * 
	 * @return
	 */
	public int edgeCount(N originNode, N destinationNode) {
		Map<N, Set<E>> map = edgesMap.get(originNode);
		if (map == null) {
			return 0;
		}
		Set<E> someEdges = map.get(destinationNode);
		if (someEdges == null) {
			return 0;
		}
		return someEdges.size();

	}

	/**
	 * Returns a hash code that is the sum of the hash codes of its nodes and
	 * edges.
	 */
	@Override
	public int hashCode() {
		int result = 0;

		for (N node : getNodes()) {
			result += node.hashCode();
		}

		for (E edge : getEdges()) {
			result += edge.hashCode();
		}
		return result;
	}

	/**
	 * Returns true if and only if the given object is an instance of
	 * {@link Graph} or {@link MutableGraph} that contains the same nodes and
	 * edges as this {@link MutableGraph}
	 */

	@SuppressWarnings("rawtypes")
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}

		MutableGraph other;
		if (obj instanceof Graph) {
			Graph graph = (Graph) obj;
			other = graph.mutableGraph;
		} else if (obj instanceof MutableGraph) {
			other = (MutableGraph) obj;
		} else {
			return false;
		}

		if (other.nodeCount() != nodeCount()) {
			return false;
		}
		if (other.edgeCount() != edgeCount()) {
			return false;
		}

		if (nodeCount() != other.nodeCount()) {
			return false;
		}

		for (N node : getNodes()) {
			if (!other.containsNode(node)) {
				return false;
			}
		}

		if (edgeCount() != other.edgeCount()) {
			return false;
		}

		for (E edge : getEdges()) {
			if (!other.containsEdge(edge)) {
				return false;
			}
			N originNode = getOriginNode(edge);
			N destinationNode = getDestinationNode(edge);
			other.formsEdgeRelationship(edge, originNode, destinationNode);
		}

		return true;
	}

}
