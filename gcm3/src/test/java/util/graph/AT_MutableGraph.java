package util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link MutableGraph}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = MutableGraph.class)
public class AT_MutableGraph {

	/**
	 * Tests {@link MutableGraph#addAll(MutableGraph)}
	 */
	@Test
	@UnitTestMethod(name = "addAll", args = { MutableGraph.class })
	public void testAddAllForMutableGraph() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addEdge("A->C", "A", "C");
		mutableGraph1.addEdge("B->C", "B", "C");
		mutableGraph1.addEdge("C->A", "C", "A");

		MutableGraph<String, String> mutableGraph2 = new MutableGraph<>();
		mutableGraph2.addEdge("A->B", "J", "K");// this will replace the
												// existing edge
		mutableGraph2.addEdge("J->V", "J", "V");// this will be a new edge
		mutableGraph2.addEdge("B->A", "B", "A");// this will be a new edge
		mutableGraph2.addEdge("C->A", "C", "A");// this will the same as an
												// existing edge

		mutableGraph1.addAll(mutableGraph2);

		assertEquals(6, mutableGraph1.edgeCount());
		assertEquals(6, mutableGraph1.nodeCount());

		assertTrue(mutableGraph1.containsNode("A"));
		assertTrue(mutableGraph1.containsNode("B"));
		assertTrue(mutableGraph1.containsNode("C"));
		assertTrue(mutableGraph1.containsNode("J"));
		assertTrue(mutableGraph1.containsNode("V"));
		assertTrue(mutableGraph1.containsNode("K"));

		assertTrue(mutableGraph1.containsEdge("A->C"));
		assertTrue(mutableGraph1.containsEdge("B->C"));
		assertTrue(mutableGraph1.containsEdge("C->A"));
		assertTrue(mutableGraph1.containsEdge("A->B"));
		assertTrue(mutableGraph1.containsEdge("J->V"));
		assertTrue(mutableGraph1.containsEdge("B->A"));

		assertEquals("A", mutableGraph1.getOriginNode("A->C"));
		assertEquals("B", mutableGraph1.getOriginNode("B->C"));
		assertEquals("C", mutableGraph1.getOriginNode("C->A"));
		assertEquals("J", mutableGraph1.getOriginNode("A->B"));
		assertEquals("J", mutableGraph1.getOriginNode("J->V"));
		assertEquals("B", mutableGraph1.getOriginNode("B->A"));

