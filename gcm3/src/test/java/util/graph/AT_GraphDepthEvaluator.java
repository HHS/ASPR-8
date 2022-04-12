package util.graph;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link GraphDepthEvaluator}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = GraphDepthEvaluator.class)
public class AT_GraphDepthEvaluator {

	/**
	 * Tests {@link GraphDepthEvaluator#getGraphDepthEvaluator(Graph)}
	 */
	@Test
	@UnitTestMethod(name = "getGraphDepthEvaluator", args = { Graph.class })
	public void testGetGraphDepthEvaluator() {
		MutableGraph<String, String> m = new MutableGraph<>();
		// empty graphs are acyclic
		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
		assertTrue(optional.isPresent());

		m.addEdge("A->B", "A", "B");
		optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
		assertTrue(optional.isPresent());

		// the graph has a cycle and so no depth evaluator is generated
		m.addEdge("B->A", "B", "A");
		optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());
		assertFalse(optional.isPresent());

	}

	/**
	 * Tests {@link GraphDepthEvaluator#getDepth(Object)}
	 */
	@Test
	@UnitTestMethod(name = "getDepth", args = { Object.class })
	public void testGetDepth() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("C->D", "C", "D");
		m.addEdge("A->D", "A", "D");

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		assertTrue(optional.isPresent());
		GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();
		assertEquals(2, graphDepthEvaluator.getDepth("A"));
		assertEquals(0, graphDepthEvaluator.getDepth("B"));
		assertEquals(1, graphDepthEvaluator.getDepth("C"));
		assertEquals(0, graphDepthEvaluator.getDepth("D"));

	}

	/**
	 * Tests {@link GraphDepthEvaluator#getMaxDepth()}
	 */
	@Test
	@UnitTestMethod(name = "getMaxDepth", args = {})
	public void testGetMaxDepth() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("C->D", "C", "D");
		m.addEdge("A->D", "A", "D");
		m.addEdge("D->E", "D", "E");

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		assertTrue(optional.isPresent());
		GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();
		assertEquals(3, graphDepthEvaluator.getMaxDepth());

	}

	/**
	 * Tests {@link GraphDepthEvaluator#getNodesForDepth(int)}
	 */
	@Test
	@UnitTestMethod(name = "getNodesForDepth", args = { int.class })
	public void testGetNodesForDepth() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("C->D", "C", "D");
		m.addEdge("A->D", "A", "D");
		m.addEdge("D->E", "D", "E");

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		assertTrue(optional.isPresent());
		GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();

		assertEquals(3, graphDepthEvaluator.getMaxDepth());

		Set<String> expected = new LinkedHashSet<>();
		expected.add("A");
		Set<String> actual = graphDepthEvaluator.getNodesForDepth(3).stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("C");
		actual = graphDepthEvaluator.getNodesForDepth(2).stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("D");
		actual = graphDepthEvaluator.getNodesForDepth(1).stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

		expected = new LinkedHashSet<>();
		expected.add("B");
		expected.add("E");
		actual = graphDepthEvaluator.getNodesForDepth(0).stream().collect(Collectors.toCollection(LinkedHashSet::new));
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link GraphDepthEvaluator#getNodesInRankOrder()}
	 */
	@Test
	@UnitTestMethod(name = "getNodesInRankOrder", args = {})
	public void testGetNodesInRankOrder() {
		MutableGraph<String, String> m = new MutableGraph<>();
		m.addEdge("A->B", "A", "B");
		m.addEdge("A->C", "A", "C");
		m.addEdge("C->D", "C", "D");
		m.addEdge("A->D", "A", "D");
		m.addEdge("D->E", "D", "E");

		Optional<GraphDepthEvaluator<String>> optional = GraphDepthEvaluator.getGraphDepthEvaluator(m.toGraph());

		assertTrue(optional.isPresent());
		GraphDepthEvaluator<String> graphDepthEvaluator = optional.get();

		List<String> actual = graphDepthEvaluator.getNodesInRankOrder();

		// show that the values returned contain each node exactly once
		assertEquals(m.nodeCount(), actual.size());
		assertEquals(m.nodeCount(), actual.stream().collect(Collectors.toCollection(LinkedHashSet::new)).size());

		int previousDepth = 0;
		for (String node : actual) {
			int depth = graphDepthEvaluator.getDepth(node);
			assertTrue(depth >= previousDepth);
			previousDepth = depth;
		}

	}

}
