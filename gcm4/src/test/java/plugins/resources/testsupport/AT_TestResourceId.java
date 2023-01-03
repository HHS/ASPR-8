package plugins.resources.testsupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceId;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TestResourceId {

	@Test
	@UnitTestMethod(target = TestResourceId.class, name = "getTimeTrackingPolicy", args = {})
	public void testGetTimeTrackingPolicy() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			assertNotNull(testResourceId.getTimeTrackingPolicy());
		}
	}

	@Test
	@UnitTestMethod(target = TestResourceId.class, name = "getRandomResourceId", args = { RandomGenerator.class })
	public void testGetRandomResourceId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5357990509395444631L);
		Map<TestResourceId, MutableInteger> idCounter = new LinkedHashMap<>();

		for (TestResourceId testResourceId : TestResourceId.values()) {
			idCounter.put(testResourceId, new MutableInteger());
		}

		for (int i = 0; i < 50; i++) {
			TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
			idCounter.get(testResourceId).increment();
		}

		for (TestResourceId testResourceId : idCounter.keySet()) {
			assertTrue(idCounter.get(testResourceId).getValue() >= 5 && idCounter.get(testResourceId).getValue() <= 30);
		}
	}

	@Test
	@UnitTestMethod(target = TestResourceId.class, name = "getUnknownResourceId", args = {})
	public void testGetUnknownResourceId() {
		Set<TestResourceId> oldIds = EnumSet.allOf(TestResourceId.class);
		Set<ResourceId> unknownIds = new LinkedHashSet<>();

		// show that each unknown id is not null and unique
		for (int i = 0; i < 50; i++) {
			ResourceId unknownResourceId = TestResourceId.getUnknownResourceId();
			assertNotNull(unknownResourceId);
			boolean unique = unknownIds.add(unknownResourceId);
			assertTrue(unique);
			assertFalse(oldIds.contains(unknownResourceId));
		}
	}

	@Test
	@UnitTestMethod(target = TestResourceId.class, name = "size", args = {})
	public void testSize() {
		assertNotNull(TestResourceId.size());
		assertEquals(TestResourceId.size(), 5);
	}

	@Test
	@UnitTestMethod(target = TestResourceId.class, name = "next", args = {})
	public void testNext() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			int index = (testResourceId.ordinal() + 1) % TestResourceId.values().length;
			TestResourceId expectedNext = TestResourceId.values()[index];
			assertNotNull(testResourceId.next());
			assertEquals(expectedNext, testResourceId.next());
		}
	}

}