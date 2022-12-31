package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

public class AT_MaterialsProducerPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(target = MaterialsProducerPropertyDefinitionEvent.class,args = { MaterialsProducerPropertyId.class })
    public void testConstructor() {

        // precondition: null producer property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> new MaterialsProducerPropertyDefinitionEvent(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
}
