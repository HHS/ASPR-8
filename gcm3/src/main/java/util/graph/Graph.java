package util.graph;

import java.util.List;

import net.jcip.annotations.Immutable;

@Immutable
public final class Graph<N, E> {

	protected final MutableGraph<N, E> mutableGraph;

	Graph(MutableGraph<N, E> mutableGraph) {
		this.mutableGraph = mutableGraph;
	}

	/**
	 * Returns a hash code that is the sum of the hash codes of its nodes and
	 * edges.
	 */
	@Override
	public int hashCode() {
		return mutableGraph.hashCode();
	}

	/**
	 * Returns true if and only if the given object is an instance of
	 * {@link Graph} or {@link MutableGraph} that contains the same nodes and
	 * edges as this {@link Graph}
	 */
	@Override
	public boolean equals(Object obj) {
		return mutableGraph.equals(obj);
	}

	public static <N, E> Builder<N, E> builder() {
		return new Builder<>();
	}

	//@Source(status = TestStatus.REQUIRED, proxy = Graph.class)
	public static class Builder<N, E> {

		private MutableGraph<N, E> mutableGraph = new MutableGraph<>();

		private Builder() {

		}

		public Graph<N, E> build() {
			try {
				return new Graph<>(mutableGraph);
			} finally {
				mutableGraph = new MutableGraph<>();
			}
		}

		/**
		 * Adds the node to this graph.
		 */
		public Builder<N, E> addNode(N node) {
			mutableGraph.addNode(node);
			return this;
		}

		/**
		 * Adds the edge to this graph, possibly replacing the edge if it is
		 * already in the graph. Adds the nodes if required.
		 */
		public Builder<N, E> addEdge(E edge, N originNode, N destinationNode) {
			mutableGraph.addEdge(edge, originNode, destinationNode);
			return this;
		}

		/**
		 * Adds the content of the given {@link Graph} to this {@link Graph}
		 */
		public Builder<N, E> addAll(Graph<N, E> graph) {
			mutableGraph.addAll(graph);
			return this;
		}

		/**
		 * Adds the content of the given {@link MutableGraph} to this
		 * {@link MutableGraph}
		 */
		public Builder<N, E> addAll(MutableGraph<N, E> graph) {
			mutableGraph.addAll(graph);
			return this;
		}

	}

	/**
	 * Returns true if and only if the node is contained in the graph.
	 * 
	 * @param node
	 * @return
	 */
	public boolean containsNode(Object node) {
		return mutableGraph.containsNode(node);
	}

	/**
	 * Returns true if and only if the edge is contained in the graph.
	 * 
	 * @param edge
	 * @return
	 */
	public boolean containsEdge(Object edge) {
		return mutableGraph.containsEdge(edge);
	}

	/**
	 * Returns the number of edges in this graph
	 * 
	 * @return
	 */
	public int edgeCount() {
		return mutableGraph.edgeCount();
	}

	/**
	 * Returns the destination node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getDestinationNode(E edge) {
		return mutableGraph.getDestinationNode(edge);
	}

	/**
	 * Returns the origin node for the given edge.
	 * 
	 * @param edge
	 * @return
	 */
	public N getOriginNode(E edge) {
		return mutableGraph.getOriginNode(edge);
	}

	/**
	 * Returns the number of nodes in this graph.
	 * 
	 * @return
	 */
	public int nodeCount() {
		return mutableGraph.nodeCount();
	}

	/**
	 * Returns true if and only if the nodeCount() is zero
	 * 
	 * @return
	 */
	public boolean isEmpty() {
		return mutableGraph.isEmpty();
	}

	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getInboundEdgeCount(N node) {
		return mutableGraph.getInboundEdgeCount(node);
	}

	/**
	 * Returns the number of edges going into the given node
	 * 
	 * @param node
	 * @return
	 */
	public int getOutboundEdgeCount(N node) {
		return mutableGraph.getOutboundEdgeCount(node);
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
		return mutableGraph.formsEdgeRelationship(edge, origin, destination);
	}

	/**
	 * Supplies an iterator over all nodes in the graph.
	 * 
	 * @return
	 */
	public List<N> getNodes() {
		return mutableGraph.getNodes();
	}

	/**
	 * Supplies an iterable over the edges from the origin node to the
	 * destination node.
	 * 
	 * @param originNode
	 * @param destinationNode
	 * @return
	 */
	public List<E> getEdges() {
		return mutableGraph.getEdges();
	}

	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * destination.
	 * 
	 * @param node
	 * @return
	 */
	public List<E> getInboundEdges(N node) {
		return mutableGraph.getInboundEdges(node);
	}

	/**
	 * Supplies an iterator over all edges in the graph that have node as their
	 * origin.
	 * 
	 * @param node
	 * @return
	 */
	public List<E> getOutboundEdges(N node) {
		return mutableGraph.getOutboundEdges(node);
	}

	/**
	 * Supplies an iterator over all edges between the given nodes.
	 * 
	 * @return
	 */
	public List<E> getEdges(N originNode, N destinationNode) {
		return mutableGraph.getEdges(originNode, destinationNode);
	}

	/**
	 * Returns the number of edges in this graph from the origin node to the
	 * destination node.
	 * 
	 * @return
	 */
	public int edgeCount(N originNode, N destinationNode) {
		return mutableGraph.edgeCount(originNode, destinationNode);
	}

	/**
	 * Returns a new {@link MutableGraph} instance from the contents of this
	 * {@link Graph}
	 */
	public MutableGraph<N, E> toMutableGraph() {
		MutableGraph<N, E> result = new MutableGraph<>();
		result.addAll(this);
		return result;
	}

}
