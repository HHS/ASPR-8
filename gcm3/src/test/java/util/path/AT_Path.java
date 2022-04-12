package util.path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;

/**
 * Test class for {@link Path}
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = Path.class)
public class AT_Path {

	/**
	 * Tests {@link Path#builder()}
	 */
	@Test
	@UnitTestMethod(name = "builder", args = {})
	public void testBuilder() {
		Path.Builder<Integer> builder = Path.builder();
		Path<Integer> path = builder.build();
		assertNotNull(path);

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		builder.addEdge(1);
		path = builder.build();
		assertNotNull(path);

	}

	/**
	 * Tests {@link Path#equals(Object)}
	 */
	@Test
	@UnitTestMethod(name = "equals", args = { Object.class })
	public void testEquals() {
		Path.Builder<Integer> builder = Path.builder();

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		builder.addEdge(1);
		Path<Integer> path1 = builder.build();

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		builder.addEdge(1);
		Path<Integer> path2 = builder.build();

		assertEquals(path1, path2);
		assertEquals(path1, path1);
		assertEquals(path2, path1);

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		// builder.addEdge(1);

		Path<Integer> path3 = builder.build();
		assertNotEquals(path1, path3);

	}

	/**
	 * Tests {@link Path#hashCode()}
	 */
	@Test
	@UnitTestMethod(name = "hashCode", args = {})
	public void testHashCode() {
		Path.Builder<Integer> builder = Path.builder();

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		builder.addEdge(1);
		Path<Integer> path1 = builder.build();

		builder.addEdge(1);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		builder.addEdge(1);
		Path<Integer> path2 = builder.build();

		assertEquals(path1, path2);

		assertEquals(path1.hashCode(), path2.hashCode());

	}

	/**
	 * Tests {@link Path#getEdges()}
	 */
	@Test
	@UnitTestMethod(name = "getEdges", args = {})
	public void testGetEdges() {
		Path.Builder<Integer> builder = Path.builder();

		List<Integer> expected = new ArrayList<>();

		expected.add(1);
		expected.add(3);
		expected.add(4);
		expected.add(5);
		expected.add(6);
		expected.add(1);

		for (Integer edge : expected) {
			builder.addEdge(edge);
		}

		Path<Integer> path = builder.build();
		List<Integer> actual = path.getEdges();
		assertEquals(expected, actual);

	}

	/**
	 * Tests {@link Path#isEmpty()}
	 */
	@Test
	@UnitTestMethod(name = "isEmpty", args = {})
	public void testIsEmpty() {
		Path.Builder<Integer> builder = Path.builder();
		Path<Integer> path = builder.build();
		assertTrue(path.isEmpty());

		builder.addEdge(1);
		path = builder.build();
		assertFalse(path.isEmpty());
	}

	/**
	 * Tests {@link Path#length()}
	 */
	@Test
	@UnitTestMethod(name = "length", args = {})
	public void testLength() {
		Path.Builder<Integer> builder = Path.builder();
		Path<Integer> path = builder.build();
		assertEquals(0, path.length());

		builder.addEdge(1);
		builder.addEdge(2);
		builder.addEdge(3);
		builder.addEdge(4);
		builder.addEdge(5);
		builder.addEdge(6);
		path = builder.build();
		assertEquals(6, path.length());

		builder.addEdge(1);
		builder.addEdge(2);
		builder.addEdge(3);
		builder.addEdge(2);
		builder.addEdge(1);
		path = builder.build();
		assertEquals(5, path.length());

	}

}