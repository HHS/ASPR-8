package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;
import util.random.RandomGeneratorProvider;

@UnitTest(target = GroupPropertyValue.class)
public class AT_GroupPropertyValue {

    @Test
    @UnitTestConstructor(args = { GroupPropertyId.class, Object.class })
    public void testConstructor() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2797741161017158600L);
        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
        String value = Integer.toString(randomGenerator.nextInt(100));

        assertNotNull(new GroupPropertyValue(groupPropertyId, value));

        // precondition: null group property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> new GroupPropertyValue(null, value));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        // precondition: null value
        contractException = assertThrows(ContractException.class,
                () -> new GroupPropertyValue(groupPropertyId, null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException.getErrorType());

    }

    @Test
    @UnitTestMethod(name = "value", args = {})
    public void testValue() {
        RandomGenerator randomGenerator = RandomGeneratorProvider.getRandomGenerator(2467680260471076873L);

        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;

        List<Integer> expectedValues = new ArrayList<>();
        List<GroupPropertyValue> groupPropertyValues = new ArrayList<>();

        for (int i = 0; i < 15; i++) {
            int value = randomGenerator.nextInt(100);
            expectedValues.add(value);
            groupPropertyValues.add(new GroupPropertyValue(groupPropertyId, value));
        }

        assertEquals(expectedValues.size(), groupPropertyValues.size());

        for (int i = 0; i < 15; i++) {
            Object value = groupPropertyValues.get(i).value();
            assertEquals(expectedValues.get(i), value);
        }
    }

    @Test
    @UnitTestMethod(name = "groupPropertyId", args = {})
    public void testGroupPropertyId() {

        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
        int value = 100;
        GroupPropertyValue groupPropertyValue = new GroupPropertyValue(groupPropertyId, value);

        assertEquals(groupPropertyId, groupPropertyValue.groupPropertyId());

        groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_3_2_INTEGER_IMMUTABLE_NO_TRACK;
        value = 100;
        groupPropertyValue = new GroupPropertyValue(groupPropertyId, value);

        assertEquals(groupPropertyId, groupPropertyValue.groupPropertyId());

    }

    @Test
    @UnitTestMethod(name = "equals", args = { Object.class })
    public void testEquals() {
        GroupPropertyId groupPropertyId1 = TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK;
        boolean value1 = false;
        GroupPropertyId groupPropertyId2 = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
        int value2 = 100;

        GroupPropertyValue groupPropertyValue1 = new GroupPropertyValue(groupPropertyId1, value1);
        GroupPropertyValue groupPropertyValue1Clone = new GroupPropertyValue(groupPropertyId1, value1);
        GroupPropertyValue groupPropertyValue2 = new GroupPropertyValue(groupPropertyId2, value2);
        GroupPropertyValue groupPropertyValue2Clone = new GroupPropertyValue(groupPropertyId2, value2);

        assertNotEquals(groupPropertyValue1, groupPropertyValue2);
        assertEquals(groupPropertyValue1, groupPropertyValue1Clone);
        assertEquals(groupPropertyValue2, groupPropertyValue2Clone);

    }

    @Test
    @UnitTestMethod(name = "toString", args = {})
    public void testToString() {

        GroupPropertyId groupPropertyId = TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK;
        int value = 100;

        GroupPropertyValue groupPropertyValue = new GroupPropertyValue(groupPropertyId, value);

        String expectedString = "GroupPropertyValue[groupPropertyId=GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, value=100]";
        assertEquals(expectedString, groupPropertyValue.toString());

    }

    @Test
    @UnitTestMethod(name = "hashCode", args = {})
    public void testHashCode() {

        GroupPropertyValue groupPropertyValue1a = new GroupPropertyValue(
                TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);
        GroupPropertyValue groupPropertyValue1b = new GroupPropertyValue(
                TestGroupPropertyId.GROUP_PROPERTY_1_1_BOOLEAN_MUTABLE_NO_TRACK, false);

        assertEquals(groupPropertyValue1a.hashCode(), groupPropertyValue1b.hashCode());
        GroupPropertyValue groupPropertyValue2a = new GroupPropertyValue(
                TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 100);
        GroupPropertyValue groupPropertyValue2b = new GroupPropertyValue(
                TestGroupPropertyId.GROUP_PROPERTY_2_2_INTEGER_MUTABLE_TRACK, 100);

        assertEquals(groupPropertyValue2a.hashCode(), groupPropertyValue2b.hashCode());

    }
}
