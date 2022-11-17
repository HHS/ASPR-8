package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = GlobalPropertyDefinitionEvent.class)
public class AT_GlobalPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = {GlobalPropertyId.class, Object.class})
    public void testConstructor() {

        ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(null, 7));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

        SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);

        ContractException contractException2 = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(goodId, null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, contractException2.getErrorType());

    }


    @Test
    @UnitTestMethod(name = "equals", args = {Object.class})
    public void testEquals() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(name = "toString", args = {})
    public void testToString() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(name = "hashCode", args = {})
    public void testHashCode() {
        // nothing to test
    }

    @Test
    @UnitTestMethod(name = "initialPropertyValue", args = {})
    public void testInitialPropertyValue() {
        // nothing to test
    }


}