package plugins.globalproperties.events;

import org.junit.jupiter.api.Test;
import plugins.globalproperties.support.GlobalPropertyId;
import plugins.globalproperties.support.SimpleGlobalPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = GlobalPropertyDefinitionEvent.class)
public class AT_GlobalPropertyDefinitionEvent {

    @Test
    public void testGlobalPropertyId() {
    //  SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);
        SimpleGlobalPropertyId badId = new SimpleGlobalPropertyId(null);
        Integer goodValue = 7;

        ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(badId, goodValue));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());

    }

    @Test
    void initialPropertyValue() {
        SimpleGlobalPropertyId goodId = new SimpleGlobalPropertyId(5);
        Integer badValue = null;

        ContractException contractException = assertThrows(ContractException.class, () -> new GlobalPropertyDefinitionEvent(goodId, badValue));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
}