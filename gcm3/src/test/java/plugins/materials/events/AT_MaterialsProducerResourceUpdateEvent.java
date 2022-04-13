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

@UnitTest(target = MaterialsProducerResourceChangeObservationEvent.class)
public class AT_MaterialsProducerResourceChangeObservationEvent {

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
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, materialsProducerResourceChangeObservationEvent.getPrimaryKeyValue());
	}

	@Test
	@UnitTestMethod(name = "getMaterialsProducerId", args = {})
	public void testGetMaterialsProducerId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(materialsProducerId, materialsProducerResourceChangeObservationEvent.getMaterialsProducerId());
	}

	@Test
	@UnitTestMethod(name = "getResourceId", args = {})
	public void testGetResourceId() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(resourceId, materialsProducerResourceChangeObservationEvent.getResourceId());
	}

	@Test
	@UnitTestMethod(name = "getPreviousResourceLevel", args = {})
	public void testGetPreviousResourceLevel() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(previousResourceLevel, materialsProducerResourceChangeObservationEvent.getPreviousResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "getCurrentResourceLevel", args = {})
	public void testGetCurrentResourceLevel() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		assertEquals(currentResourceLevel, materialsProducerResourceChangeObservationEvent.getCurrentResourceLevel());
	}

	@Test
	@UnitTestMethod(name = "toString", args = {})
	public void testToString() {
		MaterialsProducerId materialsProducerId = TestMaterialsProducerId.MATERIALS_PRODUCER_3;
		ResourceId resourceId = TestResourceId.RESOURCE_4;
		long previousResourceLevel = 23L;
		long currentResourceLevel = 346L;
		MaterialsProducerResourceChangeObservationEvent materialsProducerResourceChangeObservationEvent = new MaterialsProducerResourceChangeObservationEvent(materialsProducerId, resourceId,
				previousResourceLevel, currentResourceLevel);
		String expectedValue = "MaterialsProducerResourceChangeObservationEvent [materialsProducerId=MATERIALS_PRODUCER_3, resourceId=RESOURCE_4, previousResourceLevel=23, currentResourceLevel=346]";
		String actualValue = materialsProducerResourceChangeObservationEvent.toString();
		assertEquals(expectedValue, actualValue);
	}

	@Test
	@UnitTestMethod(name = "getEventLabelByMaterialsProducerAndResource", args = { SimulationContext.class, MaterialsProducerId.class, ResourceId.class })
	public void testGetEventLabelByMaterialsProducerAndResource() {
		MaterialsActionSupport.testConsumer(7613656660266127922L, (c) -> {
			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					EventLabel<MaterialsProducerResourceChangeObservationEvent> eventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByMaterialsProducerAndResource(c,
							testMaterialsProducerId, testResourceId);
					assertEquals(MaterialsProducerResourceChangeObservationEvent.class, eventLabel.getEventClass());
					assertEquals(testResourceId, eventLabel.getPrimaryKeyValue());
					assertEquals(MaterialsProducerResourceChangeObservationEvent.getEventLabelerForMaterialsProducerAndResource().getId(), eventLabel.getLabelerId());
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
			EventLabeler<MaterialsProducerResourceChangeObservationEvent> eventLabeler = MaterialsProducerResourceChangeObservationEvent.getEventLabelerForMaterialsProducerAndResource();
			assertEquals(MaterialsProducerResourceChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					assertEquals(MaterialsProducerResourceChangeObservationEvent.getEventLabelByMaterialsProducerAndResource(c, testMaterialsProducerId,testResourceId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerResourceChangeObservationEvent event = new MaterialsProducerResourceChangeObservationEvent(testMaterialsProducerId, testResourceId, 45L,72L);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerResourceChangeObservationEvent> expectedEventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByMaterialsProducerAndResource(c,
							testMaterialsProducerId,testResourceId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerResourceChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
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
				EventLabel<MaterialsProducerResourceChangeObservationEvent> eventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId);
				assertEquals(MaterialsProducerResourceChangeObservationEvent.class, eventLabel.getEventClass());
				assertEquals(testResourceId, eventLabel.getPrimaryKeyValue());
				assertEquals(MaterialsProducerResourceChangeObservationEvent.getEventLabelerForResource().getId(), eventLabel.getLabelerId());
			}
		});
	}

	@Test
	@UnitTestMethod(name = "getEventLabelerForResource", args = {})
	public void testGetEventLabelerForResource() {
		
		MaterialsActionSupport.testConsumer(1027156783814158843L, (c) -> {
			// show that the event labeler can be constructed has the correct
			// values
			EventLabeler<MaterialsProducerResourceChangeObservationEvent> eventLabeler = MaterialsProducerResourceChangeObservationEvent.getEventLabelerForResource();
			assertEquals(MaterialsProducerResourceChangeObservationEvent.class, eventLabeler.getEventClass());

			for (TestMaterialsProducerId testMaterialsProducerId : TestMaterialsProducerId.values()) {
				for (TestResourceId testResourceId : TestResourceId.values()) {
					assertEquals(MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(c, testResourceId).getLabelerId(), eventLabeler.getId());

					// show that the event labeler produces the expected event
					// label

					// create an event
					MaterialsProducerResourceChangeObservationEvent event = new MaterialsProducerResourceChangeObservationEvent(testMaterialsProducerId, testResourceId, 45L,72L);

					// derive the expected event label for this event
					EventLabel<MaterialsProducerResourceChangeObservationEvent> expectedEventLabel = MaterialsProducerResourceChangeObservationEvent.getEventLabelByResource(c,
							testResourceId);

					// have the event labeler produce an event label and show it
					// is equal to the expected event label
					EventLabel<MaterialsProducerResourceChangeObservationEvent> actualEventLabel = eventLabeler.getEventLabel(c, event);
					assertEquals(expectedEventLabel, actualEventLabel);

				}
			}
		});
	}

}
