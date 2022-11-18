package plugins.globalproperties.support;

import org.junit.jupiter.api.Test;
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

        builder.setValue(5);
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId("firstTestId"));
        builder.setPropertyDefinition(propertyDefinition);
        assertNotNull(builder.build());

        // precondition test: if property id is not set
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(5);
        ContractException idContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.NULL_PROPERTY_ID ,idContractException.getErrorType());

        // precondition test: if property definition is not set
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId("secondTestId"));
        builder.setValue(6);
        ContractException propertyContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());

        // precondition test: if the property value is not set
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId("thirdTestId"));
        builder.setPropertyDefinition(propertyDefinition);
        ContractException valueContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT, valueContractException.getErrorType());

        // precondition test: if the property definition and value types are incompatible
        builder.setGlobalPropertyId(new SimpleGlobalPropertyId("fourthTestId"));
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(15.5f);
        ContractException incompatibleContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(PropertyError.INCOMPATIBLE_VALUE, incompatibleContractException.getErrorType());


    }

    @Test
    @UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setGlobalPropertyId", args = {GlobalPropertyId.class})
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
    @UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setPropertyDefinition", args = {PropertyDefinition.class})
    public void testGlobalPropertyDefinition() {
        // precondition test: if the property definition is null
        ContractException propertyContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(new SimpleGlobalPropertyId("fifthTestId"))
                .setValue(5)
                .setPropertyDefinition(null));
        assertEquals(PropertyError.NULL_PROPERTY_DEFINITION, propertyContractException.getErrorType());
    }

    @Test
    @UnitTestMethod(target = GlobalPropertyInitialization.Builder.class, name = "setValue", args = {Object.class})
    public void testSetValue() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();

        // precondition test: if the property value is null
        ContractException valueContractException = assertThrows(ContractException.class, () -> GlobalPropertyInitialization.builder()
                .setGlobalPropertyId(new SimpleGlobalPropertyId("sixthTestId"))
                .setPropertyDefinition(propertyDefinition)
                .setValue(null));
        assertEquals(PropertyError.NULL_PROPERTY_VALUE, valueContractException.getErrorType());
    }

    @Test
    @UnitTestMethod(name = "getGlobalPropertyId", args = {})
    public void testGetGlobalPropertyId() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
        GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("seventhTestId");
        Integer value = 6;

        builder.setGlobalPropertyId(globalPropertyId)
                        .setPropertyDefinition(propertyDefinition)
                        .setValue(value);
        GlobalPropertyInitialization globalPropertyInitialization = builder.build();

        assertNotNull(globalPropertyInitialization.getGlobalPropertyId());
        assertEquals(globalPropertyId, globalPropertyInitialization.getGlobalPropertyId());
    }

    @Test
    @UnitTestMethod(name = "getPropertyDefinition", args = {})
    public void testGetPropertyDefinition() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
        GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("eightTestId");
        Integer value = 6;

        builder.setGlobalPropertyId(globalPropertyId)
                .setPropertyDefinition(propertyDefinition)
                .setValue(value);
        GlobalPropertyInitialization globalPropertyInitialization = builder.build();

        assertNotNull(globalPropertyInitialization.getPropertyDefinition());
        assertEquals(propertyDefinition, globalPropertyInitialization.getPropertyDefinition());

    }

    @Test
    @UnitTestMethod(name = "getValue", args = {})
    public void testGetValue() {
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        GlobalPropertyInitialization.Builder builder = GlobalPropertyInitialization.builder();
        GlobalPropertyId globalPropertyId = new SimpleGlobalPropertyId("eightTestId");
        Integer value = 6;

        builder.setGlobalPropertyId(globalPropertyId)
                .setPropertyDefinition(propertyDefinition)
                .setValue(value);
        GlobalPropertyInitialization globalPropertyInitialization = builder.build();

        assertNotNull(globalPropertyInitialization.getValue());
        assertEquals(value, globalPropertyInitialization.getValue().get());
    }
}