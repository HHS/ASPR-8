package plugins.resources.events;

import org.junit.jupiter.api.Test;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = ResourcePropertyDefinitionEvent.class)
public class AT_ResourcePropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = {ResourceId.class, ResourcePropertyId.class})
    public void testConstructor() {
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(null, testResourcePropertyId));
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        ContractException propertyIdContractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(testResourceId, null));
        assertEquals(propertyIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);
    }

    @Test
    @UnitTestMethod(name = "getResourceId", args = {})
    public void testGetResourceId() {
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;
        ResourcePropertyDefinitionEvent resourcePropertyDefinitionEvent = new ResourcePropertyDefinitionEvent(testResourceId, testResourcePropertyId);

        assertNotNull(resourcePropertyDefinitionEvent.resourceId());
        assertEquals(resourcePropertyDefinitionEvent.resourceId(), testResourceId);
    }

    @Test
    @UnitTestMethod(name = "getResourcePropertyId", args = {})
    public void testGetResourcePropertyId() {
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;
        ResourcePropertyDefinitionEvent resourcePropertyDefinitionEvent = new ResourcePropertyDefinitionEvent(testResourceId, testResourcePropertyId);

        assertNotNull(resourcePropertyDefinitionEvent.resourcePropertyId());
        assertEquals(resourcePropertyDefinitionEvent.resourcePropertyId(), testResourcePropertyId);

    }

}