package plugins.materials.events.mutation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import plugins.materials.support.BatchConstructionInfo;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.regions.support.RegionId;
import plugins.regions.testsupport.TestRegionId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import util.annotations.UnitTest;
import util.annotations.UnitTestConstructor;
import util.annotations.UnitTestMethod;

@UnitTest(target = ProducedResourceTransferToRegionEvent.class)
public final class AT_ProducedResourceTransferToRegionEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, ResourceId.class, RegionId.class, long.class })
	public void testConstructor() {
		// nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = { BatchConstructionInfo.class })
	public void testGetPrimaryKeyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		assertEquals(ProducedResourceTransferToRegionEvent.class, producedResourceTransferToRegionEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		String expectedValue = "ProducedResourceTransferToRegionEvent [materialsProducerId=MATERIALS_PRODUCER_2, resourceId=RESOURCE_3, regionId=REGION_4, amount=457]";
		String actualValue = producedResourceTransferToRegionEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		assertEquals(materialsProducerId, producedResourceTransferToRegionEvent.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		assertEquals(resourceId, producedResourceTransferToRegionEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getRegionId", args = {})
	public void testGetRegionId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		assertEquals(regionId, producedResourceTransferToRegionEvent.getRegionId());
	}

	@Test
	@UnitTestMethod(name = "getAmount", args = {})
	public void testGetAmount() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_2;
		ResourceId resourceId = TestResourceId.RESOURCE_3;
		RegionId regionId = TestRegionId.REGION_4;
		long amount = 457;
		ProducedResourceTransferToRegionEvent producedResourceTransferToRegionEvent = new ProducedResourceTransferToRegionEvent(materialsProducerId, resourceId, regionId, amount);
		assertEquals(amount, producedResourceTransferToRegionEvent.getAmount());
	}

}