		assertEquals("C", mutableGraph1.getDestinationNode("A->C"));
		assertEquals("C", mutableGraph1.getDestinationNode("B->C"));
		assertEquals("A", mutableGraph1.getDestinationNode("C->A"));
		assertEquals("K", mutableGraph1.getDestinationNode("A->B"));
		assertEquals("V", mutableGraph1.getDestinationNode("J->V"));
		assertEquals("A", mutableGraph1.getDestinationNode("B->A"));
	}

	/**
	 * Tests {@link MutableGraph#addAll(Graph)}
	 */
	@Test
	@UnitTestMethod(name = "addAll", args = { Graph.class })
	public void testAddAllForGraph() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addEdge("A->C", "A", "C");
		mutableGraph1.addEdge("B->C", "B", "C");
		mutableGraph1.addEdge("C->A", "C", "A");

		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "J", "K");// this will replace the existing edge
		builder.addEdge("J->V", "J", "V");// this will be a new edge
		builder.addEdge("B->A", "B", "A");// this will be a new edge
		builder.addEdge("C->A", "C", "A");// this will the same as an existing
											// edge

		mutableGraph1.addAll(builder.build());

		assertEquals(6, mutableGraph1.edgeCount());
		assertEquals(6, mutableGraph1.nodeCount());

		assertTrue(mutableGraph1.containsNode("A"));
		assertTrue(mutableGraph1.containsNode("B"));
		assertTrue(mutableGraph1.containsNode("C"));
		assertTrue(mutableGraph1.containsNode("J"));
		assertTrue(mutableGraph1.containsNode("V"));
		assertTrue(mutableGraph1.containsNode("K"));

		assertTrue(mutableGraph1.containsEdge("A->C"));
		assertTrue(mutableGraph1.containsEdge("B->C"));
		assertTrue(mutableGraph1.containsEdge("C->A"));
		assertTrue(mutableGraph1.containsEdge("A->B"));
		assertTrue(mutableGraph1.containsEdge("J->V"));
		assertTrue(mutableGraph1.containsEdge("B->A"));

		assertEquals("A", mutableGraph1.getOriginNode("A->C"));
		assertEquals("B", mutableGraph1.getOriginNode("B->C"));
		assertEquals("C", mutableGraph1.getOriginNode("C->A"));
		assertEquals("J", mutableGraph1.getOriginNode("A->B"));
		assertEquals("J", mutableGraph1.getOriginNode("J->V"));
		assertEquals("B", mutableGraph1.getOriginNode("B->A"));

		assertEquals("C", mutableGraph1.getDestinationNode("A->C"));
		assertEquals("C", mutableGraph1.getDestinationNode("B->C"));
		assertEquals("A", mutableGraph1.getDestinationNode("C->A"));
		assertEquals("K", mutableGraph1.getDestinationNode("A->B"));
		assertEquals("V", mutableGraph1.getDestinationNode("J->V"));
		assertEquals("A", mutableGraph1.getDestinationNode("B->A"));
	}

	/**
	 * Tests {@link MutableGraph#addEdge(Object, Object, Object)}
	 */
	@Test
	@UnitTestMethod(name = "addEdge", args = { Object.class, Object.class, Object.class })
	public void testAddEdge() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");

		assertTrue(mutableGraph.containsNode("A"));
		assertTrue(mutableGraph.containsNode("B"));
		assertTrue(mutableGraph.containsEdge("A->B"));
		assertEquals("A", mutableGraph.getOriginNode("A->B"));
		assertEquals("B", mutableGraph.getDestinationNode("A->B"));

		// show that the edge can replace an existing edge
		mutableGraph.addEdge("A->B", "B", "C");
		assertEquals("B", mutableGraph.getOriginNode("A->B"));
		assertEquals("C", mutableGraph.getDestinationNode("A->B"));

	}

	/**
	 * Tests {@link MutableGraph#MutableGraph())}
	 */
	@Test
	@UnitTestConstructor(args = {})
	public void testConstructor() {
		MutableGraph<Integer, String> mutableGraph = new MutableGraph<>();
		assertNotNull(mutableGraph);
		assertEquals(0, mutableGraph.nodeCount());
		assertEquals(0, mutableGraph.edgeCount());
	}

	/**
	 * Tests {@link MutableGraph#addNode(Object)}
	 */
	@Test
	@UnitTestMethod(name = "addNode", args = { Object.class })
	public void testAddNode() {
		MutableGraph<Integer, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode(1);
		mutableGraph.addNode(2);
		mutableGraph.addNode(3);
		mutableGraph.addNode(4);

		assertTrue(mutableGraph.containsNode(1));
		assertTrue(mutableGraph.containsNode(2));
		assertTrue(mutableGraph.containsNode(3));
		assertTrue(mutableGraph.containsNode(4));

		// re-adding should have no effect
		mutableGraph.addNode(4);
		assertTrue(mutableGraph.containsNode(4));
	}

	/**
	 * Tests {@link MutableGraph#containsEdge(Object)}
	 */
	@Test
	@UnitTestMethod(name = "containsEdge", args = { Object.class })
	public void testContainsEdge() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->A", "B", "A");

		assertTrue(mutableGraph.containsEdge("A->B"));
		assertTrue(mutableGraph.containsEdge("A->C"));
		assertTrue(mutableGraph.containsEdge("B->A"));

	}

	/**
	 * Tests {@link MutableGraph#containsNode(Object)}
	 */
	@Test
	@UnitTestMethod(name = "containsNode", args = { Object.class })
	public void testContainsNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode("A");
		mutableGraph.addNode("B");
		mutableGraph.addNode("C");

		assertTrue(mutableGraph.containsNode("A"));
		assertTrue(mutableGraph.containsNode("B"));
		assertTrue(mutableGraph.containsNode("C"));
	}

	/**
	 * Tests {@link MutableGraph#edgeCount(Object, Object)}
	 */
	@Test
	@UnitTestMethod(name = "edgeCount", args = { Object.class, Object.class })
	public void testEdgeCount_WithNodes() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B_1", "A", "B");
		mutableGraph.addEdge("A->B_2", "A", "B");
		mutableGraph.addEdge("A->B_3", "A", "B");
		mutableGraph.addEdge("A->C_1", "A", "C");
		mutableGraph.addEdge("A->C_2", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D_1", "B", "D");
		mutableGraph.addEdge("A->D_2", "A", "D");

		assertEquals(8, mutableGraph.edgeCount());

		assertEquals(3, mutableGraph.edgeCount("A", "B"));
		assertEquals(2, mutableGraph.edgeCount("A", "C"));
		assertEquals(1, mutableGraph.edgeCount("B", "C"));
		assertEquals(1, mutableGraph.edgeCount("B", "D"));
		assertEquals(1, mutableGraph.edgeCount("A", "D"));
		assertEquals(0, mutableGraph.edgeCount("C", "D"));
	}

	/**
	 * Tests {@link MutableGraph#edgeCount()}
	 */
	@Test
	@UnitTestMethod(name = "edgeCount", args = {})
	public void testEdgeCount_WithoutNodes() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B_1", "A", "B");
		mutableGraph.addEdge("A->B_2", "A", "B");
		mutableGraph.addEdge("A->B_3", "A", "B");
		mutableGraph.addEdge("A->C_1", "A", "C");
		mutableGraph.addEdge("A->C_2", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D_1", "B", "D");
		mutableGraph.addEdge("A->D_2", "A", "D");

		assertEquals(8, mutableGraph.edgeCount());

		assertEquals(3, mutableGraph.edgeCount("A", "B"));
		assertEquals(2, mutableGraph.edgeCount("A", "C"));
		assertEquals(1, mutableGraph.edgeCount("B", "C"));
		assertEquals(1, mutableGraph.edgeCount("B", "D"));
		assertEquals(1, mutableGraph.edgeCount("A", "D"));
		assertEquals(0, mutableGraph.edgeCount("C", "D"));
	}

	/**
	 * Tests {@link MutableGraph#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addNode("C");

		MutableGraph<String, String> mutableGraph2 = new MutableGraph<>();
		mutableGraph2.addEdge("A->B", "A", "B");
		mutableGraph2.addNode("C");

		assertEquals(mutableGraph1, mutableGraph2);
		mutableGraph2.addNode("D");

		assertNotEquals(mutableGraph1, mutableGraph2);

		Graph.Builder<String, String> builder = Graph.builder();
		builder.addEdge("A->B", "A", "B");
		builder.addNode("C");
		Graph<String, String> graph = builder.build();

		assertEquals(mutableGraph1, graph);

		mutableGraph1.addNode("D");
		assertNotEquals(mutableGraph1, graph);

	}

	/**
	 * Tests {@link MutableGraph#formsEdgeRelationship(Object, Object, Object)}
	 */
	@Test
	@UnitTestMethod(name = "formsEdgeRelationship", args = { Object.class, Object.class, Object.class })
	public void testFormsEdgeRelationship() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");

		assertTrue(mutableGraph.formsEdgeRelationship("A->B", "A", "B"));
		assertTrue(mutableGraph.formsEdgeRelationship("A->C", "A", "C"));
		assertTrue(mutableGraph.formsEdgeRelationship("B->C", "B", "C"));

		assertFalse(mutableGraph.formsEdgeRelationship("A->B", "A", "C"));
		assertFalse(mutableGraph.formsEdgeRelationship("A->C", "A", "B"));
		assertFalse(mutableGraph.formsEdgeRelationship("B->C", "B", "A"));

	}

	/**
	 * Tests {@link MutableGraph#getDestinationNode(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getDestinationNode", args = { Object.class })
	public void testGetDestinationNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");

		assertEquals("B", mutableGraph.getDestinationNode("A->B"));
		assertEquals("C", mutableGraph.getDestinationNode("A->C"));
		assertEquals("C", mutableGraph.getDestinationNode("B->C"));

	}

	/**
	 * Tests {@link MutableGraph#getEdges()}
	 */
	@Test
	@UnitTestMethod(name = "getEdges", args = {})
	public void testGetEdges_NoArgs() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");

		Set<String> expected = new LinkedHashSet<>();
		expected.add("A->B");
		expected.add("A->C");
		expected.add("B->C");

		Set<String> actual = mutableGraph.getEdges().stream().collect(Collectors.toCollection(LinkedHashSet::new));

		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link MutableGraph#getEdges(Object, Object)}
	 */
	@Test
	@UnitTestMethod(name = "getEdges", args = { Object.class, Object.class })
	public void testGetEdges_Objects() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();

		mutableGraph.addEdge("E1", "B", "C");
		mutableGraph.addEdge("E2", "C", "B");
		mutableGraph.addEdge("E3", "B", "C");
		mutableGraph.addEdge("E4", "B", "A");
		mutableGraph.addEdge("E5", "B", "D");
		mutableGraph.addEdge("E6", "A", "B");
		mutableGraph.addEdge("E7", "A", "A");

		Set<String> expected = new LinkedHashSet<>();
		;
		Set<String> actual;

		expected.clear();
		expected.add("E7");
		actual = mutableGraph.getEdges("A", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E6");
		actual = mutableGraph.getEdges("A", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("A", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("A", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E4");
		actual = mutableGraph.getEdges("B", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("B", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E1");
		expected.add("E3");
		actual = mutableGraph.getEdges("B", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E5");
		actual = mutableGraph.getEdges("B", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("C", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		expected.add("E2");
		actual = mutableGraph.getEdges("C", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("C", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("C", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("D", "A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("D", "B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("D", "C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected.clear();
		actual = mutableGraph.getEdges("D", "D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link MutableGraph#getInboundEdgeCount(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getInboundEdgeCount", args = { Object.class })
	public void testGetInboundEdgeCount() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		assertEquals(1, mutableGraph.getInboundEdgeCount("A"));
		assertEquals(2, mutableGraph.getInboundEdgeCount("B"));
		assertEquals(2, mutableGraph.getInboundEdgeCount("C"));
		assertEquals(1, mutableGraph.getInboundEdgeCount("D"));

	}

	/**
	 * Tests {@link MutableGraph#getInboundEdges(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getInboundEdges", args = { Object.class })
	public void testGetInboundEdges() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		Set<String> expected = new LinkedHashSet<>();
		expected.add("D->A");
		Set<String> actual = mutableGraph.getInboundEdges("A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("A->B");
		expected.add("D->B");
		actual = mutableGraph.getInboundEdges("B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("A->C");
		expected.add("B->C");
		actual = mutableGraph.getInboundEdges("C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("B->D");
		actual = mutableGraph.getInboundEdges("D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link MutableGraph#getNodes()}
	 */
	@Test
	@UnitTestMethod(name = "getNodes", args = {})
	public void testGetNodes() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		List<String> nodes = new ArrayList<>();
		nodes.add("A");
		nodes.add("B");
		nodes.add("C");
		nodes.add("D");
		nodes.add("A");
		nodes.add("B");

		Set<String> expected = new LinkedHashSet<>(nodes);

		for (String node : nodes) {
			mutableGraph.addNode(node);
		}

		List<String> nodesAsList = mutableGraph.getNodes();
		assertEquals(4, nodesAsList.size());

		Set<String> actual = nodesAsList.stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link MutableGraph#getOriginNode(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getOriginNode", args = { Object.class })
	public void testGetOriginNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		assertEquals("A", mutableGraph.getOriginNode("A->B"));
		assertEquals("A", mutableGraph.getOriginNode("A->C"));
		assertEquals("B", mutableGraph.getOriginNode("B->C"));
		assertEquals("B", mutableGraph.getOriginNode("B->D"));
		assertEquals("D", mutableGraph.getOriginNode("D->A"));
		assertEquals("D", mutableGraph.getOriginNode("D->B"));
	}

	/**
	 * Tests {@link MutableGraph#getOutboundEdgeCount(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getOutboundEdgeCount", args = { Object.class })
	public void testGetOutboundEdgeCount() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		assertEquals(2, mutableGraph.getOutboundEdgeCount("A"));
		assertEquals(2, mutableGraph.getOutboundEdgeCount("B"));
		assertEquals(0, mutableGraph.getOutboundEdgeCount("C"));
		assertEquals(2, mutableGraph.getOutboundEdgeCount("D"));
	}

	/**
	 * Tests {@link MutableGraph#getOutboundEdges(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getOutboundEdges", args = { Object.class })
	public void testGetOutboundEdges() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		Set<String> expected = new LinkedHashSet<>();
		expected.add("A->B");
		expected.add("A->C");
		Set<String> actual = mutableGraph.getOutboundEdges("A").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("B->C");
		expected.add("B->D");
		actual = mutableGraph.getOutboundEdges("B").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		actual = mutableGraph.getOutboundEdges("C").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("D->A");
		expected.add("D->B");
		actual = mutableGraph.getOutboundEdges("D").stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);
	}

	/**
	 * Tests {@link MutableGraph#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		MutableGraph<String, String> mutableGraph1 = new MutableGraph<>();
		mutableGraph1.addEdge("A->B", "A", "B");
		mutableGraph1.addEdge("A->C", "A", "C");
		mutableGraph1.addEdge("B->C", "B", "C");
		mutableGraph1.addEdge("B->D", "B", "D");
		mutableGraph1.addEdge("D->A", "D", "A");
		mutableGraph1.addEdge("D->B", "D", "B");

		int expected = "A".hashCode();
		expected += "B".hashCode();
		expected += "C".hashCode();
		expected += "D".hashCode();
		expected += "A->B".hashCode();
		expected += "A->C".hashCode();
		expected += "B->C".hashCode();
		expected += "B->D".hashCode();
		expected += "D->A".hashCode();
		expected += "D->B".hashCode();

		int actual = mutableGraph1.hashCode();
		assertEquals(expected, actual);

		// build a copy with the edges added in a different order
		MutableGraph<String, String> mutableGraph2 = new MutableGraph<>();
		mutableGraph2.addEdge("D->B", "D", "B");
		mutableGraph2.addEdge("D->A", "D", "A");
		mutableGraph2.addEdge("B->D", "B", "D");
		mutableGraph2.addEdge("B->C", "B", "C");
		mutableGraph2.addEdge("A->C", "A", "C");
		mutableGraph2.addEdge("A->B", "A", "B");

		assertEquals(mutableGraph1, mutableGraph2);

		// equal objects have equal hash codes
		assertEquals(mutableGraph1.hashCode(), mutableGraph2.hashCode());

	}

	/**
	 * Tests {@link MutableGraph#isEmpty()}
	 */
	@Test
	@UnitTestMethod(name = "isEmpty", args = {})
	public void testIsEmpty() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		assertTrue(mutableGraph.isEmpty());

		mutableGraph.addNode("A");
		assertFalse(mutableGraph.isEmpty());

		mutableGraph.removeNode("A");
		assertTrue(mutableGraph.isEmpty());
	}

	/**
	 * Tests {@link MutableGraph#nodeCount()}
	 */
	@Test
	@UnitTestMethod(name = "nodeCount", args = {})
	public void testNodeCount() {
		MutableGraph<Integer, String> mutableGraph = new MutableGraph<>();
		assertEquals(0, mutableGraph.nodeCount());
		for (int i = 0; i < 10; i++) {
			mutableGraph.addNode(i);
			assertEquals(i + 1, mutableGraph.nodeCount());
		}

		for (int i = 9; i >= 0; i--) {
			mutableGraph.removeNode(i);
			assertEquals(i, mutableGraph.nodeCount());
		}
	}

	/**
	 * Tests {@link MutableGraph#removeEdge(Object)}
	 */
	@Test
	@UnitTestMethod(name = "removeEdge", args = { Object.class })
	public void testRemoveEdge() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->C", "B", "C");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addEdge("D->A", "D", "A");
		mutableGraph.addEdge("D->B", "D", "B");

		assertEquals(6, mutableGraph.edgeCount());
		mutableGraph.removeEdge("A->B");
		assertEquals(5, mutableGraph.edgeCount());
		assertFalse(mutableGraph.containsEdge("A->B"));
		mutableGraph.removeEdge("A->B");
		assertEquals(5, mutableGraph.edgeCount());
		assertFalse(mutableGraph.containsEdge("A->B"));

		mutableGraph.removeEdge("B->C");
		assertEquals(4, mutableGraph.edgeCount());
		assertFalse(mutableGraph.containsEdge("B->C"));
		mutableGraph.removeEdge("B->C");
		assertEquals(4, mutableGraph.edgeCount());
		assertFalse(mutableGraph.containsEdge("B->C"));
	}

	/**
	 * Tests {@link MutableGraph#removeNode(Object)}
	 */
	@Test
	@UnitTestMethod(name = "removeNode", args = { Object.class })
	public void testRemoveNode() {
		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addNode("A");
		mutableGraph.addNode("B");
		mutableGraph.addNode("C");
		mutableGraph.addNode("D");
		mutableGraph.addNode("E");
		mutableGraph.addNode("F");

		assertEquals(6, mutableGraph.nodeCount());
		mutableGraph.removeNode("A");
		assertEquals(5, mutableGraph.nodeCount());
		assertFalse(mutableGraph.containsNode("A"));
		mutableGraph.removeNode("A");
		assertEquals(5, mutableGraph.nodeCount());
		assertFalse(mutableGraph.containsNode("A"));

		assertEquals(5, mutableGraph.nodeCount());
		mutableGraph.removeNode("B");
		assertEquals(4, mutableGraph.nodeCount());
		assertFalse(mutableGraph.containsNode("B"));
		mutableGraph.removeNode("B");
		assertEquals(4, mutableGraph.nodeCount());
		assertFalse(mutableGraph.containsNode("B"));

	}

	/**
	 * Tests {@link MutableGraph#toGraph()}
	 */
	@Test
	@UnitTestMethod(name = "toGraph", args = {})
	public void testToGraph() {

		MutableGraph<String, String> mutableGraph = new MutableGraph<>();
		mutableGraph.addEdge("A->B", "A", "B");
		mutableGraph.addEdge("A->C", "A", "C");
		mutableGraph.addEdge("B->A", "B", "A");
		mutableGraph.addEdge("B->D", "B", "D");
		mutableGraph.addNode("E");

		Graph<String, String> graph = mutableGraph.toGraph();

		assertEquals(graph, mutableGraph);

	}
}
