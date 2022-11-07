package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = GlobalPropertyDefinitionEvent.class)
public class AT_GlobalPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = {})
    public void testGlobalPropertyId() {

        ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(null, 7));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    @UnitTestConstructor(args = {})
    void initialPropertyValue() {

        SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);

        ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(goodId, null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
}