package plugins.globalproperties.testsupport;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.util.properties.PropertyDefinition;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.random.RandomGeneratorProvider;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static plugins.globalproperties.testsupport.TestAuxiliaryGlobalPropertyId.*;

@UnitTest(target = TestAuxiliaryGlobalPropertyId.class)
public class AT_TestAuxiliaryGlobalPropertyId {

    @Test
    @UnitTestMethod(name = "getRandomGlobalPropertyId", args = {RandomGenerator.class})
    public void testGetRandomGlobalPropertyId() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(5005107416828888981L);
        HashMap<TestAuxiliaryGlobalPropertyId, Integer> idCounter = new HashMap<>();
        Set<TestAuxiliaryGlobalPropertyId> hashSetOfRandomIds = new LinkedHashSet<>();
        idCounter.put(GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE, 0);
        idCounter.put(GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE, 0);
        idCounter.put(GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE, 0);
        idCounter.put(GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE, 0);
        idCounter.put(GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE, 0);
        idCounter.put(GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE, 0);

        // show that generated values are reasonably unique
        for (int i = 0; i < 600; i++) {
            TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId = TestAuxiliaryGlobalPropertyId.getRandomGlobalPropertyId(randomGenerator);
            hashSetOfRandomIds.add(testAuxiliaryGlobalPropertyId);
            switch(testAuxiliaryGlobalPropertyId) {
                case GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_1_BOOLEAN_MUTABLE) + 1);
                    break;
                case GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_2_INTEGER_MUTABLE) + 1);
                    break;
                case GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_3_DOUBLE_MUTABLE) + 1);
                    break;
                case GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_4_BOOLEAN_IMMUTABLE) + 1);
                    break;
                case GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_5_INTEGER_IMMUTABLE) + 1);
                    break;
                case GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE:
                    idCounter.put(GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE, idCounter.get(GLOBAL_AUX_PROPERTY_6_DOUBLE_IMMUTABLE) + 1);
                    break;
                default:
                    throw new RuntimeException("Unhandled Case");
            }
        }
        for (TestAuxiliaryGlobalPropertyId propertyId : idCounter.keySet()) {
            assertTrue(idCounter.get(propertyId) >= 30 && idCounter.get(propertyId) <= 150);
        }

        assertEquals(idCounter.values().stream().mapToInt(a -> a).sum(), 600);
        assertEquals(hashSetOfRandomIds.size(), 6);

    }

    @Test
    @UnitTestMethod(name = "getRandomPropertyValue", args = {RandomGenerator.class})
    public void testGetRandomPropertyValue() {
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
    public void testGetPropertyDefinition() {
        for (TestAuxiliaryGlobalPropertyId testAuxiliaryGlobalPropertyId : TestAuxiliaryGlobalPropertyId.values()){
            assertNotNull(testAuxiliaryGlobalPropertyId.getPropertyDefinition());
        }
    }

    @Test
    @UnitTestMethod(name = "getUnknownGlobalPropertyId", args = {})
    public void testGetUnknownGlobalPropertyId() {
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