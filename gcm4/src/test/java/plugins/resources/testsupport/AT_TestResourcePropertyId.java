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
    @UnitTestMethod(name = "getTestResourcePropertyIds", args = {TestResourceId.class})
    public void testGetTestResourcePropertyIds() {
        for (TestResourceId testResourceId : TestResourceId.values()){
            assertNotNull(TestResourcePropertyId.getTestResourcePropertyIds(testResourceId));
        }
    }

    @Test
    @UnitTestMethod(name = "getRandomResourcePropertyId", args = {TestResourceId.class, RandomGenerator.class})
    public void testGetRandomResourcePropertyId() {
        RandomGenerator randomGenerator1 = RandomGeneratorProvider.getRandomGenerator(7615402310345074403L);
        RandomGenerator randomGenerator2 = RandomGeneratorProvider.getRandomGenerator(2029800720935616669L);
        RandomGenerator randomGenerator3 = RandomGeneratorProvider.getRandomGenerator(6191500405224903552L);
        RandomGenerator randomGenerator4 = RandomGeneratorProvider.getRandomGenerator(622791461093715959L);
        RandomGenerator randomGenerator5 = RandomGeneratorProvider.getRandomGenerator(251436464410082863L);

        for (TestResourceId testResourceId : TestResourceId.values()) {
            switch (testResourceId) {
                case RESOURCE_1:
                    Map<ResourcePropertyId, MutableInteger> firstIdCounter = new LinkedHashMap<>();
                    Set<ResourcePropertyId> firstSetOfResourcePropertyIds = new LinkedHashSet<>();

                    for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
                        firstIdCounter.put(resourcePropertyId, new MutableInteger());
                    }
                    // show that resource values are reasonably unique
                    for (int i = 0; i < 30; i++) {
                        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator1);
                        firstSetOfResourcePropertyIds.add(testResourcePropertyId);
                        firstIdCounter.get(testResourcePropertyId).increment();
                    }

                    for (ResourcePropertyId resourcePropertyId : firstIdCounter.keySet()) {
                        assertTrue(firstIdCounter.get(resourcePropertyId).getValue() >= 5 && firstIdCounter.get(resourcePropertyId).getValue() <= 20);
                    }
                    assertEquals(firstSetOfResourcePropertyIds.size(), 3);
                    break;

                case RESOURCE_2:
                    Map<ResourcePropertyId, MutableInteger> secondIdCounter = new LinkedHashMap<>();
                    Set<ResourcePropertyId> secondSetOfResourcePropertyIds = new LinkedHashSet<>();

                    for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
                        secondIdCounter.put(resourcePropertyId, new MutableInteger());
                    }
                    // show that resource values are reasonably unique
                    for (int i = 0; i < 20; i++) {
                        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator2);
                        secondSetOfResourcePropertyIds.add(testResourcePropertyId);
                        secondIdCounter.get(testResourcePropertyId).increment();
                    }

                    for (ResourcePropertyId resourcePropertyId : secondIdCounter.keySet()) {
                        assertTrue(secondIdCounter.get(resourcePropertyId).getValue() >= 5 && secondIdCounter.get(resourcePropertyId).getValue() <= 15);
                    }
                    assertEquals(secondSetOfResourcePropertyIds.size(), 2);
                    break;

                case RESOURCE_3:
                    Map<ResourcePropertyId, MutableInteger> thirdIdCounter = new LinkedHashMap<>();
                    Set<ResourcePropertyId> thirdSetOfResourcePropertyIds = new LinkedHashSet<>();

                    for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
                        thirdIdCounter.put(resourcePropertyId, new MutableInteger());
                    }
                    // show that resource values are reasonably unique
                    for (int i = 0; i < 20; i++) {
                        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator3);
                        thirdSetOfResourcePropertyIds.add(testResourcePropertyId);
                        thirdIdCounter.get(testResourcePropertyId).increment();
                    }

                    for (ResourcePropertyId resourcePropertyId : thirdIdCounter.keySet()) {
                        assertTrue(thirdIdCounter.get(resourcePropertyId).getValue() >= 5 && thirdIdCounter.get(resourcePropertyId).getValue() <= 15);
                    }
                    assertEquals(thirdSetOfResourcePropertyIds.size(), 2);
                    break;

                case RESOURCE_4:
                    Map<ResourcePropertyId, MutableInteger> fourthIdCounter = new LinkedHashMap<>();
                    Set<ResourcePropertyId> fourthSetOfResourcePropertyIds = new LinkedHashSet<>();

                    for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
                        fourthIdCounter.put(resourcePropertyId, new MutableInteger());
                    }
                    // show that resource values are reasonably unique
                    for (int i = 0; i < 10; i++) {
                        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator4);
                        fourthSetOfResourcePropertyIds.add(testResourcePropertyId);
                        fourthIdCounter.get(testResourcePropertyId).increment();
                    }

                    for (ResourcePropertyId resourcePropertyId : fourthIdCounter.keySet()) {
                        assertEquals(fourthIdCounter.get(resourcePropertyId).getValue(), 10);
                    }
                    assertEquals(fourthSetOfResourcePropertyIds.size(), 1);
                    break;

                case RESOURCE_5:
                    Map<ResourcePropertyId, MutableInteger> fifthIdCounter = new LinkedHashMap<>();
                    Set<ResourcePropertyId> fifthSetOfResourcePropertyIds = new LinkedHashSet<>();

                    for (ResourcePropertyId resourcePropertyId : TestResourcePropertyId.getTestResourcePropertyIds(testResourceId)) {
                        fifthIdCounter.put(resourcePropertyId, new MutableInteger());
                    }
                    // show that resource values are reasonably unique
                    for (int i = 0; i < 20; i++) {
                        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.getRandomResourcePropertyId(testResourceId, randomGenerator5);
                        fifthSetOfResourcePropertyIds.add(testResourcePropertyId);
                        fifthIdCounter.get(testResourcePropertyId).increment();
                    }

                    for (ResourcePropertyId resourcePropertyId : fifthIdCounter.keySet()) {
                        assertTrue(fifthIdCounter.get(resourcePropertyId).getValue() >= 5 && fifthIdCounter.get(resourcePropertyId).getValue() <= 15);
                    }
                    assertEquals(fifthSetOfResourcePropertyIds.size(), 2);
                    break;

                default:
                    throw new RuntimeException("Unhandled Case");
            }
        }
    }

    @Test
    @UnitTestMethod(name = "getRandomPropertyValue", args = {RandomGenerator.class})
    public void testGetRandomPropertyValue() {

    }

}