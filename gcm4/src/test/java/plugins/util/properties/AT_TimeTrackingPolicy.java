package plugins.util.properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import util.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

public class AT_TimeTrackingPolicy {

	@Test
	@UnitTestMethod(target = TimeTrackingPolicy.class, name = "next", args = {})
	public void testNext() {
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			int index = timeTrackingPolicy.ordinal();
			index += 1;
			index %= TimeTrackingPolicy.values().length;
			TimeTrackingPolicy expectedNextTimeTrackingPolicy = TimeTrackingPolicy.values()[index];
			assertEquals(expectedNextTimeTrackingPolicy, timeTrackingPolicy.next());
		}
	}

	@Test
	@UnitTestMethod(target = TimeTrackingPolicy.class, name = "getRandomTimeTrackingPolicy", args = { RandomGenerator.class })
	public void testGetRandomTimeTrackingPolicy() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(8585032723446211346L);
		Map<TimeTrackingPolicy, MutableInteger> counterMap = new LinkedHashMap<>();
		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			counterMap.put(timeTrackingPolicy, new MutableInteger());
		}

		for (int i = 0; i < 100; i++) {
			TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.getRandomTimeTrackingPolicy(randomGenerator);
			counterMap.get(timeTrackingPolicy).increment();
		}

		for (TimeTrackingPolicy timeTrackingPolicy : TimeTrackingPolicy.values()) {
			assertTrue(counterMap.get(timeTrackingPolicy).getValue() > 40);
		}

	}

}
