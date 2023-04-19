package util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestMethod;

public class AT_Graph {

	@Test
	@UnitTestMethod(target = Graph.Builder.class, name = "build", args = {}, tags = { UnitTag.LOCAL_PROXY })
	public void testBuild() {
		// covered by testBuilder() method
	}

	@Test
	@UnitTestMethod(target = Graph.Builder.class, name = "addNode", args = { Object.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddNode() {
		// covered by testBuilder() method
	}

	@Test
	@UnitTestMethod(target = Graph.Builder.class, name = "addEdge", args = { Object.class, Object.class, Object.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddEdge() {
		// covered by testBuilder() method
	}

	@Test
	@UnitTestMethod(target = Graph.Builder.class, name = "addAll", args = { MutableGraph.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddAll_MutableGraph() {
		// covered by testBuilder() method
	}

	@Test
	@UnitTestMethod(target = Graph.Builder.class, name = "addAll", args = { Graph.class }, tags = { UnitTag.LOCAL_PROXY })
	public void testAddAll_Graph() {
		// covered by testBuilder() method
	}

	private void testConstructionWithGraph() {
		Graph.Builder<String, Integer> builder1 = Graph.builder();

		// build a few nodes and edges
		builder1.addEdge(45, "A", "B");
		builder1.addEdge(38, "B", "C");

		// now add another graph
		Graph.Builder<String, Integer> builder2 = Graph.builder();
		builder2.addNode("J");
		builder2.addNode("K");
		builder2.addNode("L");
		builder2.addEdge(1, "K", "J");
		builder2.addEdge(2, "J", "K");
		Graph<String, Integer> graph2 = builder2.build();

		// add the second graph to the first
		builder1.addAll(graph2);
		Graph<String, Integer> graph1 = builder1.build();
		assertTrue(graph1.containsNode("A"));
		assertTrue(graph1.containsNode("B"));
		assertTrue(graph1.containsNode("C"));
		assertTrue(graph1.containsNode("J"));
		assertTrue(graph1.containsNode("K"));
		assertTrue(graph1.containsNode("L"));

		assertEquals(6, graph1.nodeCount());

		assertTrue(graph1.containsEdge(1));
		assertTrue(graph1.containsEdge(2));
		assertTrue(graph1.containsEdge(38));
		assertTrue(graph1.containsEdge(45));

		assertEquals(4, graph1.edgeCount());

		assertEquals("K", graph1.getOriginNode(1));
		assertEquals("J", graph1.getDestinationNode(1));

		assertEquals("J", graph1.getOriginNode(2));
		assertEquals("K", graph1.getDestinationNode(2));

		assertEquals("A", graph1.getOriginNode(45));
		assertEquals("B", graph1.getDestinationNode(45));

		assertEquals("B", graph1.getOriginNode(38));
		assertEquals("C", graph1.getDestinationNode(38));

	}

	private void testConstructionWithMutableGraph() {
		Graph.Builder<String, Integer> builder1 = Graph.builder();

		// build a few nodes and edges
		builder1.addEdge(45, "A", "B");
		builder1.addEdge(38, "B", "C");

		// now add another graph
		MutableGraph<String, Integer> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode("J");
		mutableGraph.addNode("K");
		mutableGraph.addNode("L");
		mutableGraph.addEdge(1, "K", "J");
		mutableGraph.addEdge(2, "J", "K");

		// add the second graph to the first
		builder1.addAll(mutableGraph);
		Graph<String, Integer> graph1 = builder1.build();

		assertTrue(graph1.containsNode("A"));
		assertTrue(graph1.containsNode("B"));
		assertTrue(graph1.containsNode("C"));
		assertTrue(graph1.containsNode("J"));
		assertTrue(graph1.containsNode("K"));
		assertTrue(graph1.containsNode("L"));

		assertEquals(6, graph1.nodeCount());

		assertTrue(graph1.containsEdge(1));
		assertTrue(graph1.containsEdge(2));
		assertTrue(graph1.containsEdge(38));
		assertTrue(graph1.containsEdge(45));

		assertEquals(4, graph1.edgeCount());

		assertEquals("K", graph1.getOriginNode(1));
		assertEquals("J", graph1.getDestinationNode(1));

		assertEquals("J", graph1.getOriginNode(2));
		assertEquals("K", graph1.getDestinationNode(2));

		assertEquals("A", graph1.getOriginNode(45));
		assertEquals("B", graph1.getDestinationNode(45));

		assertEquals("B", graph1.getOriginNode(38));
		assertEquals("C", graph1.getDestinationNode(38));

	}

	/**
	 * Tests {@link Graph#builder()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "builder", args = {})
	public void testBuilder() {
		/*
		 * We will only test the builder methods for adding an entire Graph or
		 * MutableGraph. The other builder methods are tested via the remaining
		 * tests.
		 */
		testConstructionWithGraph();
		testConstructionWithMutableGraph();
	}

	/**
	 * Tests {@link Graph#containsEdge(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "containsEdge", args = { Object.class })
	public void testContainsEdge() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addNode("C");
		builder.addEdge("B->C", "B", "C");

		Graph<String, String> graph = builder.build();

		assertTrue(graph.containsEdge("A->B"));

		assertTrue(graph.containsEdge("B->C"));

		assertFalse(graph.containsEdge("A->C"));
	}

	/**
	 * Tests {@link Graph#containsNode(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "containsNode", args = { Object.class })
	public void testContainsNode() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addNode("A");
		builder.addNode("B");
		builder.addNode("C");

		Graph<String, String> graph = builder.build();

		assertTrue(graph.containsNode("A"));

		assertTrue(graph.containsNode("B"));

		assertTrue(graph.containsNode("C"));

		assertFalse(graph.containsNode("D"));
	}

	/**
	 * Tests {@link Graph#edgeCount(Object, Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "edgeCount", args = { Object.class, Object.class })
	public void testEdgeCount_Objects() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("E1", "B", "C");
		builder.addEdge("E2", "C", "B");
		builder.addEdge("E3", "B", "C");
		builder.addEdge("E4", "B", "A");
		builder.addEdge("E5", "B", "D");
		builder.addEdge("E6", "A", "B");
		builder.addEdge("E7", "A", "A");

		Graph<String, String> graph = builder.build();
		assertEquals(1, graph.edgeCount("A", "A"));
		assertEquals(1, graph.edgeCount("A", "B"));
		assertEquals(0, graph.edgeCount("A", "C"));
		assertEquals(0, graph.edgeCount("A", "D"));
		assertEquals(1, graph.edgeCount("B", "A"));
		assertEquals(0, graph.edgeCount("B", "B"));
		assertEquals(2, graph.edgeCount("B", "C"));
		assertEquals(1, graph.edgeCount("B", "D"));
		assertEquals(0, graph.edgeCount("C", "A"));
		assertEquals(1, graph.edgeCount("C", "B"));
		assertEquals(0, graph.edgeCount("C", "C"));
		assertEquals(0, graph.edgeCount("C", "D"));
		assertEquals(0, graph.edgeCount("D", "A"));
		assertEquals(0, graph.edgeCount("D", "B"));
		assertEquals(0, graph.edgeCount("D", "C"));
		assertEquals(0, graph.edgeCount("D", "D"));

	}

	/**
	 * Tests {@link Graph#edgeCount()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "edgeCount", args = {})
	public void testEdgeCount_NoArgs() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");

		// reversed edges should count as two edges
		builder.addEdge("B->C", "B", "C");
		builder.addEdge("C->B", "C", "B");

		// this edge connects B to C, but is a new edge
		builder.addEdge("X", "B", "C");

		// we are replacing the edge and so this should only count as one edge
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->A", "B", "X");

		Graph<String, String> graph = builder.build();
		assertEquals(5, graph.edgeCount());

	}

	/**
	 * Tests {@link Graph#formsEdgeRelationship(Object, Object, Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "formsEdgeRelationship", args = { Object.class, Object.class, Object.class })
	public void testFormsEdgeRelationship() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");

		// reversed edges should count as two edges
		builder.addEdge("B->C", "B", "C");
		builder.addEdge("C->B", "C", "B");

		// this edge connects B to C, but is a new edge
		builder.addEdge("X", "B", "C");

		// we are replacing the edge and so this should only count as one edge
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->A", "B", "X");

		Graph<String, String> graph = builder.build();

		assertTrue(graph.formsEdgeRelationship("A->B", "A", "B"));
		assertTrue(graph.formsEdgeRelationship("B->C", "B", "C"));
		assertTrue(graph.formsEdgeRelationship("C->B", "C", "B"));
		assertTrue(graph.formsEdgeRelationship("X", "B", "C"));
		assertTrue(graph.formsEdgeRelationship("B->A", "B", "X"));

		assertFalse(graph.formsEdgeRelationship("B->A", "B", "A"));

	}

	/**
	 * Tests {@link Graph#getDestinationNode(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getDestinationNode", args = { Object.class })
	public void testGetDestinationNode() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");

		// reversed edges should count as two edges
		builder.addEdge("B->C", "B", "C");
		builder.addEdge("C->B", "C", "B");

		// this edge connects B to C, but is a new edge
		builder.addEdge("X", "B", "C");

		// we are replacing the edge and so this should only count as one edge
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->A", "B", "X");

		Graph<String, String> graph = builder.build();

		assertEquals("B", graph.getDestinationNode("A->B"));
		assertEquals("C", graph.getDestinationNode("B->C"));
		assertEquals("B", graph.getDestinationNode("C->B"));
		assertEquals("C", graph.getDestinationNode("X"));
		assertEquals("X", graph.getDestinationNode("B->A"));

		assertNotEquals("A", graph.getDestinationNode("B->A"));
	}

	/**
	 * Tests {@link Graph#getEdges()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getEdges", args = {})
	public void testGetEdges_NoArgs() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("B->C", "B", "C");
		builder.addEdge("C->B", "C", "B");
		builder.addEdge("B->A", "B", "A");

		Set<String> expected = new LinkedHashSet<>();
		expected.add("A->B");
		expected.add("B->C");
		expected.add("C->B");
		expected.add("B->A");

		Graph<String, String> graph = builder.build();

		Set<String> actual = graph.getEdges().stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);
	}

	/**
	 * Tests{@link Graph#getEdges(Object, Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getEdges", args = { Object.class, Object.class })
	public void testGetEdges_Objects() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("E1", "B", "C");
		builder.addEdge("E2", "C", "B");
		builder.addEdge("E3", "B", "C");
		builder.addEdge("E4", "B", "A");
		builder.addEdge("E5", "B", "D");
		builder.addEdge("E6", "A", "B");
		builder.addEdge("E7", "A", "A");
		Graph<String, String> graph = builder.build();

		Set<String> expected = new LinkedHashSet<>();
		;
		Set<String> actual;

		expected.clear();
		expected.add("E7");
		actual = graph.getEdges("A", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E6");
		actual = graph.getEdges("A", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("A", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("A", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E4");
		actual = graph.getEdges("B", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("B", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E1");
		expected.add("E3");
		actual = graph.getEdges("B", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E5");
		actual = graph.getEdges("B", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("C", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E2");
		actual = graph.getEdges("C", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("C", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("C", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("D", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("D", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("D", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = graph.getEdges("D", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link Graph#getInboundEdgeCount(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getInboundEdgeCount", args = { Object.class })
	public void testGetInboundEdgeCount() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("C->B", "C", "B");

		builder.addEdge("B->C", "B", "C");

		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->A", "A", "A");

		builder.addNode("D");

		Graph<String, String> graph = builder.build();

		assertEquals(2, graph.getInboundEdgeCount("A"));
		assertEquals(2, graph.getInboundEdgeCount("B"));
		assertEquals(1, graph.getInboundEdgeCount("C"));
		assertEquals(0, graph.getInboundEdgeCount("D"));
	}

	/**
	 * Tests {@link Graph#getInboundEdges(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getInboundEdges", args = { Object.class })
	public void testGetInboundEdges() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("C->B", "C", "B");

		builder.addEdge("B->C", "B", "C");

		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->A", "A", "A");

		builder.addNode("D");

		Graph<String, String> graph = builder.build();

		Set<String> expected = new LinkedHashSet<>();
		expected.add("B->A");
		expected.add("A->A");
		assertEquals(expected, graph.getInboundEdges("A").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		expected.add("A->B");
		expected.add("C->B");
		assertEquals(expected, graph.getInboundEdges("B").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		expected.add("B->C");
		assertEquals(expected, graph.getInboundEdges("C").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		assertEquals(expected, graph.getInboundEdges("D").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

	}

	/**
	 * Tests {@link Graph#getNodes()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getNodes", args = {})
	public void testGetNodes() {
		Set<String> expected = new LinkedHashSet<>();
		expected.add("A");
		expected.add("B");
		expected.add("C");
		expected.add("D");
		expected.add("E");

		Graph.Builder<String, String> builder = Graph.builder();
		for (String node : expected) {
			builder.addNode(node);
		}

		Graph<String, String> graph = builder.build();

		Set<String> actual = graph.getNodes().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link Graph#getOriginNode(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getOriginNode", args = { Object.class })
	public void testGetOriginNode() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");

		// reversed edges should count as two edges
		builder.addEdge("B->C", "B", "C");
		builder.addEdge("C->B", "C", "B");

		// this edge connects B to C, but is a new edge
		builder.addEdge("X", "B", "C");

		// we are replacing the edge and so this should only count as one edge
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->A", "B", "X");

		Graph<String, String> graph = builder.build();

		assertEquals("A", graph.getOriginNode("A->B"));
		assertEquals("B", graph.getOriginNode("B->C"));
		assertEquals("C", graph.getOriginNode("C->B"));
		assertEquals("B", graph.getOriginNode("X"));
		assertEquals("B", graph.getOriginNode("B->A"));

	}

	/**
	 * Tests {@link Graph#getOutboundEdgeCount(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getOutboundEdgeCount", args = { Object.class })
	public void testGetOutboundEdgeCount() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("C->B", "C", "B");

		builder.addEdge("B->C", "B", "C");

		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->A", "A", "A");

		builder.addEdge("A->D", "A", "D");

		Graph<String, String> graph = builder.build();

		assertEquals(3, graph.getOutboundEdgeCount("A"));
		assertEquals(2, graph.getOutboundEdgeCount("B"));
		assertEquals(1, graph.getOutboundEdgeCount("C"));
		assertEquals(0, graph.getOutboundEdgeCount("D"));
	}

	/**
	 * Tests {@link Graph#getOutboundEdges(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "getOutboundEdges", args = { Object.class })
	public void testGetOutboundEdges() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("C->B", "C", "B");

		builder.addEdge("B->C", "B", "C");

		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->A", "A", "A");

		builder.addNode("D");

		Graph<String, String> graph = builder.build();

		Set<String> expected = new LinkedHashSet<>();
		expected.add("A->A");
		expected.add("A->B");
		assertEquals(expected, graph.getOutboundEdges("A").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		expected.add("B->C");
		expected.add("B->A");
		assertEquals(expected, graph.getOutboundEdges("B").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		expected.add("C->B");
		assertEquals(expected, graph.getOutboundEdges("C").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

		expected.clear();
		assertEquals(expected, graph.getOutboundEdges("D").stream().collect(Collectors.toCollection(LinkedHashSet::new)));

	}

	/**
	 * Tests {@link Graph#isEmpty()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "isEmpty", args = {})
	public void testIsEmpty() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addNode("A");
		Graph<String, String> graph = builder.build();

		assertFalse(graph.isEmpty());

		builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		graph = builder.build();
		assertFalse(graph.isEmpty());

		builder = Graph.builder();
		graph = builder.build();
		assertTrue(graph.isEmpty());
	}

	/**
	 * Tests {@link Graph#nodeCount()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "nodeCount", args = {})
	public void testNodeCount() {
		Graph.Builder<String, String> builder = Graph.builder();
		Graph<String, String> graph = builder.build();
		assertEquals(0, graph.nodeCount());

		builder.addNode("A");
		graph = builder.build();
		assertEquals(1, graph.nodeCount());

		builder.addNode("A");
		builder.addNode("B");
		graph = builder.build();
		assertEquals(2, graph.nodeCount());

		builder.addNode("A");
		builder.addNode("A");
		builder.addNode("B");
		graph = builder.build();
		assertEquals(2, graph.nodeCount());
	}

	/**
	 * Tests {@link Graph#toMutableGraph()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "toMutableGraph", args = {})
	public void testToMutableGraph() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("A->C", "A", "C");
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->D", "B", "D");
		builder.addNode("E");

		Graph<String, String> graph = builder.build();

		MutableGraph<String, String> mutableGraph = graph.toMutableGraph();

		assertEquals(graph, mutableGraph);

	}

	/**
	 * Tests {@link Graph#hashCode()}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "hashCode", args = {})
	public void testHashCode() {
		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addEdge("A->C", "A", "C");
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->D", "B", "D");
		builder.addNode("E");

		Graph<String, String> graph1 = builder.build();

		int expected = "A".hashCode();
		expected += "B".hashCode();
		expected += "C".hashCode();
		expected += "D".hashCode();
		expected += "E".hashCode();
		expected += "A->B".hashCode();
		expected += "A->C".hashCode();
		expected += "B->A".hashCode();
		expected += "B->D".hashCode();

		int actual = graph1.hashCode();

		assertEquals(expected, actual);

		builder.addNode("E");
		builder.addEdge("B->D", "B", "D");
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->C", "A", "C");
		builder.addEdge("A->B", "A", "B");
		Graph<String, String> graph2 = builder.build();
		assertEquals(graph1, graph2);
		assertEquals(graph1.hashCode(), graph2.hashCode());

	}

	/**
	 * Tests {@link Graph#equals(Object)}
	 */
	@Test
	@UnitTestMethod(target = Graph.class, name = "equals", args = { Object.class })
	public void testEquals() {
		Graph.Builder<String, String> builder = Graph.builder();

		builder.addEdge("A->B", "A", "B");
		builder.addEdge("A->C", "A", "C");
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("B->D", "B", "D");
		builder.addNode("E");
		Graph<String, String> graph1 = builder.build();

		builder.addNode("E");
		builder.addEdge("B->D", "B", "D");
		builder.addEdge("B->A", "B", "A");
		builder.addEdge("A->C", "A", "C");
		builder.addEdge("A->B", "A", "B");
		Graph<String, String> graph2 = builder.build();

		assertEquals(graph1, graph2);
	}

}
