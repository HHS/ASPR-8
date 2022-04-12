package util.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.graph.Graph;
import util.path.Paths.EdgeCostEvaluator;
import util.path.Paths.TravelCostEvaluator;
import util.vector.Vector2D;

/**
 * Test class for {@link Paths}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Paths.class)
public class AT_Paths {

	/**
	 * Tests {@link Paths#getCost(Path, EdgeCostEvaluator)}
	 */
	@Test
	@UnitTestMethod(name = "getCost", args = { Path.class, EdgeCostEvaluator.class })
	public void testGetCost() {
		Path.Builder<String> builder = Path.builder();
		Path<String> path = builder.build();
		assertEquals(0, Paths.getCost(path, (edge) -> 1), 0);

		builder.addEdge("A->B");
		builder.addEdge("B->C");
		builder.addEdge("C->D");
		path = builder.build();
		assertEquals(3, Paths.getCost(path, (edge) -> 1), 0);

		builder.addEdge("A->B");
		builder.addEdge("B->C");
		builder.addEdge("C->D");
		path = builder.build();
		assertEquals(2, Paths.getCost(path, (edge) -> {
			if (edge.contains("B")) {
				return 1;
			}
			return 0;
		}), 0);

	}

	private static class Edge {
		private final Node originNode;
		private final Node destinationNode;
		private final String name;

		private Edge(Node originNode, Node destinationNode) {
			this.originNode = originNode;
			this.destinationNode = destinationNode;
			this.name = originNode.name + "->" + destinationNode.name;

		}

		private double cost() {
			return originNode.position.distanceTo(destinationNode.position);
		}

		@Override
		public String toString() {
			return name;
		}

	}

	private static class Node {
		private final String name;
		private final Vector2D position;

		private Node(String name, Vector2D position) {
			this.name = name;
			this.position = position;
		}

		@Override
		public String toString() {
			return name;
		}

	}

	/*
	 * The travel cost will be the straight-line distance
	 */
	private final static TravelCostEvaluator<Node> TRAVEL_COST_EVALUATOR = new TravelCostEvaluator<AT_Paths.Node>() {

		@Override
		public double getMinimumCost(Node originNode, Node destination) {
			return originNode.position.distanceTo(destination.position);
		}
	};

	private final static EdgeCostEvaluator<Edge> EDGE_COST_EVALUATOR = new EdgeCostEvaluator<AT_Paths.Edge>() {

		@Override
		public double getEdgeCost(Edge edge) {
			return edge.cost();
		}
	};

	/**
	 * Tests
	 * {@link Paths#getPath(Graph, Object, Object, EdgeCostEvaluator, TravelCostEvaluator)}
	 */
	@Test
	@UnitTestMethod(name = "getPath", args = { Graph.class, Object.class, Object.class, EdgeCostEvaluator.class, TravelCostEvaluator.class })
	public void testGetPath() {

		// create a few nodes
		Node nodeA = new Node("A", new Vector2D(15, 7));
		Node nodeB = new Node("B", new Vector2D(12, 4));
		Node nodeC = new Node("C", new Vector2D(17, 4));
		Node nodeD = new Node("D", new Vector2D(10, 1));
		Node nodeE = new Node("E", new Vector2D(16, 2));
		Node nodeF = new Node("F", new Vector2D(20, 3));
		Node nodeG = new Node("G", new Vector2D(21, 7));

		// create a few edges
		Edge edgeAB = new Edge(nodeA, nodeB);
		Edge edgeAC = new Edge(nodeA, nodeC);
		Edge edgeBC = new Edge(nodeB, nodeC);
		Edge edgeBD = new Edge(nodeB, nodeD);
		Edge edgeCE = new Edge(nodeC, nodeE);
		Edge edgeED = new Edge(nodeE, nodeD);
		Edge edgeEF = new Edge(nodeE, nodeF);
		Edge edgeFG = new Edge(nodeF, nodeG);
		Edge edgeGA = new Edge(nodeG, nodeA);

		List<Edge> edges = new ArrayList<>();
		edges.add(edgeAB);
		edges.add(edgeAC);
		edges.add(edgeBC);
		edges.add(edgeBD);
		edges.add(edgeCE);
		edges.add(edgeED);
		edges.add(edgeEF);
		edges.add(edgeFG);
		edges.add(edgeGA);

		// build the graph
		Graph.Builder<Node, Edge> graphBuilder = Graph.builder();
		edges.forEach(edge -> graphBuilder.addEdge(edge, edge.originNode, edge.destinationNode));
		Graph<Node, Edge> graph = graphBuilder.build();

		Path.Builder<Edge> pathBuilder = Path.builder();

		// solve for path from A to D
		pathBuilder.addEdge(edgeAB);
		pathBuilder.addEdge(edgeBD);
		Path<Edge> expectedPath = pathBuilder.build();
		Optional<Path<Edge>> optionalPath = Paths.getPath(graph, nodeA, nodeD, EDGE_COST_EVALUATOR, TRAVEL_COST_EVALUATOR);
		assertTrue(optionalPath.isPresent());
		Path<Edge> actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

		/*
		 * solve for path from D to F -- there is no such path so the optional
		 * should not be present.
		 */
		optionalPath = Paths.getPath(graph, nodeD, nodeF, EDGE_COST_EVALUATOR, TRAVEL_COST_EVALUATOR);
		assertFalse(optionalPath.isPresent());

		// solve for path from A to A
		pathBuilder.addEdge(edgeAC);
		pathBuilder.addEdge(edgeCE);
		pathBuilder.addEdge(edgeEF);
		pathBuilder.addEdge(edgeFG);
		pathBuilder.addEdge(edgeGA);
		expectedPath = pathBuilder.build();
		optionalPath = Paths.getPath(graph, nodeA, nodeA, EDGE_COST_EVALUATOR, TRAVEL_COST_EVALUATOR);
		assertTrue(optionalPath.isPresent());
		actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

		// solve for path from G to D
		pathBuilder.addEdge(edgeGA);
		pathBuilder.addEdge(edgeAB);
		pathBuilder.addEdge(edgeBD);
		expectedPath = pathBuilder.build();
		optionalPath = Paths.getPath(graph, nodeG, nodeD, EDGE_COST_EVALUATOR, TRAVEL_COST_EVALUATOR);
		assertTrue(optionalPath.isPresent());
		actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

	}
}
