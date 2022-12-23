package plugins.regions.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.regions.support.RegionPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = RegionPropertyDefinitionEvent.class)
public class AT_RegionPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = { RegionPropertyId.class })
    public void testConstructor() {

        // precondition: region property id is null
        ContractException contractException = assertThrows(ContractException.class,
                () -> new RegionPropertyDefinitionEvent(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
}
