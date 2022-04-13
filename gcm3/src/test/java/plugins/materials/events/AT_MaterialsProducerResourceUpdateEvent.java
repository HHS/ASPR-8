package plugins.materials.events;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import nucleus.EventLabel;
import nucleus.EventLabeler;
import nucleus.SimulationContext;
import plugins.materials.support.MaterialsProducerId;
import plugins.materials.testsupport.MaterialsActionSupport;
import plugins.materials.testsupport.TestMaterialsProducerId;
import plugins.resources.support.ResourceId;
import plugins.resources.testsupport.TestResourceId;
import tools.annotations.UnitTest;
import tools.annotations.UnitTestConstructor;
import tools.annotations.UnitTestMethod;

@UnitTest(target = MaterialsProducerResourceUpdateEvent.class)
public class AT_MaterialsProducerResourceUpdateEvent {

	@Test
	@UnitTestConstructor(args = { MaterialsProducerId.class, ResourceId.class, long.class, long.class })
	public void testConstructor() {
		//nothing to test
	}

	@Test
	@UnitTestMethod(name = "getPrimaryKeyValue", args = {})
	public void testGetPrimaryKeyValue() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, materialsProducerResourceUpdateEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(materialsProducerId, materialsProducerResourceUpdateEvent.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, materialsProducerResourceUpdateEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, materialsProducerResourceUpdateEvent.getPreviousResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, materialsProducerResourceUpdateEvent.getCurrentResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceUpdateEvent materialsProducerResourceUpdateEvent = new MaterialsProducerResourceUpdateEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		String expectedValue = "MaterialsProducerResourceUpdateEvent [materialsProducerId=MATERIALS_PRODUCER_3, resourceId=RESOURCE_4, previousResourceLevel=23, currentResourceLevel=346]";
		String actualValue = materialsProducerResourceUpdateEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByMaterialsProducerAndResource", args = { SimulationContext.class, MaterialsProducerId.class, ResourceId.class })
	public void testGetEventLabelByMaterialsProducerAndResource() {
		MaterialsActionSupport.testConsumer(7613656660266127922L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventLabel<MaterialsProducerResourceUpdateEvent> eventLabel = MaterialsProducerResourceUpdateEvent.getEventLabelByMaterialsProducerAndResource(c,
							testMaterialsProducerId, testResourceId);
					assertEquals(MaterialsProducerResourceUpdateEvent.class, eventLabel.getEventClass());
					assertEquals(testResourceId, eventLabel.getPrimaryKeyValue());
					assertEquals(MaterialsProducerResourceUpdateEvent.getEventLabelerForMaterialsProducerAndResource().getId(), eventLabel.getLabelerId());
				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForMaterialsProducerAndResource", args = {})
	public void testGetEventLabelerForMaterialsProducerAndResource() {
		MaterialsActionSupport.testConsumer(3426923014578127127L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<MaterialsProducerResourceUpdateEvent> eventLabeler = MaterialsProducerResourceUpdateEvent.getEventLabelerForMaterialsProducerAndResource();
			assertEquals(MaterialsProducerResourceUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					assertEquals(MaterialsProducerResourceUpdateEvent.getEventLabelByMaterialsProducerAndResource(c, testMaterialsProducerId,testResourceId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerResourceUpdateEvent event = new MaterialsProducerResourceUpdateEvent(testMaterialsProducerId, testResourceId, 45L,72L);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerResourceUpdateEvent> expectedEventLabel = MaterialsProducerResourceUpdateEvent.getEventLabelByMaterialsProducerAndResource(c,
							testMaterialsProducerId,testResourceId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerResourceUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByResource", args = { SimulationContext.class, ResourceId.class })
	public void testGetEventLabelByResource() {
		MaterialsActionSupport.testConsumer(6642554036399629036L, (c) -> {
			for (TestResourceId testResourceId : TestResourceId.values()) {
				EventLabel<MaterialsProducerResourceUpdateEvent> eventLabel = MaterialsProducerResourceUpdateEvent.getEventLabelByResource(c, testResourceId);
				assertEquals(MaterialsProducerResourceUpdateEvent.class, eventLabel.getEventClass());
				assertEquals(testResourceId, eventLabel.getPrimaryKeyValue());
				assertEquals(MaterialsProducerResourceUpdateEvent.getEventLabelerForResource().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForResource", args = {})
	public void testGetEventLabelerForResource() {
		
		MaterialsActionSupport.testConsumer(1027156783814158843L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<MaterialsProducerResourceUpdateEvent> eventLabeler = MaterialsProducerResourceUpdateEvent.getEventLabelerForResource();
			assertEquals(MaterialsProducerResourceUpdateEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					assertEquals(MaterialsProducerResourceUpdateEvent.getEventLabelByResource(c, testResourceId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerResourceUpdateEvent event = new MaterialsProducerResourceUpdateEvent(testMaterialsProducerId, testResourceId, 45L,72L);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerResourceUpdateEvent> expectedEventLabel = MaterialsProducerResourceUpdateEvent.getEventLabelByResource(c,
							testResourceId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerResourceUpdateEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

}
