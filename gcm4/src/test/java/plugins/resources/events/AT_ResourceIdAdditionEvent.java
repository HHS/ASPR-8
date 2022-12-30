package plugins.resources.events;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import util.errors.ContractException;

@UnitTest(target = ResourceIdAdditionEvent.class)
public class AT_ResourceIdAdditionEvent {

    @Test
    @UnitTestConstructor(args = {ResourceId.class, TimeTrackingPolicy.class})
    public void testConstructor() {
        TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        // test case: null resource id
        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(null, timeTrackingPolicy));
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        // test case: null time tracking policy
        ContractException timeContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(testResourceId, null));
        assertEquals(timeContractException.getErrorType(), PropertyError.NULL_TIME_TRACKING_POLICY);
    }
}