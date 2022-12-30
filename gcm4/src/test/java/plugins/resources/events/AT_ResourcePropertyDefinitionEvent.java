package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.support.ResourcePropertyId;
import plugins.resources.testsupport.TestResourceId;
import plugins.resources.testsupport.TestResourcePropertyId;
import plugins.util.properties.PropertyError;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = ResourcePropertyDefinitionEvent.class)
public class AT_ResourcePropertyDefinitionEvent {

    @Test
    @UnitTestConstructor(args = {ResourceId.class, ResourcePropertyId.class})
    public void testConstructor() {
        TestResourcePropertyId testResourcePropertyId = TestResourcePropertyId.ResourceProperty_1_2_INTEGER_MUTABLE;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // test case: null resource id
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(null, testResourcePropertyId));
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        // test case: null property id
        ContractException propertyIdContractException = assertThrows(ContractException.class, () -> new ResourcePropertyDefinitionEvent(testResourceId, null));
        assertEquals(propertyIdContractException.getErrorType(), PropertyError.NULL_PROPERTY_ID);
    }
}