package plugins.groups.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.apache.commons.math3.random.RandomGenerator;
import org.junit.jupiter.api.Test;

import plugins.groups.testsupport.TestGroupPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
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

}
