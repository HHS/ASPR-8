package plugins.resources.testsupport;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import plugins.resources.support.ResourcePropertyId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = TestResourcePropertyId.class)
public class AT_TestResourcePropertyId {

	@Test
	@UnitTestMethod(name = "getPropertyDefinition", args = {})
	public void testGetPropertyDefinition() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			assertNotNull(testResourcePropertyId.getPropertyDefinition());
		}
	}

	@Test
	@UnitTestMethod(name = "getTestResourceId", args = {})
	public void testGetTestResourceId() {
		for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
			assertNotNull(testResourcePropertyId.getTestResourceId());
		}
	}

	@Test
	@UnitTestMethod(name = "getUnknownResourcePropertyId", args = {})
	public void testGetUnknownResourcePropertyId() {
		assertNotNull(TestResourcePropertyId.getUnknownResourcePropertyId());
	}

	@Test
	@UnitTestMethod(name = "getTestResourcePropertyIds", args = { TestResourceId.class })
	public void testGetTestResourcePropertyIds() {
		for (TestResourceId testResourceId : TestResourceId.values()) {
			assertNotNull(TestResourcePropertyId.getTestResourcePropertyIds(testResourceId));
		}
	}

	@Test
	@UnitTestMethod(name = "getRandomResourcePropertyId", args = { TestResourceId.class, RandomGenerator.class })
	public void testGetRandomResourcePropertyId() {
		RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(7615402310345074403L);

		for (TestResourceId testResourceId : TestResourceId.values()) {

			//gather the expected test resource property id values for each given test resource
			Set<TestResourcePropertyId> expectedTestResourcePropertyIds = new LinkedHashSet<>();
			for (TestResourcePropertyId testResourcePropertyId : TestResourcePropertyId.values()) {
				if (testResourcePropertyId.getTestResourceId().equals(testResourceId)) {
					expectedTestResourcePropertyIds.add(testResourcePropertyId);
				}
			}

			//initialize the property id counter to zeros
			Map<TestResourcePropertyId, MutableInteger> propertyIdCounter = new LinkedHashMap<>();
			for (TestResourcePropertyId testResourcePropertyId : expectedTestResourcePropertyIds) {
				propertyIdCounter.put(testResourcePropertyId, new MutableInteger());
			}
			
			//sample a reasonable number of invocations
			int sampleCount = 10 * expectedTestResourcePropertyIds.size();
			for (int i = 0; i < sampleCount; i++) {
				TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator);
				assertTrue(expectedTestResourcePropertyIds.contains(testResourcePropertyId));
				propertyIdCounter.get(testResourcePropertyId).increment();
			}
			
			//show that we get a reasonable number of matches to each resource property id
			for (TestResourcePropertyId testResourcePropertyId : propertyIdCounter.keySet()) {
				MutableInteger mutableInteger = propertyIdCounter.get(testResourcePropertyId);
				int value = mutableInteger.getValue();
				assertTrue(value >= 5 && value <= 20);
			}

		}
	}

	@Test
	@UnitTestMethod(name = "getRandomPropertyValue", args = { RandomGenerator.class })
	public void testGetRandomPropertyValue() {

	}

}