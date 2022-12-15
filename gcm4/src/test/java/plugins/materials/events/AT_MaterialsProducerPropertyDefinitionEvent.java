package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialsProducerPropertyId;
import plugins.materials.testsupport.TestMaterialsProducerPropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

@UnitTest(target = MaterialsProducerPropertyDefinitionEvent.class)
public class AT_MaterialsProducerPropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = { MaterialsProducerPropertyId.class })
    public void testConstructor() {
        MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
        MaterialsProducerPropertyDefinitionEvent event = new MaterialsProducerPropertyDefinitionEvent(
                producerPropertyId);

        assertNotNull(event);
        // precondition: null producer property id
        ContractException contractException = assertThrows(ContractException.class,
                () -> new MaterialsProducerPropertyDefinitionEvent(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "getProducerPropertyId", args = {})
    public void testGetProducerPropertyId() {
        MaterialsProducerPropertyId producerPropertyId = TestMaterialsProducerPropertyId.MATERIALS_PRODUCER_PROPERTY_2_INTEGER_MUTABLE_NO_TRACK;
        MaterialsProducerPropertyDefinitionEvent event = new MaterialsProducerPropertyDefinitionEvent(
                producerPropertyId);

        assertNotNull(event);

        assertEquals(producerPropertyId, event.getProducerPropertyId());
    }
}
