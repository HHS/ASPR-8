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
		MutableGraph<String, String> emptyGraph = new MutableGraph<>();
		Graph<String, String> reducedEmptyGraph = Graphs.getEdgeReducedGraph(emptyGraph.toGraph());
		assertTrue(reducedEmptyGraph.isEmpty());
		assertEquals(0, reducedEmptyGraph.edgeCount());
		assertEquals(emptyGraph.toGraph(), reducedEmptyGraph);

		// case: self edge
		MutableGraph<String, String> selfEdgeGraph = new MutableGraph<>();
		selfEdgeGraph.addEdge("A->A", "A", "A");
		Graph<String, String> reducedSelfEdgeGraph = Graphs.getEdgeReducedGraph(selfEdgeGraph.toGraph());
		assertEquals(1, reducedSelfEdgeGraph.edgeCount());
		assertTrue(reducedSelfEdgeGraph.containsEdge("A->A"));
		assertEquals(selfEdgeGraph.toGraph(),reducedSelfEdgeGraph);

		// case: single edge
		MutableGraph<String, String> singleEdgeGraph = new MutableGraph<>();
		singleEdgeGraph.addEdge("A->B", "A", "B");
		MutableGraph<String, String> expectedReducedSingleEdgeGraph = new MutableGraph<>();
		for (String node : singleEdgeGraph.getNodes()) {
			expectedReducedSingleEdgeGraph.addNode(node);
		}
		Graph<String, String> reducedSingleEdgeGraph = Graphs.getEdgeReducedGraph(singleEdgeGraph.toGraph());
		assertEquals(0, reducedSingleEdgeGraph.edgeCount());
		assertEquals(expectedReducedSingleEdgeGraph.toGraph(), reducedSingleEdgeGraph);

		// case: two cyclic edges
		MutableGraph<String, String> smallCyclicGraph = new MutableGraph<>();
		smallCyclicGraph.addEdge("A->B", "A", "B");
		smallCyclicGraph.addEdge("B->A", "B", "A");
		Graph<String, String> reducedSmallCyclicGraph = Graphs.getEdgeReducedGraph(smallCyclicGraph.toGraph());
		assertEquals(2, reducedSmallCyclicGraph.edgeCount());
		for (String edge : smallCyclicGraph.getEdges()) {
			assertTrue(reducedSmallCyclicGraph.containsEdge(edge));
		}
		assertEquals(smallCyclicGraph.toGraph(), reducedSmallCyclicGraph);

		// case: single sink
		MutableGraph<String, String> singleSinkGraph = new MutableGraph<>();
		singleSinkGraph.addEdge("A->F", "A", "F");
		singleSinkGraph.addEdge("B->F", "B", "F");
		singleSinkGraph.addEdge("C->F", "C","F");
		singleSinkGraph.addEdge("D->F", "D", "F");
		singleSinkGraph.addEdge("E->F", "E", "F");
		MutableGraph<String, String> expectedReducedSingleSinkGraph = new MutableGraph<>();
		for (String node : singleSinkGraph.getNodes()) {
			expectedReducedSingleSinkGraph.addNode(node);
		}
		Graph<String, String> reducedSingleSinkGraph = Graphs.getEdgeReducedGraph(singleSinkGraph.toGraph());
		assertEquals(0, reducedSingleSinkGraph.edgeCount());
		assertEquals(expectedReducedSingleSinkGraph.toGraph(), reducedSingleSinkGraph);

		// case: single source
		MutableGraph<String, String> singleSourceGraph = new MutableGraph<>();
		singleSourceGraph.addEdge("F->A", "F", "A");
		singleSourceGraph.addEdge("F->B", "F", "B");
		singleSourceGraph.addEdge("F->C", "F","C");
		singleSourceGraph.addEdge("F->D", "F", "D");
		singleSourceGraph.addEdge("F->E", "F", "E");
		MutableGraph<String, String> expectedReducedSingleSourceGraph = new MutableGraph<>();
		for (String node: singleSourceGraph.getNodes()) {
			expectedReducedSingleSourceGraph.addNode(node);
		}
		Graph<String, String> reducedSingleSourceGraph = Graphs.getEdgeReducedGraph(singleSourceGraph.toGraph());
		assertEquals(0, reducedSingleSourceGraph.edgeCount());
		assertEquals(expectedReducedSingleSourceGraph.toGraph(), reducedSingleSourceGraph);


		// case: basic cyclic graph
		MutableGraph<String, String> basicCyclicGraph = new MutableGraph<>();
		basicCyclicGraph.addEdge("A->B", "A", "B");
		basicCyclicGraph.addEdge("B->C", "B", "C");
		basicCyclicGraph.addEdge("C->A", "C", "A");
		Graph<String, String> reducedBasicCyclicGraph = Graphs.getEdgeReducedGraph(basicCyclicGraph.toGraph());
		assertEquals(3, reducedBasicCyclicGraph.edgeCount());
		for (String edge : basicCyclicGraph.getEdges()) {
			assertTrue(reducedBasicCyclicGraph.containsEdge(edge));
		}
		assertEquals(basicCyclicGraph.toGraph(), reducedBasicCyclicGraph);

		// case: basic acyclic graph
		MutableGraph<String, String> basicAcyclicGraph = new MutableGraph<>();
		basicAcyclicGraph.addEdge("A->F", "A", "F");
		basicAcyclicGraph.addEdge("A->B", "A", "B");
		basicAcyclicGraph.addEdge("B->C", "B", "C");
		basicAcyclicGraph.addEdge("C->A", "C", "A");
		MutableGraph<String, String> expectedReducedBasicAcyclicGraph = new MutableGraph<>();
		expectedReducedBasicAcyclicGraph.addEdge("A->B", "A", "B");
		expectedReducedBasicAcyclicGraph.addEdge("B->C", "B", "C");
		expectedReducedBasicAcyclicGraph.addEdge("C->A", "C", "A");
		expectedReducedBasicAcyclicGraph.addNode("F");
		Graph<String, String> reducedBasicAcyclicGraph = Graphs.getEdgeReducedGraph(basicAcyclicGraph.toGraph());
		assertEquals(3, reducedBasicAcyclicGraph.edgeCount());
		assertTrue(reducedBasicAcyclicGraph.containsEdge("A->B"));
		assertTrue(reducedBasicAcyclicGraph.containsEdge("B->C"));
		assertTrue(reducedBasicAcyclicGraph.containsEdge("C->A"));
		assertEquals(expectedReducedBasicAcyclicGraph.toGraph(), reducedBasicAcyclicGraph);

		// case: one acyclic edge connecting two cycles
		MutableGraph<String, String> bridgedCyclesGraph = new MutableGraph<>();
		bridgedCyclesGraph.addEdge("A->B", "A", "B");
		bridgedCyclesGraph.addEdge("B->C", "B", "C");
		bridgedCyclesGraph.addEdge("C->A", "C", "A");
		bridgedCyclesGraph.addEdge("C->E", "C", "E");
		bridgedCyclesGraph.addEdge("E->F", "E", "F");
		bridgedCyclesGraph.addEdge("F->G", "F", "G");
		bridgedCyclesGraph.addEdge("G->E", "G", "E");
		MutableGraph<String, String> expectedReducedBridgedCyclesGraph = new MutableGraph<>();
		expectedReducedBridgedCyclesGraph.addEdge("A->B", "A", "B");
		expectedReducedBridgedCyclesGraph.addEdge("B->C", "B", "C");
		expectedReducedBridgedCyclesGraph.addEdge("C->A", "C", "A");
		expectedReducedBridgedCyclesGraph.addEdge("E->F", "E", "F");
		expectedReducedBridgedCyclesGraph.addEdge("F->G", "F", "G");
		expectedReducedBridgedCyclesGraph.addEdge("G->E", "G", "E");
		Graph<String, String> reducedBridgedCyclesGraph = Graphs.getEdgeReducedGraph(bridgedCyclesGraph.toGraph());
		assertEquals(6, reducedBridgedCyclesGraph.edgeCount());
		assertTrue(reducedBridgedCyclesGraph.containsEdge("A->B"));
		assertTrue(reducedBridgedCyclesGraph.containsEdge("B->C"));
		assertTrue(reducedBridgedCyclesGraph.containsEdge("C->A"));
		assertTrue(reducedBridgedCyclesGraph.containsEdge("E->F"));
		assertTrue(reducedBridgedCyclesGraph.containsEdge("F->G"));
		assertTrue(reducedBridgedCyclesGraph.containsEdge("G->E"));
		assertEquals(expectedReducedBridgedCyclesGraph.toGraph(), reducedBridgedCyclesGraph);

		// case: cyclic graph with many edges
		MutableGraph<String, String> largeCyclicGraph = new MutableGraph<>();
		largeCyclicGraph.addEdge("B->A", "B", "A");
		largeCyclicGraph.addEdge("A->C", "A", "C");
		largeCyclicGraph.addEdge("C->B", "C", "B");
		largeCyclicGraph.addEdge("C->D", "C", "D");
		largeCyclicGraph.addEdge("D->E", "D", "E");
		largeCyclicGraph.addEdge("E->F", "E", "F");
		largeCyclicGraph.addEdge("F->D", "F", "D");
		largeCyclicGraph.addEdge("F->C", "F", "C");
		largeCyclicGraph.addEdge("F->G", "F", "G");
		largeCyclicGraph.addEdge("G->H", "G", "H");
		largeCyclicGraph.addEdge("H->I", "H", "I");
		largeCyclicGraph.addEdge("I->G", "I", "G");
		largeCyclicGraph.addEdge("I->B", "I", "B");
		largeCyclicGraph.addEdge("C->I", "C", "I");
		Graph<String, String> reducedLargeCyclicGraph = Graphs.getEdgeReducedGraph(largeCyclicGraph.toGraph());
		assertEquals(14, reducedLargeCyclicGraph.edgeCount());
		for (String edge : largeCyclicGraph.getEdges()) {
			assertTrue(reducedLargeCyclicGraph.containsEdge(edge));
		}
	}

}
