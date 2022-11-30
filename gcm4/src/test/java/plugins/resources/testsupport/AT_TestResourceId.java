package plugins.resources.testsupport;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import tools.annotations.UnitTag;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;
import util.wrappers.MutableInteger;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = TestResourceId.class)
public class AT_TestResourceId {

    @Test
    @UnitTestMethod(name = "getTimeTrackingPolicy", args = {})
    public void testGetTimeTrackingPolicy() {
        for (TestResourceId testResourceId : TestResourceId.values()){
            assertNotNull(testResourceId.getTimeTrackingPolicy());
        }
    }

    @Test
    @UnitTestMethod(name = "getRandomResourceId", args = {RandomGenerator.class})
    public void testGetRandomResourceId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5357990509395444631L);
        Map<TestResourceId, MutableInteger> idCounter = new LinkedHashMap<>();
        Set<TestResourceId> setOfTestResourceIds = new LinkedHashSet<>();

        for (TestResourceId testResourceId : TestResourceId.values()) {
            idCounter.put(testResourceId, new MutableInteger());
        }

        for (int i = 0; i < 50; i++) {
            TestResourceId testResourceId = TestResourceId.getRandomResourceId(randomGenerator);
            setOfTestResourceIds.add(testResourceId);
            idCounter.get(testResourceId).increment();
        }

        for (TestResourceId testResourceId : idCounter.keySet()) {
            assertTrue(idCounter.get(testResourceId).getValue() >= 5 && idCounter.get(testResourceId).getValue() <= 30);
        }
        assertEquals(setOfTestResourceIds.size(), 5);
    }

    @Test
    @UnitTestMethod(name = "getUnknownResourceId", args = {})
    public void testGetUnknownResourceId() {
        assertNotNull(TestResourceId.getUnknownResourceId());
    }

    @Test
    @UnitTestMethod(name = "size", args = {})
    public void testSize() {
        assertNotNull(TestResourceId.size());
        assertEquals(TestResourceId.size(), 5);
    }

//    @Test
//    @UnitTestMethod(name = "next", args = {}, tags = UnitTag.INCOMPLETE)
//    public void testNext() {
//
//    }

}