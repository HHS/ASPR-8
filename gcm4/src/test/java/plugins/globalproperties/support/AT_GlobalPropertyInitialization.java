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

        GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

        // precondition test: if property id is not set
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(5);
        ContractException idContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.NULL_PROPERTY_ID ,idContractException.getErrorType());

        // precondition test: if property definition is not set
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId(6));
        builder.setValue(6);
        ContractException propertyContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());

        // precondition test: if the property value is not set
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId(7));
        builder.setPropertyDefinition(propertyDefinition);
        ContractException valueContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, valueContractException.getErrorType());



    }

    @Test
    @UnitTestMethod(name = "setGlobalPropertyId", args = {})
    public void testSetGlobalPropertyId(){
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

        // precondition test: if the property id is null
        ContractException idContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setPropertyDefinition(propertyDefinition)
                .setValue(5)
                .setGlobalPropertyId(null));
        assertEquals(PropertyError.NULL_PROPERTY_ID, idContractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "setGlobalPropertyDefinition", args = {})
    public void testSetGlobalPropertyDefinition() {
        // precondition test: if the property definition is null
        ContractException propertyContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(new SimpleGlobalPropertyId(5))
                .setValue(5)
                .setPropertyDefinition(null));
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "setValue", args = {})
    public void testSetValue() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

        // precondition test: if the property value is null
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