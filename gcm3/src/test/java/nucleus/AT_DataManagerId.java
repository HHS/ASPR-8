package nucleus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import annotations.UnitTest;
import annotations.UnitTestMethod;


@UnitTest(target = DataManagerId.class)
public final class AT_DataManagerId {

	@UnitTestMethod(name = "getValue", args = {})
	@Test
	public void testGetValue() {
		for (int i = 0; i < 100; i++) {
			assertEquals(i, new DataManagerId(i).getValue());
		}
	}
	
	
	
	@UnitTestMethod(name = "toString", args = {})
	@Test
	public void testToString() {
		for (int i = 0; i < 100; i++) {
			assertEquals("DataManagerId [id=" + i + "]", new DataManagerId(i).toString());
		}
	}

	@UnitTestMethod(name = "hashCode", args = {})
	@Test
	public void testHashCode() {
		// show equal objects have equal hashcodes
		for (int i = 0; i < 10; i++) {
			DataManagerId a = new DataManagerId(i);
			DataManagerId b = new DataManagerId(i);
			assertEquals(a, b);
			assertEquals(a.hashCode(), b.hashCode());
		}
		
		//show that hash codes are dispersed
		Set<Integer> hashcodes = new LinkedHashSet<>();
		for (int i = 0; i < 1000; i++) {
			hashcodes.add(new DataManagerId(i).hashCode());
		}
		assertEquals(1000, hashcodes.size());
		
	}

	@UnitTestMethod(name = "equals", args = { Object.class })
	@Test
	public void testEquals() {
		// show data manager ids are equal if and only if they have the same base int
		// value
		for (int i = 0; i < 10; i++) {
			DataManagerId a = new DataManagerId(i);
			for (int j = 0; j < 10; j++) {
				DataManagerId b = new DataManagerId(j);
				if (i == j) {
					assertEquals(a, b);
				} else {
					assertNotEquals(a, b);
				}
			}
		}
	}

}
