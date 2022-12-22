package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.materials.support.MaterialId;
import plugins.materials.support.MaterialsError;
import plugins.materials.testsupport.TestMaterialId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = MaterialIdAdditionEvent.class)
public class AT_MaterialIdAdditionEvent {

    @Test
    @UnitTestConstructor(args = { MaterialId.class })
    public void testConstructor() {
        MaterialId materialId = TestMaterialId.MATERIAL_1;
        MaterialIdAdditionEvent event = new MaterialIdAdditionEvent(materialId);

        assertNotNull(event);

        // precondition: null material id
        ContractException contractException = assertThrows(ContractException.class,
                () -> new MaterialIdAdditionEvent(null));
        assertEquals(MaterialsError.NULL_MATERIAL_ID, contractException.getErrorType());
    }
}
