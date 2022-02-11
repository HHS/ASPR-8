package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import util.annotations.UnitTest;
import util.annotations.UnitTestMethod;

/**
 * Test unit for AgentId
 * 
 * @author Shawn Hatch
 *
 */
@UnitTest(target = AgentId.class)
public final class AT_AgentId {

	@UnitTestMethod(name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new AgentId(i).getValue());
		}
	}

	@UnitTestMethod(name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("AgentId [id=" + i + "]", new AgentId(i).toString());
		}
	}

	@UnitTestMethod(name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		// show equal objects have equal hashcodes
		for (int i = 0; i < 10; i++) {
			AgentId a = new AgentId(i);
			AgentId b = new AgentId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}
		
		//show that hash codes are dispersed
		Set<Integer> hashcodes = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			hashcodes.add(new AgentId(i).hashCode());
		}
		assertEquals(1000, hashcodes.size());
		
	}

	@UnitTestMethod(name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		// show agent ids are equal if and only if they have the same base int
		// value
		for (int i = 0; i < 10; i++) {
			AgentId a = new AgentId(i);
			for (int j = 0; j < 10; j++) {
				AgentId b = new AgentId(j);
				if (i == j) {
					assertEquals(a, b);
				} else {
					assertNotEquals(a, b);
				}
			}
		}
	}

}
