package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchPropertyId;
import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.testsupport.TestBatchPropertyId;
import plugins.materials.testsupport.TestMaterialId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = BatchPropertyDefinitionEvent.class)
public class AT_BatchPropertyDefinitionEvent {
    @Test
    @UnitTestConstructor(args = { MaterialId.class, BatchPropertyId.class })
    public void testConstructor() {
        MaterialId materialId = TestMaterialId.MATERIAL_1;
        BatchPropertyId batchPropertyId = TestBatchPropertyId.BATCH_PROPERTY_1_2_INTEGER_MUTABLE_NO_TRACK;

        BatchPropertyDefinitionEvent event = new BatchPropertyDefinitionEvent(materialId, batchPropertyId);

        assertNotNull(event);

        // precondition: null material id
        ContractException contractException = assertThrows(ContractException.class,
                () -> new BatchPropertyDefinitionEvent(null, batchPropertyId));
        assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());

        // precondition: null property id
        contractException = assertThrows(ContractException.class,
                () -> new BatchPropertyDefinitionEvent(materialId, null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, contractException.getErrorType());
    }
}
