package util.vector;

import java.util.List;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTag;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;
import util.vector.Circle2D.SolutionAlgorithm;

public class AT_Circle2D {

	@Test
	@UnitTestConstructor(target = Circle2D.class, args = { List.class, SolutionAlgorithm.class }, tags = { UnitTag.MANUAL })
	public void testConstructor() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "contains", args = { List.class }, tags = { UnitTag.MANUAL })
	public void testContains_List() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "contains", args = { Vector2D.class }, tags = { UnitTag.MANUAL })
	public void testContains_Vector() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "getCenter", args = {}, tags = { UnitTag.MANUAL })
	public void testGetCenter() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "getRadius", args = {}, tags = { UnitTag.MANUAL })
	public void testGetRadius() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "isFinite", args = {}, tags = { UnitTag.MANUAL })
	public void testIsFinite() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(target = Circle2D.class, name = "toString", args = {}, tags = { UnitTag.MANUAL })
	public void testToString() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

}
