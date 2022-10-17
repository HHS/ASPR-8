package util.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.graph.Graph;
import util.path.Paths.EdgeCostEvaluator;
import util.path.Paths.TravelCostEvaluator;
import util.vector.Vector2D;

/**
 * Test class for {@link ArrayPathSolver}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = ArrayPathSolver.class)
public class AT_ArrayPathSolver {
	/*
	 * The travel cost will be the straight-line distance
	 */
	private final static TravelCostEvaluator<Node> TRAVEL_COST_EVALUATOR = new TravelCostEvaluator<Node>() {

		@Override
		public double getMinimumCost(Node originNode, Node destination) {
			return originNode.position.distanceTo(destination.position);
		}
	};

	private final static EdgeCostEvaluator<Edge> EDGE_COST_EVALUATOR = new EdgeCostEvaluator<Edge>() {

		@Override
		public double getEdgeCost(Edge edge) {
			return edge.cost();
		}
	};

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

	/**
	 * Tests
	 * {@link ArrayPathSolver#ArrayPathSolver(Graph, EdgeCostEvaluator, TravelCostEvaluator)}
	 */
	@Test
	@UnitTestConstructor(args = { Graph.class, EdgeCostEvaluator.class, TravelCostEvaluator.class })
	public void testConstructor() {
		Graph.Builder<String, Integer> builder = Graph.builder();
		ArrayPathSolver<String, Integer> arrayPathSolver = new ArrayPathSolver<>(builder.build(), (e) -> 0.0, (n1, n2) -> 0);
		assertNotNull(arrayPathSolver);
	}

	/**
	 * Tests {@link ArrayPathSolver#getPath(Object, Object)}
	 */
	@Test
	@UnitTestMethod(name = "getPath", args = { Object.class, Object.class })
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

		PathSolver<Node, Edge> pathSolver = new ArrayPathSolver<>(graph, EDGE_COST_EVALUATOR, TRAVEL_COST_EVALUATOR);

		Path.Builder<Edge> pathBuilder = Path.builder();

		// solve for path from A to D
		pathBuilder.addEdge(edgeAB);
		pathBuilder.addEdge(edgeBD);
		Path<Edge> expectedPath = pathBuilder.build();
		Optional<Path<Edge>> optionalPath = pathSolver.getPath(nodeA, nodeD);
		assertTrue(optionalPath.isPresent());
		Path<Edge> actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

		/*
		 * solve for path from D to F -- there is no such path so the optional
		 * should not be present.
		 */
		optionalPath = pathSolver.getPath(nodeD, nodeF);
		assertFalse(optionalPath.isPresent());

		// solve for path from A to A
		pathBuilder.addEdge(edgeAC);
		pathBuilder.addEdge(edgeCE);
		pathBuilder.addEdge(edgeEF);
		pathBuilder.addEdge(edgeFG);
		pathBuilder.addEdge(edgeGA);
		expectedPath = pathBuilder.build();
		optionalPath = pathSolver.getPath(nodeA, nodeA);
		assertTrue(optionalPath.isPresent());
		actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

		// solve for path from G to D
		pathBuilder.addEdge(edgeGA);
		pathBuilder.addEdge(edgeAB);
		pathBuilder.addEdge(edgeBD);
		expectedPath = pathBuilder.build();
		optionalPath = pathSolver.getPath(nodeG, nodeD);
		assertTrue(optionalPath.isPresent());
		actualPath = optionalPath.get();
		assertEquals(expectedPath, actualPath);

	}

}
