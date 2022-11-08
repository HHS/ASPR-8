package plugins.globalproperties.testsupport;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.groups.support.GroupPropertyId;
import plugins.groups.testsupport.TestAuxiliaryGroupPropertyId;
import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Random;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = TestAuxiliaryGlobalPropertyId.class)
public class AT_TestAuxiliaryGlobalPropertyId {

    @Test
    @UnitTestMethod(name = "getRandomGlobalPropertyId", args = {RandomGenerator.class})
    void getRandomGlobalPropertyId() {
    }

    @Test
    @UnitTestMethod(name = "getRandomPropertyValue", args = {RandomGenerator.class})
    void testGetRandomPropertyValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(6173923848365818813L);
        /*
         * Show that randomly generated values are compatible with the
         * associated property definition. Show that the values are reasonably
         * unique
         */
        for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()) {
            PropertyDefinition propertyDefinition = testAuxiliaryGlobalPropertyId.getPropertyDefinition();
            Set<Object> values = new LinkedHashSet<>();
            for (int i = 0; i < 100; i++) {
                Object propertyValue = testAuxiliaryGlobalPropertyId.getRandomPropertyValue(randomGenerator);
                values.add(propertyValue);
                assertTrue(propertyDefinition.getType().isAssignableFrom(propertyValue.getClass()));
            }
            //show that the values are reasonable unique
            if (propertyDefinition.getType() != Boolean.class) {
                assertTrue(values.size() > 10);
            } else {
                assertEquals(2, values.size());
            }
        }
    }

    @Test
    @UnitTestMethod(name = "getPropertyDefinition", args = {})
    void testGetPropertyDefinition() {
        for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()){
            assertNotNull(testAuxiliaryGlobalPropertyId.getPropertyDefinition());
        }
    }

    @Test
    @UnitTestMethod(name = "getUnknownGlobalPropertyId", args = {})
    void testGetUnknownGlobalPropertyId() {
        /*
         * Shows that a generated unknown group property id is unique, not null
         * and not a member of the enum
         */
        Set<TestAuxiliaryGlobalPropertyId> testProperties = EnumSet.allOf(TestAuxiliaryGlobalPropertyId.class);
        Set<GlobalPropertyId> unknownGroupPropertyIds = new LinkedHashSet<>(); // ????
        for (int i = 0; i < 30; i++) {
            GlobalPropertyId unknownGlobalPropertyId = TestAuxiliaryGlobalPropertyId.getUnknownGlobalPropertyId();
            assertNotNull(unknownGlobalPropertyId);
            boolean unique = unknownGroupPropertyIds.add(unknownGlobalPropertyId);
            assertTrue(unique);
            assertFalse(testProperties.contains(unknownGlobalPropertyId));
        }
    }
}