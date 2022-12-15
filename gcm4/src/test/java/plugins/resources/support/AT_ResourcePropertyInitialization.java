package plugins.resources.support;

import org.junit.jupiter.api.Test;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyDefinition;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = ResourcePropertyInitialization.class)
public class AT_ResourcePropertyInitialization {

    @Test
    @UnitTestMethod(name = "builder", args = {})
    public void testBuilder() {
        assertNotNull(ResourcePropertyInitialization.builder());
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "build", args = {})
    public void testBuild() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(1);
        builder.setResourceId(testResourceId);
        builder.setResourcePropertyId(testResourcePropertyId);
        ResourcePropertyInitialization resourcePropertyInitialization = builder.build();
        assertNotNull(resourcePropertyInitialization);
        assertEquals(resourcePropertyInitialization.getValue().get(), 1);
        assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
        assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
        assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);

        // precondition test: if property definition is not set
        builder.setResourceId(testResourceId);
        builder.setValue(2);
        builder.setResourcePropertyId(testResourcePropertyId);
        ContractException definitionContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(definitionContractException.getErrorType(), PropertyError.NULL_PROPERTY_DEFINITION);

        // precondition test: if property id is not set
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(3);
        builder.setResourceId(testResourceId);
        ContractException propertyIdContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(propertyIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);

        // precondition test: if resource id is not set
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue(4);
        builder.setResourcePropertyId(testResourcePropertyId);
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        // precondition test: if the value is not set
        builder.setPropertyDefinition(propertyDefinition);
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setResourceId(testResourceId);
        ContractException insufficientValueContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(insufficientValueContractException.getErrorType(), PropertyError.INSUFFICIENT_PROPERTY_VALUE_ASSIGNMENT);

        // precondition test: if the value in incompatible with the property definition
        builder.setPropertyDefinition(propertyDefinition);
        builder.setValue("this is a string");
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setResourceId(testResourceId);
        ContractException incompatibleContractException = assertThrows(ContractException.class, () -> builder.build());
        assertEquals(incompatibleContractException.getErrorType(), PropertyError.INCOMPATIBLE_VALUE);
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setResourceId", args = {ResourceId.class})
    public void testSetResourceId() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // precondition test: if resource id is null
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> builder
                .setPropertyDefinition(propertyDefinition)
                .setValue(6)
                .setResourcePropertyId(testResourcePropertyId)
                .setResourceId(null));
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        ResourcePropertyInitialization resourcePropertyInitialization = builder.setPropertyDefinition(propertyDefinition)
                .setValue(6)
                .setPropertyDefinition(propertyDefinition)
                .setResourcePropertyId(testResourcePropertyId)
                .setResourceId(testResourceId).build();

        assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setResourcePropertyId", args = {ResourcePropertyId.class})
    public void testSetResourcePropertyId() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // precondition test: if resource property id is null
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> builder
                .setPropertyDefinition(propertyDefinition)
                .setValue(7)
                .setResourceId(testResourceId)
                .setResourcePropertyId(null));
        assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);

        ResourcePropertyInitialization resourcePropertyInitialization = builder.setPropertyDefinition(propertyDefinition)
                .setValue(6)
                .setPropertyDefinition(propertyDefinition)
                .setResourcePropertyId(testResourcePropertyId)
                .setResourceId(testResourceId).build();

        assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setPropertyDefinition", args = {PropertyDefinition.class})
    public void testSetPropertyDefinition() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // precondition test: if the property definition is null
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> builder
                .setValue(8)
                .setResourceId(testResourceId)
                .setResourcePropertyId(testResourcePropertyId)
                .setPropertyDefinition(null));
        assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_DEFINITION);

        ResourcePropertyInitialization resourcePropertyInitialization = builder.setPropertyDefinition(propertyDefinition)
                .setValue(6)
                .setPropertyDefinition(propertyDefinition)
                .setResourcePropertyId(testResourcePropertyId)
                .setResourceId(testResourceId).build();

        assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);
    }

    @Test
    @UnitTestMethod(target = ResourcePropertyInitialization.Builder.class, name = "setValue", args = {Object.class})
    public void testSetValue() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // precondition test: if the value is null
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> builder
                .setResourceId(testResourceId)
                .setResourcePropertyId(testResourcePropertyId)
                .setPropertyDefinition(propertyDefinition)
                .setValue(null));
        assertEquals(resourceIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_VALUE);

        ResourcePropertyInitialization resourcePropertyInitialization = builder.setPropertyDefinition(propertyDefinition)
                .setValue(6)
                .setPropertyDefinition(propertyDefinition)
                .setResourcePropertyId(testResourcePropertyId)
                .setResourceId(testResourceId).build();

        assertEquals(resourcePropertyInitialization.getValue().get(), 6);
    }

    @Test
    @UnitTestMethod(name = "getResourcePropertyId", args = {})
    public void testGetResourcePropertyId() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        builder.setPropertyDefinition(propertyDefinition);
        builder.setResourceId(testResourceId);
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setValue(9);
        ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

        assertNotNull(resourcePropertyInitialization.getResourcePropertyId());
        assertEquals(resourcePropertyInitialization.getResourcePropertyId(), testResourcePropertyId);
    }

    @Test
    @UnitTestMethod(name = "getResourceId", args = {})
    public void testGetResourceId() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        builder.setPropertyDefinition(propertyDefinition);
        builder.setResourceId(testResourceId);
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setValue(10);
        ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

        assertNotNull(resourcePropertyInitialization.getResourceId());
        assertEquals(resourcePropertyInitialization.getResourceId(), testResourceId);
    }

    @Test
    @UnitTestMethod(name = "getPropertyDefinition", args = {})
    public void testGetPropertyDefinition() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        builder.setPropertyDefinition(propertyDefinition);
        builder.setResourceId(testResourceId);
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setValue(11);
        ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

        assertNotNull(resourcePropertyInitialization.getPropertyDefinition());
        assertEquals(resourcePropertyInitialization.getPropertyDefinition(), propertyDefinition);
    }

    @Test
    @UnitTestMethod(name = "getValue", args = {})
    public void testGetValue() {
        ResourcePropertyInitialization.Builder builder = ResourcePropertyInitialization.builder();
        PropertyDefinition propertyDefinition = PropertyDefinition.builder().setType(Integer.class).build();
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        builder.setPropertyDefinition(propertyDefinition);
        builder.setResourceId(testResourceId);
        builder.setResourcePropertyId(testResourcePropertyId);
        builder.setValue(12);
        ResourcePropertyInitialization resourcePropertyInitialization = builder.build();

        assertNotNull(resourcePropertyInitialization.getValue());
        assertEquals(resourcePropertyInitialization.getValue().get(), 12);
    }



}