package plugins.resources.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.Event;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = InterRegionalResourceTransferEvent.class)
public final class AT_InterRegionalResourceTransferEvent implements Event {

	@Test
	@UnitTestConstructor(args = { ResourceId.class, RegionId.class, RegionId.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		RegionId sourceRegionId = TestRegionId.REGION_3; 
		RegionId destinationRegionId = TestRegionId.REGION_4;
		long amount =  245L;
		InterRegionalResourceTransferEvent interRegionalResourceTransferEvent = new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId,amount);
		assertEquals(resourceId, interRegionalResourceTransferEvent.getResourceId());	}

	@Test
	@UnitTestMethod(name = "getSourceRegionId", args = {})
	public void testGetSourceRegionId() {
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		RegionId sourceRegionId = TestRegionId.REGION_3; 
		RegionId destinationRegionId = TestRegionId.REGION_4;
		long amount =  245L;
		InterRegionalResourceTransferEvent interRegionalResourceTransferEvent = new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId,amount);
		assertEquals(sourceRegionId, interRegionalResourceTransferEvent.getSourceRegionId());
	}

	@Test
	@UnitTestMethod(name = "getDestinationRegionId", args = {})
	public void testGetDestinationRegionId() {
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		RegionId sourceRegionId = TestRegionId.REGION_3; 
		RegionId destinationRegionId = TestRegionId.REGION_4;
		long amount =  245L;
		InterRegionalResourceTransferEvent interRegionalResourceTransferEvent = new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId,amount);
		assertEquals(destinationRegionId, interRegionalResourceTransferEvent.getDestinationRegionId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		RegionId sourceRegionId = TestRegionId.REGION_3; 
		RegionId destinationRegionId = TestRegionId.REGION_4;
		long amount =  245L;
		InterRegionalResourceTransferEvent interRegionalResourceTransferEvent = new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId,amount);
		assertEquals(amount, interRegionalResourceTransferEvent.getAmount());
	}
	
	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		ResourceId resourceId = TestResourceId.RESOURCE_5;
		RegionId sourceRegionId = TestRegionId.REGION_3; 
		RegionId destinationRegionId = TestRegionId.REGION_4;
		long amount =  245L;
		InterRegionalResourceTransferEvent interRegionalResourceTransferEvent = new InterRegionalResourceTransferEvent(resourceId, sourceRegionId, destinationRegionId,amount);
		assertEquals(InterRegionalResourceTransferEvent.class, interRegionalResourceTransferEvent.getPrimaryKeyValue());
	}


}
