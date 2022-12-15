package util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.graph.Graphs.GraphConnectedness;
import util.graph.Graphs.GraphCyclisity;

/**
 * Test class for {@link Graphs}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Graphs.class)
public class AT_Graphs {

	/**
	 * Tests {@link Graphs#cutGraph(Graph)}
	 */
	@Test
	@UnitTestMethod(name = "cutGraph", args = { Graph.class })
	public void testCutGraph() {
		
		assertEquals(0,Graphs.cutGraph(new MutableGraph<>().toGraph()).size());
		
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
	@UnitTestMethod(name = "getGraphConnectedness", args = { Graph.class })
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
	@UnitTestMethod(name = "getGraphCyclisity", args = { Graph.class })
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
	@UnitTestMethod(name = "getReverseGraph", args = { Graph.class })
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
	@UnitTestMethod(name = "getSourceSinkReducedGraph", args = { Graph.class })
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
	@UnitTestMethod(name = "getEdgeReducedGraph", args = {Graph.class})
	public void testGetEdgeReducedGraph() {

		// case: empty graph
		MutableGraph<String, String> e = new MutableGraph<>();
		Graph<String, String> reducedEmptyGraph = Graphs.getEdgeReducedGraph(e.toGraph());
		assertEquals(0, reducedEmptyGraph.edgeCount());
		assertEquals(e.toGraph(), reducedEmptyGraph);

		// case: self edge
		MutableGraph<String, String> s = new MutableGraph<>();
		s.addEdge("A->A", "A", "A");
		Graph<String, String> reducedSelfEdgeGraph = Graphs.getEdgeReducedGraph(s.toGraph());
		assertEquals(1, reducedSelfEdgeGraph.edgeCount());
		assertEquals(s.toGraph(),reducedSelfEdgeGraph);

		// case: single edge
		MutableGraph<String, String> i = new MutableGraph<>();
		i.addEdge("A->B", "A", "B");
		MutableGraph<String, String> expectedReducedSingleEdgeGraph = new MutableGraph<>();
		for (String node : i.getNodes()) {
			expectedReducedSingleEdgeGraph.addNode(node);
		}
		Graph<String, String> reducedSingleEdgeGraph = Graphs.getEdgeReducedGraph(i.toGraph());
		assertEquals(0, reducedSingleEdgeGraph.edgeCount());
		assertEquals(expectedReducedSingleEdgeGraph.toGraph(), reducedSingleEdgeGraph);

		// case: two cyclic edges
		MutableGraph<String, String> c = new MutableGraph<>();
		c.addEdge("A->B", "A", "B");
		c.addEdge("B->A", "B", "A");
		Graph<String, String> reducedSmallCyclicGraph = Graphs.getEdgeReducedGraph(c.toGraph());
		assertEquals(2, reducedSmallCyclicGraph.edgeCount());
		assertEquals(c.toGraph(), reducedSmallCyclicGraph);

		// case: single sink
		MutableGraph<String, String> k = new MutableGraph<>();
		k.addEdge("A->F", "A", "F");
		k.addEdge("B->F", "B", "F");
		k.addEdge("C->F", "C","F");
		k.addEdge("D->F", "D", "F");
		k.addEdge("E->F", "E", "F");
		MutableGraph<String, String> expectedReducedSingleSinkGraph = new MutableGraph<>();
		for (String node : k.getNodes()) {
			expectedReducedSingleSinkGraph.addNode(node);
		}
		Graph<String, String> reducedSingleSinkGraph = Graphs.getEdgeReducedGraph(k.toGraph());
		assertEquals(0, reducedSingleSinkGraph.edgeCount());
		assertEquals(expectedReducedSingleSinkGraph.toGraph(), reducedSingleSinkGraph);

		// case: single source
		MutableGraph<String, String> r = new MutableGraph<>();
		r.addEdge("F->A", "F", "A");
		r.addEdge("F->B", "F", "B");
		r.addEdge("F->C", "F","C");
		r.addEdge("F->D", "F", "D");
		r.addEdge("F->E", "F", "E");
		MutableGraph<String, String> expectedReducedSingleSourceGraph = new MutableGraph<>();
		for (String node: r.getNodes()) {
			expectedReducedSingleSourceGraph.addNode(node);
		}
		Graph<String, String> reducedSingleSourceGraph = Graphs.getEdgeReducedGraph(r.toGraph());
		assertEquals(0, reducedSingleSourceGraph.edgeCount());
		assertEquals(expectedReducedSingleSourceGraph.toGraph(), reducedSingleSourceGraph);


		// case: basic cyclic graph
		MutableGraph<String, String> b = new MutableGraph<>();
		b.addEdge("A->B", "A", "B");
		b.addEdge("B->C", "B", "C");
		b.addEdge("C->A", "C", "A");
		Graph<String, String> reducedBasicCyclicGraph = Graphs.getEdgeReducedGraph(b.toGraph());
		assertEquals(3, reducedBasicCyclicGraph.edgeCount());
		assertEquals(b.toGraph(), reducedBasicCyclicGraph);

		// case: basic acyclic graph
		MutableGraph<String, String> a = new MutableGraph<>();
		a.addEdge("A->F", "A", "F");
		a.addEdge("A->B", "A", "B");
		a.addEdge("B->C", "B", "C");
		a.addEdge("C->A", "C", "A");
		MutableGraph<String, String> expectedReducedBasicAcyclicGraph = new MutableGraph<>();
		expectedReducedBasicAcyclicGraph.addAll(a);
		expectedReducedBasicAcyclicGraph.removeEdge("A->F");
		Graph<String, String> reducedBasicAcyclicGraph = Graphs.getEdgeReducedGraph(a.toGraph());
		assertEquals(3, reducedBasicAcyclicGraph.edgeCount());
		assertEquals(expectedReducedBasicAcyclicGraph.toGraph(), reducedBasicAcyclicGraph);

		// case: one acyclic edge connecting two cycles
		MutableGraph<String, String> g = new MutableGraph<>();
		g.addEdge("A->B", "A", "B");
		g.addEdge("B->C", "B", "C");
		g.addEdge("C->A", "C", "A");
		g.addEdge("C->E", "C", "E");
		g.addEdge("E->F", "E", "F");
		g.addEdge("F->G", "F", "G");
		g.addEdge("G->E", "G", "E");
		MutableGraph<String, String> expectedReducedBridgedCyclesGraph = new MutableGraph<>();
		expectedReducedBridgedCyclesGraph.addAll(g);
		expectedReducedBridgedCyclesGraph.removeEdge("C->E");
		Graph<String, String> reducedBridgedCyclesGraph = Graphs.getEdgeReducedGraph(g.toGraph());
		assertEquals(6, reducedBridgedCyclesGraph.edgeCount());
		assertEquals(expectedReducedBridgedCyclesGraph.toGraph(), reducedBridgedCyclesGraph);

		// case: cyclic graph with many edges
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("B->A", "B", "A");
		m.addEdge("A->C", "A", "C");
		m.addEdge("C->B", "C", "B");
		m.addEdge("C->D", "C", "D");
		m.addEdge("D->E", "D", "E");
		m.addEdge("E->F", "E", "F");
		m.addEdge("F->D", "F", "D");
		m.addEdge("F->C", "F", "C");
		m.addEdge("F->G", "F", "G");
		m.addEdge("G->H", "G", "H");
		m.addEdge("H->I", "H", "I");
		m.addEdge("I->G", "I", "G");
		m.addEdge("I->B", "I", "B");
		m.addEdge("C->I", "C", "I");
		Graph<String, String> reducedLargeCyclicGraph = Graphs.getEdgeReducedGraph(m.toGraph());
		assertEquals(14, reducedLargeCyclicGraph.edgeCount());
		assertEquals(m.toGraph(), reducedLargeCyclicGraph);
	}

}
