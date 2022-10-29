package util.vector;

import java.util.List;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.vector.Circle2D.SolutionAlgorithm;

@UnitTest(target = Circle2D.class)
public class AT_Circle2D {

	@Test
	@UnitTestConstructor(args = { List.class, SolutionAlgorithm.class }, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testConstructor() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "contains",args = { List.class}, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testContains_List() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "contains",args = { Vector2D.class}, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testContains_Vector() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "getCenter",args = { }, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testGetCenter() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "getRadius",args = { }, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testGetRadius() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "isFinite",args = { }, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testIsFinite() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

	@Test
	@UnitTestMethod(name = "toString",args = { }, tags = { UnitTag.MANUAL, UnitTag.CLASS_PROXY })
	public void testToString() {
		// test deferred to manual tests found in MT_Circle2D.java
	}

}
