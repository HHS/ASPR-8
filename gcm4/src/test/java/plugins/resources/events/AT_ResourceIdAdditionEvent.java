package plugins.resources.events;

import org.junit.jupiter.api.Test;
import plugins.resources.support.ResourceError;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import plugins.util.properties.PropertyError;
import plugins.util.properties.TimeTrackingPolicy;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;
import util.errors.ContractException;

import static org.junit.jupiter.api.Assertions.*;

@UnitTest(target = ResourceIdAdditionEvent.class)
public class AT_ResourceIdAdditionEvent {

    @Test
    @UnitTestConstructor(args = {ResourceId.class, TimeTrackingPolicy.class})
    public void testConstructor() {
        TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;

        ContractException resourceIdContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(null, timeTrackingPolicy));
        assertEquals(resourceIdContractException.getErrorType(), ResourceError.NULL_RESOURCE_ID);

        ContractException timeContractException = assertThrows(ContractException.class, () -> new ResourceIdAdditionEvent(testResourceId, null));
        assertEquals(timeContractException.getErrorType(), PropertyError.NULL_TIME_TRACKING_POLICY);
    }

    @Test
    @UnitTestMethod(name = "getTimeTrackingPolicy", args = {})
    public void testGetTimeTrackingPolicy() {
        TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;
        ResourceIdAdditionEvent resourceIdAdditionEvent = new ResourceIdAdditionEvent(testResourceId, timeTrackingPolicy);

        assertNotNull(resourceIdAdditionEvent.getTimeTrackingPolicy());
        assertEquals(resourceIdAdditionEvent.getTimeTrackingPolicy(), timeTrackingPolicy);
    }

    @Test
    @UnitTestMethod(name = "getResourceId", args = {})
    public void testGetResourceId() {
        TimeTrackingPolicy timeTrackingPolicy = TimeTrackingPolicy.TRACK_TIME;
        TestResourceId testResourceId = TestResourceId.RESOURCE_1;
        ResourceIdAdditionEvent resourceIdAdditionEvent = new ResourceIdAdditionEvent(testResourceId, timeTrackingPolicy);

        assertNotNull(resourceIdAdditionEvent.getResourceId());
        assertEquals(resourceIdAdditionEvent.getResourceId(), testResourceId);
    }


}