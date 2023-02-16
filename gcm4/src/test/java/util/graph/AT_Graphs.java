package util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.graph.Graphs.GraphConnectedness;
import util.graph.Graphs.GraphCyclisity;

public class AT_Graphs {

	/**
	 * Tests {@link Graphs#cutGraph(Graph)}
	 */
	@Test
	@UnitTestMethod(target = Graphs.class, name = "cutGraph", args = { Graph.class })
	public void testCutGraph() {

		assertEquals(0, Graphs.cutGraph(new MutableGraph<>().toGraph()).size());

		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("B->C", "B", "C");
		m.addEdge("D->E", "D", "E");
		m.addNode("F");

		Set<Graph<String, String>> expected = new LinkedHashSet<>();

		MutableGraph<String, String> a = new MutableGraph<>();
		a.addEdge("A->B", "A", "B");
		a.addEdge("B->C", "B", "C");
		expected.add(a.toGraph());

		MutableGraph<String, String> b = new MutableGraph<>();
		b.addEdge("D->E", "D", "E");
		expected.add(b.toGraph());

		MutableGraph<String, String> c = new MutableGraph<>();
		c.addNode("F");
		expected.add(c.toGraph());

		Set<Graph<String, String>> actual = Graphs.cutGraph(m.toGraph()).stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link Graphs#getGraphConnectedness(Graph)}
	 */
	@Test
	@UnitTestMethod(target = Graphs.class, name = "getGraphConnectedness", args = { Graph.class })
	public void testGetGraphConnectedness() {
		MutableGraph<String, String> m = new MutableGraph<>();
		assertEquals(GraphConnectedness.DISCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

		m.addNode("A");
		m.addNode("B");
		m.addNode("C");
		assertEquals(GraphConnectedness.DISCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

		m.addEdge("A->B", "A", "B");
		assertEquals(GraphConnectedness.DISCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

		m.addEdge("B->C", "B", "C");
		assertEquals(GraphConnectedness.WEAKLYCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

		m.addEdge("B->A", "B", "A");
		assertEquals(GraphConnectedness.WEAKLYCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

		m.addEdge("C->B", "C", "B");
		assertEquals(GraphConnectedness.STRONGLYCONNECTED, Graphs.getGraphConnectedness(m.toGraph()));

	}

	/**
	 * Tests {@link Graphs#getGraphCyclisity(Graph)}
	 */
	@Test
	@UnitTestMethod(target = Graphs.class, name = "getGraphCyclisity", args = { Graph.class })
	public void testGetGraphCyclisity() {
		MutableGraph<String, String> m = new MutableGraph<>();
		// empty graphs are acyclic
		assertEquals(GraphCyclisity.ACYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

		m.addNode("A");
		m.addNode("B");
		m.addNode("C");
		assertEquals(GraphCyclisity.ACYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

		m.addEdge("A->B", "A", "B");
		assertEquals(GraphCyclisity.ACYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

		m.addEdge("B->A", "B", "A");
		assertEquals(GraphCyclisity.CYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

		m.addEdge("B->C", "B", "C");
		assertEquals(GraphCyclisity.CYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

		m.addEdge("C->A", "C", "A");
		assertEquals(GraphCyclisity.CYCLIC, Graphs.getGraphCyclisity(m.toGraph()));

	}

	/**
	 * Tests {@link Graphs#getReverseGraph(Graph)}
	 */
	@Test
	@UnitTestMethod(target = Graphs.class, name = "getReverseGraph", args = { Graph.class })
	public void testGetReverseGraph() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("B->C", "B", "C");
		m.addEdge("C->D", "C", "D");
		m.addNode("E");

		MutableGraph<String, String> m2 = new MutableGraph<>();
		m2.addEdge("A->B", "B", "A");
		m2.addEdge("A->C", "C", "A");
		m2.addEdge("B->C", "C", "B");
		m2.addEdge("C->D", "D", "C");
		m2.addNode("E");

		Graph<String, String> expected = m2.toGraph();

		Graph<String, String> actual = Graphs.getReverseGraph(m.toGraph());

		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link Graphs#getSourceSinkReducedGraph(Graph)}
	 */
	@Test
	@UnitTestMethod(target = Graphs.class, name = "getSourceSinkReducedGraph", args = { Graph.class })
	public void testGetSourceSinkReducedGraph() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("B->C", "B", "C");
		m.addEdge("C->D", "C", "D");
		m.addNode("E");

		Graph<String, String> reducedGraph = Graphs.getSourceSinkReducedGraph(m.toGraph());
		assertTrue(reducedGraph.isEmpty());

		m.addEdge("D->C", "D", "C");
		reducedGraph = Graphs.getSourceSinkReducedGraph(m.toGraph());
		assertEquals(2, reducedGraph.edgeCount());
		assertEquals(2, reducedGraph.nodeCount());
		assertTrue(reducedGraph.containsEdge("C->D"));
		assertTrue(reducedGraph.containsEdge("D->C"));
		assertTrue(reducedGraph.containsNode("C"));
		assertTrue(reducedGraph.containsNode("D"));

		m.addEdge("E->E", "E", "E");
		reducedGraph = Graphs.getSourceSinkReducedGraph(m.toGraph());
		reducedGraph = Graphs.getSourceSinkReducedGraph(m.toGraph());
		assertEquals(3, reducedGraph.edgeCount());
		assertEquals(3, reducedGraph.nodeCount());
		assertTrue(reducedGraph.containsEdge("C->D"));
		assertTrue(reducedGraph.containsEdge("D->C"));
		assertTrue(reducedGraph.containsEdge("E->E"));
		assertTrue(reducedGraph.containsNode("C"));
		assertTrue(reducedGraph.containsNode("D"));
		assertTrue(reducedGraph.containsNode("E"));

	}

	@Test
	@UnitTestMethod(target = Graphs.class, name = "getEdgeReducedGraph", args = { Graph.class })
	public void testGetEdgeReducedGraph() {

		MutableGraph<String, String> mutableGraph;
		Graph<String, String> graph;
		Graph<String, String> reducedGraph;
		Graph<String, String> expectedGraph;

		// case: empty graph
		mutableGraph = new MutableGraph<>();
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: self edge
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->A", "A", "A");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: single edge
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		mutableGraph = new MutableGraph<>();
		mutableGraph.addAll(graph);
		mutableGraph.removeEdge("A->B");
		expectedGraph = mutableGraph.toGraph();
		assertEquals(expectedGraph, reducedGraph);

		// case: two cyclic edges
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("B->A", "B", "A");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: single sink
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->F", "A", "F");
		mutableGraph.addEdge("B->F", "B", "F");
		mutableGraph.addEdge("C->F", "C", "F");
		mutableGraph.addEdge("D->F", "D", "F");
		mutableGraph.addEdge("E->F", "E", "F");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		for (String edge : mutableGraph.getEdges()) {
			mutableGraph.removeEdge(edge);
		}
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: single source
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("F->A", "F", "A");
		mutableGraph.addEdge("F->B", "F", "B");
		mutableGraph.addEdge("F->C", "F", "C");
		mutableGraph.addEdge("F->D", "F", "D");
		mutableGraph.addEdge("F->E", "F", "E");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		for (String edge : mutableGraph.getEdges()) {
			mutableGraph.removeEdge(edge);
		}
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: basic cyclic graph
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("C->A", "C", "A");
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		assertEquals(expectedGraph, reducedGraph);

		// case: basic acyclic graph
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->F", "A", "F");
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("C->A", "C", "A");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		mutableGraph.removeEdge("A->F");
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: one acyclic edge connecting two cycles
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("C->A", "C", "A");
		mutableGraph.addEdge("C->E", "C", "E");
		mutableGraph.addEdge("E->F", "E", "F");
		mutableGraph.addEdge("F->G", "F", "G");
		mutableGraph.addEdge("G->E", "G", "E");
		graph = mutableGraph.toGraph();
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		mutableGraph.removeEdge("C->E");
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		assertEquals(expectedGraph, reducedGraph);

		// case: cyclic graph with many edges
		mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("B->A", "B", "A");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("C->B", "C", "B");
		mutableGraph.addEdge("C->D", "C", "D");
		mutableGraph.addEdge("D->E", "D", "E");
		mutableGraph.addEdge("E->F", "E", "F");
		mutableGraph.addEdge("F->D", "F", "D");
		mutableGraph.addEdge("F->C", "F", "C");
		mutableGraph.addEdge("F->G", "F", "G");
		mutableGraph.addEdge("G->H", "G", "H");
		mutableGraph.addEdge("H->I", "H", "I");
		mutableGraph.addEdge("I->G", "I", "G");
		mutableGraph.addEdge("I->B", "I", "B");
		mutableGraph.addEdge("C->I", "C", "I");
		graph = mutableGraph.toGraph();
		expectedGraph = graph;
		reducedGraph = Graphs.getEdgeReducedGraph(graph);
		assertEquals(expectedGraph, reducedGraph);
	}

}
