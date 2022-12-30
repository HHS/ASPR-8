package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

public final class AT_ActorId {

	@UnitTestConstructor(target = ActorId.class, args = { int.class })
	@Test
	public void testConstructor() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ActorId(i).getValue());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new ActorId(i).getValue());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("ActorId [id=" + i + "]", new ActorId(i).toString());
		}
	}

	@UnitTestMethod(target = ActorId.class, name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		// show equal objects have equal hashcodes
		for (int i = 0; i < 10; i++) {
			ActorId a = new ActorId(i);
			ActorId b = new ActorId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}

		// show that hash codes are dispersed
		Set<Integer> hashcodes = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			hashcodes.add(new ActorId(i).hashCode());
		}
		assertEquals(1000, hashcodes.size());

	}

	@UnitTestMethod(target = ActorId.class, name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		// show actor ids are equal if and only if they have the same base int
		// value
		for (int i = 0; i < 10; i++) {
			ActorId a = new ActorId(i);
			for (int j = 0; j < 10; j++) {
				ActorId b = new ActorId(j);
				if (i == j) {
					assertEquals(a, b);
				} else {
					assertNotEquals(a, b);
				}
			}
		}
	}

}
