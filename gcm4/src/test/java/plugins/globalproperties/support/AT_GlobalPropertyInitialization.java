package plugins.globalproperties.support;

import org.junit.jupiter.api.Test;
import plugins.groups.support.GroupConstructionInfo;
import plugins.groups.support.GroupError;
import plugins.groups.testsupport.TestGroupTypeId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target =GlobalPropertyInitialization.class)

public class AT_GlobalPropertyInitialization {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(GlobalPropertyInitialization.builder());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "build", args = {})
    public void testBuild() {
        // Setting property definition to use
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).setDefaultValue(true).build();

        // precondition test: if the property definition was not set(/is null?)
        ContractException propertyContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(new SimpleGlobalPropertyId(5))
                .setPropertyDefinition(null)
                .setValue(5));
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());

        // precondition test: if the property id was not set(/is null?)
        ContractException idContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(null)
                .setPropertyDefinition(propertyDefinition)
                .setValue(5));
        assertEquals(PropertyError.NULL_PROPERTY_ID, idContractException.getErrorType());

        // precondition test: if the property value was not set(/is null?
        ContractException valueContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(new SimpleGlobalPropertyId(6))
                .setPropertyDefinition(propertyDefinition)
                .setValue(null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, valueContractException.getErrorType());
    }

    
    @Test
    void getGlobalPropertyId() {

    }

    @Test
    void getPropertyDefinition() {
    }

    @Test
    void getValue() {
    }
}